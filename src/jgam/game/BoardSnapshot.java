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


/**
 * An immutable ArrayBoardSetup.
 * <p>
 * The content is fixed after the execution of the constructor
 *
 * @author Mattias Ulbrich
 */
final public class BoardSnapshot extends ArrayBoardSetup {

	public static final BoardSnapshot INITIAL_SETUP =
			new BoardSnapshot(System.getProperty("jgam.initialboard"));

	/**
	 * take a snapshot of a boardSetup.
	 *
	 * @param bs BoardSetup to be snapshot
	 */
	public BoardSnapshot(BoardSetup bs) {
		super(bs);
	}

	/**
	 * take a snapshot of a boardSetup.
	 *
	 * @param bs BoardSetup to be snapshot
	 */
	public static BoardSnapshot takeSnapshot(BoardSetup bs) {
		return new BoardSnapshot(bs);
	}

	private final static String hexchars = "0123456789abcdef";

	/**
	 * The initial board can also be set by a string argument
	 */
	private BoardSnapshot(String initial) {
		super();
		if (initial == null) {
			checkers1[24] = checkers2[24] = 2;
			checkers1[13] = checkers2[13] = 5;
			checkers1[8] = checkers2[8] = 3;
			checkers1[6] = checkers2[6] = 5;
		} else {
			initial = initial.toLowerCase();
			for (int i = 0; i < 26; i++) {
				checkers1[i] = checkers2[i] = (byte) hexchars.indexOf(initial.charAt(i));
			}
		}
	}
}
