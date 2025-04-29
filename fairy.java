import java.awt.*;
import java.awt.geom.Point2D;

public class Fairy {

    private static final int TILE_SIZE = 128;
    private static final int SCALED_WIDTH = 300;
    private static final int SCALED_HEIGHT = 225;
    private static final int ATTACK_COOLDOWN = 4000; // 5 seconds in milliseconds

    private int x, y; // Fairy's position
    private int hitboxWidth = 96;
    private int hitboxHeight = 96;

    private boolean facingLeft = true;

    private long lastAttackTime = 0;

    private Animation idleAnimation;
    private Animation attackAnimation;
    private Animation currentAnimation;

    private TileMap tileMap;
    private Player player;

    private Point2D.Float controlPoint1, controlPoint2, startPoint, endPoint;
    private float t = 0; // Parameter for cubic Bezier curve
    private boolean movingUp = true;

    public Fairy(int x, int y, TileMap tileMap, Player player) {
        this.x = x;
        this.y = y;
        this.tileMap = tileMap;
        this.player = player;

        loadAnimations();
        currentAnimation = idleAnimation;
        currentAnimation.start();

        // Define the cubic Bezier curve points
        startPoint = new Point2D.Float(x, y);
        endPoint = new Point2D.Float(x, y + 50); // Move 50 pixels down
        controlPoint1 = new Point2D.Float(x - 30, y + 25); // Control points for the curve
        controlPoint2 = new Point2D.Float(x + 30, y + 25);
    }

    private void loadAnimations() {
        idleAnimation = createAnimation("images/fairy/fly/fly", 10, 100, true);
        attackAnimation = createAnimation("images/fairy/fly_attack/fly_attack", 10, 100, false);
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
        // Update Bezier curve movement
        if (movingUp) {
            t += 0.01f; // Move up
            if (t >= 1) {
                t = 1;
                movingUp = false;
            }
        } else {
            t -= 0.01f; // Move down
            if (t <= 0) {
                t = 0;
                movingUp = true;
            }
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
                currentAnimation = attackAnimation;
                currentAnimation.start();
            }
        }

        // Update the animation
        if (!currentAnimation.isStillActive()) {
            currentAnimation = idleAnimation;
            currentAnimation.start();
        }

        currentAnimation.update();

        // Update facing direction
        facingLeft = player.getX() < x;
    }

    private void fireProjectile() {
        System.out.println("Fairy fires a projectile!");
        int projectileSpeed = 20; // Speed of the projectile
        int maxDistance = 700; // Maximum distance the projectile can travel

        // Create a new projectile
        Projectile projectile = new Projectile(x, y, player.getX(), player.getY(), tileMap, "images/collectibles/Light.png", projectileSpeed, maxDistance);
        tileMap.addProjectile(projectile);
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        Image currentImage = currentAnimation.getImage();
        int hitboxCenterX = x + hitboxWidth / 2;
        int drawX = hitboxCenterX - SCALED_WIDTH / 2 + offsetX;
        int drawY = y - (SCALED_HEIGHT - hitboxHeight)/2;
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
