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
	private Obstacle[] obstacles;

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
		overlay.clear();
		g2d.setStroke(new BasicStroke(1));
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

		detectObstacles(screen);
		
		drawObstacles();

		shootProbes(ownTank);

		int bestShot = 0;
		int furthestTank = 0;
		for (int i = 0; i < enemyTanks.length; i++) {
			if (overlay.getShootLeft()) {
				if (enemyTanks[i].getX() < enemyTanks[furthestTank].getX()) {
					furthestTank = i;
				}
			} else {
				if (enemyTanks[i].getX() > enemyTanks[furthestTank].getX()) {
					furthestTank = i;
				}
			}
		}
		for (int i = 0; i < probes.length; i++) {
			if (probes[i].getHitTank() == furthestTank) {
				if (probes[bestShot].getAngle() < probes[i].getAngle()) {
					bestShot = i;
				}
			}
		}
		selectedShot = bestShot;

		draw();
	}

	private void detectObstacles(BufferedImage screen) {
		obstacles = new Obstacle[10];
		BufferedImage obstacle = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int checkRadius = 10;
		for (int x = 0; x < obstacle.getWidth(); x++) {
			for (int y = 75; y < obstacle.getHeight(); y++) {
				if (screen.getRGB(x, y) == Color.WHITE.getRGB()) {
					for (int u = -checkRadius; u < checkRadius; u++) {
						for (int v = -checkRadius; v < checkRadius; v++) {
							int ux = u + x;
							int vy = v + y;
							if (ux > 0 && ux < obstacle.getWidth() && vy > 0 && vy < obstacle.getHeight()) {
								Color pixel = new Color(screen.getRGB(ux, vy));
								if (match(pixel, new Color(255, 80, 255), 60)) {
									if (screen.getRGB(x, y) == Color.WHITE.getRGB()) {
										obstacle.setRGB(x, y, Color.WHITE.getRGB());
									}
								}
							}
						}
					}
				}
			}
		}
		Color colorCode = new Color(50, 50, 50);
		int bindex = 0;
		for (int x = 0; x < obstacle.getWidth(); x++) {
			for (int y = 75; y < obstacle.getHeight(); y++) {
				if (obstacle.getRGB(x, y) == Color.WHITE.getRGB()) {
					Point[] bounds = fillColor(obstacle, Color.WHITE, colorCode, x, y, new Point[] {new Point(x, y), new Point(x, y)});
					boolean[] corners = checkObstacleCorners(obstacle, bounds, colorCode);
					if (corners[0] && corners[3]) {
						obstacles[bindex] = new Obstacle(bounds[0], bounds[1]);
					} else if (corners[1] && corners[2]) {
						int temp = bounds[0].x;
						bounds[0].x = bounds[1].x;
						bounds[1].x = temp;
						obstacles[bindex] = new Obstacle(bounds[0], bounds[1]);
					} else {
						int cx = bounds[0].x + (bounds[1].x - bounds[0].x) / 2;
						int cy = bounds[0].y + (bounds[1].y - bounds[0].y) / 2;
						obstacles[bindex] = new Obstacle(new Point(cx, cy), (bounds[1].x - bounds[0].x) / 2);
					}
					colorCode = new Color(colorCode.getRed() + 10, colorCode.getGreen() + 10, colorCode.getBlue() + 10);
					bindex++;
				}
			}
		}
		
		Obstacle[] cut = new Obstacle[bindex];
		for (int i = 0; i < cut.length; i++) {
			cut[i] = obstacles[i];
		}
		obstacles = cut;
	}
	
	private boolean[] checkObstacleCorners(BufferedImage obstacle, Point[] bounds, Color colorCode) {
		boolean[] corners = new boolean[4];
		int checkSize = 10;
		x: for (int x = bounds[0].x; x < bounds[0].x + checkSize; x++) {
			for (int y = bounds[0].y; y < bounds[0].y + checkSize; y++) {
				if (obstacle.getRGB(x, y) == colorCode.getRGB()) {
					corners[0] = true;
					break x;
				}
			}
		}
		x: for (int x = bounds[1].x - checkSize; x < bounds[1].x; x++) {
			for (int y = bounds[0].y; y < bounds[0].y + checkSize; y++) {
				if (obstacle.getRGB(x, y) == colorCode.getRGB()) {
					corners[1] = true;
					break x;
				}
			}
		}
		x: for (int x = bounds[0].x; x < bounds[0].x + checkSize; x++) {
			for (int y = bounds[1].y - checkSize; y < bounds[1].y; y++) {
				if (obstacle.getRGB(x, y) == colorCode.getRGB()) {
					corners[2] = true;
					break x;
				}
			}
		}
		x: for (int x = bounds[1].x - checkSize; x < bounds[1].x; x++) {
			for (int y = bounds[1].y - checkSize; y < bounds[1].y; y++) {
				if (obstacle.getRGB(x, y) == colorCode.getRGB()) {
					corners[3] = true;
					break x;
				}
			}
		}
		return corners;
	}

	private Point[] fillColor(BufferedImage img, Color key, Color fill, int x, int y, Point[] bounds) {
		img.setRGB(x, y, fill.getRGB());
		if (x > 0 && img.getRGB(x - 1, y) == key.getRGB()) {
			if (x - 1 < bounds[0].x) {
				bounds[0].x = x - 1;
			}
			bounds = fillColor(img, key, fill, x - 1, y, bounds);
		}
		if (x < img.getWidth() - 1 && img.getRGB(x + 1, y) == key.getRGB()) {
			if (x + 1 > bounds[1].x) {
				bounds[1].x = x + 1;
			}
			bounds = fillColor(img, key, fill, x + 1, y, bounds);
		}
		if (y > 0 && img.getRGB(x, y - 1) == key.getRGB()) {
			if (y - 1 < bounds[0].y) {
				bounds[0].y = y - 1;
			}
			bounds = fillColor(img, key, fill, x, y - 1, bounds);
		}
		if (y < img.getHeight() - 1 && img.getRGB(x, y + 1) == key.getRGB()) {
			if (y + 1 > bounds[1].y) {
				bounds[1].y = y + 1;
			}
			bounds = fillColor(img, key, fill, x, y + 1, bounds);
		}
		return bounds;
	}

	private void drawTraceInfo(Shot s) {
		Graphics2D g2d = overlay.getGraphics();
		g2d.setColor(Color.CYAN);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawString("Selected trace:", 50, 300);
		g2d.drawString("Power: " + s.getPower(), 50, 330);
		g2d.drawString("Angle: " + s.getAngle(), 50, 350);
		g2d.drawRect(40, 310, 125, 50);
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

	private BufferedImage blurImage(BufferedImage img, int radius) {
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
		ownTank = blurImage(ownTank, 5);
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
		enemyTank = blurImage(enemyTank, 5);

		for (int t = 0; t < 7; t++) {
			tanks[t] = new Point();
			Color brightestColor = null;
			for (int x = 0; x < enemyTank.getWidth(); x++) {
				y: for (int y = 0; y < enemyTank.getHeight(); y++) {
					if (x > ownTank.x - 40 && x < ownTank.x + 40) {
						continue;
					}
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
					probes[i].step(enemyTanks, obstacles);
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
		g2d.setStroke(new BasicStroke(1f));
		g2d.setColor(new Color(0f, 0f, 0.8f, 1f));
		for (int i = 0; i < probes.length; i++) {
			if (i == selectedShot) {
				continue;
			}
			Shot s = new Shot(ownTank, probes[i].getPower(), probes[i].getAngle(), overlay.getShootLeft());
			while (s.isAlive()) {
				s.step(enemyTanks, obstacles);
				g2d.drawLine(s.getPrevX(), s.getPrevY(), s.getX(), s.getY());
			}
		}
		BufferedImage selectedShotImg = new BufferedImage(overlay.getWidth(), overlay.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D shotg2d = selectedShotImg.createGraphics();
		shotg2d.setColor(new Color(0.6f, 0.6f, 1f, 1f));
		shotg2d.setStroke(new BasicStroke(2f));
		Shot s = new Shot(ownTank, probes[selectedShot].getPower(), probes[selectedShot].getAngle(), overlay.getShootLeft());
		while (s.isAlive()) {
			s.step(enemyTanks, obstacles);
			shotg2d.drawLine(s.getPrevX(), s.getPrevY(), s.getX(), s.getY());
		}
//		selectedShotImg = blurImage(selectedShotImg, 5);
//		ampColors(selectedShotImg);
		shotg2d = selectedShotImg.createGraphics();
		s = new Shot(ownTank, probes[selectedShot].getPower(), probes[selectedShot].getAngle(), overlay.getShootLeft());
		while (s.isAlive()) {
			s.step(enemyTanks, obstacles);
			shotg2d.drawLine(s.getPrevX(), s.getPrevY(), s.getX(), s.getY());
		}
		g2d.drawImage(selectedShotImg, 0, 0, null);
	}
	
	private void ampColors(BufferedImage img) {
		Color brightestColor = null;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color pixel = new Color(img.getRGB(x, y));
				if (brightestColor == null || pixel.getRed() + pixel.getGreen() + pixel.getBlue() > brightestColor.getRed()
						+ brightestColor.getGreen() + brightestColor.getBlue()) {
					brightestColor = pixel;
				}
			}
		}
		float redAmp = 255f / brightestColor.getRed();
		float greenAmp = 255f / brightestColor.getGreen();
		float blueAmp = 255f / brightestColor.getBlue();
		float amp = Math.max(redAmp, Math.max(greenAmp, blueAmp));
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color c = new Color(img.getRGB(x, y));
				int r = Math.min((int) (c.getRed() * amp), 255);
				int g = Math.min((int) (c.getGreen() * amp), 255);
				int b = Math.min((int) (c.getBlue() * amp), 255);
				Color newc = new Color(r, g, b, c.getAlpha());
				img.setRGB(x, y, newc.getRGB());
			}
		}
	}
	
	private void drawObstacles() {
		Graphics2D g2d = overlay.getGraphics();
		for (int i = 0; i < obstacles.length; i++) {
			obstacles[i].draw(g2d);
		}
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
		drawObstacles();
		drawTankPositions();
		drawTraceInfo(probes[selectedShot]);
		overlay.repaint();
	}
}
