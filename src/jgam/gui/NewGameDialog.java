/*
 * JGammon: A backgammon client written in Java
 * Copyright (C) 2005/06 Mattias Ulbrich
 *
 * JGammon includes: - playing over network
 *                   - plugin mechanism for graphical board implementations
 *                   - artificial intelligence player
 *                   - plugin mechanism for AI players
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package jgam.gui;

import jgam.FileBoardSetup;
import jgam.JGammon;
import jgam.ai.AI;
import jgam.ai.AIPlayer;
import jgam.board.BoardAnimation;
import jgam.game.*;
import jgam.net.GameConnection;
import jgam.net.JGammonConnection;
import jgam.net.JGammonNetPlayer;
import jgam.util.AsynchronousWaitingWindow;
import jgam.util.FormatException;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * This dialog is showed before starting a new game.
 * <p>
 * Some pictures are taken from: iconarchive.com
 *
 * @author Mattias Ulbrich
 */
public class NewGameDialog extends JDialog {

	private ResourceBundle msg = JGammon.getResources("jgam.msg.NewGameDialog");

	// Layout
	private JPanel mainPanel = new JPanel();
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private Component horizontalStrut = Box.createHorizontalStrut(8);

	// Local/Network RadioGroup
	private ButtonGroup localOrNetworkGroup = new ButtonGroup();
	private JRadioButton localPlayersRadioButton = new JRadioButton();
	private JRadioButton localPlayerVsAiRadioButton = new JRadioButton();
	private JRadioButton localAiVsAiRadioButton = new JRadioButton();
	private JRadioButton networkPlayersRadioButton = new JRadioButton();

	// Local Options
	private JLabel whitePlayerNameLabel = new JLabel();
	private JTextField whitePlayerNameField = new JTextField();
	private JLabel bluePlayerNameLabel = new JLabel();
	private JTextField bluePlayerNameField = new JTextField();

	// Local Player Name (AI)
	private JLabel localPlayerNameLabel = new JLabel();
	private JTextField localPlayerNameField = new JTextField();

	// Simulation Speed
	private JLabel simulationSpeedLabel = new JLabel();
	private JFormattedTextField simulationSpeedField = new JFormattedTextField(NumberFormat.getNumberInstance());

	// Simulation Count
	private JLabel simulationCountLabel = new JLabel();
	private JFormattedTextField simulationCountField = new JFormattedTextField(NumberFormat.getNumberInstance());

	// Network Options
	private ButtonGroup networkOptionsGroup = new ButtonGroup();
	private JRadioButton networkAsServer = new JRadioButton();
	private JRadioButton networkAsClient = new JRadioButton();

	// Server Name
	private JLabel networkServerNameLabel = new JLabel();
	private JTextField networkServerNameField = new JTextField();

	// Port Number
	private JLabel networkPortNumberLabel = new JLabel();
	private JTextField networkPortNumberField = new JTextField();

	// Network Player Name
	private JLabel networkPlayerNameLabel = new JLabel();
	private JTextField networkPlayerNameField = new JTextField();

	// Local Players Icon
	private JLabel localPlayersIconLabel = new JLabel();
	private ImageIcon localPlayersIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/local.gif"));

	// Network Icon
	private JLabel networkIconLabel = new JLabel();
	private ImageIcon networkIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/network.gif"));

	// AI Icon
	private JLabel localVsAiIconLabel = new JLabel();
	private ImageIcon aiIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/computer.gif"));

	// Load Board Icon
	private JLabel loadBoardIconLabel = new JLabel();
	private ImageIcon loadBoardIcon = new ImageIcon(NewGameDialog.class.getResource("/jgam/img/open.gif"));

	// Load Board Options
	private JToggleButton loadBoardButton = new JToggleButton();
	private JLabel loadBoardFileLabel = new JLabel();
	private File loadBoardFile = null;
	private JCheckBox storedID = new JCheckBox();
	private JCheckBox invertSnapshot = new JCheckBox();

	// Form Buttons
	private JButton cancelButton = new JButton();
	private JButton confirmButton = new JButton();
	private boolean okPressed = false;

	// Game
	private JGammon jGammon;

	private Game game;
	private Integer simulation;

	public Game getGame() {
		return game;
	}

	public Integer getSimulation() {
		return simulation;
	}

	private JGammonConnection gameConnection;

	public GameConnection getGameConnection() {
		return gameConnection;
	}

	public NewGameDialog(JGammon jGammon) {
		super(jGammon.getFrame(), true);
		this.jGammon = jGammon;
		setTitle(msg.getString("newgame"));
		try {
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			jbInit();
			getRootPane().setDefaultButton(confirmButton);
			pack();
			setLocationRelativeTo(jGammon.getFrame());
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		ChangeListener changeListener = e -> enableDisable();

		mainPanel.setLayout(gridBagLayout);

		// Local/Network
		localPlayersRadioButton.addChangeListener(changeListener);
		localPlayerVsAiRadioButton.addChangeListener(changeListener);
		localAiVsAiRadioButton.addChangeListener(changeListener);
		networkPlayersRadioButton.addChangeListener(changeListener);
		networkAsServer.addChangeListener(changeListener);
		networkAsClient.addChangeListener(changeListener);

		localPlayersRadioButton.setText(msg.getString("local"));
		localPlayerVsAiRadioButton.setText(msg.getString("gnubg"));
		localAiVsAiRadioButton.setText(msg.getString("simulation"));
		networkPlayersRadioButton.setText(msg.getString("network"));
		networkAsServer.setText(msg.getString("startServer"));
		networkAsClient.setText(msg.getString("connect"));

		localAiVsAiRadioButton.setSelected(true);
		localAiVsAiRadioButton.setEnabled(true);
		networkAsClient.setSelected(true);
		networkAsClient.setEnabled(true);

		localOrNetworkGroup.add(localPlayersRadioButton);
		localOrNetworkGroup.add(localPlayerVsAiRadioButton);
		localOrNetworkGroup.add(localAiVsAiRadioButton);
		localOrNetworkGroup.add(networkPlayersRadioButton);

		networkOptionsGroup.add(networkAsServer);
		networkOptionsGroup.add(networkAsClient);

		// White Player Name
		whitePlayerNameLabel.setText(msg.getString("locname1"));
		whitePlayerNameField.setPreferredSize(new Dimension(110, 20));
		whitePlayerNameField.setText("Antonetta");

		// Blue Player Name
		bluePlayerNameLabel.setText(msg.getString("locname2"));
		bluePlayerNameField.setPreferredSize(new Dimension(110, 20));
		bluePlayerNameField.setText("Beatrice");

		// Local Player Name (AI)
		localPlayerNameLabel.setText(msg.getString("localName"));
		localPlayerNameField.setText(InetAddress.getLocalHost().getCanonicalHostName());

		// Simulation Count
		simulationCountLabel.setText(msg.getString("simulationCount"));
		simulationCountField.setText("15");

		// Simulation Speed
		simulationSpeedLabel.setText(msg.getString("simulationSpeed"));
		simulationSpeedField.setText("5");

		// Network Server Name
		networkServerNameLabel.setText(msg.getString("server"));
		networkServerNameField.setPreferredSize(new Dimension(110, 20));

		// Network Port Number
		networkPortNumberLabel.setText(msg.getString("port"));
		networkPortNumberField.setPreferredSize(new Dimension(110, 20));
		networkPortNumberField.setText("1777");

		// Network Player Name
		networkPlayerNameLabel.setText(msg.getString("localName"));
		networkPlayerNameField.setPreferredSize(new Dimension(110, 20));
		networkPlayerNameField.setText(InetAddress.getLocalHost().getCanonicalHostName());

		// Cancel
		cancelButton.setText(msg.getString("cancel"));
		cancelButton.addActionListener(e -> setVisible(false));
		// Confirm
		confirmButton.setText(msg.getString("OK"));
		confirmButton.addActionListener(e -> {
			okPressed = true;
			setVisible(false);
		});

		// Load Board
		loadBoardButton.setEnabled(false);
		loadBoardButton.setText(msg.getString("loadBoard"));
		loadBoardButton.addActionListener(e -> loadButtonChanged());
		loadBoardFileLabel.setMaximumSize(new Dimension(150, 15));
		loadBoardFileLabel.setPreferredSize(new Dimension(150, 15));

		// Checkboxes
		storedID.setEnabled(false);
		storedID.setText(msg.getString("useidentity"));
		invertSnapshot.setEnabled(false);
		invertSnapshot.setText(msg.getString("loadinvert"));

		// Icons
		localPlayersIconLabel.setIcon(localPlayersIcon);
		networkIconLabel.setIcon(networkIcon);
		localVsAiIconLabel.setIcon(aiIcon);
		loadBoardIconLabel.setIcon(loadBoardIcon);

		int yVar = 0;

		// Local Players
		mainPanel.add(localPlayersIconLabel, new GridBagConstraints(0, yVar, 1, 3, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 20, 20, 20), 0, 0));

		mainPanel.add(localPlayersRadioButton, new GridBagConstraints(1, yVar++, 4, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 2, 0));

		mainPanel.add(whitePlayerNameLabel, new GridBagConstraints(2, yVar, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(whitePlayerNameField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
		mainPanel.add(bluePlayerNameLabel, new GridBagConstraints(2, yVar, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(bluePlayerNameField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		// Local Player vs AI
		mainPanel.add(localVsAiIconLabel, new GridBagConstraints(0, yVar, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));

		mainPanel.add(localPlayerVsAiRadioButton, new GridBagConstraints(1, yVar++, 4, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 0, 0));

		mainPanel.add(localPlayerNameLabel, new GridBagConstraints(3, yVar, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(localPlayerNameField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		mainPanel.add(localAiVsAiRadioButton, new GridBagConstraints(1, yVar++, 4, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 0, 0));

		mainPanel.add(simulationCountLabel, new GridBagConstraints(3, yVar, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(simulationCountField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		mainPanel.add(simulationSpeedLabel, new GridBagConstraints(3, yVar, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(simulationSpeedField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		// Network Players
		mainPanel.add(networkIconLabel, new GridBagConstraints(0, yVar, 1, 4, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));

		mainPanel.add(horizontalStrut, new GridBagConstraints(1, yVar, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 20, 0));

		mainPanel.add(networkPlayersRadioButton, new GridBagConstraints(1, yVar++, 4, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
		mainPanel.add(networkAsServer, new GridBagConstraints(2, yVar++, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		mainPanel.add(networkAsClient, new GridBagConstraints(2, yVar++, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		mainPanel.add(networkServerNameLabel, new GridBagConstraints(2, yVar, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(networkServerNameField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		mainPanel.add(networkPortNumberLabel, new GridBagConstraints(2, yVar, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(networkPortNumberField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		mainPanel.add(networkPlayerNameLabel, new GridBagConstraints(2, yVar, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
		mainPanel.add(networkPlayerNameField, new GridBagConstraints(4, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));

		mainPanel.add(storedID, new GridBagConstraints(3, yVar++, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 20), 0, 0));

		// Load Board
		mainPanel.add(loadBoardIconLabel, new GridBagConstraints(0, yVar, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		mainPanel.add(loadBoardButton, new GridBagConstraints(1, yVar, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
		mainPanel.add(loadBoardFileLabel, new GridBagConstraints(3, yVar++, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));

		mainPanel.add(invertSnapshot, new GridBagConstraints(2, yVar++, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));

		// Form Buttons
		mainPanel.add(cancelButton, new GridBagConstraints(1, yVar, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 19, 0), 0, 0));
		mainPanel.add(confirmButton, new GridBagConstraints(4, yVar, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 20, 20), 0, 0));

		this.getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
	}

	private void loadBoardIfSet() throws FormatException, IOException {
		if (loadBoardFile != null) {
			BoardSetup snapshot = new FileBoardSetup(loadBoardFile);
			if (invertSnapshot.isSelected()) {
				snapshot = new InvertedBoardSetup(snapshot);
			}
			game.setBoardSetup(snapshot);
		}
	}

	public boolean showAndEval() {
		gameConnection = null;
		game = null;
		simulation = null;
		BoardAnimation.unsetStepOverride();
		BoardAnimation.unsetSleepOverride();

		while (true) {
			okPressed = false;
			setVisible(true);

			// waiting for answer. ...
			if (!okPressed) {
				return false;
			}

			try {
				if (localPlayersRadioButton.isSelected()) {
					//
					// Local Player vs. Local Player
					//
					if (whitePlayerNameField.getText().length() == 0) {
						JOptionPane.showMessageDialog(this,
								msg.getString("errorName1"),
								msg.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} else if (bluePlayerNameField.getText().length() == 0) {
						JOptionPane.showMessageDialog(this,
								msg.getString("errorName2"),
								msg.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} else {
						Player player1 = new UIPlayer(whitePlayerNameField.getText(), jGammon);
						Player player2 = new UIPlayer(bluePlayerNameField.getText(), jGammon);
						game = new Game(new LocalDiceRoller(), player1, player2, jGammon);
						loadBoardIfSet();
						return true;
					}
				} else if (localPlayerVsAiRadioButton.isSelected()) {
					//
					// Local Player vs. AI
					//
					if (localPlayerNameField.getText().length() == 0) {
						JOptionPane.showMessageDialog(this,
								msg.getString("errorLocName"),
								msg.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} else {
						AI selectedAI = selectAI();
						if (selectedAI != null) {
							Player player1 = new UIPlayer(localPlayerNameField.getText(), jGammon);
							Player player2 = new AIPlayer(selectedAI);
							game = new Game(new LocalDiceRoller(), player1, player2, jGammon);
							loadBoardIfSet();
							return true;
						}
					}
				} else if (localAiVsAiRadioButton.isSelected()) {
					//
					// AI vs. AI (Simulation)
					//
					if (simulationCountField.getText() == null) {
						JOptionPane.showMessageDialog(this,
								"You must enter a number of simulations.",
								msg.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} else if (simulationSpeedField.getText() == null) {
						JOptionPane.showMessageDialog(this,
								"You must enter the simulation speed.",
								msg.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} else {
						int count = Integer.parseInt(simulationCountField.getText());
						int speed = Integer.parseInt(simulationSpeedField.getText());
						if (count < 1) {
							JOptionPane.showMessageDialog(this,
									"Invalid simulation count.",
									msg.getString("error"),
									JOptionPane.ERROR_MESSAGE);
						} else if (speed < 0 || speed > 100) {
							JOptionPane.showMessageDialog(this,
									"Invalid simulation speed.",
									msg.getString("error"),
									JOptionPane.ERROR_MESSAGE);
						} else {
							AI whiteAi = selectAI();
							AI blueAi = selectAI();
							if (whiteAi != null && blueAi != null) {
								Player player1 = new AIPlayer(whiteAi);
								Player player2 = new AIPlayer(blueAi);
								simulation = count;
								BoardAnimation.setStepOverride(speed);
								BoardAnimation.setSleepOverride(speed);
								game = new Game(new LocalDiceRoller(), player1, player2, jGammon);
								loadBoardIfSet();
								return true;
							}
						}
					}
				} else { // network game
					if (networkPlayerNameField.getText().length() == 0) {
						JOptionPane.showMessageDialog(this,
								msg.getString("errorLocName"),
								msg.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} else if (networkAsClient.isSelected()) {
						gameConnection = new JGammonConnection(networkServerNameField.getText(),
								Integer.parseInt(networkPortNumberField.getText()),
								networkPlayerNameField.getText());
						Player locPlayer = new UIPlayer(networkPlayerNameField.getText(), jGammon);
						Player remPlayer = new JGammonNetPlayer(gameConnection);
						game = new Game(gameConnection, remPlayer, locPlayer, jGammon);
						game.setBoardSetup(gameConnection.getBoardSetup());
						return true;
					} else { // server is checked
						BoardSetup snapshot = null;
						if (loadBoardFile != null) {
							snapshot = new FileBoardSetup(loadBoardFile);
							if (invertSnapshot.isSelected()) {
								snapshot = new InvertedBoardSetup(snapshot);
							}
						}

						AsynchronousWaitingWindow window = mkWaitingWindow();
						window.asyncShow();
						try {
							gameConnection = new JGammonConnection(Integer.parseInt(networkPortNumberField.getText()),
									networkPlayerNameField.getText(),
									snapshot, window);
						} finally {
							window.dispose();
						}
						Player locPlayer = new UIPlayer(networkPlayerNameField.getText(), jGammon);
						Player remPlayer = new JGammonNetPlayer(gameConnection);

						game = new Game(gameConnection, locPlayer, remPlayer, jGammon);
						game.setBoardSetup(snapshot == null ? BoardSnapshot.INITIAL_SETUP : snapshot);
						return true;
					}
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						msg.getString("errorPort"),
						msg.getString("error"),
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, ex,
						msg.getString("error"),
						JOptionPane.ERROR_MESSAGE);

				if (gameConnection != null) {
					gameConnection.close(ex.toString());
				}
			}

		}
	}

	/**
	 * Choose the AI to be used in a dialog
	 *
	 * @return AI
	 */
	private AI selectAI() {
		ArrayList ais = new ArrayList();
		ArrayList descs = new ArrayList();
		Iterator it = sun.misc.Service.providers(AI.class, JGammon.getExtClassLoader());
		while (it.hasNext()) {
			Object o = it.next();
			ais.add(o);
			descs.add(((AI) o).getDescription());
		}

		Object desc = JOptionPane.showInputDialog(this,
				"Please choose the AI implementation to play against",
				"Choose computer player",
				JOptionPane.QUESTION_MESSAGE, aiIcon, descs.toArray(), descs.get(0));

		if (desc == null) {
			return null;
		}

		return (AI) ais.get(descs.indexOf(desc));
	}

	private void enableDisable() {
		boolean local = localPlayersRadioButton.isSelected();
		boolean net = networkPlayersRadioButton.isSelected();
		boolean ai = localPlayerVsAiRadioButton.isSelected();
		boolean simulation = localAiVsAiRadioButton.isSelected();
		boolean client = net && networkAsClient.isSelected();

		whitePlayerNameField.setEnabled(local);
		whitePlayerNameLabel.setEnabled(local);
		bluePlayerNameField.setEnabled(local);
		bluePlayerNameLabel.setEnabled(local);

		networkAsClient.setEnabled(net);
		networkAsServer.setEnabled(net);

		networkServerNameField.setEnabled(client);
		networkServerNameLabel.setEnabled(client);

		networkPlayerNameField.setEnabled(net);
		networkPlayerNameLabel.setEnabled(net);
		networkPortNumberField.setEnabled(net);
		networkPortNumberLabel.setEnabled(net);

		localPlayerNameLabel.setEnabled(ai);
		localPlayerNameField.setEnabled(ai);

		simulationCountLabel.setEnabled(simulation);
		simulationCountField.setEnabled(simulation);
		simulationSpeedLabel.setEnabled(simulation);
		simulationSpeedField.setEnabled(simulation);

		loadBoardButton.setEnabled(!client);
		loadBoardFileLabel.setEnabled(!client);
	}

	/**
	 * feed infos from command line
	 */
	public void feed(String gameMode, String serverName, String portNumber, String whitePlayerName, String bluePlayerName, String loadBoardFile) {
		switch (gameMode) {
			case "local":
				localPlayersRadioButton.setSelected(true);
				break;
			case "ai":
				localPlayerVsAiRadioButton.setSelected(true);
				break;
			case "server":
				networkPlayersRadioButton.setSelected(true);
				networkAsServer.setSelected(true);
				break;
			case "client":
				networkPlayersRadioButton.setSelected(true);
				networkAsClient.setSelected(true);
				break;
			default:
				throw new RuntimeException("Unsupported Game Mode: " + gameMode);
		}

		if (serverName != null)
			networkServerNameField.setText(serverName);

		if (portNumber != null)
			networkPortNumberField.setText(portNumber);

		if (whitePlayerName != null) {
			whitePlayerNameField.setText(whitePlayerName);
			networkPlayerNameField.setText(whitePlayerName);
			localPlayerNameField.setText(whitePlayerName);
		}

		if (bluePlayerName != null)
			bluePlayerNameField.setText(bluePlayerName);

		if (loadBoardFile != null) {
			this.loadBoardFile = new File(loadBoardFile);
			loadBoardFileLabel.setText(this.loadBoardFile.getName());
			loadBoardButton.setSelected(true);
			invertSnapshot.setEnabled(true);
		}
	}

	private void loadButtonChanged() {
		if (loadBoardButton.isSelected()) {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(boardFileFilter);
			fc.setAccessory(new BoardFileView(fc));
			int result = fc.showOpenDialog(this);
			if (result == fc.APPROVE_OPTION) {
				loadBoardFile = fc.getSelectedFile();
				loadBoardFileLabel.setText(fc.getSelectedFile().getName());
				invertSnapshot.setEnabled(true);
			} else {
				loadBoardFile = null;
				loadBoardFileLabel.setText("");
				loadBoardButton.setSelected(false);
				invertSnapshot.setEnabled(false);
			}
		} else {
			loadBoardFile = null;
			loadBoardFileLabel.setText("");
			invertSnapshot.setEnabled(false);
		}

	}

	private FileFilter boardFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return (pathname.getName().toLowerCase().endsWith(".board")
					|| pathname.isDirectory());
		}

		public String getDescription() {
			return msg.getString("boardFilter");
		}
	};

	private AsynchronousWaitingWindow mkWaitingWindow() {
		return new AsynchronousWaitingWindow(JGammon.jgammon().getFrame(), msg.getString("openingServer"), msg.getString("serverListening"), msg.getString("abort"));
	}

}
