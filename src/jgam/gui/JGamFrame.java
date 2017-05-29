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
import jgam.board.BoardImplementation;
import jgam.board.BoardMouseListener;
import jgam.game.Game;
import jgam.game.Player;
import jgam.util.ActionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * This is the MainWindow-class.
 * <p>
 * On top it has got several Buttons and a message area. In the center lies the
 * board (a JComponent). There is also a menubar.
 * <p>
 * Buttons' actions are partly handled by JGam and partly by this class.
 *
 * @author Mattias Ulbrich
 */
public class JGamFrame extends JFrame implements ActionListener {
	private JPanel contentPane;

	private BorderLayout borderLayout1 = new BorderLayout();

	private JButton buttonNew = new JButton();

	private JButton buttonGiveUp = new JButton();

	private JButton buttonDouble = new JButton();

	private JButton buttonRoll = new JButton();

	private JLabel label = new JLabel();

	private BoardComponent board;

	private JGammon jGam;

	private JButton buttonTurn = new JButton();

	private JButton buttonFlip = new JButton();

	private JButton buttonHistory = new JButton();

	private JButton buttonUndo = new JButton();

	private JButton buttonChat = new JButton();

	private JButton buttonHelp = new JButton();

	private JButton buttonSave = new JButton();

	private JMenuBar jMenuBar = new JMenuBar();

	private JMenu jMenuGame = new JMenu();

	private JMenuItem jMenuItemNew = new JMenuItem();

	private JMenuItem jMenuItemAbort = new JMenuItem();

	private JMenuItem jMenuItemSave = new JMenuItem();

	private JMenu jMenuWindow = new JMenu();

	private JMenuItem jMenuItemHistory = new JMenuItem();

	private JMenuItem jMenuItemChat = new JMenuItem();

	private JMenu jMenuHelp = new JMenu();

	private JMenuItem jMenuItemHelp = new JMenuItem();

	private JMenuItem jMenuAbout = new JMenuItem();

	private JMenu jMenuDesign = new JMenu();

	private JMenuItem jMenuItemTurn = new JMenuItem();

	private JMenuItem jMenuItemFlip = new JMenuItem();

	private JToolBar jToolBarTop = new JToolBar();

	private JMenuItem jMenuItemUndo = new JMenuItem();

	private JMenuItem jMenuItemExit = new JMenuItem();

	private ButtonGroup buttonGroupDesigns = new ButtonGroup();

	private Component component1 = Box.createHorizontalStrut(12);

	private Component component2 = Box.createHorizontalStrut(12);

	private Component component3 = Box.createHorizontalStrut(12);

	private JMenuItem jMenuItemColorSwap = new JMenuItem();

	private Component component4 = Box.createHorizontalStrut(12);

	private JButton buttonSwap = new JButton();

	private JCheckBoxMenuItem jMenuEnableResizing = new JCheckBoxMenuItem();

	private JCheckBoxMenuItem jMenuShowPreviewCheckers = new JCheckBoxMenuItem();

	private JMenuItem jMenuItemDemo = new JMenuItem();

	private Player labelPlayer = null;

	private ImageIcon imageDice = icon("dice.gif");

	private ImageIcon turnIcon = icon("turn.gif");

	private ImageIcon flipIcon = icon("flip.gif");

	private ImageIcon msgIcon = icon("msg.gif");

	// private ImageIcon newMsgIcon = icon("newmsg.gif");
	private ImageIcon saveIcon = icon("save.gif");

	private ImageIcon newIcon = icon("go.gif");

	private ImageIcon whiteFlagIcon = icon("whiteflag.gif");

	private ImageIcon x2Icon = icon("x2.gif");

	private ImageIcon undoIcon = icon("undo.gif");

	private ImageIcon helpIcon = icon("help.gif");

	private ImageIcon historyIcon = icon("book.gif");

	private ImageIcon swapColorIcon = icon("swapcolors.gif");

	public static ImageIcon smallClock = icon("smallclock.gif");

	private ActionManager actionManager = new ActionManager();

	ResourceBundle msg = JGammon.getResources("jgam.msg.JGamFrame");

	private int designIndex = 0;

	public JGamFrame(JGammon jgam) {
		jGam = jgam;
		board = new BoardComponent(getMenuBoardImplementation(), null);
		new BoardMouseListener(board, jGam);
		try {
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			// setResizable(false);
			jbInit();
			subscribeActionManager();
			setGame(false);
			initDesignMenu();
			setPropertyDimension();
			pack();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * initDesignMenu
	 */
	private void initDesignMenu() {
		Iterator it = jGam.getBoardImplementations().iterator();
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				designChanged(e);
			}
		};

		int count = 0;
		while (it.hasNext()) {
			BoardImplementation bi = (BoardImplementation) it.next();
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(bi.toString());
			jMenuDesign.add(item);
			buttonGroupDesigns.add(item);
			// only select first!
			item.setSelected(count == 0);
			item.setActionCommand(Integer.toString(count));
			item.addItemListener(itemListener);
			count++;
		}

		jMenuDesign.addSeparator();
		jMenuDesign.add(jMenuShowPreviewCheckers);
		jMenuDesign.add(jMenuEnableResizing);
	}

	/**
	 * if in the properties-file the property "jgam.dimension" is set to a
	 * string like "111x2222"
	 * <p>
	 * the dimension of the BoardComponent will be set. Otherwise standard will
	 * be used.
	 */
	private void setPropertyDimension() {
		try {
			String dimstring = System.getProperty("jgam.dimension");
			if (dimstring != null) {
				String sizes[] = dimstring.split("x");
				Dimension d = new Dimension(Integer.parseInt(sizes[0]), Integer
						.parseInt(sizes[1]));
				setSize(d);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private JGamFrame() {
		try {
			jbInit();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void subscribeActionManager() {
		actionManager.subscribeHandler("newgame", jGam);
		actionManager.subscribeHandler("saveboard", jGam);
		actionManager.subscribeHandler("help", jGam);
		actionManager.subscribeHandler("close", jGam);
		actionManager.subscribeHandler("chat", jGam);
		actionManager.subscribeHandler("demo", jGam);
		actionManager.subscribeHandler("turnboard", this);
		actionManager.subscribeHandler("swapcolors", this);
		actionManager.subscribeHandler("flipboard", this);
	}

	/**
	 * Component initialization.
	 *
	 * @throws java.lang.Exception
	 */
	private void jbInit() throws Exception {
		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(borderLayout1);
		this.setJMenuBar(jMenuBar);
		setTitle("JGammon");
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				frameResized(e);
			}
		});
		this.addWindowListener(new JGamFrame_this_windowAdapter(this));
		buttonNew.setToolTipText(msg.getString("newgame"));
		actionManager.add(buttonNew, "newgame");
		buttonNew.setIcon(newIcon);
		actionManager.add(buttonGiveUp, "giveup");
		buttonGiveUp.setIcon(whiteFlagIcon);
		buttonGiveUp.setToolTipText(msg.getString("giveup"));
		actionManager.add(buttonDouble, "double");
		buttonDouble.setIcon(x2Icon);
		buttonDouble.setToolTipText(msg.getString("double"));
		actionManager.add(buttonRoll, "roll");
		buttonRoll.setIcon(imageDice);
		buttonRoll.setText(msg.getString("roll"));
		label.setMaximumSize(new Dimension(10000, 32));
		label.setPreferredSize(new Dimension(182, 25));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setText(msg.getString("welcome") + " JGammon " + JGammon.VERSION);
		buttonTurn.setToolTipText(msg.getString("turnboard"));
		buttonTurn.setIcon(turnIcon);
		buttonFlip.setToolTipText(msg.getString("flipboard"));
		actionManager.add(buttonFlip, "flipboard");
		actionManager.add(buttonTurn, "turnboard");
		buttonFlip.setIcon(flipIcon);
		buttonHistory.setIcon(historyIcon);
		buttonHistory.setToolTipText(msg.getString("history"));
		actionManager.add(buttonHistory, "history");
		actionManager.add(buttonUndo, "undo");
		buttonUndo.setIcon(undoIcon);
		buttonUndo.setToolTipText(msg.getString("undo"));
		actionManager.add(buttonChat, "chat");
		buttonChat.setIcon(msgIcon);
		buttonChat.setToolTipText(msg.getString("chat"));
		actionManager.add(buttonHelp, "help");
		buttonHelp.setIcon(helpIcon);
		buttonHelp.setToolTipText(msg.getString("help"));
		buttonSave.setToolTipText(msg.getString("saveboard"));
		actionManager.add(buttonSave, "saveboard");
		buttonSave.setIcon(saveIcon);
		jMenuGame.setText(msg.getString("game_menu"));
		actionManager.add(jMenuItemNew, "newgame");
		jMenuItemNew.setText(msg.getString("newgame"));
		actionManager.add(jMenuItemDemo, "demo");
		jMenuItemDemo.setText(msg.getString("demo"));
		actionManager.add(jMenuItemAbort, "abort");
		jMenuItemAbort.setText(msg.getString("abort"));
		actionManager.add(jMenuItemSave, "saveboard");
		jMenuItemSave.setText(msg.getString("saveboard"));
		jMenuWindow.setText(msg.getString("window_menu"));
		jMenuItemHistory.setText(msg.getString("history"));
		actionManager.add(jMenuItemHistory, "history");
		actionManager.subscribeHandler("history", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHistory();
			}
		});
		jMenuItemChat.setText(msg.getString("chat"));
		actionManager.add(jMenuItemChat, "chat");
		jMenuHelp.setText(msg.getString("help"));
		jMenuItemHelp.setText(msg.getString("help"));
		actionManager.add(jMenuItemHelp, "help");
		jMenuAbout.setText(msg.getString("about"));
		jMenuAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				about();
			}
		});
		jMenuDesign.setText(msg.getString("design_menu"));
		jMenuItemTurn.setText(msg.getString("turnboard"));
		actionManager.add(jMenuItemTurn, "turnboard");

		jMenuItemFlip.setText(msg.getString("flipboard"));
		actionManager.add(jMenuItemFlip, "flipboard");
		actionManager.add(jMenuItemUndo, "undo");
		jMenuItemUndo.setText(msg.getString("undo"));
		actionManager.add(jMenuItemExit, "close");
		jMenuItemExit.setText(msg.getString("exit"));
		jToolBarTop.setFloatable(true);
		actionManager.add(jMenuItemColorSwap, "swapcolors");
		jMenuItemColorSwap.setText(msg.getString("swapColors"));
		actionManager.add(buttonSwap, "swapcolors");
		buttonSwap.setToolTipText(msg.getString("swapColors"));
		buttonSwap.setIcon(swapColorIcon);
		jMenuShowPreviewCheckers.setText(msg.getString("showpreviewcheckers"));
		jMenuShowPreviewCheckers.setSelected(true);
		jMenuEnableResizing.setText(msg.getString("enableresize"));
		jMenuEnableResizing.setSelected(true);
		jMenuEnableResizing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuEnableResizing_actionPerformed(e);
			}
		});
		jMenuBar.add(jMenuGame);
		jMenuBar.add(jMenuWindow);
		jMenuBar.add(jMenuDesign);
		jMenuBar.add(jMenuHelp);
		jMenuGame.add(jMenuItemNew);
		jMenuGame.add(jMenuItemDemo);
		jMenuGame.add(jMenuItemAbort);
		jMenuGame.add(jMenuItemSave);
		jMenuGame.addSeparator();
		jMenuGame.add(jMenuItemUndo);
		jMenuGame.add(jMenuItemExit);
		jMenuWindow.add(jMenuItemHistory);
		jMenuWindow.add(jMenuItemChat);
		jMenuWindow.addSeparator();
		jMenuWindow.add(jMenuItemTurn);
		jMenuWindow.add(jMenuItemFlip);
		jMenuWindow.add(jMenuItemColorSwap);
		jMenuHelp.add(jMenuItemHelp);
		jMenuHelp.add(jMenuAbout);
		jToolBarTop.add(buttonNew);
		jToolBarTop.add(buttonSave);
		jToolBarTop.add(component3);
		jToolBarTop.add(buttonHistory);
		jToolBarTop.add(buttonTurn);
		jToolBarTop.add(buttonFlip);
		jToolBarTop.add(buttonSwap);
		jToolBarTop.add(buttonChat);
		jToolBarTop.add(component1);
		jToolBarTop.add(buttonGiveUp);
		jToolBarTop.add(buttonUndo);
		jToolBarTop.add(buttonDouble);
		jToolBarTop.add(component2);
		jToolBarTop.add(buttonHelp);
		jToolBarTop.add(component4);
		jToolBarTop.add(label);
		jToolBarTop.add(buttonRoll);
		contentPane.add(board, java.awt.BorderLayout.CENTER);
		contentPane.add(jToolBarTop, java.awt.BorderLayout.NORTH);
		// actionManager.traceButtons();
	}

	public void about() {
		new JGamFrame_AboutBox(this).setVisible(true);
	}

	public void showHistory() {
		if (jGam.getGame() != null) {
			jGam.getGame().getHistory().showUp();
		}
	}


	public void setLabel(String string) {
		label.setText(string);
		label.setToolTipText(string);
		labelPlayer = null;
	}

	/**
	 * set the label to show the "Name's turn (color)" information
	 *
	 * @param player Player to set.
	 */
	public void setLabelPlayer(Player player) {
		labelPlayer = player;
		updateLabel();
	}

	/**
	 * once there is stores a labelPlayer, update the label according to this
	 * player. As the color name or color may change, the label text may change
	 * too! Dont do anything, if the label is not set to a player-notice.
	 */
	private void updateLabel() {
		if (labelPlayer != null) {
			ResourceBundle gameRes = JGammon.getResources("jgam.msg.Player");
			MessageFormat msgFormat = new MessageFormat("");
			msgFormat.applyPattern(gameRes.getString("turn"));
			String color = getBoard().getColorName(labelPlayer.getNumber());
			String M = msgFormat.format(new Object[]{labelPlayer.getName(),
					color});
			label.setText(M);
			label.setToolTipText(M);
			if (labelPlayer != null && !labelPlayer.isLocal()) {
				setIcon(smallClock);
			} else {
				setIcon(board.getCheckerIcon(labelPlayer.getNumber()));
			}
		}
	}

	/**
	 * set the icon for the status label. The icon is scaled down to a height of
	 * 30.
	 *
	 * @param icon ImageIcon to be set or null.
	 */
	public void setIcon(ImageIcon icon) {
		if (icon == null) {
			label.setIcon(null);
		} else {
			if (icon.getIconHeight() > 30) {
				Image smallerImage = icon.getImage().getScaledInstance(-1, 30,
						Image.SCALE_SMOOTH);
				ImageIcon smallerIcon = new ImageIcon(smallerImage);
				label.setIcon(smallerIcon);
			} else {
				label.setIcon(icon);
			}
		}
	}

	public BoardComponent getBoard() {
		return board;
	}

	/**
	 * set the presence of a game. if none present: disable many buttons!
	 *
	 * @param present boolean
	 */
	public void setGame(boolean present) {
		actionManager.setState("history", present);
		actionManager.setState("roll", present);
		actionManager.setState("double", present);
		actionManager.setState("giveup", present);
		actionManager.setState("saveboard", present);
		actionManager.setState("undo", present);
		actionManager.setState("abort", present);
		actionManager.setState("swapcolors", present);
		actionManager.setState("flipboard", present);
		actionManager.setState("turnboard", present);
		actionManager.setState("chat", jGam.getGameConnection() != null);
	}

	public void closed() {
		jGam.handle("close");
	}

	/**
	 * get the BoardImplementation that is currently selected from the available
	 * implementations. This is not an initialized copy!
	 *
	 * @return an instance of the currently selected BoardImplementation,
	 */
	private BoardImplementation getMenuBoardImplementation() {
		try {
			return (BoardImplementation) JGammon.getBoardImplementations().get(
					designIndex);
		} catch (Exception ex) {
			throw new Error("There is no BoardImplementation available!", ex);
		}
	}

	/**
	 * called when the design is changed in the menu
	 *
	 * @param e ItemEvent
	 */
	private void designChanged(ItemEvent e) {
		if (e.getStateChange() == e.SELECTED) {
			JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getItem();
			designIndex = Integer.parseInt(item.getActionCommand());
		}
		BoardImplementation impl = getMenuBoardImplementation();
		board.setBoardImplementation(impl);
		Dimension d = board.getBoardImplementation().getPreferredSize();
		board.setSize(d);
		updateLabel();
		pack();
	}

	/**
	 * set the design by index.
	 * <p>
	 * the corresponding menu entry is selected.
	 *
	 * @param designIndex index to select.
	 */
	public void setDesignIndex(int designIndex) {
		Enumeration en = buttonGroupDesigns.getElements();
		JRadioButtonMenuItem menu = null;
		String indexString = Integer.toString(designIndex);
		while (en.hasMoreElements()) {
			menu = (JRadioButtonMenuItem) en.nextElement();
			if (indexString.equals(menu.getActionCommand())) {
				break;
			}
		}
		menu.setSelected(true);
	}

	/**
	 * create an icon from the given ressource-address.
	 * <p>
	 * The complete ressource is "/jgam/img" + string
	 * <p>
	 * ((Better use ClassLoader.getSystemResource, but JBuilder doesnt like it))
	 *
	 * @param string ressource to be searched
	 * @return found ImageIcon
	 */
	public static ImageIcon icon(String string) {
		return new ImageIcon(JGamFrame.class.getResource("/jgam/img/" + string));
	}

	public void frameResized(ComponentEvent e) {
		board.resizeEvent(e);
		board.setPreferredSize(board.getBoardImplementation().getSize());
		pack();
	}

	public void jMenuEnableResizing_actionPerformed(ActionEvent e) {
		setResizable(jMenuEnableResizing.isSelected());
		if (!isResizable()) {
			Dimension d = board.getBoardImplementation().getPreferredSize();
			board.setSize(d);
			board.setPreferredSize(d);
			pack();
		}
	}

	/**
	 * use this <i>getter</i> method to query the value of the field
	 * <code>actionManager</code>.
	 *
	 * @return the value of <code>actionManager</code>
	 */
	public ActionManager getActionManager() {
		return this.actionManager;
	}

	/**
	 * set the title plus a leading [!] if desired
	 *
	 * @param b boolean
	 */
	public void setActiveTitle(boolean active) {
		setTitle(active ? "[!] JGammon" : "JGammon");
	}

	/**
	 * check whether preview checkers on possible targets are to be painted.
	 *
	 * @return true iff preview is to be shown.
	 */
	public boolean showPreviewCheckers() {
		return jMenuShowPreviewCheckers.isSelected();
	}

	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		Game game = jGam.getGame();

		if (com.equals("swapcolors")) {
			board.toggleSwapColors();
			board.toggleTopBottom();
			updateLabel();
			repaint();
		} else if (com.equals("turnboard")) {
			board.toggleTurn();
		} else if (com.equals("flipboard")) {
			board.toggleLeftRight();
		} else {
			throw new IllegalArgumentException("Unknown command: " + com + ";" + e);
		}

	}

}

class JGamFrame_this_windowAdapter extends WindowAdapter {
	private JGamFrame adaptee;

	JGamFrame_this_windowAdapter(JGamFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.closed();
	}
}