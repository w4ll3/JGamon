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
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * A monochrome resizable implementation
 *
 * @author Mattias Ulbrich
 */
public class MonochromeBoardImplementation extends AbstractBoardImplementation {


	public final static Color colorJag1 = new Color(.7f, .7f, .7f);
	public final static Color colorJag2 = new Color(.8f, .8f, .8f);
	public final static Color colorBar = new Color(.9f, .9f, .9f);

	public final static Color colorPlayer1 = new Color(.95f, .95f, .95f);
	public final static Color colorPlayer2 = new Color(.4f, .4f, .4f);

	public final static double LENGTH_OF_JAG = .4;
	public final static double WIDTH_OF_TOKEN = .8;
	public final static double LENGTH_OF_DICE = .07;

	private int jagheight;
	private int xstep;
	private int tokensize;
	private int tokenheight;

	private Dimension size = new Dimension(700, 500);


	public void init(BoardComponent board) throws Exception {
		super.init(board);

		String language = Locale.getDefault().getLanguage();
		if (language.equals("de")) {
			color1 = "wei�";
			color2 = "schwarz";
		} else {
			color1 = "white";
			color2 = "black";
		}

		sizeChanged();

	}


	/**
	 * get the name of this Implementation.
	 *
	 * @return String used in Menus etc.
	 */
	public String toString() {
		Locale l = Locale.getDefault();
		if (Locale.getDefault().getLanguage().equals("de")) {
			return "Monochrom - Gr��e �nderbar";
		} else {
			return "Monochrome - resizable";
		}
	}

	/**
	 * @param board Board to paint
	 * @param g     Graphics to be used
	 */
	public void drawBoard(Graphics g) {
		// white background
		g.setColor(Color.white);
		g.fillRect(0, 0, size.width, size.height);

		int barwidth = size.width - 14 * xstep;

		// jags
		for (int i = 1; i <= 12; i++) {
			int pos = i * xstep;
			if (i >= 7) {
				pos += barwidth;
			}

			Polygon pg = new Polygon();
			pg.addPoint(pos, 0);
			pg.addPoint(pos + xstep, 0);
			pg.addPoint(pos + xstep / 2, jagheight);
			g.setColor(i % 2 == 0 ? colorJag1 : colorJag2);
			g.fillPolygon(pg);

			pg = new Polygon();
			pg.addPoint(pos, size.height - 1);
			pg.addPoint(pos + xstep, size.height);
			pg.addPoint(pos + xstep / 2, size.height - jagheight);
			g.setColor(i % 2 == 1 ? colorJag1 : colorJag2);
			g.fillPolygon(pg);
		}

		// bar
		g.setColor(colorBar);
		g.fillRect(7 * xstep, 0, barwidth, size.height);

		// outs
		g.fillRect(size.width - xstep, 0, xstep, size.height);
		g.fillRect(0, 0, xstep, size.height);

	}

	/**
	 * paint an icon to a Graphics content.
	 *
	 * @param g     Graphics to be used
	 * @param color 1, 2 or -1, -2 (- for preview chips)
	 * @param p     Point
	 */
	public void drawChecker(Graphics g, int player, Point p) {
		Color color = Math.abs(player) == 1 ? colorPlayer1 : colorPlayer2;
		if (player < 0) {
			g.setColor(color);
			g.fillOval(p.x + 5, p.y + 5, tokensize - 10, tokensize - 10);
			g.setColor(Color.black);
			g.drawOval(p.x + 5, p.y + 5, tokensize - 10, tokensize - 10);
		} else {
			g.setColor(color);
			g.fillOval(p.x, p.y, tokensize, tokensize);
			g.setColor(Color.black);
			g.drawOval(p.x, p.y, tokensize, tokensize);
		}
	}

	/**
	 * draw image for the dice
	 *
	 * @param g     dice to be painted. (0<=number<=5);
	 * @param color int
	 * @param dice  int[]
	 */
	public void drawDice(Graphics g, int color, int[] dice) {

		int dim = (int) (size.height * LENGTH_OF_DICE);
		int ypos = (size.height - dim) / 2;
		int xpos = color == 1 ? 2 * xstep : size.width - 4 * xstep - dim;

		Color bg = color == 1 ? colorPlayer1 : colorPlayer2;
		Color fg = color == 1 ? colorPlayer2 : colorPlayer1;

		// first dice
		drawDice(g, xpos, ypos, dim, dice[0], fg, bg);

		if (dice.length == 2)
			drawDice(g, xpos + 2 * xstep, ypos, dim, dice[1], fg, bg);

	}

	/**
	 * drawDice does the actual job of drawing
	 */
	private void drawDice(Graphics g, int xpos, int ypos, int dim, int value,
	                      Color fg, Color bg) {
		g.setColor(Color.black);
		g.drawRect(xpos, ypos, dim, dim);
		g.setColor(bg);
		g.fillRect(xpos, ypos, dim, dim);

		double step = ((double) dim) / 7.0;
		g.setColor(fg);
		switch (value) {
			case 1:
				g.fillOval(xpos + (int) (3 * step), ypos + (int) (3 * step),
						(int) step, (int) step);
				break;
			case 2:
				g.fillOval(xpos + (int) (2 * step), ypos + (int) (2 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (4 * step), ypos + (int) (4 * step),
						(int) step, (int) step);
				break;
			case 3:
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (3 * step), ypos + (int) (3 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				break;
			case 4:
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				break;
			case 5:
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (3 * step), ypos + (int) (3 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				break;
			case 6:
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (3 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (1 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (1 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (3 * step),
						(int) step, (int) step);
				g.fillOval(xpos + (int) (5 * step), ypos + (int) (5 * step),
						(int) step, (int) step);
				break;

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

		if (index == -1)
			return;

		int dim = (int) (size.height * LENGTH_OF_DICE);
		int ypos = getCheckerDiameter() / 2;
		if (!top)
			ypos = size.height - ypos - dim;
		int xpos = (size.width - dim) / 2;

		int value = 1 << (index + 1);

		g.setColor(Color.black);
		g.drawRect(xpos, ypos, dim, dim);
		g.setColor(Color.white);
		g.fillRect(xpos, ypos, dim, dim);

		g.setColor(Color.black);
		Font f = new Font("serif", Font.BOLD, size.height / 16);
		g.setFont(f);
		FontMetrics fontMetrics = g.getFontMetrics(f);
		int font_width = fontMetrics.stringWidth("" + value);
		int font_height = (int) (fontMetrics.getAscent() * .8); // little hack, Ascent is too large

		g.drawString("" + value, (size.width - font_width) / 2, ypos + (dim + font_height) / 2);
	}

	/**
	 * get the image of the chip of player1 from the side.
	 *
	 * @param g     Graphics
	 * @param color int
	 * @param p     Point
	 */
	public void drawThinChecker(Graphics g, int player, Point p) {
		Color color = player == 1 ? colorPlayer1 : colorPlayer2;
		g.setColor(color);
		g.fillRect(p.x, p.y, tokensize, tokenheight - 2);
		g.setColor(Color.black);
		g.drawRect(p.x, p.y, tokensize, tokenheight - 2);
	}

	/**
	 * @return diameter in pixel.
	 */
	public int getCheckerDiameter() {
		return tokensize;
	}

	/**
	 * render the chip for a color to an ImageIcon.
	 *
	 * @param select Used to choose the desired icon
	 * @return an ImageIcon
	 */
	public ImageIcon getCheckerIcon(int select) {
		Color color = select == 1 ? colorPlayer1 : colorPlayer2;
		BufferedImage bi = new BufferedImage(31, 31,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(color);
		g.fillOval(0, 0, 30, 30);
		g.setColor(Color.black);
		g.drawOval(0, 0, 30, 30);
		return new ImageIcon(bi);
	}

	/**
	 * @return thickness in pixels
	 */
	public int getChipThickness() {
		return tokenheight;
	}

	/**
	 * get the dimension of a jag on the board.
	 *
	 * @return width in pixels
	 */
	public Dimension2D getPointDimension() {
		return new Dimension(xstep, jagheight);
	}

	/**
	 * get the size of the board.
	 *
	 * @return Dimension of this board.
	 */
	public Dimension getSize() {
		return size;
	}

	public Dimension getPreferredSize() {
		return new Dimension(600, 400);
	}

	/**
	 * set the size for the underlying board.
	 *
	 * @param size the Dimension that
	 * @return Dimension the dimension that has been set.
	 */
	public Dimension setSize(Dimension size) {
		this.size = size;
		size.width = Math.max(100, size.width);
		size.height = Math.max(100, size.height);
		sizeChanged();
		return size;
	}


	private void sizeChanged() {
		jagheight = (int) (size.height * LENGTH_OF_JAG);
		xstep = size.width / 15;

		// calculate the token size: either percentage of width or 1/5
		// of the height of a jag (so it wont be overfull)
		int tokensize1 = (int) (WIDTH_OF_TOKEN * xstep);
		int tokensize2 = jagheight / 5;
		tokensize = Math.min(tokensize1, tokensize2);
		tokenheight = jagheight / 15;
	}

	/**
	 * get upper left corner of the board.
	 *
	 * @return Point
	 */
	public Point getUpperLeftCorner() {
		return new Point(xstep, 0);
	}

	/**
	 * get the coordinates of the upper left out field.
	 *
	 * @return Rectangle
	 */
	public Rectangle getUpperLeftOutField() {
		return new Rectangle(0, 0, xstep, jagheight);
	}

	/**
	 * get the maximum size of this implementation.
	 * <p>
	 * this one is actually unlimited!
	 *
	 * @return not null
	 */
	public Dimension getMaximumSize() {
		return new Dimension(100000, 100000);
	}

	/**
	 * get the mininum size of this implementation.
	 * <p>
	 * say, ... 40x50 pixels
	 *
	 * @return not null
	 */
	public Dimension getMinimumSize() {
		return new Dimension(50, 40);
	}
}
