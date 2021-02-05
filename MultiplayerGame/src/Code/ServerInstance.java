package Code;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

// Jay Schimmoller
// This class manages launching and communicating with the server

public class ServerInstance implements ConnectionListener {
	Server server = null;
	ServerGUI gui = null;
	private boolean running = true;
	private int numCon = 0;
	private Encoder encoder = new Encoder();
	private HashMap<Long, Player> playerData = new HashMap<>();
	private ArrayList<Level> levels = null;
	private ArrayList<Door> doors = null;
	private ArrayList<SkeletonArcher> skeleArchers = null;
	private ArrayList<Orc> orcs = null;
	private ArrayList<Point> enemyLocations = null;
	
	private NumberGenerator nG = new NumberGenerator();
	private ArrayList<CombatText> combatText = new ArrayList<>();
	
	private AudioManager audioManager = new AudioManager();
	
	public ServerInstance() {
		server = new Server();
		server.setOnTransmission(this);
		initGUI();
		initLevels();
		Thread loop = new Thread() {
			public void run() {
				final long targetTPS = 60;
				// Number of nanoseconds between each frame and tick
				final long targetNSPT = 1000000000 / targetTPS;
				// Game Loop
				long lastTime = System.nanoTime();
				long tickTime = 0;
				while(running) {
					long currentTime = System.nanoTime();
					long deltaTime = currentTime - lastTime; // Change in time from last loop cycle
					tickTime += deltaTime; // Increase tick counter

					// Catch up on ticks
					while (tickTime >= targetNSPT) {
						tickTime -= targetNSPT;
						try {
							tick();
							updateGUI();
						} catch (UnknownConnectionException e) {
							e.printStackTrace();
							System.out.println("UnknownConnection");
						}
					}

					// Update time tracker
					lastTime = currentTime;
				}
			}
		};
		loop.start();
	}
	
	// Starts the server based on the port
	public void start(int port) {
		gui.ready();
		server.startServer(port);
	}
	
	public void initGUI() {
		gui = new ServerGUI();
	}
	
	public void initLevels() {
		levels = new ArrayList<Level>();
		Level levelZero = new Level("Level0", gui.getDifficulty());
		levels.add(levelZero);
		Level levelOne = new Level("Level1", gui.getDifficulty());
		levels.add(levelOne);
		
		skeleArchers = levels.get(1).getSkeleArchers();
		orcs = levels.get(1).getOrcs();
		updateDifficulty();
		enemyLocations = levels.get(1).getEnemyLocations();
	}
	
	public void updateGUI() {
		if(!gui.isRunning()) {
			server.stopServer();
			System.exit(0);
		}
		if(gui.needReset()) {
			resetServer();
			gui.setReset(false);
		}
	}
	
	// Update method to handle things like player input
	public void tick() throws UnknownConnectionException {	
		updatePlayerLocations();
		updateEnemyLocations();
		updateMissleKnowledge();
		updateEnemyKnowledge();
		checkCollisions();
		updateEnemyXP();
		updateEnemies();
		updateCombatText();
		updateAudioManager();
		sendPacket();
		audioManager.clear();
	}
	
	public void updateDifficulty() {
		for(SkeletonArcher sa: skeleArchers) {
			sa.udpateDifficulty(gui.getDifficulty());
		}
		for(Orc o: orcs) {
			o.udpateDifficulty(gui.getDifficulty());
		}
	}
	
	public void resetServer() {
		// Loop through Players and reset their location based on current level
		ArrayList<Player> players = getPlayerData();
		for(Player p: players) {
			if(p.getLevelIndex() == 0) {
				p.setX(1000);
				p.setY(400);
			}
			else if(p.getLevelIndex() == 1) {
				p.setX(170);
				p.setY(6800);
			}
			p.reset();
		}
		initLevels();
	}
	
	public void updatePlayerLocations() {
		ArrayList<Player> players = getPlayerData();
		if(players.size() > 0) {
			for(int i = 0; i < players.size(); i++) {
				players.get(i).update();
			}
		}
	}
	
	public void updateEnemyLocations() {
		enemyLocations.clear();
		for(SkeletonArcher sa: skeleArchers) {
			if(!sa.isDead())
				enemyLocations.add(sa.getPoint());
		}
		for(Orc o: orcs) {
			if(!o.isDead())
				enemyLocations.add(o.getPoint());
		}
	}
	
	public void updateMissleKnowledge() {
		ArrayList<Player> players = getPlayerData();
		ArrayList<Player> levelOnePlayers = new ArrayList<Player>();
		for(Player p: players) {
			if(p.getLevelIndex() == 0) {
				levelOnePlayers.add(p);
			}
		}
		if(players.size() > 0) {
			ArrayList<Missile> missles = new ArrayList<Missile>();
			for(Player p: players) {
				missles.addAll(p.getMissles());
			}
			for(Missile m: missles) {
				int levelIndex = m.getLevelIndex();
				if(levelIndex == 0)
					m.setPVPTargets(levelOnePlayers);
				else if(levelIndex == 1) {
					m.setEnemyTargets(enemyLocations);
				}
			}
		}
	}
	
	public void updateEnemyKnowledge() {
		for(SkeletonArcher s: skeleArchers) {
			s.update(getPlayerData());
		}
		for(Orc o: orcs) {
			o.update(getPlayerData());
		}
	}
	
	public void updateCombatText() {
		ArrayList<CombatText> remove = new ArrayList<>();
		for(CombatText cT: combatText) {
			cT.update();
			if(!cT.isVisible()) {
				remove.add(cT);
			}
		}
		for(CombatText r: remove) {
			combatText.remove(r);
		}
	}
	
	public void checkCollisions() {
		
		// Level 0 Collision Handling - Lobby / PVP Enabled
		ArrayList<Rectangle> boundingLevelZero = levels.get(0).getBoundingBoxes();
		ArrayList<Player> players = getPlayerData();
		ArrayList<Arrow> arrows = new ArrayList<Arrow>();
		ArrayList<Missile> missles = new ArrayList<Missile>();
		for(Player p: players) {
			// If the player is in the lobby, get their arrows/missles and check for boundary collisions
			if(p.getLevelIndex() == 0) {
				arrows.addAll(p.getArrows());
				missles.addAll(p.getMissles());
				Rectangle playerRect = p.getRectangle();
				for(Rectangle r: boundingLevelZero) {
					if(playerRect.intersects(r)) {
						// Player on "top" of boundary
						if(p.getY() + p.getHeight() <= r.y + 6) {
							p.setY(r.y - p.getHeight());
						}
						// Player on "bottom" of boundary
						if(p.getY() + 6 >= r.y + r.height) {
							p.setY(r.y + r.height);
						}
						// Player on "left" of boundary
						if(p.getX() + p.getWidth() <= r.x + 6) {
							p.setX(r.x - p.getWidth());
						}
						// Player on "right" of boundary
						if(p.getX() >= r.x + r.width - 6) {
							p.setX(r.x + r.width);
						}
					}
				}
			}
		}
		// Level 0 - Lobby Arrow/Bounding Box Collisions and Arrow/Player Collisions
		for(Arrow a: arrows) {
			Rectangle arrowRect = a.getRectangle();
			// Loop through boundaries to check for arrow collisions
			for(Rectangle r: boundingLevelZero) {
				if(arrowRect.intersects(r)) {
					a.hitBoundingBox();
					if(!a.soundUsed()) {
						audioManager.addSound(new Sound(a.getX(), a.getY(), "impact"));
						a.useSound();
					}
				}
			}
			// Loop through the players to check for arrow collisions
			for(Player p: players) {
				Rectangle playerRect = p.getHitBox();
				if(playerRect.intersects(arrowRect)) {
					// Only get hit with an arrow if in the same level as that arrow. Both in level 0
					if(p.getLevelIndex() == 0) {
						a.setUsed(true);
						p.damage(a.getAttackDamage());
						CombatText cT = new CombatText(p.getCenterX() - 10, p.getY(), Integer.toString((int)a.getAttackDamage()), a.isCritical());
						combatText.add(cT);
						audioManager.addSound(new Sound(p.getX(), p.getY(), "hurt"));
					}
				}
			}
		}
		// Level 0 - Lobby Knight/Other Player Collisions
		ArrayList<SwordAttack> swordAttacks = new ArrayList<>();
		// Add all attacking knight's bounding boxes
		for(Player p: players) {
			if(p.isAttacking() && p.getPlayerClass() == PlayerClass.KNIGHT) {
				if(p.getLevelIndex() == 0) {
					int swordAttack = nG.getRandomNumber((int)p.getSwordLower(), (int)p.getSwordUpper(), p.getKnightCrit());
					boolean crit = nG.isCritical();
					Damage damage = new Damage(swordAttack, crit);
					swordAttacks.add(new SwordAttack(p.getSwordHitBox(), damage));
				}
			}
		}
		// Check if any of those attacks have gone through players
		for(Player p: players) {
			Rectangle playerRect = p.getHitBox();
			for(SwordAttack sA: swordAttacks) {
				Rectangle r = sA.getBoundingBox();
				if(playerRect.intersects(r)) {
					if(p.getLevelIndex() == 0) {
						p.swordDamage(sA.getAttackDamage());
						if(p.displayText()) {
							CombatText cT = new CombatText(p.getCenterX() - 10, p.getY(), Integer.toString((int)sA.getAttackDamage()), sA.isCritical());
							combatText.add(cT);
							p.setDisplayTextOff();
							audioManager.addSound(new Sound(p.getX(), p.getY(), "hurt"));
						}
					}
				}
			}
		}
		swordAttacks = null;
		// Level 0 - Lobby Wizard Attack/Other Player Collisions
		for(Missile m: missles) {
			Rectangle missleRect = m.getRectangle();
			// Check for boundary collision
			for(Rectangle r: boundingLevelZero) {
				if(missleRect.intersects(r)) {
					m.setVisible(false);
				}
			}
			// Check for player collision
			for(Player p: players) {
				Rectangle playerRect = p.getHitBox();
				if(playerRect.intersects(missleRect)) {
					// Only do damage if not the caster
					if(p.getConnectionID() != m.getCasterID()) {
						if(p.getLevelIndex() == 0) {
							m.setVisible(false);
							p.damage(m.getDamage());
								CombatText cT = new CombatText(p.getCenterX() - 10, p.getY(), Integer.toString((int)m.getDamage()), m.isCritical());
								combatText.add(cT);
								audioManager.addSound(new Sound(p.getX(), p.getY(), "hurt"));
						}
					}
				}
			}
		}
		// End of Level Zero - Lobby Collision Handling
		
		// Level one collision handling. PVP Disabled
		ArrayList<Rectangle> boundingLevelOne = new ArrayList<Rectangle>();
		boundingLevelOne.addAll(levels.get(1).getBoundingBoxes());
		// Add in closed doors to boundingBoxes
		doors = levels.get(1).getDoors();
		for(Door d: doors) {
			d.update();
			if(!d.isOpen()) {
				boundingLevelOne.add(d.getRectangle());
			}
			else {
				boundingLevelOne.remove(d.getRectangle());
			}
		}
		players = getPlayerData();
		ArrayList<Arrow> arrowsOne = new ArrayList<Arrow>();
		ArrayList<Missile> misslesOne = new ArrayList<Missile>();
		
		ArrayList<Player> deadPlayers = new ArrayList<>();
		
		for(Player p: players) {
			if(p.getLevelIndex() == 1) {
				Rectangle playerRect = p.getRectangle();
				arrowsOne.addAll(p.getArrows());
				misslesOne.addAll(p.getMissles());
				for(Rectangle r: boundingLevelOne) {
					if(playerRect.intersects(r)) {
						// Player on "top" of boundary
						if(p.getY() + p.getHeight() <= r.y + 6) {
							p.setY(r.y - p.getHeight());
						}
						// Player on "bottom" of boundary
						if(p.getY() + 6 >= r.y + r.height) {
							p.setY(r.y + r.height);
						}
						// Player on "left" of boundary
						if(p.getX() + p.getWidth() <= r.x + 6) {
							p.setX(r.x - p.getWidth());
						}
						// Player on "right" of boundary
						if(p.getX() >= r.x + r.width - 6) {
							p.setX(r.x + r.width);
						}
					}
					
					if(p.isDead()) {
						deadPlayers.add(p);
					}
				}
				for(Door d: doors) {
					Rectangle rD = d.getRectangle();
					Rectangle rayCast = new Rectangle(p.getX(), p.getY() - 50, 94, 150);
					if(rayCast.intersects(rD) && p.isInteracting() && !p.isDead()) {
						boolean doorLocked = d.isLocked();
						int numKeys = p.getKeys();
						if(doorLocked && numKeys > 0) {
							p.removeKey();
							d.unlock();
						}
						
						if(!d.soundUsed() && !d.isLocked()) {
							audioManager.addSound(new Sound(d.getX(), d.getY(), "door"));
							d.useSound();
						}
						d.openDoor();
					}
				}
			}
		}
		
		// Check Orcs against level 1 wall bounding boxes
		// Increments and Decrements of 40/80 account for the
		// shift in the centering and size of the bounding box
		// on the orc.
		for(Orc o: orcs) {
			Rectangle orcRect = o.getRectangle();
			for(Rectangle r: boundingLevelOne) {
				if(orcRect.intersects(r)) {
					// Orc on "top" of boundary
					if(o.getY() + o.getHeight() <= r.y + 6) {
						o.setY(r.y - o.getHeight());
						o.hitBoundingBox(0);
					}
					// Orc on "bottom" of boundary
					if(o.getY() + 6 >= r.y + r.height) {
						o.setY(r.y + r.height);
					}
					// Orc on "left" of boundary
					if(o.getX() + 40 + o.getWidth() - 80 <= r.x + 6) {
						o.setX(r.x - o.getWidth() + 40);
					}
					// Orc on "right" of boundary
					if(o.getX() + 40 >= r.x + r.width - 6) {
						o.setX(r.x + r.width - 40);
						o.hitBoundingBox(1);
					}
				}
			}
		}
		
		ArrayList<Rectangle> passableBoundingLevelOne = new ArrayList<>();
		passableBoundingLevelOne.addAll(boundingLevelOne);
		passableBoundingLevelOne.removeAll(levels.get(1).getBoundingBoxesPassable());
		
		ArrayList<Arrow> skeleArcherArrows = new ArrayList<>();
		for(SkeletonArcher sa: skeleArchers) {
			skeleArcherArrows.addAll(sa.getArrows());
		}
		// Check SkeletonArcher arrows against bounding boxes/Players
		for(Arrow a: skeleArcherArrows) {
			Rectangle arrowRect = a.getSkeleArcherRectangle();
			for(Rectangle r: passableBoundingLevelOne) {
				if(arrowRect.intersects(r)) {
					a.hitBoundingBox();
					if(!a.soundUsed()) {
						audioManager.addSound(new Sound(a.getX(), a.getY(), "impact"));
						a.useSound();
					}
				}
			}
			for(Player p: players) {
				if(p.getLevelIndex() == 1) {
					Rectangle playerRect = p.getHitBox();
					if(arrowRect.intersects(playerRect) && !p.isDead()) {
						a.setUsed(true);
						p.damage(a.getAttackDamage());
						audioManager.addSound(new Sound(p.getX(), p.getY(), "hurt"));
					}
				}
			}
		}
		
		// Loop through Orcs to check for attacking. Check against bounding boxes/Players
		for(Orc o: orcs) {
			if(o.isAttacking()) {
				Rectangle daggerRect = o.getAttackRectangle();
				for(Player p: players) {
					Rectangle playerRect = p.getHitBox();
					if(daggerRect.intersects(playerRect) && !p.isDead() && o.getAttackCD() == 40 && !o.isDead()) {
						p.damage(o.getDamage());
						audioManager.addSound(new Sound(p.getX(), p.getY(), "hurt"));
					}
				}
			}
		}
		
		for(Arrow a: arrowsOne) {
			Rectangle arrowRect = a.getRectangle();
			// Loop through boundaries to check for player arrow collisions
			for(Rectangle r: passableBoundingLevelOne) {
				if(arrowRect.intersects(r)) {
					a.hitBoundingBox();
					if(!a.soundUsed()) {
						audioManager.addSound(new Sound(a.getX(), a.getY(), "impact"));
						a.useSound();
					}
				}
			}
			// Loop through SkeletonArchers to check for player arrow collisions
			for(SkeletonArcher sa: skeleArchers) {
				Rectangle skeleArcherRect = sa.getRectangle();
				if(arrowRect.intersects(skeleArcherRect) && !sa.isDead()) {
					CombatText cT = new CombatText(sa.getCenterX(), sa.getY() + 50, Integer.toString((int)a.getAttackDamage()), a.isCritical());
					combatText.add(cT);
					sa.doDamage(a.getAttackDamage());
					a.setUsed(true);
				}
			}
			// Loop through Orcs to check for player arrow collisions
			for(Orc o: orcs) {
				Rectangle orcRect = o.getRectangle();
				if(arrowRect.intersects(orcRect) && !o.isDead()) {
					CombatText cT = new CombatText(o.getCenterX(), o.getY() + 50, Integer.toString((int)a.getAttackDamage()), a.isCritical());
					combatText.add(cT);
					o.doDamage(a.getAttackDamage());
					a.setUsed(true);
				}
			}
		}
		for(Missile m: misslesOne) {
			Rectangle missleRect = m.getRectangle();
			// Check for boundary collision
			for(Rectangle r: passableBoundingLevelOne) {
				if(missleRect.intersects(r)) {
					m.setVisible(false);
				}
			}
			// Loop through SkeletonArchers to check for player missile collisions
			for(SkeletonArcher sa: skeleArchers) {
				Rectangle skeleArcherRect = sa.getRectangle();
				if(missleRect.intersects(skeleArcherRect) && !sa.isDead()) {
					CombatText cT = new CombatText(sa.getCenterX(), sa.getY() + 50, Integer.toString((int)m.getDamage()), m.isCritical());
					combatText.add(cT);
					sa.doDamage(m.getDamage());
					m.setVisible(false);
				}
			}
			// Loop through Orcs to check for player missile collisions
			for(Orc o: orcs) {
				Rectangle orcRect = o.getRectangle();
				if(missleRect.intersects(orcRect) && !o.isDead()) {
					CombatText cT = new CombatText(o.getCenterX(), o.getY() + 50, Integer.toString((int)m.getDamage()), m.isCritical());
					combatText.add(cT);
					o.doDamage(m.getDamage());
					m.setVisible(false);
				}
			}
		}
		
		// Loop through SkeletonArchers to check for Knight player attack collision
		ArrayList<SwordAttack> knightAttacks = new ArrayList<>();
		// Add all attacking knight's bounding boxes
		for(Player p: players) {
			if(p.isAttacking() && p.getPlayerClass() == PlayerClass.KNIGHT) {
				if(p.getLevelIndex() == 1) {
					int swordAttack = nG.getRandomNumber((int)p.getSwordLower(), (int)p.getSwordUpper(), p.getKnightCrit());
					boolean crit = nG.isCritical();
					Damage damage = new Damage(swordAttack, crit);
					knightAttacks.add(new SwordAttack(p.getSwordHitBox(), damage));
				}
			}
		}
		// Check if any of those attacks have gone through SkeletonArchers
		for(SkeletonArcher sa: skeleArchers) {
			Rectangle skeleArcherRect = sa.getRectangle();
			for(SwordAttack sA: knightAttacks) {
				Rectangle r = sA.getBoundingBox();
				if(skeleArcherRect.intersects(r)) {
					sa.swordDamage(sA.getAttackDamage());
					if(sa.displayText()) {
						CombatText cT = new CombatText(sa.getCenterX(), sa.getY() + 50, Integer.toString((int)sA.getAttackDamage()), sA.isCritical());
						combatText.add(cT);
						sa.setDisplayTextOff();
					}
				}
			}
		}
		// Check if any of those attacks have gone through Orcs
		for(Orc o: orcs) {
			Rectangle orcRect = o.getRectangle();
			for(SwordAttack sA: knightAttacks) {
				Rectangle r = sA.getBoundingBox();
				if(orcRect.intersects(r)) {
					o.swordDamage(sA.getAttackDamage());
					if(o.displayText()) {
						CombatText cT = new CombatText(o.getCenterX(), o.getY() + 50, Integer.toString((int)sA.getAttackDamage()), sA.isCritical());
						combatText.add(cT);
						o.setDisplayTextOff();
					}
				}
			}
		}
		
		// Check revival attempts
		for(Player p: players) {
			Rectangle player = p.getRectangle();
			for(Player dp: deadPlayers) {
				Rectangle deadPlayer = dp.getRectangle();
				if(player.intersects(deadPlayer) && p.getConnectionID() != dp.getConnectionID() && p.isInteracting()) {
					dp.revive(p.getX(), p.getY());
				}
			}
		}
		
		boundingLevelOne.clear();
		passableBoundingLevelOne.clear();
		// End of Level One - Dungeon Collision Handling
	}
	
	private void updateEnemyXP() {
		for(SkeletonArcher sa: skeleArchers) {
			if(sa.isDead()) {
				if(!sa.xpUsed()) {
					addExperience(sa.getXP());
					sa.useXP();
				}
			}
		}
		
		for(Orc o: orcs) {
			if(o.isDead()) {
				if(!o.xpUsed()) {
					addExperience(o.getXP());
					o.useXP();
				}
			}
		}
	}
	
	private void updateEnemies() {
		ArrayList<SkeletonArcher> remove = new ArrayList<>();
		for(SkeletonArcher sa: skeleArchers) {
			if(!sa.isVisible()) {
				remove.add(sa);
			}
		}
		
		for(SkeletonArcher r: remove) {
			skeleArchers.remove(r);
		}
		
		ArrayList<Orc> removeOrcs = new ArrayList<>();
		for(Orc o: orcs) {
			if(!o.isVisible()) {
				removeOrcs.add(o);
			}
		}
		for(Orc r: removeOrcs) {
			orcs.remove(r);
		}
	}
	
	public void addExperience(double xp) {
		ArrayList<Player> players = getPlayerData();
		for(Player p: players) {
			if(p.getLevelIndex() == 1)
				p.addExperience(xp);
		}
	}
	
	public void updateAudioManager() {
		ArrayList<Player> players = getPlayerData();
		ArrayList<Sound> allSounds = new ArrayList<>();
		
		for(Player p: players) {
			allSounds.addAll(p.getSounds());
			p.clearSounds();
		}
		for(SkeletonArcher sA: skeleArchers) {
			allSounds.addAll(sA.getSounds());
			sA.clearSounds();
		}
		for(Orc o: orcs) {
			allSounds.addAll(o.getSounds());
			o.clearSounds();
		}
		
		for(Sound s: allSounds) {
			audioManager.addSound(s);
		}
	}
	
	public void sendPacket() throws UnknownConnectionException {
		try {
			//Packet packet = new Packet(playerData);
			ArrayList<Long> IDs = getPlayerIDs();
			for(int i = 0; i < IDs.size(); i++) {
				/*
				// Send each connected player the list of players
				server.sendMessage(IDs.get(i), "Packet_" + encoder.encodeObj(packet));
				
				// Send each connected player a copy of their own player
				Packet playerPacket = new Packet(playerData.get(getPlayerIDs().get(i)));
				server.sendMessage(IDs.get(i), "Player_" + encoder.encodeObj(playerPacket));
				
				// Send each player a copy of the doors in the level
				Packet doorPacket = new Packet(doors);
				server.sendMessage(IDs.get(i), "Door_" + encoder.encodeObj(doorPacket));
				
				// Send each player a copy of the skeleton archers in the level
				Packet skeleArcherPacket = new Packet(skeleArchers, true);
				server.sendMessage(IDs.get(i), "SkeleArchers_" + encoder.encodeObj(skeleArcherPacket));
				*/
				Packet one = new Packet(playerData, playerData.get(getPlayerIDs().get(i)), doors, skeleArchers, orcs, combatText, audioManager);
				server.sendMessage(IDs.get(i), "One_" + encoder.encodeObj(one));
			}
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getStackTrace());
			System.exit(0);
		}
	}
	
	public void handle(ConnectionEvent e) {
		switch(e.getCode()) {
		case CONNECTION_ESTABLISHED:
			String guiMessage = "";
			numCon++;
			playerData.put(e.getConnectionID(), new Player(1000,400,"player", e.getConnectionID()));
			
			guiMessage += "Player " + e.getConnectionID() + " has connected\n";
			guiMessage += "Number of connections: " + numCon + "\n";
			
			gui.update(guiMessage);
			
			break;
		case TRANSMISSION_RECEIVED:
			String data = e.getData();
			Player player = playerData.get(e.getConnectionID());
			// Update the player's name
			if(data.substring(0, 4).equals("name")) {
				player.setName(e.getData().substring(5));
				guiMessage = "Connection " + e.getConnectionID() + " is " + e.getData().substring(5) + "\n\n";
				gui.update(guiMessage);
			}
			// Parse and handle keyboard input from the player
			if(data.substring(0, 2).equals("KP")) {
				player.keyPressed(data.substring(2));
			}
			if(data.substring(0, 2).equals("KR")) {
				player.keyReleased(data.substring(2));
			}
			break;
		case CONNECTION_TERMINATED:
			guiMessage = "";
			String name = playerData.get(e.getConnectionID()).getName();
			playerData.remove(e.getConnectionID());
			numCon--;
			System.out.println("Number of connections: " + numCon);
			
			guiMessage += "Player " + name + " has disconnected\n";
			guiMessage += "Number of connections: " + numCon + "\n\n";
			
			gui.update(guiMessage);
			
			break;
		}
	}
	
	public ArrayList<Player> getPlayerData() {
		return new ArrayList<Player> (playerData.values());
	}
	public ArrayList<Long> getPlayerIDs() {
		return new ArrayList<Long> (playerData.keySet());
	}
	
	// Main method to call an instance of the server
	public static void main(String[] args) {
		ServerInstance serverInstance = new ServerInstance();
		serverInstance.start(2112);
	}
}