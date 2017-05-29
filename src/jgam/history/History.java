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

import jgam.game.BoardSetup;
import jgam.game.Game;
import jgam.game.Move;
import jgam.game.Player;
import jgam.gui.HistoryFrame;
import jgam.gui.JToolTipTable;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: JGam - Java Backgammon</p>
 * <p>
 * <p>Description: </p>
 * <p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class History extends AbstractTableModel implements JToolTipTable.ToolTipper {

	// if this is connected to a game
	private Game game;

	// who was the first to play?
	private int startingPlayer;

	// if this has the names of the players stored
	private String namePlayer1, namePlayer2;

	// if I have the color names stored here - colors in the preview window
	private String colorPlayer1, colorPlayer2;

	// thats where the history is kept.
	// it's list of HistoryMessage-Objects
	private List entries = new ArrayList();

	// that's the list of annotations. it has to have the same length as
	// entries
	private List annotations = new ArrayList();

	// a MoveMessage is built step by step. this is the current
	private MoveMessage moveMessage = null;

	// if this game did not start with the usual setup
	private BoardSetup initialSetup;

	// true if messages are to returned in long long string rather than short
	// use HistoryMessage.toLongString() not .toShortString()
	private boolean longMode;

	// the one and only frame for this histroy
	private HistoryFrame historyFrame;

	public History(Game game) {
		this.game = game;
	}

	public History(String name1, String name2) {
		this.namePlayer1 = name1;
		this.namePlayer2 = name2;
	}

	/**
	 * return the number of rounds that are kept in this history.
	 * <p>
	 * The first round may consist only of one step if player 2 starts.
	 * It also counts rounds that just have begun.
	 *
	 * @return number of begun rounds in this history
	 */
	public int getNumberOfRounds() {

        /*
         * that's tricky:
         * if p1 started it is: (entries.size()+1) / 2  to ensure ceiling-round
         * if p2 started it is: (entries.size()+1+1) /2  to ensure ceiling-round
         * ergo:
         */
		return (entries.size() + startingPlayer) / 2;

	}

	/**
	 * A player turns the double cube.
	 *
	 * @param p           player to double
	 * @param doubleValue value to double to
	 */
	public void addDoubleProposal(Player p, int doubleValue) {
		// last round has finished
		moveMessage = null;
		entries.add(new DoubleMessage(p.getNumber(), doubleValue));
		annotations.add("");
		fireTableDataChanged();
	}

	/**
	 * A player takes or drops a double cube.
	 *
	 * @param p    player to take/drop
	 * @param take true if take, false if drop
	 */
	public void addDoubleAnswer(Player p, boolean take) {
		entries.add(new DoubleMessage(p.getNumber(), take));
		annotations.add("");
		fireTableDataChanged();
	}

	public void addRoll(Player p, int dice[]) {
		moveMessage = new MoveMessage(p.getNumber(), dice);
		entries.add(moveMessage);
		annotations.add("");
		fireTableDataChanged();
	}

	public void addMove(Move m) {
		moveMessage.add(m);
		fireTableDataChanged();
	}

	public void addUndo() {
		moveMessage.clear();
		fireTableDataChanged();
	}

	/**
	 * A player has given up.
	 *
	 * @param p    player to give up
	 * @param type NORMAL(1), GAMMON(2), BACKGAMMON(3)
	 */
	public void addGiveup(Player p, int type) {
		entries.add(new GiveupMessage(p.getNumber(), type));
		annotations.add("");
		fireTableDataChanged();
	}

	public void setAnnotation(int index, String annotation) {
		annotations.set(index, annotation);
	}

	public String getAnnotation(int index) {
		if (index > 0 && index < annotations.size()) {
			return (String) annotations.get(index);
		} else {
			return null;
		}
	}

	public void setInitialSetup(BoardSetup bs) {
		initialSetup = bs;
		int[] dice = bs.getDice();

		startingPlayer = bs.getPlayerAtMove();

		if (dice != null && startingPlayer != 0) {
			// addroll ...
			moveMessage = new MoveMessage(startingPlayer, dice);
			entries.add(moveMessage);
			annotations.add("");
			fireTableDataChanged();
		}
	}

	public void setStartingPlayer(Player p) {
		startingPlayer = p.getNumber();
	}

	/**
	 * get the index of the starting player.
	 *
	 * @return 1 or 2, 0 if none set yet.
	 */
	public int getStartingPlayerIndex() {
		return startingPlayer;
	}


	//////////////////////////
	// TableModelStuff
	//////////////////////////

	/**
	 * use the long (true) or the short (false) textMode
	 *
	 * @param longMode true iff long texts
	 */
	public void setLongMode(boolean longMode) {
		this.longMode = longMode;
	}

	/**
	 * the number of rows is exactly the number of rounds played
	 */
	public int getRowCount() {
		return getNumberOfRounds();
	}

	/**
	 * there are 3 cols: #, Name1, Name2
	 *
	 * @return 3
	 */
	public int getColumnCount() {
		return 3;
	}

	/**
	 * given the row and col in the table return the index in the entries list.
	 * <p>
	 * It does not guarantee that the index is within the boundaries of the
	 * entry list
	 *
	 * @param rowIndex    row in the table
	 * @param columnIndex col in the table
	 * @return index in entries.
	 */
	public int getIndex(int rowIndex, int columnIndex) {
		return rowIndex * 2 + (columnIndex - 1) + ((startingPlayer == 2) ? -1 : 0);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return new Integer(rowIndex);
		}

		int index = getIndex(rowIndex, columnIndex);

		if (index == -1 || index >= entries.size()) {
			return null;
		}

		return longMode ?
				((HistoryMessage) entries.get(index)).toLongString() :
				((HistoryMessage) entries.get(index)).toShortString();
	}

	public String getToolTip(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return null;
		}

		int index = getIndex(rowIndex, columnIndex);

		if (index == -1 || index >= entries.size()) {
			return null;
		}

		String annot = (String) annotations.get(index);
		// empty annotations shall not appear as tool tips
		return annot.length() == 0 ? null : annot;
	}

	public int getNumberOfEntries() {
		return entries.size();
	}


	/**
	 * see getColumnCount
	 *
	 * @todo color stuff
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return "#";
			case 1:
				if (namePlayer1 == null)
					namePlayer1 = game.getPlayer1().getName();

				if (colorPlayer1 != null) {
					return namePlayer1 + " (" + colorPlayer1 + ")";
				} else {
					return namePlayer1;
				}
			case 2:
				if (namePlayer2 == null)
					namePlayer2 = game.getPlayer2().getName();

				if (colorPlayer2 != null) {
					return namePlayer2 + " (" + colorPlayer2 + ")";
				} else {
					return namePlayer2;
				}
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Delegate method void showUp() to panel : HistoryPanel
	 */
	public void showUp() {
		if (historyFrame == null)
			historyFrame = new HistoryFrame(this);
		historyFrame.setVisible(true);
	}

	/////////////////
	// BoardSetup stuff
	/////////////////

	/**
	 * return the setup of the board as it has been before the move at position
	 * index
	 *
	 * @param index steps to perform up to
	 * @return a LogBoardSetup that has all the moves up to index performed.
	 */
	public BoardSetup getSetupAfterIndex(int index) {
		if (index < 0 || index >= entries.size()) {
			return null;
		}

		LoggingBoardSetup ret = new LoggingBoardSetup(initialSetup);
		for (int i = 0; i <= index; i++) {
			((HistoryMessage) entries.get(i)).applyTo(ret);
		}
		return ret;
	}

	public BoardSetup getSetupBeforeIndex(int index) {
		LoggingBoardSetup bs;
		if (index == 0) {
			bs = new LoggingBoardSetup(initialSetup);
		} else {
			bs = (LoggingBoardSetup) getSetupAfterIndex(index - 1);
		}

		if (index < entries.size() && (entries.get(index) instanceof MoveMessage)) {
			bs.setDice(((MoveMessage) entries.get(index)).getDice());
			if (bs.getActivePlayer() != 0) {
				bs.setActivePlayer(((MoveMessage) entries.get(index)).getPlayer());
			}
		} else {
			bs.setDice(null);
		}

		return bs;
	}

	/**
	 * return a list of Move-Objects for an index.
	 *
	 * @param index index in the move-list
	 * @return List of Move-Objects or null, if this is not an entry with moves
	 */
	public List getMovesForIndex(int index) {
		if (index < entries.size() && entries.get(index) instanceof MoveMessage) {
			MoveMessage mm = (MoveMessage) entries.get(index);
			return mm.moves;
		}
		return null;
	}

	/**
	 * get the number of entries in this history thus the number of valid fields
	 * in the table
	 *
	 * @return int >= 0.
	 */
	public int numberOfEntries() {
		return entries.size();
	}


	/**
	 * get the list of entries.
	 *
	 * @return List of MoveMessage-Objects
	 */
	List getEntries() {
		return entries;
	}

	/**
	 * @return Returns the game.
	 */
	public Game getGame() {
		return game;
	}

	public void setColorNames(String colorName1, String colorName2) {
		colorPlayer1 = colorName1;
		colorPlayer2 = colorName2;
	}


}
