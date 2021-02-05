package Code;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

public class Missile implements Serializable {

	private double x;
	private double y;
	private double dx;
	private double dy;
	private int direction;
	private boolean seeking;
	private double damage;
	private boolean critical;
	private boolean visible = true;
	private long casterID;
	private final double SPEED = 6;
	private final double RANGE = 250;
	private ArrayList<Player> targets;
	private ArrayList<Point> enemies;
	
	private boolean PVPMissle = false;
	private int levelIndex = 0;
	
	private int SEEK_TIME = 200;
	
	public Missile(int x, int y, int directionIndex, boolean seeking, Damage d, long casterID, int levelIndex) {
		this.x = (double)x;
		this.y = (double)y;
		this.seeking = seeking;
		damage = d.getDamage();
		critical = d.isCritical();
		this.casterID = casterID;
		this.levelIndex = levelIndex;
		direction = directionIndex;
		initSpeed();
	}
	
	private void initSpeed() {
		// North
		if(direction == 0) {
			dx = 0;
			dy = -SPEED;
			// Player width = 94, missle width = 30
			x += (94 / 2) - (30 / 2);
		} 
		// East
		else if(direction == 1) {
			dx = SPEED;
			dy = 0;
			// Player height = 150, missle height = 30;
			y += (150 / 2) - (30 / 2);
			x += 60;
		} 
		// South
		else if(direction == 2) {
			dx = 0;
			dy = SPEED;
			x += (94 / 2) - (30 / 2);
			y += 150;
		} 
		// West
		else if(direction == 3) {
			dx = -SPEED;
			dy = 0;
			// Player height = 150, missle height = 30;
			y += (150 / 2) - (30 / 2);
			x -= 30;
		}
	}
		
	public void update() {
		
		if(seeking && PVPMissle) {
			updateDirection();
		}
		if(seeking && !PVPMissle) {
			findEnemy();
		}
		x += dx;
		y += dy;
		SEEK_TIME--;
	}
	
	// Loop through the targets to find the closest one
	private void updateDirection() {
		double minDistance = 10000.0;
		int index = 0;
		for(int i = 0; i < targets.size(); i++) {
			double distance = Math.sqrt(Math.pow(targets.get(i).getCenterX() - x, 2) + Math.pow(targets.get(i).getCenterY() - y, 2));
			if(distance < minDistance) {
				minDistance = distance;
				index = i;
			}
		}
		if(minDistance < RANGE && SEEK_TIME > 0) {
			int destX = targets.get(index).getCenterX();
			int destY = targets.get(index).getCenterY();
			double theta = Math.atan2((destY - y),(destX - x));
			theta += Math.PI / 2;
			dx = Math.sin(theta) * SPEED;
			dy = Math.cos(theta) * -1 * SPEED;
		}
	}
	
	private void findEnemy() {
		double minDistance = 10000.0;
		int index = 0;
		for(int i = 0; i < enemies.size(); i++) {
			double distance = Math.sqrt(Math.pow(enemies.get(i).getX() - x, 2) + Math.pow(enemies.get(i).getY() - y, 2));
			if(distance < minDistance) {
				minDistance = distance;
				index = i;
			}
		}
		if(minDistance < RANGE && SEEK_TIME > 0) {
			int destX = (int)enemies.get(index).getX();
			int destY = (int)enemies.get(index).getY();
			double theta = Math.atan2((destY - y),(destX - x));
			theta += Math.PI / 2;
			dx = Math.sin(theta) * SPEED;
			dy = Math.cos(theta) * -1 * SPEED;
		}
	}
	
	public void setPVPTargets(ArrayList<Player> players) {
		// Make a deep copy of the players
		targets = new ArrayList<Player>();
		for(Player p: players) {
			targets.add(p);
		}
		// Remove caster from list of targets
		int index = 0;
		for(int i = 0; i < targets.size(); i++) {
			if(targets.get(i).getConnectionID() == casterID) {
				index = i;
			}
		}
		if(targets.size() > 0) {
			targets.remove(index);
			setSeeking(true);
			PVPMissle = true;
		}
	}
	
	public void setEnemyTargets(ArrayList<Point> enemies) {
		this.enemies = enemies;
		if(enemies.size() > 0) 
			seeking = true;
		else
			seeking = false;
	}
	
	private void setSeeking(boolean s) {
		if(targets.size() > 0) {
			seeking = s;
		}
		else {
			seeking = false; 
		}
	}
	
	public Rectangle getRectangle() {
		return new Rectangle((int)x, (int)y, 30, 30);
	}
	
	public void hitBoundingBox() {
		visible = false;
	}
	
	public long getCasterID() {
		return casterID;
	}
	
	public void setVisible(boolean b) {
		visible = b;
	}
	public boolean isVisible() {
		return visible;
	}
	
	public int getLevelIndex() {
		return levelIndex;
	}
	
	public boolean isCritical() {
		return critical;
	}
	
	public int getX() {
		return (int)x;
	}
	public int getY() {
		return (int)y;
	}
	public double getDamage() {
		return damage;
	}
}