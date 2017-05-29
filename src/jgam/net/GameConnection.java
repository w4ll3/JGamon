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


package jgam.net;

import jgam.game.DiceRoller;

/**
 * The Game class communicates with a remote partner via a GameConnection.
 * <p>
 * There are two implementations: JGammonConnection and FIBSConnection
 */
public interface GameConnection extends DiceRoller {

	/**
	 * close the connection and send a close message to the opposite site
	 */
	public void close(String message);

	/**
	 * get the name of the remote partner
	 */
	public String getRemoteName();

	/**
	 * is the undo mechanism allowed?
	 */
	public boolean supportsUndo();

	/**
	 * open chat window
	 */
	public void openChatWindow();

}
