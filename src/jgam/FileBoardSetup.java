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

import jgam.game.ArrayBoardSetup;
import jgam.game.BoardSetup;
import jgam.util.FormatException;

import java.io.*;
import java.util.Date;

/**
 * <p>Title: JGam - Java Backgammon</p>
 * <p>
 * <p>Description: </p>
 * <p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FileBoardSetup extends ArrayBoardSetup {

	private Date date;
	private String comment;

	public FileBoardSetup(File file) throws FormatException, IOException {

		try {

			Reader r = new FileReader(file);

			String header = readLine(r);
			if (!new String(header).equals("JGAM")) {
				throw new FormatException("Expected JGAM Header; received: " +
						header);
			}

			String dateLine = readLine(r);
			comment = readLine(r);
			String whiteLine = readLine(r);
			String blueLine = readLine(r);
			String turnLine = readLine(r);
			String cubeLine = readLine(r);
			String diceLine = readLine(r);

			date = new Date(Long.parseLong(dateLine));

			parseLine(checkers1, whiteLine);
			parseLine(checkers2, blueLine);
			activePlayer = Integer.parseInt(turnLine);

			String d[] = cubeLine.split(" ");
			doubleCube = Integer.parseInt(d[0]);
			doublePlayer = Integer.parseInt(d[1]);
			if (d.length == 2 && d[1].equalsIgnoreCase("blue")) {
				doubleCube *= -1;
			}

			d = diceLine.split(" ");
			if (d.length == 2) {
				dice = new int[2];
				dice[0] = Integer.parseInt(d[0]);
				dice[1] = Integer.parseInt(d[1]);
			}
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new FormatException(ex);
		}

	}

	/**
	 * ignore comments (#....)
	 * return null at end of stream
	 *
	 * @param r Reader
	 * @return String
	 * @throws IOException
	 */
	public static String readLine(Reader r) throws IOException {
		while (true) {
			StringBuffer ret = new StringBuffer();
			int c = r.read();
			while (c != -1 && c != '\n') {
				if (c != '\r') {
					ret.append((char) c);
				}
				c = r.read();
			}
			if (c == -1 && ret.length() == 0) {
				return null;
			}

			String s = ret.toString();
			if (!s.startsWith("#") && s.trim().length() > 0) {
				return s;
			}
		}
	}


	/**
	 * parse a line that describes a board:
	 * read in the point 1-24 and the bar (25), separated by ":"
	 * the chips already played off are then calculated
	 *
	 * @param board int[] array to write to
	 * @param line  read line
	 */
	private void parseLine(byte[] board, String line) {
		String elems[] = line.split(":");
		assert elems.length == 25;
		int total = 0;
		for (int i = 0; i < elems.length; i++) {
			board[i + 1] = Byte.parseByte(elems[i]);
			total += board[i + 1];
		}
		board[0] = (byte) (15 - total);
	}

	public static void saveBoardSetup(BoardSetup bs, File file, String comment) throws
			IOException {

		PrintWriter pw = new PrintWriter(new FileWriter(file));
		pw.println("JGAM");
		pw.println("# This is a JGam saved backgammon board");
		Date d = new Date();
		pw.println();
		pw.println("# Created: " + d);
		pw.println(d.getTime());
		pw.println();

		pw.println("# Comment: ");
		pw.println(comment);
		pw.println();

		pw.println("#player1's board:");
		for (int i = 1; i < 25; i++) {
			pw.print("" + bs.getPoint(1, i) + ":");
		}
		pw.println(bs.getBar(1));
		pw.println();

		pw.println("#player2's board:");
		for (int i = 1; i < 25; i++) {
			pw.print("" + bs.getPoint(2, i) + ":");
		}
		pw.println(bs.getBar(2));
		pw.println();

		pw.println("# Whose turn is it? 1 for player1, 2 for player2, 0 not yet decided");
		pw.println(bs.getActivePlayer());
		pw.println();

		pw.println("# Double cube. value of the double player and then the player who may double (or 1)");
		pw.println(bs.getDoubleCube() + " " + (bs.mayDouble(1) ? 1 : 2));
		pw.println();

		pw.println("# the dice (if any) e.g. 2 4. Or \"nodice\"");
		int dice[] = bs.getDice();
		if (dice == null) {
			pw.println("nodice");
		} else {
			pw.println(dice[0] + " " + dice[1]);
		}

		pw.flush();
		pw.close();

	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>comment</code>.
	 *
	 * @return the value of <code>comment</code>
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>date</code>.
	 *
	 * @return the value of <code>date</code>
	 */
	public Date getDate() {
		return this.date;
	}
}
