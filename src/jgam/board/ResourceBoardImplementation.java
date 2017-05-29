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
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;


/**
 * An implementation of ScalableBoardImplementation that is configured by a Properties
 * object.
 * <p>
 * An instance of ResourceBoardImplementation is usually created using the
 * constructor ResourceBoardImplementation(String resourcename)
 * <p>
 * For example launching
 * ResourceBoardImplementation("jgam.board.sample.SampleImplementation")
 * <p>
 * would result in searching for the resource "jgam/board/sample/SampleImplementation.properties"
 * in the classpath.
 * <p>
 * This file is then read and stored in a hashtable.
 * All values describing filenames are to be local to the properties directory
 * ("jgam/board/sample")
 * <p>
 * The following keys can be set using the usual <key>=<value> syntax of
 * property files:
 * - name (local string)
 * - background (Imagefile=IF)
 * - checker1 (IF)
 * - checker2 (IF)
 * - thinChecker1 (IF)
 * - thinChecker2 (IF)
 * - previewChecker (IF)
 * - doubleDice1 .. doubleDice6 (IF)
 * - dice1_1 ..dice1_6 (IF)
 * - dice2_1 ..dice2_6 (IF)
 * - pointDimension (Dimension <int, int>)
 * - leftDiceLocation (Point <int, int>)
 * - upperLeftOutField (Rectangle <int, int, int, int>)
 * - diceDistance (int)
 * - doubleDiceDistance (int)
 * - barWidth (int)
 * - color1 (local string)
 * - color2 (local string)
 * <p>
 * Attributes that are local strings are searched the following way:
 * 1. look for <key>_<language>  (for instance "name_de") using the default
 * language
 * 2. if not existing look for <key> without language extension
 *
 * @author Mattias Ulbrich
 */
public class ResourceBoardImplementation extends ScalableBoardImplementation {

	private Properties properties;
	private String packagePath;
	private Locale locale = Locale.getDefault();

	/**
	 * create a ResourceBoardImplementation that represents the resource name.
	 * <p>
	 * both the path and the resource name are deducted from the argument.
	 * The properties must be stored in a file with the suffix ".properties".
	 *
	 * @param resourcename String
	 * @throws Exception
	 */
	public ResourceBoardImplementation(String resourcename) throws IOException {
		super();

		// make resourcename compatible: . --> /
		resourcename = resourcename.replace('.', '/');

		// last bit is name, firstbit is packagePath
		int index = resourcename.lastIndexOf("/");
		packagePath = resourcename.substring(0, index);

		// resourcename is completename + ".properties"
		String name = resourcename + ".properties";
		properties = new Properties();
		properties.load(ClassLoader.getSystemResourceAsStream(name));
	}

	public void init(BoardComponent board) throws Exception {
		if (!isAlreadyInitialized()) {
			super.init(board);
			background = loadIcon("background");
			backgroundFillColor = getColor("backgroundFillColor");
			chip1 = loadIcon("checker1");
			thinChip1 = loadIcon("thinChecker1");
			chip2 = loadIcon("checker2");
			thinChip2 = loadIcon("thinChecker2");
			previewChip = loadIcon("previewChecker");

			for (int i = 0; i < 6; i++) {
				doubleDice[i] = loadIcon("doubleDice" + (i + 1));
				dice1[i] = loadIcon("dice1_" + (i + 1));
				dice2[i] = loadIcon("dice2_" + (i + 1));
			}

			turnedDoubleDice = loadIcon("turnedDoubleDice");

			pointDimension = getDimension("pointDimension");
			leftDiceLocation = getPoint("leftDiceLocation");
			upperLeftCorner = getPoint("upperLeftCorner");
			upperLeftOutField = getRectangle("upperLeftOutField");
			diceDistance = getInt("diceDistance", 0);
			doubleDiceDistance = getInt("doubleDiceDistance", 0);
			barWidth = getInt("barWidth", 0);

			color1 = getLocalString("color1");
			color2 = getLocalString("color2");

			factor_minimal = getDouble("minimalScaling", factor_minimal);
			factor_maximal = getDouble("maximalScaling", factor_maximal);
		}
	}

	public String toString() {
		return getLocalString("name");
	}

	protected ImageIcon loadIcon(String key) throws IOException {
		String value = (String) properties.get(key);
		if (value == null) {
			return null;
		}

		URL url = ClassLoader.getSystemResource(packagePath + "/" + value);
		if (url == null)
			throw new IOException("Resource not found: " + packagePath + "/" + value);
		return new ImageIcon(url);
	}

	protected Dimension getDimension(String key) {
		String value = (String) properties.get(key);
		if (value == null) {
			return null;
		}

		String[] tokens = value.split(", *");
		return new Dimension(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]));
	}

	protected Point getPoint(String key) {
		String value = (String) properties.get(key);
		if (value == null) {
			return null;
		}

		String[] tokens = value.split(", *");
		return new Point(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]));
	}

	protected Rectangle getRectangle(String key) {
		String value = (String) properties.get(key);
		if (value == null) {
			return null;
		}

		String[] tokens = value.split(", *");
		return new Rectangle(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]),
				Integer.parseInt(tokens[2]),
				Integer.parseInt(tokens[3]));
	}

	protected int getInt(String key, int defaultVal) {
		String value = (String) properties.get(key);
		if (value == null) {
			return defaultVal;
		}

		return Integer.parseInt(value);
	}

	protected double getDouble(String key, double defaultVal) {
		String value = (String) properties.get(key);
		if (value == null) {
			return defaultVal;
		}

		return Double.parseDouble(value);
	}


	protected String getLocalString(String key) {
		String localkey = key + "_" + locale.getLanguage();
		String value = (String) properties.get(localkey);
		if (value == null)
			return (String) properties.get(key);
		else
			return value;
	}

	protected Color getColor(String key) {
		String value = (String) properties.get(key);
		if (value == null) {
			return null;
		}

		if (value.charAt(0) == '#')
			value = value.substring(1);
		return new Color(Integer.parseInt(value, 16));
	}
}
