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
 * wraps a board setup and delegates all method calls to the wrapped setup in
 * a way that the two players exchanged their places.
 * <p>
 * <p>This is used to load setups the way round or to paint with colors swapped.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */

/*
 * Note:   3-x  == (x==1 ? 2 : 1)  == otherPlayer(x)
 */
public class InvertedBoardSetup extends WrappedBoardSetup {

	public InvertedBoardSetup(BoardSetup bs) {
		super(bs);
	}

	/**
	 * get the number of the player who may make the next move.
	 *
	 * @return 1 for player1, 2 for player2, 0 if this is the initial setup
	 */
	public int getActivePlayer() {
		switch (board.getActivePlayer()) {
			case 0:
				return 0;
			case 1:
				return 2;
			case 2:
				return 1;
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * get the number of checkers on a specific point for a player.
	 *
	 * @param player      Player to check for (1 or 2)
	 * @param pointnumber Point to check (1-24 for points, 0 for off, 25 for
	 *                    bar)
	 * @return a value between 0 and 15
	 */
	public int getPoint(int player, int pointnumber) {
		return board.getPoint(3 - player, pointnumber);
	}

	/**
	 * may a player double the game value.
	 *
	 * @param playerno player to check
	 * @return true iff playerno may double when its his turn
	 */
	public boolean mayDouble(int playerno) {
		return board.mayDouble(3 - playerno);
	}
}
