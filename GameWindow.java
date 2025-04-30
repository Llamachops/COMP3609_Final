import javax.swing.*; // need this for GUI objects
import java.awt.*; // need this for certain AWT classes
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.event.*;
import java.awt.image.BufferStrategy; // need this to implement page flipping

public class GameWindow extends JFrame implements
		Runnable,
		KeyListener,
		MouseListener,
		MouseMotionListener {
	private static final int NUM_BUFFERS = 2; // used for page flipping

	private int pWidth, pHeight; // width and height of screen

	private Thread gameThread = null; // the thread that controls the game
	private volatile boolean isRunning = false; // used to stop the game thread

	private boolean movingLeft = false; // used to move the player left
	private boolean movingRight = false; // used to move the player right

	private BufferedImage image; // drawing area for each frame

	private Image quit1Image; // first image for quit button
	private Image quit2Image; // second image for quit button

	private boolean finishedOff = false; // used when the game terminates

	private volatile boolean isOverQuitButton = false;
	private Rectangle quitButtonArea; // used by the quit button

	private volatile boolean isOverPauseButton = false;
	private Rectangle pauseButtonArea; // used by the pause 'button'
	private volatile boolean isPaused = false;

	private volatile boolean isOverStopButton = false;
	private Rectangle stopButtonArea; // used by the stop 'button'
	private volatile boolean isStopped = false;

	private GraphicsDevice device; // used for full-screen exclusive mode
	private Graphics gScr;
	private BufferStrategy bufferStrategy;

	private Font statsFont = new Font("Arial", Font.BOLD, 32);
	SoundManager soundManager;
	TileMapManager tileManager;
	TileMap tileMap;
	private int level = 1; // Current level of the game

	void restartLevel() {
		isPaused = true; // Pause the game during the restart

		// Reset player lives and coin counter
		tileMap.changeNumLives(3 - tileMap.getNumLives()); // Reset lives to 3
		tileMap.setCoinCounter(0); // Reset coin counter

		// Reset player position
		Player player = tileMap.getPlayer();
		player.setX(192); // Reset player X position
		player.setY(630);

		// Reload the map
		try {
			tileMap = tileManager.loadMap("maps/map" + level + ".txt", tileMap.getPlayer());
		} catch (IOException e) {
			e.printStackTrace();
		}

		isPaused = false; // Resume the game
	}

	void changeLevel() {
		isPaused = true;
		level++; // Increment the level count

		try {
			// Pass the existing player object to retain stats
			int coins = tileMap.getCoinCounter(); // Get the current coin count
			tileMap = tileManager.loadMap("maps/map" + level + ".txt", tileMap.getPlayer());
			tileMap.setCoinCounter(coins); // Set the coin counter to the previous value
			Player player = tileMap.getPlayer();
			player.setX(192); // Reset player X position
			player.setY(630);
			System.out.println("Loaded map: maps/map" + level + ".txt");
		} catch (IOException e) {
			System.out.println("Failed to load map: maps/map" + level + ".txt");
			restartGame();
		}

		isPaused = false;
	}

	private void restartGame() {
		isPaused = true; // Pause the game during the restart
		level = 1; // Reset to level 1 if the next level doesn't exist

		// Reset player stats
		tileMap.getPlayer().resetStats();

		// Reset player lives and coin counter
		tileMap.changeNumLives(3 - tileMap.getNumLives()); // Reset lives to 3
		tileMap.setCoinCounter(0); // Reset coin counter

		// Reset player position
		Player player = tileMap.getPlayer();
		player.setX(192); // Reset player X position
		player.setY(630);

		// Reload the map
		try {
			tileMap = tileManager.loadMap("maps/map" + level + ".txt", null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		isPaused = false; // Resume the game
	}

	public GameWindow() {

		super("Tiled Bat and Ball Game: Full Screen Exclusive Mode");

		initFullScreen();

		quit1Image = ImageManager.loadImage("images/Quit1.png");
		quit2Image = ImageManager.loadImage("images/Quit2.png");

		setButtonAreas();

		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		// animation = new BirdAnimation();
		soundManager = SoundManager.getInstance();
		image = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_RGB);

		startGame();
	}

	// implementation of Runnable interface

	public void run() {
		try {
			isRunning = true;
			while (isRunning) {
				if (isPaused == false) {
					gameUpdate();
				}
				screenUpdate();
				Thread.sleep(1000 / 60);
			}
		} catch (InterruptedException e) {
		}

		finishOff();
	}

	/*
	 * This method performs some tasks before closing the game.
	 * The call to System.exit() should not be necessary; however,
	 * it prevents hanging when the game terminates.
	 */

	private void finishOff() {
		if (!finishedOff) {
			finishedOff = true;
			restoreScreen();
			System.exit(0);
		}
	}

	/*
	 * This method switches off full screen mode. The display
	 * mode is also reset if it has been changed.
	 */

	private void restoreScreen() {
		Window w = device.getFullScreenWindow();

		if (w != null)
			w.dispose();

		device.setFullScreenWindow(null);
	}

	public void gameUpdate() {

		if (!isPaused && !isStopped) {
			if (movingLeft) {
				tileMap.moveLeft();
			} else if (movingRight) {
				tileMap.moveRight();
			} else {
				tileMap.stopMoving();
			}

			tileMap.update();
		}
	}

	private void screenUpdate() {

		try {
			gScr = bufferStrategy.getDrawGraphics();
			gameRender(gScr);
			gScr.dispose();
			if (!bufferStrategy.contentsLost())
				bufferStrategy.show();
			else
				System.out.println("Contents of buffer lost.");

			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)

			Toolkit.getDefaultToolkit().sync();
		} catch (Exception e) {
			e.printStackTrace();
			isRunning = false;
		}
	}

	public void gameRender(Graphics gScr) { // draw the game objects
		Graphics2D imageContext = (Graphics2D) image.getGraphics();

		tileMap.draw(imageContext);

		drawLivesCounter(imageContext); // draw the lives counter
		drawPlayerStats(imageContext); // draw the player's stats
		drawCoinCounter(imageContext); // draw the coin counter
		drawButtons(imageContext); // draw the buttons

		Graphics2D g2 = (Graphics2D) gScr;
		g2.drawImage(image, 0, 0, pWidth, pHeight, null);

		imageContext.dispose();
		g2.dispose();
	}

	private void initFullScreen() { // standard procedure to get into FSEM

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		device = ge.getDefaultScreenDevice();

		setUndecorated(true); // no menu bar, borders, etc.
		setIgnoreRepaint(true); // turn off all paint events since doing active rendering
		setResizable(false); // screen cannot be resized

		if (!device.isFullScreenSupported()) {
			System.out.println("Full-screen exclusive mode not supported");
			System.exit(0);
		}

		device.setFullScreenWindow(this); // switch on full-screen exclusive mode

		// we can now adjust the display modes, if we wish

		showCurrentMode();

		pWidth = getBounds().width;
		pHeight = getBounds().height;

		System.out.println("Width of window is " + pWidth);
		System.out.println("Height of window is " + pHeight);

		try {
			createBufferStrategy(NUM_BUFFERS);
		} catch (Exception e) {
			System.out.println("Error while creating buffer strategy " + e);
			System.exit(0);
		}

		bufferStrategy = getBufferStrategy();
	}

	// This method provides details about the current display mode.

	private void showCurrentMode() {

		DisplayMode dm[] = device.getDisplayModes();

		for (int i = 0; i < dm.length; i++) {
			System.out.println("Current Display Mode: (" +
					dm[i].getWidth() + "," + dm[i].getHeight() + "," +
					dm[i].getBitDepth() + "," + dm[i].getRefreshRate() + ")  ");
		}

		// DisplayMode d = new DisplayMode (800, 600, 32, 60);
		// device.setDisplayMode(d);

		DisplayMode dm1 = device.getDisplayMode();

		dm1 = device.getDisplayMode();

		System.out.println("Current Display Mode: (" +
				dm1.getWidth() + "," + dm1.getHeight() + "," +
				dm1.getBitDepth() + "," + dm1.getRefreshRate() + ")  ");
	}

	// Specify screen areas for the buttons and create bounding rectangles

	private void setButtonAreas() {

		// leftOffset is the distance of a button from the left side of the window.
		// Buttons are placed at the top of the window.

		int leftOffset = (pWidth - (5 * 150) - (4 * 20)) / 2;
		pauseButtonArea = new Rectangle(leftOffset, 60, 150, 40);

		leftOffset = leftOffset + 170;
		stopButtonArea = new Rectangle(leftOffset, 60, 150, 40);

		leftOffset = leftOffset + 170;
		quitButtonArea = new Rectangle(leftOffset, 55, 180, 50);
	}

	private void drawButtons(Graphics g) {
		Font oldFont, newFont;

		oldFont = g.getFont(); // save current font to restore when finished

		newFont = new Font("TimesRoman", Font.ITALIC + Font.BOLD, 18);
		g.setFont(newFont); // set this as font for text on buttons

		g.setColor(Color.black); // set outline colour of button

		// draw the pause 'button'

		g.setColor(Color.BLACK);
		g.drawOval(pauseButtonArea.x, pauseButtonArea.y,
				pauseButtonArea.width, pauseButtonArea.height);

		if (isOverPauseButton && !isStopped)
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.RED);

		if (isPaused && !isStopped)
			g.drawString("Paused", pauseButtonArea.x + 45, pauseButtonArea.y + 25);
		else
			g.drawString("Pause", pauseButtonArea.x + 55, pauseButtonArea.y + 25);

		// draw the stop 'button'

		g.setColor(Color.BLACK);
		g.drawOval(stopButtonArea.x, stopButtonArea.y,
				stopButtonArea.width, stopButtonArea.height);

		if (isOverStopButton && !isStopped)
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.RED);

		if (isStopped)
			g.drawString("Stopped", stopButtonArea.x + 40, stopButtonArea.y + 25);
		else
			g.drawString("Stop", stopButtonArea.x + 60, stopButtonArea.y + 25);

		// draw the quit button (an actual image that changes when the mouse moves over
		// it)

		if (isOverQuitButton)
			g.drawImage(quit1Image, quitButtonArea.x, quitButtonArea.y, 180, 50, null);
		// quitButtonArea.width, quitButtonArea.height, null);

		else
			g.drawImage(quit2Image, quitButtonArea.x, quitButtonArea.y, 180, 50, null);
		// quitButtonArea.width, quitButtonArea.height, null);
		/*
		 * g.setColor(Color.BLACK);
		 * g.drawOval(quitButtonArea.x, quitButtonArea.y,
		 * quitButtonArea.width, quitButtonArea.height);
		 * if (isOverQuitButton)
		 * g.setColor(Color.WHITE);
		 * else
		 * g.setColor(Color.RED);
		 * 
		 * g.drawString("Quit", quitButtonArea.x+60, quitButtonArea.y+25);
		 */
		g.setFont(oldFont); // reset font

	}

	private void drawCoinCounter(Graphics2D g2) {
		int coinCount = tileMap.getCoinCounter(); // Get the current coin count
		Image coinImage = ImageManager.loadImage("images/coin/coin_1.png"); // Use the first coin image as the icon

		// Set the position for the coin counter
		int x = pWidth - 150; // 150 pixels from the right edge
		int y = 20; // 20 pixels from the top edge

		// Draw the coin count text
		g2.setFont(statsFont);
		g2.setColor(Color.BLACK);
		g2.drawString(coinCount + " x", x, y + 40);

		// Draw the coin image next to the text
		g2.drawImage(coinImage, x + 60, y, 64, 64, null);
	}

	private void drawLivesCounter(Graphics2D g2) {
		int lives = tileMap.getNumLives();
		Image lifeImage = ImageManager.loadImage("images/collectibles/Life.png");
		int x = 20; // 20 pixels from the left edge
		int y = 20; // 20 pixels from the top edge

		for (int i = 0; i < lives; i++) {
			g2.drawImage(lifeImage, x + (i * (lifeImage.getWidth(null) + 5) / 2), y, 64, 64, null);
		}
	}

	private void drawPlayerStats(Graphics2D g2) {
		Player player = tileMap.getPlayer();

		// Get player stats
		int damage = player.getAttackDamage();
		float critChance = player.getCritChance() * 100; // Convert to percentage
		float critMultiplier = player.getCritMultiplier();

		// Set the position for the stats
		int x = 20; // 20 pixels from the left edge
		int y = 120; // Start below the lives counter

		// Set font and color
		g2.setFont(statsFont);
		g2.setColor(Color.BLACK);

		// Draw the stats
		g2.drawString("Damage: " + damage, x, y);
		g2.drawString("Crit Odds: " + String.format("%.0f%%", critChance), x, y + 30);
		g2.drawString("Crit Mult: " + String.format("%.1f", critMultiplier) + "x", x, y + 60);
	}

	private void startGame() {
		if (gameThread == null) {
			soundManager.playSound ("background", true);
			tileManager = new TileMapManager(this);

			try {
				tileMap = tileManager.loadMap("maps/map" + level + ".txt", null);
				int w, h;
				w = tileMap.getWidth();
				h = tileMap.getHeight();
				System.out.println("Width of tilemap " + w);
				System.out.println("Height of tilemap " + h);
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}

			// imageEffect = new ImageEffect (this);
			gameThread = new Thread(this);
			gameThread.start();

		}
	}

	// implementation of methods in KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {

		if (isPaused)
			return;

		int keyCode = e.getKeyCode();

		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) ||
				(keyCode == KeyEvent.VK_END)) {
			isRunning = false; // user can quit by pressing ESC, Q or END
			return;
		} else if (keyCode == KeyEvent.VK_LEFT) {
			movingLeft = true;
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			movingRight = true;
		}

		if ((keyCode == KeyEvent.VK_UP)) {
			tileMap.jump();
		}

		if (keyCode == KeyEvent.VK_SPACE) {
			tileMap.getPlayer().attack(tileMap.getEnemies());
		}
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_LEFT) {
			movingLeft = false;
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			movingRight = false;
		}

	}

	public void keyTyped(KeyEvent e) {

	}

	// implement methods of MouseListener interface

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		testMousePress(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) {

	}

	// implement methods of MouseMotionListener interface

	public void mouseDragged(MouseEvent e) {

	}

	public void mouseMoved(MouseEvent e) {
		testMouseMove(e.getX(), e.getY());
	}

	/*
	 * This method handles mouse clicks on one of the buttons
	 * (Pause, Stop, Start Anim, Pause Anim, and Quit).
	 */

	private void testMousePress(int x, int y) {

		if (isStopped && !isOverQuitButton) // don't do anything if game stopped
			return;

		if (isOverStopButton) { // mouse click on Stop button
			isStopped = true;
			isPaused = false;
		} else if (isOverPauseButton) { // mouse click on Pause button
			isPaused = !isPaused; // toggle pausing
		} else if (isOverQuitButton) { // mouse click on Quit button
			isRunning = false; // set running to false to terminate
		}
	}

	/*
	 * This method checks to see if the mouse is currently moving over one of
	 * the buttons (Pause, Stop, Show Anim, Pause Anim, and Quit). It sets a
	 * boolean value which will cause the button to be displayed accordingly.
	 */

	private void testMouseMove(int x, int y) {
		if (isRunning) {
			isOverPauseButton = pauseButtonArea.contains(x, y) ? true : false;
			isOverStopButton = stopButtonArea.contains(x, y) ? true : false;
			isOverQuitButton = quitButtonArea.contains(x, y) ? true : false;
		}
	}

}