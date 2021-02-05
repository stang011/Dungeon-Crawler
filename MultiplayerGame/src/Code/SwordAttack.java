package Code;

import java.awt.Rectangle;

public class SwordAttack {
	
	private Rectangle boundingBox;
	private double attackDamage;
	
	private boolean statusTextUsed = false;
	
	private boolean critical;
	
	public SwordAttack(Rectangle boundingBox, Damage d) {
		this.boundingBox = boundingBox;
		attackDamage = d.getDamage();
		critical = d.isCritical();
	}
	
	public Rectangle getBoundingBox() {
		return boundingBox;
	}
	public double getAttackDamage() {
		return attackDamage;
	}
	public boolean isCritical() {
		return critical;
	}
	
	public void setStatusTextUsed(boolean s) {
		statusTextUsed = s;
	}
	public boolean statusTextUsed() {
		return statusTextUsed;
	}
}