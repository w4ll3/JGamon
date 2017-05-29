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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * An object of class SingleMove describes a single move in the backgammon game.
 * <p>
 * This is moving one checker by the value of one dice.
 *
 * @author Mattias Ulbrich
 */
public class SingleMove implements Move {

	private int from;
	private int to;
	private int player;

	// true if it hit!
	private boolean hit;

	public SingleMove(int player, int from, int to) {
		this.player = player;
		this.from = from;
		this.to = to;
	}

	public SingleMove(int player, String desc) {
		this(desc);
		this.player = player;
	}

	public SingleMove(String desc) throws IllegalArgumentException {
		try {
			Matcher m = Pattern.compile("([0-9]+)/([0-9]+)\\*?").matcher(desc);
			m.matches();
			from = Integer.parseInt(m.group(1));
			to = Integer.parseInt(m.group(2));
		} catch (Exception ex) {
			throw (IllegalArgumentException) (new IllegalArgumentException(desc +
					" does not describe a move")).initCause(ex);
		}
	}

	public String toString() {
		return "" + from + "/" + to + (hit ? "*" : "");
	}

	public int from() {
		return from;
	}

	public int to() {
		return to;
	}

	public void setHit(boolean b) {
		hit = b;
	}

	public boolean isHit() {
		return hit;
	}

	/**
	 * get the moves of which this move is compound
	 */
	public List getSingleMoves() {
		return Collections.singletonList(this);
	}

	/**
	 * get the owner of this move.
	 *
	 * @return Player to which this move belongs
	 */
	public int player() {
		return player;
	}

	public int length() {
		return from - to;
	}

	public int getSingleMovesCount() {
		return 1;
	}

	// sorting moves according to the number of hops
	public int compareTo(Object o) {
		return getSingleMovesCount() - ((Move) o).getSingleMovesCount();
	}

	public int[] getUsedSteps() {
		return new int[]{length()};
	}

	public int getCount() {
		return 1;
	}

	public boolean equals(Object o) {
		try {
			SingleMove m = (SingleMove) o;
			return m.from == from && m.to == to && m.player == player;
		} catch (ClassCastException ex) {
			return false;
		}
	}

}
