package Code;

import java.io.Serializable;

public class Ability implements Serializable {
	
	private int cooldown;
	private int cooldownLeft = 0;
	
	private boolean hasDuration = false;
	private int duration;
	private int durationLeft;
	
	private boolean ableToActivate = false;
	
	private boolean abilityActive = false;
	
	public Ability(int cd, boolean hasDur, int dur) {
		cooldown = cd;
		hasDuration = hasDur;
		if(hasDuration)
			duration = dur;
	}
	
	public void activate() {
		if(ableToActivate) {
			cooldownLeft = cooldown;
			if(hasDuration) {
				durationLeft = duration;
				abilityActive = true;
			}
		}
	}
	
	public void update() {
		cooldownLeft--;
		if(cooldownLeft <= 0) {
			ableToActivate = true;
			cooldownLeft = 0;
		}
		if(abilityActive) {
			durationLeft--;
			if(durationLeft < 0) {
				abilityActive = false;
				durationLeft = 0;
			}
		}
		
	}
	
	public void reset() {
		cooldownLeft = 0;
		durationLeft = 0;
		ableToActivate = true;
		abilityActive = false;
	}
	
	public boolean isActive() {
		return abilityActive;
	}

	public int getCooldown() {
		return cooldown;
	}
	public int getCooldownLeft() {
		return cooldownLeft;
	}
	public int getDuration() {
		return duration;
	}
	public int getDurationLeft() {
		return durationLeft;
	}
}