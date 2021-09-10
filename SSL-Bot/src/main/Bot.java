package main;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class Bot {

	private static Bot instance;

	private Overlay overlay;
	private Robot robot;
	private Point ownTank;
	private Point[] enemyTanks;
	private Shot[] probes;
	private int selectedShot;

	public Bot() {
		overlay = new Overlay();
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static Bot get() {
		if (instance == null) {
			instance = new Bot();
		}
		return instance;
	}

	public void start() {
		BufferedImage screen = robot
				.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize().width, overlay.getScreenshotHeight()));
		overlay.maximize();
		Graphics2D g2d = overlay.getGraphics();
		g2d.setFont(overlay.getFont());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		detectOwnTank(screen);
		g2d.setColor(Color.GREEN);
		drawTankPos(ownTank, g2d);
		overlay.repaint();

		detectEnemyTanks(screen);
		g2d.setColor(Color.RED);
		for (int i = 0; i < enemyTanks.length; i++) {
			drawTankPos(enemyTanks[i], g2d);
		}
		overlay.repaint();

		shootProbes(ownTank);
		
		int bestShot = 0;
		int mostRightTank = 0;
		for (int i = 0; i < enemyTanks.length; i++) {
			if (enemyTanks[i].getX() > enemyTanks[mostRightTank].getX()) {
				mostRightTank = i;
			}
		}
		for (int i = 0; i < probes.length; i++) {
			if (probes[i].getHitTank() == mostRightTank) {
				if (probes[bestShot].getAngle() < probes[i].getAngle()) {
					bestShot = i;
				}
			}
		}
		selectedShot = bestShot;
		
		draw();
	}
	
	private void drawTraceInfo(Shot s) {
		Graphics2D g2d = overlay.getGraphics();
		g2d.setColor(Color.CYAN);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawString("Selected Trace:", 50, 300);
		g2d.drawString("Power: " + (s.getPower() + 1), 50, 330);
		g2d.drawString("Angle: " + s.getAngle(), 50, 350);
		g2d.drawRect(40, 310, 435, 110);
	}

	private void drawTankPositions() {
		Graphics2D g2d = overlay.getGraphics();
		g2d.setColor(Color.GREEN);
		drawTankPos(ownTank, g2d);
		g2d.drawString("You", ownTank.x + 40, ownTank.y + 20);
		g2d.setColor(Color.RED);
		for (int i = 0; i < enemyTanks.length; i++) {
			drawTankPos(enemyTanks[i], g2d);
			g2d.drawString("i: " + i, enemyTanks[i].x + 40, enemyTanks[i].y + 20);
		}
	}

	private boolean match(Color color1, Color color2, int tolerance) {
		int r1 = color1.getRed();
		int r2 = color2.getRed();
		int g1 = color1.getGreen();
		int g2 = color2.getGreen();
		int b1 = color1.getBlue();
		int b2 = color2.getBlue();
		boolean r = r1 - tolerance < r2 && r1 + tolerance > r2;
		boolean g = g1 - tolerance < g2 && g1 + tolerance > g2;
		boolean b = b1 - tolerance < b2 && r1 + tolerance > b2;
		return r && g && b;
	}

	private void drawTankPos(Point pos, Graphics2D g2d) {
		g2d.drawRect(pos.x - 30, pos.y - 30, 60, 60);
		g2d.drawLine(pos.x - 20, pos.y, pos.x + 20, pos.y);
		g2d.drawLine(pos.x, pos.y - 20, pos.x, pos.y + 20);
		g2d.drawString("x: " + pos.x, pos.x + 40, pos.y - 20);
		g2d.drawString("y: " + pos.y, pos.x + 40, pos.y);
	}

	private BufferedImage blurImage(BufferedImage img) {
		int radius = 5;
		int size = radius * 2 + 1;
		float weight = 1.0f / (size * size);
		float[] data = new float[size * size];

		for (int i = 0; i < data.length; i++) {
			data[i] = weight;
		}
		Kernel kernel = new Kernel(size, size, data);
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

		return op.filter(img, null);
	}

	private void detectOwnTank(BufferedImage screen) {
		BufferedImage ownTank = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < screen.getWidth(); x++) {
			for (int y = 0; y < screen.getHeight(); y++) {
				Color pixel = new Color(screen.getRGB(x, y));
				if (match(pixel, new Color(0, 205, 8), 30)) {
					ownTank.setRGB(x, y, pixel.getRGB());
				}
			}
		}
		ownTank = blurImage(ownTank);
		Point pos = new Point();
		Color brightestColor = null;
		for (int x = 0; x < ownTank.getWidth(); x++) {
			for (int y = 0; y < ownTank.getHeight(); y++) {
				Color pixel = new Color(ownTank.getRGB(x, y));
				if (brightestColor == null || pixel.getRed() + pixel.getGreen() + pixel.getBlue() > brightestColor.getRed()
						+ brightestColor.getGreen() + brightestColor.getBlue()) {
					brightestColor = pixel;
					pos.x = x;
					pos.y = y;
				}
			}
		}
		this.ownTank = pos;
	}

	private void detectEnemyTanks(BufferedImage screen) {
		Point[] tanks = new Point[7];
		BufferedImage enemyTank = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < screen.getWidth(); x++) {
			for (int y = 0; y < screen.getHeight(); y++) {
				Color pixel = new Color(screen.getRGB(x, y));
				if (match(pixel, new Color(223, 15, 15), 30)) {
					enemyTank.setRGB(x, y, pixel.getRGB());
				}
			}
		}
		enemyTank = blurImage(enemyTank);

		for (int t = 0; t < 7; t++) {
			tanks[t] = new Point();
			Color brightestColor = null;
			for (int x = 0; x < enemyTank.getWidth(); x++) {
				y: for (int y = 0; y < enemyTank.getHeight(); y++) {
					Color pixel = new Color(enemyTank.getRGB(x, y));
					if (brightestColor == null || pixel.getRed() + pixel.getGreen() + pixel.getBlue() > brightestColor.getRed()
							+ brightestColor.getGreen() + brightestColor.getBlue()) {
						for (int i = 0; i < tanks.length; i++) {
							if (tanks[i] != null && t != i) {
								if (tanks[i].distance(x, y) < 60) {
									continue y;
								}
							}
						}
						brightestColor = pixel;
						tanks[t].x = x;
						tanks[t].y = y;
					}
				}
			}
		}
		int count = 0;
		for (int i = 0; i < tanks.length; i++) {
			if (tanks[i].x != 0 && tanks[i].y != 0) {
				count++;
			}
		}
		Point[] cut = new Point[count];
		for (int i = 0; i < cut.length; i++) {
			cut[i] = tanks[i];
		}
		this.enemyTanks = cut;
	}

	private void shootProbes(Point start) {

		Graphics2D g2d = overlay.getGraphics();

		probes = new Shot[9000];
		for (int i = 89; i >= 0; i--) {
			for (int j = 0; j < 100; j++) {
				probes[i * 100 + j] = new Shot(start, j, i, overlay.getShootLeft());
			}
		}

		g2d.setColor(new Color(1f, 1f, 1f, 0.1f));

		while (true) {
			boolean probesAlive = false;
			for (int i = 0; i < probes.length; i++) {
				if (probes[i].isAlive()) {
					probesAlive = true;
					probes[i].step(enemyTanks);
					g2d.drawLine(probes[i].getPrevX(), probes[i].getPrevY(), probes[i].getX(), probes[i].getY());
				}
			}
			overlay.repaint();
			if (!probesAlive) {
				break;
			}
		}

		int count = 0;
		for (int i = 0; i < probes.length; i++) {
			if (probes[i].hit()) {
				count++;
			}
		}
		Shot[] hitProbes = new Shot[count];
		int index = 0;
		for (int i = 0; i < probes.length; i++) {
			if (probes[i].hit()) {
				hitProbes[index] = probes[i];
				index++;
			}
		}
		probes = hitProbes;
	}

	private void drawShots() {
		Graphics2D g2d = overlay.getGraphics();
		g2d.setColor(new Color(0f, 0f, 0.8f, 1f));
		for (int i = 0; i < probes.length; i++) {
			if (i == selectedShot) {
				continue;
			}
			Shot s = new Shot(ownTank, probes[i].getPower(), probes[i].getAngle(), overlay.getShootLeft());
			while (s.isAlive()) {
				s.step(enemyTanks);
				g2d.drawLine(s.getPrevX(), s.getPrevY(), s.getX(), s.getY());
			}
		}
		g2d.setColor(new Color(0.6f, 0.6f, 1f, 1f));
		g2d.setStroke(new BasicStroke(4f));
		Shot s = new Shot(ownTank, probes[selectedShot].getPower(), probes[selectedShot].getAngle(), overlay.getShootLeft());
		while (s.isAlive()) {
			s.step(enemyTanks);
			g2d.drawLine(s.getPrevX(), s.getPrevY(), s.getX(), s.getY());
		}
		g2d.setStroke(new BasicStroke(1f));
	}

	public int getScreenWidth() {
		return overlay.getScreenSize().width;
	}

	public int getScreenHeight() {
		return overlay.getScreenSize().height;
	}

	public Overlay getOverlay() {
		return overlay;
	}

	public void selectLeft() {
		if (selectedShot > 0) {
			selectedShot--;
			draw();
		}
	}

	public void selectRight() {
		if (selectedShot < probes.length - 1) {
			selectedShot++;
			draw();
		}
	}
	
	private void draw() {
		overlay.clear();
		drawShots();
		drawTankPositions();
		drawTraceInfo(probes[selectedShot]);
		overlay.repaint();
	}
}
