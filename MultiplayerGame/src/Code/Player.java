package Code;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

// Jay Schimmoller
// This class manages one player

public class Player implements Serializable {
	private double x, dx = 0;
	private double y, dy = 0;
	private String name;
	private long connectionID;
	
	private Ability ability = null;
	
	private boolean displayCombatText = true;
	
	// Location states
	private int levelIndex = 0; // 0 is the lobby level
	
	private boolean dead = false;
	
	private int width = 94;
	private int height = 150;
	private double totalHealth;
	private double healthLeft;
	
	// Knight stats
	private final double KNIGHTSPEED = 4.5;
	private double KNIGHTHEALTH = 600;
	private double KNIGHTATTACKLOWER = 65;
	private double KNIGHTATTACKUPPER = 90;
	private final double KNIGHTSPIRIT = 0.0002;
	private final double KNIGHTCRIT = 0.05;
	private double knightBuffAmount;
	private double KNIGHTBUFFPERCENT = 0.4;
	
	// Archer stats
	private final double ARCHERSPEED = 5.5;
	private double ARCHERHEALTH = 400;
	private double ARCHERATTACKLOWER = 80;
	private double ARCHERATTACKUPPER = 115;
	private final double ARCHERSPIRIT = 0.00015;
	private final double ARCHERCRIT = 0.1;
	
	// Wizard stats
	private final double WIZARDSPEED = 5.0;
	private double WIZARDHEALTH = 300;
	private double WIZARDATTACKLOWER = 5;
	private double WIZARDATTACKUPPER = 150;
	private final double WIZARDSPIRIT = 0.0005;
	private final double WIZARDCRIT = 0.05;
	
	private double damageMultiplier = 1.0;
	private double damageMitigation = 0.25;
	private double attackDamage;
	
	private Health potion = null;
	
	private double speed = KNIGHTSPEED;
	private boolean moving = false;
	private boolean aPressed, dPressed;
	private boolean wPressed, sPressed;
	
	private PlayerClass playerClass;
	
	private int immunity = 25;
	
	private PlayerDirection direction = PlayerDirection.STILL;
	private int previousIndex = 1;
	private int directionIndex = 2; // Load in looking South
	private int arrowIndex = 4;     // Load in shooting South
	
	private int attackCountDown = 0;
	private boolean ableToMove = true;
	private boolean isAttacking = false;
	private final int SWORD_ATTACK_DUR = 30;
	private final int BOW_ATTACK_DUR = 30;
	private final int WIZ_ATTACK_DUR = 30;
	
	private ArrayList<Arrow> arrows = new ArrayList<Arrow>();
	private ArrayList<Missile> missles = new ArrayList<Missile>();
	
	// Bounding box correction amount
	private final int CORRECTION = 10;
	
	// Interaction button pressed
	private boolean interacting = false;
	private boolean spaceHeld = false;
	private boolean attackInterrupted = false;
	
	private int keys = 0;
	
	NumberGenerator nG = new NumberGenerator();
	
	// List of sounds
	ArrayList<Sound> sounds = new ArrayList<>();
	
	// XP / Level
	private double currentExperience = 0;
	private double levelExperience = 500;
	private int playerLevel = 1;
	
	public Player(int x, int y, String name, long CID) {
		this.x = (double) x;
		this.y = (double) y;
		this.name = name;
		this.connectionID = CID;
		playerClass = PlayerClass.KNIGHT;
		totalHealth = KNIGHTHEALTH;
		healthLeft = totalHealth;
		ability = new Ability(1000, true, 500);
		potion = new Health(1500);
	}
	
	// Update location of the player
	public void update() {
		
		if(!isAttacking && !dead) {
			updateDirection();
		}
		
		updateAttackCounter();
		updateArrows();
		updateMissles();
		updateAbility();
		
		if(ableToMove && !dead) {
			x += dx;
			y += dy;
		}
		
		if(playerClass == PlayerClass.WIZARD && !dead) {
			healthLeft += (WIZARDSPIRIT * WIZARDHEALTH);
			if(healthLeft > totalHealth)
				healthLeft = totalHealth;
		} else if(playerClass == PlayerClass.ARCHER && !dead) {
			healthLeft += (ARCHERSPIRIT * ARCHERHEALTH);
			if(healthLeft > totalHealth)
				healthLeft = totalHealth;
		} else if(playerClass == PlayerClass.KNIGHT && !dead) {
			healthLeft += (KNIGHTSPIRIT * KNIGHTHEALTH);
			if(healthLeft > totalHealth)
				healthLeft = totalHealth;
		}
		
		if(spaceHeld && attackInterrupted)
			attack();
	}
	
	public void changeClass() {
		if(!isAttacking && !moving && healthLeft == totalHealth && ability.getCooldownLeft() == 0 && potion.getCooldownLeft() == 0) {
			if(playerClass == PlayerClass.KNIGHT) {
				playerClass = PlayerClass.ARCHER;
				speed = ARCHERSPEED;
				totalHealth = ARCHERHEALTH;
				healthLeft = totalHealth;
				damageMitigation = 0;
			}
			else if(playerClass == PlayerClass.ARCHER) {
				playerClass = PlayerClass.WIZARD;
				speed = WIZARDSPEED;
				totalHealth = WIZARDHEALTH;
				healthLeft = totalHealth;
				damageMitigation = 0;
			}
			else if(playerClass == PlayerClass.WIZARD) {
				playerClass = PlayerClass.KNIGHT;
				speed = KNIGHTSPEED;
				totalHealth = KNIGHTHEALTH;
				healthLeft = totalHealth;
				damageMitigation = 0.25;
			}
			resetAbilities();
		}
	}
	
	public void updateAttackCounter() {
		if(attackCountDown > 0) {
			attackCountDown--;
			// Add arrow after annimation is almost complete
			if(attackCountDown == 1) {
				if(playerClass == PlayerClass.ARCHER) {
					if(!dead)
						addArrow();
				}
				if(playerClass == PlayerClass.WIZARD) {
					if(!dead)
						addMissile();
				}
			}
		} else {
			ableToMove = true;
			isAttacking = false;
		}
	}
	
	public void updateAbility() {
		ability.update();
		potion.update();
		if(ability.isActive() && ability.getDurationLeft() == 0 && playerClass == PlayerClass.KNIGHT) {
			debuffKnight();
		}
	}
	
	public void resetAbilities() {
		ability.reset();
		potion.reset();
	}
	
	public void attack() {
		// Previous attack must be completed before another attack can occur
		if(attackCountDown == 0) {
			ableToMove = false;
			if(playerClass == PlayerClass.KNIGHT) {
				attackCountDown = SWORD_ATTACK_DUR;
				if(!dead)
					sounds.add(new Sound((int)x, (int)y, "swing"));
			} 
			else if(playerClass == PlayerClass.ARCHER) {
				attackCountDown = BOW_ATTACK_DUR;
				if(!dead)
					sounds.add(new Sound((int)x, (int)y, "shoot"));
			}
			else if(playerClass == PlayerClass.WIZARD) {
				attackCountDown = WIZ_ATTACK_DUR;
				if(!dead)
					sounds.add(new Sound((int)x, (int)y, "spell"));
			}

			if(!dead)
				isAttacking = true;
		} 
	}
	
	public void damage(double damage) {
		healthLeft -= damage - (damage * damageMitigation);
		if(healthLeft <= 0) {
			healthLeft = 0;
			if(levelIndex == 0)
				respawn();
			if(levelIndex == 1)
				dead();
		}
	}
	
	public void swordDamage(double damage) {
		if(immunity <= 0) {
			healthLeft -= damage - (damage * damageMitigation);
			displayCombatText = true;
			if(healthLeft <= 0) {
				healthLeft = 0;
				if(levelIndex == 0)
					respawn();
				if(levelIndex == 1)
					dead();
			}
			immunity = 25;
		} else {
			immunity--;
		}
	}
	
	// Loop to keep trying random locations while not 
	// within bounds of either of the lobby pillars
	public void respawn() {
		boolean goodData = false;
		Random rand = new Random();
		do {
			x = rand.nextInt(1500) + 300;
			y = rand.nextInt(1500) + 300;
			goodData = true;
		}
		while(!goodData);
		
		// Good location while not within pillar location
		// X != (410 - 665) and (1315 - 1370)
		// Y != (560 - 640) and (1220 - 1500)
		
		healthLeft = totalHealth;
	}
	
	public void revive(int x, int y) {
		this.x = x;
		this.y = y;
		healthLeft = totalHealth / 2;
		dead = false;
	}
	
	public void reset() {
		try {
			healthLeft = totalHealth;
			dead = false;
			resetAbilities();
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, e.getStackTrace());
			System.exit(0);
		}
	}
	
	public void dead() {
		dead = true;
	}
	
	public void addArrow() {
		attackDamage = nG.getRandomNumber((int)ARCHERATTACKLOWER, (int)ARCHERATTACKUPPER, ARCHERCRIT) * damageMultiplier;
		boolean crit = nG.isCritical();
		Damage damage = new Damage(attackDamage, crit);
		Arrow arrow = new Arrow(x, y, arrowIndex, damage, false, 0, 0);
		arrows.add(arrow);
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
	
	public void addMissile() {
		attackDamage = nG.getRandomNumber((int)WIZARDATTACKLOWER, (int)WIZARDATTACKUPPER, WIZARDCRIT) * damageMultiplier;
		boolean crit = nG.isCritical();
		Damage damage = new Damage(attackDamage, crit);
		Missile missile = new Missile((int)x, (int)y, directionIndex, false, damage, connectionID, levelIndex);
		missles.add(missile);
	}
	
	public void updateMissles() {
		ArrayList<Missile> remove = new ArrayList<>();
		for(Missile m: missles) {
			m.update();
			if(!m.isVisible()) {
				remove.add(m);
			}
		}
		for(Missile m: remove) 
			missles.remove(m);
	}
	
	public void updateDirection() {
		if(dy < 0 && dx == 0) {
			direction = PlayerDirection.NORTH;
			directionIndex = 0;
			arrowIndex = 0;
		}
		if(dy < 0 && dx > 0) {
			direction = PlayerDirection.NORTHEAST;
			arrowIndex = 1;
		}
		if(dy == 0 && dx > 0) {
			direction = PlayerDirection.EAST;
			directionIndex = 1;
			arrowIndex = 2;
		}
		if(dy > 0 && dx > 0) {
			direction = PlayerDirection.SOUTHEAST;
			arrowIndex = 3;
		}
		if(dy > 0 &&  dx == 0) {
			direction = PlayerDirection.SOUTH;
			directionIndex = 2;
			arrowIndex = 4;
		}
		if(dy > 0 &&  dx < 0) {
			direction = PlayerDirection.SOUTHWEST;
			arrowIndex = 5;
		}
		if(dy == 0 && dx < 0) {
			direction = PlayerDirection.WEST;
			directionIndex = 3;
			arrowIndex = 6;
		}
		if(dy < 0 && dx < 0) {
			direction = PlayerDirection.NORTHWEST;
			arrowIndex = 7;
		}
		if(dy == 0 && dx == 0) {
			direction = PlayerDirection.STILL;
			directionIndex = directionIndex;
		}
		
		if(dy != 0 || dx != 0 && ableToMove) {
			moving = true;
		} else {
			moving = false;
		}
	}
	
	public void useAbility() {
		if(ability.getCooldownLeft() == 0 && !dead) {
			ability.activate();
			if(playerClass == PlayerClass.ARCHER) {
				int oldIndex = arrowIndex;
				for(int i = 0; i < 8; i++) {
					arrowIndex = i;
					damageMultiplier = 1.5;
					addArrow();
				}
				damageMultiplier = 1.0;
				arrowIndex = oldIndex;
				sounds.add(new Sound((int)x, (int)y, "shoot"));
			}
			else if(playerClass == PlayerClass.WIZARD) {
				int oldIndex = directionIndex;
				for(int i = 0; i < 4; i++) {
					directionIndex = i;
					damageMultiplier = 2.0;
					addMissile();
				}
				sounds.add(new Sound((int)x, (int)y, "spell"));
				damageMultiplier = 1.0;
				directionIndex = oldIndex;
			}
			else if(playerClass == PlayerClass.KNIGHT) {
				buffKnight();
			}
		}
	}
	
	public void usePotion() {
		if(potion.getCooldownLeft() == 0 && !dead) {
			potion.activate();
			sounds.add(new Sound((int)x, (int)y, "potion"));
			healthLeft += totalHealth * 0.5;
			if(healthLeft > totalHealth)
				healthLeft = totalHealth;
		}
	}
	
	// Ability buffs knight with +40% hp
	private void buffKnight() {
		knightBuffAmount = totalHealth * KNIGHTBUFFPERCENT;
		totalHealth += knightBuffAmount;
		healthLeft += knightBuffAmount;
	}
	// When the buff falls off, cannot fall below 20% health
	private void debuffKnight() {
		totalHealth -= knightBuffAmount;
		healthLeft -= knightBuffAmount;
		if(healthLeft <= (totalHealth * 0.2)) {
			healthLeft = (totalHealth * 0.2);
		}
		if(dead) {
			healthLeft = 0;
		}
	}
	
	public void clearSounds() {
		sounds.clear();
	}
	
	public ArrayList<Sound> getSounds() {
		return sounds;
	}
	
	public long getConnectionID() {
		return connectionID;
	}
	
	public PlayerDirection getDirection() {
		return direction;
	}
	public void setPreviousIndex(int i) {
		previousIndex = i;
	}
	public int getPreviousIndex() {
		return previousIndex;
	}
	
	public int getDirectionIndex() {
		return directionIndex;
	}
	
	public int getLevelIndex() {
		return levelIndex;
	}
	
	public boolean isMoving() {
		return moving;
	}
	public boolean isAttacking() {
		return isAttacking;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public ArrayList<Arrow> getArrows() {
		return arrows;
	}
	
	public ArrayList<Missile> getMissles() {
		return missles;
	}
	
	public Rectangle getHitBox() {
		return new Rectangle((int)x + CORRECTION, (int)y + CORRECTION, 94 - 2 * CORRECTION, 150 - 2 * CORRECTION);
	}
	
	public Ability getAbility() {
		return ability;
	}
	public Health getPotion() {
		return potion;
	}
	
	public Rectangle getSwordHitBox() {
		// North
		if(directionIndex == 0) {
			return new Rectangle((int)x-80,(int) y-70, 94 + 160, 70);
		// East
		} else if(directionIndex == 1) {
			return new Rectangle((int)x+94,(int)y, 180, 150);
	    // South
		} else if(directionIndex == 2) {
			return new Rectangle((int)x-80,(int)y+150, 94+160, 70);
		// West
		} else if(directionIndex == 3) {
			return new Rectangle((int)x-180,(int)y, 180, 150);
		}
		return null;
	}
	
	public Rectangle getRectangle() {
		return new Rectangle((int)x, (int)y, 94, 150);
	}

	// Location Setters/Getters
	public int getX() {
		return (int) x;
	}
	public int getY() {
		return (int) y;
	}
	public void setX(int x) {
		this.x = (double) x;
	}
	public void setY(int y) {
		this.y = (double) y;
	}
	
	// Center X/Y
	public int getCenterX() {
		return (int) x + (94 / 2);
	}
	public int getCenterY() {
		return (int) y + (150 / 2);
	}
	
	// Movement Setters
	public void setDX(double dx) {
		this.dx = dx;
	}
	public void setDY(double dy) {
		this.dy = dy;
	}
	
	public double getKnightCrit() {
		return KNIGHTCRIT;
	}
	
	// Name getter
	public String getName() {
		return name;
	}
	public void setName(String n) {
		name = n;
	}
	
	public double getHealth() {
		return totalHealth;
	}
	public double getHealthLeft() {
		return healthLeft;
	}
	
	public boolean displayText() {
		return displayCombatText;
	}
	public void setDisplayTextOff() {
		displayCombatText = false;
	}
	
	public String getAttack() {
		if(playerClass == PlayerClass.ARCHER) {
			return (int)ARCHERATTACKLOWER + "-" + (int)ARCHERATTACKUPPER;
		}
		if(playerClass == PlayerClass.KNIGHT) {
			return (int)KNIGHTATTACKLOWER + "-" + (int)KNIGHTATTACKUPPER;
		}
		if(playerClass == PlayerClass.WIZARD) {
			return (int)WIZARDATTACKLOWER + "-" + (int)WIZARDATTACKUPPER;
		}
		return null;
	}
	
	public int getSpeedPercent() {
		if(playerClass == PlayerClass.ARCHER) {
			return 125;
		}
		if(playerClass == PlayerClass.KNIGHT) {
			return 100;
		}
		if(playerClass == PlayerClass.WIZARD) {
			return 110;
		}
		return 0;
	}
	
	public PlayerClass getPlayerClass() {
		return playerClass;
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public void setWidth(int w) {
		width = w;
	}
	public void setHeight(int h) {
		height = h;
	}
	
	public boolean isInteracting() {
		return interacting;
	}
	
	public int getKeys() {
		return keys;
	}
	public void removeKey() {
		keys--;
	}
	public double getSwordLower() {
		return KNIGHTATTACKLOWER;
	}
	public double getSwordUpper() {
		return KNIGHTATTACKUPPER;
	}
	
	public double getCurrentXP() {
		return currentExperience;
	}
	public double getTotalXP() {
		return levelExperience;
	}
	public int getPlayerLevel() {
		return playerLevel;
	}
	
	public void addExperience(double xp) {
		if(!dead) {
			double fallOverXP = 0;
			currentExperience += xp;
			do {
				if(currentExperience >= levelExperience) {
					playerLevel += 1;
					fallOverXP = currentExperience - levelExperience;
					currentExperience = fallOverXP;
					levelExperience += (100 * playerLevel);
					updateStats(playerLevel);
				}
			}	while(currentExperience >= levelExperience);
		}
	}
	
	private void updateStats(int playerLevel) {
		WIZARDHEALTH += 50 + (50 * playerLevel / 5);
		WIZARDATTACKLOWER += 1 + (1 * playerLevel / 5);
		WIZARDATTACKUPPER += 15 + (15 * playerLevel / 5);
		
		KNIGHTHEALTH += 50 + (50 * playerLevel / 5);
		KNIGHTATTACKLOWER += 10 + (10 * playerLevel / 5);
		KNIGHTATTACKUPPER += 10 + (10 * playerLevel / 5);
		
		ARCHERHEALTH += 50 + (50 * playerLevel / 5);
		ARCHERATTACKLOWER += 10 + (10 * playerLevel / 5);
		ARCHERATTACKUPPER += 10 + (10 * playerLevel / 5);
		
		
		if(playerClass == PlayerClass.KNIGHT) {
			totalHealth = KNIGHTHEALTH;
			if(ability.isActive()) {
				totalHealth += knightBuffAmount;
			}
			healthLeft = totalHealth;
		}
		else if(playerClass == PlayerClass.ARCHER) {
			totalHealth = ARCHERHEALTH;
			healthLeft = totalHealth;
		}
		else if(playerClass == PlayerClass.WIZARD) {
			totalHealth = WIZARDHEALTH;
			healthLeft = totalHealth;
		}
	}
	
	public void keyPressed(String s) {
		
		int key = Integer.parseInt(s);
		
		if(key == KeyEvent.VK_W) {
			dy = -1 * speed;
			wPressed = true;
		}
		if(key == KeyEvent.VK_S) {
			dy = speed;
			sPressed = true;
		}
		if(key == KeyEvent.VK_A) {
			dx = -1 * speed;
			aPressed = true;
		}
		if(key == KeyEvent.VK_D) {
			dx = speed;
			dPressed = true;
		}
		if(key == KeyEvent.VK_SPACE) {
			attack();
			spaceHeld = true;
			attackInterrupted = false;
		}
		if(key == KeyEvent.VK_E) {
			if(levelIndex == 0)  { // In Lobby
				changeClass();
			} else { 
				if(!dead)
					interacting = true;
			}
		}
		if(key == KeyEvent.VK_L) {
			levelIndex += 1;
			if(levelIndex > 1)
				levelIndex = 1;
			x = 170;
			y = 6800;
		}
		if(key == KeyEvent.VK_O) {
			levelIndex -= 1;
			if(levelIndex < 0) {
				levelIndex = 0;
			}
			x = 1000;
			y = 400;
			dead = false;
		}
		if(key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
			useAbility();
			if(spaceHeld)
				attackInterrupted = true;
		}
		if(key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) {
			usePotion();
			if(spaceHeld)
				attackInterrupted = true;
		}
		if(key == KeyEvent.VK_Y) {
			addExperience(5000);
		}
	}
	
	public void keyReleased(String s) {
		int key = Integer.parseInt(s);
		
		if(key == KeyEvent.VK_W) {
			if(sPressed)
				dy = speed;
			else
				dy = 0;
			wPressed = false;
		}
		if(key == KeyEvent.VK_S) {
			if(wPressed)
				dy = -1 * speed;
			else
				dy = 0;
			sPressed = false;
		}
		if(key == KeyEvent.VK_A) {
			if(dPressed)
				dx = speed;
			else
				dx = 0;
			aPressed = false;
		}
		if(key == KeyEvent.VK_D) {
			if(aPressed)
				dx = -1 * speed;
			else
				dx = 0;
			dPressed = false;
		}
		if(key == KeyEvent.VK_E) {
			interacting = false;
		}
		if(key == KeyEvent.VK_SPACE) {
			spaceHeld = false;
			attackInterrupted = false;
		}
	}
}