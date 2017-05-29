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
 * Messages that are sent between the game and the players.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class PlayerMessage {

	// Messages:
	public static final int ABNORMAL_ABORT = -1;
	public static final int UNDO_REQUEST = 2;
	public static final int UNDO = 3;
	public static final int DOUBLE = 4;
	public static final int DOUBLE_TAKEN = 5;
	public static final int DOUBLE_DROPPED = 6;
	public static final int ROLL = 7;
	public static final int GIVEUP_REQUEST = 8;
	public static final int GIVEUP_TAKEN = 9;
	public static final int GIVEUP_DROPPED = 10;
	public static final int MOVE = 11;
	public static final int MY_TURN = 12;
	public static final int DICES = 13;
	public static final int GAME_OVER = 100;
	public static final int START_GAME = 101;

	Player owner;
	int type;
	Object object;
	int value;


	public PlayerMessage(Player player, int type) {
		this(player, type, -1);
	}

	public PlayerMessage(Player player, int type, int value) {
		this.owner = player;
		this.type = type;
		this.value = value;
	}

	public PlayerMessage(Player player, int type, Object object) {
		this.owner = player;
		this.type = type;
		this.object = object;
	}


	public PlayerMessage(int type) {
		this.type = type;
	}

	/**
	 * get the type of the message
	 *
	 * @return number of the message type
	 */
	public int getMessage() {
		return type;
	}

	public String toString() {
		return "[PlayerMessage: type=" + type + "; Player=" + owner + "; param=" + object + "; value=" + value + "]";
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>object</code>.
	 *
	 * @return the value of <code>object</code>
	 */
	public Object getObject() {
		return this.object;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>owner</code>.
	 *
	 * @return the value of <code>owner</code>
	 */
	public Player getOwner() {
		return this.owner;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>value</code>.
	 *
	 * @return the value of <code>value</code>
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * use this <i>setter</i> method to set the value of the field <code>object</code>.
	 *
	 * @param object new value to be set for <code>object</code>
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * use this <i>setter</i> method to set the value of the field <code>value</code>.
	 *
	 * @param value new value to be set for <code>value</code>
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * set owner of this message
	 */
	public void setOwner(Player player) {
		this.owner = player;
	}
}
