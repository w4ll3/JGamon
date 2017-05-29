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
 * Implementation of a backgammon board layout.
 * <p>
 * It must provide the graphical objects for:
 * - background
 * - chips (aka tokens)
 * - chips from side (when played out)
 * - doubleDice
 * - dice
 * <p>
 * It must know how to handle resizing (may interact)
 * And must know about its maximal and minimal sizes
 *
 * @author Mattias Ulbrich
 */
public interface BoardImplementation {

	/**
	 * get the name of this Implementation. It should briefly describe the
	 * look and feel.
	 *
	 * @return String used in Menus etc.
	 */
	public String toString();

	/**
	 * initialize the implementation. Loading of ressources should be done
	 * here.
	 * This method may be called more than once, so a check might be
	 * necessary.
	 *
	 * @param board Board upon which we work
	 * @throws thrown if something went wrong
	 */
	public void init(BoardComponent board) throws Exception;

	/**
	 * get the size of the board.
	 * only called after init has been called.
	 *
	 * @return Dimension of this board.
	 */
	public Dimension getSize();

	/**
	 * set the size for the underlying board.
	 * <p>
	 * If this does not work (too large / too small / aspect ratio) the dimension
	 * may be changed and then set. The result of this method is the
	 * size that was actually set.
	 *
	 * @param dim the Dimension to be set
	 * @return Dimension the dimension that has been set.
	 */
	public Dimension setSize(Dimension size);

	/**
	 * get the preferred size of this implementation.
	 * it will be read by the component and used for it
	 *
	 * @return not null
	 */
	public Dimension getPreferredSize();

	/**
	 * get the mininum size of this implementation.
	 * it will be read by the component and used for it
	 *
	 * @return not null
	 */
	public Dimension getMinimumSize();

	/**
	 * get the maximum size of this implementation.
	 * it will be read by the component and used for it
	 *
	 * @return not null
	 */
	public Dimension getMaximumSize();


	/**
	 * paint the background to a Graphic context
	 *
	 * @param board Board to paint
	 * @param g     Graphics to be used
	 */
	public void drawBoard(Graphics g);

	/**
	 * paint an icon to a Graphics content.
	 * at the position p.
	 * Color may be 1 or 2. -1 and -2 are for preview chips of the
	 * corresponding colors.
	 *
	 * @param board Board to paint
	 * @param g     Graphics to be used
	 * @param color either 0 or 1 to choose the color
	 */
	public void drawChecker(Graphics g, int color, Point p);

	/**
	 * render the chip for a color to an ImageIcon. Every call should return
	 * the same size.
	 * 1 is for Icon for player 1,
	 * 2 is for Icon for player 2,
	 * -1 is for previewicon for player1
	 * and -2 is for previewicon for player2
	 *
	 * @param select Used to choose the desired icon
	 * @return an ImageIcon
	 */
	public ImageIcon getCheckerIcon(int select);

	/**
	 * get the image for a dice for player 1. This needs to be a image of same
	 * size for all arguments
	 *
	 * @param index dice to be painted. (0<=number<=5);
	 * @return ImageIcon
	 */
	public void drawDice(Graphics g, int color, int[] dice);

	/**
	 * get the image for the doubleDice.
	 * The values 2,4,6,8,16,32,64 are indexed with 0-5
	 * -1 is for the turned 64 dice. (top doesnt matter then)
	 *
	 * @param index index of the cube sides.
	 * @param top   true if in the upper half of the field
	 * @return ImageIcon
	 */
	public void drawDoubleDice(Graphics g, int index, boolean top);

	/**
	 * get the image of the chip of player1 from the side.
	 *
	 * @return ImageIcon
	 */
	public void drawThinChecker(Graphics g, int color, Point p);

	/**
	 * get the diameter of a chip in the game (=length=height=width)
	 *
	 * @return diameter in pixel.
	 */
	public int getCheckerDiameter();

	/**
	 * get the coordinates of the upper left out field.
	 *
	 * @return Rectangle
	 */
	public Rectangle getUpperLeftOutField();

	/**
	 * get upper left corner of the board.
	 * thats where upperleft point begins.
	 *
	 * @return Point
	 */
	public Point getUpperLeftCorner();

	/**
	 * get the dimension of a point on the board.
	 *
	 * @return width in pixels
	 */
	public Dimension2D getPointDimension();

	/**
	 * get the thickness of the chip (when seen from the side);
	 *
	 * @return thickness in pixels
	 */
	public int getChipThickness();

	/**
	 * the color of the first player. needed to print it in the label.
	 *
	 * @return localised String describing the color.
	 */
	public String getColorName1();

	/**
	 * the color of the second player. needed to print it in the label.
	 *
	 * @return localised String describing the color.
	 */
	public String getColorName2();

	/**
	 * if the BoardComponent is too large we can fill the background
	 * underneath upon which the board is centered then.
	 *
	 * @param g Graphics to work with
	 */
	public void fillBackground(Graphics g);

	/**
	 * clone this implementation so that it can be used with multiple
	 * BoardComponents.
	 *
	 * @return BoardImplementation
	 */
	public BoardImplementation newInstance();


}
