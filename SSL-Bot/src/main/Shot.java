package main;

import java.awt.Point;

public class Shot {

	// 1u = 1 screen width
	private float G = 0.19621259f; // in u/s^2
	private float INITIAL_VERTICAL_VELOCITY = -0.51005462f; // in u/s

	// 0.662943495u max height
	// in 2.5995s

	private final float dt = 0.01f; // trace iterations

	private float posX;
	private float posY;
	private float velX;
	private float velY;
	private float prevPosX;
	private float prevPosY;
	private int power;
	private int angle;
	private int hitTank;
	private boolean hit = false;
	private boolean alive = true;

	public Shot(Point start, int power, int angle, boolean left) {

		this.power = power;
		this.angle = angle;

		posX = (float) start.x / Bot.get().getScreenWidth();
		posY = (float) start.y / Bot.get().getScreenWidth();

		float y = INITIAL_VERTICAL_VELOCITY * (power / 100f);

		rotateVel(0, y, Math.toRadians(90 - angle));

		if (left) {
			velX *= -1;
		}
	}

	public void step(Point[] enemyTanks, Obstacle[] obstacles) {
		prevPosX = posX;
		prevPosY = posY;
		posX += velX * dt;
		posY += velY * dt;
		velY += G * dt;

		if (prevPosX == 0) {
			prevPosX = posX;
		}
		if (prevPosY == 0) {
			prevPosY = posY;
		}

		for (int i = 0; i < obstacles.length; i++) {
			if (obstacles[i].getType() == Obstacle.LINE) {
				double length = obstacles[i].getStart().distanceSq(obstacles[i].getEnd());
				double adist = obstacles[i].getStart().distanceSq(getX(), getY());
				double bdist = obstacles[i].getEnd().distanceSq(getX(), getY());
				if (adist < length && bdist < length) {
					Point p1 = obstacles[i].getStart();
					Point p2 = obstacles[i].getEnd();
					if (isLeft(p1, p2, getX(), getY()) != isLeft(p1, p2, getPrevX(), getPrevY())) {
						posX = prevPosX;
						posY = prevPosY;

						int w = p2.x - p1.x;
						int h = p2.y - p1.y;

						float nx = -h;
						float ny = w;
						
						float normal_angle = (float) Math.atan2(nx, ny);
						float shot_angle = (float) Math.atan2(velX, velY);
						float theta = normal_angle - shot_angle;
						rotateVel(velX, velY, Math.PI - theta * 2);
					}
				}
			}
		}

		for (int i = 0; i < enemyTanks.length; i++) {
			if (enemyTanks[i].distance(getX(), getY()) < 10) {
				hit = true;
				hitTank = i;
			}
		}
		if (posY > (float) Bot.get().getOverlay().getScreenshotHeight() / Bot.get().getScreenWidth() || posX < 0 || posX > 1 || hit) {
			alive = false;
		}
	}

	private void rotateVel(float x, float y, double rangle) {

		velX = (float) (Math.cos(rangle) * x - Math.sin(rangle) * y);
		velY = (float) (Math.sin(rangle) * x + Math.cos(rangle) * y);
	}

	private boolean isLeft(Point a, Point b, int x, int y) {
		return ((b.x - a.x) * (y - a.y) - (b.y - a.y) * (x - a.x)) > 0;
	}

	public boolean isAlive() {
		return alive;
	}

	public int getX() {
		return (int) (posX * Bot.get().getScreenWidth());
	}

	public int getY() {
		return (int) (posY * Bot.get().getScreenWidth());
	}

	public int getPrevX() {
		return (int) (prevPosX * Bot.get().getScreenWidth());
	}

	public int getPrevY() {
		return (int) (prevPosY * Bot.get().getScreenWidth());
	}

	public boolean hit() {
		return hit;
	}

	public int getPower() {
		return power;
	}

	public int getAngle() {
		return angle;
	}

	public int getHitTank() {
		return hitTank;
	}
}
