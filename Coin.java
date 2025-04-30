import java.awt.*;

public class Coin {

    private static final int TILE_SIZE = 128;
    private Animation animation;
    private int x, y; // Position of the coin
    private boolean collected; // Whether the coin has been collected
    private Rectangle hitbox; // Hitbox for collision detection

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
        this.collected = false;

        // Initialize the animation
        animation = new Animation(true);
        loadAnimation();

        // Create the hitbox
        hitbox = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void loadAnimation() {
        for (int i = 1; i <= 6; i++) {
            String filePath = String.format("images/coin/coin_%d.png", i);
            Image frame = ImageManager.loadImage(filePath);
            animation.addFrame(frame, 100); // Each frame lasts 100ms
        }
        animation.start();
    }

    public void update() {
        if (!collected) {
            animation.update();
        }
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        if (!collected) {
            g2.drawImage(animation.getImage(), x + offsetX, y+10, TileMap.TILE_SIZE, TileMap.TILE_SIZE, null);
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public Animation getAnimation() {
        return animation;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
