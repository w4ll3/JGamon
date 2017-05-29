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
 * A Text-Message that will be printed in the history window.
 *
 * @author not attributable
 * @version 1.0
 */
interface HistoryMessage {

	/**
	 * get the owner of this. represented as a number.
	 *
	 * @return either 1 or two.
	 */
	int getPlayer();

	/**
	 * get a short string representation of this message.
	 * used for display.
	 *
	 * @return String
	 */
	String toShortString();

	/**
	 * get a longer string representation of this message.
	 * used for display.
	 *
	 * @return String
	 */
	String toLongString();

	public void applyTo(HistoryMessageReceiver hmr);

	interface HistoryMessageReceiver {
		public void receiveDoubleMessage(DoubleMessage doubleMessage);

		public void receiveMoveMessage(MoveMessage moveMessage);

		public void receiveGiveupMessage(GiveupMessage giveupMessage);
	}

}




