// Jonathan Gin and Raymond Ye
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Random;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class Bingo {

	private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 24);
	private static final Border TITLE_BORDER = BorderFactory.createLineBorder(Color.BLACK, 3);
	private static final Border CELL_BORDER = BorderFactory.createLineBorder(Color.BLACK);
	private static final Dimension CELL_SIZE = new Dimension(50, 50);
	private static JFrame frame = new JFrame("Bingo");
	private static JLabel call = new JLabel();
	private static JLabel[][] callBoardLabels = new JLabel[5][15];
	private static JLabel[][][] computerCards = new JLabel[2][5][5];
	private static JButton[][][] playerCards = new JButton[2][5][5];
	private static Timer timer = new Timer(4000, new BingoCall());

	public static void main(String[] args) {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem newGame = new JMenuItem("New Game");
		JMenuItem callBingo = new JMenuItem("Call BINGO");
		JMenuItem exit = new JMenuItem("Exit");

		timer.setInitialDelay(2000);
		call.setFont(new Font("Calibri", Font.BOLD, 26));
		menu.setMnemonic(KeyEvent.VK_F);
		newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		callBingo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		newGame.addActionListener(new MenuAction());
		callBingo.addActionListener(new MenuAction());
		exit.addActionListener(new MenuAction());
		menu.add(newGame);
		menu.add(callBingo);
		menu.addSeparator();
		menu.add(exit);
		menuBar.add(menu);
		buildFrame();

		frame.setVisible(true);
		frame.setResizable(false);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
	}

	// Creates and adds frame's components, then starts timer
	private static void buildFrame(){
		JPanel mainPanel = new JPanel();
		JPanel callBoard = new JPanel(new GridLayout(5, 16));
		JPanel callBoardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel computerCardPanel = new JPanel();
		JPanel playerCardPanel = new JPanel();
		JPanel callPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// Resets call text, call board and cards
		call.setText("Get ready to play BINGO!");
		createCallBoard(callBoard);
		computerCards[0] = (JLabel[][]) generateCard("CPU", -1);
		computerCards[1] = (JLabel[][]) generateCard("CPU", -1);
		playerCards[0] = (JButton[][]) generateCard("player", 0);
		playerCards[1] = (JButton[][]) generateCard("player", 1);

		callBoardPanel.add(callBoard);
		cardPanel.add(computerCardPanel);
		cardPanel.add(playerCardPanel);
		callPanel.add(call);
		mainPanel.add(callBoardPanel);
		mainPanel.add(cardPanel);
		mainPanel.add(callPanel);
		addCards(computerCardPanel, "CPU");
		addCards(playerCardPanel, "player");

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		callBoard.setBorder(BorderFactory.createTitledBorder(TITLE_BORDER, "Call Board", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, TITLE_FONT, Color.GREEN));
		computerCardPanel.setBorder(BorderFactory.createTitledBorder(TITLE_BORDER, "CPU Board", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, TITLE_FONT, Color.BLUE));
		playerCardPanel.setBorder(BorderFactory.createTitledBorder(TITLE_BORDER, "Player Board", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, TITLE_FONT, Color.RED));

		frame.setContentPane(mainPanel);
		frame.revalidate();
		frame.repaint();
		timer.start();
	}

	// Generates a bingo call and reads out call
	private static class BingoCall implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			Random rand = new Random();
			int randNum;
			// Maps out "B", "I", "N", "G", "O" to their associated integer values
			NavigableMap<Integer, String> bingoLetters = new TreeMap<Integer, String>();
			bingoLetters.put(1, "B");
			bingoLetters.put(16, "I");
			bingoLetters.put(31, "N");
			bingoLetters.put(46, "G");
			bingoLetters.put(61, "O");

			// Checks if number has already been called by cross referencing call board, if taken, generates another random number
			do {
				randNum = rand.nextInt(75) + 1;
			} while (callBoardLabels[(randNum - 1) / 15][(randNum - 1) % 15].isOpaque());

			call.setText("The current call is: " + bingoLetters.floorEntry(randNum).getValue() + Integer.toString(randNum));
			// Updates call board, then daubs for the CPU
			callBoardLabels[(randNum - 1) / 15][(randNum - 1) % 15].setBackground(Color.GREEN);
			callBoardLabels[(randNum - 1) / 15][(randNum - 1) % 15].setOpaque(true);
			daub("CPU", -1);
		}
	}

	// Player daub. Checks if daub is valid, if not, ends game
	private static class ButtonPressed implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			if (event.getActionCommand().equals(call.getText().substring(call.getText().indexOf(":") + 3).trim())) {
				// Uses name of button set during card creation to determine which card to daub
				daub("player", Integer.parseInt(((JButton) event.getSource()).getName()));
			} else {
				endGame("False daub! You have lost the game!");
			}
		}
	}

	private static class MenuAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			if (((JMenuItem) event.getSource()).getText().equals("New Game")) { // Stops timer and resets frame
				timer.stop();
				buildFrame();
			} else if (timer.isRunning() && ((JMenuItem) event.getSource()).getText().equals("Call BINGO")) { // Player BINGO call. Ends game if invalid
				if (!hasWon("player")) { // If hasWon is true, endGame is handled in the hasWon method
					endGame("False BINGO call! You have lost the game!");
				}
			} else if (timer.isRunning() && ((JMenuItem) event.getSource()).getText().equals("Exit")) { // Exits application
				System.exit(0);
			}
		}
	}

	// Creates call board
	public static void createCallBoard(JPanel callBoard) {
		for (int row = 0; row < callBoardLabels.length; row++) {
			// Adds the BINGO bar on the side
			callBoard.add(addBingoHeader(null)[row]);
			for (int col = 0; col < callBoardLabels[row].length; col++) {
				callBoardLabels[row][col] = new JLabel(Integer.toString(col + row * 15 + 1), SwingConstants.CENTER);
				callBoardLabels[row][col].setForeground(Color.BLUE);
				callBoardLabels[row][col].setBorder(CELL_BORDER);
				callBoardLabels[row][col].setPreferredSize(CELL_SIZE);
				callBoard.add(callBoardLabels[row][col]);
			}
		}
	}

	// Creates and returns a JLabel array, if panel is given, directly add header to panel
	public static JLabel[] addBingoHeader(JPanel panel) {
		JLabel[] bingo = {new JLabel("B", SwingConstants.CENTER), new JLabel("I", SwingConstants.CENTER), new JLabel("N", SwingConstants.CENTER), new JLabel("G", SwingConstants.CENTER), new JLabel("O", SwingConstants.CENTER)};

		for (int i = 0; i < bingo.length; i++) {
			bingo[i].setForeground(Color.RED);
			bingo[i].setPreferredSize(CELL_SIZE);
			bingo[i].setBorder(CELL_BORDER);
			if (panel != null) {
				panel.add(bingo[i]);
			}
		}
		return bingo;
	}

	// Returns a String array of randomly generated numbers to be inserted into cards
	public static String[][] generateCardNumbers() {
		Random rand = new Random((int) Math.ceil(Math.random() * 1000000000));
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		String[][] cardNumbers = new String[5][5];
		int randNum = rand.nextInt(15) + 1;

		for (int col = 0; col < cardNumbers[0].length; col++) {
			for (int row = 0; row < cardNumbers.length; row++) {
				// Checks if number has already been used
				while (numbers.contains(randNum)) {
					randNum = rand.nextInt(15) + 15 * col + 1;
				}
				numbers.add(randNum);
				cardNumbers[row][col] = Integer.toString(randNum);
			}
		}
		cardNumbers[2][2] = "\u2605";
		return cardNumbers;
	}

	// Used to generate player or CPU cards
	public static Object generateCard(String identity, int cardNumber) {
		JLabel[][] computerLabels = new JLabel[5][5];
		JButton[][] playerButtons = new JButton[5][5];
		String[][] cardNumbers = generateCardNumbers();

		for (int row = 0; row < computerLabels.length; row++) {
			for (int col = 0; col < computerLabels[row].length; col++) {
				// Sets size, colour and number of every individual cell
				if (identity.equals("CPU")) {
					computerLabels[row][col] = new JLabel(cardNumbers[row][col], SwingConstants.CENTER);
					computerLabels[row][col].setForeground(Color.BLUE);
					computerLabels[row][col].setPreferredSize(CELL_SIZE);
					computerLabels[row][col].setBorder(CELL_BORDER);
				} else {
					playerButtons[row][col] = new JButton(cardNumbers[row][col]);
					playerButtons[row][col].setForeground(Color.RED);
					playerButtons[row][col].setPreferredSize(CELL_SIZE);
					playerButtons[row][col].addActionListener(new ButtonPressed());
					playerButtons[row][col].setOpaque(false);
					playerButtons[row][col].setName(Integer.toString(cardNumber)); // Name is used to differentiate between the 2 player cards (for daubing purposes)
				}
			}
		}
		// Sets the centre of each card as daubed
		if (identity.equals("CPU")) {
			computerLabels[2][2].setForeground(Color.GRAY);
			computerLabels[2][2].setBackground(Color.BLUE);
			computerLabels[2][2].setOpaque(true);
		} else {
			playerButtons[2][2].setBackground(Color.RED);
			playerButtons[2][2].setEnabled(false);
			playerButtons[2][2].setOpaque(true);
		}
		// Returns either CPU card or player card based on the parameter String identity
		return identity.equals("CPU") ? computerLabels : playerButtons;
	}

	// Adds either player or CPU cards to cardPanel (based on String identity)
	public static void addCards(JPanel panel, String identity) {
		JPanel cardPanel;

		for (int card = 0; card < computerCards.length; card++) {
			cardPanel = new JPanel(new GridLayout(6, 5));
			addBingoHeader(cardPanel);
			for (int row = 0; row < computerCards[card].length; row++) {
				for (int col = 0; col < computerCards[card][row].length; col++) {
					if (identity.equals("CPU")) {
						cardPanel.add(computerCards[card][row][col]);
					} else {
						cardPanel.add(playerCards[card][row][col]);
					}
				}
			}
			panel.add(cardPanel);
		}
	}

	// Daubs CPU or player cards (based on String identity). Checks for win if computer has daubed
	public static void daub(String identity, int cardNumber) {
		for (int card = 0; card < computerCards.length; card++) {
			for (int row = 0; row < computerCards[card].length; row++) {
				for (int col = 0; col < computerCards[card][row].length; col++) {
					if (identity.equals("CPU") && computerCards[card][row][col].getText().equals(call.getText().substring(call.getText().indexOf(":") + 3).trim())) {
						computerCards[card][row][col].setForeground(Color.GRAY);
						computerCards[card][row][col].setBackground(Color.BLUE);
						computerCards[card][row][col].setOpaque(true);
						hasWon("CPU");
					} else if (identity.equals("player") && playerCards[cardNumber][row][col].getText().equals(call.getText().substring(call.getText().indexOf(":") + 3).trim())) {
						playerCards[cardNumber][row][col].setForeground(Color.GRAY);
						playerCards[cardNumber][row][col].setBackground(Color.RED);
						playerCards[cardNumber][row][col].setOpaque(true);
						playerCards[cardNumber][row][col].setEnabled(false);
					}
				}
			}
		}
	}

	// Checks CPU or player cards (based on String identity) for a win. If there is a win, highlight winning row/col/diagonal green
	public static boolean hasWon(String identity) {
		int countHorizontal = 0;
		int countVertical = 0;
		int countDownDiagonal = 0;
		int countUpDiagonal = 0;

		for (int card = 0; card < computerCards.length; card++) {
			countDownDiagonal = 0;
			countUpDiagonal = 0;
			for (int row = 0; row < computerCards[card].length; row++) {
				countHorizontal = 0;
				countVertical = 0;
				// Diagonal win check
				countDownDiagonal = identity.equals("CPU") && computerCards[card][row][row].isOpaque() || identity.equals("player") && playerCards[card][row][row].isOpaque() ? ++countDownDiagonal : 0;
				countUpDiagonal = identity.equals("CPU") && computerCards[card][row][computerCards[0].length - row - 1].isOpaque() || identity.equals("player") && playerCards[card][row][playerCards[0].length - row - 1].isOpaque() ? ++countUpDiagonal : 0;
				for (int col = 0; col < computerCards[0][0].length; col++) {
					// Horizontal and vertical win check
					countHorizontal = identity.equals("CPU") && computerCards[card][row][col].isOpaque() || identity.equals("player") && playerCards[card][row][col].isOpaque() ? ++countHorizontal : 0;
					countVertical = identity.equals("CPU") && computerCards[card][col][row].isOpaque() || identity.equals("player") && playerCards[card][col][row].isOpaque() ? ++countVertical : 0;
					// Checks for win, then identifies it (in green)
					if (countHorizontal == 5 || countVertical == 5 || countDownDiagonal == 5 || countUpDiagonal == 5) {
						for (int i = 0; i < computerCards[card].length; i++) {
							if (countHorizontal == 5 && identity.equals("CPU")) {
								computerCards[card][row][i].setBackground(Color.GREEN);
							} else if (countVertical == 5 && identity.equals("CPU")) {
								computerCards[card][i][row].setBackground(Color.GREEN);
							} else if (countDownDiagonal == 5 && identity.equals("CPU")) {
								computerCards[card][i][i].setBackground(Color.GREEN);
							} else if (countUpDiagonal == 5 && identity.equals("CPU")) {
								computerCards[card][computerCards[0].length - 1 - i][i].setBackground(Color.GREEN);
							} else if (countHorizontal == 5 && identity.equals("player")) {
								playerCards[card][row][i].setBackground(Color.GREEN);
							} else if (countVertical == 5 && identity.equals("player")) {
								playerCards[card][i][row].setBackground(Color.GREEN);
							} else if (countDownDiagonal == 5 && identity.equals("player")) {
								playerCards[card][i][i].setBackground(Color.GREEN);
							} else if (countUpDiagonal == 5 && identity.equals("player")) {
								playerCards[card][computerCards[0].length - 1 - i][i].setBackground(Color.GREEN);
							}
						}
						// Determines who won the game and whether it was a horizontal, vertical, or diagonal win
						endGame("The " + identity + " has won the game! (" + (countHorizontal == 5 ? "Horizontal" : countVertical == 5 ? "Vertical" : "Diagonal") + " Bingo)");
						return true;
					}
				}
			}
		}
		return false;
	}

	// End game routine
	public static void endGame(String display) {
		timer.stop();
		JOptionPane.showMessageDialog(frame, display); // Displays circumstances on which the game was ended
		call.setText("Select New Game from the menu to play again!");
		// Disables CPU and player cards
		for (int card = 0; card < playerCards.length; card++) {
			for (int row = 0; row < playerCards[card].length; row++) {
				for (int col = 0; col < playerCards[card][row].length; col++) {
					playerCards[card][row][col].setEnabled(false);
					computerCards[card][row][col].setEnabled(false);
				}
			}
		}
	}
}