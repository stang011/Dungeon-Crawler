package Code;

import java.io.Serializable;

public class CombatText implements Serializable {
	
	private int x;
	private int y;
	private String damage;
	
	private int dy = - 1;
	private int duration = 80;
	
	private boolean visible = true;
	
	private boolean critical = false;
	
	public CombatText(int x, int y, String damage, boolean crit) {
		this.x = x;
		this.y = y;
		this.damage = damage;
		critical = crit;
	}
	
	public void update() {
		duration--;
		y += dy;
		if(duration <= 0) {
			visible = false;
		}
	}
	
	public boolean isVisible() {
		return visible;
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public String getText() {
		return damage;
	}
	public boolean isCritical() {
		return critical;
	}
}