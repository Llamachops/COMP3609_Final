import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
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

    static final int TILE_SIZE = 128;
    private Image[][] tiles;

    private ArrayList<Coin> coins; // List of coins in the map
    private int coinCounter = 0; // Counter for collected coins

    private int livesCounter = 3; // Number of lives

    private ArrayList<Troll> trolls = new ArrayList<>();
    private ArrayList<Fairy> fairies = new ArrayList<>();
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private Chest chest = null;
    private ArrayList<PowerUp> powerUps = new ArrayList<>();

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

        bgManager = new BackgroundManager(window);

        coins = new ArrayList<>();

        tiles = new Image[mapWidth][mapHeight];
        player = new Player(window, this, bgManager);

        int x, y;

        x = 192; // position player
        y = 630;

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

        // get the scrolling position of the map based on player's position
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
                    // g2.setColor(Color.GREEN);
                    // g2.drawRect(tilesToPixels(x) + offsetX,
                    //         tilesToPixels(y) + offsetY,
                    //         TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw the player
        player.draw(g2, offsetX, offsetY);

        // Draw coins
        for (Coin coin : coins) {
            if (!coin.isCollected()) {
                g2.drawImage(coin.getAnimation().getImage(),
                        coin.getX() + offsetX,
                        coin.getY(),
                        TILE_SIZE, TILE_SIZE, null);

                // Draw coin hitbox for debugging
                // g2.setColor(Color.RED);
                // Rectangle coinHitbox = coin.getHitbox(); // World coordinates
                // g2.drawRect(
                //         coinHitbox.x + offsetX, // Convert to screen X
                //         coinHitbox.y,
                //         coinHitbox.width,
                //         coinHitbox.height);
            }
        }

        // Draw trolls
        for (Troll troll : trolls) {
            troll.draw(g2, offsetX, offsetY);
        }

        // Draw fairies
        for (Fairy fairy : fairies) {
            fairy.draw(g2, offsetX, offsetY);
        }

        // Draw projectiles
        for (Projectile projectile : projectiles) {
            projectile.draw(g2, offsetX, offsetY);
        }

        // Draw power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2, offsetX, offsetY);
        }

        // Draw the chest if it exists
        if (chest != null) {
            chest.draw(g2, offsetX, offsetY);
        }
    }

    public void moveLeft() {
        player.move(1);

    }

    public void moveRight() {
        player.move(2);

    }

    public void stopMoving() {
        player.setNotMovingHorizontal();
    }

    public void jump() {
        player.move(3);

    }

    public void update() {
        if (chest != null && chest.isOpened()) {
            triggerLevelComplete();
        }

        player.update();

        // Get the scrolling offsets
        int offsetX = screenWidth / 2 - Math.round(player.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - tilesToPixels(mapWidth));

        // Check for coin collection
        for (Coin coin : coins) {
            if (!coin.isCollected() && player.getHitbox().intersects(coin.getHitbox())) {
                coin.collect();
                coinCounter++;
                System.out.println("Coin collected! Total coins: " + coinCounter);
            }
            coin.update();
        }

        // Update trolls
        Iterator<Troll> trollIterator = trolls.iterator();
        while (trollIterator.hasNext()) {
            Troll troll = trollIterator.next();
            troll.update();
            if (troll.isDead()) {
                trollIterator.remove();
                System.out.println("Troll removed!");
            }
        }

        // Update fairies
        Iterator<Fairy> fairyIterator = fairies.iterator();
        while (fairyIterator.hasNext()) {
            Fairy fairy = fairyIterator.next();
            fairy.update();
            if (fairy.isDead()) {
                fairyIterator.remove();
                System.out.println("Fairy removed!");
            }
        }

        // Update projectiles
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.update();
            if (!projectile.isActive()) {
                // System.out.println("Projectile being removed!");
                projectileIterator.remove();
                // System.out.println("Projectile removed!");
            }
        }

        // Check for power-up collection
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            if (player.getHitbox().intersects(powerUp.getHitbox())) {
                applyPowerUp(powerUp);
                powerUpIterator.remove();
            }
        }
        
        // Check for collision with the chest
        if (chest != null) {
            chest.update();
        }
    }

    public int getNumLives() {
        return livesCounter;
    }

    public void changeNumLives(int amount) {
        livesCounter += amount;
        if (livesCounter < 1) {
            livesCounter = 3;
            gameOver();
        }
    }

    public void gameOver() {
        System.out.println("Game Over!");
        Image gameOverImage = ImageManager.loadImage("images/game_over.png");

        Graphics2D g2 = (Graphics2D) window.getGraphics();
        g2.drawImage(gameOverImage, (screenWidth - 400) / 2, (screenHeight - 200) / 2, 400, 200, null);
        g2.dispose();

        try {
            Thread.sleep(3000); // Wait for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Call restartGame in GameWindow
        ((GameWindow) window).restartLevel();
    }

    public void setCoinCounter(int value) {
        coinCounter = value;
    }

    public void addTroll(Troll troll) {
        trolls.add(troll);
    }

    public void addFairy(Fairy fairy) {
        fairies.add(fairy);
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Enemy> getEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.addAll(trolls);
        enemies.addAll(fairies);
        return enemies;
    }

    public void addChest(Chest chest) {
        this.chest = chest;
    }

    public void removeChest() {
        this.chest = null;
    }

    private void triggerLevelComplete() {
        System.out.println("Level Complete!");
        Image completeImage = ImageManager.loadImage("images/complete.png");

        // Display the "Level Complete" screen
        Graphics2D g2 = (Graphics2D) window.getGraphics();
        g2.drawImage(completeImage, (screenWidth - 800) / 2, (screenHeight - 1000) / 2, 800, 1000, null);
        g2.dispose();

        // Start a timer to load the next level
        try {
            Thread.sleep(3000); // Wait for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Call loadNextLevel in GameWindow
        ((GameWindow) window).changeLevel();
    }

    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case DAMAGE:
                player.increaseAttackDamage(10); // Increase damage by 10
                break;
            case CRIT_CHANCE:
                player.increaseCritChance(0.05f); // Increase critical chance by 5%
                break;
            case CRIT_DAMAGE:
                player.increaseCritMultiplier(0.5f); // Increase critical damage multiplier by 0.5x
                break;
            case LIVES:
                changeNumLives(1); // Increase lives by 1
                break;
        }
    }

    public void setPlayer(Player player, BackgroundManager bgManager) {
        this.player = player;
        player.setTileMap(this);
        player.setBackgroundManager(bgManager);
    }
    
    public void clearLevel() {
        coins.clear();
        trolls.clear();
        fairies.clear();
        projectiles.clear();
        powerUps.clear();
        chest = null;
    }
}
