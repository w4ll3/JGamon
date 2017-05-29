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

package jgam;

import jgam.ai.AI;
import jgam.ai.AIPlayer;
import jgam.board.BoardComponent;
import jgam.board.MonochromeBoardImplementation;
import jgam.board.ResourceBoardImplementation;
import jgam.game.BoardSetup;
import jgam.game.Game;
import jgam.game.LocalDiceRoller;
import jgam.game.Player;
import jgam.gui.BoardFileView;
import jgam.gui.JGamFrame;
import jgam.gui.JGamSplashScreen;
import jgam.gui.NewGameDialog;
import jgam.net.GameConnection;
import jgam.util.DebugPropertyResourceBundle;
import jgam.util.ExceptionDialog;
import jgam.util.JobThread;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;

/**
 * The application Main-class.
 * <p>
 * It is a singleton, only one instance exists and this can be reached from
 * everywhere by JGammon.jgammon()
 * <p>
 * The main()-method checks for commandline arguments and creates the singleton
 * object.
 *
 * @author Mattias Ulbrich
 */
public class JGammon implements ActionListener {

	public final static String VERSION = Version.VERSION;

	private JGamFrame jGamFrame;

	private NewGameDialog newGameDialog;

	private Game game;

	private GameConnection gameConnection;

	private static JGammon theJGammon;

	// Thread for all purposes.
	public static JobThread jobThread = new JobThread(true);

	// Log the dice with this:
	private PrintWriter diceLogger;

	private ResourceBundle msg = JGammon.getResources("jgam.msg.JGam");

	/**
	 * Construct and show the application.
	 */
	private JGammon() {
		jGamFrame = new JGamFrame(this);
		newGameDialog = new NewGameDialog(this);
		jGamFrame.setLocationRelativeTo(null);
		jGamFrame.setVisible(true);

		String logfile = System.getProperty("jgam.dicelogfile");
		if (logfile != null) {
			try {
				diceLogger = new PrintWriter(new FileWriter(logfile, true));
			} catch (IOException ex) {
				System.err.println("can't open dicelog " + logfile + ": " + ex);
			}
		}

	}

	/**
	 * Application entry point.
	 *
	 * @param args String[]
	 */
	public static void main(String[] args) throws Exception {

		try {
			System.getProperties().load(new FileInputStream("jgam.properties"));
		} catch (Exception ex) {
		} // egal

		// we are still beta. ... better log ...
		// System.getProperties().setProperty("jgam.debug", "true");

		JGamSplashScreen splash = null;
		try {
			if (System.getProperty("jgam.splashscreen", "true").equals("true")) {
				splash = new JGamSplashScreen();
				splash.show(5000);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String mode = null;
		String port = null;
		String server = null;
		String name1 = null;
		String name2 = null;
		String board = null;
		int design = 0;

		int cur = 0;
		try {
			for (cur = 0; cur < args.length; cur++) {
				if (args[cur].equalsIgnoreCase("-server")) {
					mode = "server";
					port = args[++cur];
				} else if (args[cur].equalsIgnoreCase("-connect")) {
					mode = "client";
					String S[] = args[++cur].split(":");
					server = S[0];
					port = S[1];
				} else if (args[cur].equalsIgnoreCase("-local")) {
					mode = "local";
				} else if (args[cur].equalsIgnoreCase("-name")) {
					if (name1 != null) {
						name2 = args[++cur];
					} else {
						name1 = args[++cur];
					}
				} else if (args[cur].equalsIgnoreCase("-ai")) {
					mode = "ai";
				} else if (args[cur].equalsIgnoreCase("-ai")) {
					mode = "demo";
				} else if (args[cur].equalsIgnoreCase("-board")) {
					board = args[++cur];
				} else if (args[cur].equalsIgnoreCase("-help")) {
					help();
					System.exit(0);
				} else if (args[cur].equalsIgnoreCase("-version")) {
					System.out.println("JGammon Version " + VERSION + "-"
							+ Version.BUILD_NUMBER + "; build date: "
							+ Version.BUILD_DATE);
					System.exit(0);
				} else if (args[cur].equalsIgnoreCase("-design")) {
					design = Integer.parseInt(args[++cur]);

				} else {
					System.err.println("Unknown commandline option: "
							+ args[cur]);
					System.err.println("Try \"-help\"");
					System.exit(0);
				}
			}
		} catch (IndexOutOfBoundsException ex) {
			System.err.println("Some options need an argument! Try -help");
			System.exit(0);
		}

		theJGammon = new JGammon();

		// bring the splash back to front
		if (splash != null) {
			splash.setVisible(false);
			splash.setVisible(true);
			splash.allowCloseClick();
		}

		if (design != 0) {
			theJGammon.getFrame().setDesignIndex(design - 1);
		}

		if (mode != null && !mode.equals("demo")) {
			theJGammon.newGameDialog.feed(mode, server, port, name1, name2, board);
			theJGammon.handle("newgame");
		}

		if (mode != null && mode.equals("demo")) {
			theJGammon.demoMode();
		}

	}

	private static void help() {
		System.out.println("Command line parameters:\n");
		System.out
				.println("  -connect <server>:<port>        starts connecting to a server at a port");
		System.out
				.println("  -server <port>                  starts as serverlistening to port");
		System.out
				.println("  -local                          starts a local game");
		System.out
				.println("  -demo                           play a demo game");
		System.out
				.println("  -ai                             play a game against the artifical intelligence");
		System.out
				.println("  -name                           provide a name for a player (2 can be specified)");
		System.out
				.println("  -board <file>                   sets the board file to be loaded");
		System.out
				.println("  -design <number>                the index of the desired design in the menu order (starting with 1)");
		System.out
				.println("\nAfter the main window is launched, press OK to launch the game.\n");
	}

	/**
	 * return the VERSION plus some information about enabled features
	 *
	 * @return String extended Version
	 */
	public static String getExtVersion() {
		String ret = VERSION;
		if (Boolean.getBoolean("jgam.manualdice")) {
			ret += " MD";
		}
		if (System.getProperty("jgam.initialboard") != null) {
			ret += " modified-initial-board";
		}
		if (Boolean.getBoolean("jgam.unsecuredice")) {
			ret += " UD";
		}
		return ret;
	}

	/**
	 * one of the buttons has been pressed. Handle it appropriately.
	 *
	 * @param e ActionEvent telling which button it is
	 */
	public void actionPerformed(ActionEvent e) {
		handle(e.getActionCommand());
	}

	public void handle(String command) {
		if (Boolean.getBoolean("jgam.debug")) {
			System.out.println("JGammon.handle called for " + command);
		}

		switch (command) {
			case "newgame":
				jobThread.add(this::startNewGame);
				break;
			case "demo":
				demoMode();
				break;
			case "saveboard":
				saveBoard();
				break;
			case "close":
				if (game != null && game.isRunning()) {
					if (JOptionPane.showConfirmDialog(getFrame(), msg
									.getString("gameabort"), msg.getString("quit"),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						exit(0);
					}
				} else {
					exit(0);
				}
				break;
			case "help":
				jgam.help.HelpFrame.main(null);
				break;
			case "chat":
				if (gameConnection != null)
					gameConnection.openChatWindow();
				break;
		}
	}

	/**
	 * things to be done at the end
	 */
	private void exit(int i) {
		clearGame();
		System.exit(i);
	}

	/**
	 * show a dialog box to start a new game and perhaps start it.
	 */
	private void startNewGame() {
		if (game != null && game.isRunning()) {
			if (JOptionPane.showConfirmDialog(getFrame(), msg
							.getString("gameabort"), msg.getString("newgame"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
		}

		if (newGameDialog.showAndEval()) {
			if (newGameDialog.getSimulation() != null) {
				clearGame();
				game = newGameDialog.getGame();
				game.startSimulation(newGameDialog.getSimulation());
				getFrame().setGame(true);
			} else {
				clearGame();
				game = newGameDialog.getGame();
				gameConnection = newGameDialog.getGameConnection();
				game.start();
				getFrame().setGame(true);
			}
		}
	}

	private JDialog progressDialog;
	private Thread progress;
	private JProgressBar progressBar;
	private BackgroundWorker backgroundWorker;

	public void startProgress(int[] count, int[] i) {
		backgroundWorker = new BackgroundWorker(count, i);
		progress = new Thread(() -> JGammon.this.backgroundWorker.execute());
		progress.start();
	}

	public void setProgress(int i, int max) {
		if (progressBar != null) progressBar.setValue((int) Math.min(Math.max((100.0 * i) / max, 0), 99));
		else System.out.println("NO PROGRESS BAR HERE");
	}

	public void disposeProgress() {
		progressDialog.dispose();
		progress.interrupt();
	}

	public class BackgroundWorker extends SwingWorker<Void, Void> {
		private JProgressBar pb;
		private JDialog dialog;
		private final int[] total;
		private final int[] counter;

		public BackgroundWorker(int[] total, int[] counter) {
			this.total = total;
			this.counter = counter;

			this.dialog = new JDialog(JGammon.this.getFrame(), true);
			this.dialog.setTitle("Simulating");
			this.dialog.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.weightx = 1;
			gbc.gridy = 0;
			this.dialog.add(new JLabel("Processing..."), gbc);
			pb = new JProgressBar();
			gbc.gridy = 1;
			this.dialog.add(pb, gbc);
			this.dialog.pack();
			this.dialog.setLocationRelativeTo(null);
			this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			this.dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (game != null && game.isRunning()) {
						if (JOptionPane.showConfirmDialog(getFrame(), msg.getString("gameabort"), msg.getString("newgame"),
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							game.abortGame();
							dialog.dispose();
						}
					}
				}
			});

			this.pb.setMinimum(0);
			this.pb.setMaximum(98);
			JGammon.this.progressBar = this.pb;
			JGammon.this.progressDialog = this.dialog;
		}

		@Override
		protected Void doInBackground() throws Exception {
			this.dialog.setVisible(true);
			if (counter != null && total != null)
				while (counter[0] < total[0])
					pb.setValue((int) Math.min(Math.max((100.0 * counter[0]) / total[0], 0), 99));
			this.dialog.dispose();
			return null;
		}
	}

	/**
	 * show a dialog and start a demo game
	 */
	private void demoMode() {
		if (game != null && game.isRunning()) {
			if (JOptionPane.showConfirmDialog(getFrame(), msg
							.getString("gameabort"), msg.getString("newgame"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			clearGame();
			Class aiClass = Class.forName(System.getProperty("jgam.demoai1",
					"jgam.ai.InitialAI"));
			Player p1 = new AIPlayer((AI) aiClass.newInstance());
			aiClass = Class.forName(System.getProperty("jgam.demoai2",
					"jgam.ai.InitialAI"));
			Player p2 = new AIPlayer((AI) aiClass.newInstance());
			game = new Game(new LocalDiceRoller(), p1, p2, this);
			gameConnection = null;
			game.start();
			getFrame().setGame(true);
		} catch (Exception ex) {
			ExceptionDialog.show(ex);
			ex.printStackTrace();
		}
	}

	/**
	 * show a saveAs Dialog for the current game.
	 * <p>
	 * This method is not synchronized with the interactionLock.
	 */
	public void saveBoard() {
		if (game == null) {
			return;
		}

		BoardSetup snapshot = game.getGameBoard();
		if (snapshot == null) {
			return;
		}

		JFileChooser jfc = new JFileChooser();
		jfc.addChoosableFileFilter(boardFileFilter);
		jfc.setAccessory(new BoardFileView(jfc));

		while (true) {
			if (jfc.showSaveDialog(getFrame()) != jfc.APPROVE_OPTION) {
				return;
			}

			try {
				File f = jfc.getSelectedFile();
				if (f.exists()) {
					switch (JOptionPane.showConfirmDialog(getFrame(), msg
									.getString("overwrite"),
							msg.getString("saveboard"),
							JOptionPane.YES_NO_CANCEL_OPTION)) {
						case JOptionPane.CANCEL_OPTION:
							return;
						case JOptionPane.YES_OPTION:
							String comment = askComment();
							FileBoardSetup.saveBoardSetup(snapshot, f, comment);
							return;
					}
				} else {
					if (jfc.getFileFilter() == boardFileFilter
							&& !f.getName().toLowerCase().endsWith(".board")) {
						f = new File(f.getPath() + ".board");
					}
					String comment = askComment();
					FileBoardSetup.saveBoardSetup(snapshot, f, comment);
					return;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(getFrame(), ex, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static JGammon jgammon() {
		return theJGammon;
	}

	public Game getGame() {
		return game;
	}

	/**
	 * get the board of this session or null if no board or no frame
	 */
	public BoardComponent getBoard() {
		JGamFrame f = getFrame();
		if (f == null) {
			return null;
		}

		return f.getBoard();
	}

	public JGamFrame getFrame() {
		return jGamFrame;
	}

	public GameConnection getGameConnection() {
		return gameConnection;
	}

	public PrintWriter getDiceLogger() {
		return diceLogger;
	}

	public void clearGame() {
		if (game != null) {
			game.abortGame();
		}

		if (gameConnection != null) {
			gameConnection.close("game has been cleared");
		}

		game = null;
		gameConnection = null;
		jGamFrame.setLabel("");
		jGamFrame.setIcon(null);
		jGamFrame.repaint();
		jGamFrame.setGame(false);
		jGamFrame.getBoard().useBoardSetup(null);
	}

	private String askComment() {
		String answer = JOptionPane.showInputDialog(getFrame(), msg
				.getString("inputcomment"), "");
		/**
		 * @todo find some better solution to the empty comment problem - how
		 *       about "..." in general?
		 */
		if (answer.trim().length() == 0) {
			answer = "-";
		}
		return answer;
	}

	private FileFilter boardFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return (pathname.getName().toLowerCase().endsWith(".board") || pathname
					.isDirectory());
		}

		public String getDescription() {
			return msg.getString("boardFilter");
		}
	};

	/**
	 * this array stores the available "BoardImplementation"s. These are those
	 * known to the system and those provided in the property
	 * "jgam.boardImplementation", separated by comma. This array stores
	 * instances of the the implementations.
	 */
	private static List boardImplementations;

	/**
	 * look for resources with the path
	 * "META-INF/services/jgam.board.BoardImplementation".
	 * <p>
	 * Read them line by line and either instanciate a class or create a
	 * ResourceBoardImplementation.
	 */
	private static void initBoardImplementations() throws Exception {
		Enumeration urls = JGammon.getExtClassLoader().getResources(
				"META-INF/services/jgam.board.BoardImplementation");
		boardImplementations = new ArrayList();
		while (urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			BufferedReader is = new BufferedReader(new InputStreamReader(url
					.openStream()));
			while (is.ready()) {
				String line = is.readLine().trim();

				// empty lines or comments.
				if (line.length() == 0 || line.startsWith("#")) {
					continue;
				}

				try {
					boardImplementations.add(Class.forName(line).newInstance());
				} catch (ClassNotFoundException ex) {
					try {
						boardImplementations
								.add(new ResourceBoardImplementation(line));
					} catch (Exception ex1) {
						System.err
								.println("Neither a class nor a properties-file can be found for: "
										+ line);
						ex.printStackTrace();
						ex1.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * return a list of all known BoardImplementations.
	 *
	 * @return a list of BoardImplementations
	 */
	public static List getBoardImplementations() {
		if (boardImplementations == null) {
			try {
				initBoardImplementations();
			} catch (Exception ex) {
				System.err
						.println("An error occured during reading the BoardImplementations");
				ex.printStackTrace();
				boardImplementations = Collections
						.singletonList(new MonochromeBoardImplementation());
			}
		}
		return Collections.unmodifiableList(JGammon.boardImplementations);
	}

	private static ClassLoader extClassLoader = null;

	public static ClassLoader getExtClassLoader() {
		if (extClassLoader == null) {
			try {
				File extDir = new File("ext");
				if (extDir.isDirectory()) {
					File[] jars = extDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
					URL[] urls = new URL[jars.length];

					for (int i = 0; i < urls.length; i++) {
						urls[i] = jars[i].toURL();
					}
					extClassLoader = new URLClassLoader(urls, JGammon.class.getClassLoader());
				} else
					extClassLoader = JGammon.class.getClassLoader();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return extClassLoader;
	}

	/**
	 * provide a ResourceBundle for the desired resources.
	 * <p>
	 * If "jgam.debuglanguage" is set, then a DebugPropertyResourceBundle will
	 * be used (indicating the missing entries), otherwise the normal way will
	 * be gone.
	 *
	 * @param resource to be be looked up
	 * @return ResourceBundle as returned by ResourceBundle.getBundle
	 */
	public static ResourceBundle getResources(String resource) {
		String debuglanguage = System.getProperty("jgam.debuglanguage");
		if (debuglanguage == null) {
			return ResourceBundle.getBundle(resource);
		} else if (debuglanguage.equals("en")) {
			return new DebugPropertyResourceBundle(resource);
		} else {
			return new DebugPropertyResourceBundle(resource, debuglanguage);
		}
	}
}
