import java.awt.*;
import java.util.HashMap;

public class Troll implements Enemy {

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

    private HashMap<String, Animation> animations;
    private Animation currentAnimation;

    private TileMap tileMap;
    private Player player;
    private float health = 100; // Default health
    private boolean alive = true;
    private boolean dead = false;

    public Troll(int x, int y, TileMap tileMap, Player player) {
        this.x = x;
        this.y = y;
        this.tileMap = tileMap;
        this.player = player;

        animations = new HashMap<>();
        loadAnimations();
    }

    private void loadAnimations() {
        animations.put("idle", createAnimation("images/troll/idle/idle", 10, 100, true));
        animations.put("run", createAnimation("images/troll/run/run", 10, 100, true));
        animations.put("attack", createAnimation("images/troll/attack/attack", 10, 100, false));
        animations.put("die", createAnimation("images/troll/die/die", 10, 100, false));
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

    public void update() {
        if (!alive) {
            currentAnimation.update();
            if (!currentAnimation.isStillActive()) {
                dead = true;
            }
            return;
        }

        if (isAttacking) {
            if (!currentAnimation.isStillActive()) {
                isAttacking = false;
                currentAnimation = animations.get("idle");
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
                currentAnimation = animations.get("attack");
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

                if (currentAnimation != animations.get("run")) {
                    currentAnimation = animations.get("run");
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
        if (currentAnimation == null || (!isRunning && currentAnimation != animations.get("idle"))) {
            currentAnimation = animations.get("idle");
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
        if (currentAnimation == null) {
            currentAnimation = animations.get("idle");
            currentAnimation.start();
        }
        ;
        Image currentImage = currentAnimation.getImage();
        int hitboxCenterX = x + hitboxWidth / 2;
        int drawX = hitboxCenterX - SCALED_WIDTH / 2 + offsetX;
        int drawY = y - (SCALED_HEIGHT - hitboxHeight) / 2 - 35;
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

    @Override
    public void takeDamage(float damage) {
        if (!alive || dead)
            return;

        health -= damage;
        if (health <= 0 && alive) {
            health = 0;
            alive = false;
            currentAnimation = animations.get("die");
            currentAnimation.start();
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle(x, y, hitboxWidth, hitboxHeight);
    }
}