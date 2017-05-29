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

package tools;

import jgam.SecureRoll;
import jgam.net.ChannelWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

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
public class TelnetClient {
	public TelnetClient() throws IOException {
		pipedWriter = new PipedWriter(pipedReader);
	}

	Socket socket;
	SecureRoll secureDice;
	private BufferedReader reader;
	private OutputStreamWriter writer;

	private boolean server = false;

	private PipedReader pipedReader = new PipedReader();
	private PipedWriter pipedWriter;

	public void init() throws NoSuchAlgorithmException, UnknownHostException, IOException {
		socket = new Socket("localhost", 1777);
		secureDice = new SecureRoll();
		reader = new BufferedReader(new InputStreamReader(socket.
				getInputStream(), "UTF-8"), 1);
		writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

		writer.write("SERVUS JGAM 04 - This is the telnet client\n");
		writer.flush();
		String opponentVersion = reader.readLine();
		System.out.println(">>VERSION>> " + opponentVersion);
		writer.write("CALLME Telnet\n");
		writer.flush();
		String name = reader.readLine();
		System.out.println(">>NAME>> " + name);
		String setup = reader.readLine();
		System.out.println(">>SETUP>> " + setup);
	}

	public Runnable output = new Runnable() {
		public void run() {
			try {
				while (true) {
					String line = reader.readLine();
					System.out.println(">> " + line);

					if (line.startsWith("3 ")) {
						pipedWriter.write(line.substring(2) + "\n");
						writer.flush();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	};

	public void input() throws IOException, InterruptedException {
		StringBuffer buffer = new StringBuffer();
		while (true) {
			int c = System.in.read();
			if (c == '\r') {
				continue;
			}
			buffer.append((char) c);
			if (c == '\n') {
				Thread.sleep(1000);
				writer.write(buffer.toString());
				writer.flush();
				buffer.setLength(0);
			}
		}
	}

	public static void main(String[] args) throws IOException, UnknownHostException, NoSuchAlgorithmException, InterruptedException {
		TelnetClient telnetclient = new TelnetClient();
		if (args.length == 0) {
			telnetclient.init();
		} else {
			telnetclient.initServer();
		}
		new Thread(telnetclient.secure, "secdice").start();
		new Thread(telnetclient.output, "output").start();
		telnetclient.input();
	}

	/**
	 * initServer
	 */
	private void initServer() throws IOException, NoSuchAlgorithmException {

		ServerSocket serverSocket = new ServerSocket(1777);
		socket = serverSocket.accept();
		server = true;
		secureDice = new SecureRoll();
		reader = new BufferedReader(new InputStreamReader(socket.
				getInputStream(), "UTF-8"), 1);
		writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

		writer.write("SERVUS JGAM 04 - This is the telnet client\n");
		writer.flush();
		String opponentVersion = reader.readLine();
		System.out.println(">>VERSION>> " + opponentVersion);
		writer.write("CALLME Telnet\n");
		writer.flush();
		String name = reader.readLine();
		System.out.println(">>NAME>> " + name);
		System.out.println("NEWGAME");
		writer.write("NEWGAME\n");
		writer.flush();
	}

	/**
	 * run the secure dice thing
	 */
	private Runnable secure = new Runnable() {
		public void run() {
			try {
				BufferedReader br = new BufferedReader(pipedReader);
				Writer w = new ChannelWriter(writer, 3);
				while (true) {
					secureDice.negotiate(br, w, server);
					int[] d = secureDice.getDice();
					System.out.println("we negotiated: " + d[0] + " " + d[1]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	};
}
