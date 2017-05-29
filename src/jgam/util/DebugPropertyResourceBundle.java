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


package jgam.util;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * A resource bundle that indicates that entries are missing instead of calling
 * the superclass.
 * <p>
 * Assume "name" is not set in the underlying property for "jgam.msg.A".
 * <p>
 * The current locale will be appended.
 * <p>
 * get("name") will result in "[jgam.msg.A: name]"
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class DebugPropertyResourceBundle extends ResourceBundle {

	private String resource;
	Properties properties;

	public DebugPropertyResourceBundle(String resource) {
		try {
			InputStream is = makeStream(resource);
			properties = new Properties();
			if (is == null) {
				throw new NullPointerException();
			}
			properties.load(is);
		} catch (Exception ex) {
			properties = null;
		}

		this.resource = resource;
	}

	public DebugPropertyResourceBundle(String resource, String language) {
		this(resource + "_" + language);
	}

	private static InputStream makeStream(String resource) {
		resource = resource.replace('.', '/');
		return ClassLoader.getSystemResourceAsStream(resource + ".properties");
	}

	public Object handleGetObject(String key) {
		if (properties == null) {
			return "[" + resource + " missing!]";
		}

		Object superresult = properties.get(key);

		if (superresult == null) {
			return "[" + resource + ": " + key + "]";
		} else {
			return superresult;
		}
	}

	public Enumeration getKeys() {
		return properties.keys();
	}
}
