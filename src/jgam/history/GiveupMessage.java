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

/**
 * This message is used to protocol giveups.
 * Only taken giveup requests are protocolled.
 *
 * @author Mattias Ulbrich
 */

class GiveupMessage implements HistoryMessage {

	private int player;
	private int mode;

	GiveupMessage(int player, int mode) {
		this.player = player;
		this.mode = mode;
	}


	/**
	 * get the owner of this.
	 *
	 * @return either 1 or two.
	 */
	public int getPlayer() {
		return player;
	}

	/**
	 * get a short string representation of this message.
	 *
	 * @return String
	 */
	public String toShortString() {
		return "Give-up (" + mode + ")";

	}

	/**
	 * get a longer string representation of this message.
	 *
	 * @return String
	 */
	public String toLongString() {
		switch (mode) {
			case 1:
				return "Give-up";
			case 2:
				return "Give-up Gammon";
			case 3:
				return "Give-up Backgammon";
		}
		throw new IllegalStateException();
	}

	/**
	 * applyTo
	 *
	 * @param hmr HistoryMessageReceiver
	 */
	public void applyTo(jgam.history.HistoryMessage.HistoryMessageReceiver hmr) {
		hmr.receiveGiveupMessage(this);
	}

}
