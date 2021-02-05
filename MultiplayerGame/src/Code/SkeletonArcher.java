package Code;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

public class SkeletonArcher implements Serializable {
	
	private String name = "Archer";
	
	private double x;
	private double y;
	// 0 = N, 1 = E, 2 = S, 3 = W
	private int direction = 0;
	private int arrowIndex = 0;
	
	private int arrowDamageLower = 30;
	private int arrowDamageUpper = 50;
	private double arrowDamage;
	
	private double difficulty = 1;
	
	private int width = 175;
	private int height = 165;
	
	private int range = 700;
	
	private int targetX;
	private int targetY;
	private double theta;
	
	private int healthLower = 210;
	private int healthUpper = 270;
	
	private double health;
	private double healthLeft;
	
	private boolean dead = false;
	
	private boolean isAttacking = false;
	private boolean canAttack = false;
	private int attackingCooldown = 0;
	private final int ATTACK_COUNTER = 100;
	
	private boolean displayCombatText = false;
	
	private double experienceProvided;
	private boolean xpUsed = false;
	
	// For Knight attack debuff
	private int immunity = 30;
	
	private boolean visible = true;
	private int visibleCounter = 200;
	
	private ArrayList<Arrow> arrows = new ArrayList<>();
	private ArrayList<Player> targets = new ArrayList<>();
	
	NumberGenerator nG = new NumberGenerator();
	
	ArrayList<Sound> sounds = new ArrayList<>();
	
	public SkeletonArcher(int x, int y, int startingDirection) {
		this.x = x;
		this.y = y;
		direction = startingDirection;
		
		// Init health and damage numbers
		health = nG.getRandomNumber(healthLower, healthUpper, 0) * difficulty;
		healthLeft = health;
		
		arrowDamage = nG.getRandomNumber(arrowDamageLower, arrowDamageUpper, 0);
		experienceProvided = nG.getRandomNumber(60, 70, 0);
	}
	
	public void update(ArrayList<Player> players) {
		targets = players;
		if(targets != null && targets.size() > 0)
			canAttack = true;
		
		if(canAttack) {
			// Update target x/y by finding the closest player
			double minDistance = 10000.0;
			double distance = 10000.0;
			int index = 0;
			for(int i = 0; i < targets.size(); i++) {
				if(!targets.get(i).isDead())
					distance = Math.sqrt(Math.pow(targets.get(i).getCenterX() - (x + width / 2), 2) + Math.pow(targets.get(i).getCenterY() - (y + height / 2), 2));
				if(distance < minDistance) {
					minDistance = distance;
					index = i;
				}
			}
			
			if(minDistance <= range) {
				targetX = targets.get(index).getCenterX();
				targetY = targets.get(index).getCenterY();
				double theta = Math.atan2((targetY - (y + (height / 2))),(targetX - (x + (width / 2))));
				theta += Math.PI / 2;
				
				// Calculate the new direction of the archer
				double degrees = Math.toDegrees(theta);
				if(degrees > -50 && degrees < 80) {
					direction = 0;
					arrowIndex = 0;
				}
				else if(degrees >= 80 && degrees < 140) {
					direction = 1;
					arrowIndex = 2;
				}
				else if(degrees >= 140 && degrees < 190) {
					direction = 2;
					arrowIndex = 4;
				}
				else {
					direction = 3;
					arrowIndex = 6;
				}
				
				isAttacking = true;
				
				if(!dead) {
					addArrow();
				}
			} else {
				isAttacking = false;
			}
		}
		
		updateArrows();
		updateDeath();
	}
	
	public void addArrow() {
		if(attackingCooldown > 0) {
			attackingCooldown--;
		} else {
			Damage damage = new Damage(arrowDamage, false);
			Arrow arrow = new Arrow(x + (width / 2), y + 80, 0, damage, true, targetX, targetY);
			arrows.add(arrow);
			attackingCooldown = ATTACK_COUNTER;
			sounds.add(new Sound((int)x, (int)y, "shoot"));
		}
	}
	
	public void updateArrows() {
		ArrayList<Arrow> remove = new ArrayList<>();
		for(Arrow a: arrows) {
			a.update();
			if(!a.isVisible() || a.isUsed()) {
				remove.add(a);
			}
		}
		for(Arrow a: remove) {
			arrows.remove(a);
		}
	}
	
	public Rectangle getRectangle() {
		return new Rectangle((int)x+65, (int)y+40, 55, 110);
	}
	
	public void doDamage(double damage) {
		healthLeft -= damage;
		sounds.add(new Sound((int)x, (int)y, "skeletonHit"));
		if(healthLeft <= 0) {
			healthLeft = 0;
			dead = true;
		}
	}
	
	public void updateDeath() {
		if(dead) {
			if(visibleCounter < 0) {
				visible = false;
			} else {
				visibleCounter--;
			}
			if(visibleCounter == 199) {
				setDisplayTextOff();
			}
		}
	}
	
	public void swordDamage(double damage) {
		if(!dead) {
			if(immunity <= 0) {
				healthLeft -= damage;
				sounds.add(new Sound((int)x, (int)y, "skeletonHit"));
				displayCombatText = true;
				if(healthLeft <= 0) {
					healthLeft = 0;
					dead = true;
				}
				immunity = 30;
			} else {
				immunity--;
			}
		}
	}
	
	public void clearSounds() {
		sounds.clear();
	}
	
	public ArrayList<Sound> getSounds() {
		return sounds;
	}
	
	public void udpateDifficulty(double d) {
		difficulty = d;
		health = health * difficulty;
		healthLeft = health;
		arrowDamage += (arrowDamage * difficulty / 5);
	}
	
	public boolean displayText() {
		return displayCombatText;
	}
	public void setDisplayTextOff() {
		displayCombatText = false;
	}
	
	public void respawn() {
		dead = false;
		healthLeft = health;
		visible = true;
	}
	
	public double getHealth() {
		return health;
	}
	public double getHealthLeft() {
		return healthLeft;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public int getImmunity() {
		return immunity;
	}
	
	public int getCenterX() {
		return (int)x + (width / 2);
	}
	public int getCenterY() {
		return (int)y + (height / 2);
	}
	
	public boolean xpUsed() {
		return xpUsed;
	}
	public void useXP() {
		xpUsed = true;
	}
	public double getXP() {
		return experienceProvided * difficulty;
	}
	public Point getPoint() {
		return new Point(getCenterX(), getCenterY());
	}
	public int getX() {
		return (int)x;
	}
	public int getY() {
		return (int)y;
	}
	public int getWidth() {
		return width;
	}
	public boolean isAttacking() {
		return isAttacking;
	}
	public ArrayList<Arrow> getArrows() {
		return arrows;
	}
	public int getDirection() {
		return direction;
	}
	public String getName() {
		return name;
	}
	public int getDifficulty() {
		return (int)difficulty;
	}
}