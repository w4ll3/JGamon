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

package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.SingleMove;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

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
public class GnuBgAI implements AI {

	private Socket socket;
	private BufferedReader reader;
	private Writer writer;

	public GnuBgAI() {
	}

	/**
	 * get a short description of this method.
	 *
	 * @return String
	 */
	public String getDescription() {
		return "GNU Backgammon on port 2000";
	}

	/**
	 * get the name of this AI Method.
	 *
	 * @return String
	 */
	public String getName() {
		return "GNU Backgammon";
	}

	/**
	 * initialize this instance.
	 *
	 * @throws Exception if sth goes wrong during init.
	 */
	public void init() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 2000);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new OutputStreamWriter(socket.getOutputStream());
	}

	/**
	 * given a board make decide which moves to make.
	 *
	 * @param boardSetup BoardSetup to evaluate
	 * @return SingleMove[] a complete set of moves.
	 */
	public synchronized SingleMove[] makeMoves(BoardSetup boardSetup) {
		try {
			int player = boardSetup.getPlayerAtMove();
			writer.write(makeBoard(boardSetup, false, true, player));
			//System.out.println(makeBoard(boardSetup, false, true, player));
			writer.write("\n");
			writer.flush();

			String line = reader.readLine();
			//System.out.println("line = -" + line+"-");
			if (line.equals("")) {
				return new SingleMove[0];
			}
			String[] strings = line.split(" ");
			SingleMove[] moves = new SingleMove[strings.length];
			for (int i = 0; i < moves.length; i++) {
				moves[i] = new SingleMove(player, strings[i]);
			}
			return moves;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * given a board make decide whether to roll or to double.
	 *
	 * @param boardSetup BoardSetup
	 * @return either DOUBLE or ROLL
	 */
	public synchronized int rollOrDouble(BoardSetup boardSetup) {
		try {
			int player = boardSetup.getPlayerAtMove();
			writer.write(makeBoard(boardSetup, false, false, player));
//            System.out.println(makeBoard(boardSetup, false, false,player));
			writer.write("\n");
			writer.flush();

			String line = reader.readLine();
//            System.out.println("line="+line);
			return line.equalsIgnoreCase("roll") ? ROLL : DOUBLE;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}

	/**
	 * given a board and a double offer, take or drop.
	 *
	 * @param boardSetup BoardSetup
	 * @return either TAKE or DROP
	 */
	public synchronized int takeOrDrop(BoardSetup boardSetup) {
		try {
			int player = boardSetup.getPlayerAtMove();
			writer.write(makeBoard(boardSetup, true, false, 3 - player));
			writer.write("\n");
			writer.flush();

			String line = reader.readLine();
			return line.equalsIgnoreCase("take") ? TAKE : DROP;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}

	/**
	 * make a FIBS String to send to gnubg. Not all of the fields are set
	 * correctly only the ones needed!
	 * <p>
	 * see end of this file for details from
	 * <p>
	 * Player 1 is "player1" plays o
	 * Player 2 is "player2" plays x
	 *
	 * @return String FIBS board-string
	 */
	private String makeBoard(BoardSetup setup, boolean doubleProposed, boolean dicePresent, int player) {
		StringBuffer ret = new StringBuffer("board");

		ret.append(":user:gnubg:0:0:0:");
		ret.append(-setup.getBar(2));
		ret.append(":");
		for (int i = 1; i < 25; i++) {
			ret.append(setup.getPoint(1, i) - setup.getPoint(2, 25 - i));
			ret.append(":");
		}
		ret.append(setup.getBar(1));
		ret.append(":");
		ret.append(player == 1 ? 1 : -1);
		ret.append(":");
		int[] dice = setup.getDice();
		if (!dicePresent || dice == null) {
			ret.append("0:0:0:0:");
		} else {
			String D = "" + dice[0] + ":" + dice[1] + ":";
			ret.append(D).append(D);
		}
		ret.append(setup.getDoubleCube()).append(":");
		ret.append(setup.mayDouble(1) ? "1:" : "0:");
		ret.append(setup.mayDouble(2) ? "1:" : "0:");
		ret.append(doubleProposed ? "1:" : "0:");
		ret.append("1:"); // color
		ret.append("-1:"); // direction
		ret.append("0:25:"); // home and bar
		ret.append(setup.getBar(1)).append(":");
		ret.append(setup.getBar(2)).append(":");
		ret.append("0:0:0:0:0:0");

		return ret.toString();
	}


	/**
	 * free the socket.
	 */
	public void dispose() {
		try {
			socket.close();
		} catch (IOException ex) {
		}
	}
}


/*
 The rawboard format

      This page describes the contents of the "board:....." messages that contain a board position when using boardstyle 3. This information is not necessary to use MacFIBS or play backgammon, it is provided simply as material for those who may be interested in this.

      The tokens are separated by colon ':' characters.
      Token           here           Description
      "board"         board          first token is always "board"
      name            player1        the player's name (either you, or if you are watching someone else, that person)
      name            player2        opponent's name
      match length    0              match length or 9999 for unlimited matches
      player got      0              player's points in the match so far
      opponent got    0              opponent's points in the match so far
      board                          26 numbers giving the board. Positions 0 and 25 represent the bars for the players (see below). Positive numbers represent O's pieces negative numbers represent X's pieces
      turn 	                     -1 if it's X's turn, +1 if it's O's turn 0 if the game is over
      dice 	                     2 numbers giving the player's dice. If it's the players turn and she or he hasn't rolled, yet both numbers are 0
      dice 	                     the opponent's dice (2 numbers)
      cube                           the number on the doubling cube
      may double                     1 if player is allowed to double, 0 otherwise
      may double                     the same for the opponent
      was doubled                    1 if your opponent has just doubled, 0 otherwise
      color                          -1 if you are X, +1 if you are O
      direction                      -1 if you play from position 24 to position 1 +1 if you play from position 1 to position 24
      home            0              0 or 25 depending on direction (obsolete but included anyway)
      bar             25             25 or 0 (see home)
      on home                        number of pieces already removed from the board by player
      on home                        same for opponent
      on bar                         number of player's pieces on the bar
      on bar                         same for opponent
      can move                       a number between 0 and 4. This is the number of pieces you can move. This token is valid if it's your turn and you have already rolled.
      forced move                    don't use this token
      did crawford                   don't use this token
      redoubles                      maximum number of instant redoubles in unlimited matches
 */
