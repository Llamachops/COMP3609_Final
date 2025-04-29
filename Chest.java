import java.awt.*;

public class Chest {
    private int x, y;
    private Rectangle hitbox;
    private boolean opened = false;
    private Image chestImage;
    private Player player;

    public Chest(int x, int y, Player player) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.hitbox = new Rectangle(x, y, TileMap.TILE_SIZE, TileMap.TILE_SIZE);
        this.chestImage = ImageManager.loadImage("images/collectibles/Chest_01_Locked.png");
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        if (!opened) {
            g2.drawImage(chestImage, x + offsetX, y+10, TileMap.TILE_SIZE, TileMap.TILE_SIZE, null);
        } else {
            this.chestImage = ImageManager.loadImage("images/collectibles/Chest_01_Unlocked.png");
            g2.drawImage(chestImage, x + offsetX, y+10, TileMap.TILE_SIZE, TileMap.TILE_SIZE, null);
        }
    }

    public void update() {        
        if (player.getHitbox().intersects(getHitbox())) {
            open();
            System.out.println("Chest collected!");
        }
    }
    
    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isOpened() {
        return opened;
    }

    public void open() {
        opened = true;
    }
}