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
import jgam.game.SingleMove;

/**
 * The interface for all artificial intellegence players.
 */
public interface AI {

	/**
	 * initialize this instance. Is called before it is used to
	 * make decisions.
	 *
	 * @throws java.lang.Exception if sth goes wrong during init.
	 */
	void init() throws Exception;

	/**
	 * free all used resources.
	 */
	void dispose();

	/**
	 * get the name of this AI Method.
	 *
	 * @return String
	 */
	String getName();

	/**
	 * get a short description of this method.
	 *
	 * @return String
	 */
	String getDescription();

	int ROLL = 0;
	int DOUBLE = 1;
	int DROP = 0;
	int TAKE = 1;

	/**
	 * given a board make decide which moves to make.
	 * There may not be any dice values left after call to this function.
	 *
	 * @param boardSetup BoardSetup to evaluate
	 * @return SingleMove[] a complete set of moves.
	 * @throws CannotDecideException if the AI cannot decide which moves to make
	 */
	SingleMove[] makeMoves(BoardSetup boardSetup) throws CannotDecideException;

	/**
	 * given a board make decide whether to roll or to double.
	 *
	 * @param boardSetup BoardSetup
	 * @return either DOUBLE or ROLL
	 * @throws CannotDecideException if the AI cannot decide which moves to make
	 */
	int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException;

	/**
	 * given a board and a double offer, take or drop.
	 * Evalutate the player whose turn it is NOT!
	 *
	 * @param boardSetup BoardSetup
	 * @return either TAKE or DROP
	 * @throws CannotDecideException if the AI cannot decide which moves to make
	 */
	int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException;

}
