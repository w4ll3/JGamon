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
import java.util.Hashtable;

/**
 * A subclass of AbstractBoardImplementation that supports scaling of bitmap
 * images.
 * <p>
 * by resizing the corresponding BoardComponent all scaled images are discarded.
 * The scaled copies of the original images are provided on demand.
 * <p>
 * Aspect ratio is always kept as there is only one scaling factor.
 * A minimum and maximum factor can be set to limit the reszing.
 *
 * @author Mattias Ulbrich
 */
public abstract class ScalableBoardImplementation extends AbstractBoardImplementation {

	private Hashtable scaledInstances;

	private double factor = 1.0;

	private static final int DEFAULT_SCALE_METHOD =
			Integer.getInteger("jgam.board.scalemethod", Image.SCALE_SMOOTH).intValue();

	protected double factor_minimal = 0.5;
	protected double factor_maximal = 2;

	public void init(BoardComponent board) throws Exception {
		super.init(board);
		scaledInstances = new Hashtable();
		factor = 1.0;
	}

	/**
	 * paint the background. If it has not been resized yet, resize.
	 *
	 * @param g Graphics to be used
	 */
	public void drawBoard(Graphics g) {
		int method = Image.SCALE_FAST;
		Image scaledBackground = getScaledInstance(background, method);

		g.drawImage(scaledBackground, 0, 0, getBoard());
	}


	/**
	 * get the image for the chip of player 1.
	 *
	 * @return the image of the chip of player 1
	 */
	public ImageIcon getCheckerIcon(int color) {

		if (color == 1) {
			return new ImageIcon(getScaledInstance(chip1));
		}
		if (color == 2) {
			return new ImageIcon(getScaledInstance(chip2));
		}
		if (color == -1 || color == -2) {
			return new ImageIcon(getScaledInstance(previewChip));

		}
		throw new IllegalArgumentException(
				"One of {1,2,-1,-2} expected, but got: " + color);
	}

	/**
	 * get the diameter of the checkers.
	 * <p>
	 * takes the width of scaledChip1.
	 *
	 * @return diameter in pixel.
	 */
	public int getCheckerDiameter() {
		return (int) (super.getCheckerDiameter() * factor);
	}

	/**
	 * get the thickness of the chips.
	 * <p>
	 * returns the height of thinChip1
	 *
	 * @return thickness in pixels
	 */
	public int getChipThickness() {
		return (int) (super.getChipThickness() * factor);
	}


	/**
	 * get the dimension of a point on the board.
	 *
	 * @return width in pixels
	 */
	public Dimension2D getPointDimension() {
		DoubleDimension d = new DoubleDimension();
		d.setSize(pointDimension.getWidth() * factor,
				pointDimension.getHeight() * factor);
		return d;
	}

	/**
	 * get upper left corner of the board.
	 *
	 * @return Point
	 */
	public Point getUpperLeftCorner() {
		return new Point((int) (upperLeftCorner.x * factor),
				(int) (upperLeftCorner.y * factor));
	}

	/**
	 * get the coordinates of the upper left out field.
	 *
	 * @return Rectangle
	 */
	public Rectangle getUpperLeftOutField() {
		return new Rectangle((int) (upperLeftOutField.x * factor),
				(int) (upperLeftOutField.y * factor),
				(int) (upperLeftOutField.width * factor),
				(int) (upperLeftOutField.height * factor));
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
			g.drawImage(getScaledInstance(thinChip1), p.x, p.y, getBoard());
		} else {
			g.drawImage(getScaledInstance(thinChip2), p.x, p.y, getBoard());
		}
	}

	/**
	 * get the image for a dice for player 1.
	 *
	 * @param g     dice to be painted. (0<=number<=5);
	 * @param color int
	 * @param dice  int[]
	 */
	public void drawDice(Graphics g, int color, int[] d) {
		Point p = (Point) leftDiceLocation.clone();
		p.x = (int) (factor * p.x);
		p.y = (int) (factor * p.y);

		if (color == 1) {
			g.drawImage(getScaledInstance(dice1[d[0] - 1]), p.x, p.y, getBoard());

			if (d.length > 1) {
				p.translate((int) (factor * diceDistance), 0);
				g.drawImage(getScaledInstance(dice1[d[1] - 1]), p.x, p.y, getBoard());
			}
		} else {
			p.x = getSize().width - (int) (factor * dice2[0].getIconWidth()) - p.x;
			g.drawImage(getScaledInstance(dice2[d[0] - 1]), p.x, p.y, getBoard());
			if (d.length > 1) {
				p.translate(-(int) (factor * diceDistance), 0);
				g.drawImage(getScaledInstance(dice2[d[1] - 1]), p.x, p.y, getBoard());
			}

		}

	}

	/**
	 * get the image for the doubleDice.
	 *
	 * @param g     Graphics
	 * @param index index of the cube sides.
	 * @param top   true if in the upper half of the field
	 */

	public void drawDoubleDice(Graphics g, int index, boolean top) {
		if (index == -1) {
			// simple rotation won't match because of the lights!
			// extra image needed
			if (turnedDoubleDice != null) {
				int x = (int) (-turnedDoubleDice.getIconWidth() * factor) + getSize().width / 2;
				int y = (int) (-turnedDoubleDice.getIconHeight() * factor) + getSize().height / 2;
				g.drawImage(getScaledInstance(turnedDoubleDice), x, y, getBoard());
			}
			return;
		}

		// get the corresponding icon

		int xpos = (int) ((getSize().width - doubleDice[index].getIconWidth() * factor) / 2);
		int ypos;
		if (!top) {
			ypos = getSize().height - (int) ((doubleDiceDistance +
					doubleDice[index].getIconHeight()) * factor);
		} else {
			ypos = (int) (doubleDiceDistance * factor);
		}

		g.drawImage(getScaledInstance(doubleDice[index]), xpos, ypos, getBoard());

	}

	/**
	 * return a scaled copy of an image. factor is used as scale factor.
	 * <p>
	 * The method is read from the system property "jgam.board.scalemethod".
	 * Or if not defined Image.SCALE_SMOOTH is used.
	 *
	 * @param imageIcon ImageIcon to be scaled
	 * @return Image scaled image.
	 */
	protected Image getScaledInstance(ImageIcon imageIcon) {
		return getScaledInstance(imageIcon, DEFAULT_SCALE_METHOD);
	}

	/**
	 * return a scaled copy of an image. factor is used as scale factor.
	 *
	 * @param imageIcon ImageIcon to be scaled
	 * @param method    the scale method to be used
	 * @return Image scaled image.
	 */
	protected Image getScaledInstance(ImageIcon imageIcon, int method) {
		if (factor == 1.0) {
			return imageIcon.getImage();
		}

		Image image = (Image) scaledInstances.get(imageIcon);

		if (image == null) {
			image = imageIcon.getImage().
					getScaledInstance((int) (imageIcon.getIconWidth() * factor),
							(int) (imageIcon.getIconHeight() * factor),
							method);
			scaledInstances.put(imageIcon, image);
		}

		return image;
	}

	/**
	 * get the size of the board.
	 *
	 * @return Dimension of this board.
	 */
	public Dimension getSize() {
		Dimension orig = super.getSize();
		return new Dimension((int) (orig.width * factor), (int) (orig.height * factor));
	}


	/**
	 * when this view is to be resized, then all scaled images are cleared.
	 * <p>
	 * The factor to be used is the minimum of the x- ans y-factor
	 * It has to keep smaller than maximal and smaller then minimal factor
	 *
	 * @param size Dimension proposed
	 * @return Dimension that is used
	 */
	public Dimension setSize(Dimension size) {

		// dont do anything if same size!
		if (getSize().equals(size)) {
			return size;
		}

		Dimension orig = super.getSize();
		double factorX = size.getWidth() / orig.getWidth();
		double factorY = size.getHeight() / orig.getHeight();
		factor = Math.min(factorX, factorY);
		factor = Math.min(factor_maximal, factor);
		factor = Math.max(factor_minimal, factor);

		scaledInstances.clear();

		return new Dimension((int) (factor * orig.width),
				(int) (factor * orig.height));
	}

	public Dimension getPreferredSize() {
		return super.getSize();
	}

	/**
	 * get the maximum size of this implementation.
	 *
	 * @return not null
	 */
	public Dimension getMaximumSize() {
		Dimension d = super.getSize();
		return new Dimension((int) (d.width * factor_maximal), (int) (d.height * factor_maximal));
	}

	/**
	 * get the mininum size of this implementation.
	 *
	 * @return not null
	 */
	public Dimension getMinimumSize() {
		Dimension d = super.getSize();
		return new Dimension((int) (d.width * factor_minimal), (int) (d.height * factor_minimal));
	}


	/**
	 * when creating a new instance, reset scaling to 1.0
	 *
	 * @return BoardImplementation
	 */
	public BoardImplementation newInstance() {
		ScalableBoardImplementation ret = (ScalableBoardImplementation) super.newInstance();
		ret.factor = 1.0;
		ret.scaledInstances = null;
		return ret;
	}

	/**
	 * use this <i>getter</i> method to query the value of the field <code>factor</code>.
	 *
	 * @return the value of <code>factor</code>
	 */
	public double getFactor() {
		return this.factor;
	}


}


class DoubleDimension extends Dimension2D {

	double height;
	double width;

	/**
	 * Returns the height of this <code>Dimension</code> in double precision.
	 *
	 * @return the height of this <code>Dimension</code>.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Returns the width of this <code>Dimension</code> in double precision.
	 *
	 * @return the width of this <code>Dimension</code>.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Sets the size of this <code>Dimension2D</code> object to match the
	 * specified size.
	 *
	 * @param d the new size for the <code>Dimension2D</code> object
	 */
	public void setSize(Dimension2D d) {
		height = d.getHeight();
		width = d.getWidth();
	}

	/**
	 * Sets the size of this <code>Dimension</code> object to the specified
	 * width and height.
	 *
	 * @param width  the new width for the <code>Dimension</code> object
	 * @param height the new height for the <code>Dimension</code> object
	 */
	public void setSize(double width, double height) {
		this.height = height;
		this.width = width;
	}

}
