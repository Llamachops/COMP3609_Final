import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Troll {

    private static final int TILE_SIZE = 128;
    private static final int SCALED_WIDTH = 300;
    private static final int SCALED_HEIGHT = 300;
    private static final int SPEED = 4; // Troll's movement speed
    private static final int ATTACK_COOLDOWN = 1000; // 1 second cooldown in milliseconds

    private int x, y; // Troll's position
    private int hitboxWidth = 100;
    private int hitboxHeight = 128;

    private boolean facingLeft = true;
    private boolean isRunning = false;
    private boolean isAttacking = false;

    private long lastAttackTime = 0;

    private Animation idleAnimation;
    private Animation runAnimation;
    private Animation attackAnimation;
    private Animation currentAnimation;

    private TileMap tileMap;
    private Player player;

    public Troll(int x, int y, TileMap tileMap, Player player) {
        this.x = x;
        this.y = y;
        this.tileMap = tileMap;
        this.player = player;

        loadAnimations();
        currentAnimation = idleAnimation;
        currentAnimation.start();
    }

    private void loadAnimations() {
        idleAnimation = createAnimation("images/troll/idle/idle", 10, 100, true);
        runAnimation = createAnimation("images/troll/run/run", 10, 100, true);
        attackAnimation = createAnimation("images/troll/attack/attack", 10, 100, false);
    }

    private Animation createAnimation(String filePath, int numFrames, int duration, boolean loop) {
        Animation animation = new Animation(loop);
        for (int i = 1; i <= numFrames; i++) {
            String filename = filePath + i + ".png";
            Image originalImage = ImageManager.loadImage(filename);
            Image scaledImage = originalImage.getScaledInstance(SCALED_WIDTH, SCALED_HEIGHT, Image.SCALE_SMOOTH);
            animation.addFrame(scaledImage, duration);
        }
        return animation;
    }

    public void update() {
        if (isAttacking) {
            if (!currentAnimation.isStillActive()) {
                isAttacking = false;
                currentAnimation = idleAnimation;
                currentAnimation.start();
            }
            currentAnimation.update(); // Ensure the attack animation updates
            return;
        }

        Rectangle playerHitbox = player.getHitbox();
        Rectangle trollHitbox = getHitbox();

        if (trollHitbox.intersects(playerHitbox)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime >= ATTACK_COOLDOWN) {
                lastAttackTime = currentTime;
                isAttacking = true;
                currentAnimation = attackAnimation;
                currentAnimation.start();
                tileMap.changeNumLives(-1); // Reduce player's lives
            }
            currentAnimation.update(); // Ensure the attack animation updates
            return;
        }

        int playerX = player.getX();
        boolean shouldChase = Math.abs(playerX - x) <= TILE_SIZE * 5;
        boolean hasGroundBelow = checkGroundBelow();
        boolean isAtEdge = isAtEdge();

        if (shouldChase) {
            // Update facing direction based on player position
            facingLeft = playerX < x;

            // Only move if there's ground below and not at an edge
            if (hasGroundBelow && !isAtEdge) {
                isRunning = true;

                if (currentAnimation != runAnimation) {
                    currentAnimation = runAnimation;
                    currentAnimation.start();
                }

                int newX = facingLeft ? x - SPEED : x + SPEED;
                Point tilePos = collidesWithTile(newX, y);
                if (tilePos == null) {
                    x = newX;
                }
            } else {
                isRunning = false;
            }
        } else {
            isRunning = false;
        }

        // Switch to idle animation if not running
        if (!isRunning && currentAnimation != idleAnimation) {
            currentAnimation = idleAnimation;
            currentAnimation.start();
        }

        currentAnimation.update();
    }

    private boolean isAtEdge() {
        // Check if there's ground in front of the troll
        int checkX = facingLeft ? x - 1 : x + hitboxWidth + 1;
        int checkY = y + hitboxHeight + 1;

        int tileX = TileMap.pixelsToTiles(checkX);
        int tileY = TileMap.pixelsToTiles(checkY - TileMap.getOffsetY());

        return tileMap.getTile(tileX, tileY) == null;
    }

    private boolean checkGroundBelow() {
        // Check one tile below the troll's feet
        int checkX = x + hitboxWidth / 2; // Center of troll
        int checkY = y + hitboxHeight + 1; // Just below feet

        int tileX = TileMap.pixelsToTiles(checkX);
        int tileY = TileMap.pixelsToTiles(checkY - TileMap.getOffsetY());

        return tileMap.getTile(tileX, tileY) != null;
    }

    private Point collidesWithTile(int newX, int newY) {
        int offsetY = TileMap.getOffsetY();
        int xTile = TileMap.pixelsToTiles(newX);
        int yTile = TileMap.pixelsToTiles(newY - offsetY);

        if (tileMap.getTile(xTile, yTile) != null) {
            return new Point(xTile, yTile);
        }
        return null;
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        Image currentImage = currentAnimation.getImage();
        int hitboxCenterX = x + hitboxWidth / 2;
        int drawX = hitboxCenterX - SCALED_WIDTH / 2 + offsetX;
        int drawY = y - (SCALED_HEIGHT - hitboxHeight)/2 - 35;
        if (facingLeft) {
            g2.drawImage(currentImage, drawX + SCALED_WIDTH, drawY,
                    -SCALED_WIDTH, SCALED_HEIGHT, null);
        } else {
            g2.drawImage(currentImage, drawX, drawY,
                    SCALED_WIDTH, SCALED_HEIGHT, null);
        }

        // Debug: Draw hitbox
        g2.setColor(Color.RED);
        g2.drawRect(x + offsetX, y, hitboxWidth, hitboxHeight);
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, hitboxWidth, hitboxHeight);
    }
}