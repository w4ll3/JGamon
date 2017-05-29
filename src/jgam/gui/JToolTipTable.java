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

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * A modified JTable that supports ToolTip for every cell.
 * <p>
 * An interface ToolTipper is provided that sets the tooltip for every
 * cell
 *
 * @author Mattias Ulbrich
 */
public class JToolTipTable extends JTable {

	public static interface ToolTipper {
		public String getToolTip(int row, int col);
	}

	private ToolTipper toolTipper;

	public JToolTipTable() {
		super();
	}

	public JToolTipTable(int numRows, int numColumns) {
		super(numRows, numColumns);
	}

	public JToolTipTable(TableModel dm) {
		super(dm);
	}

	public JToolTipTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
	}

	public JToolTipTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
	}

	public JToolTipTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
	}

	public JToolTipTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>toolTipper</code>.
	 *
	 * @return the value of <code>toolTipper</code>
	 */
	public jgam.gui.JToolTipTable.ToolTipper getToolTipper() {
		return this.toolTipper;
	}

	/**
	 * use this <i>setter</i> method to set the value of the field <code>toolTipper</code>.
	 *
	 * @param toolTipper new value to be set for <code>toolTipper</code>
	 */
	public void setToolTipper(jgam.gui.JToolTipTable.ToolTipper toolTipper) {
		this.toolTipper = toolTipper;
	}

	/**
	 * Returns the string to be used as the tooltip for <i>event</i>.
	 * Calculate the cell from the mouse event.
	 *
	 * @param event MouseEvent to get tooltip for
	 * @return String corresponding to the cell for the mouse event
	 */
	public String getToolTipText(MouseEvent event) {

		java.awt.Point p = event.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);

		return toolTipper.getToolTip(rowIndex, colIndex);
	}


}
