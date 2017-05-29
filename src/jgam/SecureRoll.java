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


package jgam;

import jgam.util.Base64;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ProtocolException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Protocol for secure dice!
 * <p>
 * Bargain the value for the dice so that none of the players can cheat.
 * <p>
 * SHA is a message digest-algorithm.
 * <p>
 * R is a randomly generated 64-bit value with
 * the first byte < 36
 * SHA_R = SHA("SECROLL R")
 * <p>
 * 1. The server player P1 chooses R randomly and sends SHA_R to the other player P2.
 * 2. P2 chooses 0 <= q < 36 and sends it to P1.
 * 3. P1 sends R to P2
 * 4. P2 verifies SHA_R = SHA("SECROLL R")
 * 5. both calculate X:=(R[0]+q) mod 36.
 * <p>
 * This first byte of R ought to be 0 <= x < 36.
 * <p>
 * dice1 = X % 6
 * dice2 = X / 6
 * <p>
 * For legacy reasons there is also an unsecure communication implemented.
 *
 * @author Mattias Ulbrich
 */
public class SecureRoll {

	private final static int PAD_LENGTH = 8;
	private SecureRandom random;
	private int dice[] = null;
	private MessageDigest messageDigest;

	public SecureRoll() throws NoSuchAlgorithmException {
		random = new SecureRandom();
		messageDigest = MessageDigest.getInstance("SHA");
	}

	/**
	 * talk to the other party using w and r and negotiate a pair of dices.
	 * <p>
	 * Server:                             Client:
	 * -->     SECDICE_VEILED <SHA_R>     <--
	 * <--     SECDICE <0-35>             -->
	 * -->     SECDICE <8bytes>           <-- (+check)
	 * <p>
	 * The server starts sending:
	 *
	 * @param r          BufferedReader
	 * @param w          Writer
	 * @param serverMode boolean
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public void negotiate(BufferedReader r, Writer w, boolean serverMode) throws IOException,
			jgam.net.JGamProtocolException {

		byte hisValue;
		byte myValue = (byte) random.nextInt(36);

		if (serverMode) {
			byte[] myPad = new byte[PAD_LENGTH];
			random.nextBytes(myPad);
			myPad[0] = myValue;

			// STEP 1 SERVER
			//
			messageDigest.reset();
			messageDigest.update("SECURE DICE".getBytes());
			byte[] myDigest = messageDigest.digest(myPad);
			String stringMyDigest = Base64.encode(myDigest);

			String message = "SECDICE_VEILED " + stringMyDigest;
			w.write(message + "\n");
			w.flush();

			// STEP 2 SERVER
			//
			String line = r.readLine();
			if (!line.startsWith("SECDICE ")) {
				throw new ProtocolException("SECDICE expected, got: " + line);
			}
			hisValue = Base64.decode(line.substring(8))[0];

			// STEP 3 SERVER
			//
			message = "SECDICE " + Base64.encode(myPad);
			w.write(message + "\n");
			w.flush();

		} else { // CLIENT MODE

			// STEP 1 CLIENT
			//
			String line = r.readLine();
			if (!line.startsWith("SECDICE_VEILED ")) {
				throw new ProtocolException("SECDICE_VEILED expected, got: " + line);
			}
			byte[] hisDigest = Base64.decode(line.substring(15));

			// STEP 2 CLIENT
			//
			String message = "SECDICE " + Base64.encode(myValue);
			w.write(message + "\n");
			w.flush();

			// STEP 3 CLIENT
			//
			line = r.readLine();
			if (!line.startsWith("SECDICE ")) {
				throw new ProtocolException("SECDICE expected, got: " + line);
			}
			byte[] hisPad = Base64.decode(line.substring(8));

			// Check that digest is ok!
			messageDigest.reset();
			messageDigest.update("SECURE DICE".getBytes());
			if (!MessageDigest.isEqual(hisDigest, messageDigest.digest(hisPad))) {
				throw new ProtocolException("The fingerprint and the pad do not match!");
			}

			hisValue = hisPad[0];
		}

		// calc the value!
		int D = (hisValue + myValue) % 36;

		dice = new int[2];
		dice[0] = (D / 6) + 1;
		dice[1] = (D % 6) + 1;
	}

	/**
	 * use an unsecure protocol to negotiate the dice.
	 * The server sends DICE <val1> <val2> which is received by the client.
	 *
	 * @param r          BufferedReader to read
	 * @param w          Writer to write
	 * @param serverMode true if send, false if receive
	 */
	public void negotiateUnsecure(BufferedReader r, Writer w, boolean serverMode) throws IOException,
			jgam.net.JGamProtocolException {

		if (!serverMode) {

			String line = r.readLine();
			if (!line.startsWith("DICE ")) {
				throw new ProtocolException("DICE expected, got: " + line);
			}
			dice = new int[2];
			dice[0] = Integer.parseInt(line.substring(5, 6));
			dice[1] = Integer.parseInt(line.substring(7, 8));

		} else {

			dice = new int[2];
			if (Boolean.getBoolean("jgam.manualdice")) {
				dice[0] = Integer.parseInt(JOptionPane.showInputDialog(null,
						"Enter first dice value:",
						"2"));
				dice[1] = Integer.parseInt(JOptionPane.showInputDialog(null,
						"Enter first dice value:",
						"2"));
			} else {
				dice[0] = (random.nextInt(6) + 1);
				dice[1] = (random.nextInt(6) + 1);
			}

			w.write("DICE " + dice[0] + " " + dice[1] + "\n");
			w.flush();
		}
	}

	public int getOneDice() {
		return dice[0];
	}

	public int[] getDice() {
		return dice;
	}

	public int[] getDice(int count) {
		if (count == 1) {
			return new int[]{dice[0]};
		} else if (count == 2) {
			return dice;
		} else {
			throw new IndexOutOfBoundsException("dice must be 1 or 2");
		}
	}
}
