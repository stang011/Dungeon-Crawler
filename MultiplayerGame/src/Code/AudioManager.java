package Code;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class AudioManager implements Serializable {
	
	private Queue<Sound> sounds = new LinkedList<>();
	
	public void addSound(Sound newSound) {
		sounds.add(newSound);
	}
	public Queue<Sound> getQueue() {
		return sounds;
	}
	
	public void clear() {
		sounds.clear();
	}
}