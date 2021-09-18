package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class Obstacle {

	public static final int LINE = 0;
	public static final int CIRCLE = 1;

	private Point start;
	private Point end;
	private Point center;
	private int radius;

	private int type;

	public Obstacle(Point start, Point end) {
		type = LINE;
		this.start = start;
		this.end = end;
	}

	public Obstacle(Point center, int radius) {
		type = CIRCLE;
		this.center = center;
		this.radius = radius;
	}

	public void draw(Graphics2D g2d) {
		if (type == LINE) {
			g2d.setColor(new Color(255, 155, 255, 255));
			Point min = new Point(Math.min(start.x, end.x), Math.min(start.y, end.y));
			Point max = new Point(Math.max(start.x, end.x), Math.max(start.y, end.y));
			g2d.drawRect(min.x, min.y, max.x - min.x, max.y - min.y);
			g2d.drawLine(start.x, start.y, end.x, end.y);
		}
		if (type == CIRCLE) {
			g2d.setColor(new Color(255, 155, 255, 255));
			g2d.drawRect(center.x - radius, center.y - radius, radius * 2, radius * 2);
			g2d.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
		}
	}

	public int getType() {
		return type;
	}
	
	public Point getStart() {
		return start;
	}

	public void setStart(Point start) {
		this.start = start;
	}

	public Point getEnd() {
		return end;
	}

	public void setEnd(Point end) {
		this.end = end;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}
