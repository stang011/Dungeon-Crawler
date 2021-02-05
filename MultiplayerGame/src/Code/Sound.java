package Code;

import java.io.Serializable;

public class Sound implements Serializable {
	
	private int x;
	private int y;
	
	private double range = 1000;
	
	private String soundName;
	
	// Must add a level parameter as to only play a sound is the client player
	// is in the same level instance as the origin of the sound.
	
	public Sound(int x, int y, String name) {
		this.x = x;
		this.y = y;
		soundName = name;
	}
	
	public float calculateVolume(int playerX, int playerY) {
		
		double distance = Math.sqrt((playerY - y)*(playerY - y) + (playerX - x)*(playerX - x));
		
		// Linear formula
		// float volume = (float) (1 - (1/range) * distance);
		
		// Exponential Decay formula
		float volume = (float) Math.exp(-0.0035 * distance);

		if(volume < 0.001f) 
			volume = 0.001f;
		
		return volume;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public String getName() {
		return soundName;
	}
}