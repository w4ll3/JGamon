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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Dimension2D;

/**
 * The abstract derivation of BoardImplementation.
 * <p>
 * It provides a lot variables that can be set.
 * A subclass may set these values in the init-Method in order to
 * describe a fixed-size implementation.
 *
 * @author Mattias Ulbrich
 */
public abstract class AbstractBoardImplementation implements
		BoardImplementation, Cloneable {

	protected ImageIcon background;
	protected Color backgroundFillColor;

	protected ImageIcon chip1;
	protected ImageIcon thinChip1;
	protected ImageIcon chip2;
	protected ImageIcon thinChip2;
	protected ImageIcon previewChip;

	protected ImageIcon doubleDice[] = new ImageIcon[6];
	protected ImageIcon turnedDoubleDice;
	protected ImageIcon[] dice1 = new ImageIcon[6];
	protected ImageIcon[] dice2 = new ImageIcon[6];

	protected Dimension pointDimension;
	protected Point leftDiceLocation;
	protected Point upperLeftCorner;
	protected Rectangle upperLeftOutField;
	protected int diceDistance;
	protected int doubleDiceDistance;
	protected int barWidth;

	protected String color1;
	protected String color2;

	private BoardComponent board;
	private boolean alreadyInitialized;


	public void init(BoardComponent board) throws Exception {
		this.board = board;
		this.alreadyInitialized = true;
	}

	/**
	 * paint the background, which is a fixed size image
	 *
	 * @param g Graphics to be used
	 */
	public void drawBoard(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, board);
	}

	/**
	 * get the image for the chip of player 1.
	 *
	 * @return the image of the chip of player 1
	 */
	public ImageIcon getCheckerIcon(int color) {
		if (color == 1) {
			return chip1;
		}
		if (color == 2) {
			return chip2;
		}
		if (color == -1 || color == -2) {
			return previewChip;
		}
		throw new IllegalArgumentException(
				"One of {1,2,-1,-2} expected, but got: " + color);
	}

	/**
	 * paint an icon to a Graphics content.
	 * at the position p
	 *
	 * @param board Board to paint
	 * @param g     Graphics to be used
	 * @param color either 0 or 1 to choose the color
	 */
	public void drawChecker(Graphics g, int color, Point p) {
		ImageIcon icon = getCheckerIcon(color);
		p = new Point(p.x + (getCheckerDiameter() - icon.getIconWidth()) / 2,
				p.y + (getCheckerDiameter() - icon.getIconHeight()) / 2);
		g.drawImage(icon.getImage(), p.x, p.y, getBoard());
	}

	/**
	 * get the diameter of the chips.
	 * <p>
	 * takes the width of chip1.
	 *
	 * @return diameter in pixel.
	 */
	public int getCheckerDiameter() {
		return chip1.getIconWidth();
	}

	/**
	 * get the thickness of the chips.
	 * <p>
	 * returns the height of thinChip1
	 *
	 * @return thickness in pixels
	 */
	public int getChipThickness() {
		return thinChip1.getIconHeight();
	}


	/**
	 * get the dimension of a point on the board.
	 *
	 * @return width in pixels
	 */
	public Dimension2D getPointDimension() {
		return (Dimension) pointDimension.clone();
	}

	/**
	 * get the size of the board.
	 *
	 * @return Dimension of this board.
	 */
	public Dimension getSize() {
		return new Dimension(background.getIconWidth(),
				background.getIconHeight());
	}


	/**
	 * get upper left corner of the board.
	 *
	 * @return Point
	 */
	public Point getUpperLeftCorner() {
		return new Point(upperLeftCorner);
	}

	/**
	 * get the coordinates of the upper left out field.
	 *
	 * @return Rectangle
	 */
	public Rectangle getUpperLeftOutField() {
		return new Rectangle(upperLeftOutField);
	}


	/**
	 * the color of the first player. needed to print it in the label.
	 *
	 * @return localised String describing the color.
	 */
	public String getColorName1() {
		return color1;
	}

	/**
	 * the color of the second player. needed to print it in the label.
	 *
	 * @return localised String describing the color.
	 */
	public String getColorName2() {
		return color2;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>board</code>.
	 *
	 * @return the value of <code>board</code>
	 */
	public BoardComponent getBoard() {
		return this.board;
	}

	public abstract String toString();

	/**
	 * draw the dice.
	 * The location is calculated with leftDiceLocation and diceDistance.
	 * <p>
	 * load the appropriate icon and display it.
	 *
	 * @param g     Graphics to paint on
	 * @param color 1 or 2
	 * @param dice  may not be null
	 */
	public void drawDice(Graphics g, int color, int[] d) {

		Point p = (Point) leftDiceLocation.clone();
		if (color == 1) {

			g.drawImage(dice1[d[0] - 1].getImage(), p.x, p.y, board);
			if (d.length > 1) {
				p.translate(diceDistance, 0);
				g.drawImage(dice1[d[1] - 1].getImage(), p.x, p.y, board);
			}

		} else {

			p.x = getSize().width - dice1[0].getIconWidth() - p.x;
			g.drawImage(dice2[d[0] - 1].getImage(), p.x, p.y, board);
			if (d.length > 1) {
				p.translate(-diceDistance, 0);
				g.drawImage(dice2[d[1] - 1].getImage(), p.x, p.y, board);
			}

		}
	}

	/**
	 * paint the double cube at the appropriate place.
	 *
	 * @param g     Graphic context to draw with
	 * @param index int
	 * @param top   boolean
	 */
	public void drawDoubleDice(Graphics g, int index, boolean top) {

		if (index == -1) {
			// simple rotation won't match because of the lights!
			// extra image needed
			if (turnedDoubleDice != null) {
				int x = -turnedDoubleDice.getIconWidth() + getSize().width / 2;
				int y = -turnedDoubleDice.getIconHeight() + getSize().height / 2;
				g.drawImage(turnedDoubleDice.getImage(), x, y, board);
			}
			return;
		}

		// get the corresponding icon
		ImageIcon theIcon = doubleDice[index];

		int xpos = (getSize().width - theIcon.getIconWidth()) / 2;
		int ypos;
		if (!top) {
			ypos = getSize().height - doubleDiceDistance -
					theIcon.getIconHeight();
		} else {
			ypos = doubleDiceDistance;
		}

		g.drawImage(theIcon.getImage(), xpos, ypos, board);

	}

	/**
	 * paint the thin chip (seen from the side)
	 *
	 * @param g     Graphics to paint with
	 * @param color 1 or 2
	 * @param p     location to paint ro
	 */
	public void drawThinChecker(Graphics g, int color, Point p) {

		if (color == 1) {
			g.drawImage(thinChip1.getImage(), p.x, p.y, board);
		} else {
			g.drawImage(thinChip2.getImage(), p.x, p.y, board);
		}

	}

	/**
	 * just keep the look and feel background then
	 *
	 * @param g Graphics to work with
	 */
	public void fillBackground(Graphics g) {
		if (backgroundFillColor != null) {
//            Color c = g.getColor();
			g.setColor(backgroundFillColor);
			g.fillRect(0, 0, getBoard().getWidth(), getBoard().getHeight());
		}
	}


	/**
	 * get a new copy of this BoardComponent
	 *
	 * @return BoardImplementation
	 */
	public BoardImplementation newInstance() {
		try {
			return (BoardImplementation) clone();
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}


	/**
	 * use this <i>getter</i> method to query the value of the field <code>alreadyInitialized</code>.
	 *
	 * @return the value of <code>alreadyInitialized</code>
	 */
	public boolean isAlreadyInitialized() {
		return this.alreadyInitialized;
	}



    /*   ****************************
     * we do not support resizing
     *   **************************** */

	public Dimension setSize(Dimension size) {
		return getSize();
	}

	public Dimension getPreferredSize() {
		return getSize();
	}

	public Dimension getMinimumSize() {
		return getSize();
	}

	public Dimension getMaximumSize() {
		return getSize();
	}


}
