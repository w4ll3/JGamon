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

package jgam.help;

import jgam.JGammon;
import jgam.gui.JGamFrame;
import jgam.util.ActionManager;
import jgam.util.ExceptionDialog;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * A HelpWindow with a content + index frame on the left and a content window on
 * the right
 *
 * @author Mattias Ulbrich
 */
public class HelpFrame extends JFrame implements HyperlinkListener {

	static ResourceBundle msg = JGammon.getResources("jgam.msg.Help");

	private static HelpFrame theOneAndOnly;

	private BorderLayout borderLayout1 = new BorderLayout();

	private JSplitPane jSplitPane1 = new JSplitPane();

	private JTabbedPane jTabbedPane1 = new JTabbedPane();

	private JPanel jPanel1 = new JPanel();

	private JPanel jPanel2 = new JPanel();

	private ContentTree topicTree;

	private BorderLayout borderLayout2 = new BorderLayout();

	private JTextField search = new JTextField();

	private BorderLayout borderLayout3 = new BorderLayout();

	private JScrollPane jScrollPane1 = new JScrollPane();

	private JList jList1 = new JList();

	private JScrollPane jScrollPane2 = new JScrollPane();

	private JToolBar jToolBar1 = new JToolBar();

	private JButton buttonBack = new JButton();

	private JButton buttonClose = new JButton();

	private JScrollPane jScrollPane3 = new JScrollPane();

	private JEditorPane browser = new JEditorPane();

	private JButton buttonHome = new JButton();

	private ImageIcon backIcon = getIcon("historyback.gif");

	private ImageIcon forwardIcon = getIcon("historyforward.gif");

	private ImageIcon homeIcon = getIcon("home.png");

	private ImageIcon browserIcon = getIcon("world.png");

	private ImageIcon closeIcon = getIcon("exit.png");

	private JButton buttonForward = new JButton();

	private Component component1 = Box.createHorizontalStrut(12);

	private JButton buttonSetup = new JButton();

	private ActionManager actionManager = new ActionManager();

	private HelpFrame() throws Exception {
		jbInit();
		home();
	}

	private void jbInit() throws Exception {
		setTitle(msg.getString("title"));
		topicTree = new ContentTree(this);
		browser.addHyperlinkListener(this);
		getContentPane().setLayout(borderLayout1);
		jSplitPane1.setOneTouchExpandable(true);
		jPanel1.setLayout(borderLayout2);
		jPanel2.setLayout(borderLayout3);
		buttonBack.setToolTipText(msg.getString("back"));
		buttonBack.setIcon(backIcon);
		buttonBack.setEnabled(false);
		actionManager.add(buttonBack, "back");
		buttonClose.setToolTipText(msg.getString("close"));
		buttonClose.setIcon(closeIcon);
		buttonClose.addActionListener(new HelpFrame_buttonClose_actionAdapter(
				this));
		browser.setEditable(false);
		jScrollPane3.setPreferredSize(new Dimension(500, 350));
		buttonHome.setToolTipText(msg.getString("home"));
		buttonHome.setIcon(homeIcon);
		actionManager.add(buttonHome, "home");
		buttonSetup.setToolTipText(msg.getString("browsersetup"));
		buttonSetup.setIcon(browserIcon);
		buttonSetup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserLauncher.showConfigWindow();
			}
		});

		jPanel2.setEnabled(false);
		buttonForward.setIcon(forwardIcon);
		actionManager.add(buttonForward, "forward");
		buttonForward.setActionCommand("forward");
		buttonForward.setEnabled(false);
		buttonForward.setToolTipText(msg.getString("forward"));
		this.getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);
		jScrollPane2.getViewport().add(topicTree);
		jScrollPane1.getViewport().add(jList1);
		jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jPanel2.add(search, java.awt.BorderLayout.NORTH);
		jSplitPane1.add(jScrollPane3, JSplitPane.RIGHT);
		jScrollPane3.getViewport().add(browser);
		jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);
		this.getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);
		jToolBar1.add(buttonBack);
		jToolBar1.add(buttonHome);
		jToolBar1.add(buttonForward);
		jToolBar1.add(component1);
		jToolBar1.add(buttonSetup);
		jToolBar1.add(buttonClose);

		jSplitPane1.add(jTabbedPane1, JSplitPane.LEFT);
		jTabbedPane1.add(jPanel1, msg.getString("toc"));
		jTabbedPane1.add(jPanel2, msg.getString("index"));
		// TODO not yet supported in 1.1
		jTabbedPane1.setEnabledAt(1, false);

		actionManager.subscribeHandler("back", topicTree);
		actionManager.subscribeHandler("home", topicTree);
		actionManager.subscribeHandler("forward", topicTree);

        /* set style sheet! */
		StyleSheet css = new StyleSheet();
		InputStreamReader r = new InputStreamReader(getClass()
				.getResourceAsStream("styles.css"));
		css.loadRules(r, null);
		HTMLEditorKit kit = new HTMLEditorKit();
		kit.setStyleSheet(css);
		browser.setEditorKit(kit);
	}

	public static void main(String args[]) {
		HelpFrame f = HelpFrame.theFrame();
		if (f != null) {
			f.pack();
			f.setVisible(true);
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String desc = event.getDescription();
			if (desc.startsWith("page:")) {
				topicTree.selectPage(desc.substring(5));
			} else {
				BrowserLauncher.launch(event.getURL());
			}
		}
	}

	public void showPage(String page) throws IOException {
		URL url = getClass().getResource(page);
		browser.setPage(url);
	}

	public static HelpFrame theFrame() {
		try {
			if (theOneAndOnly == null) {
				theOneAndOnly = new HelpFrame();
			}
			return theOneAndOnly;
		} catch (Exception ex) {
			ExceptionDialog.show(ex);
			return null;
		}
	}

	public void close() {
		setVisible(false);
	}

	/**
	 * select the top item in the tree. the selection listener changes the
	 * address.
	 */
	public void home() {
		topicTree.setSelectionInterval(0, 0);
	}

	/**
	 * getIcon
	 *
	 * @param string String
	 * @return ImageIcon
	 */
	private static ImageIcon getIcon(String string) throws IOException {
		try {
			return new ImageIcon(JGamFrame.class.getResource("/jgam/img/"
					+ string));
		} catch (NullPointerException ex) {
			throw new FileNotFoundException("Resource " + string + " not found");
		}
	}

	public ActionManager getActionManager() {
		return actionManager;
	}

}

class HelpFrame_buttonClose_actionAdapter implements ActionListener {
	private HelpFrame adaptee;

	HelpFrame_buttonClose_actionAdapter(HelpFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.close();
	}
}
