package Code;

import java.awt.Rectangle;
import java.io.Serializable;

public class Arrow implements Serializable {
	private double x;
	private double y;
	private double dx;
	private double dy;
	private int arrowIndex = 0;
	private final double SPEED = 15;
	private final double SKELE_SPEED = 10;
	
	private double attackDamage = 0;
	
	private boolean critical = false;
	
	private int width;
	private int height;
	
	// Seeking for SkeletonArcher
	private boolean seeking = false;
	private double targetX = 0;
	private double targetY = 0;
	private double theta = 0;
	
	private boolean isVisible = true;
	private boolean used = false;
	private int countDown = 60;
	
	private boolean soundUsed = false;
	
	public Arrow(double x, double y, int arrowIndex, Damage aD, boolean seeking, double tarX, double tarY) {
		this.x = x;
		this.y = y;
		this.arrowIndex = arrowIndex;
		this.seeking = seeking;
		targetX = tarX;
		targetY = tarY;
		attackDamage = aD.getDamage();
		critical = aD.isCritical();
		if(!seeking)
			calculateArrow();
		else
			calculateTarget();
	}
	
	public void update() {
		if(isVisible) {
			x += dx;
			y += dy;
		} else {
			countDown--;
			if(countDown < 0) {
				countDown = 0;
			}
		}
	}
	
	public void calculateArrow() {
		// North
		if(arrowIndex == 0) {
			dx = 0;
			dy = -SPEED;
			// Width of player = 94. Width of arrow = 14
			x += (94 / 2) - (14 / 2);
			width = 14;
			height = 104;
		// North-East
		} else if(arrowIndex == 1) {
			dx = SPEED / Math.sqrt(2);
			dy = -SPEED / Math.sqrt(2);
			width = 80;
			height = 80;
		// East
		} else if(arrowIndex == 2) {
			dx = SPEED;
			dy = 0;
			// Height of player = 150, Height of arrow = 14
			y += (150 / 2) - (14 / 2);
			x += 50;
			width = 104;
			height = 14;
		// South-East
		} else if(arrowIndex == 3) {
			dx = SPEED / Math.sqrt(2);
			dy = SPEED / Math.sqrt(2);
			width = 80;
			height = 80;
			x += 50;
			y += 50;
		// South
		} else if(arrowIndex == 4) {
			dx = 0;
			dy = SPEED;
			// Width of player = 94, Width of arrow = 14;
			// Move arrow to below body
			x += (94 / 2) - (14 / 2);
			y += 75;
			width = 14;
			height = 104;
		// South-West
		} else if(arrowIndex == 5) {
			dx = -SPEED / Math.sqrt(2);
			dy = SPEED / Math.sqrt(2);
			width = 80;
			height = 80;
		// West
		} else if(arrowIndex == 6) {
			dx = -SPEED;
			dy = 0;
			// Height of player = 150, Height of arrow = 14
			y += (150 / 2) - (14 / 2);
			x -= 50;
			width = 104;
			height = 14;
		// North-West
		} else if(arrowIndex == 7) {
			dx = -SPEED / Math.sqrt(2);
			dy = -SPEED / Math.sqrt(2);
			width = 80;
			height = 80;
		}
	}
	
	public void calculateTarget() {
		theta = Math.atan2((targetY - y),(targetX - x));
		theta += Math.PI / 2;
		dx = Math.sin(theta) * SKELE_SPEED;
		dy = Math.cos(theta) * -1 * SKELE_SPEED;
		width = 14;
		height = 104;
	}
	
	public int getX() {
		return (int)x;
	}
	public int getY() {
		return (int)y;
	}
	
	public double getAttackDamage() {
		return attackDamage;
	}
	
	public boolean isCritical() {
		return critical;
	}
	
	public Rectangle getRectangle() {
		// North
		if(arrowIndex == 0) {
			return new Rectangle((int)x,(int)y,15,15);	
		// North-East
		} else if(arrowIndex == 1) {
			return new Rectangle((int)x+60,(int)y,15,15);	
		// East
		} else if(arrowIndex == 2) {
			return new Rectangle((int)x+89,(int)y,15,15);		
		// South-East
		} else if(arrowIndex == 3) {
			return new Rectangle((int)x+60,(int)y+60,15,15);		
		// South
		} else if(arrowIndex == 4) {
			return new Rectangle((int)x,(int)y+89,15,15);		
		// South-West
		} else if(arrowIndex == 5) {
			return new Rectangle((int)x,(int)y+60,15,15);		
		// West
		} else if(arrowIndex == 6) {
			return new Rectangle((int)x,(int)y,15,15);		
		// North-West
		} else if(arrowIndex == 7) {
			return new Rectangle((int)x,(int)y,15,15);		
		}
		return null;
	}
	
	public Rectangle getSkeleArcherRectangle() {
		
		double xStart = x + Math.sin(theta) * 50;
		double yStart = (y + 104/2) + Math.cos(theta) * -1 * 50;
		
		return new Rectangle((int)xStart, (int)yStart, 15, 15);
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public void setWidth(int w) {
		width = w;
	}
	public void setHeight(int h) {
		height = h;
	}
	
	public int getIndex() {
		return arrowIndex;
	}
	
	public double getTheta() {
		return theta;
	}
	
	public void hitBoundingBox() {
		isVisible = false;
	}
	public void setUsed(boolean u) {
		used = u;
	}
	
	public boolean isUsed() {
		return used;
	}
	
	public boolean soundUsed() {
		return soundUsed;
	}
	public void useSound() {
		soundUsed = true;
	}
	
	public boolean isVisible() {
		if(countDown == 0) {
			return false;
		} else {
			return true;
		}
	}
}