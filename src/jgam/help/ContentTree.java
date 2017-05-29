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

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * A JTree that is used in a HelpFrame-Context.
 * <p>
 * A hierachy of help topics is loaded in a treefrom and each node can be
 * selected and is subsequently loaded into the HelpFrame-Content-Area.
 * <p>
 * The tree-structure is read from a file.
 * This is locale-dependent. The filename is stored in the resource "jgam.msg.Help->tocFile"
 * <p>
 * The tree structure is represented by leading spaces. For instance:
 * <p>
 * A.html textA
 * B.html#topic1 textB
 * C.html textC contains spaces which is ok
 * D.htm textD
 * E.html textE
 * F.txt textF
 * <p>
 * First token is the resource to be loaded, second and following are the
 * description.
 * <p>
 * Launched pages are kept in a Stack for history.
 *
 * @author Mattias Ulbrich
 */
public class ContentTree extends JTree implements TreeSelectionListener, ActionListener {

	private ResourceBundle msg = jgam.JGammon.getResources("jgam.msg.Help");
	private HelpFrame helpFrame;
	private LinkedList pageHistory = new LinkedList();

	// the page to select when going home
	private String homePage;
	private int posInHistory = 0;
	private static final int MAXLEVEL = 4;

	public ContentTree(HelpFrame helpFrame) throws IOException {
		super();
		this.helpFrame = helpFrame;
		setModel(new DefaultTreeModel(readin()));
		addTreeSelectionListener(this);
		selectPage(homePage);
	}

	private DefaultMutableTreeNode readin() throws IOException {
		DefaultMutableTreeNode levels[] = new DefaultMutableTreeNode[MAXLEVEL];
		InputStream is = getClass().getResourceAsStream(msg.getString("tocFile"));
		if (is == null)
			throw new IOException("table of contents not found");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		while (br.ready()) {
			String line = br.readLine();
			int level = countInitialSpaces(line);
			Item item = new Item(line.trim());
			DefaultMutableTreeNode tn = new DefaultMutableTreeNode(item);
			if (level != 0) {
				levels[level - 1].add(tn);
			}
			levels[level] = tn;
			if (homePage == null)
				homePage = item.page;
		}

		return levels[0];
	}


	private int countInitialSpaces(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != ' ') {
				return i;
			}
		}
		return string.length();
	}

	public boolean selectPage(String page) {
		Object root = getModel().getRoot();
		TreePath path = new TreePath(root);
		return selectPage(page, path);
	}

	private boolean selectPage(String page, TreePath path) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) path.getLastPathComponent();
		Item item = (Item) root.getUserObject();
		if (item.page.equals(page)) {
			setSelectionPath(path);
			return true;
		}

		for (int i = 0; i < root.getChildCount(); i++) {
			Object child = root.getChildAt(i);
			TreePath p = path.pathByAddingChild(child);
			if (selectPage(page, p))
				return true;
		}

		return false;
	}


	/**
	 * TreeSelectionListener must implement this.
	 * Show the selected page.
	 *
	 * @param e TreeSelectionEvent
	 */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				getLastSelectedPathComponent();
		if (node == null)
			return;

		Item item = (Item) node.getUserObject();
		try {
			helpFrame.showPage(item.page);
			addToHistory(item.page);
		} catch (IOException ex) {
			System.err.println("Could not load page: " + item.page);
			try {
				helpFrame.showPage(msg.getString("invalid"));
			} catch (IOException ex1) {
				System.err.println("Could not load error page");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * An item in the tree.
	 * Read from a text file line
	 * <page> <space> <Description>
	 */
	class Item {
		String desc;
		String page;

		Item(String line) {
			int firstspace = line.indexOf(' ');
			page = line.substring(0, firstspace);
			desc = line.substring(firstspace + 1);
		}

		public String toString() {
			return desc;
		}
	}

	/*
	 * react to back/forward/home
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.equals("back")) {
			if (posInHistory != 0) {
				removeTreeSelectionListener(this);
				String page = (String) pageHistory.get(--posInHistory);
				selectPage(page);
				try {
					helpFrame.showPage(page);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				addTreeSelectionListener(this);
			}
		} else if (com.equals("forward")) {
			if (posInHistory != pageHistory.size() - 1) {
				removeTreeSelectionListener(this);
				String page = (String) pageHistory.get(++posInHistory);
				selectPage(page);
				try {
					helpFrame.showPage(page);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				addTreeSelectionListener(this);
			}
		} else if (com.equals("home")) {
			selectPage(homePage);
		}

		if (posInHistory == 0) {
			helpFrame.getActionManager().disable("back");
		} else {
			helpFrame.getActionManager().enable("back");
		}

		if (posInHistory == pageHistory.size() - 1) {
			helpFrame.getActionManager().disable("forward");
		} else {
			helpFrame.getActionManager().enable("forward");
		}

	}

	private void addToHistory(String page) {
		while (pageHistory.size() > posInHistory + 1)
			pageHistory.removeLast();

		pageHistory.add(page);
		posInHistory++;
		helpFrame.getActionManager().enable("back");
		helpFrame.getActionManager().disable("forward");
	}
}
