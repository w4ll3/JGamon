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

import javax.swing.*;
import java.util.Random;

/**
 * Use a pseudo random number generator to produce a pair of dice values.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class LocalDiceRoller implements DiceRoller {

	private Random random;

	public LocalDiceRoller() {
		random = new Random();
	}

	/**
	 * throw the dices.
	 * <p>
	 * Use a pseudo random number generator to produce a pair of dice values.
	 * if the property jgam.manualdice is set, then a message box will open up
	 * to enter the values.
	 *
	 * @param game the game to report to
	 */
	public void rollDices(Game game) {
		int dice[] = new int[2];

		if (Boolean.getBoolean("jgam.manualdice")) {
			dice[0] = Integer.parseInt(JOptionPane.showInputDialog(null,
					"Enter first dice value:",
					"2"));
			dice[1] = Integer.parseInt(JOptionPane.showInputDialog(null,
					"Enter 2nd dice value:",
					"2"));
		} else {
			dice[0] = (random.nextInt(6) + 1);
			dice[1] = (random.nextInt(6) + 1);
		}

		game.sendPlayerMessage(new PlayerMessage(null, PlayerMessage.DICES, dice));
	}
}
