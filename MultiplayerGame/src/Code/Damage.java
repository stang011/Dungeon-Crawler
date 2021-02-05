package Code;

import java.io.Serializable;

public class Damage implements Serializable {
	
	private double damage;
	private boolean critical;

	public Damage(double damage, boolean critical) {
		this.damage = damage;
		this.critical = critical;
	}
	
	public double getDamage() {
		return damage;
	}
	public boolean isCritical() {
		return critical;
	}
}