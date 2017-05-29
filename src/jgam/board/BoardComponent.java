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
import jgam.game.BoardSnapshot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Component to draw the backgammon board.
 * <p>
 * It fetches the information from a Game-instance.
 * <p>
 * The actual drawing is done by an instance of "BoardImplementation". Thus
 * several different board layouts can be used. ...
 * <p>
 * It allows drag and drop via a BoardMouseListener.
 * It allows an Animation to be drawn via BoardAnimation
 *
 * @author Mattias Ulbrich
 */
public class BoardComponent extends JComponent {

	// the used BoardImplementation
	private BoardImplementation impl;

	// the displayed setup (or null)
	private BoardSnapshot boardSnapshot;

	// the elements in this list are called to do additional painting
	// (mouse listener, animation)
	private List paintHooks = new ArrayList();

	/*
	 * some drawing variants
	 */
	private boolean flipTopBottom = false;
	private boolean flipLeftRight = true;
	private boolean colorsSwapped = false;


	public BoardComponent(BoardImplementation impl, BoardSetup boardSetup) {
		setBoardImplementation(impl);
		useBoardSetup(boardSetup);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				resizeEvent(e);
			}
		});
	}


	/**
	 * set the implementation that knows about all the resources for this
	 * board.
	 * You have to repaint the board after selecting a new implementation.
	 *
	 * @param impl BoardImplementation to set
	 */
	public void setBoardImplementation(BoardImplementation impl) {
		try {
			impl = impl.newInstance();
			impl.init(this);

		} catch (Exception ex) {
			System.err.println("Cannot initialize the BoardImplementation " +
					impl);
			ex.printStackTrace();
			return;
		}

		this.impl = impl;
		setPreferredSize(impl.getPreferredSize());
		setMinimumSize(impl.getMinimumSize());
		setMaximumSize(impl.getMaximumSize());
		repaint();
	}

	public void resizeEvent(ComponentEvent e) {
		Dimension origsize = getSize();
		Dimension implSize = impl.setSize(origsize);
		repaint();
	}

	/**
	 * paint the board. This is the key function that does the following
	 * tasks:
	 * - paint the background if needed.
	 * - translate the ontext if needed.
	 * - call paintBoard or impl.paint (if it is a PaintingBoardImplementation)
	 * - call paint on all hooks
	 * - undo translation of the context.
	 * <p>
	 * Note: Rathe use paintComponent than paint so I can still add a border.
	 *
	 * @param g Graphics to paint with
	 */
	public void paintComponent(Graphics g) {

		if (impl == null) {
			return;
		}

        /*
         * This component might be larger than the implementation
         * so fill with background (if there is one)
         * and translate the Graphics object
         */
		Dimension componentSize = getSize();
		Dimension implSize = impl.getSize();
		int xtrans = 0, ytrans = 0;

		if (!componentSize.equals(implSize)) {
			impl.fillBackground(g);
			g.translate(xtrans = (componentSize.width - implSize.width) / 2,
					ytrans = (componentSize.height - implSize.height) / 2);
		}

		// now draw the actual board
		if (impl instanceof PaintingBoardImplementation) {
			((PaintingBoardImplementation) impl).paint(g);
		} else {
			paintBoard(g);
		}

		// call the hooks!
		synchronized (paintHooks) {
			for (Iterator iter = paintHooks.iterator(); iter.hasNext(); ) {
				Paintable item = (Paintable) iter.next();
				item.paint(g);
			}
		}

		// undo a previously set translation!
		g.translate(-xtrans, -ytrans);

		if (Boolean.getBoolean("jgam.board.debug")) {
			Rectangle r = g.getClipBounds();
			g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
		}

		// set the backgroundcolor in the end.
		g.setColor(getBackground());
	}


	/**
	 * This method paints the board, all checkers, the dice and double cube
	 * to the Graphics-context.
	 * It is the default implementation if the implementation does not provide
	 * a paint method of its own.
	 *
	 * @param g Graphics
	 */
	public void paintBoard(Graphics g) {
		impl.drawBoard(g);

		BoardSetup setup = getBoardSnapshot();
		if (setup != null) {

			// color 1:
			int p = 1;
			for (int i = 1; i <= 25; i++) {
				int nativePoint = playerToNativePoint(i, p);
				paintPoint(1, g, nativePoint, setup.getPoint(p, i));
			}

			// color 2:
			p = 2;
			for (int i = 1; i <= 25; i++) {
				int nativePoint = playerToNativePoint(i, p);
				paintPoint(2, g, nativePoint, setup.getPoint(p, i));
			}

			// the dice
			paintDice(g, setup.getDice(), setup.getActivePlayer());

			// the outs
			paintOut(1, g);
			paintOut(2, g);

			// the doubleDice
			paintDoubleDice(g);
		}

	}

	public void paintDice(Graphics g, int[] d, int playeratmove) {
		// the dice
		// player 1
		if (d != null) {
			if (playeratmove == 0) {
				impl.drawDice(g, getColorForPlayer(1), new int[]{d[0]});
				impl.drawDice(g, getColorForPlayer(2), new int[]{d[1]});
			} else {
				impl.drawDice(g, getColorForPlayer(playeratmove), d);
			}
		}
	}

	/**
	 * paint a point with chips in a certain color.
	 * The point number is the logical point number which needs to be translated.
	 *
	 * @param player number of player to paint for (1 or 2)
	 * @param point  number of the point in native scheme!
	 * @param count  number of chips to paint
	 */
	public void paintPoint(int player, Graphics g, int point, int count) {
		for (int i = 0; i < count; i++) {
			paintChip(player, g, point, i);
		}
	}


	/**
	 * paint a chip at a native point. they are correctly piled.
	 *
	 * @param int   Player to paint for
	 * @param g     Graphics to draw with
	 * @param point native point to paint on, 0-25, 0,25 are bar
	 * @param index index of the chip on the point, 0-14 (there are only 15 chips
	 *              per player)
	 */
	public void paintChip(int player, Graphics g, int point, int index) {
		Point p = getPointForChecker(point, index);
		impl.drawChecker(g, getColorForPlayer(player), p);
	}

	/**
	 * paint the final output fields
	 *
	 * @param image    Image thin chips
	 * @param playerno Player to draw
	 * @param g        Graphics to write to
	 */
	public void paintOut(int playerno, Graphics g) {

		Rectangle outfield = getOffField(playerno);

        /* in case the out is wider than a chip.
         * center it */
		Point start = new Point(outfield.x +
				(outfield.width - impl.getCheckerDiameter()) / 2,
				outfield.y + outfield.height);

		for (int i = 0; i < boardSnapshot.getOff(playerno); i++) {
			start.translate(0, -impl.getChipThickness());
			impl.drawThinChecker(g, getColorForPlayer(playerno), start);
		}
	}

	/**
	 * paint the double dice at the appropriate place.
	 *
	 * @param g Graphics
	 */
	public void paintDoubleDice(Graphics g) {
		int doubleValue = getBoardSnapshot().getDoubleCube();
		int index = log2(doubleValue) - 1;
		boolean mayTopDouble = getBoardSnapshot().mayDouble(1) != flipTopBottom;
		impl.drawDoubleDice(g, index, mayTopDouble);
	}


	// calculations

	/**
	 * as the colors may have been swapped the color to use for a player
	 * must accordingly be adjusted. Call this to get the color index for the
	 * player index.
	 * <p>
	 * Also, if negative numbers are used to indicate preview checkers, they
	 * are also corrected
	 *
	 * @param player player index (1 or 2, -1 or -2)
	 * @return color index (1 or 2, -1 or -2)
	 */
	public int getColorForPlayer(int player) {
		if (isColorSwapped()) {
			if (player > 0) {
				return 3 - player;
			} else {
				return -player - 3;
			}
		} else {
			return player;
		}
	}

	/**
	 * get the point to draw the chip at for a specific position an a native point.
	 *
	 * @param point  native index of a point
	 * @param number index of the chip on the point between 0 and 14.
	 * @return Point to draw to
	 */
	public Point getPointForChecker(int point, int number) {

		Point p;

		//  bar first!   25 is upper bar   0 is lower bar
		if (point == 25) {
			return new Point((impl.getSize().width - impl.getCheckerDiameter()) /
					2,
					(2 + number) * impl.getCheckerDiameter());
		}

		if (point == 0) {
			return new Point((impl.getSize().width - impl.getCheckerDiameter()) /
					2,
					impl.getSize().height -
							((3 + number) * impl.getCheckerDiameter()));
		}

        /* store in offset the vertical offset of this chip.
         * the chips are to be piled in the following order (side view)
         *
         *         14
         *       12  13
         *     9   10  11
         *   5   6   7   8
         * 0   1   2   3   4
         *
         * This ought to work with any number between 0 and 14 (incl).
         */

		int offset = 0;
		int length = 5; // length of this pile row.
		while (number >= length) {
			offset += impl.getCheckerDiameter() / 2;
			number -= length;
			length--;
		}
		offset += number * impl.getCheckerDiameter();

		if (point <= 12) {
			p = impl.getUpperLeftCorner();
			p.translate((int) (impl.getPointDimension().getWidth() * (point - 1)), offset);
			// beyond the bar
			if (point >= 7) {
				p.translate(getBarWidth(), 0);
			}
		} else {
			p = impl.getUpperLeftCorner();
			p.y = impl.getSize().height - p.y - impl.getCheckerDiameter();
			p.translate((int) (impl.getPointDimension().getWidth() * (point - 13)), -offset);
			// beyond the bar
			if (point >= 19) {
				p.translate(getBarWidth(), 0);
			}
		}

        /* if the chip is smaller than the point, it has to be corrected to the
         * center of the point */
		p.translate((int) ((impl.getPointDimension().getWidth() - impl.getCheckerDiameter()) / 2),
				0);

		return p;
	}

	/**
	 * get the rectangle that describes the currently available outfield.
	 *
	 * @param player Player for whom to describe the outfield.
	 * @return Rectangle within the board.
	 */
	public Rectangle getOffField(int playerno) {

		Rectangle r = impl.getUpperLeftOutField();

		if (!isPlayerOnTop(playerno)) {
			r.y = impl.getSize().height - r.y - r.height;
		}

		if (isLeftRightFlipped()) {
			r.x = impl.getSize().width - r.x - r.width;
		}

		return r;
	}


    /*
     * There a two ways to enumerate the points.
     *
     * One is the native numbering. these are used to directly adress the
     * screen:
     * |  1  2  3  4  5  6  |25|  7  8  9 10 11 12 |
     * |                    |  |                   |
     * | 13 14 15 16 17 18  | 0| 19 20 21 22 23 24 |
     *
     * upper bar is 25, lower is 0.
     *
     * Or the logical (player) numbering. These must be used used when indexing the
     * points  for a player. There are 2 different ones: One for player 1 and one
     * for player2. Obviously they are mostly in a 25-x relationship. Additionally
     * the board may be flipped or turned. then it changes as well.
     * For all flips off
     *
     * |  1  2  3  4  5  6  |  |  7  8  9 10 11 12 |
     * |                    |25|                   |
     * | 24 23 22 21 20 19  |  | 18 17 16 15 14 13 |
     *
     * is the logical board for player 1.
     * off is 0 and bar 25.
     */

	/**
	 * calculate the player's point number for a native point.
	 * The result for 25 and 0 are both 25 (the bar)
	 *
	 * @param point    native point (0-25, 0,25 are bar)
	 * @param playerno Player to whom it shall be calculated
	 * @return player point number 1-25
	 */
	public int nativeToPlayerPoint(int point, int playerno) {

		if (point == 0 || point == 25) {
			return 25;
		}

		//
		// rearrange lower row.
		// flipping left/right
		//
		// if flipleftright is true both rows have to be flipped.
		// the second has to flipped anyway
		// --> flip the second only if not flipleftright
		if (point >= 13 && point <= 24 && !flipLeftRight) {
			point = 37 - point;
		}
		if (point >= 1 && point <= 12 && flipLeftRight) {
			point = 13 - point;
		}

		//
		// xchg if not on top. (they always sum up to 25)
		if (!isPlayerOnTop(playerno)) {
			point = 25 - point;
		}

		return point;
	}

	/**
	 * calculate the native point number for a player point number.
	 *
	 * @param point    player-point number (1-25, 0 not supported!)
	 * @param playerno Player to whom it belongs
	 * @return native point number
	 */
	public int playerToNativePoint(int point, int playerno) {

		assert point > 0 && point <= 25 : "only between 1 and 25 - " + point;

		//
		// xchg if not on top. (they always sum up to 25)
		if (!isPlayerOnTop(playerno)) {
			point = 25 - point;
		}

		//
		// rearrange lower row.
		// flipping left/right
		//
		// if flipleftright is true both rows have to be flipped.
		// the second has to flipped anyway
		// --> flip the second only if not flipleftright
		if (point >= 13 && point <= 24 && !flipLeftRight) {
			point = 37 - point;
		}
		if (point >= 1 && point <= 12 && flipLeftRight) {
			point = 13 - point;
		}

		return point;

	}

	// actions

	/**
	 * turn the board for 180�
	 */
	public void toggleTurn() {
		toggleTopBottom();
		toggleLeftRight();
	}

	/**
	 * mirror the board top to bottom
	 */
	public void toggleTopBottom() {
		flipTopBottom = !flipTopBottom;
		repaint();
	}

	/**
	 * mirror the board left to right
	 */
	public void toggleLeftRight() {
		flipLeftRight = !flipLeftRight;
		repaint();
	}

	/**
	 * change the colors.
	 * ==> invert the board when painting and toggle top/bottom
	 */
	public void toggleSwapColors() {
		colorsSwapped = !colorsSwapped;
		toggleTopBottom();
	}

	/**
	 * add a paint Hook
	 *
	 * @param paintable Paintable to be drawn along
	 */
	public void addPaintHook(Paintable paintable) {
		synchronized (paintHooks) {
			paintHooks.add(paintable);
		}
	}

	/**
	 * remove a paint hook
	 */
	public void removePaintHook(Paintable paintable) {
		synchronized (paintHooks) {
			paintHooks.remove(paintable);
		}
	}

	/**
	 * set the BoardSetup to be displayed in this BoardComponent.
	 * This value may be changed by other parts of the program.
	 * <p>
	 * The component takes a snapshot of the BoardSetup instead of the
	 * possibly changing BoardSetup itself.
	 *
	 * @param boardSetup new value to be set for <code>boardSetup</code>
	 */
	synchronized public void useBoardSetup(BoardSetup boardSetup) {
		if (boardSetup == null) {
			this.boardSnapshot = null;
		} else {
			this.boardSnapshot = new BoardSnapshot(boardSetup);
		}
		/*        System.out.println("Use board setup:");
                if(boardSetup != null)
                    boardSetup.debugOut();*/
		repaint();
	}

	// queries go here

	/**
	 * return the setup that is displayed in this board.
	 * This is the temporary board if there is one, else the boardSetup.
	 *
	 * @return the displayed boardsetup or null if there is none.
	 */
	public BoardSetup getBoardSnapshot() {
		return boardSnapshot;
	}

	/**
	 * calculate bar width from the implementation data
	 *
	 * @return width of the bar.
	 */
	public int getBarWidth() {
		return (int) (impl.getSize().width -
				2 * impl.getUpperLeftCorner().x -
				12 * impl.getPointDimension().getWidth());
	}


	/**
	 * return true if the player has his home in the upper half of the board.
	 * this is the case if not vert-flipped and player1 or flipped and player2
	 *
	 * @param playerno 1 or 2.
	 * @return boolean
	 */
	public boolean isPlayerOnTop(int playerno) {
		return ((playerno == 1) != isTopBottomFlipped());
	}

	/**
	 * return if this board is flipped on a vertical axis. (left and right flipped)
	 *
	 * @return true iff so.
	 */
	public boolean isLeftRightFlipped() {
		return flipLeftRight;
	}

	/**
	 * return if this board is flipped on a horizontal axis. (top and bottom flipped)
	 *
	 * @return true iff so.
	 */
	public boolean isTopBottomFlipped() {
		return flipTopBottom;
	}

	/**
	 * return whether the colors are swapped with regard to the original setup.
	 *
	 * @return true iff so.
	 */
	public boolean isColorSwapped() {
		return colorsSwapped;
	}

	/**
	 * return the used BoardImplementation
	 *
	 * @return the used BoardImplementation
	 */
	public BoardImplementation getBoardImplementation() {
		return this.impl;
	}

	/**
	 * get the color string for a player.
	 *
	 * @param player Player
	 * @return the name of the color as returned by the BoardImplementation
	 */
	public String getColorName(int playerno) {
		if (colorsSwapped) {
			playerno = 3 - playerno;
		}
		return playerno == 1 ?
				getBoardImplementation().getColorName1() :
				getBoardImplementation().getColorName2();
	}


	/**
	 * get the checker icon for a player
	 *
	 * @param playerno Player to get the icon for
	 * @return ImageIcon the icon returned by the BoardImplementation
	 */
	public ImageIcon getCheckerIcon(int playerno) {
		if (colorsSwapped) {
			playerno = 3 - playerno;
		}
		return getBoardImplementation().getCheckerIcon(playerno);
	}


	/**
	 * Get the number of checkers on a native point. The point may also be
	 * 0 or 25 for the upper or lower bar.
	 *
	 * @param nativepoint the point to look at (0 <= nativepoint <= 25)
	 * @return number of checkers on this point. Positive for color1, negative
	 * for color2.
	 */
	public int getCheckerOnPoint(int nativepoint) {

		if (nativepoint == 25 || nativepoint == 0) {
			boolean player1Bar = (nativepoint == 25) == (isPlayerOnTop(1));
			if (player1Bar) {
				int value = getBoardSnapshot().getBar(1);
				return isColorSwapped() ? -value : value;
			} else {
				int value = getBoardSnapshot().getBar(2);
				return isColorSwapped() ? value : -value;
			}
		} else {

			int playerpoint = nativeToPlayerPoint(nativepoint, 1);
			int value1 = getBoardSnapshot().getPoint(1, playerpoint);

			playerpoint = nativeToPlayerPoint(nativepoint, 2);
			int value2 = getBoardSnapshot().getPoint(2, playerpoint);

			return isColorSwapped() ? value2 - value1 : value1 - value2;
		}
	}


	// helpers

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


	public static void main(String[] args) {
		BoardComponent bc = new BoardComponent(null, null);

		System.out.println("Native to player 1:");
		for (int i = 1; i < 25; i++) {
			System.out.print(" " + bc.nativeToPlayerPoint(i, 1));
			if (i == 12) {
				System.out.println();
			}
		}
		System.out.println("\nNative to player 2:");
		for (int i = 1; i < 25; i++) {
			System.out.print(" " + bc.nativeToPlayerPoint(i, 2));
			if (i == 12) {
				System.out.println();
			}
		}
		System.out.println();

	}

}
