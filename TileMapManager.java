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
    private int currentMap = 0;

    private JFrame window;

    public TileMapManager(JFrame window) {
        this.window = window;

        loadTileImages();
    }

    public TileMap loadMap(String filename)
            throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        int mapWidth = 0;
        int mapHeight = 0;

        // read every line in the text file into the list

        BufferedReader reader = new BufferedReader(
                new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
                mapWidth = Math.max(mapWidth, line.length());
            }
        }

        // parse the lines to create a TileMap
        mapHeight = lines.size();

        TileMap newMap = new TileMap(window, mapWidth, mapHeight);
        for (int y = 0; y < mapHeight; y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, tiles.get(tile));
                } else if (ch == 'o') {
                    Coin coin = new Coin(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY());
                    newMap.addCoin(coin);
                } else if (ch == 't') {
                    Troll troll = new Troll(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY(), newMap, newMap.getPlayer());
                    newMap.addTroll(troll);
                    System.out.println("Troll added at: " + TileMap.tilesToPixels(x) + ", " + TileMap.tilesToPixels(y));
                } else if (ch == 'f') {
                    // Fairy fairy = new Fairy(TileMap.tilesToPixels(x), TileMap.tilesToPixels(y) + TileMap.getOffsetY(), newMap);
                    // newMap.addFairy(fairy);
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
