import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * This class implements the CardGameUI inteface.
 * This class is used to build a GUI for the Big Two card game and handle all user actions.
 * 
 * @author Kang Hyunwoo
 */
public class BigTwoGUI implements CardGameUI {
    /**
     * Creates a constructor for creating a BigTwoGUI.
     * 
     * @param game A reference to a Big Two card game associated with this GUI.
     */
    public BigTwoGUI(BigTwo game) {
		this.game = game;
        playerList = game.getPlayerList();
		handsOnTable = game.getHandsOnTable();

		frame = new JFrame();
		frame.setTitle("Big Two");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
        JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setPreferredSize(new Dimension(frame.getWidth(), 30));
		toolBar.add(Box.createHorizontalGlue());
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton connectButton = new JButton("Connect");
		JButton quitButton = new JButton("Quit");
        toolBar.add(connectButton);
        toolBar.add(quitButton);

		connectButton.addActionListener(new ConnectMenuItemListener());
        quitButton.addActionListener(new QuitMenuItemListener());
        frame.add(toolBar, BorderLayout.NORTH);

        JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        bigTwoPanel = new BigTwoPanel();
		bigTwoPanel.setLayout(new BorderLayout());
		bigTwoPanel.setBackground(new Color(0, 128, 0));
		bigTwoPanel.setOpaque(true);
		bigTwoPanel.setVisible(true);
		bigTwoPanel.setLocation(0, 0);
        topPanel.add(bigTwoPanel, BorderLayout.WEST);

        JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		//Create Play button
		playButton = new JButton("Play");
		bottomPanel.add(playButton, BorderLayout.WEST);
		
		//Create Pass button
		passButton = new JButton("Pass");
		bottomPanel.add(passButton, BorderLayout.EAST);
		
		//add event listener to bigtwo panel (mouse click) and action listeners to Play and Pass buttons
		playButton.addActionListener(new PlayButtonListener());
		passButton.addActionListener(new PassButtonListener());
		bigTwoPanel.addMouseListener(new BigTwoPanel());
		
		//panel for reulst messages and chat messages at right
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		
		//panel for result messages
		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout());
		
		msgArea = new JTextArea("");
		msgArea.setEditable(false);
		JScrollPane scrollMsg = new JScrollPane(msgArea);
		scrollMsg.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollMsg.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		msgPanel.add(scrollMsg, BorderLayout.CENTER);
		
		//Create chat panel
		JPanel chatPanel = new JPanel(new BorderLayout());
		chatArea = new JTextArea("");
		JScrollPane scrollChat = new JScrollPane(chatArea);
		chatArea.setEditable(false);
		scrollChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		chatPanel.add(scrollChat, BorderLayout.CENTER);
		
		//make panel for chat and send buttons
		rightPanel.add(msgPanel);
		rightPanel.add(chatPanel);
		topPanel.add(rightPanel, BorderLayout.CENTER); 
		
		//Label the message input
		JLabel sendLabel = new JLabel("Message: ");
		chatInput = new JTextField(20);
		bottomPanel.add(sendLabel);
		bottomPanel.add(chatInput, BorderLayout.EAST);
		chatInput.addActionListener(new SendMessageListener());
		
		//add two panels to the frame
		frame.add(topPanel);
		frame.add(bottomPanel);
		
		frame.setSize(1000, 800);
		frame.setVisible(true);
    }

    private BigTwo game;
    private JFrame frame;
    private JPanel bigTwoPanel;
    private JButton playButton;
    private JButton passButton;
    private JTextArea msgArea;
    private JTextArea chatArea;
    private JTextField chatInput;

    private final static int MAX_CARD_NUM = 13;
	private ArrayList<CardGamePlayer> playerList;
	private ArrayList<Hand> handsOnTable;
	private int activePlayer;
	private boolean[] selected = new boolean[MAX_CARD_NUM];
    private Image cardBackImage;
    private Image[] playerImage;
    private Image[][] cardImage;

    /**
	 * Gets the text area for showing the current game status as well as end of game messages.
     * 
	 * @return The text area for showing the current game status as well as end of game messages.
	 */
    public JTextArea getMsgArea() {
        return msgArea;
    }
    /**
	 * Gets the text area for showing chat messages sent by the players.
     * 
	 * @return The text area for showing chat messages sent by the players.
	 */
    public JTextArea getChatArea() {
        return chatArea;
    }
    /**
	 * Sets the index of the active player.
     * Active player is the player having control of the GUI.
	 * 
	 * @param activePlayer The index of the active player.
	 */
    public void setActivePlayer(int activePlayer) {
        if (activePlayer < 0 || activePlayer >= playerList.size()) {
			this.activePlayer = -1;
		} else {
			this.activePlayer = activePlayer;
		}
    }
    /**
	 * Repaints the GUI.
	 */
    public void repaint() {
        if(!game.endOfGame()) {
			String name = playerList.get(activePlayer).getName();
			String msg = String.format("%s's turn:\n", name);
			getMsgArea().append(msg);
		}
		frame.repaint();
    }
	/**
	 * Prints the specified string to the chat area of the GUI. 
	 * 
	 * @param msg The message to be printed.
	 */
    public void printChat(String msg) {
        chatArea.append(msg);
		chatArea.append("\n");
    }
    /**
	 * Prints the specified string to the message area of the GUI. 
	 * 
	 * @param msg The message to be printed.
	 */
    public void printMsg(String msg) {
        getMsgArea().append(msg);
		getMsgArea().setCaretPosition(getMsgArea().getDocument().getLength());
    }
    /**
	 * Clears the message area of the GUI.
	 */
    public void clearMsgArea() {
        msgArea.setText("");
    }
    /**
	 * Resets the GUI.
	 */
    public void reset() {
        resetSelected();
        clearMsgArea();
        enable();
    }
    /**
	 * Enables user interactions with the GUI.
	 */
    public void enable() {
        this.playButton.setEnabled(true);
		this.passButton.setEnabled(true);
        this.bigTwoPanel.setEnabled(true);
    }
    /**
	 * Disables user interactions with the GUI.
	 */
    public void disable() {
        this.playButton.setEnabled(false);
		this.passButton.setEnabled(false);
        this.bigTwoPanel.setEnabled(false);
    }
    /**
	 * Prompts the active player to select cards and make his/her move.
	 */
    public void promptActivePlayer() {
        int[] cardIdx = getSelected();
		resetSelected();	
		game.makeMove(activePlayer, cardIdx);
    }

	private int[] getSelected() {
		int[] cardIdx = null;
		int count = 0;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				count++;
			}
		}

		if (count != 0) {
			cardIdx = new int[count];
			count = 0;
			for (int i = 0; i < selected.length; i++) {
				if (selected[i]) {
					cardIdx[count] = i;
					count++;
				}
			}
		}
		return cardIdx;
	}
	private void resetSelected() {
		for (int i = 0; i < selected.length; i++) {
			selected[i] = false;
		}
	}

    class BigTwoPanel extends JPanel implements MouseListener {        
        /**
         * Creates a constructor for creating a BigTwoPanel.
         * 
         * @param game A reference to a Big Two card game associated with this GUI.
         */
        public BigTwoPanel() {
            cardBackImage = new ImageIcon("Images/cards/b.jpg").getImage();
			
			playerImage = new Image[4];
            playerImage[0] = new ImageIcon("Images/avatars/Cordelius.png").getImage();
            playerImage[1] = new ImageIcon("Images/avatars/Piper.png").getImage();
            playerImage[2] = new ImageIcon("Images/avatars/Spike.png").getImage();
            playerImage[3] = new ImageIcon("Images/avatars/Tara.png").getImage();

            cardImage = new Image[4][13];
            String[] SUITS = {"d", "c", "h", "s"};
			String[] RANKS = {"a", "2", "3", "4", "5", "6", "7", "8", "9", "t", "j", "q", "k"};
            for(int suitIdx = 0; suitIdx < SUITS.length; suitIdx++) {
				for(int rankIdx=0; rankIdx < RANKS.length; rankIdx++) {
					String imagePath = "Images/cards/" + RANKS[rankIdx] + SUITS[suitIdx] + ".gif";
					cardImage[suitIdx][rankIdx] = new ImageIcon(imagePath).getImage();
				}
			}
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

			bigTwoPanel.setOpaque(true);
			int imageWidth = 60;
			int imageHeight = 60;
			int rowHeight = getHeight() / 5;
			Font labelFont = new Font("Arial", Font.BOLD, 12); // Define the font for the labels
			int labelOffsetY = 15;
			
			for (int i = 0; i < playerImage.length; i++) {
		        int y = 35 + i * rowHeight; // Calculate the y-coordinate for each row

		        // Draw the image at the center of the row
		        int x = 0; // Align the image to the left
		        g.drawImage(playerImage[i], x, y, imageWidth, imageHeight, null);
		        String label = "";
		        if(i == activePlayer) {
		        	label = "You";
		        }
		        else {
		        	label = "Player " + i; // Generate the player label
		        }
		        
		        int labelWidth = g.getFontMetrics(labelFont).stringWidth(label); // Calculate the width of the label
		        int labelX = x + (imageWidth - labelWidth) / 2; // Calculate the x-coordinate for centering the label
		        int labelY = y + imageHeight + labelOffsetY; // Calculate the y-coordinate for the label
		        g.setFont(labelFont);
		        g.drawString(label, labelX, labelY);
			}
			
			//print the name of LastPlayer on the Table
	        Hand lastHandOnTable = (handsOnTable.isEmpty()) ? null : handsOnTable.get(handsOnTable.size() - 1);
			if (lastHandOnTable != null) {
				String lastHandLabel = lastHandOnTable.getPlayer().getName();
				int labelWidth = g.getFontMetrics(labelFont).stringWidth(lastHandLabel); // Calculate the width of the label
		        int labelX = 0 + (imageWidth - labelWidth) / 2; // Calculate the x-coordinate for centering the label
		        int labelY = 35 + 4  * rowHeight ; // Calculate the y-coordinate for the label
		        g.setFont(labelFont);
		        g.drawString("<Table>", labelX, labelY);
		        labelX += 15;
		        labelY += 15;
		        g.drawString(lastHandLabel, labelX - 10, labelY);
			}
			
			//Draw cards for each player
			for (int i = 0; i < 4; i++) {
		        // Draw the image at the center of the row
		        int x = 0; // Align the image to the left
				int y=this.getHeight()/5;
				
		        if (i==activePlayer) {
		        	CardList cardsInHand = game.getPlayerList().get(activePlayer).getCardsInHand();
		        	
		        	for(int j=0; j<cardsInHand.size(); j++) {
		        		Card card = cardsInHand.getCard(j);
		        		
		        		if (selected[j]) { //if card is selected one, make it little bit higher
		        			g.drawImage(cardImage[card.getSuit()][card.getRank()], this.getWidth()/6+x, 30+y*i-25, this);
		        			x+=this.getWidth()/30;
		        		}
		        		else { //initially draw cards
		        			g.drawImage(cardImage[card.getSuit()][card.getRank()], this.getWidth()/6+x, 30+y*i,  this);
							x+=this.getWidth()/30;
		        		}
		        	}
		        }
		        else { //draw other player's cards
		        	CardList cardsInHand = game.getPlayerList().get(i).getCardsInHand();
		        	for (int k=0; k< cardsInHand.size();k++) {
						g.drawImage(cardBackImage, this.getWidth()/6+x, 30+y*i, this);
						x+=this.getWidth()/30;
					}
		        }
		        
			}
			
			
			//draw table cards
			if (game.getHandsOnTable().size()!=0) {
				int x =0; 
				Hand handsOnTable = game.getHandsOnTable().get(game.getHandsOnTable().size()-1);		
				
				for (int i=0; i<handsOnTable.size();i++) {
					Card card = handsOnTable.getCard(i);
					g.drawImage(cardImage[card.getSuit()][card.getRank()], this.getWidth()/6+x, 10+4*(this.getHeight()/5),this);
					x+=this.getWidth()/30;
				}
			}
		}

        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {
            int mouseX = e.getX();
			int mouseY = e.getY();
			int cardHeight = 97;
		    int cardWidth = 73;
		    int xGap = bigTwoPanel.getWidth() / 30; //distance between overlapped card

		    for (int i = 0; i < 4; i++) {
		    	int playerCardY = 30 + activePlayer * (bigTwoPanel.getHeight() / 5);
		    	
		    	if(mouseY >= playerCardY && mouseY <= playerCardY + cardHeight) {
		    		CardList cardsInHand = game.getPlayerList().get(activePlayer).getCardsInHand();
			    	for (int j=0; j<cardsInHand.size(); j++) {
			    		int cardX = bigTwoPanel.getWidth() / 6 + j * (bigTwoPanel.getWidth()/30); //calculate the x-coordinate of card
			    		
			    		//handle clicking the last card of player
			    		if(j== cardsInHand.size() - 1) {
			    			if (mouseX >= cardX && mouseX <= cardX + cardWidth) {
			    				//if card is clicked already, unclick it
				    			selected[j] = !selected[j];
				    			bigTwoPanel.repaint();
				    			return;
			    			}
			    		}
			    		//handle clicking non-last card of player
			    		if (mouseX >= cardX && mouseX <= cardX + xGap) {
			    			selected[j] = !selected[j];
			    			bigTwoPanel.repaint();
			    			return;
			    		}
			    	}
		    	}
		    }
        }

        public void mouseEntered(MouseEvent e) { }

        public void mouseExited(MouseEvent e) { }
    }
    class PlayButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(getSelected()!=null ) {
				promptActivePlayer();
				repaint();
			} else {
				String msg = "Please select cards to play.\n";
				printMsg(msg);
				repaint();
			}
        }
    }
    class PassButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(getSelected() == null) {
				promptActivePlayer();
				repaint();
			}
			else {
				for(int i=0; i<selected.length; i++) {
					selected[i] = false;
				}
				promptActivePlayer();
				repaint();
			}
        }
    }
    class ConnectMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            game.getClient().connect();
        }
    }
    class QuitMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    class SendMessageListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String chat = chatInput.getText();
			chatArea.append("Player " + activePlayer + ": " + chat + "\n");
			chatInput.setText("");
        }
    }
}
