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
import jgam.util.IntList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A MultiMove is a move that is done with two or more checkers simultaeously.
 * <p>
 * You should better not embed this into a DoubleMove, though you could.
 *
 * @author Mattias Ulbrich
 */
public class MultiMove implements Move {

	// the underlying move that is done (may be a DoubleMove)
	Move basicmove;

	// number of checkers
	int count;

	public MultiMove(Move m, int count) {
		assert count > 0;
		this.count = count;
		this.basicmove = m;
	}

	/**
	 * build a MultiMove out of 2 parallel moves.
	 * Asserts that they start and end at the same point.
	 * <p>
	 * This construction is not simple.
	 * If one or both of them are alreay MultiMoves, the underlying basic moves
	 * are extracted, the counts are summed up.
	 * <p>
	 * the new basicMove is constructed from the simpleMoves of the two
	 * possible basicMoves. Always take the hitting ones!
	 *
	 * @param m1 first move
	 * @param m2 second move
	 */
	public MultiMove(Move m1, Move m2) {
		assert m1.from() == m2.from();
		assert m1.to() == m2.to();

		Move basic1 = (m1 instanceof MultiMove ? ((MultiMove) m1).basicmove : m1);
		Move basic2 = (m2 instanceof MultiMove ? ((MultiMove) m2).basicmove : m2);

		this.count = (m1 instanceof MultiMove ? ((MultiMove) m1).count : 1) +
				(m2 instanceof MultiMove ? ((MultiMove) m2).count : 1);

		basicmove = null;
		assert basic1.getSingleMovesCount() == basic2.getSingleMovesCount();
		Iterator i1 = basic1.getSingleMoves().iterator();
		Iterator i2 = basic2.getSingleMoves().iterator();
		while (i1.hasNext()) {
			SingleMove sm1 = (SingleMove) i1.next();
			SingleMove sm2 = (SingleMove) i2.next();
			if (basicmove == null) {
				basicmove = sm1.isHit() ? sm1 : sm2;
			} else {
				basicmove = new DoubleMove(basicmove, sm1.isHit() ? sm1 : sm2);
			}
		}
	}


	/**
	 * Compares this object with the specified object for order.
	 * sorting moves according to the number of hops
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this
	 * object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Object o) {
		return getSingleMovesCount() - ((Move) o).getSingleMovesCount();
	}

	/**
	 * represent as a string.
	 * <p>
	 * Basically it is "n/m (count)" such as "8/6 (2)".
	 * But if basicMove hits the other then it is "8/6* (2)".
	 * <p>
	 * Generally it takes the underlying move's toString and appends the count.
	 * <p>
	 * count == 1 is not displayed. count == 0 should actually not happen.
	 *
	 * @return string representation of this Move
	 */
	public String toString() {
		if (count > 1) {
			return basicmove.toString() + "(" + count + ")";
		} else {
			return basicmove.toString();
		}
	}

	/**
	 * @return integer between 1 and 25.
	 */
	public int from() {
		return basicmove.from();
	}

	/**
	 * return the single moves of bascimove. count times.
	 *
	 * @return List of Move-Objects
	 */
	public List getSingleMoves() {
		List l = basicmove.getSingleMoves();
		ArrayList ret = new ArrayList();
		for (int i = 0; i < count; i++) {
			ret.addAll(l);
		}
		return ret;
	}

	/**
	 * return the number of single moves in basicmove. count times.
	 *
	 * @return number of basic moves in this move
	 */
	public int getSingleMovesCount() {
		return count * basicmove.getSingleMovesCount();
	}

	/**
	 * @return Player to which this belongs
	 */
	public int player() {
		return basicmove.player();
	}

	/**
	 * @return integer between 0 and 24.
	 */
	public int to() {
		return basicmove.to();
	}

	public int[] getUsedSteps() {
		IntList l = new IntList();
		for (int i = 0; i < count; i++) {
			l.addAll(basicmove.getUsedSteps());
		}
		return l.toArray();
	}

	public int getCount() {
		return count;
	}
}
