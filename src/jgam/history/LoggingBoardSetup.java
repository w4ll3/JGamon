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


package jgam.history;

import jgam.game.*;

import java.util.Iterator;

/**
 * A BoardSetup that, given a startup setup, can perform moves upon it, to
 * replay up a game.
 *
 * @author Mattias Ulbrich
 */
public class LoggingBoardSetup extends ArrayBoardSetup implements HistoryMessage.HistoryMessageReceiver {

	public LoggingBoardSetup(BoardSetup initialSetup) {
		super(initialSetup);
	}

	public LoggingBoardSetup() {
		this(BoardSnapshot.INITIAL_SETUP);
	}

	public void setDice(int dice[]) {
		if (dice != null) {
			this.dice = (int[]) dice.clone();
		} else {
			this.dice = null;
		}
	}

	/**
	 * set the checkers for a player on a point.
	 * <p>
	 * Use this with care, may lead to unallowed setups!
	 *
	 * @param player player to work with, 1 or 2
	 * @param point  point to set, 0 - 25
	 * @param value  number of checkers, 0 - 15
	 */
	synchronized protected void setPoint(int player, int point, int value) {
		if (player == 1) {
			checkers1[point] = (byte) value;
		} else {
			checkers2[point] = (byte) value;
		}
	}

	/**
	 * setActivePlayer
	 *
	 * @param player int
	 */
	public void setActivePlayer(int player) {
		activePlayer = player;
	}


	/**
	 * include a move into the data.
	 * <p>
	 * synchronized because we alter ArrayBoardSetup's data
	 *
	 * @param m Move to be made
	 */
	synchronized public void performMove(Move m) {
		for (Iterator iter = m.getSingleMoves().iterator(); iter.hasNext(); ) {
			SingleMove singleMove = (SingleMove) iter.next();
			int playerno = singleMove.player();
			moveChecker(playerno, singleMove.from(), singleMove.to());
			if (singleMove.to() != 0 && getPoint(3 - playerno, 25 - singleMove.to()) == 1) {
				moveChecker(3 - playerno, 25 - singleMove.to(), 25);
			}
		}

	}

	private void moveChecker(int playerno, int from, int to) {
		int v1 = getPoint(playerno, from);
		int v2 = getPoint(playerno, to);
		setPoint(playerno, from, v1 - 1);
		setPoint(playerno, to, v2 + 1);
	}

	synchronized public void receiveDoubleMessage(DoubleMessage doubleMessage) {
		setDice(null);
		if (doubleMessage.getMode() == DoubleMessage.TAKE) {
			doubleCube *= 2;
		}
	}

	synchronized public void receiveMoveMessage(MoveMessage moveMessage) {
		setDice(moveMessage.getDice());
		activePlayer = moveMessage.getPlayer();
		for (Iterator iter = moveMessage.moves.iterator(); iter.hasNext(); ) {
			Move m = (Move) iter.next();
			performMove(m);
		}
	}

	public void receiveGiveupMessage(GiveupMessage giveupMessage) {
		setDice(null);
	}


}
