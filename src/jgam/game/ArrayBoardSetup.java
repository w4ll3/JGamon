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

import jgam.util.Base64;

import java.io.Serializable;


/**
 * A BoardSetup implementation that simply stores the data in ints and arrays.
 * It is used by a couple of derived classes.
 * <p>
 * Derived classes may alter the contents of these containers.
 * <p>
 * Do such changes only in synchronized blocks. So a thread-safe mode can be
 * established.
 *
 * @author Mattias Ulbrich
 */
public class ArrayBoardSetup extends BoardSetup implements Serializable {

	protected byte checkers1[] = new byte[26];
	protected byte checkers2[] = new byte[26];
	protected int activePlayer = 0;
	protected int doubleCube = 1;
	protected int doublePlayer = 0;
	protected int dice[] = null;

	protected ArrayBoardSetup() {
	}

	/**
	 * initialize this object from another BoardSetup
	 *
	 * @param bs BoardSetup to setup from
	 */
	protected ArrayBoardSetup(BoardSetup bs) {
		assert bs != null;
		synchronized (bs) {
			for (int i = 0; i < 26; i++) {
				checkers1[i] = (byte) bs.getPoint(1, i);
				checkers2[i] = (byte) bs.getPoint(2, i);
			}
			doubleCube = bs.getDoubleCube();
			dice = bs.getDiceCopy();

			activePlayer = bs.getActivePlayer();
			doublePlayer = bs.mayDouble(1) ? 1 : 2;
		}
	}

	/**
	 * get the dice.
	 *
	 * @return int[] an array of length 2 and each element between 1 and 6
	 * or null
	 */
	public int[] getDice() {
		return dice;
	}

	/**
	 * get the value of the doubling cube.
	 *
	 * @return 1, 2, 4, 8, 16, 32, 64
	 */
	public int getDoubleCube() {
		return doubleCube;
	}

	/**
	 * get the number of the player who may make the next move.
	 *
	 * @return 1 for player1, 2 for player2, 0 if this is the initial setup
	 */
	public int getActivePlayer() {
		return activePlayer;
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
		return player == 1 ? checkers1[pointnumber] : checkers2[pointnumber];
	}

	/**
	 * may a player double the game value.
	 *
	 * @param playerno player to check
	 * @return true iff playerno may double when its his turn
	 */
	public boolean mayDouble(int playerno) {
		return (doubleCube == 1 || playerno == doublePlayer) && doubleCube < 64;
	}


	////////////////////////////////////
	// Base64   coding
	////////////////////////////////////

	/**
	 * create a board from the base64 coded information about it.
	 *
	 * @param base64coded String
	 */
	public static BoardSetup decodeBase64(String base64coded) {
		byte[] data = Base64.decode(base64coded);
		ArrayBoardSetup ret = new ArrayBoardSetup();

		// 1. the board
		int player1Total = 0, player2Total = 0;
		for (int i = 0; i < 24; i++) {
			if (data[i] > 0) {
				ret.checkers1[i + 1] = data[i];
				player1Total += data[i];
			} else {
				ret.checkers2[24 - i] = (byte) (-data[i]);
				player2Total += -data[i];
			}
		}

		// 2. the bars
		ret.checkers1[25] = data[24];
		player1Total += data[24];
		ret.checkers2[25] = data[25];
		player2Total += data[25];

		// ==> the offs
		ret.checkers1[0] = (byte) (15 - player1Total);
		ret.checkers2[0] = (byte) (15 - player2Total);

		// 3. doubleCube
		ret.doubleCube = data[26];

		// 4. player at move
		ret.activePlayer = data[27];

		// 5. dice
		if (data[28] != -1) {
			ret.dice = new int[2];
			ret.dice[0] = (data[28] / 6) + 1;
			ret.dice[1] = (data[28] % 6) + 1;
		}

		return ret;
	}

	public static String encodeBase64(BoardSetup bs) {
		byte[] data = new byte[29];

		// 1. the board
		for (int i = 0; i < 24; i++) {
			data[i] = (byte) (bs.getPoint(1, i + 1) - bs.getPoint(2, 24 - i));
		}

		// 2. the bars
		data[24] = (byte) bs.getBar(1);
		data[25] = (byte) bs.getBar(2);

		// 3.+4. doubleCube and white's turn
		data[26] = (byte) bs.getDoubleCube();
		data[27] = (byte) bs.getActivePlayer();

		int[] dice = bs.getDice();
		if (dice == null) {
			data[28] = -1;
		} else {
			data[28] = (byte) ((dice[0] - 1) * 6 + dice[1] - 1);
		}

		return Base64.encode(data);
	}

	/**
	 * Creates and returns a copy of this object.
	 * <p>
	 * Copy checker-board, dices, cube and activeplayer info
	 *
	 * @return a clone of this instance.
	 */
	public Object clone() {

		ArrayBoardSetup ret = new ArrayBoardSetup();
		ret.checkers1 = (byte[]) checkers1.clone();
		ret.checkers2 = (byte[]) checkers2.clone();
		ret.dice = dice == null ? null : (int[]) dice.clone();
		ret.doubleCube = doubleCube;
		ret.activePlayer = activePlayer;
		ret.doublePlayer = doublePlayer;

		return ret;
	}


}
