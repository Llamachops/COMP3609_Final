import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;

public class Fairy implements Enemy {

    private static final int SCALED_WIDTH = 300;
    private static final int SCALED_HEIGHT = 225;
    private static final int ATTACK_COOLDOWN = 4000; // 5 seconds in milliseconds

    private int x, y; // Fairy's position
    private int hitboxWidth = 96;
    private int hitboxHeight = 96;

    private boolean facingLeft = true;

    private long lastAttackTime = 0;

    private HashMap<String, Animation> animations;
    private Animation currentAnimation;

    private TileMap tileMap;
    private Player player;

    private Point2D.Float controlPoint1, controlPoint2, startPoint, endPoint;
    private float t = 0; // Parameter for cubic Bezier curve
    private float health = 100; // Default health
    private boolean alive = true;
    private boolean dead = false;

    public Fairy(int x, int y, TileMap tileMap, Player player) {
        this.x = x;
        this.y = y;
        this.tileMap = tileMap;
        this.player = player;

        animations = new HashMap<>();
        loadAnimations();

        // Define the cubic Bezier curve points
        startPoint = new Point2D.Float(x, y);
        endPoint = startPoint;
        controlPoint1 = new Point2D.Float(x, y + 75);
        controlPoint2 = new Point2D.Float(x, y - 75);
    }

    private void loadAnimations() {
        animations.put("idle", createAnimation("images/fairy/fly/fly", 10, 100, true));
        animations.put("fly_attack", createAnimation("images/fairy/fly_attack/fly_attack", 10, 100, false));
        animations.put("die", createAnimation("images/fairy/die/die", 10, 100, false));
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
        // Update Bezier curve movement
        t += 0.01f;
        if (t > 1) {
            t = 0;
        }

        // Calculate the new position along the cubic Bezier curve
        float newX = (float) (Math.pow(1 - t, 3) * startPoint.x +
                3 * Math.pow(1 - t, 2) * t * controlPoint1.x +
                3 * (1 - t) * Math.pow(t, 2) * controlPoint2.x +
                Math.pow(t, 3) * endPoint.x);

        float newY = (float) (Math.pow(1 - t, 3) * startPoint.y +
                3 * Math.pow(1 - t, 2) * t * controlPoint1.y +
                3 * (1 - t) * Math.pow(t, 2) * controlPoint2.y +
                Math.pow(t, 3) * endPoint.y);

        x = (int) newX;
        y = (int) newY;

        if (!alive) {
            currentAnimation.update();
            if (!currentAnimation.isStillActive()) {
                dead = true;
            }
            return;
        }

        // Check if the player is in range
        int range = 400; // Range within which the fairy can attack
        int dx = player.getX() - x;
        int dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= range) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime >= ATTACK_COOLDOWN) {
                lastAttackTime = currentTime;
                fireProjectile();
                currentAnimation = animations.get("fly_attack");
                currentAnimation.start();
            }
        }

        // Update the animation
        if (currentAnimation == null || !currentAnimation.isStillActive()) {
            currentAnimation = animations.get("idle");
            currentAnimation.start();
        }

        currentAnimation.update();

        // Update facing direction
        facingLeft = player.getX() < x;
    }

    private void fireProjectile() {
        System.out.println("Fairy fires a projectile!");
        int projectileSpeed = 15; // Speed of the projectile
        int maxDistance = 700; // Maximum distance the projectile can travel

        // Create a new projectile
        Projectile projectile = new Projectile(x + (hitboxWidth / 2), y, player.getX(), player.getY(), tileMap,
                "images/collectibles/Light.png", projectileSpeed, maxDistance);
        tileMap.addProjectile(projectile);
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        if (currentAnimation == null) {
            currentAnimation = animations.get("idle");
            currentAnimation.start();
        }

        Image currentImage = currentAnimation.getImage();
        int hitboxCenterX = x + hitboxWidth / 2;
        int drawX = hitboxCenterX - SCALED_WIDTH / 2 + offsetX;
        int drawY = y - (SCALED_HEIGHT - hitboxHeight) / 2;

        if (facingLeft) {
            g2.drawImage(currentImage, drawX + SCALED_WIDTH, drawY, -SCALED_WIDTH, SCALED_HEIGHT, null);
        } else {
            g2.drawImage(currentImage, drawX, drawY, SCALED_WIDTH, SCALED_HEIGHT, null);
        }

        // Draw health bar
        int barWidth = 80; // Width of the health bar
        int barHeight = 8; // Height of the health bar
        int barX = x + (hitboxWidth - barWidth) / 2 + offsetX; // Center the bar below the enemy
        int barY = y + hitboxHeight + 5; // Position the bar just below the enemy

        // Draw the black background of the health bar
        g2.setColor(Color.BLACK);
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Draw the red health fill based on the enemy's current health
        int healthFillWidth = (int) ((health / 100.0f) * barWidth);
        g2.setColor(Color.RED);
        g2.fillRect(barX, barY, healthFillWidth, barHeight);

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
