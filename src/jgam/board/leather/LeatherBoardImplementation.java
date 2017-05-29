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


package jgam.board.leather;

import jgam.board.BoardComponent;
import jgam.board.PaintingBoardImplementation;
import jgam.board.ResourceBoardImplementation;
import jgam.game.BoardSetup;
import jgam.game.InvertedBoardSetup;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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
public class LeatherBoardImplementation extends ResourceBoardImplementation implements PaintingBoardImplementation {

	private Image[] scaledShadedCheckers = new Image[2];
	private ImageIcon[] shadedCheckers = new ImageIcon[2];

	private ImageIcon[] shadows = new ImageIcon[5];
	private Rectangle clipArea;

	private int barDistance;
	private int doubleDiceWidth;

	private static final int NORMAL = -1;
	private static final int LEFTSHADED = 0;
	private static final int TOPSHADED = 1;
	private static final int TOPLEFTSHADED = 2;
	private static final int LITTLELEFTSHADED = 3;
	private static final int THINSHADED = 4;


	public LeatherBoardImplementation() throws IOException {
		super("jgam.board.leather.LeatherBoardImplementation");
	}

	public void init(BoardComponent board) throws Exception {

		if (!isAlreadyInitialized()) {
			super.init(board);

			clipArea = getRectangle("clipArea");

			shadedCheckers[0] = loadIcon("shadowChecker1");
			shadedCheckers[1] = loadIcon("shadowChecker2");

			shadows[LEFTSHADED] = loadIcon("leftShadow");
			shadows[TOPSHADED] = loadIcon("topShadow");
			shadows[TOPLEFTSHADED] = loadIcon("topLeftShadow");
			shadows[LITTLELEFTSHADED] = loadIcon("littleLeftShadow");
			shadows[THINSHADED] = loadIcon("thinShadow");

			barDistance = getInt("barDistance", 0);
			doubleDiceWidth = getInt("doubleDiceWidth", -1);
		}
	}


	public void paint(Graphics g) {

		BoardComponent board = getBoard();
		Rectangle leftClip = new Rectangle((int) (clipArea.x * getFactor()),
				(int) (clipArea.y * getFactor()),
				(int) (clipArea.width * getFactor()),
				(int) (clipArea.height * getFactor()));
		Rectangle rightClip = (Rectangle) leftClip.clone();
		rightClip.x = getSize().width - leftClip.x - leftClip.width;

		// background
		drawBoard(g);

        /*
                 g.setColor(Color.green);
                 g.drawRect(leftClip.x,leftClip.y, leftClip.width, leftClip.height);
                 g.drawRect(rightClip.x,rightClip.y, rightClip.width, rightClip.height);
        */

		BoardSetup setup = board.getBoardSnapshot();
		BoardSetup origsetup = setup;
		if (setup != null) {

			// swapped colors?
			if (board.isColorSwapped()) {
				setup = new InvertedBoardSetup(setup);
			}

			// upper half:
			g.setClip(leftClip);

			for (int pt = 1; pt <= 12; pt++) {
				if (pt == 7) {
					g.setClip(rightClip);
				}
				int amount = board.getCheckerOnPoint(pt);
				int color = 0;
				if (amount < 0) {
					color = 1;
					amount = -amount;
				}

				for (int i = 0; i < amount; i++) {
					Point p = board.getPointForChecker(pt, i);
					g.drawImage(getScaledInstance(shadedCheckers[color]), p.x, p.y, board);
					Image shadow = getCheckerShadow(pt, i);
					if (shadow != null) {
						g.drawImage(shadow, p.x, p.y, board);
					}
				}
			}

			// lower half:
			g.setClip(leftClip);
			for (int pt = 13; pt <= 24; pt++) {
				if (pt == 19) {
					g.setClip(rightClip);
				}

				int amount = board.getCheckerOnPoint(pt);
				int color = 0;
				if (amount < 0) {
					color = 1;
					amount = -amount;
				}

				for (int i = 0; i < amount; i++) {
					int no = bottomOrder(i, amount);
					Point p = board.getPointForChecker(pt, no);
					g.drawImage(getScaledInstance(shadedCheckers[color]), p.x, p.y, board);
					Image shadow = getCheckerShadow(pt, no);
					if (shadow != null) {
						g.drawImage(shadow, p.x, p.y, board);
					}
				}
			}

			g.setClip(new Rectangle(getSize()));

			// upper Bar.
			{
				int amount = board.getCheckerOnPoint(25);
				int color = 0;
				if (amount < 0) {
					color = 1;
					amount = -amount;
				}
				for (int i = 0; i < amount; i++) {
					Point p = board.getPointForChecker(25, i);
					// correct position!!
					p.y -= barDistance;
					g.drawImage(getScaledInstance(shadedCheckers[color]), p.x, p.y, board);
				}
			}

			// lower bar
			{
				int amount = board.getCheckerOnPoint(0);
				int color = 0;
				if (amount < 0) {
					color = 1;
					amount = -amount;
				}
				for (int i = 0; i < amount; i++) {
					int no = bottomOrder(i, amount);
					Point p = board.getPointForChecker(0, no);
					// correct position!!
					p.y += barDistance;
					g.drawImage(getScaledInstance(shadedCheckers[color]), p.x, p.y, board);
				}
			}

			// the dice
			// bugfix: use origsetup to not twice correct the swapping
			board.paintDice(g, setup.getDice(), origsetup.getActivePlayer());

			// the outs
			board.paintOut(1, g);
			board.paintOut(2, g);

			// the doubleDice
			board.paintDoubleDice(g);
		}

	}

	public void drawThinChecker(Graphics g, int color, Point p) {
		Image image = getScaledInstance(color == 1 ? thinChip1 : thinChip2);
		g.drawImage(image, p.x, p.y, getBoard());
		if (p.x < getSize().width / 2) {
			g.drawImage(getScaledInstance(shadows[THINSHADED]), p.x, p.y, getBoard());
		}
	}

	/**
	 * get the shadow to add to a checker.
	 */
	private Image getCheckerShadow(int pt, int no) {

		int shadetype;

		if ((pt == 7 || pt == 1) && no == 0) {
			shadetype = TOPLEFTSHADED;
		} else if (pt == 1 || pt == 13 || pt == 7 || pt == 19) {
			if (no < 5) {
				shadetype = LEFTSHADED;
			} else if (no < 9) {
				shadetype = LITTLELEFTSHADED;
			} else {
				shadetype = NORMAL;
			}
		} else if (pt <= 12 && no == 0) {
			shadetype = TOPSHADED;
		} else {
			shadetype = NORMAL;
		}

		if (shadetype != NORMAL && shadows[shadetype] != null) {
			return getScaledInstance(shadows[shadetype]);
		} else {
			return null;
		}
	}

	/*
	 * because of the shadow the points on the lower half of the screen have to
	 * be painted with a different checkerorder
	 */
	private int bottomOrder(int no, int sum) {
		if (no <= 4) {
			return Math.min(4, sum - 1) - no;
		} else if (no <= 8) {
			return Math.min(8, sum - 1) - no + 5;
		} else if (no <= 11) {
			return Math.min(11, sum - 1) - no + 9;
		} else if (no <= 13) {
			return Math.min(13, sum - 1) - no + 12;
		} else {
			return 14;
		}
	}

	/**
	 * determine the log to the base of 2 of an integer.
	 * The result is the largest integer such that
	 * 2^result <= value
	 *
	 * @param value int to determine the log of. must be >= 1
	 * @return the largest int result with 2^result <= value
	 */
	private static int log2(int value) {
		if (value <= 0) {
			throw new IllegalArgumentException("log2 argument error: " + value);
		}
		if (value == 1) {
			return 0;
		}
		return log2(value >> 1) + 1;
	}

	/**
	 * draw the doubleDice.
	 * <p>
	 * load the width from resources and don't calc it from image,
	 * because of shadow
	 *
	 * @param g     Graphics
	 * @param index index of the cube sides.
	 * @param top   true if in the upper half of the field
	 */
	public void drawDoubleDice(Graphics g, int index, boolean top) {

		// don't paint the turned 64 - dice.
		if (index == -1)
			return;

		int xpos = (int) ((getSize().width - doubleDiceWidth * getFactor()) / 2);
		int ypos;
		if (!top) {
			ypos = getSize().height - (int) ((doubleDiceDistance +
					doubleDice[index].getIconHeight()) * getFactor());
		} else {
			ypos = (int) (doubleDiceDistance * getFactor());
		}

		g.drawImage(getScaledInstance(doubleDice[index]), xpos, ypos, getBoard());

	}

}
