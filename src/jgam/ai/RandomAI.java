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

import java.util.List;
import java.util.Random;

/**
 * stupid random move maker for first tests.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class RandomAI implements AI {

	Random random;

	public RandomAI() {
	}

	/**
	 * get a short description of this method.
	 *
	 * @return String
	 */
	public String getDescription() {
		return "play random moves - w/o intelligence";
	}

	/**
	 * get the name of this AI Method.
	 *
	 * @return String
	 */
	public String getName() {
		return "Random AI";
	}

	/**
	 * initialize this instance.
	 */
	public void init() {
		random = new Random();
	}

	/**
	 * given a board make decide which moves to make.
	 *
	 * @param boardSetup BoardSetup to evaluate
	 * @return SingleMove[] a complete set of moves.
	 */
	public SingleMove[] makeMoves(BoardSetup boardSetup) {
		PossibleMoves pm = new PossibleMoves(boardSetup);
		List chains = pm.getPossibleMoveChains();
		if (chains.isEmpty()) {
			return new SingleMove[0];
		} else {
			int index = random.nextInt(chains.size());
			return pm.getMoveChain(index);
		}
	}

	/**
	 * given a board make decide whether to roll or to double.
	 *
	 * @param boardSetup BoardSetup
	 * @return either DOUBLE or ROLL
	 */
	public int rollOrDouble(BoardSetup boardSetup) {
		return random.nextDouble() < 0.05 ? DOUBLE : ROLL;
	}

	/**
	 * given a board and a double offer, take or drop.
	 *
	 * @param boardSetup BoardSetup
	 * @return either TAKE or DROP
	 * s
	 */
	public int takeOrDrop(BoardSetup boardSetup) {
		return random.nextDouble() < 0.1 ? DROP : TAKE;
	}

	public void dispose() {
	}
}
