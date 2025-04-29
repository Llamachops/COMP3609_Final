import java.awt.*;

public class Projectile {

    private int x, y;
    private int speed;
    private boolean active = true;
    private float directionX, directionY;

    private TileMap tileMap;

    private int startX, startY;
    private int maxDistance;
    private Image projectileImage;

    private static final int HITBOX_WIDTH = 30;
    private static final int HITBOX_HEIGHT = 30;
    private static final int IMAGE_WIDTH = 48;
    private static final int IMAGE_HEIGHT = 48;

    public Projectile(int x, int y, int targetX, int targetY, TileMap tileMap, String imagePath, int speed, int maxDistance) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.tileMap = tileMap;
        this.speed = speed;
        this.maxDistance = maxDistance;

        int dx = targetX - x;
        int dy = targetY - y;
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        this.directionX = (float) (dx / magnitude);
        this.directionY = (float) (dy / magnitude);

        this.projectileImage = ImageManager.loadImage(imagePath);
    }

    public void update() {
        if (!active) return;

        x += directionX * speed;
        y += directionY * speed;

        double traveledDistance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        if (traveledDistance >= maxDistance) {
            active = false;
            return;
        }

        if (tileMap.getPlayer().getHitbox().intersects(getHitbox())) {
            tileMap.changeNumLives(-1);
            active = false;
        }
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        if (!active) return;

        int drawX = x + offsetX + (HITBOX_WIDTH - IMAGE_WIDTH) / 2;
        int drawY = y + (HITBOX_HEIGHT - IMAGE_HEIGHT) / 2;

        g2.drawImage(projectileImage, drawX, drawY, IMAGE_WIDTH, IMAGE_HEIGHT, null);

        // Debug: Draw hitbox
        // TODO: Remove this in production
        g2.setColor(Color.RED);
        g2.drawRect(x + offsetX, y, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    public boolean isActive() {
        return active;
    }
}
