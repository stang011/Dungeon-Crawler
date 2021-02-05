package Code;

import java.awt.Rectangle;
import java.io.Serializable;

public class Door implements Serializable {
	
	private int x;
	private int y;
	private int width = 225; 
	private int height = 225;
	private boolean open = false;
	private boolean isOpening = false;
	private boolean locked = false;
	private boolean lockedFromFile = false;
	
	private int openingDur = 45;
	
	private boolean soundUsed = false;
	
	public Door(int x, int y, boolean locked) {
		this.x = x;
		this.y = y;
		this.locked = locked;
		lockedFromFile = locked;
	}
	
	public void update() {
		if(isOpening) {
			openingDur--;
			if(openingDur <= 0) {
				open = true;
			}
		}
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
	
	public int getOpeningDur() {
		return openingDur;
	}
	
	public boolean isOpen() {
		return open;
	}
	public boolean isOpening() {
		return isOpening;
	}
	public boolean isLocked() {
		return locked;
	}
	
	public boolean soundUsed() {
		return soundUsed;
	}
	public void useSound() {
		soundUsed = true;
	}
	
	public void unlock() {
		locked = false;
	}
	
	public void reset() {
		open = false;
		isOpening = false;
		locked = lockedFromFile;
	}
	
	public void openDoor() {
		if(!locked) {
			isOpening = true;
		}
	}
	
	public Rectangle getRectangle() {
		return new Rectangle(x,y,width,165);
	}
}