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

package jgam.net;

import jgam.JGammon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * A simple chatter window that reacts on special messages send over the
 * connection and sends messages as well.
 *
 * @author Mattias Ulbrich
 */
public class Chatter extends JFrame implements ChannelListener {

	private static ResourceBundle msg = JGammon
			.getResources("jgam.msg.Chatter"); //$NON-NLS-1$

	private BorderLayout borderLayout1 = new BorderLayout();

	private JPanel jPanel2 = new JPanel();

	private Border border1 = BorderFactory.createCompoundBorder(BorderFactory
			.createLineBorder(new Color(199, 199, 207), 3), BorderFactory
			.createEmptyBorder(5, 10, 5, 10));

	private JTextField input = new JTextField();

	private JScrollPane jScrollPane1 = new JScrollPane();

	private Border border3 = BorderFactory.createLineBorder(new Color(199, 199,
			207), 3);

	private JLabel jLabel1 = new JLabel();

	private JButton send = new JButton();

	private JTextPane output = new JTextPane();

	private String remoteName, localName;

	private Writer writer;

	private NewMessageWindow messageWindow;

	public Chatter(String partner, String myName, Writer writer) {
		super(msg.getString("chatwith") + partner); //$NON-NLS-1$
		this.remoteName = partner;
		this.localName = myName;
		this.writer = writer;
		try {
			jbInit();
			pack();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		getContentPane().setLayout(borderLayout1);
		jScrollPane1.setBorder(border3);
		// input.setPreferredSize(new Dimension(220, 20));
		jLabel1.setText(msg.getString("message")); //$NON-NLS-1$
		send.setText(msg.getString("send")); //$NON-NLS-1$
		getRootPane().setDefaultButton(send);
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		jScrollPane1.setPreferredSize(new Dimension(350, 200));
		output.setEditable(false);
		this.getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);
		jPanel2.setBorder(border1);
		BorderLayout bl = new BorderLayout(10, 10);
		jPanel2.setLayout(bl);
		jPanel2.add(jLabel1, BorderLayout.WEST);
		jPanel2.add(input, BorderLayout.CENTER);
		jPanel2.add(send, BorderLayout.EAST);
		this.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jScrollPane1.getViewport().add(output);
	}

	public void receiveChannelMessage(int channel, String message) {

		if (channel != JGammonConnection.CHAT_CHANNEL) {
			return;
		}

		addMsg(message, true);
		if (!isVisible())
			newMessageWindow(message);
		else
			toFront();

	}

	public void newMessageWindow(String message) {
		if (messageWindow == null) {
			messageWindow = new NewMessageWindow(this, JGammon.jgammon()
					.getFrame());
		}
		messageWindow.show(message);
	}

	public void send() {

		String msg = input.getText();

		if (msg.length() == 0) {
			return;
		}

		try {
			writer.write(msg + "\n"); //$NON-NLS-1$
			writer.flush();
			addMsg(msg, false);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		input.setText(""); //$NON-NLS-1$

	}

	public void addMsg(final String message, boolean remote) {

		// insert text in the editor
		final SimpleAttributeSet norm = new SimpleAttributeSet();
		if (remote) {
			StyleConstants.setForeground(norm, Color.blue);
		}
		SimpleAttributeSet bold = new SimpleAttributeSet(norm);
		StyleConstants.setBold(bold, true);
		final Document d = output.getStyledDocument();

		final DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);

		final String name = remote ? remoteName : localName;

        /*
         * bugfix: calling this function with the lock for this object causes
         * a deadlock. Instead: synchronize only the insert part with the document.
         */
		try {
			synchronized (d) {
				d.insertString(d.getLength(), name
								+ " (" + df.format(new Date()) + "): ", //$NON-NLS-1$ //$NON-NLS-2$
						norm);
				d.insertString(d.getLength(), message + "\n", norm); //$NON-NLS-1$
			}
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		final Dimension size = output.getSize();
		output.scrollRectToVisible(new Rectangle(0, size.height - 1, 1,
				size.height));
	}

}

/**
 * In order to announnce a new message pop up a tooltip like frameless window.
 */
class NewMessageWindow extends Window {
	ResourceBundle msg = JGammon.getResources("jgam.msg.Chatter"); //$NON-NLS-1$

	JLabel label = new JLabel();

	Chatter chatter;

	NewMessageWindow(Chatter chatter, Frame parent) {
		super(parent);
		this.chatter = chatter;
		JPanel p = new JPanel(new GridLayout(3, 1));
		p.add(new JLabel(msg.getString("newchat"))); //$NON-NLS-1$
		p.add(label);
		{
			JLabel clickhere = new JLabel(msg.getString("clickhere"));
			clickhere.setForeground(Color.blue);
			clickhere.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					NewMessageWindow.this.chatter.setVisible(true);
					setVisible(false);
				}
			});
			p.add(clickhere);
		}
		p.setBackground(UIManager.getColor("ToolTip.background")); //$NON-NLS-1$
		p.setBorder(BorderFactory.createCompoundBorder(BorderFactory
						.createLineBorder(UIManager.getColor("ToolTip.foreround"), 1), //$NON-NLS-1$
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		p.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				setVisible(false);
			}
		});
		add(p);
	}

	public void show(String message) {
		if (message.length() > 80)
			message = message.substring(0, 79) + "...";
		label.setText(message);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}
}
