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

import jgam.JGammon;
import jgam.gui.UIObject;
import jgam.history.History;
import jgam.util.BlockingQueue;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * This is the game itsself - the logic etc.
 * The players are contained in here as well.
 * <p>
 * After construction a game is started by the start()-method.
 * It then runs in its own thread.
 * <p>
 * After finding out the beginning party, the players take turns.
 * (method play()).
 * <p>
 * A game can be stopped via the abort-method.
 * <p>
 * Many methods are available both for Player and for int arguments.
 * Player was first. Introducing the BoardSetup led to the int-versions.
 *
 * @author Mattias Ulbrich
 */

public class Game {

	// two players
	private Player player1, player2;

	// the current player
	private Player currentPlayer;

	// the history behind
	private History history = new History(this);

	// the singleton
	private JGammon jgam;

	// the Object that handles all UI-stuff.
	private UIObject uiObject;

	// the game runs in its own thread
	private static Thread gameThread;

	// the last snapshot: this can be saved to disk.
	private BoardSetup snapshot;

	// this is the setup to which must be returned to undo
	private BoardSetup undoSetup;

	// the player that may undo
	private Player undoPlayer;

	// the board of the game
	private GameBoard gameBoard;

	// to trigger dice rolling
	private DiceRoller diceRoller;

	// log the dices to a file
	private PrintWriter diceLogger;

	// there is a winner!
	private Player winner = null;

	// the type (factor) of winning: 1=normal, 2=gammon, 3=backgammon
	// -1 not yet decided
	private int wintype = -1;
	public static final int WINTYPE_NORMAL = 1;
	public static final int WINTYPE_GAMMON = 2;
	public static final int WINTYPE_BACKGAMMON = 3;

	// the queue of messages that have been sent by the players.
	private BlockingQueue messageQueue = new BlockingQueue();

	// the state the game is in
	private int state = -1;
	public final static int STATE_CHOOSE_BEGINNER = 0;
	public final static int STATE_AT_MOVE = 1;
	public final static int STATE_AT_DECISION = 2;
	public final static int STATE_GAME_OVER = -1;
	public final static int STATE_WAIT_FOR_DOUBLE_ANSWER = 3;
	public final static int STATE_WAIT_FOR_DICES = 4;

	public boolean isSimulation = false;

	/**
	 * construct a new Game Object.
	 *
	 * @param diceRoller DiceRoller
	 * @param p1         Player
	 * @param p2         Player
	 * @param jgam       JGammon
	 * @throws IOException
	 */
	public Game(DiceRoller diceRoller, Player p1, Player p2, JGammon jgam) {
		this.player1 = p1;
		this.player2 = p2;
		this.gameBoard = new GameBoard();
		this.diceRoller = diceRoller;
		this.player1.setGame(this, 1);
		this.player2.setGame(this, 2);
		this.jgam = jgam;
		this.uiObject = new UIObject(jgam, this);
		this.diceLogger = jgam.getDiceLogger();
	}

	public void reset() {
		this.gameBoard = new GameBoard();
		this.state = -1;
		this.wintype = -1;
		this.winner = null;
		this.history = new History(this);
		this.snapshot = BoardSnapshot.INITIAL_SETUP;
	}

	/**
	 * return current player
	 *
	 * @return Player
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * return the player that is not the current player.
	 *
	 * @return Player
	 */
	public Player getOtherPlayer() {
		return (currentPlayer == player1) ? player2 : player1;
	}

	/**
	 * return the player that is not p.
	 *
	 * @param p Player NOT to return
	 * @return the other player
	 */
	public Player getOtherPlayer(Player p) {
		return p == player1 ? player2 : player1;
	}

	/**
	 * return the one player that may press undo
	 *
	 * @return Player
	 */
	public Player getUndoPlayer() {
		return undoPlayer;
	}

	/**
	 * add a message to the queue of messages to be processed in the loop.
	 *
	 * @param pm PlayerMessage to be add
	 */
	public void sendPlayerMessage(PlayerMessage pm) {
		if (!messageQueue.isAborted()) {
			messageQueue.add(pm);
		}
	}

	/**
	 * start a thread and save in gameThread.
	 */
	public void start() {
		assert gameThread == null;
		isSimulation = false;
		if (snapshot == null) {
			snapshot = BoardSnapshot.INITIAL_SETUP;
		}
		gameThread = new Thread("Game-Thread") {
			public void run() {
				Game.this.run();
			}
		};
		gameThread.start();
	}

	private int[] simulationI = {0};
	private int[] simulationCount = {0};

	public void startSimulation(final int number) {
		assert gameThread == null;
		if (snapshot == null) {
			snapshot = BoardSnapshot.INITIAL_SETUP;
		}
		isSimulation = true;
		simulationEnded = false;
		simulationCount[0] = number;
		jgam.startProgress(simulationI, simulationCount);
		gameThread = new Thread("Game-Thread") {
			public void run() {
				Game.this.simulationI[0] = 0;
				for (; Game.this.simulationI[0] < Game.this.simulationCount[0]; Game.this.simulationI[0]++) {
					Game.this.run();
					Game.this.reset();
					jgam.setProgress(Game.this.simulationI[0], Game.this.simulationCount[0]);
				}
				Game.this.endSimulation(Game.this.simulationCount[0]);
				jgam.disposeProgress();
			}
		};
		gameThread.start();
	}

	private int simulationRuns = 0;
	private int playerOneWins = 0;
	private int playerTwoWins = 0;

	public int getSimulationWins(int index) {
		if (index == 0) return playerOneWins;
		else if (index == 1) return playerTwoWins;
		else throw new Error("Invalid index: " + index);
	}

	private boolean simulationEnded = false;

	private void endSimulation(int totalRuns) {
		this.simulationRuns = totalRuns;
		this.simulationEnded = true;
	}

	public boolean hasSimulationEnded() {
		return this.simulationEnded;
	}

	public int getSimulationCount() {
		return this.simulationRuns;
	}

	/**
	 * run the thread: initiate the loop.
	 */
	private void run() {
		// setup board
		gameBoard.applySetup(snapshot);
		history.setInitialSetup(snapshot);
		currentPlayer = gameBoard.getPlayerAtMove() == 1 ? player1 : player2;
		undoSetup = snapshot;

		try {
			// determine initialstate
			if (gameBoard.getPlayerAtMove() == 0) {
				chooseBeginner();
			}

			setState(STATE_AT_MOVE);
			history.setStartingPlayer(getCurrentPlayer());
			informPlayers(new PlayerMessage(PlayerMessage.START_GAME));

			while (state != STATE_GAME_OVER) {
				switch (state) {
					case STATE_AT_MOVE:
						atMove();
						break;
					case STATE_AT_DECISION:
						atDecision();
						break;
					case STATE_WAIT_FOR_DOUBLE_ANSWER:
						atWaitForDoubleAnswer();
						break;
					case STATE_WAIT_FOR_DICES:
						atAwaitDices();
						break;
					default:
						throw new IllegalStateException("Unknown state: " + state);
				}
			}

			if (winner != null) {
				atWinning();
			}

		} catch (BlockingQueue.BQInterruptedException ex) {
			// there must have been a triggered abort - it's ok
			messageQueue.abort();
			jgam.clearGame();
		} catch (Exception ex) {
			ex.printStackTrace();
			informPlayers(new PlayerMessage(null, PlayerMessage.ABNORMAL_ABORT, ex));
			messageQueue.abort();
			jgam.clearGame();
		} finally {
			player1.dispose();
			player2.dispose();
			if (diceLogger != null)
				diceLogger.close();
		}
	}

	/**
	 * atWinning - at the end of a game
	 */
	private void atWinning() {

		// determine the type of winning, if not already done by giveup or
		// dropped double
		if (wintype == -1) {
			int loser = 3 - winner.getNumber();
			if (gameBoard.getPoint(loser, 0) > 0) {
				wintype = WINTYPE_NORMAL;
			} else if (gameBoard.getMaxPoint(loser) <= 18) {
				wintype = WINTYPE_GAMMON;
			} else {
				wintype = WINTYPE_BACKGAMMON;
			}
		}

		if (winner.getNumber() == 1) {
			playerOneWins++;
		} else if (winner.getNumber() == 2) {
			playerTwoWins++;
		}

		PlayerMessage pm = new PlayerMessage(winner, PlayerMessage.GAME_OVER);
		pm.setValue(wintype);
		pm.setObject(new Integer(wintype * gameBoard.getDoubleCube()));

		informPlayers(pm);
	}

	/**
	 * atMove - a method for the FSM
	 */
	private void atMove() throws InterruptedException, GameException {

		if (gameBoard.getMaxPoint(currentPlayer.getNumber()) == 0) {
			winner = currentPlayer;
			setState(STATE_GAME_OVER);
			return;
		} else if (!gameBoard.canMove()) {
			setState(STATE_AT_DECISION);
			switchPlayers();
			return;
		}

		PlayerMessage pm = receiveMessage();

		switch (pm.getMessage()) {

			case PlayerMessage.MOVE:
				if (pm.getOwner() != getCurrentPlayer()) {
					throw new IllegalStateException("Only the current player may 'move': " + pm);
				}
				Move move = (Move) pm.getObject();
				gameBoard.performMove(move);
				history.addMove(move);
				informPlayers(pm);

				break;

			case PlayerMessage.UNDO:
				if (pm.getOwner() != currentPlayer) {
					throw new IllegalStateException("Only the current player may 'undo': " + pm);
				}

				/** @todo if(!undoPlayer.wantsUndo()) throw new IllegalStateException("No undo wanted! "+pm); */
				gameBoard.applySetup(undoSetup);
				history.addUndo();
				informPlayers(pm);
				break;

			case PlayerMessage.GIVEUP_TAKEN:
				winner = pm.getOwner();
				Player loser = getOtherPlayer(winner);
				wintype = pm.getValue();

				/** @todo if(!loser.wantsGiveup()) throw new IllegalStateException("unwanted giveup!" + pm); */
				/** @todo history */
				setState(STATE_GAME_OVER);
				informPlayers(pm);
				break;

			default:
				throw new IllegalStateException("Unexpected Message " + pm);
		}
	}

	/**
	 * switch the current and the other player
	 */
	private void switchPlayers() {
		currentPlayer = getOtherPlayer();
		gameBoard.setActivePlayer(currentPlayer.getNumber());
	}

	/**
	 * atWaitForDoubleAnswer - a method for the FSM
	 */
	private void atWaitForDoubleAnswer() throws InterruptedException, GameException {

		PlayerMessage pm = receiveMessage();

		// no more undo_requests once a doubling has been requested
		undoPlayer = null;

		switch (pm.getMessage()) {

			case PlayerMessage.DOUBLE_TAKEN:
				if (pm.getOwner() != getOtherPlayer()) {
					throw new IllegalStateException("Only the other player may 'take': " + pm);
				}
				gameBoard.doubleCube.doubling(getCurrentPlayer().getNumber());
				gameBoard.clearDices();
				history.addDoubleAnswer(pm.getOwner(), true);
				undoSetup = null;
				informPlayers(pm);
				setState(STATE_AT_DECISION);
				break;

			case PlayerMessage.DOUBLE_DROPPED:
				if (pm.getOwner() != getOtherPlayer()) {
					throw new IllegalStateException("Only the other player may 'drop': " + pm);
				}
				winner = currentPlayer;
				wintype = WINTYPE_NORMAL;
				history.addDoubleAnswer(pm.getOwner(), false);
				setState(STATE_GAME_OVER);
				informPlayers(pm);
				break;

			case PlayerMessage.GIVEUP_TAKEN:
				winner = pm.getOwner();
				Player loser = getOtherPlayer(winner);
				wintype = pm.getValue();

				/** @todo if(!loser.wantsGiveup()) throw new IllegalStateException("unwanted giveup!" + pm); */
				history.addGiveup(pm.getOwner(), wintype);
				setState(STATE_GAME_OVER);
				informPlayers(pm);
				break;

			default:
				throw new IllegalStateException("Unexpected Message " + pm);
		}
	}

	/**
	 * tell the players that sth has happened
	 *
	 * @param pm message to be sent
	 */
	private void informPlayers(PlayerMessage pm) {
		player1.inform(pm);
		player2.inform(pm);
		uiObject.inform(pm);
	}

	/**
	 * atDecision - a method for the FSM
	 */
	private void atDecision() throws InterruptedException, GameException {

		// tell whose move it is.
		informPlayers(new PlayerMessage(currentPlayer, PlayerMessage.MY_TURN));

		PlayerMessage pm = receiveMessage();

		switch (pm.getMessage()) {
			case PlayerMessage.UNDO:
				if (pm.getOwner() != currentPlayer) {
					throw new IllegalStateException("Only the undo player may 'undo': " + pm);
				}
				if (undoSetup == null) {
					throw new IllegalStateException("No situation to undo anything");
				}

				/** @todo if(!undoPlayer.wantsUndo()) throw new IllegalStateException("No undo wanted! "+pm); */
				/** @todo history */
				gameBoard.applySetup(undoSetup);
				snapshot = undoSetup;
				currentPlayer = undoPlayer;
				setState(STATE_AT_MOVE);
				informPlayers(pm);
				break;

			case PlayerMessage.DOUBLE:
				if (pm.getOwner() != currentPlayer) {
					throw new IllegalStateException("Only the current player may 'double': " + pm);
				}
				if (!gameBoard.mayDouble(currentPlayer.getNumber())) {
					throw new IllegalArgumentException("current player may not double! " + pm);
				}
				history.addDoubleProposal(pm.getOwner(), gameBoard.getDoubleCube() * 2);
				setState(STATE_WAIT_FOR_DOUBLE_ANSWER);
				informPlayers(pm);
				break;

			case PlayerMessage.ROLL:
				if (pm.getOwner() != currentPlayer) {
					throw new IllegalStateException("Only the current player may 'roll': " + pm);
				}
				diceRoller.rollDices(this);
				informPlayers(pm);
				setState(STATE_WAIT_FOR_DICES);
				break;

			case PlayerMessage.GIVEUP_TAKEN:
				winner = pm.getOwner();
				Player loser = getOtherPlayer(winner);
				wintype = pm.getValue();

				/** @todo if(!loser.wantsGiveup()) throw new IllegalStateException("unwanted giveup!" + pm); */
				history.addGiveup(pm.getOwner(), wintype);
				setState(STATE_GAME_OVER);
				informPlayers(pm);
				break;

			default:
				throw new IllegalStateException("Unexpected Message " + pm);
		}

	}

	/**
	 * atAwaitDices - method for the FSM
	 */
	private void atAwaitDices() throws InterruptedException, GameException {
		PlayerMessage pm = receiveMessage();

		switch (pm.getMessage()) {

			case PlayerMessage.DICES:
				int[] dice = (int[]) pm.getObject();
				gameBoard.setDice(dice);
				snapshot = undoSetup = new BoardSnapshot(gameBoard);
				undoPlayer = getCurrentPlayer();
				history.addRoll(getCurrentPlayer(), gameBoard.getDice());
				logDices();
				pm.setOwner(getCurrentPlayer());
				informPlayers(pm);
				setState(STATE_AT_MOVE);
				break;

			case PlayerMessage.GIVEUP_TAKEN:
				winner = pm.getOwner();
				Player loser = getOtherPlayer(winner);
				wintype = pm.getValue();

				/** @todo if(!loser.wantsGiveup()) throw new IllegalStateException("unwanted giveup!" + pm); */
				history.addGiveup(pm.getOwner(), wintype);
				setState(STATE_GAME_OVER);
				informPlayers(pm);
				break;

			default:
				throw new IllegalStateException("Unexpected Message " + pm);
		}

	}

	/**
	 * log dices to diceLogger
	 */
	private void logDices() {
		if (diceLogger != null) {
			int[] dices = gameBoard.getDice();
			diceLogger.println("" + dices[0] + "," + dices[1]);
		}
	}


	/**
	 * take a message from the messageQueue.
	 * <p>
	 * check for ABNORMAL_ERROR
	 *
	 * @return message read from the queue
	 */
	private PlayerMessage receiveMessage() throws InterruptedException, GameException {
		PlayerMessage pm = (PlayerMessage) messageQueue.take();
		if (pm.getMessage() == pm.ABNORMAL_ABORT) {
			throw new GameException((Exception) pm.getObject());
		}
		return pm;
	}

	private static class GameException extends Exception {
		GameException(Exception cause) {
			super("Game ended abnormally", cause);
		}
	}


	/**
	 * chooseBeginner - a method for the finite state machine
	 */
	private void chooseBeginner() throws InterruptedException, GameException {
		int d[];
		do {
			diceRoller.rollDices(this);
			PlayerMessage pm = receiveMessage();
			if (pm.getMessage() != pm.DICES) {
				throw new IllegalStateException("this must be DICES");
			}
			d = (int[]) pm.getObject();
		} while (d[0] == d[1]);

		if (d[0] > d[1]) {
			currentPlayer = player1;
		} else {
			currentPlayer = player2;
		}

		gameBoard.setDice(d);
		history.addRoll(currentPlayer, d);

		// initial setup. player at move deduces from dices.
		gameBoard.setActivePlayer(0);
		undoPlayer = currentPlayer;
		snapshot = undoSetup = new BoardSnapshot(gameBoard);

	}

// a couple of delegated methods to the gameboard:

	/**
	 * get the dice that are currently used in the game
	 *
	 * @return dices.
	 */
	public int[] getDice() {
		return gameBoard.getDiceCopy();
	}

	/**
	 * get the current setup of the board
	 *
	 * @return current setup of the board
	 */
	public GameBoard getGameBoard() {
		return gameBoard;
	}

	/**
	 * after creating a game object, the initial snapshot can
	 * still be changed, as long as there is no running process yet.
	 *
	 * @param bs BoardSetup to be set.
	 */
	public void setBoardSetup(BoardSetup bs) {
		if (gameThread != null) {
			throw new IllegalStateException("thread already running");
		}
		snapshot = new BoardSnapshot(bs);
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>player1</code>.
	 *
	 * @return the value of <code>player1</code>
	 */
	public Player getPlayer1() {
		return this.player1;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>player2</code>.
	 *
	 * @return the value of <code>player2</code>
	 */
	public Player getPlayer2() {
		return this.player2;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>state</code>.
	 *
	 * @return the value of <code>state</code>
	 */
	public int getState() {
		return this.state;
	}

	/**
	 * use this <i>setter</i> method to set the value of the field <code>state</code>.
	 *
	 * @param state new value to be set for <code>state</code>
	 */
	private void setState(int state) {
		this.state = state;
	}

	/**
	 * is there a game alive?
	 *
	 * @return boolean
	 * @throws NullPointerException if thread is null
	 */
	public boolean isRunning() {
		return gameThread.isAlive();
	}

	/**
	 * aborts a running game.
	 */
	public void abortGame() {
		if (!messageQueue.isAborted()) {
			messageQueue.abort();
			try {
				gameThread.join();
			} catch (InterruptedException ex) {
				throw new Error(ex);
			}
		}
	}

	/**
	 * get the history for this game
	 *
	 * @return History object for this game
	 */
	public History getHistory() {
		return history;
	}

}
