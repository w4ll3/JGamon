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

import jgam.util.IntList;

import java.util.Iterator;

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
 * @todo do not use PossibleMoves - this is total overkill!
 * write mayMove /can move on your own.
 */
public class GameBoard extends BoardSetup {
	public GameBoard() {
		dices = new Dices();
		doubleCube = new DoubleCube();
	}

	private int checkers1[] = new int[26];
	private int checkers2[] = new int[26];

	private int playerAtMove = 0;

	private PossibleMoves possibleMoves = null;

	public Dices dices;

	public DoubleCube doubleCube;


	/**
	 * get the stored dice.
	 *
	 * @return an array of length 2 and each element between 1 and 6
	 * or null
	 */
	public int[] getDice() {
		return dices.getAsArray();
	}

	/**
	 * return the dices object
	 *
	 * @return Dices
	 */
	public Dices getDicesObject() {
		return dices;
	}

	/**
	 * get the value of the doubling cube.
	 *
	 * @return 1, 2, 4, 8, 16, 32, 64
	 */
	public int getDoubleCube() {
		return doubleCube.getValue();
	}

	/**
	 * ret the underlying double cube object
	 *
	 * @return the double cube
	 */
	public DoubleCube getDoubleCubeObject() {
		return doubleCube;
	}

	/**
	 * get the number of the player who may make the next move.
	 *
	 * @return 1 for player1, 2 for player2, 0 if this is the initial setup
	 */
	public int getActivePlayer() {
		return playerAtMove;
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
		if (player == 1) {
			return checkers1[pointnumber];
		} else {
			return checkers2[pointnumber];
		}
	}

	/**
	 * may a player double the game value.
	 *
	 * @param playerno player to check
	 * @return true iff playerno may double when its his turn
	 */
	public boolean mayDouble(int playerno) {
		return doubleCube.mayDouble(playerno);
	}


	/**
	 * fix a move in the data. the remaining steps are updated, too.
	 *
	 * @param m a Move, not necessarily a SingleMove
	 */
	/*package*/ void performMove(Move m) {
		for (Iterator iter = m.getSingleMoves().iterator(); iter.hasNext(); ) {
			SingleMove sm = (SingleMove) iter.next();
			performSingleMove(sm);
			calcRemainingSteps(sm);
		}
	}

	/**
	 * calculate the steps that can still be made with the given dice and
	 * the given board.
	 * <p>
	 * (We assume that all is valid! So consume max if length not available)
	 */
	private void calcRemainingSteps(SingleMove sm) {
		IntList steps = dices.getSteps();
		if (steps.contains(sm.length()))
			dices.consume(sm.length());
		else
			dices.consume(steps.max());
		possibleMoves = new PossibleMoves(this, dices.getSteps());
	}

	/**
	 * fix a move in the data
	 *
	 * @param m a SingleMove
	 * @throws IllegalArgumentException if the move may not be done
	 */

	private void performSingleMove(SingleMove m) throws IllegalArgumentException {
		int playerno = m.player();
		int[] checkers = getCheckers(playerno);
		int[] othersCheckers = getCheckers(3 - playerno);

		if (!possibleMoves.mayMove(m)) {
			throw new IllegalArgumentException("Impossible move: " + m);
		}

		if (m.to() != 0 && othersCheckers[25 - m.to()] == 1) {
			m.setHit(true);
			othersCheckers[25 - m.to()] = 0;
			othersCheckers[25]++;
		}
		checkers[m.from()]--;
		checkers[m.to()]++;
	}


	/**
	 * return the checker array for a player
	 *
	 * @param player 1 or 2
	 * @return int[] the original array
	 */
	private int[] getCheckers(int player) {
		if (player == 1) {
			return checkers1;
		} else {
			return checkers2;
		}
	}

	/**
	 * set the point for a player to a specific value.
	 * <b>The correctness has to be guaranteed by caller!</a>
	 *
	 * @param player 1 or 2
	 * @param point  0 - 25
	 * @param value  0 - 15
	 */
    /*package*/ void setPoint(int player, int point, int value) {
		if (player == 1) {
			checkers1[point] = value;
		} else {
			checkers2[point] = value;
		}
	}

	/**
	 * whats this for again?
	 *
	 * @param player int
	 * @param setup  BoardSetup
	 * @return boolean
	 * <p>
	 * public boolean canMove(int player) {
	 * return false;
	 * }
	 * <p>
	 * /**
	 */
     /*package*/ void applySetup(BoardSetup setup) {
		checkers1 = setup.getBoardAsArray(1);
		checkers2 = setup.getBoardAsArray(2);
		playerAtMove = setup.getActivePlayer();
		doubleCube.setValue(setup.getDoubleCube(), setup.mayDouble(1) ? 1 : 2);
		int dice[] = setup.getDice();
		if (dice != null) {
			dices.set(dice);
			possibleMoves = new PossibleMoves(this);
		} else
			possibleMoves = null;

	}

	/**
	 * the the player whose turn it is.
	 *
	 * @param playerAtMove new value to be set for <code>playerAtMove</code>
	 */
    /*package*/ void setActivePlayer(int playerAtMove) {
		this.playerAtMove = playerAtMove;
	}

	/**
	 * Delegate method void roll() to dices : Dices
	 */
	public void setDice(int[] dice) {
		dices.set(dice);
		possibleMoves = new PossibleMoves(this);
	}

	/**
	 * can the player whose turn it is still move?
	 *
	 * @return boolean
	 */
	public boolean canMove() {
		if (possibleMoves == null)
			return false;
		return possibleMoves.canMove();
	}

	/**
	 * clear the set dices and information about moves
	 */
	public void clearDices() {
		dices.set(null);
	}

}
