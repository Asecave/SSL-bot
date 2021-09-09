package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Overlay extends JPanel implements KeyListener {

	private JFrame frame;
	private Rectangle size;
	private BufferedImage img;
	private Graphics2D g2d;
	private int screenshotHeight;

	public Overlay() {

		setOpaque(false);

		size = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		screenshotHeight = size.height - size.width / 12;

		frame = new JFrame("SSL Bot");
		frame.setUndecorated(true);
		frame.setBackground(new Color(0f, 0f, 0f, 0f));
		frame.setBounds(0, 0, size.width, size.height);
		frame.setAlwaysOnTop(true);

		frame.addKeyListener(this);
		frame.add(this);
		frame.setVisible(true);

		img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		g2d = img.createGraphics();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(0f, 0f, 0f, 0.5f));
		g2d.clearRect(0, 0, size.width, size.height);
		g2d.fillRect(0, 0, size.width, size.height);
		g2d.drawImage(img, 0, 0, null);
		drawInfo(g2d);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			frame.dispose();
			System.exit(0);
		}
		if (e.getKeyCode() == KeyEvent.VK_X) {
			minimize();
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			minimize();
			Thread loop = new Thread() {
				public void run() {
					Bot.get().start();
				};
			};
			loop.start();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	public void minimize() {
		frame.setState(Frame.ICONIFIED);
	}

	public void maximize() {
		frame.setState(Frame.NORMAL);
	}

	private void drawInfo(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(new Font("Courier New", Font.BOLD, 18));

		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.CYAN);
		g2d.drawString("SSL Bot", 50, 50);
		g2d.drawString("Controls", 50, 100);
		g2d.drawString("Press Esc to exit", 50, 130);
		g2d.drawString("Press x to hide", 50, 150);
		g2d.drawString("Press space to calculate shot", 50, 170);
		g2d.drawRect(40, 110, 400, 200);
		g2d.setColor(Color.RED);
		g2d.drawLine(0, screenshotHeight, size.width, screenshotHeight);
	}

	public Graphics2D getGraphics() {
		return g2d;
	}

	public int getScreenshotHeight() {
		return screenshotHeight;
	}

	public Rectangle getScreenSize() {
		return size;
	}

	public void clear() {
		g2d.setBackground(new Color(0f, 0f, 0f, 0f));
		g2d.clearRect(0, 0, img.getWidth(), img.getHeight());
	}
}
