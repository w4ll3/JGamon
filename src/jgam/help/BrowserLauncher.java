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
package jgam.help;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Find a way to launch a browser from java. This won't work in general but
 * might in Windows and on other systems.
 *
 * @author Mattias Ulbrich
 */
public class BrowserLauncher {

	private static String command;

	static {
		command = System.getProperty("jgam.browsercmd"); //$NON-NLS-1$
		if (command == null) {
			String os = System.getProperty("os.name"); //$NON-NLS-1$
			if (os.startsWith("Windows")) { //$NON-NLS-1$
				command = "rundll32 url.dll,FileProtocolHandler"; //$NON-NLS-1$
			} else if (os.startsWith("Mac")) { //$NON-NLS-1$
				command = "open"; //$NON-NLS-1$
			} else if (os.startsWith("SunOS")) { //$NON-NLS-1$
				command = "/usr/dt/bin/sdtwebclient"; //$NON-NLS-1$
			} else {
				command = "mozilla"; //$NON-NLS-1$
			}
		}

	}

	public static void launch(URL url) {
		try {
			Process p = Runtime.getRuntime().exec(command + " " + url); //$NON-NLS-1$
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, HelpFrame.msg.getString("BrowserLauncher.errorHelpFrame.msg")); //$NON-NLS-1$
		}
	}

	public static void showConfigWindow() {
		String res = JOptionPane.showInputDialog(null, HelpFrame.msg.getString("BrowserLauncher.entercommand"), command); //$NON-NLS-1$
		if (res != null)
			command = res;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		showConfigWindow();
		launch(new URL("http://www.hurra.de")); //$NON-NLS-1$

	}

}
