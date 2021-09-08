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
	private int screenshotHeight;

	public Overlay() {
		
		this.setBackground(new Color(0f, 0f, 0f, 0.5f));
		
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
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (img != null) {
			g2d.drawImage(img, 0, 0, null);
		}
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
		if (e.getKeyCode() == KeyEvent.VK_P) {
			minimize();
			Bot.get().start();
			maximize();
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
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(new Font("Courier New", Font.BOLD, 18));
		
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.CYAN);
		g2d.drawString("SSL Bot", 50, 50);
		g2d.drawString("Controls", 50, 100);
		g2d.drawString("Press Esc to exit", 50, 130);
		g2d.drawString("Press x to hide", 50, 150);
		g2d.drawString("Press p to calculate shot", 50, 170);
		g2d.drawRect(40, 110, 400, 200);
		g2d.setColor(Color.RED);
		g2d.drawLine(0, screenshotHeight, size.width, screenshotHeight);
	}
	
	public void drawImage(BufferedImage img) {
		this.img = img;
		repaint();
	}

	public int getScreenshotHeight() {
		return screenshotHeight;
	}
	
}
