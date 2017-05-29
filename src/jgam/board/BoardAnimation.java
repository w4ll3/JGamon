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


package jgam.board;

import jgam.game.BoardSetup;

import javax.swing.*;
import java.awt.*;

/**
 * Animation that is shown if the remote player drags a chip.
 * <p>
 * It is one in a thread of its own.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class BoardAnimation implements Paintable {

	private int player;
	private ImageIcon chip;
	private int fromPoint;
	private int toPoint;

	private double curX, curY;
	private double offsetX, offsetY;

	public static void setStepOverride(int number) {
		STEP_OVERRIDE = STEP_LENGTH * number;
	}

	public static void unsetStepOverride() {
		STEP_OVERRIDE = null;
	}

	private static Integer STEP_OVERRIDE = null;
	private static final int STEP_LENGTH = Integer.getInteger("jgam.animationstep", 25);

	public static void setSleepOverride(int number) {
		SLEEP_OVERRIDE = Math.toIntExact(SLEEP_TIME / number);
	}

	public static void unsetSleepOverride() {
		SLEEP_OVERRIDE = null;
	}

	private static Integer SLEEP_OVERRIDE = null;
	private static final long SLEEP_TIME = Integer.getInteger("jgam.animationdelay", 35);

	// the previously set setup
	private BoardSetup origSetup;

	public BoardAnimation(int player, int from, int to) {
		this.player = player;

		fromPoint = from;
		toPoint = to;
	}

	public void animate(BoardComponent board) {
		try {
			chip = board.getCheckerIcon(player);
			origSetup = board.getBoardSnapshot();
			int fromNo = origSetup.getPoint(player, fromPoint) - 1;
			int toNo = origSetup.getPoint(player, toPoint);

			Point fromLocation, toLocation;
			// from Bar
			fromLocation = board.getPointForChecker(board.playerToNativePoint(fromPoint, player),
					fromNo);

			// to off
			if (toPoint == 0) {
			    /* in case the out is wider than a checker.
			     * center it */
				Rectangle outfield = board.getOffField(player);
				toLocation = new Point(outfield.x +
						(outfield.width -
								board.getBoardImplementation().getCheckerDiameter()) /
								2,
						outfield.y + outfield.height);
				toLocation.translate(0,
						-board.getBoardImplementation().
								getCheckerDiameter() -
								origSetup.getOff(player) *
										board.getBoardImplementation().
												getChipThickness());
			} else {
				toLocation = board.getPointForChecker(board.playerToNativePoint(toPoint, player), toNo);
			}

			curX = fromLocation.x;
			curY = fromLocation.y;
			double distance = toLocation.distance(fromLocation);
			int nosteps = (int) (distance / (STEP_OVERRIDE == null ? STEP_LENGTH : STEP_OVERRIDE));

			offsetX = (toLocation.x - fromLocation.x) / ((double) nosteps);
			offsetY = (toLocation.y - fromLocation.y) / ((double) nosteps);

			board.useBoardSetup(new DraggedBoardSetup(board.getBoardSnapshot(), player, fromPoint));
			board.addPaintHook(this);
			int dmt = board.getBoardImplementation().getCheckerDiameter();
			for (int i = 0; i <= nosteps; i++) {
				int minx = (int) Math.min(curX, curX - offsetX);
				int miny = (int) Math.min(curY, curY - offsetY);
				Rectangle clip = new Rectangle(minx, miny,
						(int) (Math.abs(offsetX) + 1 + dmt),
						(int) (Math.abs(offsetY) + 1 + dmt));
				board.repaint(clip);
				Thread.sleep((SLEEP_OVERRIDE == null ? SLEEP_TIME : SLEEP_OVERRIDE));
				curX += offsetX;
				curY += offsetY;
			}
		} catch (InterruptedException ex) {
		} finally {
			board.removePaintHook(this);
		}
	}

	public void paint(Graphics g) {
		g.drawImage(chip.getImage(), (int) curX, (int) curY, null);
	}

}
