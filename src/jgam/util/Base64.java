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

import java.io.ByteArrayOutputStream;

/**
 * This class provides static methods to encode byte-arrays in base64 format
 * and to decode base64-coded strings.
 * <p>
 * Base64 is explained in rfc 1521 e.g.
 * http://www.freesoft.org/CIE/RFC/1521/7.htm
 * <p>
 * <p>
 * The created strings do not contain line break characters.
 * Any charactor that does not belong to the alphabet is ignored.
 * Padding characters must be set correctly.
 *
 * @author Mattias Ulbrich
 */

public class Base64 {
	private Base64() {
	}

	// the characterset
	private static char[] alphabet =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".
					toCharArray();

	/**
	 * encode data in base64.
	 * <p>
	 * The data block is encoded in the Base64 format. Padding ("=") is provided
	 * if needed. Newlines are not inserted.
	 * <p>
	 * Use ubyte() to ensure that 0 are shifted from the left.
	 *
	 * @param data data to be encoded
	 * @return String representation of the encoded data.
	 */
	public static String encode(byte[] data) {

		StringBuffer ret = new StringBuffer();

		int extrabytes = data.length % 3;
		int extraindex = data.length - extrabytes;

		for (int i = 0; i < extraindex; i += 3) {
			int hextet1 = (data[0 + i] >>> 2) & 0x3f;
			int hextet2 = ((data[0 + i] << 4) + (ubyte(data[1 + i]) >>> 4)) & 0x3f;
			int hextet3 = ((data[1 + i] << 2) + (ubyte(data[2 + i]) >>> 6)) & 0x3f;
			int hextet4 = data[2 + i] & 0x3f;
			ret.append(alphabet[hextet1])
					.append(alphabet[hextet2])
					.append(alphabet[hextet3])
					.append(alphabet[hextet4]);
		}

		if (extrabytes != 0) {
			ret.append(alphabet[(ubyte(data[extraindex]) >>> 2) & 0x3f]);
			if (extrabytes == 1) {
				ret.append(alphabet[(data[extraindex] << 4) & 0x3f]).append("==");
			} else { // extrabytes == 2
				ret.append(alphabet[((data[extraindex] << 4) + (ubyte(data[extraindex + 1]) >>> 4)) & 0x3f])
						.append(alphabet[(data[extraindex + 1] << 2) & 0x3f])
						.append("=");
			}
		}

		return ret.toString();

	}

	/**
	 * decode a Base64 encoded String to an array of bytes.
	 * <p>
	 * The string is also decoded if the trailing "=" are missing,
	 * arbitrary characters (also =) are ignored.
	 *
	 * @param string a Base64-encoded string
	 * @return the decoded byte array.
	 */
	public static byte[] decode(String string) {

		int buf[] = new int[string.length()];
		int buflength = 0;

		for (int i = 0; i < string.length(); i++) {
			int index = indexOf(string.charAt(i));
			if (index != -1) {
				buf[buflength++] = index;
			}
		}

		int extrachars = buflength % 4;
		int extraindex = buflength - extrachars;
		if (extrachars == 1) {
			throw new IllegalArgumentException("This string has an irregular length of 1 modulo 4)");
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		for (int i = 0; i < buflength - extrachars; i += 4) {
			os.write((buf[i + 0] << 2) + (buf[i + 1] >>> 4));
			os.write(((buf[i + 1] << 4) + (buf[i + 2] >>> 2)) & 0xff);
			os.write(((buf[i + 2] << 6) + buf[i + 3]) & 0xff);
		}

		if (extrachars > 0) {
			os.write((buf[extraindex + 0] << 2) + (buf[extraindex + 1] >>> 4));
			if (extrachars > 2) {
				os.write(((buf[extraindex + 1] << 4) + (buf[extraindex + 2] >>> 2)) & 0xff);
			}
		}

		return os.toByteArray();
	}

	/**
	 * encode data in base64.
	 * <p>
	 * The data block is encoded in the Base64 format. Padding ("=") is provided
	 * if needed. Newlines are inserted every 76 characters.
	 *
	 * @param data data to be encoded
	 * @return String representation of the encoded data.
	 */
	public static String encodeWithLinebreaks(byte[] data) {

		String res = encode(data);
		StringBuffer ret = new StringBuffer();
		int restlength = res.length() % 76;

		for (int i = 0; i < res.length() - restlength; i += 76) {
			ret.append(res.substring(i * 76, (i + 1) * 76));
			ret.append("\n");
		}

		ret.append(res.substring(res.length() - restlength));

		return ret.toString();

	}

	/**
	 * find a character in the alphabet.
	 *
	 * @param c character to find
	 * @return index of the character in the alphabet or -1 if not found.
	 */
	private static int indexOf(char c) {
		for (int i = 0; i < alphabet.length; i++) {
			if (alphabet[i] == c) {
				return i;
			}
		}
		return -1;
	}


	/**
	 * interpret a byte unsigned.
	 *
	 * @param value byte the byte to see unsigned
	 * @return the value of the byte embedded in an int. only the lowest 8
	 * bit are set.
	 */
	private static int ubyte(byte value) {
		return ((int) value) & 0xff;
	}

	public static void main(String[] args) {
		if (args[0].startsWith("decode")) {
			System.out.println("Decode: " + args[1]);
			System.out.println("   Res: " + new String(Base64.decode(args[1])));
		} else {
			System.out.println("Encode: " + args[1]);
			System.out.println("   Res: " + Base64.encode(args[1].getBytes()));
			System.out.println("Res+lb: " + Base64.encodeWithLinebreaks(args[1].getBytes()));
		}

	}

	/**
	 * encode a single byte in base64. it will look like XY==, X and Y
	 * base64 characters
	 *
	 * @return base64 coding of the byte
	 */
	public static String encode(byte singleByte) {
		return encode(new byte[]{singleByte});
	}
}
