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

import java.io.PrintWriter;

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
public abstract class BoardSetup {

	/**
	 * get the number of checkers on a specific point for a player. It may
	 * also be 0 for the off and 25 for the bar
	 *
	 * @param player      Player to check for (1 or 2)
	 * @param pointnumber Point to check (1-24 for points, 0 for off, 25 for bar)
	 * @return a value between 0 and 15
	 */
	public abstract int getPoint(int player, int pointnumber);

	/**
	 * get the dice of this setup
	 *
	 * @return int[] an array of length 2 and each element between 1 and 6 or
	 * null
	 */
	public abstract int[] getDice();

	/**
	 * get the number of the player who may make the next move or decision.
	 * If this is 0 it is the initial setup and the player to act can be
	 * deduced from the dice values. (the higher begins)
	 *
	 * @return 1 for player1, 2 for player2, 0 if this is the initial setup
	 */
	public abstract int getActivePlayer();


	/**
	 * get the value of the doubling cube.
	 *
	 * @return 1, 2, 4, 8, 16, 32, 64
	 */
	public abstract int getDoubleCube();

	/**
	 * may a player double the game value.
	 * <p>
	 * If this cube is set to 1, this must return true!
	 *
	 * @param playerno player to check
	 * @return true iff playerno may double when its his turn
	 */
	public abstract boolean mayDouble(int playerno);


	/**
	 * get the number of checkers already played off.
	 * shortcut for getPoint(playerno, 0);
	 *
	 * @param playerno Player to look at.
	 * @return the number of checkers in the off (0 - 15)
	 */
	public int getOff(int playerno) {
		return getPoint(playerno, 0);
	}

	/**
	 * get the number of checkers on the bar.
	 * shortcut for getPoint(playerno, 25);
	 *
	 * @param playerno Player to look at.
	 * @return the number of checkers on the bar (0 - 15)
	 */
	public int getBar(int playerno) {
		return getPoint(playerno, 25);
	}

	/**
	 * get a fresh copy of the dice value.
	 *
	 * @return a newly allocated array of the dice or null, if the dice are
	 * null themselves
	 */
	public int[] getDiceCopy() {
		int dice[] = getDice();
		if (dice == null) {
			return null;
		} else {
			return dice.clone();
		}
	}

	/**
	 * get a fresh copy of the board of one player.
	 *
	 * @param playerno player to look at
	 * @return an array of length 26.
	 */
	public int[] getBoardAsArray(int playerno) {
		int[] ret = new int[26];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = getPoint(playerno, i);
		}
		return ret;
	}

	/**
	 * get the number of the player who may make the next move or decision.
	 * <p>
	 * In case of the initial setup look at the dice!
	 *
	 * @return 1 for player1, 2 for player2, 0 if this is the initial setup
	 * and cannot be determined!
	 */
	public int getPlayerAtMove() {
		int player = getActivePlayer();
		int[] dice = getDice();
		if (player == 0 && dice != null) {
			return dice[0] > dice[1] ? 1 : 2;
		}
		return player;
	}

	/**
	 * get the point with the highest number that has at least one checker on
	 * it.
	 *
	 * @param player player to look up for (1 or 2)
	 * @return integer between 0 and 25 (incl)
	 */
	public int getMaxPoint(int player) {
		int maxpoint = -1;
		for (int i = 0; i <= 25; i++) {
			if (getPoint(player, i) > 0) {
				maxpoint = i;
			}
		}
		return maxpoint;
	}


	// Overwrite some Object methods

	/**
	 * Two BoardSetups are considered equal if they are the same:
	 * - checker-positions
	 * - dice
	 * - double dice
	 * <p>
	 * (The BASE64 are equal then --> alternatively)
	 *
	 * @param o can be true only for BoardSetupInterface
	 * @return true iff the fields are the same
	 */
	public boolean equals(Object o) {
		if (!(o instanceof BoardSetup)) {
			return false;
		}

		BoardSetup s = (BoardSetup) o;

		for (int i = 0; i <= 25; i++) {
			if (getPoint(1, i) != s.getPoint(1, i) || getPoint(2, i) != s.getPoint(2, i)) {
				return false;
			}
		}

		int[] d = getDice();
		int[] d2 = s.getDice();

		if (d == null) {
			if (d2 != null) {
				return false;
			}
		} else {
			if (d2 == null || d[0] != d2[0] || d[1] != d2[1]) {
				return false;
			}
		}

		if (getActivePlayer() != s.getActivePlayer()) {
			return false;
		}

		if (getDoubleCube() != s.getDoubleCube()) {
			return false;
		}

		if (mayDouble(1) != s.mayDouble(1) || mayDouble(2) != s.mayDouble(2)) {
			return false;
		}

		return true;

	}

	/**
	 * are the two players separated, i.e. can they no longer hit each other.
	 *
	 * @return true iff they cannot hit each other any more
	 */
	public boolean isSeparated() {
		return getMaxPoint(1) + getMaxPoint(2) < 25;
	}

	/**
	 * calculate the pip value for a player.
	 * <p>
	 * this value is defined as
	 * <pre>
	 *
	 *     SUM    i * getPoint(player, i)
	 *   i=1..25
	 *
	 * </pre>
	 *
	 * @param player player to calc for
	 * @return the pip value.
	 */
	public int calcPip(int player) {
		int pip = 0;
		for (int i = 1; i <= 25; i++) {
			pip += i * getPoint(player, i);
		}
		return pip;
	}

	/**
	 * Returns a hash code value for the object.
	 * <p>
	 * 8 points make an integer (4 bits each)
	 * 24*2 points make 6 integers which are xored
	 * <p>
	 * xor it with dice and cube infos
	 *
	 * @return a hash code value for this object.
	 */
	public int hashCode() {

		int[] dice = getDice();

		int hash = makeWord(1, 0) ^
				makeWord(1, 8) ^
				makeWord(1, 16) ^
				makeWord(2, 0) ^
				makeWord(2, 8) ^
				makeWord(2, 16) ^
				(getPoint(1, 24) << 24 |
						getPoint(1, 25) << 16 |
						getPoint(2, 24) << 8 |
						getPoint(2, 25)) ^
				(getDice() == null ? 0 : dice[0] << 3 + dice[1]) ^
				getDoubleCube() ^
				(mayDouble(1) ? 0xf0 : 0);

		return hash;
	}

	private int makeWord(int player, int frompoint) {
		int ret = 0;
		for (int i = 0; i < 8; i++) {
			ret = (ret << 4) | getPoint(player, frompoint + i);
		}
		return ret;
	}


	// Debug output support

	/**
	 * print a debug message describing this setup to a PrintWriter.
	 * This is multiple lines long.
	 *
	 * @param pw PrintWriter to print to
	 */
	synchronized public void debugOut(PrintWriter pw) {
		pw.println("BoardSetup-Object: " + this);
		for (int i = 13; i <= 24; i++) {
			if (getPoint(1, i) > 0) {
				pw.print("" + getPoint(1, i) + "o ");
			} else if (getPoint(2, 25 - i) > 0) {
				pw.print("" + getPoint(2, 25 - i) + "x ");
			} else {
				pw.print(" _ ");
			}

			if (i == 18) {
				pw.print("|| ");
			}
		}
		pw.println();
		pw.println();
		for (int i = 12; i >= 1; i--) {
			if (getPoint(1, i) > 0) {
				pw.print("" + getPoint(1, i) + "o ");
			} else if (getPoint(2, 25 - i) > 0) {
				pw.print("" + getPoint(2, 25 - i) + "x ");
			} else {
				pw.print(" _ ");
			}

			if (i == 7) {
				pw.print("|| ");
			}
		}
		pw.println();
		pw.println("o has home on lower right");
		pw.println("Bar:  o " + getBar(1) + "  x " + getBar(2));
		pw.println("Off:  o " + getOff(1) + "  x " + getOff(2));
		pw.println("Active player: " + getActivePlayer() + " (" + (getActivePlayer() == 1 ? "o)" : "x)"));
		pw.println("Doubling cube: " + getDoubleCube());
		pw.println("o may double: " + mayDouble(1) + ", x may double: " + mayDouble(2));
		pw.print("dice: ");
		int[] dice = getDice();
		if (getDice() == null) {
			pw.println("no dice");
		} else {
			pw.println(dice[0] + " " + dice[1]);
		}
		pw.flush();
	}

	/**
	 * print a debug message describing this object to System.out
	 */
	public void debugOut() {
		PrintWriter p = new PrintWriter(System.out);
		debugOut(p);
		p.flush();
	}

	public static void main(String[] args) {
		BoardSnapshot.INITIAL_SETUP.debugOut();
		System.out.println(BoardSnapshot.INITIAL_SETUP.hashCode());
	}


}
