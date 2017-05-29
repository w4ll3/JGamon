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
 * a player with functionality seen from the game's point of view.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public abstract class Player {

	/**
	 * construct a new player with a name.
	 *
	 * @param name player's name
	 */
	public Player(String name) throws Exception {
		this.name = name;
		this.number = -1;
		this.game = null;
	}

	private String name;

	private int number;

	protected Game game;

	/**
	 * set the game to which this player belongs.
	 *
	 * @param g      Game to belong to
	 * @param number no in the the game (1 or 2)
	 */
	public void setGame(Game g, int number) {
		this.game = g;
		this.number = number;
	}


	/**
	 * is this the current player or the other one?
	 *
	 * @return true iff this is the current player
	 */
	public boolean isCurrentPlayer() {
		return game != null && game.getCurrentPlayer() == this;
	}

	/**
	 * All data of the game is told the players via the inform method
	 *
	 * @param playerMessage a Message telling what to do next
	 */
	public abstract void inform(PlayerMessage playerMessage);

	/**
	 * get the name of this player.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * the number of this player. either 1 or 2.
	 *
	 * @return the number of this player. either 1 or 2.
	 */
	public int getNumber() {
		return this.number;
	}

	/**
	 * does this player use the local UI? If so, return false, else true.
	 *
	 * @return true iff player ueses local UI
	 */
	public abstract boolean isLocal();


	/**
	 * String representation: name and number
	 *
	 * @return String
	 */
	public String toString() {
		return getName() + "(" + getNumber() + ")";
	}

	/**
	 * called to "finish" the player so that it can close open connections
	 * to deallocate resources, unsubsribe from handlers etc.
	 */
	public void dispose() {
	}

	/**
	 * get the other Player in the game
	 *
	 * @return other player from game
	 */
	protected Player getOtherPlayer() {
		return game.getOtherPlayer(this);
	}
}
