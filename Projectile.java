import java.awt.*;

public class Projectile {

    private int x, y;
    private int targetX, targetY;
    private int speed;
    private boolean active = true;
    private float directionX, directionY; // Direction vector
    
    private TileMap tileMap;

    private int startX, startY; // Starting position of the projectile
    private int maxDistance; // Maximum distance the projectile can travel
    private Image projectileImage;

    public Projectile(int x, int y, int targetX, int targetY, TileMap tileMap, String imagePath, int speed, int maxDistance) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.targetX = targetX;
        this.targetY = targetY;
        this.tileMap = tileMap;
        this.speed = speed;
        this.maxDistance = maxDistance;

        // Calculate the direction vector
        int dx = targetX - x;
        int dy = targetY - y;
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        this.directionX = (float) (dx / magnitude);
        this.directionY = (float) (dy / magnitude);

        // Load the projectile image
        this.projectileImage = ImageManager.loadImage(imagePath);
    }

    public void update() {
        if (!active) return;

        // Move the projectile in the direction of the normalized vector
        x += directionX * speed;
        y += directionY * speed;

        // Check if the projectile has traveled beyond its maximum distance
        double traveledDistance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        if (traveledDistance >= maxDistance) {
            active = false; // Mark the projectile as inactive
            return;
        }

        // Check for collision with the player
        if (tileMap.getPlayer().getHitbox().intersects(getHitbox())) {
            tileMap.changeNumLives(-1); // Damage the player
            active = false; // Deactivate the projectile
            return;
        }

        // Check if the projectile is out of bounds
        if (x < 0 || x > tileMap.getWidthPixels() || y < 0 || y > tileMap.getHeight() * TileMap.TILE_SIZE) {
            active = false; // Mark the projectile as inactive
        }
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        if (!active) return;

        g2.drawImage(projectileImage, x + offsetX, y, 48, 48, null); // Draw the projectile
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, 10, 10);
    }

    public boolean isActive() {
        return active;
    }
}