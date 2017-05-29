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
import jgam.board.BoardMouseListener;
import jgam.game.*;
import jgam.util.ActionManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * <p>Title: JGam - Java Backgammon</p>
 * <p>
 * <p>Description: </p>
 * <p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class UIPlayer extends Player implements ActionListener {

	// message formatting
	private ResourceBundle msg = JGammon.getResources("jgam.msg.Player");
	private MessageFormat msgFormat = new MessageFormat("");

	// Action Manager
	private ActionManager actionManager;

	// provides access to most of th UI
	private JGammon jgam;

	/**
	 * construct a new UIPlayer.
	 *
	 * @param name      Name of the playr
	 * @param jgam      underlying JGammon
	 * @param reporting iff this player is to output infos in the frame
	 */
	public UIPlayer(String name, JGammon jgam) throws Exception {
		super(name);
		this.jgam = jgam;

		actionManager = jgam.getFrame().getActionManager();
		actionManager.subscribeHandler("roll", this);
		actionManager.subscribeHandler("double", this);
		actionManager.subscribeHandler("undo", this);
		actionManager.subscribeHandler("giveup", this);
	}

	/**
	 * inform
	 *
	 * @param playerMessage PlayerMessage
	 */
	synchronized public void inform(final PlayerMessage playerMessage) {
//        System.err.println("called inform on " + this +" for " + playerMessage);

		switch (playerMessage.getMessage()) {

			// ---------------------------------
			case PlayerMessage.MY_TURN:
				if (playerMessage.getOwner() == this) {
					actionManager.enable("roll");
					if (game.getGameBoard().mayDouble(getNumber()))
						actionManager.enable("double");
				}
				break;

			// ---------------------------------
			case PlayerMessage.DOUBLE:
				if (playerMessage.getOwner() != this) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							acceptDouble();
						}
					});
					actionManager.disable("double");
				}
				break;

			// ---------------------------------
			case PlayerMessage.UNDO_REQUEST:
				if (isCurrentPlayer()) {
					game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.UNDO));
				}
				break;

			// ---------------------------------
			case PlayerMessage.GIVEUP_REQUEST:
				if (acceptGiveup(playerMessage.getValue())) {
					game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.GIVEUP_TAKEN, playerMessage.getValue()));
				} else {
					getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.GIVEUP_DROPPED));
				}

				break;

			// ---------------------------------
			case PlayerMessage.GIVEUP_DROPPED:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(jgam.getFrame(),
								msg.getString("giveupdropped"));
					}
				});
				break;

			// ---------------------------------
//        default:
//            System.err.println("  ... which is an unhandled message");
		}

	}

	private boolean acceptGiveup(int level) {

		// bugfix: first switch off input if at move
		((BoardMouseListener) jgam.jgammon().getBoard().getMouseListeners()[0]).drop();

		String[] winlevels = msg.getString("winlevels").split(", *");
		String mess = MessageFormat.format(msg.getString("confirmGiveup"),
				new Object[]{getOtherPlayer().getName(), winlevels[level - 1]});
		return JOptionPane.showConfirmDialog(jgam.getFrame(),
				mess,
				msg.getString("confirm"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) ==
				JOptionPane.YES_OPTION;

	}

	private void acceptDouble() {
		String M = msgFormat.format(msg.getString("confirmDouble"),
				new Object[]{
						getOtherPlayer().getName(),
						new Integer(2 * game.getGameBoard().getDoubleCube())});

		boolean answer = JOptionPane.showConfirmDialog(jgam.getFrame(),
				M,
				msg.getString("confirm"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				jgam.getBoard().getCheckerIcon(getNumber())) ==
				JOptionPane.YES_OPTION;

		if (answer) {
			game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.DOUBLE_TAKEN));
		} else {
			game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.DOUBLE_DROPPED));
		}
	}

	/**
	 * this player is local!
	 */
	public boolean isLocal() {
		return true;
	}

	/**
	 * waitingForMoves
	 *
	 * @return boolean
	 */
	public boolean isWaitingForUIMove() {
		return game.getCurrentPlayer() == this && game.getState() == game.STATE_AT_MOVE;
	}

	/**
	 * handleMove
	 *
	 * @param object Object
	 */
	synchronized public void handleMove(Move move) {
		PlayerMessage pm = new PlayerMessage(this, PlayerMessage.MOVE);
		pm.setObject(move);
		game.sendPlayerMessage(pm);
	}

	/**
	 * getPossibleMovesFrom returns a List with all Moves that are possible
	 * from point on given the current remaining hops.
	 *
	 * @param point point to start
	 * @return List of Move-Objects
	 */
	public Set getPossibleMovesFrom(int point) {
		PossibleMoves pm = new PossibleMoves(game.getGameBoard(),
				game.getGameBoard().getDicesObject().getSteps());

		return pm.getPossibleMovesFrom(point);
	}

	/**
	 * when clicking on a point twice do this move.
	 * <p>
	 * This is the longest one hop possible.
	 *
	 * @param point int
	 * @return Move
	 */
	public Move getDoubleClickMove(int point) {
		PossibleMoves pm = new PossibleMoves(game.getGameBoard(),
				game.getGameBoard().getDicesObject().getSteps());
		SingleMove longest = null;

		for (Iterator iter = pm.getPossibleMoveChains().iterator(); iter.hasNext(); ) {
			List chain = (List) iter.next();
			SingleMove m = (SingleMove) chain.get(0);
			if (m.from() == point && (longest == null || longest.to() > m.to())) {
				longest = m;
			}
		}

		return longest;
	}

	/**
	 * called to "finish" the player so that it can close open connections to
	 * deallocate resources, unsubsribe from handlers etc.
	 * <p>
	 * Here: unsubscribe from the buttons.
	 */
	public void dispose() {
		jgam.getFrame().getActionManager().removeHandler(this);
	}

	/**
	 * react to button presses in the frame
	 *
	 * @param e event that has been issued.
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("roll") && isCurrentPlayer()) {
			PlayerMessage pm = new PlayerMessage(this, PlayerMessage.ROLL);
			game.sendPlayerMessage(pm);
		} else if (e.getActionCommand().equals("double") && isCurrentPlayer()) {
			if (game.getGameBoard().mayDouble(getNumber())) {
				PlayerMessage pm = new PlayerMessage(this, PlayerMessage.DOUBLE);
				game.sendPlayerMessage(pm);
			} else {
				JOptionPane.showMessageDialog(jgam.getFrame(),
						msg.getString("doublenotallowed"));

			}
		} else if (e.getActionCommand().equals("undo") && game.getUndoPlayer() == this) {
			if (game.getState() == game.STATE_AT_MOVE) {
				PlayerMessage pm = new PlayerMessage(this, PlayerMessage.UNDO);
				game.sendPlayerMessage(pm);
			} else {
				getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.UNDO_REQUEST));
			}
		} else if (e.getActionCommand().equals("giveup") && (!getOtherPlayer().isLocal() || isCurrentPlayer())) {
			int level = showGiveUpWindow();
			if (level != 0) {
				getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.GIVEUP_REQUEST, level));
			}
		}
	}

	private int showGiveUpWindow() {
		String[] winlevels = msg.getString("winlevels").split(", *");
		String answer = (String)
				JOptionPane.showInputDialog(jgam.getFrame(),
						msg.getString("proposeGiveup"),
						msg.getString("choose"),
						JOptionPane.PLAIN_MESSAGE,
						jgam.getBoard().getCheckerIcon(getNumber()),
						winlevels, winlevels[0]);
		for (int i = 0; i < winlevels.length; i++) {
			if (winlevels[i].equals(answer)) {
				return i + 1;
			}
		}

		return 0;
	}


}
