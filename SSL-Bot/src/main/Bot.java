package main;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;

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
		BufferedImage screen = robot.createScreenCapture(overlay.size);
		BufferedImage info = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		
		
		overlay.drawImage(info);
	}
}
