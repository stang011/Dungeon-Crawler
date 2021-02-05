package Code;

// Jay Schimmoller

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Game extends JPanel {
	private String ip = "";
	private String name = null;
	private JFrame frame;
	private boolean running = false;
	private BufferedReader fromServer;
	private PrintWriter toServer;
	private Encoder encoder = new Encoder();
	private int screenWidth;
	private int screenHeight;
	private int centerX = 0;
	private int centerY = 0;
	
	private double xScale;
	private double yScale;
	
	// Background information
	private int backgroundWidth = 0;
	private int backgroundHeight = 0;
	private Image backgroundImage = null;
	private Image mask = null;
	private Image levelOne = null;
	private ArrayList<Door> doors = null;
	private Image doorImages[] = new Image[3];
	private Image lockImage;
	
	// Key
	private Image keyImage = null;
	
	// Client player information
	private Player player = null;
	private boolean playerConnected = false;
	// Knight Images
	private Image knightStill[] = new Image[4];
	private Image knightMoving[] = new Image[4];
	private Image knightAttack[] = new Image[4];
	private Image knightDead = null;
	private Image knightAbility = null;
	// Archer Images
	private Image archerStill[] = new Image[4];
	private Image archerMoving[] = new Image[4];
	private Image archerAttack[] = new Image[4];
	private Image arrowImages[] = new Image[8];
	private Image archerDead = null;
	private Image archerAbility = null;
	// Wizard Images
	private Image wizardStill[] = new Image[4];
	private Image wizardMoving[] = new Image[4];
	private Image wizardAttack[] = new Image[4];
	private Image missle = null;
	private Image wizardDead = null;
	private Image wizardAbility = null;
	
	private Image healImage = null;
	
	// UI Images
	private Image hudOverlay = null;
	private Image exitImage = null;
	
	// Character information panels
	private int panelX = 0;
	private int panelY = 130;
	private Image wizardPanel = null;
	private Image knightPanel = null;
	private Image archerPanel = null;
	private boolean showPanel = false;
	
	// Enemy Images
	private Image SkeletonArcherShooting[] = new Image[4];
	private Image SkeletonArcherIdle[] = new Image[4];
	private ArrayList<SkeletonArcher> skeleArchers = null;
	private ArrayList<Arrow> skeleArcherArrows = new ArrayList<>();
	private Image skeletonArcherDead = null;
	
	private ArrayList<Orc> orcs = null;
	private Image orcIdle[] = new Image[4];
	private Image orcAttacking[] = new Image[4];
	private Image orcWalking[] = new Image[4];
	private Image orcDead = null;
	
	// CombatText
	private ArrayList<CombatText> combatText = null;
	
	private int index = 0; // Direction Array Index
	private PlayerClass playerClass;
	private ArrayList<Player> playerData = null;
	Font nameFont = new Font("Courier", Font.BOLD, 20);
	Font controlsFont = new Font("Courier", Font.BOLD, 23);
	Font escFont = new Font("Courier", Font.BOLD, 25);
	Font titleFont = new Font("Courier", Font.BOLD, 40);
	Font levelFont = new Font("Courier", Font.BOLD, 28);
	
	/*
	 * Create a hashmap of sound effects and a sound class with a location. In the server, create a sound when something should make a sound.
	 * Give the sound a name and location and add it to a list of sounds (AudioManager). Pass that AudioManager to the client and have the client
	 * loop through all of the sounds in the manager and play them with varied volume based on the distance from the player (0.0f - 1.0f) until the list
	 * of sounds in the manager is empty. 
	 */
	
	// Audio Assets
	private AudioManager audioManager = null;
	private HashMap<String, ArrayList<AudioPlayer>> audioMap = new HashMap<>();
	private boolean playAudio = true;
	/*
	private AudioPlayer swing;
	private AudioPlayer spell;
	private AudioPlayer shoot;
	*/
	
	public Game(String ip, String name, int sHeight, int sWidth, double xScale, double yScale) {
		
		LoadingScreen();
		
		this.ip = ip;
		this.name = name;
		this.screenWidth = sWidth;
		this.screenHeight = sHeight;
		this.xScale = xScale;
		this.yScale = yScale;
		frame = new JFrame();
		frame.setUndecorated(true);
		
		centerX = (screenWidth / 2) - (94 / 2);
		centerY = (screenHeight / 2) - (150 / 2);
		
		this.addKeyListener(new KAdapter());
		this.setFocusable(true);
		this.setBackground(Color.BLACK);
		this.setDoubleBuffered(true);
		frame.add(this);
		frame.setEnabled(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setEnabled(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		initBackground();
		initPlayer();
		initProjectiles();
		initAttackAnnimations();
		initDying();
		initEnemies();
		initAbilities();
		initUI();
		initPanels();
		initAudio();
		run();
	}
	
	public void LoadingScreen() {
		repaint();
	}
	
	public void initBackground() {
		ImageIcon ii = new ImageIcon(Game.class.getResource("Images/lobbyFour.png"));
		if(ii != null) {
			backgroundImage = ii.getImage();
			backgroundWidth = backgroundImage.getWidth(null);
			backgroundHeight = backgroundImage.getHeight(null);
		}
		ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/maskTwo.png"));
		if(ii2 != null)
			mask = ii2.getImage();
		ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/levelOneV15Final.png"));
		if(ii3 != null)
			levelOne = ii3.getImage();
		ImageIcon ii4 = new ImageIcon(Game.class.getResource("Images/darkDoor1.png"));
		if(ii4 != null)
			doorImages[0] = ii4.getImage();
		ImageIcon ii5 = new ImageIcon(Game.class.getResource("Images/darkDoor2.png"));
		if(ii5 != null)
			doorImages[1] = ii5.getImage();
		ImageIcon ii6 = new ImageIcon(Game.class.getResource("Images/darkDoor3.png"));
		if(ii6 != null)
			doorImages[2] = ii6.getImage();
		ImageIcon ii7 = new ImageIcon(Game.class.getResource("Images/lock.png"));
		if(ii7 != null)
			lockImage = ii7.getImage();
		
		ImageIcon ii8 = new ImageIcon(Game.class.getResource("Images/key.png"));
		if(ii8 != null)
			keyImage = ii8.getImage();
	}
	
	public void initPlayer() {
		String chars[] = {"N", "E", "S", "W"};
		
		// Initialize the still images
		for(int i = 0; i < 4; i++) {
			ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Knight/" + chars[i] + "Knight.png"));
			if(ii != null)
				knightStill[i] = ii.getImage();
			ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Archer/" + chars[i] + "Archer.png"));
			if(ii2 != null)
				archerStill[i] = ii2.getImage();
			ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/Wizard/" + chars[i] + "Wizard.png"));
			if(ii3 != null)
				wizardStill[i] = ii3.getImage();
		}
		// Initialize the .gif files
		for(int i = 0; i < 4; i++) {	
			ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Knight/" + chars[i] + "KnightMoving.gif"));
			if(ii != null)
				knightMoving[i] = ii.getImage();
			ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Archer/" + chars[i] + "ArcherMoving.gif"));
			if(ii2 != null) 
				archerMoving[i] = ii2.getImage();
			ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/Wizard/" + chars[i] + "WizardMoving.gif"));
			if(ii3 != null)
				wizardMoving[i] = ii3.getImage();
		}
	}
	
	public void initAttackAnnimations() {		
		String chars[] = {"N", "E", "S", "W"};
		// Initialize sword animations
		for(int i = 0; i < 4; i++) {
			ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Knight/" + chars[i] + "Sword.gif"));
			if(ii != null)
				knightAttack[i] = ii.getImage();
			ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Archer/" + chars[i] + "BowAttack2.gif"));
			if(ii2 != null)
				archerAttack[i] = ii2.getImage();
			ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/Wizard/"+ chars[i] +"WizardAttack.gif"));
			if(ii3 != null)
				wizardAttack[i] = ii3.getImage();
		}
	}
	
	public void initDying() {
		ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Knight/knightDead.gif"));
		if(ii != null)
			knightDead = ii.getImage();
		ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Archer/archerDead.gif"));
		if(ii2 != null)
			archerDead = ii2.getImage();
		ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/Wizard/wizardDead.gif"));
		if(ii3 != null)
			wizardDead = ii3.getImage();
		ImageIcon ii4 = new ImageIcon(Game.class.getResource("Images/SkeletonArcher/skeletonArcherDead.gif"));
		if(ii4 != null)
			skeletonArcherDead = ii4.getImage();
		ImageIcon ii5 = new ImageIcon(Game.class.getResource("Images/Orc/orcDead.gif"));
		if(ii5 != null) {
			orcDead = ii5.getImage();
		}
	}
	
	public void initProjectiles() {
		String chars[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		
		for(int i = 0; i < 8; i++) {
			ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Archer/" + chars[i] + "Arrow.png"));
			if(ii != null)
				arrowImages[i] = ii.getImage();
		}
		
		ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Wizard/missle.gif"));
		if(ii2 != null)
			missle = ii2.getImage();
	}
	
	public void initEnemies() {
		String chars[] = {"N", "E", "S", "W"};
		
		for(int i = 0; i < 4; i++) {
			ImageIcon ii = new ImageIcon(Game.class.getResource("Images/SkeletonArcher/" + chars[i] + "Shoot.gif"));
			ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/SkeletonArcher/" + chars[i] + "Idle.gif"));
			if(ii != null)
				SkeletonArcherShooting[i] = ii.getImage();
			if(ii2 != null)
				SkeletonArcherIdle[i] = ii2.getImage();
		}
		for(int i = 0; i < 4; i++) {
			ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Orc/orc" + chars[i] + "Still.gif"));
			ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Orc/orc" + chars[i] + "Attack.gif"));
			ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/Orc/orc" + chars[i] + "Walk.gif"));
			if(ii != null) {
				orcIdle[i] = ii.getImage();
			}
			if(ii2 != null) {
				orcAttacking[i] = ii2.getImage();
			}
			if(ii3 != null) {
				orcWalking[i] = ii3.getImage();
			}
		}
	}
	
	public void initAbilities() {
		ImageIcon ii = new ImageIcon(Game.class.getResource("Images/Ability/wizardAbility.png"));
		if(ii != null)
			wizardAbility = ii.getImage();
		ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/Ability/archerAbility.png"));
		if(ii2 != null)
			archerAbility = ii2.getImage();
		ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/Ability/knightAbility.png"));
		if(ii3 != null)
			knightAbility = ii3.getImage();
		ImageIcon ii4 = new ImageIcon(Game.class.getResource("Images/Ability/health.png"));
		if(ii4 != null)
			healImage = ii4.getImage();
	}
	
	public void initUI() {
		ImageIcon ii = new ImageIcon(Game.class.getResource("Images/hudWithXP4.png"));
		if(ii != null)
			hudOverlay = ii.getImage();
		// Add exit image
		ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/exitRed.png"));
		if(ii2 != null)
			exitImage = ii2.getImage();
	}
	
	public void initPanels() {
		ImageIcon ii = new ImageIcon(Game.class.getResource("Images/knightPanel.png"));
		if(ii != null) {
			knightPanel = ii.getImage();
		}
		ImageIcon ii2 = new ImageIcon(Game.class.getResource("Images/archerPanel.png"));
		if(ii2 != null) {
			archerPanel = ii2.getImage();
		}
		ImageIcon ii3 = new ImageIcon(Game.class.getResource("Images/wizardPanel.png"));
		if(ii3 != null) {
			wizardPanel = ii3.getImage();
		}
	}
	
	public void initAudio() {
		// Init four shoot sounds
		audioMap.put("shoot", new ArrayList<AudioPlayer>());
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		
		audioMap.put("impact", new ArrayList<AudioPlayer>());
		audioMap.get("impact").add(new AudioPlayer("SFX/impact5.mp3"));
		audioMap.get("impact").add(new AudioPlayer("SFX/impact5.mp3"));
		audioMap.get("impact").add(new AudioPlayer("SFX/impact5.mp3"));
		audioMap.get("impact").add(new AudioPlayer("SFX/impact5.mp3"));
		audioMap.get("impact").add(new AudioPlayer("SFX/impact5.mp3"));
		audioMap.get("impact").add(new AudioPlayer("SFX/impact5.mp3"));
		
		audioMap.put("hurt", new ArrayList<AudioPlayer>());
		audioMap.get("hurt").add(new AudioPlayer("SFX/hurt.mp3"));
		audioMap.get("hurt").add(new AudioPlayer("SFX/hurt.mp3"));
		audioMap.get("hurt").add(new AudioPlayer("SFX/hurt.mp3"));
		audioMap.get("hurt").add(new AudioPlayer("SFX/hurt.mp3"));
		
		audioMap.put("shoot", new ArrayList<AudioPlayer>());
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		audioMap.get("shoot").add(new AudioPlayer("SFX/shoot.mp3"));
		
		audioMap.put("spell", new ArrayList<AudioPlayer>());
		audioMap.get("spell").add(new AudioPlayer("SFX/spell.mp3"));
		audioMap.get("spell").add(new AudioPlayer("SFX/spell.mp3"));
		audioMap.get("spell").add(new AudioPlayer("SFX/spell.mp3"));
		audioMap.get("spell").add(new AudioPlayer("SFX/spell.mp3"));
		
		audioMap.put("swing", new ArrayList<AudioPlayer>());
		audioMap.get("swing").add(new AudioPlayer("SFX/swing.mp3"));
		audioMap.get("swing").add(new AudioPlayer("SFX/swing.mp3"));
		audioMap.get("swing").add(new AudioPlayer("SFX/swing.mp3"));
		audioMap.get("swing").add(new AudioPlayer("SFX/swing.mp3"));
		
		audioMap.put("potion", new ArrayList<AudioPlayer>());
		audioMap.get("potion").add(new AudioPlayer("SFX/potion.mp3"));
		audioMap.get("potion").add(new AudioPlayer("SFX/potion.mp3"));
		
		audioMap.put("skeletonHit", new ArrayList<AudioPlayer>());
		audioMap.get("skeletonHit").add(new AudioPlayer("SFX/skeletonHit.mp3"));
		audioMap.get("skeletonHit").add(new AudioPlayer("SFX/skeletonHit.mp3"));
		audioMap.get("skeletonHit").add(new AudioPlayer("SFX/skeletonHit.mp3"));
		audioMap.get("skeletonHit").add(new AudioPlayer("SFX/skeletonHit.mp3"));
		
		audioMap.put("door", new ArrayList<AudioPlayer>());
		audioMap.get("door").add(new AudioPlayer("SFX/door.mp3"));
		
		audioMap.put("orcHurt", new ArrayList<AudioPlayer>());
		audioMap.get("orcHurt").add(new AudioPlayer("SFX/orcHurt.mp3"));
		audioMap.get("orcHurt").add(new AudioPlayer("SFX/orcHurt.mp3"));
		audioMap.get("orcHurt").add(new AudioPlayer("SFX/orcHurt.mp3"));
		audioMap.get("orcHurt").add(new AudioPlayer("SFX/orcHurt.mp3"));
	}
	
	public void run() {
		final long targetFPS = 100;
		final long targetTPS = 60; 
		
		// Calculates the number of nanoseconds between each frame and tick.
		final long targetNSPF = 1000000000 / targetFPS;
		final long targetNSPT = 1000000000 / targetTPS;
		
		// Game loop
		long lastTime = System.nanoTime();
		long renderTime = 0; // Nanoseconds since last render
		long tickTime = 0; // Nanoseconds since last tick
		long fps = 0;
		long lastFPS = 0;
		
		try(Socket server = new Socket(ip, Integer.valueOf(2112))) {
			server.setTcpNoDelay(true);
			System.out.println("Connected to Server host " + server.getInetAddress());
			//JOptionPane.showMessageDialog(null, "Connected to: " + ip);
			
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
			toServer = new PrintWriter(server.getOutputStream(), true);
			toServer.println("name_" + name);
			running = true;
					
			while(running) {
				long currentTime = System.nanoTime();
				long deltaTime = currentTime - lastTime; // Change in time from last loop cycle
				renderTime += deltaTime; // Increase render counter
				tickTime += deltaTime; // Increase tick counter

				// Catch up on ticks
				while (tickTime >= targetNSPT) {
					tickTime -= targetNSPT;
					tick();
				}

				// Only render once, even if there's a delay or something
				if (renderTime > targetNSPF) {
					render();
					renderTime = 0;
				}

				// Update time tracker
				lastTime = currentTime;
			}
			fromServer.close();
			toServer.close();
			System.out.println("Game Loop Stopped, Exiting Game");
			System.exit(0);
			
		} catch(UnknownHostException e) {
			e.printStackTrace();
			System.out.println("Unknown Host");
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("No host");
			JOptionPane.showMessageDialog(null, "Server connection failed!");
			System.exit(0);
		} finally {
			System.out.println("Finally");
		}
	}
	
	public void tick() throws IOException {
		while(fromServer.ready()) {
			String s = fromServer.readLine();
			if(s.equals("Connected") && !playerConnected) {
				playerConnected = true;
				JOptionPane.showMessageDialog(null, "The server says hello!");
			}
			
			String[] array = s.split("_");
			String data = array[1];
			switch(array[0]) {
			case "Packet": {
				Packet packet = (Packet) encoder.decodeObj(data);
				playerData = new ArrayList<Player>(packet.getPlayerData().values());
			} 
			break;
			case "Player": {
				Packet packet = (Packet) encoder.decodeObj(data);
				player = packet.getPlayer();
			} 
			break;
			case "Door": {
				Packet packet = (Packet) encoder.decodeObj(data);
				doors = packet.getDoors();
			} 
			break;
			case "SkeleArchers": {
				Packet packet = (Packet) encoder.decodeObj(data);
				skeleArchers = packet.getSkeletonArchers();
				skeleArcherArrows.clear();
				for(int i = 0; i < skeleArchers.size(); i++) {
					if(skeleArchers.get(i).getArrows().size() > 0 && skeleArchers != null)
						skeleArcherArrows.addAll(skeleArchers.get(i).getArrows());
				}
			}
			break;
			case "One": {
				Packet packet = (Packet) encoder.decodeObj(data);
				playerData = new ArrayList<Player>(packet.getPlayerData().values());
				player = packet.getPlayer();
				doors = packet.getDoors();
				combatText = packet.getCombatText();
				audioManager = packet.getAudioManager();
				skeleArchers = packet.getSkeletonArchers();
				skeleArcherArrows.clear();
				for(int i = 0; i < skeleArchers.size(); i++) {
					if(skeleArchers.get(i).getArrows().size() > 0 && skeleArchers != null)
						skeleArcherArrows.addAll(skeleArchers.get(i).getArrows());
				}
				orcs = packet.getOrcs();
			}
			break;
			}
		}
		if(playAudio) {
			manageAudio();
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform backup = g2.getTransform();
		g2.scale(xScale, yScale);
		if(running && player != null) {
			g.setColor(Color.WHITE);
			drawBackground(g2);
			drawRemainingPlayers(g);
			drawEnemies(g);
			drawEnemyProjectiles(g);
			drawPlayerArrows(g);
			drawPlayerMissles(g);
			drawOtherArrows(g);
			drawOtherMissles(g);
			g2.transform(backup);
			drawPlayer(g);
			drawCombatText(g);
			drawMask(g);
			drawUI(g);
			drawClassPanels(g);
			drawAbilities(g);
		} else {
			Font loadingFont = new Font("Courier", Font.BOLD, 60);
			g.setColor(Color.WHITE);
			g.setFont(loadingFont);
			FontMetrics fm = getFontMetrics(loadingFont);
			g.drawString("Loading", screenWidth / 2 - fm.stringWidth("Loading") / 2, 500);
		}
	}
	
	public void drawBackground(Graphics g) {
		if(backgroundImage != null && player != null) {
			if(player.getLevelIndex() == 0) {
				g.drawImage(backgroundImage, 0 - player.getX() + centerX, 0 - player.getY() + centerY, backgroundWidth, backgroundHeight, this);
			}
			if(player.getLevelIndex() == 1) {
				g.drawImage(levelOne, 0 - player.getX() + centerX, 0 - player.getY() + centerY, levelOne.getWidth(null), levelOne.getHeight(null), this);
			}
		}
		
		drawDoors(g);
	}
	
	public void drawDoors(Graphics g) {
		if(doors != null) {
			for(Door d: doors) {
				if(!d.isOpen() && !d.isOpening()) {
					g.drawImage(doorImages[0], d.getX() - player.getX() + centerX, d.getY() - player.getY() + centerY, d.getHeight(), d.getWidth(), this);
					if(d.isLocked())
						// If the door is locked, draw the lock
						g.drawImage(lockImage, d.getX() - player.getX() + centerX + d.getWidth() / 2 - lockImage.getWidth(null) / 2, d.getY() - player.getY() + centerY + 150, lockImage.getHeight(null), lockImage.getWidth(null), this);
				} else if(d.isOpening() && !d.isOpen()) {
					g.drawImage(doorImages[1], d.getX() - player.getX() + centerX, d.getY() - player.getY() + centerY, d.getHeight(), d.getWidth(), this);
				} else if(d.isOpen()) {
					g.drawImage(doorImages[2], d.getX() - player.getX() + centerX, d.getY() - player.getY() + centerY, d.getHeight(), d.getWidth(), this);
				}
			}
		}
	}
	
	public void drawEnemies(Graphics g) {
		if(player.getLevelIndex() == 1) {
			// Draw SkeletonArchers
			if(skeleArchers != null && skeleArchers.size() > 0) {
				for(SkeletonArcher s: skeleArchers) {
					int direction = s.getDirection();
					boolean attacking = s.isAttacking();
					if(!s.isDead()) {
						if(attacking)
							g.drawImage(SkeletonArcherShooting[direction], s.getX() - player.getX() + centerX, s.getY() - player.getY() + centerY, SkeletonArcherShooting[direction].getWidth(null), SkeletonArcherShooting[direction].getHeight(null), this);
						else
							g.drawImage(SkeletonArcherIdle[direction], s.getX() - player.getX() + centerX, s.getY() - player.getY() + centerY, SkeletonArcherIdle[direction].getWidth(null), SkeletonArcherIdle[direction].getHeight(null), this);
						
						// Draw health bars for Skeleton Archers
						double percent = s.getHealthLeft() / s.getHealth();
						g.setColor(Color.WHITE);
						g.drawRoundRect(s.getX() - player.getX() + centerX + 50, s.getY() - player.getY() + centerY + SkeletonArcherShooting[direction].getHeight(null) + 5, SkeletonArcherShooting[direction].getWidth(null) - 100, 8, 5, 5);
						g.setColor(Color.RED);
						g.fillRoundRect(s.getX() - player.getX() + centerX + 51, s.getY() - player.getY() + centerY + SkeletonArcherShooting[direction].getHeight(null) + 6, (int)((SkeletonArcherShooting[direction].getWidth(null) - 102)*percent), 6, 5, 5);
						
						// Draw name and level of the Skeleton Archer
						g.setColor(Color.WHITE);
						g.setFont(nameFont);
						String heading = s.getName() + " - " + s.getDifficulty();
						FontMetrics fmHeading = getFontMetrics(nameFont);
						g.drawString(heading, s.getX() + s.getWidth() / 2 - fmHeading.stringWidth(heading) / 2 - player.getX() + centerX, s.getY() - player.getY() + centerY + 10);
						
					} else {
						g.drawImage(skeletonArcherDead, s.getX() - player.getX() + centerX, s.getY() - player.getY() + centerY, skeletonArcherDead.getWidth(null), skeletonArcherDead.getHeight(null), this);
					}
				}
			}
			// Draw Orcs
			if(orcs != null && orcs.size() > 0) {
				for(Orc o: orcs) {
					int direction = o.getDirection();
					boolean attacking = o.isAttacking();
					boolean walking = o.isWalking();
					if(!o.isDead()) {
						if(walking && !attacking) {
							g.drawImage(orcWalking[direction], o.getX() - player.getX() + centerX, o.getY() - player.getY() + centerY, orcWalking[direction].getWidth(null), orcWalking[direction].getHeight(null), this);
						} else if(!walking && !attacking) {
							g.drawImage(orcIdle[direction], o.getX() - player.getX() + centerX, o.getY() - player.getY() + centerY, orcIdle[direction].getWidth(null), orcIdle[direction].getHeight(null), this);
						} else if(attacking) {
							g.drawImage(orcAttacking[direction], o.getX() - player.getX() + centerX, o.getY() - player.getY() + centerY, orcAttacking[direction].getWidth(null), orcAttacking[direction].getHeight(null), this);
						}
						
						// Draw health bars for Orcs
						double percent = o.getHealthLeft() / o.getHealth();
						g.setColor(Color.WHITE);
						g.drawRoundRect(o.getX() - player.getX() + centerX + 50, o.getY() - player.getY() + centerY + orcWalking[direction].getHeight(null) + 5, orcWalking[direction].getWidth(null) - 100, 8, 5, 5);
						g.setColor(Color.RED);
						g.fillRoundRect(o.getX() - player.getX() + centerX + 51, o.getY() - player.getY() + centerY + orcWalking[direction].getHeight(null) + 6, (int)((orcWalking[direction].getWidth(null) - 102)*percent), 6, 5, 5);
						
						// Draw name and level of the Orc
						g.setColor(Color.WHITE);
						g.setFont(nameFont);
						String heading = o.getName() + " - " + o.getDifficulty();
						FontMetrics fmHeading = getFontMetrics(nameFont);
						g.drawString(heading, o.getX() + o.getWidth() / 2 - fmHeading.stringWidth(heading) / 2 - player.getX() + centerX, o.getY() - player.getY() + centerY - 10);
						
					} else {
						g.drawImage(orcDead, o.getX() - player.getX() + centerX, o.getY() - player.getY() + centerY, orcDead.getWidth(null), orcDead.getHeight(null), this);
					}
				}
			}
		}
	}
	
	public void drawEnemyProjectiles(Graphics g) {
		if(skeleArcherArrows != null) {
			for(int i = 0; i < skeleArcherArrows.size(); i++) {
				Arrow a = skeleArcherArrows.get(i);
				if(a != null) {
					int index = a.getIndex();
					
					Graphics2D g2 = (Graphics2D) g;
					AffineTransform backup = g2.getTransform();
					AffineTransform trans = new AffineTransform();
					trans.rotate(a.getTheta(), a.getX() - player.getX() + centerX + a.getWidth() / 2, a.getY() - player.getY() + centerY + a.getHeight() / 2);
					
					g2.setTransform(trans);
					g2.drawImage(arrowImages[index], a.getX() - player.getX() + centerX, a.getY() - player.getY() + centerY, arrowImages[index].getWidth(null), arrowImages[index].getHeight(null), this);
					g2.setTransform(backup);
				}
			}
		}
	}
	
	public void drawPlayer(Graphics g) {
		if(player != null) {
			index = player.getDirectionIndex();
			playerClass = player.getPlayerClass();
			// Render the knight images
			if(playerClass == PlayerClass.KNIGHT) {
				if(!player.isDead()) {
					if(player.isMoving() && !player.isAttacking()) {
						//centerX = (screenWidth / 2) - (knightMoving[index].getWidth(null) / 2);
						//centerY = (screenHeight / 2) - (knightMoving[index].getHeight(null) / 2);
						g.drawImage(knightMoving[index], centerX, centerY, knightMoving[index].getWidth(null), knightMoving[index].getHeight(null), this);
					} else if(!player.isMoving() && !player.isAttacking()) {
						//centerX = (screenWidth / 2) - (knightStill[index].getWidth(null) / 2);
						//centerY = (screenHeight / 2) - (knightStill[index].getHeight(null) / 2);
						g.drawImage(knightStill[index], centerX, centerY, knightStill[index].getWidth(null), knightStill[index].getHeight(null), this);
					} else if(player.isAttacking()) {
						int swordX = (screenWidth / 2) - (knightAttack[index].getWidth(null) / 2);
						int swordY = (screenHeight / 2) - (knightAttack[index].getHeight(null) / 2);
						// Adjust for translation when attacking North
						if(index == 0) {
							swordY -= 35;
						}
						// Adjust for translation when attacking South
						if(index == 2) {
							swordY += 35;
						}
						g.drawImage(knightAttack[index], swordX, swordY, knightAttack[index].getWidth(null), knightAttack[index].getHeight(null), this);
					}
				} else {
					int deadX = (screenWidth / 2) - (knightDead.getWidth(null) / 2);
					int deadY = (screenHeight / 2) - (knightDead.getHeight(null) / 2);
					g.drawImage(knightDead, deadX, deadY, knightDead.getWidth(null), knightDead.getHeight(null), this);
				}
			}
			// Render the archer images
			else if(playerClass == PlayerClass.ARCHER) {
				if(!player.isDead()) {
					if(player.isMoving() && !player.isAttacking()) {
						//centerX = (screenWidth / 2) - (archerMoving[index].getWidth(null) / 2);
						//centerY = (screenHeight / 2) - (archerMoving[index].getHeight(null) / 2);
						g.drawImage(archerMoving[index], centerX, centerY, archerMoving[index].getWidth(null), archerMoving[index].getHeight(null), this);
					} else if(!player.isMoving() && !player.isAttacking()) {
						//centerX = (screenWidth / 2) - (archerStill[index].getWidth(null) / 2);
						//centerY = (screenHeight / 2) - (archerStill[index].getHeight(null) / 2);
						g.drawImage(archerStill[index], centerX, centerY, archerStill[index].getWidth(null), archerStill[index].getHeight(null), this);
					} else if(player.isAttacking()) {
						int bowX = (screenWidth / 2) - (archerAttack[index].getWidth(null) / 2);
						int bowY = (screenHeight / 2) - (archerAttack[index].getHeight(null) / 2);
						g.drawImage(archerAttack[index], bowX, bowY, archerAttack[index].getWidth(null), archerAttack[index].getHeight(null), this);
					}
				} else {
					int deadX = (screenWidth / 2) - (archerDead.getWidth(null) / 2);
					int deadY = (screenHeight / 2) - (archerDead.getHeight(null) / 2);
					g.drawImage(archerDead, deadX, deadY, archerDead.getWidth(null), archerDead.getHeight(null), this);
				}
			}
			// Render the wizard images
			else if(playerClass == PlayerClass.WIZARD) {
				if(!player.isDead()) {
					if(player.isMoving() && !player.isAttacking()) {
						//centerX = (screenWidth / 2) - (wizardMoving[index].getWidth(null) / 2);
						//centerY = (screenHeight / 2) - (wizardMoving[index].getHeight(null) / 2);
						g.drawImage(wizardMoving[index], centerX, centerY, wizardMoving[index].getWidth(null), wizardMoving[index].getHeight(null), this);
					} else if(!player.isMoving() && !player.isAttacking()) {
						//centerX = (screenWidth / 2) - (wizardStill[index].getWidth(null) / 2);
						//centerY = (screenHeight / 2) - (wizardStill[index].getHeight(null) / 2);
						g.drawImage(wizardStill[index], centerX, centerY, wizardStill[index].getWidth(null), wizardStill[index].getHeight(null), this);
					} else if(player.isAttacking()) {
						int staffX = (screenWidth / 2) - (wizardAttack[index].getWidth(null) / 2);
						int staffY = (screenHeight / 2) - (wizardAttack[index].getHeight(null) / 2);
						g.drawImage(wizardAttack[index], staffX, staffY, wizardAttack[index].getWidth(null), wizardAttack[index].getHeight(null), this);
					}
				}
				else {
					int deadX = (screenWidth / 2) - (wizardDead.getWidth(null) / 2);
					int deadY = (screenHeight / 2) - (wizardDead.getHeight(null) / 2);
					g.drawImage(wizardDead, deadX, deadY, wizardDead.getWidth(null), wizardDead.getHeight(null), this);
				}
			}
		}
	}
	
	public void drawPlayerArrows(Graphics g) {
		if(player != null) {
			ArrayList<Arrow> arrows = player.getArrows();
			for(Arrow a: arrows) {
				int index = a.getIndex();
				g.drawImage(arrowImages[index], a.getX() - player.getX() + centerX, a.getY() - player.getY() + centerY, arrowImages[index].getWidth(null), arrowImages[index].getHeight(null), this);
			}
		}
	}
	
	public void drawOtherArrows(Graphics g) {
		// 2D ArrayList of arrows
		ArrayList<ArrayList<Arrow>> otherArrows = new ArrayList<ArrayList<Arrow>>();
		for(Player p: playerData) {
			if(p.getLevelIndex() == player.getLevelIndex()) {
				otherArrows.add(p.getArrows());
				for(ArrayList<Arrow> list: otherArrows) {
					for(Arrow a: list) {
						int index = a.getIndex();
						g.drawImage(arrowImages[index], a.getX() - player.getX() + centerX, a.getY() - player.getY() + centerY, arrowImages[index].getWidth(null), arrowImages[index].getHeight(null), this);
					}
				}
			}
		}
	}
	
	public void drawPlayerMissles(Graphics g) {
		if(player != null) {
			ArrayList<Missile> missles = player.getMissles();
			for(Missile m: missles) {
				g.drawImage(missle, m.getX() - player.getX() + centerX, m.getY() - player.getY() + centerY, missle.getWidth(null), missle.getHeight(null), this);
			}
		}
	}
	
	public void drawOtherMissles(Graphics g) {
		ArrayList<ArrayList<Missile>> otherMissles = new ArrayList<ArrayList<Missile>>();
		for(Player p: playerData) {
			if(p.getLevelIndex() == player.getLevelIndex()) {
				otherMissles.add(p.getMissles());
				for(ArrayList<Missile> list: otherMissles) {
					for(Missile m: list) {
						g.drawImage(missle, m.getX() - player.getX() + centerX, m.getY() - player.getY() + centerY, missle.getWidth(null), missle.getHeight(null), this);
					}
				}
			}
		}
	}
	
	public void drawRemainingPlayers(Graphics g) {
		// Draws the rest of the players
		if(playerData != null) {
			for(int i = 0; i < playerData.size(); i++) {
				Player tempPlayer = playerData.get(i);
				String name = tempPlayer.getName();
				
				// Only render the players that are not the client player
				if(tempPlayer.getConnectionID() != player.getConnectionID() && tempPlayer.getLevelIndex() == player.getLevelIndex()) {
					
					int tempIndex = tempPlayer.getDirectionIndex();
					boolean tempMoving = tempPlayer.isMoving();
					boolean tempAttacking = tempPlayer.isAttacking();
					PlayerClass playerClass = tempPlayer.getPlayerClass();
					
					// Draw Health bar under player first
					double totalHealth = tempPlayer.getHealth();
					double healthLeft = tempPlayer.getHealthLeft();
					double percent = (healthLeft / totalHealth);
					
					// Draw outline
					g.setColor(Color.WHITE);
					g.drawRoundRect(tempPlayer.getX() - player.getX() + centerX + 20, tempPlayer.getY() - player.getY() + centerY + knightStill[tempIndex].getHeight(null) + 5, knightStill[0].getWidth(null) - 40, 8, 5, 5);
					g.setColor(Color.RED);
					g.fillRoundRect(tempPlayer.getX() - player.getX() + centerX + 21, tempPlayer.getY() - player.getY() + centerY + knightStill[tempIndex].getHeight(null) + 6, (int)((knightStill[0].getWidth(null) - 42)*percent), 6, 5, 5);
					
					if(playerClass == PlayerClass.KNIGHT) {
						tempPlayer.setWidth(knightStill[0].getWidth(null));
						tempPlayer.setHeight(knightStill[0].getHeight(null));
						if(!tempPlayer.isDead()) {
							if(tempMoving && !tempAttacking) {
								g.drawImage(knightMoving[tempIndex], tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, knightMoving[tempIndex].getWidth(null), knightMoving[tempIndex].getHeight(null), this);
							} else if(!tempMoving && !tempAttacking){
								g.drawImage(knightStill[tempIndex], tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, knightStill[tempIndex].getWidth(null), knightStill[tempIndex].getHeight(null), this);
							} else if(tempAttacking) {
								int swordX = (screenWidth / 2) - (knightAttack[tempIndex].getWidth(null) / 2);
								int swordY = (screenHeight / 2) - (knightAttack[tempIndex].getHeight(null) / 2);
								// Adjust for translation when attacking North
								if(tempIndex == 0) {
									swordY -= 35;
								}
								// Adjust for translation when attacking South
								if(tempIndex == 2) {
									swordY += 35;
								}
								g.drawImage(knightAttack[tempIndex], tempPlayer.getX() - player.getX() + swordX, tempPlayer.getY() - player.getY() + swordY, knightAttack[tempIndex].getWidth(null), knightAttack[tempIndex].getHeight(null), this);
							}
						} else {
							centerX = (screenWidth / 2) - (knightDead.getWidth(null) / 2);
							centerY = (screenHeight / 2) - (knightDead.getHeight(null) / 2);
							g.drawImage(knightDead, tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, knightDead.getWidth(null), knightDead.getHeight(null), this);
						}
					}
					else if(playerClass == PlayerClass.ARCHER ) {
						tempPlayer.setWidth(archerStill[0].getWidth(null));
						tempPlayer.setHeight(archerStill[0].getHeight(null));
						if(!tempPlayer.isDead()) {
							if(tempMoving && !tempAttacking) {
								g.drawImage(archerMoving[tempIndex], tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, archerMoving[tempIndex].getWidth(null), archerMoving[tempIndex].getHeight(null), this);
							} else if(!tempMoving && !tempAttacking){
								g.drawImage(archerStill[tempIndex], tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, archerStill[tempIndex].getWidth(null), archerStill[tempIndex].getHeight(null), this);
							} else if(tempAttacking) {
								int bowX = (screenWidth / 2) - (archerAttack[tempIndex].getWidth(null) / 2);
								int bowY = (screenHeight / 2) - (archerAttack[tempIndex].getHeight(null) / 2);
								g.drawImage(archerAttack[tempIndex], tempPlayer.getX() - player.getX() + bowX, tempPlayer.getY() - player.getY() + bowY, archerAttack[tempIndex].getWidth(null), archerAttack[tempIndex].getHeight(null), this);
							}
						} else {
							centerX = (screenWidth / 2) - (archerDead.getWidth(null) / 2);
							centerY = (screenHeight / 2) - (archerDead.getHeight(null) / 2);
							g.drawImage(archerDead, tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, archerDead.getWidth(null), archerDead.getHeight(null), this);
						}
					}
					else if(playerClass == PlayerClass.WIZARD) {
						tempPlayer.setWidth(wizardStill[0].getWidth(null));
						tempPlayer.setHeight(wizardStill[0].getHeight(null));
						if(!tempPlayer.isDead()) {
							if(tempMoving && !tempAttacking) {
								g.drawImage(wizardMoving[tempIndex], tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, wizardMoving[tempIndex].getWidth(null), wizardMoving[tempIndex].getHeight(null), this);
							} else if(!tempMoving && !tempAttacking){
								g.drawImage(wizardStill[tempIndex], tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, wizardStill[tempIndex].getWidth(null), wizardStill[tempIndex].getHeight(null), this);
							} else if(tempAttacking) {
								int staffX = (screenWidth / 2) - (wizardAttack[tempIndex].getWidth(null) / 2);
								int staffY = (screenHeight / 2) - (wizardAttack[tempIndex].getHeight(null) / 2);
								g.drawImage(wizardAttack[tempIndex], tempPlayer.getX() - player.getX() + staffX, tempPlayer.getY() - player.getY() + staffY, wizardAttack[tempIndex].getWidth(null), wizardAttack[tempIndex].getHeight(null), this);
							}
						} else {
							centerX = (screenWidth / 2) - (wizardDead.getWidth(null) / 2);
							centerY = (screenHeight / 2) - (wizardDead.getHeight(null) / 2);
							g.drawImage(wizardDead, tempPlayer.getX() - player.getX() + centerX, tempPlayer.getY() - player.getY() + centerY, wizardDead.getWidth(null), wizardDead.getHeight(null), this);
						}
					}
					
					// Draw player name
					g.setFont(nameFont);
					g.setColor(Color.WHITE);
					FontMetrics fm = getFontMetrics(nameFont);
					name += " - " + tempPlayer.getPlayerLevel();
					g.drawString(name, tempPlayer.getX() - player.getX() + centerX + (tempPlayer.getWidth() / 2) - (fm.stringWidth(name) / 2), tempPlayer.getY() - player.getY() + centerY - 5);
				}
			}
		}
	}
	
	public void drawCombatText(Graphics g) {
		g.setFont(controlsFont);
		if(combatText != null) {
			for(int i = 0; i < combatText.size(); i++) {
				g.setColor(Color.WHITE);
				CombatText cT = combatText.get(i);
				if(cT.isCritical()) {
					g.setColor(Color.RED);
				}
				g.drawString(cT.getText(), cT.getX() - player.getX() + centerX, cT.getY() - player.getY() + centerY);
			}
		}
	}
	
	public void drawMask(Graphics g) {
		g.drawImage(mask, 0, 0, mask.getWidth(null), mask.getHeight(null), this);
	}
	
	public void drawClassPanels(Graphics g) {
		if(showPanel && player.getLevelIndex() == 0) {
			if(playerClass == PlayerClass.KNIGHT) {
				g.drawImage(knightPanel, panelX, panelY, knightPanel.getWidth(null), knightPanel.getHeight(null), this);
			} 
			else if(playerClass == PlayerClass.ARCHER) {
				g.drawImage(archerPanel, panelX, panelY, archerPanel.getWidth(null), archerPanel.getHeight(null), this);
			}
			else if(playerClass == PlayerClass.WIZARD) {
				g.drawImage(wizardPanel, panelX, panelY, wizardPanel.getWidth(null), wizardPanel.getHeight(null), this);
			}
		}
	}
	
	public void drawUI(Graphics g) {
		int hudX = screenWidth / 2 - hudOverlay.getWidth(null) / 2;
		int hudY = screenHeight - hudOverlay.getHeight(null);
		// Draw remaining health in health bubble
		double totalHealth = player.getHealth();
		double healthLeft = player.getHealthLeft();
		double percentLeft = (healthLeft / totalHealth);
		
		g.setColor(new Color(115,13,13));
		
		// Draw health bubble based on percent of health points remaining
		g.fillRect(hudX+145, hudY + 53 + 122 - (int)(115 * percentLeft), 126, 115);
		
		// Draw in XP bar information
		double totalXP = player.getTotalXP();
		double currentXP = player.getCurrentXP();
		double percentXP = (currentXP / totalXP);
		
		g.setColor(new Color(100,0,100));
		
		g.fillRect(hudX+5, hudY+36, (int)(410 * percentXP), 13);
		
		// Draw UI Overlay
		g.drawImage(hudOverlay, hudX, hudY, hudOverlay.getWidth(null), hudOverlay.getHeight(null), this);
		
		// Draw UI Text
		g.setColor(Color.WHITE);
		g.setFont(nameFont);
		FontMetrics fm = getFontMetrics(nameFont);
		
		// Health
		String health = (int)healthLeft + "/" + (int)totalHealth;
		if((int)totalHealth > 999) {
			String leftKWhole = (int)healthLeft / 1000 + "";
			String leftKRemain = ((int)healthLeft % 1000) / 100 + "";
			String totalKWhole = (int)totalHealth / 1000 + "";
			String totalKRemain = ((int)totalHealth % 1000) / 100 + "";
			
			health = leftKWhole + "." + leftKRemain + "k" + "/" + totalKWhole + "." + totalKRemain + "k";
			
			if(healthLeft < 1000) {
				health = (int)healthLeft + "/" + totalKWhole + "." + totalKRemain + "k";
			}
		}
		g.drawString(health, hudX + hudOverlay.getWidth(null) / 2 - fm.stringWidth(health) / 2, hudY + hudOverlay.getHeight(null) / 2 + 31);
		
		hudY += 53; // Accounts for the extra height coming from the xp bar addition
		
		// Player panel information
		String name = player.getName() + "";
		g.drawString(name, (hudX + 290) + (115 / 2) - (fm.stringWidth(name) / 2), (hudY + 30));
		String classString = "";
		String attackString = "Atk Damage";
		String attackDamage = player.getAttack();
		PlayerClass pc = player.getPlayerClass();
		if(pc == PlayerClass.ARCHER) {
			classString = "Archer";
		}
		else if(pc == PlayerClass.KNIGHT) {
			classString = "Knight";
		}
		else if(pc == PlayerClass.WIZARD) {
			classString = "Wizard";
		}
		
		g.drawString(classString, (hudX + 290) + (115 / 2) - (fm.stringWidth(classString) / 2), (hudY + 60));
		g.drawString(attackString, (hudX + 290) + (115 / 2) - (fm.stringWidth(attackString) / 2), (hudY + 80));
		g.drawString(attackDamage, (hudX + 290) + (115 / 2) - (fm.stringWidth(attackDamage) / 2), (hudY + 100));
		
		FontMetrics fm3 = getFontMetrics(controlsFont);
		// Draw informational text
		if(player.getLevelIndex() == 0) {
			String controls = "WASD - Move | Space - Attack | E - Change Class";
			g.setFont(controlsFont);
			g.drawString(controls, hudX / 2 - fm3.stringWidth(controls) / 2, 1000);
			
			String objective = "Objective - Fight against other players";
			g.drawString(objective, hudX / 2 - fm3.stringWidth(objective) / 2, 1040);
			
			g.setFont(titleFont);
			String title = "Welcome to the PVP Lobby!";
			FontMetrics fm2 = getFontMetrics(titleFont);
			g.drawString(title, screenWidth / 2 - fm.stringWidth(title), 50);
			
			// Draw panel key toggle T - toggle class information panel
			if(player.getLevelIndex() == 0) {
				g.setFont(controlsFont);
				g.drawString("T - Toggle class information panel", 1240, 1040);
			}
		}
		if(player.getLevelIndex() == 1) {
			String controls = "WASD - Move | Space - Attack | E - Interact";
			g.setFont(controlsFont);
			g.drawString(controls, hudX / 2 - fm3.stringWidth(controls) / 2, 1000);
			
			String objective = "Objective - Clear the dungeon of enemies";
			g.drawString(objective, hudX / 2 - fm3.stringWidth(objective) / 2, 1040);
		}
		
		g.setFont(escFont);
		g.drawString("(Esc)", 1780, 37);
		g.drawImage(exitImage, 1860, 10, exitImage.getWidth(null), exitImage.getHeight(null), this);
		
		// Draw Key
		g.setFont(titleFont);
		g.drawImage(keyImage, 1850, 1000, keyImage.getWidth(null), keyImage.getHeight(null), this);
		int keys = player.getKeys();
		g.drawString(keys + "", 1820, 1040);
		
		// Draw in player level
		int playerLevel = player.getPlayerLevel();
		g.setFont(levelFont);
		FontMetrics fm4 = getFontMetrics(levelFont);
		g.drawString(playerLevel + "", hudX + hudOverlay.getWidth(null) / 2 - fm4.stringWidth(playerLevel + "") / 2, hudY - 9);
	}
	
	public void drawAbilities(Graphics g) {
		// Draw ability images
		PlayerClass pc = player.getPlayerClass();
		if(pc == PlayerClass.ARCHER) {
			g.drawImage(archerAbility, 760, 957, archerAbility.getWidth(null), archerAbility.getHeight(null), this);
		}
		else if(pc == PlayerClass.KNIGHT) {
			g.drawImage(knightAbility, 760, 957, knightAbility.getWidth(null), knightAbility.getHeight(null), this);
		}
		else if(pc == PlayerClass.WIZARD) {
			g.drawImage(wizardAbility, 760, 957, wizardAbility.getWidth(null), wizardAbility.getHeight(null), this);
		}
		
		// Draw health spell
		g.drawImage(healImage, 830, 957, healImage.getWidth(null), healImage.getHeight(null), this);
		
		// Draw number text over the ability
		g.setColor(Color.WHITE);
		g.setFont(nameFont);
		g.drawString("1", 770, 1000);
		g.drawString("2", 840, 1000);
		
		Ability ability = player.getAbility();
		double abilityPercent = (double)ability.getCooldownLeft() / ability.getCooldown();
		g.setColor(new Color(0,0,0,200));
		g.fillRect(760, (int)(1012 - (55 * abilityPercent)), 55, (int)(55 * abilityPercent));
		
		Health potion = player.getPotion();
		double healthPercent = (double)potion.getCooldownLeft() / potion.getCooldown();
		g.fillRect(830, (int)(1012 - (55 * healthPercent)), 55, (int)(55 * healthPercent));
	}
	
	public void render() {
		repaint();
	}
	
	private void manageAudio() {
		if(audioManager != null) {
			Queue<Sound> sounds = audioManager.getQueue();
			Sound sound;
			if(sounds != null) {
				while(!sounds.isEmpty()) {
					sound = sounds.remove();
					
					boolean foundOpenSFX = false;
					int index = 0;
					while(!foundOpenSFX) {
						ArrayList<AudioPlayer> audioPlayers = audioMap.get(sound.getName());
						for(int i = 0; i < audioPlayers.size(); i++) {
							AudioPlayer AP = audioPlayers.get(i);
							if(!AP.stillPlaying()) {
								foundOpenSFX = true;
								index = i;
								break;
							}
						}
						
						float volume = sound.calculateVolume(player.getX(), player.getY());
						audioPlayers.get(index).play(volume);
					}
				}
			}
		}
	}
	
	// Send keyboard input to the server to handle player movement
	private class KAdapter extends KeyAdapter { 
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			}
			if(key == KeyEvent.VK_T) {
				showPanel = !showPanel;
			}
			if(key == KeyEvent.VK_M) {
				playAudio = !playAudio;
			}
			/*
			if(key == KeyEvent.VK_SPACE) {		
				if(playerClass == PlayerClass.KNIGHT) {
					if(!swing.stillPlaying())
						swing.play(1.0f);
				} 
				else if(playerClass == PlayerClass.ARCHER) {
					if(!shoot.stillPlaying())
						shoot.play(1.0f);
				}
				else if(playerClass == PlayerClass.WIZARD) {
					if(!spell.stillPlaying())
						spell.play(1.0f);
				}
			}
			*/
			if(toServer != null)
				toServer.println("KP" + e.getKeyCode());
		}
		public void keyReleased(KeyEvent ke) {
			if(toServer != null)
				toServer.println("KR" + ke.getKeyCode());
		}
	}
}