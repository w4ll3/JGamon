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


package jgam.util;

import jgam.JGammon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

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
 */
public class ExceptionDialog extends JDialog {

	private ResourceBundle msg = JGammon.getResources("jgam.msg.ExceptionDialog");

	private JPanel panel1 = new JPanel();
	private JLabel iconLabel = new JLabel();
	private JLabel messageLabel = new JLabel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JScrollPane jScrollPane = new JScrollPane();
	private Icon icon = UIManager.getIcon("OptionPane.errorIcon");
	private JPanel jPanel1 = new JPanel();
	private JButton OKButton = new JButton();
	private JToggleButton detailsButton = new JToggleButton();
	private JButton secondButton = new JButton();
	private JTextArea detailArea = new JTextArea();
	private JLabel headLabel = new JLabel();
	private String secondButtonTitle;
	private boolean secondPressed = false;

	public ExceptionDialog(Frame owner, String second) {
		super(owner, true);
		setTitle(msg.getString("title"));
		secondButtonTitle = second;
		try {
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			jbInit();
			pack();
			setLocationRelativeTo(getOwner());
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private ExceptionDialog() {
		// for jbuilder
		this(null, "second");
	}

	public static void show(Exception ex) {
		show(null, ex);
	}

	public static void show(Frame owner, Exception ex) {
		show(owner, null, ex);
	}


	public static void show(Frame owner, String errorMessage) {
		show(owner, errorMessage, null);
	}

	public static void show(Frame owner, String errorMessage, Exception ex) {
		show(owner, errorMessage, ex, null);
	}

	public static boolean show(Frame owner, String errorMessage, Exception ex, String second) {

		ExceptionDialog dialog = new ExceptionDialog(owner, second);

		if (errorMessage == null) {
			String exMsg = ex.getMessage();
			if (exMsg != null) {
				errorMessage = exMsg;
			} else {
				errorMessage = ex.toString();
			}
		}

		dialog.messageLabel.setText(errorMessage);


		if (ex != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.flush();
			dialog.detailArea.setText(sw.toString());
		}

		dialog.setVisible(true);

		return dialog.secondPressed;
	}

	private void jbInit() throws Exception {
		getRootPane().setDefaultButton(OKButton);
		panel1.setLayout(gridBagLayout1);
		messageLabel.setForeground(Color.red);
		messageLabel.setPreferredSize(new Dimension(300, 20));
		messageLabel.setText("some rather long error message");
		iconLabel.setIcon(icon);
		OKButton.setText(msg.getString("OK"));
		OKButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		detailsButton.setText(msg.getString("details"));
		detailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				details();
			}
		});

		jScrollPane.setPreferredSize(new Dimension(1, 160));
		detailArea.setEditable(false);
		detailArea.setBackground(UIManager.getColor("Panel.background"));
		detailArea.setTabSize(2);
		this.setResizable(false);
		headLabel.setText(msg.getString("head"));
		getContentPane().add(panel1);
		jScrollPane.getViewport().add(detailArea);
		jPanel1.add(detailsButton);
		if (secondButtonTitle != null) {
			secondButton.setText(secondButtonTitle);
			jPanel1.add(secondButton);
			secondButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					second();
				}
			});
		}
		jPanel1.add(OKButton);
		panel1.add(jPanel1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 10, 15), 0, 0));
		panel1.add(iconLabel, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 15, 15, 15), 0, 0));
		panel1.add(headLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(15, 0, 0, 0), 0, 0));
		panel1.add(messageLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
	}

	private void details() {
		if (detailsButton.isSelected()) {
			panel1.add(jScrollPane, new GridBagConstraints(0, 4, 2, 1, 0.0, 1.0
					, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 15, 15, 15), 0, 0));
		} else {
			panel1.remove(jScrollPane);
		}
		pack();
	}

	private void ok() {
		dispose();
	}

	private void second() {
		secondPressed = true;
		dispose();
	}


	public static void main(String[] args) {
		try {
			int i = 1 / 0;
			System.out.println("no error: " + i);
		} catch (Exception ex) {
			show(ex);
			System.exit(0);
		}
	}
}
