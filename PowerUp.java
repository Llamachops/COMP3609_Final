import java.awt.*;

public class PowerUp {
    private int x, y;
    private Rectangle hitbox;
    private Image image;
    private PowerUpType type;

    public enum PowerUpType {
        DAMAGE, CRIT_CHANCE, CRIT_DAMAGE, LIVES
    }

    public PowerUp(int x, int y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.hitbox = new Rectangle(x, y, TileMap.TILE_SIZE, TileMap.TILE_SIZE);

        // Load the appropriate image based on the power-up type
        switch (type) {
            case DAMAGE:
                this.image = ImageManager.loadImage("images/collectibles/Sword.png");
                break;
            case CRIT_CHANCE:
                this.image = ImageManager.loadImage("images/collectibles/Star.png");
                break;
            case CRIT_DAMAGE:
                this.image = ImageManager.loadImage("images/collectibles/Diamond.png");
                break;
            case LIVES:
                this.image = ImageManager.loadImage("images/collectibles/Life.png");
                break;
        }
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        g2.drawImage(image, x + offsetX, y + 10, TileMap.TILE_SIZE, TileMap.TILE_SIZE, null);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public PowerUpType getType() {
        return type;
    }
}