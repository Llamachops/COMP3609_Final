import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.Point;

public class Player {

	private static final int DX = 20; // amount of X pixels to move in one keystroke
	private static final int DY = 32; // amount of Y pixels to move in one keystroke
	private static final int TILE_SIZE = 128;
	private static final int SCALED_WIDTH = 270;
	private static final int SCALED_HEIGHT = 150;

	private JFrame window;
	private TileMap tileMap;
	private BackgroundManager bgManager;

	private int x;
	private int y;
	private int hitboxWidth = 32; // width of the hitbox
	private int hitboxHeight = 64; // height of the hitbox

    private HashMap<String, Animation> animations; // Store animations for actions
    private Animation currentAnimation;

	Graphics2D g2;
	private Dimension dimension;

	private Image playerImage, playerLeftImage, playerRightImage;

	private String currentState;
	private boolean facingLeft;
	private boolean jumping;
    private boolean movingLeft;
    private boolean movingRight;

	private int timeElapsed;
	private int startY;

	private boolean goingUp;
	private boolean goingDown;

	private boolean inAir;
	private int initialVelocity;

	public Player(JFrame window, TileMap t, BackgroundManager b) {
		this.window = window;

		tileMap = t; // tile map on which the player's sprite is displayed
		bgManager = b; // instance of BackgroundManager

		facingLeft = false;
		goingUp = goingDown = false;
		inAir = false;

		animations = new HashMap<>(); // Initialize the animations map
		loadAnimations(); // Load animations for different actions
		currentState = "idle"; // Set the default state to idle
		currentAnimation = animations.get(currentState); // Set the default animation to idle
		currentAnimation.start(); // Start the idle animation
		
	}

	public void loadAnimations() {
		// Load animations for different actions
		animations.put("idle", createAnimation("images/knight/idle/idle", 10, 100));
		animations.put("run", createAnimation("images/knight/run/run", 10, 100));
		animations.put("jump", createAnimation("images/knight/jump/jump", 10, 100));
		animations.put("hurt", createAnimation("images/knight/hurt/hurt", 10, 100));
		animations.put("attack", createAnimation("images/knight/attack/attack", 10, 100));
		animations.put("die", createAnimation("images/knight/die/die", 10, 100));
	}

	private Animation createAnimation(String filePath, int numFrames, int duration) {
		Animation animation = new Animation(true);
		for (int i = 1; i <= numFrames; i++) {
			String filename = filePath + i + ".png";
			Image originalImage = ImageManager.loadImage(filename);
			Image scaledImage = originalImage.getScaledInstance(SCALED_WIDTH, SCALED_HEIGHT, Image.SCALE_SMOOTH);
			animation.addFrame(scaledImage, duration);
		}
		return animation;
	}

	public Point collidesWithTile(int newX, int newY) {

		int playerWidth = hitboxWidth;
		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTile = tileMap.pixelsToTiles(newY - offsetY);

		if (tileMap.getTile(xTile, yTile) != null) {
			Point tilePos = new Point(xTile, yTile);
			return tilePos;
		} else {
			return null;
		}
	}

	public Point collidesWithTileDown(int newX, int newY) {

		int playerWidth = hitboxWidth;
		int playerHeight = hitboxHeight;
		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
		int yTileTo = tileMap.pixelsToTiles(newY - offsetY + playerHeight);

		for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
			if (tileMap.getTile(xTile, yTile) != null) {
				Point tilePos = new Point(xTile, yTile);
				return tilePos;
			} else {
				if (tileMap.getTile(xTile + 1, yTile) != null) {
					int leftSide = (xTile + 1) * TILE_SIZE;
					if (newX + playerWidth > leftSide) {
						Point tilePos = new Point(xTile + 1, yTile);
						return tilePos;
					}
				}
			}
		}

		return null;
	}

	public Point collidesWithTileUp(int newX, int newY) {

		int playerWidth = hitboxWidth;

		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);

		int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
		int yTileTo = tileMap.pixelsToTiles(newY - offsetY);

		for (int yTile = yTileFrom; yTile >= yTileTo; yTile--) {
			if (tileMap.getTile(xTile, yTile) != null) {
				Point tilePos = new Point(xTile, yTile);
				return tilePos;
			} else {
				if (tileMap.getTile(xTile + 1, yTile) != null) {
					int leftSide = (xTile + 1) * TILE_SIZE;
					if (newX + playerWidth > leftSide) {
						Point tilePos = new Point(xTile + 1, yTile);
						return tilePos;
					}
				}
			}

		}

		return null;
	}

	public synchronized void move(int direction) {

		int newX = x;
		Point tilePos = null;

		if (!window.isVisible())
			return;

		if (direction == 1) { // move left
			movingLeft = true;
			movingRight = false;
			facingLeft = true;
			currentAnimation = animations.get("run"); // Set animation to run
			newX = x - DX;
			if (newX < 0) {
				x = 0;
				return;
			}

			tilePos = collidesWithTile(newX, y);
		} else if (direction == 2) { // move right
			movingLeft = false;
			movingRight = true;
			facingLeft = false;
			currentAnimation = animations.get("run"); // Set animation to run
			int playerWidth = hitboxWidth;
			newX = x + DX;

			int tileMapWidth = tileMap.getWidthPixels();

			if (newX + hitboxWidth >= tileMapWidth) {
				x = tileMapWidth - hitboxWidth;
				return;
			}

			tilePos = collidesWithTile(newX + playerWidth, y);
		} else // jump
		if (direction == 3 && !jumping) {
			jumping = true;
			currentAnimation = animations.get("jump"); // Set animation to jump
			jump();
			return;
		}

		if (tilePos != null) {
			if (direction == 1) {
				System.out.println(": Collision going left");
				x = ((int) tilePos.getX() + 1) * TILE_SIZE; // keep flush with right side of tile
			} else if (direction == 2) {
				System.out.println(": Collision going right");
				int playerWidth = hitboxWidth;
				x = ((int) tilePos.getX()) * TILE_SIZE - playerWidth; // keep flush with left side of tile
			}
		} else {
			if (direction == 1) {
				x = newX;
				bgManager.moveLeft();
			} else if (direction == 2) {
				x = newX;
				bgManager.moveRight();
			}

			if (isInAir()) {
				System.out.println("In the air. Starting to fall.");
				if (direction == 1) { // make adjustment for falling on left side of tile
					int playerWidth = hitboxWidth;
					x = x - playerWidth + DX;
				}
				fall();
			}
		}
	}

	public boolean isInAir() {

		int playerHeight;
		Point tilePos;

		if (!jumping && !inAir) {
			playerHeight = hitboxHeight;
			tilePos = collidesWithTile(x, y + playerHeight + 1); // check below player to see if there is a tile

			if (tilePos == null) // there is no tile below player, so player is in the air
				return true;
			else // there is a tile below player, so the player is on a tile
				return false;
		}

		return false;
	}

	private void fall() {

		jumping = false;
		inAir = true;
		timeElapsed = 0;

		goingUp = false;
		goingDown = true;

		startY = y;
		initialVelocity = 0;
	}

	public void jump() {

		if (!window.isVisible())
			return;

		jumping = true;
		timeElapsed = 0;

		goingUp = true;
		goingDown = false;

		startY = y;
		initialVelocity = 70;
	}

	public void update() {
		int distance = 0;
		int newY = 0;

		timeElapsed++;
		String newState = currentState;
		if (jumping || inAir) {
			newState = "jump"; // Set animation to jump
			distance = (int) (initialVelocity * timeElapsed -
					4.9 * timeElapsed * timeElapsed);
			newY = startY - distance;

			if (newY > y && goingUp) {
				goingUp = false;
				goingDown = true;
			}

			if (goingUp) {
				Point tilePos = collidesWithTileUp(x, newY);
				if (tilePos != null) { // hits a tile going up
					System.out.println("Jumping: Collision Going Up!");

					int offsetY = tileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;
					int bottomTileY = topTileY + TILE_SIZE;

					y = bottomTileY;
					fall();
				} else {
					y = newY;
					// System.out.println("Jumping: No collision.");
				}
			} else if (goingDown) {
				Point tilePos = collidesWithTileDown(x, newY);
				if (tilePos != null) { // hits a tile going down
					System.out.println("Jumping: Collision Going Down!");
					int playerHeight = hitboxHeight;
					goingDown = false;

					int offsetY = tileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;

					y = topTileY - playerHeight;
					jumping = false;
					inAir = false;
				} else {
					y = newY;
					// System.out.println("Jumping: No collision.");
				}
			}
		} else if (movingLeft || movingRight) {
			newState = "run"; // Set animation to run
		} else {
			newState = "idle"; // Set animation to idle
		}
		if (!currentState.equals(newState)) {
			currentState = newState;
			currentAnimation = animations.get(currentState); // Set the new animation
			currentAnimation.start(); // Start the new animation
		}
		currentAnimation.update(); // Update the current animation
	}

	public void moveUp() {

		if (!window.isVisible())
			return;

		y = y - DY;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getHitboxWidth() {
		return hitboxWidth; // Return the width of the hitbox
	}

	public int getHitboxHeight() {
		return hitboxHeight; // Return the height of the hitbox
	}

	public boolean isFacingLeft() {
		return facingLeft; // Return whether the player is facing left
	}

	public Image getCurrentAnimImage() {
		return currentAnimation.getImage(); // Get the current animation image
	}

	public void setNotMovingHorizontal() {
		movingLeft = false;
		movingRight = false;
	}

	public Rectangle getHitbox() {
		return new Rectangle(x, y, hitboxWidth, hitboxHeight); // Return the hitbox as a rectangle
	}

}