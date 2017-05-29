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

import jgam.game.ArrayBoardSetup;
import jgam.game.BoardSetup;
import jgam.game.SingleMove;

import java.util.Random;

class RolloutSetup extends ArrayBoardSetup {

	private AI ai;

	private static Random random = new Random();

	/**
	 * @param ai AI to be used during rollout
	 */
	public RolloutSetup(AI ai) {
		super();
		this.ai = ai;
	}

	void assignFrom(BoardSetup bs) {
		synchronized (bs) {
			for (int i = 0; i < 26; i++) {
				checkers1[i] = (byte) bs.getPoint(1, i);
				checkers2[i] = (byte) bs.getPoint(2, i);
			}
			doubleCube = bs.getDoubleCube();
			dice = bs.getDiceCopy();

			activePlayer = bs.getActivePlayer();
			doublePlayer = bs.mayDouble(1) ? 1 : 2;
		}
	}

	public int rollout() throws CannotDecideException {

		int initialplayer = getPlayerAtMove();

		// starting position without dice!
		while (initialplayer == 0) {
			throw new IllegalStateException(
					"cannot rollout position w/o player at move");
		}

		while (getMaxPoint(1) > 0 && getMaxPoint(2) > 0) {
			rolldice();
			SingleMove[] moves = ai.makeMoves(this);
			for (int i = 0; i < moves.length; i++) {
				performMove(moves[i]);
			}
			activePlayer = 3 - activePlayer;
		}

		if (getMaxPoint(initialplayer) == 0)
			return 1;
		else
			return 0;
	}

	/**
	 * make a move persistently. the values are not checked
	 *
	 * @param m SingleMove
	 */
	private void performMove(SingleMove m) {
		if (m.player() == 1) {
			checkers1[m.from()]--;
			checkers1[m.to()]++;
			if (m.to() != 0 && checkers2[25 - m.to()] == 1) {
				checkers2[25 - m.to()]--;
				checkers2[25]++;
			}
		} else {
			checkers2[m.from()]--;
			checkers2[m.to()]++;
			if (m.to() != 0 && checkers1[25 - m.to()] == 1) {
				checkers1[25 - m.to()]--;
				checkers1[25]++;
			}
		}
	}

	private void rolldice() {
		dice[0] = random.nextInt(6) + 1;
		dice[1] = random.nextInt(6) + 1;
	}

}
