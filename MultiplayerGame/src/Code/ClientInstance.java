package Code;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

// Jay Schimmoller
// This class manages launching a client

public class ClientInstance extends JFrame implements Runnable {
	
	private JButton exitButton;
	private JButton playButton;
	private JLabel ipLabel;
	private JTextField ipField;
	private JLabel nameLabel;
	private JTextField nameField;
	private JLabel title;
	private JLabel background;
	private JPanel panel;
	
	private static String name;
	private static String ip;
	private static double xScale;
	private static double yScale;
	
	private Thread gameThread = new Thread(this);
	
	public static void main(String[] args) {
		
		boolean goodInput = false;
		name = "";
		ip = "";
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		
		// 1:1 scale is based on 1920x1080 resolution at 16:9 ratio
		xScale = 0;
		yScale = 0;
		
		// Version 1 - Fit to screen in both directions, maybe messing up aspect ratio on overall game image
		xScale = (double)screenWidth / 1920;
		yScale = (double)screenHeight / 1080;
		
		ClientInstance CI = new ClientInstance();
		CI.setUndecorated(true);
		CI.setVisible(true);
		CI.setEnabled(true);
		CI.setSize(300,250);
		CI.setLocationRelativeTo(null);
	}
	
	public void run() {
		Game game = new Game(ip, name, 1080, 1920, xScale, yScale);
	}
	
	public ClientInstance() {
		panel = new JPanel();
		panel.setLayout(null);
		add(panel);
		
		Font labelFont = new Font("Courier", Font.BOLD, 20);
		
		ImageIcon backgroundII = new ImageIcon(Game.class.getResource("Images/launchBackground.png"));
		background = new JLabel(backgroundII);
		background.setBounds(0,0,300,250);
		
		exitButton = new JButton("Exit");
		ButtonHandler handler = new ButtonHandler();
		exitButton.setBounds(170,200,90,30);
		exitButton.setFont(labelFont);
		exitButton.setBackground(Color.BLACK);
		exitButton.setForeground(Color.WHITE);
		
		playButton = new JButton("Play");
		playButton.setBounds(50,200,90,30);
		playButton.setFont(labelFont);
		playButton.setBackground(Color.BLACK);
		playButton.setForeground(Color.WHITE);
		
		Font titleFont = new Font("Courier", Font.BOLD, 35);
		title = new JLabel("Dungeon Hack");
		title.setFont(titleFont);
		title.setBounds(25,20,300,50);
		title.setForeground(Color.WHITE);
		
		ipLabel = new JLabel("IP:");
		ipLabel.setBounds(40,130,80,50);
		ipLabel.setFont(labelFont);
		ipLabel.setForeground(Color.WHITE);
		ipField = new JTextField(10);
		ipField.setBounds(90,140,180,30);
		ipField.setFont(labelFont);
		ipField.setBackground(Color.BLACK);
		ipField.setForeground(Color.WHITE);
		ipField.setCaretColor(Color.WHITE);
		//ipField.setText("47.6.34.41");
		
		nameLabel = new JLabel("Name:");
		nameLabel.setBounds(40,80,80,50);
		nameLabel.setFont(labelFont);
		nameLabel.setForeground(Color.WHITE);
		nameField = new JTextField(10);
		nameField.setBounds(120,95,150,30);
		nameField.setFont(labelFont);
		nameField.setBackground(Color.BLACK);
		nameField.setForeground(Color.WHITE);
		nameField.setCaretColor(Color.WHITE);
		
		panel.add(exitButton);
		panel.add(playButton);
		panel.add(ipLabel);
		panel.add(ipField);
		panel.add(nameLabel);
		panel.add(nameField);
		panel.add(title);
		panel.add(background);
		exitButton.addActionListener(handler);
		playButton.addActionListener(handler);
		
		nameField.addKeyListener(new KAdapter());
		ipField.addKeyListener(new KAdapter());
	}
	
	private class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == playButton) {
				try {
					ip = ipField.getText();
					name = nameField.getText();
					
					if(ip.equals(""))
						ip = InetAddress.getLocalHost().getHostAddress();
					boolean goodInput = validateInput(name, ip);
					
					if(goodInput) {
						dispose();
						gameThread.start();
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(event.getSource() == exitButton) {
				System.exit(0);
			}
		}
	}
	
	private class KAdapter extends KeyAdapter { 
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER) {
				playButton.doClick();
			}
			if(key == KeyEvent.VK_ESCAPE) {
				exitButton.doClick();
			}
		}
	}
	
	private static boolean validateInput(String name, String ip) {
		try {
			if(name.length() > 10 || name.length() < 2) {
				JOptionPane.showMessageDialog(null, "Name must be between 2 and 10 characters");
				return false;
			} 
			String[] array = ip.split("\\.");
			if(array.length == 4) {
				if(Integer.parseInt(array[0]) >= 0 && Integer.parseInt(array[0]) <= 255 &&
				   Integer.parseInt(array[1]) >= 0 && Integer.parseInt(array[1]) <= 255 && 
				   Integer.parseInt(array[2]) >= 0 && Integer.parseInt(array[2]) <= 255 &&
				   Integer.parseInt(array[3]) >= 0 && Integer.parseInt(array[3]) <= 255) {
					return true;
				}
			} else {
				JOptionPane.showMessageDialog(null, "IP address format error");
				return false;
			}
			
			return false;
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}