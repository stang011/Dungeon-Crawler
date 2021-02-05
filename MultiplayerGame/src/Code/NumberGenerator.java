package Code;

import java.io.Serializable;
import java.util.Random;

public class NumberGenerator implements Serializable {
	
	Random random = null;
	private boolean critical = false;
	
	public NumberGenerator() {
		random = new Random();
	}
	
	public int getRandomNumber(int lower, int upper, double critChance) {
		int damage = random.nextInt(upper - lower + 1) + (lower);
		
		double crit = random.nextDouble();
		if(crit <= critChance) {
			damage += (damage * 0.5);
			critical = true;
		} else {
			critical = false;
		}
		
		return damage;
	}

	public boolean isCritical() {
		return critical;
	}
}
