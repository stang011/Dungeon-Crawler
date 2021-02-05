package Code;
// Jay Schimmoller
// This class manages a server for a mutliplayer game

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;

public class Server {
	
	/*
	 * Private Inner class to manage one instance of a connection.
	 * connectionID = unique ID for the connection
	 * socket = actual connection
	 * input = input stream from the connection (client)
	 * output = the output stream to send information (to the client)
	 * Server = an instance of the outer class
	 */
	private class Connection {
		long connectionID = System.currentTimeMillis();
		Socket socket = null; 
		BufferedReader input = null;
		PrintWriter output = null;
		Server server = null;
		
		// Constructor used to create an instance of a connection based on current server
		public Connection(Server server, Socket socket) throws IOException {
			this.server = server;
			this.socket = socket;
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		}
		
		// Returns the connectionID
		public long getConnectionID() {
			return connectionID;
		}
		// Set a new connectionID
		public void setConnectionID(long connectionID) {
			this.connectionID = connectionID;
		}
	}
	
	/*
	 * Private Inner class to manage a thread to handle input from the connection
	 * This is where input from the connection is accepted and set to a listener
	 * Multiple connections can be made and handled through this class
	 */
	private class ClientThread extends Thread {
		Connection connection = null;
		
		// Constructor used to try to start a thread using a connection
		public ClientThread(Connection connection) {
			try {
				this.connection = connection;
				start();
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("ClientThread Connection Exception");
			}
		}
		
		/*
		 * Loops over input from the connection
		 * Sends signal to the connection listener to handle input
		 */
		public void run() {
			try {
				// A new connection has occurred
				ConnectionEvent event = new ConnectionEvent(ConnectionEventCode.CONNECTION_ESTABLISHED, connection.connectionID, "CONNECTION_ESTABLISHED");
				connection.server.listener.handle(event);
				String s = "";
				while(connection.server.isRunning && (s = connection.input.readLine()) != null) {
					event = new ConnectionEvent(ConnectionEventCode.TRANSMISSION_RECEIVED, connection.getConnectionID(), s);
					connection.server.listener.handle(event);
				}
				connection.input.close();
				connection.output.close();
				connection.socket.close();
			} catch(SocketException se) {

			} catch(Exception e) {
				//e.printStackTrace();
				System.out.println("Connection Event Error");
			} finally {
				// The connection has terminated
				ConnectionEvent event = new ConnectionEvent ( ConnectionEventCode.CONNECTION_TERMINATED, connection.connectionID, "CONNECTION TERMINATED" );
				connection.server.connectionMap.remove (connection.connectionID);
				connection.server.listener.handle(event);
				System.out.println("Player " + event.getConnectionID() + " disconnected");
			}
		}
	}
	
	// Variables to help control the server running
	private ConnectionListener listener = null;
	private int port = 2112;
	private boolean isRunning = false;
	private HashMap<Long, Connection> connectionMap = new HashMap<>();
	private ServerSocket serverSocket = null;
	
	public void startServer(int port) {
		this.port = port;
		if(!isRunning) {
			// Try to set up the server
			try(ServerSocket server = new ServerSocket(port)) {
				serverSocket = server;
				System.out.println("Waiting on " + getInetAddress() + " at " + getPort());
				isRunning = true;
				
				while(isRunning) {
					Socket client = server.accept();
					Connection connection = new Connection(this, client);
					connectionMap.put(connection.getConnectionID(), connection);
					System.out.println("Connected to " + connection.getConnectionID() + client.getInetAddress());
					// Adds the new client to a thread to handle events
					new ClientThread(connection);
				}
				
			} catch(SocketException se) {
				//se.printStackTrace();
				System.out.println("Server Socket Exception");
			} catch(IOException e) {
				//e.printStackTrace();
				System.out.println("Server Socket IOException");
			}
		}
	}
	
	// Returns the port being used to host the server
	public int getPort() {
		return port;
	}
	
	// Returns the host server IP Address
	public String getInetAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
	
	// Stops the server
	public void stopServer() {
		isRunning = false;
		try {
			serverSocket.close();
			System.exit(0);
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error stopping server");
		}
	}
	
	// Set the transmission listener
	public void setOnTransmission(ConnectionListener listener) {
		this.listener = listener;
	}
	
	// Returns true if a connection exists
	public boolean isConnected(long connectionID) {
		return (connectionMap.get(connectionID) != null) ? true : false;
	}
	
	// Close the connection
	public void disconnect(long connectionID) throws IOException, UnknownConnectionException {
		Connection connection = connectionMap.get(connectionID);
		if(connection != null) {
			connection.socket.close();
			connectionMap.remove(connectionID);
		}
		else {
			throw new UnknownConnectionException(connectionID, "Unknown Connection: " + connectionID);
		}
	}
	
	// Change the connection ID
	public void changeConnectionID(long connectionID, long newConnectionID) throws UnknownConnectionException {
		Connection connection = connectionMap.get(connectionID);
		if(connection != null) {
			connectionMap.remove(connectionID);
			connection.setConnectionID(newConnectionID);
			connectionMap.put(newConnectionID, connection);
		} 
		else {
			throw new UnknownConnectionException(connectionID, "Unknown Connection: " + connectionID);
		}
	}
	
	// Send text through the connection
	public void sendMessage(long connectionID, String message) throws UnknownConnectionException {
		Connection connection = connectionMap.get(connectionID);
		if(connection != null) {
			connection.output.println(message);
			connection.output.flush();
		} 
		else {
			throw new UnknownConnectionException(connectionID, "Unknown Connection: " + connectionID);
		}
	}
}