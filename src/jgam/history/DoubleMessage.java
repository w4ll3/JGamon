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

package jgam.history;

import jgam.JGammon;

import java.util.ResourceBundle;

/**
 * This message is used to protocol doublings.
 * There are 3 modes: propose, take and drop.
 *
 * @author Mattias Ulbrich
 */

class DoubleMessage implements HistoryMessage {

	private static ResourceBundle msg = JGammon.getResources("jgam.msg.History");

	private int player;
	private int mode;
	private int doubleValue;

	final static int PROPOSE = 0;
	final static int TAKE = 1;
	final static int DROP = 2;

	/**
	 * create a double proposing message.
	 *
	 * @param player      player to double
	 * @param doubleValue value to double TO.
	 */
	public DoubleMessage(int player, int doubleValue) {
		this.player = player;
		mode = PROPOSE;
		this.doubleValue = doubleValue;
	}

	/**
	 * react to a double proposal.
	 *
	 * @param player player to react
	 * @param take   true if take, false if drop.
	 */
	public DoubleMessage(int player, boolean take) {
		this.player = player;
		mode = take ? TAKE : DROP;
	}

	/**
	 * get the mode of this Message
	 *
	 * @return PROPOSE, TAKE or DROP.
	 */
	public int getMode() {
		return mode;
	}


	/**
	 * get the number of the owning player
	 *
	 * @return either 1 or 2. depending on the owner
	 */
	public int getPlayer() {
		return player;
	}

	/**
	 * toLongString
	 * text representation
	 */
	public String toLongString() {
		switch (mode) {
			case PROPOSE:
				return msg.getString("DoubleMessage.doublesto") + doubleValue + "."; //$NON-NLS-1$
			case TAKE:
				return msg.getString("DoubleMessage.takes"); //$NON-NLS-1$
			case DROP:
				return msg.getString("DoubleMessage.drops"); //$NON-NLS-1$
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * same as toLongString
	 */
	public String toShortString() {
		return toLongString();
	}

	public void applyTo(HistoryMessage.HistoryMessageReceiver hmr) {
		hmr.receiveDoubleMessage(this);
	}
}
