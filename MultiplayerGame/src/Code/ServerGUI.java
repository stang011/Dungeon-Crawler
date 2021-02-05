package Code;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

// Jay Schimmoller

public class ServerGUI extends JFrame {
	
	private int width = 300;
	private int height = 400;
	private boolean running = true;
	private JButton exit;
	private JButton clear;
	private JButton reset;
	private JButton diffUp;
	private JButton diffDown;
	private JTextArea text;
	private String ip;
	
	private double difficulty = 1;
	
	private boolean needReset = false;

	public ServerGUI() {
		Container window = getContentPane();
		window.setLayout(new BorderLayout());
		
		JPanel bottomPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		
		diffUp = new JButton("+");
		diffUp.addActionListener(new ButtonListener());
		bottomPanel.add(diffUp);
		
		diffDown = new JButton("-");
		diffDown.addActionListener(new ButtonListener());
		bottomPanel.add(diffDown);
		
		clear = new JButton("Clear");
		clear.addActionListener(new ButtonListener());
		bottomPanel.add(clear);
		
		reset = new JButton("Reset");
		reset.addActionListener(new ButtonListener());
		bottomPanel.add(reset);
		
		exit = new JButton("Exit");
		exit.addActionListener(new ButtonListener());
		bottomPanel.add(exit);
		
		
		String ip = getIP();
		
		text = new JTextArea(ip, 15, 26);
		text.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(text);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		centerPanel.add(scroll);
		
		window.add(centerPanel, BorderLayout.CENTER);
		window.add(bottomPanel, BorderLayout.SOUTH);
		
		setTitle("Server Status");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    setSize(325,325);
	    setVisible(true);
	    setResizable(false); 
	    setLocation(1565, 30);
	    //setLocation(1950, 30);
	}
	
	public class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == exit) {
				running = false;
			}
			if(event.getSource() == reset) {
				needReset = true;
			}
			if(event.getSource() == clear) {
				text.setText(ip);
			}
			if(event.getSource() == diffUp) {
				difficulty += 1;
				text.append("Difficulty level: "  + difficulty + "\n\n");
				text.setCaretPosition(text.getDocument().getLength());
			}
			if(event.getSource() == diffDown) {
				difficulty -= 1;
				if(difficulty < 1) {
					text.append("Difficulty level cannot be less than 1\n");
					difficulty = 1;
				}
				text.append("Difficulty level: "  + difficulty + "\n\n");
				text.setCaretPosition(text.getDocument().getLength());
			}
		}	   
	}
	
	public String getIP() {
		ip = "Public IP: ";
		try {
			URL urlName = new URL("http://bot.whatismyipaddress.com");
			BufferedReader sc = new BufferedReader(new InputStreamReader(urlName.openStream()));
			ip += sc.readLine().trim() + "\n";
			sc.close();
		}
		catch(Exception e) {
			ip += "IP fetching error\n";
		}
		
		String localIP = "";
		try {
			localIP = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
			localIP = "Local IP error";
		}
		ip += "Local IP: " + localIP + "\n";
		
		return ip;
	}
	
	public void setReset(boolean r) {
		needReset = r;
	}
	
	public void ready() {
		text.append("Server ready for connection.\n\n");
	}
	
	public double getDifficulty() {
		return difficulty;
	}
	
	public boolean isRunning() {
		return running;
	}
	public boolean needReset() {
		return needReset;
	}
	public void update(String message) {
		text.append(message);
		text.setCaretPosition(text.getDocument().getLength());
	}
}