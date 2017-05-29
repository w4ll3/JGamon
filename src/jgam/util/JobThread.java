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

/**
 * Jobs in form of Runnable objects are executed within a thread.
 * <p>
 * They can be added to the end of the queue and one after the other will be
 * executed.
 *
 * @author Mattias Ulbrich
 */
public class JobThread extends Thread {

	BlockingQueue jobqueue = new BlockingQueue();

	/**
	 * create a new job thread but do not start it yet.
	 */
	public JobThread() {
		super("JobThread");
	}

	/**
	 * create a new job thread.
	 *
	 * @param start start the thread if start is true
	 */
	public JobThread(boolean start) {
		this();
		if (start)
			start();
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used to
	 * create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 */
	public void run() {
		while (!jobqueue.isAborted()) {
			try {
				Runnable runnable = (Runnable) jobqueue.take();
				runnable.run();
			} catch (InterruptedException ex) {
				// nothing to do.
			} catch (Throwable ex) {
				System.err.println("Uncaught exception: ");
				ex.printStackTrace();
			}
		}
	}

	public void add(Runnable runnable) {
		jobqueue.add(runnable);
	}

	public void abort() {
		jobqueue.abort();
	}

}
