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

package jgam.gui;

import jgam.JGammon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A splash screen for jgammon.
 * <p>
 * It disappears after a while or when clicked.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class JGamSplashScreen extends JWindow {

	Image image;

	public JGamSplashScreen() throws IOException {
		InputStream is = getClass().getResourceAsStream("/jgam/img/about.png");
		if (is == null)
			throw new FileNotFoundException("The splashscreen resource is not available");
		image = ImageIO.read(is);
		setSize(image.getWidth(null), image.getHeight(null));
		setLocationRelativeTo(null);
	}

	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, Color.black, this);
		g.drawString("Version " + JGammon.VERSION, 270, 20);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	public void allowCloseClick() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});
	}

	public void show(final int milliseconds) {
		setVisible(true);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(milliseconds);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				setVisible(false);
			}
		}.start();
	}
}
