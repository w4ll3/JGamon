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

import java.io.IOException;
import java.io.*;

import jgam.util.*;
import jgam.game.*;
import java.util.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 *
 * @todo changed as the new game came in ... not checked
 */
public class AskGnuBG {
    private String command;
    private Process process;
    private List answer = new ArrayList();
    private PrintStream printer;
    private BufferedReader reader;

    public AskGnuBG(String command) throws IOException {
        this.command = command;
        startProcess();
    }

    private void startProcess() throws IOException {
        process = Runtime.getRuntime().exec(command);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        printer = new PrintStream(process.getOutputStream());

        sendCommand("set player 0 human");
        sendCommand("set player 1 human");
    }


    private void sendCommand(String command) throws IOException {
        printer.println(command);
        printer.flush();

        answer.clear();
        String line = reader.readLine();
        while (line.charAt(0) != '(') {
            answer.add(line);
            System.out.println(line);
        }
    }

    public double askGnu(BoardSetup setup) throws IOException {
        sendCommand("first move");
        sendCommand("new game");
        sendCommand("set turn 0");
        sendCommand("set board " + positionID(setup));
        sendCommand("set turn " + (setup.getPlayerAtMove() - 1));
        int dice[] = setup.getDice();
        if (dice != null) {
            sendCommand("set dice " + dice[0] + " " + dice[1]);
        }
        sendCommand("hint");
        String[] vals = ((String) answer.get(1)).split(" +");
        double d1 = Double.parseDouble(vals[0]);
        double d2 = Double.parseDouble(vals[1]);
        double d3 = Double.parseDouble(vals[2]);
        return d1 + d2 + d3;
    }

    /**
     * see: http://www.gnu.org/software/gnubg/manual/html_node/A-technical-description-of-the-position-ID.html
     * @param game Game
     * @return String
     */
    private static String positionID(BoardSetup setup) {

        BitArray bits = new BitArray(80);
        int counter = 0;
        for (int i = 1; i <= 25; i++) {
            for (int j = 0; j < setup.getPoint(1, i); j++) {
                bits.setBit(counter++);
            }
            bits.clearBit(counter++);
        }

        for (int i = 1; i <= 25; i++) {
            for (int j = 0; j < setup.getPoint(0, i); j++) {
                bits.setBit(counter++);
            }
            bits.clearBit(counter++);
        }

        byte byteRes[] = bits.toByteArray(BitArray.LSB_FIRST);
        String stringRes = Base64.encode(byteRes);
        return stringRes.substring(0, 14); // strip trailing ==
    }
}
