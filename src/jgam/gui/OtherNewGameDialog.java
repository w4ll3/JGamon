///*
// JGammon: A Backgammon client with nice graphics written in Java
// Copyright (C) 2005 Mattias Ulbrich
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//package jgam.gui;
//
//import jgam.JGammon;
//import jgam.game.BoardSnapshot;
//import jgam.game.Game;
//import jgam.game.Player;
//import jgam.net.GameConnection;
//
//import javax.swing.*;
//import javax.swing.event.ChangeListener;
//import javax.swing.filechooser.FileFilter;
//import java.awt.*;
//import java.io.File;
//import java.net.InetAddress;
//import java.util.ResourceBundle;
//
///**
// * This dialog is showed before starting a new game.
// * <p>
// * Some pictures are taken from: iconarchive.com
// *
// * @author Mattias Ulbrich
// */
//class OtherNewGameDialog extends JDialog {
//
//	private ResourceBundle msg = ResourceBundle.getBundle("jgam.msg.NewGameDialog");
//
//	// Layout
//	private JPanel mainPanel = new JPanel();
//	private GridBagLayout gridBagLayout = new GridBagLayout();
//	private Component component = Box.createHorizontalStrut(8);
//
//	// Local vs. Network RadioGroup
//	private ButtonGroup localOrNetworkGroup = new ButtonGroup();
//	private JRadioButton localPlayersRadioButton = new JRadioButton();
//	private JRadioButton localPlayerVsAiRadioButton = new JRadioButton();
//	private JRadioButton localAiVsAiRadioButton = new JRadioButton();
//	private JRadioButton networkPlayersRadioButton = new JRadioButton();
//
//	// Local Options
//	private JLabel whitePlayerNameLabel = new JLabel();
//	private JTextField whitePlayerNameField = new JTextField();
//	private JLabel bluePlayerNameLabel = new JLabel();
//	private JTextField bluePlayerNameField = new JTextField();
//
//	// Network Options
//	private ButtonGroup networkOptionsGroup = new ButtonGroup();
//	private JRadioButton networkServerIsWhite = new JRadioButton();
//	private JRadioButton networkServerIsBlue = new JRadioButton();
//	private JRadioButton networkJoinAsClient = new JRadioButton();
//	private JRadioButton networkJoinGnuBG = new JRadioButton();
//
//	// Server Name
//	private JLabel networkServerNameLabel = new JLabel();
//	private JTextField networkServerNameField = new JTextField();
//
//	// Port Number
//	private JLabel networkPortNumberLabel = new JLabel();
//	private JTextField networkPortNumberField = new JTextField();
//
//	// Network Player Name
//	private JLabel networkPlayerNameLabel = new JLabel();
//	private JTextField networkPlayerNameField = new JTextField();
//
//	// Local Players Icon
//	private JLabel localPlayersIconLabel = new JLabel();
//	private ImageIcon localPlayersIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/local.gif"));
//
//	// Network Server Icon
//	private JLabel networkServerIconLabel = new JLabel();
//	private ImageIcon networkServerIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/server.gif"));
//
//	// Network Client Icon
//	private JLabel networkClientIconLabel = new JLabel();
//	private ImageIcon networkClientIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/connect.gif"));
//
//	// Load Board Icon
//	private JLabel loadBoardIconLabel = new JLabel();
//	private ImageIcon loadBoardIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/open.gif"));
//
//	// Load Board Options
//	private JToggleButton loadBoardButton = new JToggleButton();
//	private JLabel loadBoardFileLabel = new JLabel();
//	private File loadBoardFile = null;
//
//	// Form Buttons
//	private JButton cancelButton = new JButton();
//	private JButton confirmButton = new JButton();
//	private boolean okPressed = false;
//
//	// Game
//	private JGammon jGammon;
//
//	OtherNewGameDialog(JGammon jGammon) {
//		super(jGammon.getFrame(), true);
//		this.jGammon = jGammon;
//		setTitle(msg.getString("newgame"));
//		try {
//			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//			jbInit();
//			getRootPane().setDefaultButton(confirmButton);
//			pack();
//			JGammon.centerFrame(this);
//		} catch (Exception exception) {
//			exception.printStackTrace();
//		}
//	}
//
//	private void jbInit() throws Exception {
//		ChangeListener changeListener = e -> enableDisable();
//
//		// Layout
//		mainPanel.setLayout(gridBagLayout);
//
//		// Local or Network
//		localPlayersRadioButton.setSelected(true);
//		networkJoinAsClient.setSelected(true);
//
//		localPlayersRadioButton.addChangeListener(changeListener);
//		localPlayerVsAiRadioButton.addChangeListener(changeListener);
//		localAiVsAiRadioButton.addChangeListener(changeListener);
//		networkPlayersRadioButton.addChangeListener(changeListener);
//		networkServerIsWhite.addChangeListener(changeListener);
//		networkServerIsBlue.addChangeListener(changeListener);
//		networkJoinAsClient.addChangeListener(changeListener);
//		networkJoinGnuBG.addChangeListener(changeListener);
//
//		localPlayersRadioButton.setText(msg.getString("local"));
//		localPlayerVsAiRadioButton.setText(msg.getString("vsAi"));
//		localAiVsAiRadioButton.setText(msg.getString("simulation"));
//		networkPlayersRadioButton.setText(msg.getString("network"));
//		networkServerIsWhite.setText(msg.getString("whiteServer"));
//		networkServerIsBlue.setText(msg.getString("blueServer"));
//		networkJoinAsClient.setText(msg.getString("connect"));
//		networkJoinGnuBG.setText(msg.getString("gnubg"));
//
//		localPlayersRadioButton.setEnabled(true);
//		networkJoinAsClient.setEnabled(true);
//
//		localOrNetworkGroup.add(localPlayersRadioButton);
//		localOrNetworkGroup.add(localPlayerVsAiRadioButton);
//		localOrNetworkGroup.add(localAiVsAiRadioButton);
//		localOrNetworkGroup.add(networkPlayersRadioButton);
//
//		networkOptionsGroup.add(networkServerIsWhite);
//		networkOptionsGroup.add(networkServerIsBlue);
//		networkOptionsGroup.add(networkJoinAsClient);
//		networkOptionsGroup.add(networkJoinGnuBG);
//
//		// White Player Name
//		whitePlayerNameLabel.setText(msg.getString("whiteName"));
//		whitePlayerNameField.setPreferredSize(new Dimension(110, 20));
//		whitePlayerNameField.setText("Anastasia");
//
//		// Blue Player Name
//		bluePlayerNameLabel.setText(msg.getString("blueName"));
//		bluePlayerNameField.setPreferredSize(new Dimension(110, 20));
//		bluePlayerNameField.setText("Basil");
//
//		// Server Name
//		networkServerNameLabel.setText(msg.getString("server"));
//		networkServerNameField.setPreferredSize(new Dimension(110, 20));
//
//		// Port Number
//		networkPortNumberLabel.setText(msg.getString("port"));
//		networkPortNumberField.setPreferredSize(new Dimension(110, 20));
//		networkPortNumberField.setText("1777");
//
//		// Network Player Name
//		networkPlayerNameLabel.setText(msg.getString("localName"));
//		networkPlayerNameField.setPreferredSize(new Dimension(110, 20));
//		networkPlayerNameField.setText(InetAddress.getLocalHost().getCanonicalHostName());
//
//		// Cancel
//		cancelButton.setText(msg.getString("cancel"));
//		cancelButton.addActionListener(e -> setVisible(false));
//
//		// Confirm
//		confirmButton.setText(msg.getString("OK"));
//		confirmButton.addActionListener(e -> {
//			okPressed = true;
//			setVisible(false);
//		});
//
//		// Load Board
//		loadBoardButton.setEnabled(false);
//		loadBoardButton.setText(msg.getString("loadBoard"));
//		loadBoardButton.addActionListener(e -> loadButtonChanged());
//		loadBoardFileLabel.setMaximumSize(new Dimension(150, 15));
//		loadBoardFileLabel.setPreferredSize(new Dimension(150, 15));
//
//		// Icons
//		localPlayersIconLabel.setIcon(localPlayersIcon);
//		networkServerIconLabel.setIcon(networkServerIcon);
//		networkClientIconLabel.setIcon(networkClientIcon);
//		loadBoardIconLabel.setIcon(loadBoardIcon);
//
//		// Local Icon
//		mainPanel.add(localPlayersIconLabel, new GridBagConstraints(0, 0, 1, 3,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(20, 20, 20, 20), 0, 0));
//
//		//// Local Players
//		mainPanel.add(localPlayersRadioButton, new GridBagConstraints(1, 0, 4, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(20, 0, 0, 0), 2, 0));
//
//		// AI
//		mainPanel.add(localPlayerVsAiRadioButton, new GridBagConstraints(1, 1, 4, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(0, 0, 0, 0), 2, 0));
//		mainPanel.add(localAiVsAiRadioButton, new GridBagConstraints(1, 2, 4, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(0, 0, 0, 0), 2, 0));
//
//		//////// White Player Name
//		mainPanel.add(whitePlayerNameLabel, new GridBagConstraints(2, 3, 2, 1,
//				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 7), 0, 0));
//		mainPanel.add(whitePlayerNameField, new GridBagConstraints(4, 3, 1, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 20), 0, 0));
//
//		//////// Blue Player Name
//		mainPanel.add(bluePlayerNameLabel, new GridBagConstraints(2, 4, 2, 1,
//				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 7), 0, 0));
//		mainPanel.add(bluePlayerNameField, new GridBagConstraints(4, 4, 1, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 20), 0, 0));
//
//		// Network Icons
//		mainPanel.add(networkServerIconLabel, new GridBagConstraints(0, 6, 1, 2,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 0), 0, 0));
//		mainPanel.add(networkClientIconLabel, new GridBagConstraints(0, 8, 1, 2,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 0), 0, 0));
//
//		//// Network Options
//		mainPanel.add(networkPlayersRadioButton, new GridBagConstraints(1, 5, 4, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
//				new Insets(20, 0, 0, 0), 0, 0));
//
//		//////// Network Sub Options
//		mainPanel.add(networkServerIsWhite, new GridBagConstraints(2, 6, 3, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
//				new Insets(0, 0, 0, 0), 0, 0));
//		mainPanel.add(networkServerIsBlue, new GridBagConstraints(2, 7, 3, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 0), 0, 0));
//		mainPanel.add(networkJoinAsClient, new GridBagConstraints(2, 8, 3, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
//				new Insets(0, 0, 0, 0), 0, 0));
//		mainPanel.add(networkJoinGnuBG, new GridBagConstraints(2, 9, 3, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(0, 0, 0, 0), 0, 0));
//
//		//////////// Server Name
//		mainPanel.add(networkServerNameLabel, new GridBagConstraints(2, 10, 2, 1,
//				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 7), 0, 0));
//		mainPanel.add(networkServerNameField, new GridBagConstraints(4, 10, 1, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 20), 0, 0));
//
//		//////////// Port Number
//		mainPanel.add(networkPortNumberLabel, new GridBagConstraints(2, 11, 2, 1,
//				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 7), 0, 0));
//		mainPanel.add(networkPortNumberField, new GridBagConstraints(4, 11, 1, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 20), 0, 0));
//
//		//////////// Network Player Name
//		mainPanel.add(networkPlayerNameLabel, new GridBagConstraints(2, 12, 2, 1,
//				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 7), 0, 0));
//		mainPanel.add(networkPlayerNameField, new GridBagConstraints(4, 12, 1, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 20), 0, 0));
//
//		// Component
//		mainPanel.add(component, new GridBagConstraints(1, 6, 1, 1,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(0, 0, 0, 0), 20, 0));
//
//		// Load Board
//		mainPanel.add(loadBoardIconLabel, new GridBagConstraints(0, 13, 1, 2,
//				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(0, 20, 0, 0), 0, 0));
//		mainPanel.add(loadBoardButton, new GridBagConstraints(1, 13, 2, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
//				new Insets(10, 0, 10, 0), 0, 0));
//		mainPanel.add(loadBoardFileLabel, new GridBagConstraints(3, 13, 1, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
//				new Insets(10, 10, 10, 0), 0, 20));
//
//		// Form Buttons
//		mainPanel.add(cancelButton, new GridBagConstraints(1, 15, 3, 1,
//				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
//				new Insets(0, 0, 19, 0), 0, 0));
//		mainPanel.add(confirmButton, new GridBagConstraints(4, 15, 1, 1,
//				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//				new Insets(0, 0, 20, 20), 0, 0));
//
//		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
//	}
//
//	private boolean assertLocalNames() {
//		if (whitePlayerNameField.getText().length() == 0) {
//			JOptionPane.showMessageDialog(this, msg.getString("errorName1"),
//					msg.getString("error"),
//					JOptionPane.ERROR_MESSAGE);
//		} else if (bluePlayerNameField.getText().length() == 0) {
//			JOptionPane.showMessageDialog(this, msg.getString("errorName2"),
//					msg.getString("error"),
//					JOptionPane.ERROR_MESSAGE);
//		} else {
//			return true;
//		}
//		return false;
//	}
//
//	Game showAndEval() {
//		GameConnection gc = null;
//
//		while (true) {
//			okPressed = false;
//			setVisible(true);
//
//			// waiting for answer. ...
//			if (!okPressed) {
//				return null;
//			}
//
//			try {
//				gc = null;
//				if (localPlayersRadioButton.isSelected()) {
//					/*
//					 * Local Players
//					 */
//					if (assertLocalNames()) {
//						Player player1 = new LocalPlayer(whitePlayerNameField.getText());
//						Player player2 = new LocalPlayer(bluePlayerNameField.getText());
//						Game game = new Game(null, player1, player2, jGammon);
//						if (loadBoardFile != null) {
//							game.setSnapshot(new BoardSnapshot(loadBoardFile));
//						}
//						return game;
//					}
//				} else if (localPlayerVsAiRadioButton.isSelected()) {
//					/*
//					 * Local Player vs AI
//					 */
//					if (assertLocalNames()) {
////						Player player1 = new LocalPlayer(whitePlayerNameField.getText());
////						Player player2 = new LocalPlayer(bluePlayerNameField.getText());
////						Game game = new Game(null, player1, player2, jGammon);
////						if (loadBoardFile != null) {
////							game.setSnapshot(new BoardSnapshot(loadBoardFile));
////						}
////						return game;
//					}
//				} else if (localAiVsAiRadioButton.isSelected()) {
//					/*
//					 * Local Simulation (AI vs. AI)
//					 */
//					if (assertLocalNames()) {
////						Player player1 = new LocalPlayer(whitePlayerNameField.getText());
////						Player player2 = new LocalPlayer(bluePlayerNameField.getText());
////						Game game = new Game(null, player1, player2, jGammon);
////						if (loadBoardFile != null) {
////							game.setSnapshot(new BoardSnapshot(loadBoardFile));
////						}
////						return game;
//					}
//				} else {
//					/*
//					 * Network Game
//					 */
//					if (networkPlayerNameField.getText().length() == 0) {
//						JOptionPane.showMessageDialog(this,
//								msg.getString("errorLocName"),
//								msg.getString("error"),
//								JOptionPane.ERROR_MESSAGE);
//					} else if (networkJoinGnuBG.isSelected()) {
//						Player player1 = new LocalPlayer(networkPlayerNameField.getText());
//						// Player player1 = new GnubgPlayer("localhost", 1779);
//						Player player2 = new GnubgPlayer(networkServerNameField.getText(),
//								Integer.parseInt(networkPortNumberField.getText()));
//						Game game = new Game(null, player1, player2, jGammon);
//						if (loadBoardFile != null) {
//							game.setSnapshot(new BoardSnapshot(loadBoardFile));
//						}
//						return game;
//					} else if (networkJoinAsClient.isSelected()) {
//						gc = new GameConnection(networkServerNameField.getText(),
//								Integer.parseInt(networkPortNumberField.getText()));
//						Player locPlayer = new LocalPlayer(networkPlayerNameField.getText());
//						Player remPlayer = new NetworkPlayer(gc);
//						Game game;
//						if (gc.receiveColorIsWhite()) {
//							game = new Game(gc, locPlayer, remPlayer, jGammon);
//						} else {
//							game = new Game(gc, remPlayer, locPlayer, jGammon);
//						}
//						BoardSnapshot snapshot = BoardSnapshot.readSnapshotLine(gc.getGameReader());
//						game.setSnapshot(snapshot);
//						return game;
//					} else { // server is checked
//						gc = new GameConnection(Integer.parseInt(networkPortNumberField.getText()));
//						Player locPlayer = new LocalPlayer(networkPlayerNameField.getText());
//						Player remPlayer = new NetworkPlayer(gc);
//						gc.sendColor(!networkServerIsWhite.isSelected());
//						Game game;
//						if (networkServerIsWhite.isSelected()) {
//							game = new Game(gc, locPlayer, remPlayer, jGammon);
//						} else {
//							game = new Game(gc, remPlayer, locPlayer, jGammon);
//						}
//						if (loadBoardFile != null) {
//							BoardSnapshot snapshot = new BoardSnapshot(loadBoardFile);
//							game.setSnapshot(snapshot);
//							gc.getWriter().write(snapshot.toSendLine() + "\n");
//							gc.getWriter().flush();
//						} else {
//							gc.getWriter().write(BoardSnapshot.NEWGAME + "\n");
//							gc.getWriter().flush();
//						}
//						return game;
//					}
//				}
//			} catch (NumberFormatException ex) {
//				JOptionPane.showMessageDialog(this,
//						msg.getString("errorPort"),
//						msg.getString("error"),
//						JOptionPane.ERROR_MESSAGE);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				JOptionPane.showMessageDialog(this, ex,
//						msg.getString("error"),
//						JOptionPane.ERROR_MESSAGE);
//
//				if (gc != null) {
//					gc.close();
//				}
//			}
//		}
//	}
//
//	private void enableDisable() {
//		boolean isLocalGame = localPlayersRadioButton.isSelected() || localPlayerVsAiRadioButton.isSelected() || localAiVsAiRadioButton.isSelected();
//		boolean isNetworkGame = !isLocalGame;
//		boolean isJoiningAsClient = !isLocalGame && (networkJoinAsClient.isSelected() || networkJoinGnuBG.isSelected());
//
//		whitePlayerNameField.setEnabled(isLocalGame);
//		whitePlayerNameLabel.setEnabled(isLocalGame);
//		bluePlayerNameField.setEnabled(isLocalGame);
//		bluePlayerNameLabel.setEnabled(isLocalGame);
//
//		networkServerIsWhite.setEnabled(isNetworkGame);
//		networkServerIsBlue.setEnabled(isNetworkGame);
//		networkJoinAsClient.setEnabled(isNetworkGame);
//		networkJoinGnuBG.setEnabled(isNetworkGame);
//
//		networkServerNameLabel.setEnabled(isJoiningAsClient);
//		networkServerNameField.setEnabled(isJoiningAsClient);
//
//		networkPortNumberLabel.setEnabled(isNetworkGame);
//		networkPortNumberField.setEnabled(isNetworkGame);
//
//		networkPlayerNameLabel.setEnabled(isNetworkGame);
//		networkPlayerNameField.setEnabled(isNetworkGame);
//
//		loadBoardButton.setEnabled(!isJoiningAsClient || networkJoinGnuBG.isSelected());
//		loadBoardFileLabel.setEnabled(!isJoiningAsClient || networkJoinGnuBG.isSelected());
//	}
//
//	/**
//	 * feed infos from command line
//	 */
//	void feed(String gameMode, String portNumber, String serverName, String whitePlayerName, String bluePlayerName, String loadBoardFile) {
//		switch (gameMode) {
//			case "local":
//				localPlayersRadioButton.setSelected(true);
//				break;
//
//			case "client":
//				networkPlayersRadioButton.setSelected(true);
//				networkJoinAsClient.setSelected(true);
//				break;
//
//			case "serverWhite":
//				networkPlayersRadioButton.setSelected(true);
//				networkServerIsWhite.setSelected(true);
//				break;
//
//			case "serverBlue":
//				networkPlayersRadioButton.setSelected(true);
//				networkServerIsBlue.setSelected(true);
//				break;
//
//			case "gnubg":
//				networkPlayersRadioButton.setSelected(true);
//				networkJoinGnuBG.setSelected(true);
//				break;
//
//			default:
//				throw new RuntimeException("unsupported mode: " + gameMode);
//		}
//
//		if (serverName != null)
//			networkServerNameField.setText(serverName);
//
//		if (portNumber != null)
//			networkPortNumberField.setText(portNumber);
//
//		if (whitePlayerName != null) {
//			whitePlayerNameField.setText(whitePlayerName);
//			networkPlayerNameField.setText(whitePlayerName);
//		}
//
//		if (bluePlayerName != null)
//			bluePlayerNameField.setText(bluePlayerName);
//
//		if (loadBoardFile != null) {
//			this.loadBoardFile = new File(loadBoardFile);
//			loadBoardFileLabel.setText(this.loadBoardFile.getName());
//			loadBoardButton.setSelected(true);
//		}
//	}
//
//	private void loadButtonChanged() {
//		if (loadBoardButton.isSelected()) {
//			JFileChooser fc = new JFileChooser();
//			fc.addChoosableFileFilter(boardFileFilter);
//			fc.setAccessory(new BoardFileView(fc));
//			int result = fc.showOpenDialog(this);
//			if (result == JFileChooser.APPROVE_OPTION) {
//				loadBoardFile = fc.getSelectedFile();
//				loadBoardFileLabel.setText(fc.getSelectedFile().getName());
//			} else {
//				loadBoardFile = null;
//				loadBoardFileLabel.setText("");
//				loadBoardButton.setSelected(false);
//			}
//		} else {
//			loadBoardFile = null;
//			loadBoardFileLabel.setText("");
//		}
//
//	}
//
//	private FileFilter boardFileFilter = new FileFilter() {
//		public boolean accept(File pathname) {
//			return (pathname.getName().toLowerCase().endsWith(".board")
//					|| pathname.isDirectory());
//		}
//
//		public String getDescription() {
//			return msg.getString("boardFilter");
//		}
//	};
//}