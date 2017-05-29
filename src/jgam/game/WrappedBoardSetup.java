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
 * A BoardSetup which wraps another setup and delegates all calls to this
 * other setup
 *
 * @author Mattias Ulbrich
 */
public class WrappedBoardSetup extends BoardSetup {

	protected BoardSetup board;

	public WrappedBoardSetup(BoardSetup wrapped) {
		board = wrapped;
	}

	public int getPoint(int player, int pointnumber) {
		return board.getPoint(player, pointnumber);
	}

	public int[] getDice() {
		return board.getDice();
	}

	public int getActivePlayer() {
		return board.getActivePlayer();
	}

	public int getDoubleCube() {
		return board.getDoubleCube();
	}

	public boolean mayDouble(int playerno) {
		return board.mayDouble(playerno);
	}

}
