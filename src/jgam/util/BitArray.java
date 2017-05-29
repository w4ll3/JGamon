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

/**
 * An array of bits.
 * <p>
 * It has fixed size.
 * Each bit can be set or cleared.
 * <p>
 * The array can be exported as a byte-array.
 * In order to do so, the order of bits must be set.
 * <p>
 * Mode MSB_FIRST exports the first bit in the highest (0x80) bit of the first
 * byte
 * Mode LSB_FIRST exports the first bit in the lowest (0x01) bit of the first
 * byte.
 * <p>
 * Ideas:
 * Exporting to integer/long/int array ...
 *
 * @author Mattias Ulbrich
 */
public class BitArray {

	// array[0] is the most sig bit in the first byte
	public static final int MSB_FIRST = 0;
	// array[0] is the least sig bit in the first byte
	public static final int LSB_FIRST = 1;

	private boolean bits[];

	public BitArray(int size) {
		bits = new boolean[size];
	}

	public void setBit(int index, boolean value) {
		bits[index] = value;
	}

	public void setBit(int index, int value) {
		setBit(index, value != 0);
	}

	public void setBit(int index) {
		setBit(index, true);
	}

	public void clearBit(int index) {
		setBit(index, false);
	}

	public byte[] toByteArray(int offset, int length, int mode) {
		if (offset + length > bits.length)
			throw new IllegalArgumentException("offset + length > length of array!");

		byte[] ret = new byte[(length + 7) / 8];

		for (int i = 0; i < length; i++) {
			if (bits[offset + i] == true) {
				int bytePos = i / 8;
				int bitPos = i % 8;
				if (mode == MSB_FIRST) {
					bitPos = 7 - bitPos;
				}
				ret[bytePos] |= (1 << bitPos);
			}
		}
		return ret;
	}

	public byte[] toByteArray(int mode) {
		return toByteArray(0, bits.length, mode);
	}


	public byte[] toByteArray() {
		return toByteArray(MSB_FIRST);
	}

	public long getLong(int offset, int length, int mode) {
		if (length >= 64 || length <= 0)
			throw new IllegalArgumentException("length must be < 64 and > 0");

		long ret = 0;
		for (int i = 0; i < length; i++) {
			if (bits[i + offset]) {
				if (mode == MSB_FIRST)
					ret |= 1 << (length - i);
				else
					ret |= 1 << i;
			}
		}
		return ret;
	}

	public int getInt(int offset, int length, int mode) {
		if (length >= 32)
			throw new IllegalArgumentException("length must be < 32");
		return (int) getLong(offset, length, mode);
	}

	public void putLong(int offset, int length, int mode, long value) {
		if (length >= 64 || length <= 0)
			throw new IllegalArgumentException("length must be < 64 and > 0");

		for (int i = 0; i < length; i++) {
			if (mode == MSB_FIRST)
				bits[offset + (length - i)] = (value & (1 << i)) != 0;
			else
				bits[offset + i] = (value & (1 << i)) != 0;
		}

	}

}
