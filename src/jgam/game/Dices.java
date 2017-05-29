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


package jgam.game;

import jgam.util.IntList;

/**
 * Dices encapsulate the dices in the backgammon game.
 * <p>
 * It contains the two values and all possible step-amounts.
 * These can be consumed.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class Dices {

	private int dice1 = -1;
	private int dice2 = -1;

	private IntList steps = new IntList();

	/**
	 * make a new Dices object with the two dice values.
	 * the steps list is also filled.
	 *
	 * @param dice1 between 1 and 6 (inclusive)
	 * @param dice2 between 1 and 6 (inclusive)
	 */
	public Dices() {
	}

	/**
	 * remove a step from the list of steps.
	 *
	 * @param value to be removed. must be contained in the step list.
	 */
	public void consume(int value) {
		assert steps.contains(value);
		steps.remove(value);
	}


	/**
	 * get the list of the steps that are still possible.
	 *
	 * @return list of possible steps.
	 */
	public IntList getSteps() {
		return (IntList) steps.clone();
	}

	/**
	 * set the dices.
	 *
	 * @param dices values to be set, each 1-6; or null
	 */
	public void set(int[] dices) {

		steps.clear();

		if (dices == null) {
			dice1 = dice2 = -1;
			return;
		}

		assert dices.length == 2;
		assert dices[0] <= 6 && dices[0] >= 1;
		assert dices[0] <= 6 && dices[1] >= 1;

		this.dice1 = dices[0];
		this.dice2 = dices[1];

		if (dice1 == dice2) {
			steps.add(dice1);
			steps.add(dice1);
			steps.add(dice1);
			steps.add(dice1);
		} else {
			steps.add(dice1);
			steps.add(dice2);
		}
	}

	/**
	 * getAsArray
	 *
	 * @return int[]
	 */
	public int[] getAsArray() {
		if (dice1 != -1) {
			return new int[]{dice1, dice2};
		} else {
			return null;
		}
	}
}
