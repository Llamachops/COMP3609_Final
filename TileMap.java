import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.swing.JFrame;

/**
 * The TileMap class contains the data for a tile-based
 * map, including Sprites. Each tile is a reference to an
 * Image. Images are used multiple times in the tile map.
 * map.
 */

public class TileMap {

    private static final int TILE_SIZE = 128;
    private static final int TILE_SIZE_BITS = 6;

    private Image[][] tiles;

    private ArrayList<Coin> coins; // List of coins in the map
    private int coinCounter = 0; // Counter for collected coins

    private int screenWidth, screenHeight;
    private int mapWidth, mapHeight;
    private static int offsetY;

    private Player player;

    BackgroundManager bgManager;

    private JFrame window;
    private Dimension dimension;

    /**
     * Creates a new TileMap with the specified width and
     * height (in number of tiles) of the map.
     */
    public TileMap(JFrame window, int width, int height) {

        this.window = window;
        dimension = window.getSize();

        screenWidth = dimension.width;
        screenHeight = dimension.height;

        mapWidth = width;
        mapHeight = height;

        // get the y offset to draw all sprites and tiles

        offsetY = screenHeight - tilesToPixels(mapHeight);
        System.out.println("screenWidth: " + screenWidth);
        System.out.println("screenHeight: " + screenHeight);
        System.out.println("mapWidth: " + mapWidth);
        System.out.println("mapHeight: " + mapHeight);
        System.out.println("offsetY: " + offsetY);

        bgManager = new BackgroundManager(window, 12);

        coins = new ArrayList<>();

        tiles = new Image[mapWidth][mapHeight];
        player = new Player(window, this, bgManager);
        // sprites = new LinkedList();

        int playerHeight = player.getHitboxHeight();

        int x, y;
        // x = (dimension.width / 2) + TILE_SIZE; // position player in middle of screen

        x = 192; // position player in 'random' location
        y = tilesToPixels(mapHeight) - TILE_SIZE - playerHeight;

        player.setX(x);
        player.setY(y);

        System.out.println("Player coordinates: " + x + "," + y);

    }

    /**
     * Gets the width of this TileMap (number of pixels across).
     */
    public int getWidthPixels() {
        return tilesToPixels(mapWidth);
    }

    /**
     * Gets the width of this TileMap (number of tiles across).
     */
    public int getWidth() {
        return mapWidth;
    }

    /**
     * Gets the height of this TileMap (number of tiles down).
     */
    public int getHeight() {
        return mapHeight;
    }

    public static int getOffsetY() {
        return offsetY;
    }

    public int getCoinCounter() {
        return coinCounter;
    }

    public void addCoin(Coin coin) {
        coins.add(coin);
    }

    /**
     * Gets the tile at the specified location. Returns null if
     * no tile is at the location or if the location is out of
     * bounds.
     */
    public Image getTile(int x, int y) {
        if (x < 0 || x >= mapWidth ||
                y < 0 || y >= mapHeight) {
            return null;
        } else {
            return tiles[x][y];
        }
    }

    /**
     * Sets the tile at the specified location.
     */
    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }

    /**
     * Class method to convert a pixel position to a tile position.
     */

    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }

    /**
     * Class method to convert a pixel position to a tile position.
     */

    public static int pixelsToTiles(int pixels) {
        return (int) Math.floor((float) pixels / TILE_SIZE);
    }

    /**
     * Class method to convert a tile position to a pixel position.
     */

    public static int tilesToPixels(int numTiles) {
        return numTiles * TILE_SIZE;
    }

    /**
     * Draws the specified TileMap.
     */
    public void draw(Graphics2D g2) {
        int mapWidthPixels = tilesToPixels(mapWidth);

        // get the scrolling position of the map
        // based on player's position

        int offsetX = screenWidth / 2 - Math.round(player.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

        bgManager.draw(g2);

        // draw the visible tiles

        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
        for (int y = 0; y < mapHeight; y++) {
            for (int x = firstTileX; x <= lastTileX; x++) {
                Image image = getTile(x, y);
                if (image != null) {
                    g2.drawImage(image,
                            tilesToPixels(x) + offsetX,
                            tilesToPixels(y) + offsetY,
                            null);
                    // Draw tile hitbox for debugging
                    g2.setColor(Color.GREEN);
                    g2.drawRect(tilesToPixels(x) + offsetX,
                            tilesToPixels(y) + offsetY,
                            TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // draw player
        Image playerAnimImage = player.getCurrentAnimImage();
        int animWidth = playerAnimImage.getWidth(null);
        int animHeight = playerAnimImage.getHeight(null);
        int playerHitboxWidth = player.getHitboxWidth();
        int playerHitboxHeight = player.getHitboxHeight();
        int playerX = player.getX();
        int playerY = player.getY();

        int drawX = playerX + (playerHitboxWidth - animWidth) / 2;
        int drawY = playerY + (playerHitboxHeight - animHeight) / 2;

        if (player.isFacingLeft()) {
            g2.drawImage(playerAnimImage, drawX + animWidth + offsetX,
                    drawY, -animWidth, animHeight, null);
        } else {
            g2.drawImage(playerAnimImage, drawX + offsetX, drawY, null);
        }

        // Draw player hitbox for debugging
        // TODO: Remove this in production code
        Rectangle hitbox = player.getHitbox();
        g2.setColor(Color.BLUE);
        g2.drawRect(
                hitbox.x + offsetX, // Apply horizontal scroll offset
                hitbox.y, // No vertical scroll offset needed for player
                hitbox.width,
                hitbox.height);

        // Draw coins
        for (Coin coin : coins) {
            if (!coin.isCollected()) {
                g2.drawImage(coin.getAnimation().getImage(),
                        coin.getX() + offsetX,
                        coin.getY(),
                        TILE_SIZE, TILE_SIZE, null);

                // Draw coin hitbox for debugging
                // TODO: Remove this in production code
                g2.setColor(Color.RED);
                Rectangle coinHitbox = coin.getHitbox(); // World coordinates
                g2.drawRect(
                    coinHitbox.x + offsetX, // Convert to screen X
                    coinHitbox.y,
                    coinHitbox.width,
                    coinHitbox.height
                );
            }
        }
    }

    public void moveLeft() {
        int x, y;
        x = player.getX();
        y = player.getY();

        String mess = "Going left. x = " + x + " y = " + y;
        System.out.println(mess);

        player.move(1);

    }

    public void moveRight() {
        int x, y;
        x = player.getX();
        y = player.getY();

        String mess = "Going right. x = " + x + " y = " + y;
        System.out.println(mess);

        player.move(2);

    }

    public void stopMoving() {
        player.setNotMovingHorizontal();
    }

    public void jump() {
        int x, y;
        x = player.getX();
        y = player.getY();

        String mess = "Jumping. x = " + x + " y = " + y;
        System.out.println(mess);

        player.move(3);

    }

    public void update() {
        player.update();

        // Get the scrolling offsets
        int offsetX = screenWidth / 2 - Math.round(player.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - tilesToPixels(mapWidth));

        int offsetY = this.offsetY;

        // Check for coin collection
        for (Coin coin : coins) {
            if (!coin.isCollected() && player.getHitbox().intersects(coin.getHitbox())) {
                coin.collect();
                coinCounter++;
                System.out.println("Coin collected! Total coins: " + coinCounter);
            }
            coin.update();
        }
    }

}
