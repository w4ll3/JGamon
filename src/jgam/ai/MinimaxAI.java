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
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * stupid random move maker for first tests.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class MinimaxAI implements AI {

	private static int DEPTH = 2;

	public MinimaxAI() {
	}

	/**
	 * get a short description of this method.
	 *
	 * @return String
	 */
	public String getDescription() {
		return "Minimax - Gregório e Tiago";
	}

	/**
	 * get the name of this AI Method.
	 *
	 * @return String
	 */
	public String getName() {
		return "Minimax";
	}

	/**
	 * initialize this instance.
	 */
	public void init() {
		//
	}

	/**
	 * given a board make decide which moves to make.
	 *
	 * @param boardSetup BoardSetup to evaluate
	 * @return SingleMove[] a complete set of moves.
	 */
	public SingleMove[] makeMoves(BoardSetup boardSetup) {
		Node initialNode = new Node();
		initialNode.board = boardSetup;
		initialNode.moves = new SingleMove[0];
		initialNode.value = 0;
		SingleMove[] moves = minimax(initialNode, DEPTH).moves;
		System.out.println(Arrays.toString(moves));
		return moves;
	}

	public static class Node {
		BoardSetup board;
		SingleMove[] moves;
		double value;

		@Override
		public String toString() {
			return "Node{" +
					"board=" + board +
					", moves=" + Arrays.toString(moves) +
					", value=" + value +
					'}';
		}
	}

	private Node minimaxRecursion(Node node, int depth, double alpha, double beta, boolean isMax) {
		if (depth == 0 /* || node.isWon() */) {
			node.value = heuristic(node.board);
			return node;
		}

		List<Node> children = generateChildren(node);
		if (isMax) {
			node.value = Double.NEGATIVE_INFINITY;
			for (Node child : children) {
				node.value = Math.max(minimaxRecursion(child, depth - 1, alpha, beta, true).value, node.value);
				alpha = Math.max(alpha, node.value);
				if (beta <= alpha)
					return node;
			}
		} else {
			node.value = Double.POSITIVE_INFINITY;
			for (Node child : children) {
				node.value = Math.min(minimaxRecursion(child, depth - 1, alpha, beta, true).value, node.value);
				beta = Math.min(beta, node.value);
				if (beta <= alpha)
					return node;
			}
		}
		return node;
	}

	private Node minimax(Node node, int depth) {
		node.value = Double.NEGATIVE_INFINITY;
		for (Node child : generateChildren(node)) {
			child = minimaxRecursion(child, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
			if (child.value > node.value) {
				node.value = child.value;
				node.moves = child.moves;
			}
		}
		return node;
	}

	private List<Node> generateChildren(Node parent) {
		ArrayList<Node> children = new ArrayList<>();

		PossibleMoves possibleMoves;
		List<BoardSetup> boards;

		possibleMoves = new PossibleMoves(parent.board);
		boards = possibleMoves.getPossibleNextSetups();

		for (int i = 0; i < boards.size(); i++) {
			Node child = new Node();
			child.board = boards.get(i);
			child.moves = possibleMoves.getMoveChain(i);
			child.value = heuristic(child.board);
			children.add(child);
		}
		return children;
	}

	private double heuristic(BoardSetup setup) {
		int blots = 0;
		int blocks = 0;
		int player = setup.getPlayerAtMove();

		for (int i = 1; i <= 24; i++) {
			int p = setup.getPoint(player, i);
			if (p == 1)
				blots++;
			else if (p >= 2)
				blocks++;
		}

		int hisBar = setup.getBar(3 - player);
		double v = setup.calcPip(3 - player) - setup.calcPip(player);
		v += -blots * .6;
		v += blocks * .4;
		v += -setup.getMaxPoint(player);
		v += hisBar * .2;

		return v;
	}

	/**
	 * given a board make decide whether to roll or to double.
	 *
	 * @param boardSetup BoardSetup
	 * @return either DOUBLE or ROLL
	 */
	public int rollOrDouble(BoardSetup boardSetup) {
		return /*random.nextDouble() < 0.05 ? DOUBLE : */ROLL;
	}

	/**
	 * given a board and a double offer, take or drop.
	 *
	 * @param boardSetup BoardSetup
	 * @return either TAKE or DROP
	 * s
	 */
	public int takeOrDrop(BoardSetup boardSetup) {
		return /*random.nextDouble() < 0.1 ? DROP : */TAKE;
	}

	public void dispose() {
	}
}
