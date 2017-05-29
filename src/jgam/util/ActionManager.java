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

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of AbstractButtons and their commands.
 * The commands are sent to the registered CommandHandlers.
 * Commands can be enabled/disabled.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ActionManager {

	private List buttons = new ArrayList();

	public ActionManager() {
	}

	public void add(AbstractButton obj, String command) {
		obj.setActionCommand(command);
		buttons.add(obj);
	}

	public void remove(AbstractButton obj) {
		buttons.remove(obj);
	}

	/**
	 * add a handler for a command to the list of command handlers.
	 * The command may be null, in which case all commands are to be handled.
	 *
	 * @param command command to be handled or null
	 * @param ch      CommandHandler handling the command
	 */
	public void subscribeHandler(String command, ActionListener ch) {
		for (Iterator iter = buttons.iterator(); iter.hasNext(); ) {
			AbstractButton button = (AbstractButton) iter.next();
			if (command == null || command.equals(button.getActionCommand())) {
				button.addActionListener(ch);
			}
		}
	}

	/**
	 * remove the given CommandHandler. If it was inscribed for more than one
	 * command, it is removed for each one.
	 *
	 * @param ch CommandHandler to be removed
	 */
	public void removeHandler(ActionListener ch) {
		for (Iterator iter = buttons.iterator(); iter.hasNext(); ) {
			AbstractButton button = (AbstractButton) iter.next();
			button.removeActionListener(ch);
		}
	}

	public void enable(String command) {
		setState(command, true);
	}

	public void disable(String command) {
		setState(command, false);
	}

	/**
	 * enable or disable a command.
	 * <p>
	 * Calls setEnabled on all apropriate buttons
	 *
	 * @param command command to be changed for
	 * @param state   true=enabled; false=disabled
	 */
	public void setState(String command, boolean state) {
		for (Iterator iter = buttons.iterator(); iter.hasNext(); ) {
			AbstractButton item = (AbstractButton) iter.next();
			if (command.equals(item.getActionCommand())) {
				item.setEnabled(state);
			}
		}
	}

	/**
	 * print out all registered buttons and their commands
	 */
	public void traceButtons() {
		for (Iterator iter = buttons.iterator(); iter.hasNext(); ) {
			AbstractButton item = (AbstractButton) iter.next();
			System.out.println(item.getActionCommand() + ": " + item);
		}
	}

}
