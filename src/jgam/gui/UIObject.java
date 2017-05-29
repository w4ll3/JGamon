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

import jgam.JGammon;
import jgam.board.BoardAnimation;
import jgam.game.Game;
import jgam.game.Move;
import jgam.game.PlayerMessage;
import jgam.util.ActionManager;
import jgam.util.ExceptionDialog;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * This fills the gap between Game and the JGamFrame + UI.
 * <p>
 * It handles PlayerMessages and puts them into UI.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class UIObject {

	// message formatting
	private ResourceBundle msg = JGammon.getResources("jgam.msg.Player");

	private boolean switchToFront = Boolean.getBoolean("jgam.switchtofront");

	private JGammon jgam;
	private ActionManager actionManager;
	private Game game;


	public UIObject(JGammon jgam, Game game) {
		this.jgam = jgam;
		this.actionManager = jgam.getFrame().getActionManager();
		this.game = game;
	}

	public void inform(final PlayerMessage playerMessage) {

		switch (playerMessage.getMessage()) {

			case PlayerMessage.START_GAME:
				jgam.getBoard().useBoardSetup(game.getGameBoard());
				jgam.getFrame().setLabelPlayer(game.getCurrentPlayer());
				actionManager.disable("roll");
				actionManager.disable("double");
				if (switchToFront) {
					if (game.getCurrentPlayer().isLocal()) {
						jgam.getFrame().toFront();
						jgam.getFrame().setActiveTitle(true);
					} else {
						jgam.getFrame().setActiveTitle(false);
					}
				}
				break;

			// ---------------------------------
			case PlayerMessage.MY_TURN:
				if (switchToFront) {
					if (game.getCurrentPlayer().isLocal()) {
						jgam.getFrame().toFront();
						jgam.getFrame().setActiveTitle(true);
					} else {
						jgam.getFrame().setActiveTitle(false);
					}
				}
				jgam.getFrame().setLabelPlayer(playerMessage.getOwner());
				break;

			// ---------------------------------
			case PlayerMessage.ROLL:
				actionManager.disable("roll");
				actionManager.disable("double");
				break;

			// ---------------------------------
			case PlayerMessage.DICES:
				jgam.getBoard().useBoardSetup(game.getGameBoard());
				break;

			// ---------------------------------
			case PlayerMessage.MOVE:
				if (!playerMessage.getOwner().isLocal()) {
					Move m = (Move) playerMessage.getObject();
					BoardAnimation animation = new BoardAnimation(m.player(), m.from(), m.to());
					animation.animate(jgam.getBoard());
				}
				jgam.getBoard().useBoardSetup(game.getGameBoard());
				break;

			// ---------------------------------
			case PlayerMessage.GAME_OVER:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String m = null;
						int level = playerMessage.getValue();
						Integer amount = (Integer) playerMessage.getObject();

						switch (level) {
							case 1:
								m = msg.getString("wins1");
								break;
							case 2:
								m = msg.getString("wins2");
								break;
							case 3:
								m = msg.getString("wins3");
								break;
						}
						String playername = playerMessage.getOwner().getName();

						String message1 = MessageFormat.format(m, new Object[]{playername});
						String message2 = MessageFormat.format(msg.getString("worth"), new Object[]{amount});
						if (!game.isSimulation) {
							JOptionPane.showMessageDialog(jgam.getFrame(), message1 + " " + message2,
									msg.getString("gameover"), JOptionPane.INFORMATION_MESSAGE,
									jgam.getBoard().getCheckerIcon(playerMessage.getOwner().getNumber()));
						} else if (game.hasSimulationEnded()) {
							int totalGames = game.getSimulationCount();
							int playerOneWins = game.getSimulationWins(0);
							int playerTwoWins = game.getSimulationWins(1);
							String playerOnePercent = String.format("%5.2f", 100 * ((float) playerOneWins) / totalGames);
							String playerTwoPercent = String.format("%5.2f", 100 * ((float) playerTwoWins) / totalGames);

							String simulationMessage = "Simulation Count: " + totalGames + "\n";
							simulationMessage += "Player 1 Wins: " + playerOneWins + " (" + playerOnePercent + "%)\n";
							simulationMessage += "Player 2 Wins: " + playerTwoWins + " (" + playerTwoPercent + "%)\n";

							JOptionPane.showMessageDialog(jgam.getFrame(), simulationMessage,
									msg.getString("gameover"), JOptionPane.INFORMATION_MESSAGE,
									jgam.getBoard().getCheckerIcon(playerOneWins > playerTwoWins ? 1 : 2));
						}

						jgam.getFrame().setLabel(null);
						jgam.getFrame().setIcon(null);
						actionManager.disable("double");
						actionManager.disable("roll");
						actionManager.disable("giveup");
						actionManager.disable("undo");
					}
				});
				break;

			// ---------------------------------
			case PlayerMessage.DOUBLE:
				Object[] params = {game.getOtherPlayer(playerMessage.getOwner()).getName()};
				String m = MessageFormat.format(msg.getString("wait"), params);
				jgam.getFrame().setLabel(m);
				jgam.getFrame().setIcon(JGamFrame.smallClock);
				break;

			// ---------------------------------
			case PlayerMessage.DOUBLE_TAKEN:
				jgam.getFrame().setLabelPlayer(game.getCurrentPlayer());
				jgam.getBoard().useBoardSetup(game.getGameBoard());
				break;

			// ---------------------------------
			case PlayerMessage.UNDO:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(jgam.getFrame(),
								msg.getString("undone"));
						jgam.getBoard().useBoardSetup(game.getGameBoard());
						jgam.getFrame().setLabelPlayer(game.getCurrentPlayer());
					}
				});
				break;

			// ---------------------------------
			case PlayerMessage.ABNORMAL_ABORT:

				if (ExceptionDialog.show(jgam.getFrame(),
						msg.getString("errormsg"),
						(Exception) playerMessage.getObject(),
						msg.getString("save"))) {
					jgam.saveBoard();
				}
				break;

		}
	}
}
