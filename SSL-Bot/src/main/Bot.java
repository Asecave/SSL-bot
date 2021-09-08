package main;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class Bot {

	private static Bot instance;

	private Overlay overlay;
	private Robot robot;

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
		BufferedImage info = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);

		// Own tank detection
		Point ownTank = detectOwnTank(screen);

		// Enemy tank detection
//		Point[] enemyTanks = detectEnemyTanks(screen);

		Graphics2D g2d = info.createGraphics();
//		g2d.drawImage(enemyTank, 0, 0, null);
		g2d.setColor(Color.GREEN);
		g2d.drawRect(ownTank.x - 30, ownTank.y - 30, 60, 60);
		g2d.drawLine(ownTank.x - 20, ownTank.y, ownTank.x + 20, ownTank.y);
		g2d.drawLine(ownTank.x, ownTank.y - 20, ownTank.x, ownTank.y + 20);
		g2d.setColor(Color.RED);
//		g2d.drawRect(enemyTankX - 30, enemyTankY - 30, 60, 60);
//		g2d.drawLine(enemyTankX - 20, enemyTankY, enemyTankX + 20, enemyTankY);
//		g2d.drawLine(enemyTankX, enemyTankY - 20, enemyTankX, enemyTankY + 20);
		overlay.drawImage(info);
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
	
	private Point detectOwnTank(BufferedImage screen) {
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
		return pos;
	}
	
//	private Point[] detectEnemyTanks(BufferedImage screen) {
//		BufferedImage enemyTank = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);
//		for (int x = 0; x < screen.getWidth(); x++) {
//			for (int y = 0; y < screen.getHeight(); y++) {
//				Color pixel = new Color(screen.getRGB(x, y));
//				if (match(pixel, new Color(200, 5, 5), 30)) {
//					enemyTank.setRGB(x, y, pixel.getRGB());
//				}
//			}
//		}
//		enemyTank = blurImage(enemyTank);
//		int enemyTankX = 0;
//		int enemyTankY = 0;
//		Color brightestColor = null;
//		for (int x = 0; x < enemyTank.getWidth(); x++) {
//			for (int y = 0; y < enemyTank.getHeight(); y++) {
//				Color pixel = new Color(enemyTank.getRGB(x, y));
//				if (brightestColor == null || pixel.getRed() + pixel.getGreen() + pixel.getBlue() > brightestColor.getRed()
//						+ brightestColor.getGreen() + brightestColor.getBlue()) {
//					brightestColor = pixel;
//					enemyTankX = x;
//					enemyTankY = y;
//				}
//			}
//		}
//	}
}
