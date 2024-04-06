import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;

/**
 * This class is used to model a Big Two card game server.
 * 
 * @author Kang Hyunwoo
 */
public class BigTwoClient implements NetworkGame {
	/**
     * Creates a Big Two client
     * 
     * @param game A reference to a BigTwo object associated with this client
     * @param gui A reference to a BigTwoGUI object associated the BigTwo object
     */
    public BigTwoClient(BigTwo game, BigTwoGUI gui) {
        this.game = game;
        this.gui = gui;

        setServerIP("127.0.0.1");
		setServerPort(2396);

		String nameInput = JOptionPane.showInputDialog("Enter your name: ");
		this.setPlayerName(nameInput);
    }
    
    // Private instance variables
    private BigTwo game;
    private BigTwoGUI gui;
    private Socket sock;
    private ObjectOutputStream oos;
    private int playerID;
    private String playerName;
    private String serverIP;
    private int serverPort;

    // NetworkGame interface methods
    /**
     * Gets the playerID (i.e., index) of the local player
     * 
     * @return The playerID (i.e., index) of the local player
     */
    public int getPlayerID() {
        return this.playerID;
    }
    /**
     * Sets the playerID (i.e., index) of the local player
     * 
     * @param playerID Player ID (index) of the local player
     */
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
    /**
     * Gets the name of the local player
     * 
     * @return The name of the local player
     */
    public String getPlayerName() {
        return this.playerName;
    }
    /**
     * Sets the name of the local player
     * 
     * @param playerName The name of the local player
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    /**
     * Gets the IP address of the game server
     * 
     * @return The IP address of the game server
     */
    public String getServerIP() {
        return this.serverIP;
    }
    /**
     * Sets the IP address of the game server
     * 
     * @param serverIP The IP address of the game server
     */
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }
    /**
     * Gets the TCP port of the game server
     * 
     * @return The TCP port of the game server
     */
    public int getServerPort() {
        return this.serverPort;
    }
    /**
     * Sets the TCP port of the game server
     * 
     * @param serverPort The TCP port of the game server
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    /**
     * Makes a socket connection with the game server
     */
    public synchronized void connect() {
        try {
            this.sock = new Socket(this.serverIP, this.serverPort);
            this.oos = new ObjectOutputStream(sock.getOutputStream());

            Thread readerThread = new Thread(new ServerHandler());
            readerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Parses the messages received from the game server
     * 
     * @param message The message to be parsed
     */
    public synchronized void parseMessage(GameMessage message) {
        int id = message.getPlayerID();
        Object data = message.getData();
        String[] playerNames = game.getPlayerNames();

        switch (message.getType()) {
            case 0: // PLAYER_LIST
                this.setPlayerID(id);
                game.setPlayerNames((String[]) data);

                CardGameMessage joinMessage = new CardGameMessage(CardGameMessage.JOIN, -1, this.getPlayerName());
                this.sendMessage(joinMessage);
                break;
            case 1: // JOIN
                playerNames[id] = (String) data;
                game.setPlayerNames(playerNames);
                if(this.playerID == id) {
                    CardGameMessage readyMessage = new CardGameMessage(CardGameMessage.READY, -1, null);
                    sendMessage(readyMessage);
                }
                else {
                    gui.printMsg((String) message.getData()+" joined the game\n");
                }
                
                game.getPlayerList().get(message.getPlayerID()).setName((String)message.getData());
                gui.repaint();
    
            case 2: // FULL    
                gui.printMsg("The server is full, you cannot join the game.");
                break;
            case 3: // QUIT
                playerNames[id] = "";
                game.setPlayerNames(playerNames);

                gui.disable();
                gui.repaint();

                CardGameMessage readyMessage = new CardGameMessage(CardGameMessage.READY, -1, null);
                this.sendMessage(readyMessage);
                break;
            case 4: // READY
                String playerName = game.getPlayerNames()[id];
                gui.printMsg(String.format("%s (Player ID: %s) is ready.\n", playerName, id));
                break;
            case 5: // START
                game.start((Deck) data);
            case 6: // MOVE
                game.checkMove(id, (int[]) data);
            case 7: // MSG
                gui.printChat((String) data);
            default:
                break;
        }
    }
    /**
     * Sends the specified message to the game server
     * 
     * @param message The message to be sent
     */
    public void sendMessage(GameMessage message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class
    class ServerHandler implements Runnable {
        private ObjectInputStream os;

        public ServerHandler() {
            try {
                this.os = new ObjectInputStream(sock.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void run() {
            CardGameMessage message;
            try {
                while ((message = (CardGameMessage) os.readObject()) != null) {
                    parseMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            gui.repaint();
        }
        
    } 
}
