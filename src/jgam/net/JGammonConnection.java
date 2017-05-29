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
import jgam.SecureRoll;
import jgam.game.*;
import jgam.util.AsynchronousWaitingWindow;
import jgam.util.ProtocollingReader;
import jgam.util.ProtocollingWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Use this class to handle connections between jgam-programs.
 * <p>
 * The reader is constantly read (in run())
 * If a message (a line ending with \n) is read
 * all ConnectionListeners are informed
 * if the reader cant read any more (socket error) also
 * <p>
 * One listener is used to feed a pipedReader that only has got the
 * lines that are relevant for the game.
 * <p>
 * <p>
 * Here is a brief description of the begin of the protocol.
 * <pre>
 *
 * This is the server                This is the client
 *
 * SERVUS JGAM - Version Information1
 *                                   SERVUS JGAM - Version Information2
 * YOUPLAY WHITE / BLUE  [protocol 00; no longer in 01]
 * CALLME MyName1
 *                                   CALLME MyName2
 * NEWGAME / BOARDSETUP ...
 *
 * </pre>
 * That is where the game chatter begins.
 * <p>
 * <p>
 * Channels:
 * 0  Errors / Closing / Sysmessages
 * 1  Gametalk - read by JGammonNetPlayer
 * 2  Messages - read by Chatter
 * 3  Secdice - read by SecureRoll
 *
 * @author Mattias Ulbrich
 */
public class JGammonConnection implements GameConnection {

	private Socket socket;
	private BufferedReader reader;
	private Writer writer;

	private BufferedReader secdiceReader;
	private Writer secdiceWriter;

	private String localName;
	private String remoteName;
	private BoardSetup setup;

	private boolean serverMode;
	private String opponentVersion;

	private List channelListeners = new ArrayList();

	private SecureRoll secureDice;

	public static final int SECDICE_CHANNEL = 3;
	public static final int CHAT_CHANNEL = 2;

	private Chatter chatter;


	/**
	 * create a new connection by connecting to a server
	 *
	 * @param servername server
	 * @param portno     Port number
	 */
	public JGammonConnection(String servername, int portno, String localName) throws
			UnknownHostException, IOException, JGamProtocolException, NoSuchAlgorithmException {

		socket = new Socket(servername, portno);
		secureDice = new SecureRoll();
		this.localName = localName;
		serverMode = false;
		init();
	}

	/**
	 * create a server waiting for connection.
	 * <p>
	 * An AsynchronousWaitingWindow may be specified.
	 * If there the abort button is pressed, the connection process will be
	 * stopped!
	 *
	 * @param portno int
	 */
	public JGammonConnection(int portno, String localName, BoardSetup snapshot, AsynchronousWaitingWindow window) throws
			Exception {
		ServerSocket serverSocket = null;
		serverMode = true;
		this.setup = snapshot;
		secureDice = new SecureRoll();
		this.localName = localName;
		try {
			serverSocket = new ServerSocket(portno);
			serverSocket.setSoTimeout(3000);

			final ServerSocket locServerSocket = serverSocket;
			// the following is active waiting and may not happen
			// in the awt event-queue -> start thread!
			while (socket == null && (window == null || !window.buttonPressed())) {
				try {
					socket = locServerSocket.accept();
				} catch (SocketTimeoutException ex) {
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			if (socket == null) {
				throw new InterruptedException(
						"Server interrupted or no connection setup possible");
			}

			serverSocket.close();
			init();
		} catch (Exception ex) {
			if (serverSocket != null) {
				serverSocket.close();
			}
			close(ex.toString());
			throw ex;
		}
	}

	private void init() throws IOException, JGamProtocolException {

		reader = new BufferedReader(new InputStreamReader(socket.
				getInputStream(), "UTF-8"), 1);
		writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

		// protocolling
		if (Boolean.getBoolean("jgam.debug")) {
			writer = new ProtocollingWriter(writer);
			reader = new BufferedReader(new ProtocollingReader(reader), 1); // no buffering!
		}

		writer.write("SERVUS JGAM 04 - This is JGammon Version " +
				JGammon.getExtVersion() + "\n");
		writer.flush();
		opponentVersion = reader.readLine();
		if (!opponentVersion.startsWith("SERVUS JGAM")) {
			close("Wrong header, 'SERVUS JGAM' expected");
			throw new JGamProtocolException("This seems not to be a JGAM-partner");
		}
		System.out.println("Opponent's version: " + opponentVersion);

		negotiateNames();

		if (serverMode) {
			sendSetup();
		} else {
			receiveSetup();
		}

		// secdice - I/O
		secdiceReader = getChannelReader(SECDICE_CHANNEL);
		secdiceWriter = getChannelWriter(SECDICE_CHANNEL);

		// chatter
		chatter = new Chatter(remoteName, localName, getChannelWriter(CHAT_CHANNEL));
		addChannelListener(chatter);

		// start producing thread
		new Thread("JGammonConnection-comm") {
			public void run() {
				runCommunicationThread();
			}
		}.start();
	}

	/**
	 * receiveSnapshot
	 */
	private void receiveSetup() throws IOException, JGamProtocolException {

		String line = reader.readLine();
		if (line.equals("NEWGAME")) {
			setup = BoardSnapshot.INITIAL_SETUP;
		} else if (line.startsWith("SETUPBOARD ")) {
			setup = ArrayBoardSetup.decodeBase64(line.substring(11));
		} else {
			throw new JGamProtocolException("NEWGAME or SETUPBOARD expected, but received: " + line);
		}
	}

	/**
	 * sendSnapshot
	 */
	private void sendSetup() throws IOException {

		if (setup == null) {
			writer.write("NEWGAME\n");
		} else {
			writer.write("SETUPBOARD " + ArrayBoardSetup.encodeBase64(setup) + "\n");
		}

		writer.flush();
	}

	private void negotiateNames() throws IOException, JGamProtocolException {
		writer.write("CALLME " + localName + "\n");
		writer.flush();
		String line = reader.readLine();
		if (!line.startsWith("CALLME ")) {
			throw new JGamProtocolException("Expected CALLME but received: " + line);
		}
		remoteName = line.substring(7);
	}

	/**
	 * create a reader for a channel
	 *
	 * @param channel int
	 * @return BufferedReader
	 */
	public BufferedReader getChannelReader(int channel) {
		ChannelReader cr = new ChannelReader(channel, this);
		return new BufferedReader(cr);
	}

	public Writer getChannelWriter(int channel) {
		return new ChannelWriter(writer, channel);
	}

	public void close(String message) {
		if (socket != null) {
			try {
				writer.write("0 CLOSING");
				if (message != null) {
					writer.write(" " + message);
				}
				writer.write("\n");
				writer.flush();
				socket.close();
				socket = null;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	synchronized public void runCommunicationThread() {
		try {
			String line = reader.readLine();
			while (line != null) {
				int channel = line.charAt(0) - '0';
				String msg = line.substring(2);
				handleMessage(channel, msg);
				line = reader.readLine();
			}
			handleMessage(0, "CLOSEDSTREAM");
		} catch (Exception ex) {
			handleMessage(0, "EXCEPTION " + ex);
		}
	}

	/**
	 * handle a message on a channel
	 *
	 * @param channel int
	 * @param message String
	 */
	private void handleMessage(int channel, String message) {
		for (Iterator iter = channelListeners.iterator(); iter.hasNext(); ) {
			ChannelListener listener = (ChannelListener) iter.next();
			listener.receiveChannelMessage(channel, message);
		}
	}

	public void rollDices(final Game game) {
		Runnable diceJob = new Runnable() {
			public void run() {
				try {
					if (Boolean.getBoolean("jgam.unsecuredice")) {
						secureDice.negotiateUnsecure(secdiceReader, secdiceWriter, serverMode);
					} else {
						secureDice.negotiate(secdiceReader, secdiceWriter, serverMode);
					}

					game.sendPlayerMessage(new PlayerMessage(null, PlayerMessage.DICES, secureDice.getDice()));
				} catch (Exception ex) {
					throw new Error(ex);
				}
			}
		};
		JGammon.jobThread.add(diceJob);
	}

	public void addChannelListener(ChannelListener cl) {
		channelListeners.add(cl);
	}

	public void removeChannelListener(ChannelListener cl) {
		channelListeners.remove(cl);
	}

	public String getRemoteName() {
		return remoteName;
	}

	public BoardSetup getBoardSetup() {
		return setup;
	}

	public boolean supportsUndo() {
		return true;
	}

	public void openChatWindow() {
		chatter.setVisible(true);
	}
}
