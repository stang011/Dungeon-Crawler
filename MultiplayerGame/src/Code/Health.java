package Code;

import java.io.Serializable;

public class Health implements Serializable {
	
	private int cooldown;
	private int cooldownLeft = 0;
	private boolean ableToActivate = false;
	
	public Health(int cd) {
		cooldown = cd;
	}
	
	public void activate() {
		if(ableToActivate) {
			cooldownLeft = cooldown;
		}
	}
	
	public void update() {
		cooldownLeft--;
		if(cooldownLeft <= 0) {
			ableToActivate = true;
			cooldownLeft = 0;
		}
	}
	
	public void reset() {
		cooldownLeft = 0;
		ableToActivate = true;
	}
	
	public int getCooldown() {
		return cooldown;
	}
	public int getCooldownLeft() {
		return cooldownLeft;
	}
}