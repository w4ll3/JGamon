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

import jgam.ai.AI;
import jgam.ai.InitialAI;
import jgam.game.BoardSetup;
import jgam.game.Move;
import jgam.game.SingleMove;
import jgam.game.WrappedBoardSetup;
import jgam.history.HistoryMessage.HistoryMessageReceiver;

import java.util.Iterator;
import java.util.List;

/**
 * Collect the statistics of the game.
 * <p>
 * To do this go over all moves that have been made.
 *
 * @author Mattias Ulbrich
 */
public class GameStatistics implements HistoryMessageReceiver {

	// the remaining checkers
	private int checkers[] = {15, 15};

	// the sum of eyes on the dices
	private int eyes[] = {0, 0};

	// how often have I hit the opponent
	private int hits[] = {0, 0};

	// how often did I throw a double
	private int doubles[] = {0, 0};

	// may I use the double cube
	private boolean maydouble[] = {true, true};

	// the AI used to value the postition
	private AI ai;

	private History history;

	/**
	 * new Game statistics.
	 *
	 * @param history History to get data from
	 */
	public GameStatistics(History history) {
		this.history = history;
		ai = new InitialAI();
		try {
			ai.init();
		} catch (Exception e) {
			System.err.println("No AI installable");
			e.printStackTrace();
		}
	}

	/**
	 * reset all data
	 */
	private void rewind() {
		eyes[0] = eyes[1] = 0;
		hits[0] = hits[1] = 0;
		doubles[0] = doubles[1] = 0;
		checkers[0] = checkers[1] = 15;
		maydouble[0] = maydouble[1] = true;
	}

	/**
	 * fetch the information that are valid after this move.
	 *
	 * @param index the index of the move
	 */
	public void goAfterIndex(int index) {
		rewind();
		List entries = history.getEntries();
		int cnt = 0;
		for (Iterator iter = entries.iterator(); iter.hasNext() && cnt <= index; cnt++) {
			HistoryMessage item = (HistoryMessage) iter.next();
			item.applyTo(this);
		}
	}

	/**
	 * don't do anything for doublings.
	 *
	 * @param doubleMessage DoubleMessage
	 */
	public void receiveDoubleMessage(DoubleMessage doubleMessage) {
		if (doubleMessage.getMode() == DoubleMessage.TAKE) {
			int taker = doubleMessage.getPlayer();
			maydouble[taker - 1] = true;
			maydouble[2 - taker] = false;
		}

	}

	/**
	 * don't do anything for give-ups.
	 *
	 * @param giveupMessage GiveupMessage
	 */
	public void receiveGiveupMessage(GiveupMessage giveupMessage) {
	}

	/**
	 * receiveMoveMessage: count doubles and hits.
	 *
	 * @param moveMessage MoveMessage
	 */
	public void receiveMoveMessage(MoveMessage moveMessage) {
		// -1 becaus of 1/2 vs. 0/1 array index
		int player = moveMessage.getPlayer() - 1;

		//
		// count hits and checkers
		for (Iterator iter = moveMessage.moves.iterator(); iter.hasNext(); ) {
			Move move = (Move) iter.next();
			for (Iterator singleIt = move.getSingleMoves().iterator(); singleIt
					.hasNext(); ) {
				SingleMove singleMove = (SingleMove) singleIt.next();
				if (singleMove.isHit()) {
					hits[player]++;
				}
				if (singleMove.to() == 0) {
					checkers[player]--;
				}
			}
		}

		//
		// count dice.
		int[] dice = moveMessage.getDice();
		if (dice[0] == dice[1]) {
			doubles[player]++;
			eyes[player] += 4 * dice[0];
		} else {
			eyes[player] += dice[0] + dice[1];
		}
	}

	/**
	 * get the number of doubles rolled by a player.
	 *
	 * @param player the player to look up
	 * @return the stored statistics
	 */
	public int getDoubles(int player) {
		return this.doubles[player - 1];
	}

	/**
	 * get the sum of dice pips rolled by a player.
	 *
	 * @param player the player to look up
	 * @return the stored statistics
	 */
	public int getPipsSoFar(int player) {
		return this.eyes[player - 1];
	}

	/**
	 * get the total number of hits that a player had so far.
	 *
	 * @param player the player to look up
	 * @return the stored statistics
	 */
	public int getHits(int player) {
		return this.hits[player - 1];
	}

	/**
	 * get the number of checkers that remain on the board.
	 *
	 * @param player the player to look up
	 * @return the stored statistics
	 */
	public int getCheckersOnBoard(int player) {
		return this.checkers[player - 1];
	}

	/**
	 * get whether a player may double or not
	 *
	 * @param player player to look up
	 * @return true if he may double
	 */
	public boolean mayDouble(int player) {
		return this.maydouble[player - 1];
	}

	/**
	 * get the evaluation by an AI for a setup.
	 *
	 * @param setup  Board to evaluate
	 * @param player player the player to look up
	 * @return the calculated statistics
	 */
	public double getAIEval(BoardSetup setup, int player) {
		// TODO activate AI stats when available
		try {
			if (setup.getPlayerAtMove() != player)
				setup = new OtherPlayerSetup(setup);

			// Not yet implemented
			return -1; // ai.propabilityToWin(setup);
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}

	/**
	 * use the race condition AI to estimate the number of dice throws to finish
	 * the game
	 *
	 * @param setup
	 * @param player
	 * @return
	 */
	public double getAIMoves(BoardSetup setup, int player) {
		// TODO activate AI stats when available
//        if (setup.isSeparated()) {
//            try {
//                return ((RoundEstimatingAI) ai.getAIForCategory(ai.RACE))
//                        .getEstimatedMoves(setup, player);
//            } catch (CannotDecideException e) {
//            }
//        }
		return -1;
	}

	private static class OtherPlayerSetup extends WrappedBoardSetup {
		OtherPlayerSetup(BoardSetup setup) {
			super(setup);
		}

		public int getActivePlayer() {
			return 3 - board.getActivePlayer();
		}
	}

	/**
	 * get the category in which a setup is for the CombiAI
	 *
	 * @param setup  setup to evaluate
	 * @param player
	 * @return NORMAL, BEAROFF or RACE
	 * @see jgam.ai.CombiAI
	 */
	public int getAICategory(BoardSetup setup, int player) {
		// TODO implement the AI print thing
//        return ai.getCategory(setup, player);
		return -1;
	}

}
