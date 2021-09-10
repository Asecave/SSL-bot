package main;

import java.awt.Point;

public class Shot {
	
	// 1u = 1 screen width
	private float G = 0.19621259f; // in u/s^2
	private float INITIAL_VERTICAL_VELOCITY = -0.51005462f; // in u/s

	// 0.662943495u max height
	// in 2.5995s
	
	private final float dt = 0.05f; // trace iterations
	
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
		
		float dirX = 0;
		float dirY = INITIAL_VERTICAL_VELOCITY * (power / 100f);
		
		float rangle = (float) Math.toRadians(90 - angle);
		
		velX = (float) (Math.cos(rangle) * dirX - Math.sin(rangle) * dirY);
		velY = (float) (Math.sin(rangle) * dirX + Math.cos(rangle) * dirY);
		
		if (left) {
			velX *= -1;
		}
	}
	
	public void step(Point[] enemyTanks) {
		prevPosX = posX;
		prevPosY = posY;
		posX += velX * dt;
		posY += velY * dt;
		velY += G * dt;
		
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
