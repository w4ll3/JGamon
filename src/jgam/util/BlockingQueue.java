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

import java.util.LinkedList;

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

public class BlockingQueue {

	private LinkedList queue = new LinkedList();

	private int limitSize = -1;

	private boolean aborted = false;

	public int size() {
		return queue.size();
	}

	public BlockingQueue() {
		limitSize = -1;
	}

	public BlockingQueue(int s) {
		limitSize = s;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	synchronized public void add(Object a) {
		if (isAborted())
			throw new IllegalStateException("You may not add to an aborted queue");

		queue.addLast(a);
		if (limitSize != -1 && size() > limitSize) {
			queue.removeFirst();
		}
		notify();
	}

	synchronized public Object take() throws InterruptedException {
		if (isAborted())
			throw new BQInterruptedException();

		while (isEmpty()) {
			wait();
			if (isAborted())
				throw new BQInterruptedException();
		}

		return queue.removeFirst();
	}

	synchronized public void abort() {
		aborted = true;
		notifyAll();
	}

	public boolean isAborted() {
		return aborted;
	}

	public static class BQInterruptedException extends InterruptedException {
		BQInterruptedException() {
			super("queue has been aborted");
		}
	}
}
