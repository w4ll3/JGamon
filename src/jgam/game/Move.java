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

import java.util.List;

/**
 * A move of checkers on the board.
 * <p>
 * A general Moves object can consist of multiple moves that are related
 * in some way.
 * <p>
 * A Move moves <getCount()> checkers from the point <from()> to the point
 * <to()>.
 *
 * @author Mattias Ulbrich
 */
public interface Move extends Comparable {

	/**
	 * which dice steps have been used to make this/these move/s.
	 *
	 * @return int[] a (possibly empty, but non-null) array of values betw. 1 and 6.
	 */
	public int[] getUsedSteps();

	/**
	 * a move may be composed of several basic moves, this methods returns how
	 * many basic moves are composed
	 *
	 * @return number of basic moves in this move
	 */
	public int getSingleMovesCount();

	/**
	 * a move may be composed of several basic moves, this methods returns these
	 * basic moves.
	 *
	 * @return List of SingleMove-Objets
	 */
	public List getSingleMoves();

	/**
	 * get the owner of this move.
	 *
	 * @return Player to which this move belongs
	 */
	public int player();

	/**
	 * where the move starts.
	 *
	 * @return number of the point (or 25 for bar)
	 */
	public int from();

	/**
	 * where the moves ends.
	 *
	 * @return number of the point (or 0 for off)
	 */
	public int to();

	/**
	 * number of checkers involved in this move
	 *
	 * @return number of checkers moved in parallel
	 */
	public int getCount();

}
