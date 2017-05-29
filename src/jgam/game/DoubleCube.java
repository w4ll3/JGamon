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
 * <p>Title: JGam - Java Backgammon</p>
 * <p>
 * <p>Description: </p>
 * <p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class DoubleCube {

	private int value = 1;

	private int playerToDouble;

	public boolean mayDouble(int player) {
		return (value == 1 || playerToDouble == player) && value < 64;
	}

	/**
	 * get the value of the doubling process.
	 *
	 * @return 1, 2, 4, ..., 64
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * do a doubling of the cube!
	 *
	 * @param player player to double (1 or 2)
	 */
	public void doubling(int player) {
		assert mayDouble(player);
		value *= 2;
		playerToDouble = otherPlayer(player);
	}

	/**
	 * return the other player
	 *
	 * @param player 1 or 2
	 * @return 2 or 1
	 */
	private int otherPlayer(int player) {
		return 3 - player;
	}

	/**
	 * set the value and the player
	 *
	 * @param val          1,2,4,8,...,64
	 * @param doublePlayer irrel if val==1 else the player who may double
	 */
	public void setValue(int val, int doublePlayer) {
		value = val;
		playerToDouble = doublePlayer;
	}

}
