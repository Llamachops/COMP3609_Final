import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import java.awt.Image;
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

	private boolean isAttacking = false;
	private int attackDamage = 25; // Default damage
	private long attackCooldown = 500; // Cooldown in milliseconds
	private long lastAttackTime = 0;
	private Rectangle attackHitbox = new Rectangle();
	private float critMultiplier = 2.0f;
	private float critChance = 0.2f;

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
		animations.put("idle", createAnimation("images/knight/idle/idle", 10, 100, true));
		animations.put("run", createAnimation("images/knight/run/run", 10, 100, true));
		animations.put("jump", createAnimation("images/knight/jump/jump", 10, 100, false));
		animations.put("hurt", createAnimation("images/knight/hurt/hurt", 10, 100, false));
		animations.put("attack", createAnimation("images/knight/attack/attack", 10, 50, false));
		animations.put("die", createAnimation("images/knight/die/die", 10, 100, false));
	}

	private Animation createAnimation(String filePath, int numFrames, int duration, boolean loop) {
		Animation animation = new Animation(loop);
		for (int i = 1; i <= numFrames; i++) {
			String filename = filePath + i + ".png";
			Image originalImage = ImageManager.loadImage(filename);
			Image scaledImage = originalImage.getScaledInstance(SCALED_WIDTH, SCALED_HEIGHT, Image.SCALE_SMOOTH);

			// Force the image to load into memory to prevent flickering
			scaledImage.getWidth(null);
			scaledImage.getHeight(null);

			animation.addFrame(scaledImage, duration);
		}
		return animation;
	}

	public Point collidesWithTile(int newX, int newY) {

		int offsetY = TileMap.getOffsetY();
		int xTile = TileMap.pixelsToTiles(newX);
		int yTile = TileMap.pixelsToTiles(newY - offsetY);

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
		int offsetY = TileMap.getOffsetY();
		int xTile = TileMap.pixelsToTiles(newX);
		int yTileFrom = TileMap.pixelsToTiles(y - offsetY);
		int yTileTo = TileMap.pixelsToTiles(newY - offsetY + playerHeight);

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

		int offsetY = TileMap.getOffsetY();
		int xTile = TileMap.pixelsToTiles(newX);

		int yTileFrom = TileMap.pixelsToTiles(y - offsetY);
		int yTileTo = TileMap.pixelsToTiles(newY - offsetY);

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
		if (jumping || inAir) {
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
					// System.out.println("Jumping: Collision Going Up!");

					int offsetY = TileMap.getOffsetY();
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
					// System.out.println("Jumping: Collision Going Down!");
					int playerHeight = hitboxHeight;
					goingDown = false;

					int offsetY = TileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;

					y = topTileY - playerHeight;
					jumping = false;
					inAir = false;
				} else {
					y = newY;
					// System.out.println("Jumping: No collision.");
				}
			}
		}

		if (isAttacking) {
			if (!animations.get("attack").isStillActive()) {
				isAttacking = false;
			} else {
				currentAnimation.update();
				return;
			}
		}

		String newState;
		if (jumping || inAir) {
			newState = "jump";
		} else if (movingLeft || movingRight) {
			newState = "run"; // Set animation to run
		} else {
			newState = "idle"; // Set animation to idle
		}

		if (!newState.equals(currentState)) {
			currentState = newState;
			currentAnimation = animations.get(currentState); // Set the new animation
			currentAnimation.start(); // Start the new animation
		}

		if (currentAnimation.isStillActive()) {
			currentAnimation.update(); // Update the current animation
		} else {
			currentAnimation.start(); // Restart the animation if it has finished
		}
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

	public void attack(ArrayList<Enemy> enemies) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastAttackTime < attackCooldown) {
			return; // Prevent attacking if still on cooldown
		}

		lastAttackTime = currentTime;
		isAttacking = true;
		currentState = "attack";
		currentAnimation = animations.get(currentState);
		currentAnimation.start();

		// Define the attack hitbox
		int attackWidth = 150; // Length of the attack range
		int attackHeight = 50; // Height of the attack range
		int attackX = isFacingLeft() ? getX() - attackWidth : getX() + getHitboxWidth();
		int attackY = getY() + (getHitboxHeight() - attackHeight) / 2;

		attackHitbox.setBounds(attackX, attackY, attackWidth, attackHeight);

		// Check for enemies in range
		for (Enemy enemy : enemies) {
			if (enemy.isAlive() && attackHitbox.intersects(enemy.getHitbox())) {
				// Calculate damage with a 20% chance for critical hit
				float damage = attackDamage;
				if (Math.random() < critChance) {
					damage *= critMultiplier; // Critical hit
				}
				enemy.takeDamage(damage);
			}
		}
	}

	public void draw(Graphics2D g2, int offsetX, int offsetY) {
		Image playerAnimImage = currentAnimation.getImage();
		int animWidth = playerAnimImage.getWidth(null);
		int animHeight = playerAnimImage.getHeight(null);

		int drawX = x + (hitboxWidth - animWidth) / 2 + offsetX;
		int drawY = y + (hitboxHeight - animHeight) / 2 + 10;

		if (facingLeft) {
			g2.drawImage(playerAnimImage, drawX + animWidth, drawY, -animWidth, animHeight, null);
		} else {
			g2.drawImage(playerAnimImage, drawX, drawY, animWidth, animHeight, null);
		}

		// Draw player hitbox for debugging
		g2.setColor(Color.BLUE);
		g2.drawRect(
				x + offsetX, // Apply horizontal scroll offset
				y, // No vertical scroll offset needed for player
				hitboxWidth,
				hitboxHeight);

		// Draw the attack effect as a moving gray triangle
		if (isAttacking) {
			// Calculate the progress of the attack animation (0.0 to 1.0)
			float progress = (float) currentAnimation.getCurrentFrameIndex() / currentAnimation.getNumFrames();

			// Calculate the triangle's position based on the progress
			int triangleXStart = attackHitbox.x + offsetX;
			int triangleXEnd = attackHitbox.x + attackHitbox.width + offsetX;
			int triangleX;

			if (facingLeft) {
				// Move the triangle from right to left when facing left
				triangleX = (int) (triangleXEnd - progress * (triangleXEnd - triangleXStart));
			} else {
				// Move the triangle from left to right when facing right
				triangleX = (int) (triangleXStart + progress * (triangleXEnd - triangleXStart));
			}

			int triangleYTop = attackHitbox.y;
			int triangleYBottom = attackHitbox.y + attackHitbox.height;

			// Draw the triangle
			g2.setColor(Color.GRAY);
			int[] xPoints;
			int[] yPoints = { triangleYTop, triangleYBottom, (triangleYTop + triangleYBottom) / 2 };

			if (facingLeft) {
				xPoints = new int[] { triangleX, triangleX, triangleX - 20 }; // Triangle points for left-facing attack
			} else {
				xPoints = new int[] { triangleX, triangleX, triangleX + 20 }; // Triangle points for right-facing attack
			}

			g2.fillPolygon(xPoints, yPoints, 3);
		}
	}

	public int getAttackDamage() {
		return attackDamage;
	}

	public float getCritChance() {
		return critChance; // Assume this is a float value between 0.0 and 1.0
	}

	public float getCritMultiplier() {
		return critMultiplier; // Assume this is a float value (e.g., 2.0 for double damage)
	}

	public void increaseAttackDamage(int amount) {
	    attackDamage += amount;
	}

	public void increaseCritChance(float amount) {
	    critChance = Math.min(critChance + amount, 1.0f); // Cap at 100%
	}

	public void increaseCritMultiplier(float amount) {
	    critMultiplier += amount;
	}
	
	public void resetStats() {
	    attackDamage = 25; // Reset to default damage
	    critChance = 0.2f; // Reset to default critical chance
	    critMultiplier = 2.0f; // Reset to default critical multiplier
	}

	public void setTileMap(TileMap t) {
		this.tileMap = t; // Set the tile map
	}

	public void setBackgroundManager(BackgroundManager b) {
		this.bgManager = b; // Set the background manager
	}
}