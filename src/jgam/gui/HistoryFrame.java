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

package jgam.gui;

import jgam.JGammon;
import jgam.board.BoardComponent;
import jgam.board.MonochromeBoardImplementation;
import jgam.board.Paintable;
import jgam.game.BoardSetup;
import jgam.game.Move;
import jgam.history.GameStatistics;
import jgam.history.History;
import jgam.history.LoggingBoardSetup;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A little window that lists the history of moves in the ongoing game.
 *
 * @author Mattias Ulbrich
 */
public class HistoryFrame extends JFrame {

	private static ResourceBundle msg = JGammon
			.getResources("jgam.msg.History"); //$NON-NLS-1$

	public HistoryFrame() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	History history;

	public HistoryFrame(History history) {
		super();
		this.history = history;
		try {
			/** @todo make it somehow chooseable which BoardImpl to use */
			board = new BoardComponent(new MonochromeBoardImplementation(),
					null);
			board.addPaintHook(new MovesPainter());
			history.setColorNames(board.getColorName(1), board.getColorName(2));
			gameStatistics = new GameStatistics(history);
			jbInit();
			pack();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private JScrollPane jScrollPane1 = new JScrollPane();

	private JToolTipTable jTable = new JToolTipTable();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JCheckBox jLongNotation = new JCheckBox();

	private JCheckBox jShowBoard = new JCheckBox();

	private JTextField jAnnotation = new JTextField();

	private JPanel annotationPanel = new JPanel();

	private BorderLayout borderLayout1 = new BorderLayout();

	private JPanel ChboxesPanel = new JPanel();

	private JPanel leftPanel = new JPanel();

	private FlowLayout flowLayout1 = new FlowLayout();

	private JSplitPane jSplitPane1 = new JSplitPane();

	private JPanel rightPanel = new JPanel();

	private BorderLayout borderLayout2 = new BorderLayout();

	private JPanel buttonsPanel = new JPanel();

	private JButton jButtonForward = new JButton();

	private JButton jButtonBack = new JButton();

	private JButton jButtonBegin = new JButton();

	private JButton jButtonEnd = new JButton();

	private JTabbedPane jTabbedPane = new JTabbedPane();

	private ImageIcon back = new ImageIcon(HistoryFrame.class
			.getResource("/jgam/img/historyback.gif")); //$NON-NLS-1$

	private ImageIcon forward = new ImageIcon(HistoryFrame.class
			.getResource("/jgam/img/historyforward.gif")); //$NON-NLS-1$

	private ImageIcon end = new ImageIcon(HistoryFrame.class
			.getResource("/jgam/img/historyend.gif")); //$NON-NLS-1$

	private ImageIcon begin = new ImageIcon(HistoryFrame.class
			.getResource("/jgam/img/historybegin.gif")); //$NON-NLS-1$

	private BoardComponent board; // = new BoardComponent(null, null); // for

	// JBuilder ...

	// Marked Index in the history
	private int lastIndex = -1;

	// the list of Moves to be painted on top
	private List movesToOverlay = null;

	// the stored setup
	private BoardSetup boardSetup = null;

	// the statitics support
	private GameStatistics gameStatistics = null;

	private DefaultTableModel statTableModel;

	private JTable statTable;

	private static NumberFormat numberFormatter = NumberFormat.getInstance();

	private void jbInit() throws Exception {
		setTitle(msg.getString("history")); //$NON-NLS-1$
		leftPanel.setLayout(gridBagLayout1);
		jLongNotation.setText(msg.getString("longnotation")); //$NON-NLS-1$
		jLongNotation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				notationChanged(e);
			}
		});
		jTable.setCellSelectionEnabled(true);
		jTable.setRowSelectionAllowed(true);
		jTable.setColumnSelectionAllowed(true);
		jTable.setToolTipper(history);
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable.getColumnModel().getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(javax.swing.event.ListSelectionEvent e) {
						selectionHasChanged();
					}
				});

		jTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(javax.swing.event.ListSelectionEvent e) {
						selectionHasChanged();
					}
				});
		jScrollPane1.setPreferredSize(new Dimension(270, 300));
		jShowBoard.setSelected(true);
		jShowBoard.setText(msg.getString("showboard")); //$NON-NLS-1$
		annotationPanel.setLayout(borderLayout1);
		annotationPanel
				.setBorder(new TitledBorder(
						BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
								Color.white, new Color(148, 145, 140)), msg
						.getString("annotationround"))); //$NON-NLS-1$
		ChboxesPanel.setLayout(flowLayout1);
		rightPanel.setLayout(borderLayout2);
		jButtonForward.setIcon(forward);
		jButtonForward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});
		jButtonBack.setIcon(back);
		jButtonBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				back();
			}
		});
		jButtonBegin.setIcon(begin);
		jButtonBegin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				first();
			}
		});
		jButtonEnd.setIcon(end);
		jButtonEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				last();
			}
		});
		board.setBorder(null);

		leftPanel.add(jScrollPane1, new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		leftPanel.add(annotationPanel, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		annotationPanel.add(jAnnotation, java.awt.BorderLayout.CENTER);
		leftPanel.add(ChboxesPanel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		jScrollPane1.getViewport().add(jTable);

		ChboxesPanel.add(jLongNotation, null);
		ChboxesPanel.add(jShowBoard, null);
		jSplitPane1.add(rightPanel, JSplitPane.RIGHT);
		this.getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);
		jSplitPane1.add(leftPanel, JSplitPane.LEFT);
		rightPanel.add(buttonsPanel, java.awt.BorderLayout.SOUTH);
		buttonsPanel.add(jButtonBegin);
		buttonsPanel.add(jButtonBack);
		buttonsPanel.add(jButtonForward);
		buttonsPanel.add(jButtonEnd);
		rightPanel.add(jTabbedPane, java.awt.BorderLayout.CENTER);
		jTabbedPane.add(board, msg.getString("board")); //$NON-NLS-1$
		{
			statTableModel = new DefaultTableModel(9, 3);
			statTableModel.setValueAt(history.getGame().getPlayer1().getName(),
					0, 1);
			statTableModel.setValueAt(history.getGame().getPlayer2().getName(),
					0, 2);
			statTableModel.setValueAt(msg.getString("coloronboard"), 1, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("maydouble"), 2, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("checkers"), 3, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("piptogo"), 4, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("pipsofar"), 5, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("doubles"), 6, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("hits"), 7, 0); //$NON-NLS-1$
			statTableModel.setValueAt(msg.getString("ai_eval"), 8, 0); //$NON-NLS-1$
			statTable = new JTable();
			statTable.setModel(statTableModel);
			jTabbedPane.add(statTable, msg.getString("statistics")); //$NON-NLS-1$
		}
		jTable.setModel(history);
		jTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		jTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		jTable.getColumnModel().getColumn(0).setMaxWidth(30);
		board.setPreferredSize(new Dimension(400, 300));
		{
			JPopupMenu boardPopupMenu = new JPopupMenu();
			setComponentPopupMenu(board, boardPopupMenu);
			{
				JMenuItem jMenuItem1 = new JMenuItem();
				boardPopupMenu.add(jMenuItem1);
				jMenuItem1.setText(msg.getString("turnboard")); //$NON-NLS-1$
				jMenuItem1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						board.toggleTurn();
					}
				});
			}
			{
				JMenuItem jMenuItem2 = new JMenuItem();
				boardPopupMenu.add(jMenuItem2);
				jMenuItem2.setText(msg.getString("flipboard")); //$NON-NLS-1$
				jMenuItem2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						board.toggleLeftRight();
					}
				});
			}
		}
	}

	/**
	 * Auto-generated method for setting the popup menu for a component
	 */
	private void setComponentPopupMenu(final java.awt.Component parent,
	                                   final javax.swing.JPopupMenu menu) {
		parent.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent e) {
				if (e.isPopupTrigger())
					menu.show(parent, e.getX(), e.getY());
			}

			public void mouseReleased(java.awt.event.MouseEvent e) {
				if (e.isPopupTrigger())
					menu.show(parent, e.getX(), e.getY());
			}
		});
	}

	/**
	 * if the checkbox is selected set the longFormat-Mode in the history
	 * appropriate.
	 */
	public void notationChanged(ChangeEvent e) {
		history.setLongMode(jLongNotation.isSelected());
		repaint();
	}

	/**
	 * the selection in the table MIGHT have changed!
	 */
	public void selectionHasChanged() {

		int col = jTable.getSelectedColumn();
		int row = jTable.getSelectedRow();

		if (col < 0 || row < 0) {
			return; // dont know why this happens ...
		}

		if (col == 0) {
			jTable.getColumnModel().getSelectionModel().setSelectionInterval(1,
					1);
			return;
		}

		int index = history.getIndex(row, col);

		if (index == -1) {
			first();
			return;
		}

		if (index == lastIndex) {
			return;
		}

		if (index >= history.numberOfEntries()) {
			last();
			return;
		}

		if (lastIndex != -1) {
			history.setAnnotation(lastIndex, jAnnotation.getText());
		}

		jAnnotation.setText(history.getAnnotation(index));
		boardSetup = history.getSetupBeforeIndex(index);
		board.useBoardSetup(boardSetup);
		movesToOverlay = history.getMovesForIndex(index);
		lastIndex = index;
		updateStatistics(index);
		repaint();
	}

	/**
	 * the data in the table may have changed, update the statistics!
	 */
	private void updateStatistics(int index) {
		gameStatistics.goAfterIndex(index);
		BoardSetup after = history.getSetupAfterIndex(index);
		// if (jTabbedPane.getSelectedIndex() == 1) {
		statTableModel.setValueAt(JGammon.jgammon().getBoard().getColorName(1), 1, 1);
		statTableModel.setValueAt(JGammon.jgammon().getBoard().getColorName(2), 1, 2);
		statTableModel.setValueAt(gameStatistics.mayDouble(1) ? msg.getString("yes") //$NON-NLS-1$
				: msg.getString("no"), 2, 1); //$NON-NLS-1$
		statTableModel.setValueAt(gameStatistics.mayDouble(2) ? msg.getString("yes") //$NON-NLS-1$
				: msg.getString("no"), 2, 2); //$NON-NLS-1$
		statTableModel.setValueAt(Integer.toString(gameStatistics
				.getCheckersOnBoard(1)), 3, 1);
		statTableModel.setValueAt(Integer.toString(gameStatistics
				.getCheckersOnBoard(2)), 3, 2);
		statTableModel.setValueAt(Integer.toString(after.calcPip(1)), 4, 1);
		statTableModel.setValueAt(Integer.toString(after.calcPip(2)), 4, 2);
		statTableModel.setValueAt(Integer.toString(gameStatistics
				.getPipsSoFar(1)), 5, 1);
		statTableModel.setValueAt(Integer.toString(gameStatistics
				.getPipsSoFar(2)), 5, 2);
		statTableModel.setValueAt(Integer
				.toString(gameStatistics.getDoubles(1)), 6, 1);
		statTableModel.setValueAt(Integer
				.toString(gameStatistics.getDoubles(2)), 6, 2);
		statTableModel.setValueAt(Integer.toString(gameStatistics.getHits(1)),
				7, 1);
		statTableModel.setValueAt(Integer.toString(gameStatistics.getHits(1)),
				7, 2);
		statTableModel.setValueAt(makeAIString(after, 1), 8, 1);
		statTableModel.setValueAt(makeAIString(after, 2), 8, 2);

		// }
	}

	private String makeAIString(BoardSetup setup, int player) {
		return numberFormatter.format(gameStatistics.getAIEval(setup, player))
				+ " [" //$NON-NLS-1$
				+ numberFormatter.format(gameStatistics.getAIMoves(setup,
				player)) + "] (" //$NON-NLS-1$
				+ gameStatistics.getAICategory(setup, player) + ")"; //$NON-NLS-1$
	}

	/**
	 * move the selection of the table to the given index.
	 *
	 * @param index to go to
	 */
	public void gotoIndex(int index) {

		if (history.getStartingPlayerIndex() == 2) {
			index++;
		}

		int row = index / 2;
		int col = index % 2 + 1;

		jTable.getColumnModel().getSelectionModel().setSelectionInterval(col,
				col);
		jTable.getSelectionModel().setSelectionInterval(row, row);
	}

	public void back() {
		gotoIndex(Math.max(0, lastIndex - 1));
	}

	public void first() {
		gotoIndex(0);
	}

	public void next() {
		gotoIndex(Math.min(lastIndex + 1, history.getNumberOfEntries() - 1));
	}

	public void last() {
		gotoIndex(history.getNumberOfEntries() - 1);
	}

	/**
	 * This class is used to paint the lines to illustrate in the preview which
	 * moves have been made.
	 */
	private class MovesPainter implements Paintable {
		private Color overlayColor = Color.getColor(
				"jgam.history.overlaycolor", Color.ORANGE); //$NON-NLS-1$

		public void paint(Graphics g) {
			if (movesToOverlay == null || boardSetup == null) {
				return;
			}
			LoggingBoardSetup bs = new LoggingBoardSetup(boardSetup);
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(overlayColor);
			int offset = board.getBoardImplementation().getCheckerDiameter() / 2;
			g2d.setStroke(new BasicStroke(3f));

			for (Iterator iter = movesToOverlay.iterator(); iter.hasNext(); ) {
				Move move = (Move) iter.next();
				int player = move.player();
				int natfrom = board.playerToNativePoint(move.from(), player);
				Point from = board.getPointForChecker(natfrom, bs.getPoint(
						player, move.from()) - 1);
				from.translate(offset, offset);
				Point to;
				if (move.to() != 0) {
					int natto = board.playerToNativePoint(move.to(), player);
					to = board.getPointForChecker(natto, bs.getPoint(player,
							move.to()));
				} else {
					Rectangle r = board.getOffField(player);
					to = r.getLocation();
					to.translate(0, r.height - 2 * offset);
				}

				to.translate(offset, offset);
				g.drawLine(from.x, from.y, to.x, to.y);
				bs.performMove(move);
			}
		}
	}

}
