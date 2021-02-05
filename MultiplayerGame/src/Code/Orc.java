package Code;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

public class Orc implements Serializable {
	
	private String name = "Orc";
	
	private double x;
	private double y;
	private double dx = 0;
	private double dy = 0;
	
	private double tempDX = 0;
	private double tempDY = 0;
	private final int controlCD = 15;
	private int controlCountdown = controlCD;
	
	private int width = 180;
	private int height = 140;
	
	private double difficulty = 1;
	
	private final double SPEED = 3.5;
	private final int range = 800;
	private final int followRange = 1000;
	private final int meleeRange = 90;
	private int homeX;
	private int homeY;
	
	private final int findHomeCD = 300;
	private int findHomeLeft = findHomeCD;
	
	private ArrayList<Player> targets = new ArrayList<>();
	private int targetX;
	private int targetY;
	
	private double theta;
	private double returnTheta;
	
	private int directionIndex;
	private boolean attacking;
	private boolean walking;
	
	private boolean canAttack = false;
	private int attackingCooldown = 0;
	private final int ATTACK_COUNTER = 60;
	
	private boolean followingPlayer;
	
	private Rectangle attackBoundingBox = null;
	
	private int daggerDamageLower = 40;
	private int daggerDamageUpper = 70;
	private double daggerDamage;
	
	private int healthLower = 310;
	private int healthUpper = 370;
	
	private double health;
	private double healthLeft;
	
	private boolean dead = false;
	
	private boolean displayCombatText = false;
	
	private double experienceProvided;
	private boolean xpUsed = false;
	
	// For Knight attack debuff
	private int immunity = 30;
	
	private boolean visible = true;
	private int visibleCounter = 200;
	
	NumberGenerator nG = new NumberGenerator();
	
	ArrayList<Sound> sounds = new ArrayList<>();
	
	public Orc(int x, int y, int index) {
		this.x = x;
		this.y = y;
		homeX = x;
		homeY = y;
		this.directionIndex = index;
		attacking = false;
		walking = false;
		followingPlayer = true;
		
		// Init health and damage numbers
		health = nG.getRandomNumber(healthLower, healthUpper, 0) * difficulty;
		healthLeft = health;
				
		daggerDamage = nG.getRandomNumber(daggerDamageLower, daggerDamageUpper, 0);
		experienceProvided = nG.getRandomNumber(60, 70, 0);
	}
	
	public void update(ArrayList<Player> players) {
		targets = players;
		if(targets != null && targets.size() > 0)
			canAttack = true;
		
		if(!attacking && !dead && controlCountdown == 0) {
			x += dx;
			y += dy;
		}
		if(controlCountdown > 0) {
			controlCountdown--;
			x += tempDX;
			y += tempDY;
			if(controlCountdown < 0) {
				controlCountdown = 0;
			}
		}
		
		if(canAttack && !dead) {
			checkHomeRange();
			calculateDirection();
		}
		if(attacking && !dead) {
			attack();
		}
		
		updateDeath();
	}
	
	public void checkHomeRange() {
		double distanceFromHome = Math.sqrt(Math.pow(homeX - x, 2) + Math.pow(homeY - y, 2));
		if(distanceFromHome > followRange || (findHomeLeft == 0 && !followingPlayer)) {
			followingPlayer = false;
			returnTheta = Math.atan2((homeY - y),(homeX - x));
			returnTheta += Math.PI / 2;
			
			dx = Math.sin(returnTheta) * SPEED;
			dy = Math.cos(returnTheta) * -1 * SPEED;
			walking = true;
			
			setDirectionIndex(returnTheta);
			findHomeLeft = findHomeCD;
		}
		// If pretty close to home, but off some, recalculate to try to 
		// generate a path that will get within the 30 bound below
		if(distanceFromHome < 200 && !followingPlayer) {
			returnTheta = Math.atan2((homeY - y),(homeX - x));
			returnTheta += Math.PI / 2;
			
			dx = Math.sin(returnTheta) * SPEED;
			dy = Math.cos(returnTheta) * -1 * SPEED;
			
			setDirectionIndex(returnTheta);
		}
		if(distanceFromHome < 30 && !followingPlayer) {
			followingPlayer = true;
			dx = 0;
			dy = 0;
			walking = false;
		}
		findHomeLeft--;
		if(findHomeLeft <0) {
			findHomeLeft = 0;
		}
	}
	
	public void calculateDirection() {
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
		
		if(minDistance <= range && followingPlayer) {
			targetX = targets.get(index).getCenterX();
			targetY = targets.get(index).getCenterY();
			theta = Math.atan2((targetY - (y + (height / 2))),(targetX - (x + (width / 2))));
			theta += Math.PI / 2;
			
			dx = Math.sin(theta) * SPEED;
			dy = Math.cos(theta) * -1 * SPEED;
			
			walking = true;
			
			// Calculate the new direction of the archer
			setDirectionIndex(theta);
			
			followingPlayer = true;
		}
		
		// Within melee range of target
		if(minDistance <= meleeRange) {
			attacking = true;
		} else {
			attacking = false;
		}
	}
	
	// Updates the direction index of the orc based on the angle of movement
	private void setDirectionIndex(double theta) {
		double degrees = Math.toDegrees(theta);
		if(degrees > -50 && degrees < 80) {
			directionIndex = 0;
		}
		else if(degrees >= 80 && degrees < 140) {
			directionIndex = 1;
		}
		else if(degrees >= 140 && degrees < 190) {
			directionIndex = 2;
		}
		else {
			directionIndex = 3;
		}
	}
	
	public void attack() {
		if(attackingCooldown > 0) {
			attackingCooldown--;
		} else { 
			if(directionIndex == 0) {
				attackBoundingBox = new Rectangle((int)x,(int)y,width,20);
			} else if(directionIndex == 1) {
				attackBoundingBox = new Rectangle((int)x+150,(int)y,20,height);
			} else if(directionIndex == 2) {
				attackBoundingBox = new Rectangle((int)x,(int)y+100,width,20);
			} else if(directionIndex == 3) {
				attackBoundingBox = new Rectangle((int)x,(int)y,20,height);
			}
			attackingCooldown = ATTACK_COUNTER;
		}
	}
	
	public int getAttackCD() {
		return attackingCooldown;
	}
	
	public void udpateDifficulty(double d) {
		difficulty = d;
		health = health * difficulty;
		healthLeft = health;
		daggerDamage += (daggerDamage * difficulty / 5);
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
	
	public void doDamage(double damage) {
		healthLeft -= damage;
		sounds.add(new Sound((int)x, (int)y, "orcHurt"));
		if(healthLeft <= 0) {
			healthLeft = 0;
			dead = true;
		}
	}
	
	public void swordDamage(double damage) {
		if(!dead) {
			if(immunity <= 0) {
				healthLeft -= damage;
				sounds.add(new Sound((int)x, (int)y, "orcHurt"));
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
	
	// Called when Orc comes in contact with a wall.
	// Param direction is passed to determine with direction
	// contact with a wall is made
	// 0 - Orc is on "top" of wall
	// 1 - Orc is on "right" of wall
	// 2 - Orc is on "left" of wall
	// 3 - Orc is on "bottom" of wall
	public void hitBoundingBox(int direction) {
		if(direction == 0 || direction == 3) {
			// He was trying to go right
			if(dx >= 0) {
				tempDX = SPEED;
				tempDY = 0;
			} 
			// He was trying to go left
			else {
				tempDX = -SPEED;
				tempDY = 0;
			}
		} else if(direction == 1 || direction == 2) {
			// He was trying to go "up" the screen
			if(dy <= 0) {
				tempDY = -SPEED;
				tempDX = 0;
			}
			// He was trying to go "down" the screen
			else {
				tempDY = SPEED;
				tempDX = 0;
			}
		}
		controlCountdown = controlCD;
	}
	
	public double getDamage() {
		return daggerDamage;
	}
	
	public void clearSounds() {
		sounds.clear();
	}
	
	public ArrayList<Sound> getSounds() {
		return sounds;
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
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return (int)y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getCenterX() {
		return (int)x + (width / 2);
	}
	public int getCenterY() {
		return (int)y + (height / 2);
	}
	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
	public int getDirection() {
		return directionIndex;
	}
	public boolean isAttacking() {
		return attacking;
	}
	public Rectangle getAttackRectangle() {
		return attackBoundingBox;
	}
	public Rectangle getRectangle() {
		return new Rectangle((int)x + 40, (int)y, width - 80, height);
	}
	public boolean isWalking() {
		return walking;
	}
	public String getName() {
		return name;
	}
	public int getDifficulty() {
		return (int)difficulty;
	}
}