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
import jgam.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;

/**
 * A simple about box.
 *
 * @author Mattias Ulbrich
 */
public class JGamFrame_AboutBox extends JDialog implements ActionListener {
	private JPanel panel1 = new JPanel();
	private JPanel insetsPanel1 = new JPanel();
	private JPanel aboutPanel = new JPanel();
	private JButton button1 = new JButton();
	private JLabel imageLabel = new JLabel();
	private JLabel label2 = new JLabel();
	private JLabel label3 = new JLabel();
	private ImageIcon image1 = new ImageIcon();
	private BorderLayout borderLayout1 = new BorderLayout();
	private String version = JGammon.VERSION + "-" + Version.BUILD_NUMBER + ": " + Version.BUILD_DATE;
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel1 = new JLabel();
	private JTabbedPane jTabbedPane1 = new JTabbedPane();
	private JPanel creditPanel = new JPanel();
	private JTextArea creditArea = new JTextArea();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();

	public JGamFrame_AboutBox(Frame parent) {
		super(parent);
		try {
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			jbInit();
			pack();
			creditArea.read(new InputStreamReader(getClass().getResourceAsStream("credits.txt")), null);
			setLocationRelativeTo(parent);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public JGamFrame_AboutBox() {
		this(null);
	}

	/**
	 * Component initialization.
	 *
	 * @throws java.lang.Exception
	 */
	private void jbInit() throws Exception {
		image1 = new ImageIcon(jgam.gui.JGamFrame.class.getResource("/jgam/img/about.png"));
		imageLabel.setIcon(image1);
		setTitle("About");
		panel1.setLayout(borderLayout1);
		label2.setText(version);
		label3.setText(
				"Copyright (c) 2005/06 - Published under GNU General Public License");
		aboutPanel.setLayout(gridBagLayout1);
		button1.setText("OK");
		button1.addActionListener(this);
		jLabel1.setText("Version  ");
		aboutPanel.setBackground(Color.white);
		aboutPanel.setBorder(BorderFactory.createEtchedBorder(Color.white,
				new Color(178, 178, 178)));
		panel1.setBorder(null);
		creditPanel.setLayout(borderLayout2);
		creditArea.setEditable(false);
		getContentPane().add(panel1, null);
		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		insetsPanel1.add(button1, null);
		aboutPanel.add(imageLabel,
				new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
						, GridBagConstraints.EAST,
						GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		aboutPanel.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15, 0, 0, 0), 0, 0));
		aboutPanel.add(label3, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 15, 0), 0, 0));
		jTabbedPane1.add(aboutPanel, "JGammon");
		aboutPanel.add(label2, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 0, 0, 0), 0, 0));
		jTabbedPane1.add(creditPanel, "Credits");
		creditPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jScrollPane1.getViewport().add(creditArea);
		panel1.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
		setResizable(true);
	}

	/**
	 * Close the dialog on a button event.
	 *
	 * @param actionEvent ActionEvent
	 */
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == button1) {
			dispose();
		}
	}
}
