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

package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.Iterator;
import java.util.List;

/**
 * This AI generically works with position evaluation.
 * <p>
 * Evalution must return a value between 0 and 1. it is the probability to win
 * from this situation on.
 * <p>
 * Gammons/Doubles are ignored in this evaluation.
 * <p>
 * Doubles / takes are made if a threshold DOUBLE_THRESHOLD is reached (see
 * internet!).
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public abstract class EvaluatingAI implements AI {

	public static final double DOUBLE_THRESHOLD = .22;

	/**
	 * given a board make decide which moves to make.
	 *
	 * @param boardSetup BoardSetup to evaluate
	 * @return SingleMove[] a complete set of moves.
	 * @todo do not check setups twice!
	 * @todo ply 1 or 2
	 */
	public SingleMove[] makeMoves(BoardSetup boardSetup)
			throws CannotDecideException {
		double bestValue = -1;
		int bestIndex = -1;

		PossibleMoves pm = new PossibleMoves(boardSetup);
		List list = pm.getPossibleNextSetups();
		int index = 0;
		for (Iterator iter = list.iterator(); iter.hasNext(); index++) {
			BoardSetup setup = (BoardSetup) iter.next();
			double value = propabilityToWin(setup);
			if (value > bestValue) {
				bestValue = value;
				bestIndex = index;
			}
		}

		if (bestIndex == -1)
			return new SingleMove[0];
		else {
			// System.out.println("Evaluation for this move: "+bestValue);
			return pm.getMoveChain(bestIndex);
		}
	}

	/**
	 * evaluate a BoardSetup.
	 * <p>
	 * The result is an approximation of the probabiity to win.
	 *
	 * @param setup BoardSetup
	 * @return double between 0 and 1.
	 */
	public abstract double propabilityToWin(BoardSetup setup)
			throws CannotDecideException;

	/**
	 * given a board make decide whether to roll or to double.
	 * <p>
	 * Evaluate and decide whether to double or not.
	 *
	 * @param boardSetup BoardSetup
	 * @return either DOUBLE or ROLL
	 */
	public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
		if (boardSetup.mayDouble(boardSetup.getPlayerAtMove())) {
			double eval = propabilityToWin(boardSetup);
			if (eval >= 1 - DOUBLE_THRESHOLD)
				return DOUBLE;
		}
		return ROLL;
	}

	/**
	 * given a board and a double offer, take or drop.
	 *
	 * @param boardSetup BoardSetup
	 * @return either TAKE or DROP
	 */
	public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
		double eval = propabilityToWin(boardSetup);
		if (eval > 1 - DOUBLE_THRESHOLD)
			return DROP;
		else
			return TAKE;
	}

//    private static class OthersTurnSetup extends WrappedBoardSetup {
//        OthersTurnSetup(BoardSetup s) {
//            super(s);
//        }
//
//        public int getActivePlayer() {
//            int p = board.getActivePlayer();
//            return p == 0 ? 0 : 3 - p;
//        }
//    }
}
