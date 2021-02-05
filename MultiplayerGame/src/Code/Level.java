package Code;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

public class Level {
	
	// Enemies eventually
	ArrayList<Rectangle> boundingBoxes = new ArrayList<>();
	ArrayList<Rectangle> boundingBoxesPassable = new ArrayList<>();
	ArrayList<Door> doors = new ArrayList<Door>();
	ArrayList<SkeletonArcher> skeleArchers = new ArrayList<>();
	ArrayList<Orc> orcs = new ArrayList<>();
	ArrayList<Point> enemyLocations = new ArrayList<>();
	
	ArrayList<SpawnZone> spawnZones = new ArrayList<>();
	private int numberOfSkeletonArchers = 10;
	Random rand = new Random();
	
	double difficulty = 1; 
	
	public Level(String fileName, double difficulty) {
		this.difficulty = difficulty;
		InputStream is = null;
		BufferedReader br = null;
		String line = "";
		
		try {
			is = Level.class.getResourceAsStream("Levels/" + fileName);
			br = new BufferedReader(new InputStreamReader(is));
			while(null != (line = br.readLine())) {
				String[] array = line.split(",");
				// Bounding Box
				if(array[0].equals("BB")) {
					Rectangle boundingBox = new Rectangle(Integer.parseInt(array[1]), Integer.parseInt(array[2]), Integer.parseInt(array[3]), Integer.parseInt(array[4]));
					boundingBoxes.add(boundingBox);
				}
				if(array[0].equals("BBP")) {
					Rectangle boundingBox = new Rectangle(Integer.parseInt(array[1]), Integer.parseInt(array[2]), Integer.parseInt(array[3]), Integer.parseInt(array[4]));
					boundingBoxesPassable.add(boundingBox);
					boundingBoxes.add(boundingBox);
				}
				if(array[0].equals("D")) {
					Door newDoor = new Door(Integer.parseInt(array[1]), Integer.parseInt(array[2]), Boolean.parseBoolean(array[3]));
					doors.add(newDoor);
				}
				/*
				if(array[0].equals("SA")) {
					SkeletonArcher sa = new SkeletonArcher(Integer.parseInt(array[1]), Integer.parseInt(array[2]), Integer.parseInt(array[3]));
					skeleArchers.add(sa);
					Point point = new Point(sa.getCenterX(), sa.getCenterY());
					enemyLocations.add(point);
				}
				*/
				if(array[0].equals("SZ")) {
					SpawnZone spawnZone = new SpawnZone(Integer.parseInt(array[1]), Integer.parseInt(array[2]), Integer.parseInt(array[3]), Integer.parseInt(array[4]), Integer.parseInt(array[5]), Integer.parseInt(array[6]));
					spawnZones.add(spawnZone);
				}
			}
			
			//spawnSkeletonArchers();
			spawnSkeletonArchersTwo();
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getStackTrace());
			System.exit(0);
		} finally {
			try {
				if(br != null) br.close();
				if(is != null) is.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void spawnSkeletonArchers() {
		if(spawnZones.size() > 0) {
			int numberSpawned = 0;
			
			while(numberSpawned < numberOfSkeletonArchers) {
				
				int randX = rand.nextInt(7000);
				int randY = rand.nextInt(2000) + 5000;
				
				//System.out.println(randX + " " + randY);
				
				Rectangle enemyAttempt = new Rectangle(randX, randY, 175, 165);
				
				for(SpawnZone zone: spawnZones) {
					Rectangle sz = zone.getRectangle();
					if(enemyAttempt.intersects(sz)) {
						if(checkAreaIntersection(sz, enemyAttempt) == 28875.0) {
							SkeletonArcher sa = new SkeletonArcher(randX, randY, rand.nextInt(4));
							skeleArchers.add(sa);
							Point point = new Point(sa.getCenterX(), sa.getCenterY());
							enemyLocations.add(point);
							numberSpawned++;
							break;
						}
					}
				}
			}
		}
	}
	
	public void spawnSkeletonArchersTwo() {
		if(spawnZones.size() > 0) {
			for(SpawnZone zone: spawnZones) {
				
				Rectangle sz = zone.getRectangle();
				int SAToSpawn = zone.getNumberOfSkeletonArchers();
				int spawned = 0;
				
				while(spawned < SAToSpawn) {
					int randX = rand.nextInt(7000);
					int randY = rand.nextInt(7000);
					
					Rectangle enemyAttempt = new Rectangle(randX, randY, 175, 165);
					
					if(enemyAttempt.intersects(sz)) {
						if(checkAreaIntersection(sz, enemyAttempt) == 28875.0) {
							SkeletonArcher sa = new SkeletonArcher(randX, randY, rand.nextInt(4));
							skeleArchers.add(sa);
							Point point = new Point(sa.getCenterX(), sa.getCenterY());
							enemyLocations.add(point);
							spawned++;
						}
					}
				}
				
				int orcToSpawn = zone.getNumberOfOrcs();
				spawned = 0;
				
				while(spawned < orcToSpawn) {
					int randX = rand.nextInt(7000);
					int randY = rand.nextInt(7000);
					
					Rectangle enemyAttempt = new Rectangle(randX, randY, 175, 165);
					
					if(enemyAttempt.intersects(sz)) {
						if(checkAreaIntersection(sz, enemyAttempt) == 28875.0) {
							Orc orc = new Orc(randX, randY, rand.nextInt(4));
							orcs.add(orc);
							Point point = new Point(orc.getCenterX(), orc.getCenterY());
							enemyLocations.add(point);
							spawned++;
						}
					}
				}
			}
		}
	}
	
	public double checkAreaIntersection(Rectangle sz, Rectangle ea) {
		
		Rectangle intersection = sz.intersection(ea);
		
		return intersection.width * intersection.height;
	}
	
	public ArrayList<Rectangle> getBoundingBoxes() {
		return boundingBoxes;
	}
	public ArrayList<Rectangle> getBoundingBoxesPassable() {
		return boundingBoxesPassable;
	}
	
	public ArrayList<Door> getDoors() {
		return doors;
	}
	
	public ArrayList<SkeletonArcher> getSkeleArchers() {
		return skeleArchers;
	}
	
	public ArrayList<Orc> getOrcs() {
		return orcs;
	}
	
	public ArrayList<Point> getEnemyLocations() {
		return enemyLocations;
	}
}