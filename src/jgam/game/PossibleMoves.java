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

import jgam.FileBoardSetup;
import jgam.util.FormatException;
import jgam.util.IntList;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * use an object of this class to keep book about the possible moves that
 * can still be made.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class PossibleMoves {

	BoardSetup setup;
	public List moveChains;
	IntList hops = new IntList();

	public PossibleMoves(BoardSetup setup) {
		this.setup = new BoardSnapshot(setup);
		int dices[] = setup.getDice();

		if (dices[0] == dices[1]) {
			hops.add(dices[0], 4);
		} else {
			hops.addAll(dices);
		}

		moveChains = allMoves(setup);
	}

	public PossibleMoves(BoardSetup setup, IntList hops) {
		this.hops = hops;
		this.setup = new BoardSnapshot(setup);

		// System.out.println("hops: "+hops);
		moveChains = allMoves(setup);
	}


	/**
	 * in this situation, is there still a move possible
	 *
	 * @return boolean
	 */
	public boolean canMove() {
		return !moveChains.isEmpty();
	}

	/**
	 * get all combinations of possible moves.
	 * <p>
	 * The resulting lists consists of Lists of SingleMoves.
	 * (List<List<SingleMove>>).
	 * The order in the inner list is of importance.
	 *
	 * @return a list of lists of SingleMoves
	 */
	public List getPossibleMoveChains() {
		return this.moveChains;
	}

	/**
	 * get a possible chain of SingleMoves.
	 *
	 * @param index index of the chain in the list returned by getPossibleMoveChains
	 * @return SingleMove[] the resulting moves in correct order.
	 */
	public SingleMove[] getMoveChain(int index) {
		List chain = (List) moveChains.get(index);
		return (SingleMove[]) chain.toArray(new SingleMove[0]);
	}

	/**
	 * @param point int
	 * @return List
	 */
	public Set getPossibleMovesFrom(int point) {
		Set ret = new HashSet();

		for (Iterator iter = moveChains.iterator(); iter.hasNext(); ) {
			List chain = (List) iter.next();
			Move m = (SingleMove) chain.get(0);
			if (m.from() == point) {
				int lookfor = m.to();
				ret.add(m);
				for (int i = 1; i < chain.size() && ((Move) chain.get(i)).from() == lookfor; i++) {
					m = new DoubleMove(m, (Move) chain.get(i));
					ret.add(m);
					lookfor = m.to();
				}
			}
		}

		return ret;
	}

	/**
	 * corresponding to the elements in getPossibleMoveChains()
	 * return a list of setups.
	 *
	 * @return List of BoardSetups
	 */
	public List getPossibleNextSetups() {
		ArrayList ret = new ArrayList();
		for (Iterator iter = moveChains.iterator(); iter.hasNext(); ) {
			ret.add(setupAfterMoves(setup, (List) iter.next()));
		}
		return ret;
	}

	private BoardSetup setupAfterMoves(BoardSetup setup, List list) {
		BoardSetup ret = setup;
		for (Iterator iter = list.iterator(); iter.hasNext(); ) {
			SingleMove move = (SingleMove) iter.next();
			ret = new SetupAfterMove(ret, move);
		}
		return ret;
	}

	/**
	 * may a move be done.
	 * - Is the player valid
	 * - Are the points valid.
	 *
	 * @param move Move to check
	 */
	public boolean mayMove(SingleMove move) {
		for (Iterator iter = moveChains.iterator(); iter.hasNext(); ) {
			List chain = (List) iter.next();
			if (chain.get(0).equals(move)) {
				return true;
			}
		}
		return false;
	}


	///////////////////////////////////////////
	// Calclulation functions
	///////////////////////////////////////////

	/*
	 * get all moves from a board given a number of hops left
	 */
	private List allMoves(BoardSetup setup) {
		IntList distVal = hops.distinctValues();
		int player = setup.getPlayerAtMove();
		List ret = new ArrayList();
		for (int i = 0; i < distVal.length(); i++) {
			int diceval = distVal.get(i);
			for (int from = diceval; from <= 25; from++) {
				if (movePossible(setup, from, from - diceval)) {
					ret.addAll(allMovesStartingWith(setup,
							makeMove(setup, from, from - diceval), diceval));
				}
			}
			int maxPoint = setup.getMaxPoint(player);
			if (diceval == distVal.max() && maxPoint < diceval && maxPoint > 0) {
				ret.addAll(allMovesStartingWith(setup,
						makeMove(setup, maxPoint, 0), diceval));
			}
		}
		return ret;
	}

	/*
	 * construct a move with the desired data. eventually tag it as hitting
	 */
	private SingleMove makeMove(BoardSetup setup, int from, int to) {
		SingleMove sm = new SingleMove(setup.getPlayerAtMove(), from, to);
		if (setup.getPoint(3 - setup.getPlayerAtMove(), 25 - to) == 1) {
			sm.setHit(true);
		}
		return sm;
	}

	/*
	 * return true if there is a normal (no max-playout) move from <from> to <to>
	 */
	private boolean movePossible(BoardSetup setup, int from, int to) {

		int player = setup.getPlayerAtMove();
		assert player == 1 || player == 2;

		// the length must be available in the hops
		if (!hops.contains(from - to)) {
			return false;
		}

		// there must be a checker on the the from point
		if (setup.getPoint(player, from) == 0) {
			return false;
		}

		// there may be at most one checker on the goal
		if (to > 0 && setup.getPoint(3 - player, 25 - to) > 1) {
			return false;
		}

		// if playing out: maxpoint must be <= 6
		if (to == 0 && setup.getMaxPoint(player) > 6) {
			return false;
		}

		// if there is one on the bar play it first
		if (setup.getPoint(player, 25) > 0 && from != 25) {
			return false;
		}

		return true;
	}


	private List allMovesStartingWith(BoardSetup setup, SingleMove m, int diceval) {
		hops.remove(diceval);
		setup = new SetupAfterMove(setup, m);
		List ret = allMoves(setup);
		for (Iterator iter = ret.iterator(); iter.hasNext(); ) {
			List item = (List) iter.next();
			item.add(0, m);
		}
		hops.add(diceval);

		// make one entry if empty
		if (ret.isEmpty()) {
			List l = new ArrayList();
			l.add(m);
			ret.add(l);
		}

		return ret;
	}


	public static void main(String[] args) throws IOException, FormatException {
		BoardSetup s = new FileBoardSetup(new File("deleteme.boad"));
		s.debugOut();
		PossibleMoves pm = new PossibleMoves(s);
		System.out.println("Resulting possible moves");
		System.out.println(pm.moveChains);
		System.out.println(pm.getPossibleMovesFrom(13));
	}
}