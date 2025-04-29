import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.ImageIcon;

public class Background {
    private Image bgImage;
    private int bgImageWidth; // width of the background (>= panel Width)
    private int bgImageHeight; // height of the background

    private Dimension dimension;

    private int bgX;
    private int backgroundX;
    private int backgroundX2;
    private int bgDX; // size of the background move (in pixels)

    private double scaleX; // scaling factor for width
    private double scaleY; // scaling factor for height

    public Background(JFrame window, String imageFile, int bgDX) {
        this.bgImage = loadImage(imageFile);
        bgImageWidth = bgImage.getWidth(null); // get width of the background
        bgImageHeight = bgImage.getHeight(null); // get height of the background

        System.out.println("bgImageWidth = " + bgImageWidth);
        System.out.println("bgImageHeight = " + bgImageHeight);

        dimension = window.getSize();

        // Calculate scaling factors
        scaleX = dimension.width / (double) bgImageWidth;
        scaleY = dimension.height / (double) bgImageHeight;

        // Adjust movement speed based on scaling
        this.bgDX = (int) (bgDX * scaleX);
    }

    public void moveRight() {
        if (bgX == 0) {
            backgroundX = 0;
            backgroundX2 = (int) (bgImageWidth * scaleX);
        }

        bgX = bgX - bgDX;

        backgroundX = backgroundX - bgDX;
        backgroundX2 = backgroundX2 - bgDX;

        if ((bgX + (int) (bgImageWidth * scaleX)) % (int) (bgImageWidth * scaleX) == 0) {
            // System.out.println("Background change: bgX = " + bgX);
            backgroundX = 0;
            backgroundX2 = (int) (bgImageWidth * scaleX);
        }
    }

    public void moveLeft() {
        if (bgX == 0) {
            backgroundX = (int) (bgImageWidth * scaleX) * -1;
            backgroundX2 = 0;
        }

        bgX = bgX + bgDX;

        backgroundX = backgroundX + bgDX;
        backgroundX2 = backgroundX2 + bgDX;

        if ((bgX + (int) (bgImageWidth * scaleX)) % (int) (bgImageWidth * scaleX) == 0) {
            backgroundX = (int) (bgImageWidth * scaleX) * -1;
            backgroundX2 = 0;
        }
    }

    public void draw(Graphics2D g2) {
        // Scale and draw the background images
        int scaledWidth = (int) (bgImageWidth * scaleX);
        int scaledHeight = (int) (bgImageHeight * scaleY);

        g2.drawImage(bgImage, backgroundX, 0, scaledWidth, scaledHeight, null);
        g2.drawImage(bgImage, backgroundX2, 0, scaledWidth, scaledHeight, null);
    }

    public Image loadImage(String fileName) {
        return new ImageIcon(fileName).getImage();
    }
}
