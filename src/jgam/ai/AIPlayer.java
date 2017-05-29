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

import jgam.JGammon;
import jgam.game.Player;
import jgam.game.PlayerMessage;
import jgam.game.SingleMove;

/**
 * An AIPlayer delegates all jobs to an underlying AI-Object.
 * <p>
 * The "thinking" work is done in threads on their own in order to not slow down
 * the gamethread.
 */
public class AIPlayer extends Player {

	private AI ai;
//    private Thread thread;

	public AIPlayer(AI ai) throws Exception {
		super(ai.getName());
		this.ai = ai;
		ai.init();
	}

	/**
	 * send everything to the thread!
	 *
	 * @param playerMessage PlayerMessage
	 */
	public void inform(final PlayerMessage playerMessage) {
		JGammon.jobThread.add(new Runnable() {
			public void run() {
				handleInform(playerMessage);
			}
		});
	}

	/**
	 * only react to MY_TURN, DOUBLE and GIVEUP
	 *
	 * @param playerMessage a Message telling what to do next
	 */
	public void handleInform(PlayerMessage playerMessage) {
		try {

			switch (playerMessage.getMessage()) {
				case PlayerMessage.MY_TURN:
					if (playerMessage.getOwner() == this) {
						myturn();
					}
					break;

				case PlayerMessage.START_GAME:
					if (game.getGameBoard().getPlayerAtMove() == getNumber()) {
						mymoves();
					}
					break;

				case PlayerMessage.DICES:
					if (playerMessage.getOwner() == this) {
						mymoves();
					}
					break;

				case PlayerMessage.DOUBLE:
					if (playerMessage.getOwner() != this) {
						answerDouble();
					}
					break;

				case PlayerMessage.GIVEUP_REQUEST:
					answerGiveUp();
					break;

//                default:
//                    System.err.println("Unhandled for AI: " + playerMessage);

			}
		} catch (Exception ex) {
			game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.ABNORMAL_ABORT, ex));
		}
	}


	/**
	 * mymoves
	 */
	private void mymoves() throws CannotDecideException {
		if (game.getGameBoard().canMove()) {
			SingleMove moves[] = ai.makeMoves(game.getGameBoard());
			if (ai instanceof EvaluatingAI) {
				double eval = ((EvaluatingAI) ai).propabilityToWin(game.getGameBoard());
				System.out.println("Board estimated to a winning chance of " + eval + " for " + getName() + ".");
			}
			for (SingleMove move : moves) {
				game.sendPlayerMessage(new PlayerMessage(AIPlayer.this, PlayerMessage.MOVE, move));
			}

		}
	}

	/**
	 * answerGiveUp
	 */
	private void answerGiveUp() {
		getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.GIVEUP_DROPPED));
	}

	/**
	 * answerDouble
	 */
	private void answerDouble() throws CannotDecideException {
		if (ai.takeOrDrop(game.getGameBoard()) == ai.TAKE) {
			game.sendPlayerMessage(new PlayerMessage(AIPlayer.this, PlayerMessage.DOUBLE_TAKEN));
		} else {
			game.sendPlayerMessage(new PlayerMessage(AIPlayer.this, PlayerMessage.DOUBLE_DROPPED));
		}
	}

	/**
	 * myturn
	 */
	private void myturn() throws CannotDecideException {
		if (game.getGameBoard().mayDouble(getNumber())) {
			int rollOrDouble = ai.rollOrDouble(game.getGameBoard());
			if (rollOrDouble == ai.DOUBLE) {
				game.sendPlayerMessage(new PlayerMessage(AIPlayer.this, PlayerMessage.DOUBLE));
				return;
			}
		}
		game.sendPlayerMessage(new PlayerMessage(AIPlayer.this, PlayerMessage.ROLL));
	}

	/**
	 * does this player use the local UI? If so, return false, else true.
	 *
	 * @return true iff player uses local UI
	 */
	public boolean isLocal() {
		return false;
	}

	/**
	 * called to "finish" the player so that it can close open connections to
	 * deallocate resources, unsubsribe from handlers etc.
	 */
	public void dispose() {
		ai.dispose();
	}

}
