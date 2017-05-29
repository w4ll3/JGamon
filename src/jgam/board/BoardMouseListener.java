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

import jgam.JGammon;
import jgam.game.BoardSetup;
import jgam.game.Move;
import jgam.game.Player;
import jgam.game.WrappedBoardSetup;
import jgam.gui.UIPlayer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Dimension2D;
import java.util.Iterator;
import java.util.Set;

/**
 * This is used in Board to react to mouse events, such as drag and drop and
 * double click.
 * <p>
 * During Board.paint() its paint() method is also called.
 *
 * @author Mattias Ulbrich
 */
public class BoardMouseListener implements MouseListener, MouseMotionListener,
		Paintable {

	private BoardComponent board;
	private JGammon jgam;

	// if dragging around, what chip at what position
	private int draggingPlayer;
	private Point position;

	// store the possible goals, they are painted in green
	private Set possibleMoves;

	// where dragging began
	private int startPoint;

	// the setup that was valid before this appeared.
	private BoardSetup oldSetup;

	public BoardMouseListener(BoardComponent b, JGammon g) {
		board = b;
		jgam = g;
		b.addMouseListener(this);
		b.addMouseMotionListener(this);
		b.addPaintHook(this);
	}

	/**
	 * when the mouse is moved parts of the board have to repainted.
	 * The cliping area is calculated so that the mouse movement is
	 * correctly repainted. (old + new dragged checker must be
	 * repainted)
	 *
	 * @param e get the new mouse position from this
	 */
	public void mouseDragged(MouseEvent e) {
		if (draggingPlayer == 0) {
			return;
		}

		int diamt = board.getBoardImplementation().getCheckerDiameter();
		Rectangle clipBox = new Rectangle(position.x - diamt / 2 - 1, position.y - diamt / 2 - 1, diamt + 2, diamt + 2);

		position = e.getPoint();
		clipBox.add(new Rectangle(position.x - diamt / 2 - 1, position.y - diamt / 2 - 1, diamt + 2, diamt + 2));

		board.repaint(clipBox);
	}

	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * make the longest move that is possible from the current position.
	 *
	 * @param e MouseEvent
	 * @since 1.062 changed behavior. no longer for the only possible.
	 */
	public void mouseClicked(MouseEvent e) {

		if (e.getClickCount() != 2) {
			return;
		}

		UIPlayer player = getPlayer();

		if (player == null || !player.isWaitingForUIMove()) {
			return;
		}

		Point position = e.getPoint();
		int playerno = player.getNumber();
		int point = calcPlayerPoint(position, playerno);

		if (point == -1) {
			return;
		}

		Move m = player.getDoubleClickMove(point);

		if (m != null) {
			player.handleMove(m);
		}
	}

	/**
	 * getPlayer
	 *
	 * @return UIPlayer
	 */
	private UIPlayer getPlayer() {
		Player player = null;

		if (jgam.getGame() != null) {
			player = jgam.getGame().getCurrentPlayer();
		}

		if (player == null || !player.isLocal()) {
			return null;
		}

		return (UIPlayer) player;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * dragging begins
	 *
	 * @param e MouseEvent
	 */
	public void mousePressed(MouseEvent e) {
		position = e.getPoint();
		BoardSetup originalBoardSetup = board.getBoardSnapshot();

		UIPlayer player = getPlayer();

		if (player == null || !player.isWaitingForUIMove()) {
			return;
		}

		int playerno = player.getNumber();
		startPoint = calcPlayerPoint(position, playerno);

		if (startPoint == -1) {
			return;
		}

		if (originalBoardSetup.getPoint(playerno, startPoint) <= 0) {
			startPoint = -1;
			return;
		}

		draggingPlayer = playerno;
		oldSetup = originalBoardSetup;
		board.useBoardSetup(new DraggedBoardSetup(originalBoardSetup, draggingPlayer, startPoint));
		possibleMoves = player.getPossibleMovesFrom(startPoint);

		// repaint in order to show the preview checkers!
		board.repaint();
	}

	/**
	 * dragging ends
	 *
	 * @param e MouseEvent
	 */
	public void mouseReleased(MouseEvent e) {
		position = e.getPoint();
		UIPlayer player = getPlayer();

		if (player == null || !player.isWaitingForUIMove() || startPoint == -1) {
			return;
		}

		int endPoint = calcPlayerPoint(e.getPoint(), draggingPlayer);

		draggingPlayer = 0;
		if (endPoint == -1) {
			restoreOldSetup();
			return;
		}

		// choose the shortest move from startTag to endTag
		if (possibleMoves != null) {
			for (Iterator iter = possibleMoves.iterator(); iter.hasNext(); ) {
				Move move = (Move) iter.next();
				if (move.to() == endPoint) {
					player.handleMove(move);
					return;
				}
			}
		}

		restoreOldSetup();
	}

	/**
	 * restore the BoardSetup that was valid before me
	 */
	private void restoreOldSetup() {
		board.useBoardSetup(oldSetup);
	}

	/**
	 * draw the the dragged chip and the green possible goals
	 *
	 * @param g Graphics to draw to
	 */
	public void paint(Graphics g) {
		if (draggingPlayer != 0) {

			if (jgam.getFrame().showPreviewCheckers()) {
				for (Iterator iter = possibleMoves.iterator(); iter.hasNext(); ) {
					Move m = (Move) iter.next();
					if (m.to() == 0) {
						paintGreenOff(draggingPlayer, g);
					} else {
						int point = board.playerToNativePoint(m.to(),
								draggingPlayer);
						board.paintChip(-draggingPlayer, g, point,
								oldSetup.getPoint(m.player(), m.to()));
					}
				}
			}

			int w = board.getBoardImplementation().getCheckerDiameter();
			Point p = new Point(position.x - w / 2,
					position.y - w / 2);

            /* bugfix: if the implementation is smaller than the component itself */
			Dimension componentSize = board.getSize();
			Dimension implSize = board.getBoardImplementation().getSize();
			if (!componentSize.equals(implSize)) {
				p.translate(-(componentSize.width - implSize.width) / 2,
						-(componentSize.height - implSize.height) / 2);
			}
	        /* end of bugfix */

			board.getBoardImplementation().drawChecker(g, board.getColorForPlayer(draggingPlayer), p);
		}

		// paint the mouse sensitive areas for debug purposes
		if (Boolean.getBoolean("jgam.board.debug")) {
			paintMouseAreas(g);
		}

	}


	/**
	 * debug facility so that errors can be found.
	 * It draws the areas in which the mouse reacts.
	 *
	 * @param g Graphics to paint to
	 */
	public void paintMouseAreas(Graphics g) {
		g.setColor(Color.red);
		for (int i = 1; i < 25; i++) {
			Rectangle window = getPointWindow(i);
			g.drawRect(window.x, window.y, window.width, window.height);
		}
		Rectangle r = getOffField();
		g.drawRect(r.x, r.y, r.width, r.height);
		r = getBar();
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	public void paintGreenOff(int playerno, Graphics g) {

		Rectangle r = board.getOffField(playerno);

        /* in case the out is wider than a chip.
         * center it */
		int xpos = r.x +
				(r.width - board.getBoardImplementation().getCheckerDiameter()) /
						2;
		int ypos = r.y + r.height -
				board.getBoardImplementation().getCheckerDiameter();

		board.getBoardImplementation().drawChecker(g, -draggingPlayer, new Point(xpos, ypos));

	}

	/**
	 * calculate the native point number of a point.
	 * Native points are explained in Board.java
	 * 0 1 2 3 .. 11
	 * 12 ... 23
	 *
	 * @param point Point within the board
	 * @return 1-24 a native point number, -1 if none
	 */
	private int calcPoint(Point location) {
		for (int i = 1; i < 25; i++) {
			if (getPointWindow(i).contains(location)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * return the window for a native jag.
	 *
	 * @param index nativ index of the jag 1-24
	 * @return the window of this jag.
	 */
	private Rectangle getPointWindow(int index) {
		index--; // we start at 0 for simplicity
		Dimension2D pointwindow = board.getBoardImplementation().getPointDimension();
		Rectangle ret = new Rectangle();
		ret.setLocation(board.getBoardImplementation().getUpperLeftCorner());
		ret.setSize((int) pointwindow.getWidth(), (int) pointwindow.getHeight());
		ret.x += (int) ((index % 12) * pointwindow.getWidth());
		if ((index % 12) >= 6) {
			ret.x += board.getBarWidth();
		}
		if (index >= 12) {
			ret.y = board.getBoardImplementation().getSize().height - ret.y -
					ret.height;
		}

		return ret;
	}

	/**
	 * calculate the player jag number of a point. for the current Player
	 * Native jags are explained in Board.java
	 * out is 0 and bar is 25
	 *
	 * @param point    Point to check
	 * @param playerno player to check for
	 * @return out (0), bar (25), jag (1-24) or -1 if not within any of these.
	 */
	private int calcPlayerPoint(Point point, int playerno) {
        /* bugfix: if the implementation is smaller than the component itself */
		point = (Point) point.clone();
		Dimension componentSize = board.getSize();
		Dimension implSize = board.getBoardImplementation().getSize();
		if (!componentSize.equals(implSize)) {
			point.translate(-(componentSize.width - implSize.width) / 2,
					-(componentSize.height - implSize.height) / 2);
		}
        /* end of bugfix */

		int nat = calcPoint(point);
		if (nat == -1) {
			if (getOffField().contains(point)) {
				return 0; // out
			}
			if (getBar().contains(point)) {
				return 25;
			}
			return -1;
		}

		return board.nativeToPlayerPoint(nat, playerno);
	}

	/**
	 * return the outField for the current player.
	 *
	 * @return Rectangle
	 */
	private Rectangle getOffField() {
		Rectangle r = board.getOffField(draggingPlayer);

		return r;
	}

	private Rectangle getBar() {
		BoardImplementation imp = board.getBoardImplementation();
		int x = imp.getUpperLeftCorner().x +
				(int) (6 * imp.getPointDimension().getWidth());
		int y = imp.getUpperLeftCorner().y;
		int w = board.getBarWidth();
		int h = imp.getSize().height - (2 * imp.getUpperLeftCorner().y);
		Rectangle r = new Rectangle(x, y, w, h);

		return r;
	}

	/**
	 * force this listener to drop the checker which it possibly held
	 * to where it originally was.
	 * <p>
	 * This is needed if a messagebox pops up while dragging a checker.
	 */
	public void drop() {
		draggingPlayer = 0;
		restoreOldSetup();
	}

}


class DraggedBoardSetup extends WrappedBoardSetup {

	int playerno;
	int pointno;

	DraggedBoardSetup(BoardSetup bs, int playerno, int pointno) {
		super(bs);
		this.playerno = playerno;
		this.pointno = pointno;
	}

	/**
	 * get the number of checkers on a specific point for a player.
	 * If the playerno and the pointno match return a value reduced by 1, else
	 * the value of the inner boardSetup
	 */
	public int getPoint(int player, int pointnumber) {
		if (player == this.playerno && pointnumber == this.pointno) {
			return board.getPoint(player, pointnumber) - 1;
		} else {
			return board.getPoint(player, pointnumber);
		}
	}

}
