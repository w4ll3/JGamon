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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;

/**
 * JGammonConnection works with channels. With this class you can listen to
 * just one channel ignore the other communication
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ChannelReader extends Reader implements ChannelListener {

	private int channel;
	private JGammonConnection connection;

	/**
	 * create a channel reader that connects to a JGameConnection and receives
	 * messages from there.
	 * <p>
	 * When closing the JGammonConnection will be detached
	 *
	 * @param channel channel to listen to
	 * @param conn    JGammonConnection to connect to
	 */
	public ChannelReader(int channel, JGammonConnection conn) {
		this.channel = channel;
		this.connection = conn;
		connection.addChannelListener(this);
	}

	/**
	 * create a channel reader not connected to a MessageProducer.
	 *
	 * @param channel channel to listen to.
	 */
	public ChannelReader(int channel) {
		this.channel = channel;
	}

	private StringBuffer buf = new StringBuffer();

	/**
	 * Close the stream.
	 * set the buffer to null, detach from the JGammonConnection.
	 */
	public void close() {
		buf = null;
		if (connection != null) {
			connection.removeChannelListener(this);
		}
	}

	/**
	 * Read characters into a portion of an array. Read from from the stringbuffer.
	 * If this becomes empty, block till it is refilled.
	 *
	 * @param cbuf Destination buffer
	 * @param off  Offset at which to start storing characters
	 * @param len  Maximum number of characters to read
	 * @return The number of characters read, or -1 if the end of the stream
	 * has been reached
	 * @throws IOException If an I/O error occurs
	 */
	public synchronized int read(char[] cbuf, int off, int len) throws IOException {
		if (buf == null) {
			return -1;
		}

		while (buf.length() == 0) {
			try {
				wait();
			} catch (InterruptedException ex) {
				IOException iox = new InterruptedIOException();
				iox.initCause(ex);
				throw iox;
			}
		}

		int cnt = Math.min(len, buf.length());
		buf.getChars(0, cnt, cbuf, off);
		buf.delete(0, cnt);

		return cnt;
	}

	public synchronized void receiveChannelMessage(int channel, String message) {
		if (buf != null && channel == this.channel) {
			buf.append(message).append("\n");
			notify();
		}
	}
}
