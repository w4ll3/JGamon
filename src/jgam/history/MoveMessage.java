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

import jgam.game.DoubleMove;
import jgam.game.Move;
import jgam.game.SingleMove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A move history message contains:
 * - the dice values
 * - a set of moves.
 * <p>
 * It can be displayed as a string or send to a LoggingBoardSetup.
 */
class MoveMessage implements HistoryMessage {

	int player;
	int dice[];
	List moves = new ArrayList();

	public MoveMessage(int player, int dice[]) {
		assert dice.length == 2;
		this.dice = (int[]) dice.clone();
		this.player = player;
	}

	/**
	 * get the owner of this.
	 *
	 * @return 1 or 2
	 */
	public int getPlayer() {
		return player;
	}

	/**
	 * add a move to the list for this round.
	 *
	 * @param m Move to add
	 */
	public void add(Move m) {
		moves.add(m);
	}

	/**
	 * return:
	 * <dice1><dice2>: <move1.from>/<move2.to>{*} ... <moveN.from>/<moveN.to>{*}
	 * <p>
	 * All move are single hops.
	 */
	public String toLongString() {
		String ret = "" + dice[0] + dice[1] + ":";
		for (Iterator iter = moves.iterator(); iter.hasNext(); ) {
			Move item = (Move) iter.next();
			for (Iterator iter2 = item.getSingleMoves().iterator(); iter2.hasNext(); ) {
				SingleMove singleMove = (SingleMove) iter2.next();
				ret += " " + singleMove.toString();
			}
		}
		return ret;
	}

	/**
	 * return a compact description of the move.
	 * <p>
	 * This description can be shorter than the value returned by toLongString()
	 * It is more human readable and more compact.
	 *
	 * @return a short string describing the moves made in here
	 */
	public String toShortString() {
		List compactmoves = compactMoveList(moves);
		String ret = "" + dice[0] + dice[1] + ":";
		for (Iterator iter = compactmoves.iterator(); iter.hasNext(); ) {
			Move m = (Move) iter.next();
			ret += " " + m;
		}
		return ret;
	}


	/**
	 * compact a List of Moves.
	 * <p>
	 * Moves can be merged to DoubleMoves or MultiMoves.
	 * <p>
	 * First DoubleMoves are melted then it is checked whether there are
	 * MultiMoves.
	 * <p>
	 * It assumes there are no MultiMoves yet in it!
	 * <p>
	 * DoubleMoves are made by comparison of start and end location.
	 * MultiMoves are made ???
	 */
	public static List compactMoveList(List moves) {

		Move[] array = new Move[0];
		array = (Move[]) moves.toArray(array);
		try {
			for (int i = 0; i < array.length - 1; i++) {
				if (array[i] != null) {
					for (int j = i + 1; j < array.length; j++) {
						if (array[j] != null) {
							if (array[i].to() == array[j].from()) {
								array[i] = new DoubleMove(array[i], array[j]);
								array[j] = null;
							} else if (array[i].from() == array[j].to()) {
								array[i] = new DoubleMove(array[j], array[i]);
								array[j] = null;
							}
						}
					}
				}
			}

			for (int i = 0; i < array.length - 1; i++) {
				if (array[i] != null) {
					for (int j = i + 1; j < array.length; j++) {
						if (array[j] != null) {
							if ((array[i].from() == array[j].from()) &&
									(array[i].to() == array[j].to())) {
								array[i] = new MultiMove(array[j], array[i]);
								array[j] = null;
							}
						}
					}
				}
			}

			ArrayList ret = new ArrayList();
			for (int i = 0; i < array.length; i++) {
				if (array[i] != null) {
					ret.add(array[i]);
				}
			}
			return ret;
		} catch (RuntimeException ex) {
			throw ex;
		}

	}

	public void applyTo(jgam.history.HistoryMessage.HistoryMessageReceiver hmr) {
		hmr.receiveMoveMessage(this);
	}

	/**
	 * return the dice for these moves
	 *
	 * @return the dice
	 */
	public int[] getDice() {
		return dice;
	}

	/**
	 * clear the moves in this message.
	 * <p>
	 * This is done when an undo is issued.
	 */
	public void clear() {
		moves.clear();
	}
}

