package Code;

import java.awt.Rectangle;

public class SpawnZone {
	
	private int x, y, width, height;
	
	private int numberOfSA;
	private int numberOfOrc;
	
	public SpawnZone(int x, int y, int width, int height, int nSA, int nOrc) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.numberOfSA = nSA;
		this.numberOfOrc = nOrc;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public int getNumberOfSkeletonArchers() {
		return numberOfSA;
	}
	
	public int getNumberOfOrcs() {
		return numberOfOrc;
	}
	
	public Rectangle getRectangle() {
		return new Rectangle(x, y, width, height);
	}
}