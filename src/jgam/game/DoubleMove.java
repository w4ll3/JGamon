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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * A DoubleMove is a move that is composed of two Move-Objects m1 and m2.
 * <p>
 * But m1.to = m2.from must always be true!
 * <p>
 * Both m1 or m2 may be DoubleMove-Objects themselves.
 *
 * @author Mattias Ulbrich
 */
public class DoubleMove implements Move {

	private Move move1, move2;

	public DoubleMove(Move m1, Move m2) {
		if (m1.to() != m2.from()) {
			throw new IllegalArgumentException("m2 does not continue m2: " + m1 +
					" " + m2);
		}
		//assert m1.player()==m2.player();
		move1 = m1;
		move2 = m2;
	}

	public Move move1() {
		return move1;
	}

	public Move move2() {
		return move2;
	}

	/**
	 * get the SingleMoves of which this move is compound
	 */
	public List getSingleMoves() {
		List ret = new LinkedList(move1.getSingleMoves());
		ret.addAll(move2.getSingleMoves());
		return ret;
	}

	public int from() {
		return move1.from();
	}

	public int to() {
		return move2.to();
	}

	public int player() {
		return move1.player();
	}

	public String toString() {
		String ret = "" + move1.from();
		SingleMove item = null;
		for (Iterator iter = getSingleMoves().iterator(); iter.hasNext(); ) {
			item = (SingleMove) iter.next();
			if (item.isHit()) {
				ret += "/" + item.to() + "*";
			}
		}
		// the last under any circumstances
		if (!item.isHit()) {
			ret += "/" + item.to();
		}
		return ret;
	}

	public int length() {
		return from() - to();
	}

	public int getSingleMovesCount() {
		return move1.getSingleMovesCount() + move2.getSingleMovesCount();
	}

	// sorting moves according to the number of hops
	public int compareTo(Object o) {
		return getSingleMovesCount() - ((Move) o).getSingleMovesCount();
	}

	public int[] getUsedSteps() {
		IntList l = new IntList(move1.getUsedSteps());
		l.addAll(move2.getUsedSteps());
		return l.toArray();
	}

	public int getCount() {
		return move1.getCount() + move2.getCount();
	}

	public boolean equals(Object o) {
		try {
			DoubleMove m = (DoubleMove) o;
			return move1.equals(m.move1) && move2.equals(m.move2);
		} catch (ClassCastException ex) {
			return false;
		}

	}

}
