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
import java.io.Writer;

/**
 * prepend each line (ended by '\n') with the channel number.
 * discard '\r'
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ChannelWriter extends Writer {

	/**
	 * create a new channel writer
	 *
	 * @param out     underlying Writer
	 * @param channel channel to be used
	 */
	public ChannelWriter(Writer out, int channel) {
		this.out = out;
		this.channel = Integer.toString(channel) + " ";
		this.buf = new StringBuffer(this.channel);
	}

	private Writer out;
	private String channel;
	private StringBuffer buf;


	// true if a newline character was the last one
	private boolean newline = true;

	public void write(char cbuf[], int off, int len) throws IOException {

		for (int i = 0; i < len; i++) {
			char c = cbuf[off + i];
			if (c != '\r')
				buf.append(c);

			if (c == '\n') {
				synchronized (out) {
					out.write(buf.toString());
//                    System.out.println("send "+buf.toString());
				}
				buf.setLength(0);
				buf.append(channel);
			}
		}
	}

	public void flush() throws IOException {
		synchronized (out) {
			out.flush();
		}
	}

	public void close() throws IOException {
		synchronized (out) {
			out.close();
		}
	}
}
