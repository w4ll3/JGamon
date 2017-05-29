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


package jgam.board;

import java.awt.*;

/**
 * A PaintingBoardImplementation is an implementation used by BoardComponent
 * that does not use the standard paint-method used by BoardComponent but has
 * got an own method to do this.
 * <p>
 * If a BoardComponent has got a PaintingBoardImplementation installed, it will
 * call paintComponent on it rather than use the standard methods for
 * painting the board.
 */
public interface PaintingBoardImplementation {

	/**
	 * This method is called to paint the entire board.
	 * <p>
	 * The background (aside the regular area) has already been painted before.
	 * The context is correctly translated, so that (0,0) is the upper left
	 * corner of the board to be painted.
	 * <p>
	 * Painting functions of the underlying board may be called.
	 *
	 * @param g Graphics to paint with
	 */
	public void paint(Graphics g);

}

