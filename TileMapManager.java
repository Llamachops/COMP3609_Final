import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.ImageIcon;

/*
 * The ResourceManager class loads and manages tile Images
 */
public class TileMapManager {

    private ArrayList<Image> tiles;
    private JFrame window;

    public TileMapManager(JFrame window) {
        this.window = window;

        loadTileImages();
    }

    public TileMap loadMap(String filename, Player existingPlayer) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        int mapWidth = 0;
        int mapHeight = 0;

        // Read every line in the text file into the list
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                reader.close();
                break;
            }

            // Add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
                mapWidth = Math.max(mapWidth, line.length());
            }
        }

        // Parse the lines to create a TileMap
        mapHeight = lines.size();
        TileMap newMap = new TileMap(window, mapWidth, mapHeight);
        newMap.clearLevel();
        newMap.bgManager.reset();

        // Reuse the existing player object
        if (existingPlayer != null) {
            newMap.setPlayer(existingPlayer, newMap.bgManager);
        }

        for (int y = 0; y < mapHeight; y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char ch = line.charAt(x);

                // Check if the char represents tile A, B, C, etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, tiles.get(tile));
                } else if (ch == 'o') {
                    Coin coin = new Coin(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY());
                    newMap.addCoin(coin);
                } else if (ch == 't') {
                    Troll troll = new Troll(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            newMap, newMap.getPlayer());
                    newMap.addTroll(troll);
                } else if (ch == 'f') {
                    Fairy fairy = new Fairy(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            newMap, newMap.getPlayer());
                    newMap.addFairy(fairy);
                } else if (ch == '*') {
                    Chest chest = new Chest(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            newMap.getPlayer());
                    newMap.addChest(chest);
                } else if (ch == '1') { // Damage power-up
                    PowerUp powerUp = new PowerUp(TileMap.tilesToPixels(x),
                            TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            PowerUp.PowerUpType.DAMAGE);
                    newMap.addPowerUp(powerUp);
                } else if (ch == '2') { // Critical chance power-up
                    PowerUp powerUp = new PowerUp(TileMap.tilesToPixels(x),
                            TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            PowerUp.PowerUpType.CRIT_CHANCE);
                    newMap.addPowerUp(powerUp);
                } else if (ch == '3') { // Critical damage power-up
                    PowerUp powerUp = new PowerUp(TileMap.tilesToPixels(x),
                            TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            PowerUp.PowerUpType.CRIT_DAMAGE);
                    newMap.addPowerUp(powerUp);
                } else if (ch == '4') { // Lives power-up
                    PowerUp powerUp = new PowerUp(TileMap.tilesToPixels(x),
                            TileMap.tilesToPixels(y) + TileMap.getOffsetY(),
                            PowerUp.PowerUpType.LIVES);
                    newMap.addPowerUp(powerUp);
                }
            }
        }

        return newMap;
    }

    // -----------------------------------------------------------
    // code for loading sprites and images
    // -----------------------------------------------------------

    public void loadTileImages() {
        // keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ folder

        File file;

        System.out.println("loadTileImages called.");

        tiles = new ArrayList<Image>();
        char ch = 'A';
        while (true) {
            String filename = "images/zone1/tile_" + ch + ".png";
            file = new File(filename);
            if (!file.exists()) {
                System.out.println("Image file could not be opened: " + filename);
                break;
            } else
                System.out.println("Image file opened: " + filename);
            Image tileImage = new ImageIcon(filename).getImage();
            tiles.add(tileImage);
            ch++;
        }
    }
}
