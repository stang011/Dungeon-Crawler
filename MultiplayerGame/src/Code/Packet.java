package Code;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// Jay Schimmoller

public class Packet implements Serializable {
	private HashMap<Long, Player> playerData;
	private Player player;
	private Long connectionID;
	private ArrayList<Door> doors;
	private ArrayList<SkeletonArcher> skeleArchers;
	private ArrayList<Orc> orcs;
	private ArrayList<CombatText> combatText;
	private AudioManager audioManager;
	
	public Packet(HashMap<Long, Player> playerData) {
		this.playerData = playerData;
	}
	public Packet(Player player) {
		this.player = player;
	}
	public Packet(ArrayList<Door> doors) {
		this.doors = doors;
	}
	public Packet(ArrayList<SkeletonArcher> skeleArchers, boolean dumb) {
		this.skeleArchers = skeleArchers;
	}
	
	// One big packet instead of many little packets
	public Packet(HashMap<Long, Player> playerData, Player player, ArrayList<Door> doors, ArrayList<SkeletonArcher> skeleArchers, ArrayList<Orc> orcs, ArrayList<CombatText> combatText, AudioManager audioManager) {
		this.playerData = playerData;
		this.player = player;
		this.doors = doors;
		this.skeleArchers = skeleArchers;
		this.orcs = orcs;
		this.combatText = combatText;
		this.audioManager = audioManager;
	}
	
	
	public HashMap<Long, Player> getPlayerData() {
		return playerData;
	}
	public Player getPlayer() {
		return player;
	}
	public Long getConnectionID() {
		return connectionID;
	}
	public ArrayList<Door> getDoors() {
		return doors;
	}
	public ArrayList<SkeletonArcher> getSkeletonArchers() {
		return skeleArchers;
	}
	public ArrayList<Orc> getOrcs() {
		return orcs;
	}
	public ArrayList<CombatText> getCombatText() {
		return combatText;
	}
	public AudioManager getAudioManager() {
		return audioManager;
	}
}