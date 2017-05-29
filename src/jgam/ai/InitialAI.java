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
 * Very simple rules for an initial AI
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class InitialAI implements AI {
	public InitialAI() {
	}

	/**
	 * get a short description of this method.
	 *
	 * @return String
	 */
	public String getDescription() {
		return "Computer player - beginner";
	}

	/**
	 * get the name of this AI Method.
	 *
	 * @return String
	 */
	public String getName() {
		return "BeginnerAI";
	}

	public void init() {
	}

	public void dispose() {
	}

	/**
	 * given a board and a double offer, take or drop.
	 * <p>
	 * just always take ...
	 *
	 * @param boardSetup BoardSetup
	 * @return TAKE
	 */
	public int takeOrDrop(BoardSetup boardSetup) {
		return TAKE;
	}

	public SingleMove[] makeMoves(BoardSetup boardSetup) {

		double bestValue = Double.NEGATIVE_INFINITY;
		int bestIndex = -1;

		PossibleMoves pm = new PossibleMoves(boardSetup);
		List list = pm.getPossibleNextSetups();
		int index = 0;
		for (Iterator iter = list.iterator(); iter.hasNext(); index++) {
			BoardSetup setup = (BoardSetup) iter.next();
			double value = eval(setup);
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

	private double eval(BoardSetup setup) {
		int blots = 0;
		int blocks = 0;
		int player = setup.getPlayerAtMove();

		for (int i = 1; i <= 24; i++) {
			int p = setup.getPoint(player, i);
			if (p == 1) {
				blots++;
			} else if (p >= 2) {
				blocks++;
			}
		}

		int hisbar = setup.getBar(3 - player);
		double v = setup.calcPip(3 - player) - setup.calcPip(player);
		v += -blots * .2;
		v += blocks * .4;
		v += -setup.getMaxPoint(player);
		v += hisbar * .2;

		return v;
	}

	public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
		return ROLL;
	}
}
