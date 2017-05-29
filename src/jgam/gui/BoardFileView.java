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

import jgam.FileBoardSetup;
import jgam.board.BoardComponent;
import jgam.board.MonochromeBoardImplementation;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;


/**
 * Show additional information about .board-Files in the load/save-dialogs.
 *
 * @author Mattias Ulbrich
 */
public class BoardFileView extends JTabbedPane implements PropertyChangeListener {
	public BoardFileView() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public BoardFileView(JFileChooser fc) {
		fc.addPropertyChangeListener(this);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	File file = null;
	private JLabel jLabel1 = new JLabel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel2 = new JLabel();
	private JTextPane commentArea = new JTextPane();
	private JLabel dateArea = new JLabel();
	private JPanel infoPanel = new JPanel();
	private BoardComponent boardComponent = new BoardComponent(new MonochromeBoardImplementation(), null);

	public void propertyChange(PropertyChangeEvent e) {
		boolean update = false;
		String prop = e.getPropertyName();

		// If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			file = null;
			update = true;
		} else
			// If a file became selected, find out which one.
			if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
				file = (File) e.getNewValue();
				update = true;
			}

		// Update the preview accordingly.
		if (update) {
			FileBoardSetup snap = null;
			commentArea.setText("");
			dateArea.setText("");
			try {
				snap = new FileBoardSetup(file);
				String comment = snap.getComment();
				commentArea.setText(comment);
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				dateArea.setText(df.format(snap.getDate()));
				boardComponent.useBoardSetup(snap);
			} catch (Exception ex) {
				commentArea.setText("This is not a JGammon-file");
			}
		}
	}


	private void jbInit() throws Exception {
		jLabel1.setText("Comment:");
		infoPanel.setLayout(gridBagLayout1);
		jLabel2.setText("Date: ");
		boardComponent.setPreferredSize(new Dimension(200, 100));
		commentArea.setBackground(UIManager.getColor("Label.background"));
		commentArea.setBorder(null);
		commentArea.setEditable(false);
		infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,
				new Color(148, 145, 140)), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
		infoPanel.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		infoPanel.add(jLabel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
				, GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		infoPanel.add(dateArea, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST,
				GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		infoPanel.add(commentArea, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(infoPanel, "File Info");
		this.add(boardComponent, "Board");
	}
}
