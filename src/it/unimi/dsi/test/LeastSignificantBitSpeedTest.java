/*
 * DSI utilities
 *
 * Copyright (C) 2012-2022 Sebastiano Vigna
 *
 * This program and the accompanying materials are made available under the
 * terms of the GNU Lesser General Public License v2.1 or later,
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html,
 * or the Apache Software License 2.0, which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later OR Apache-2.0
 */

package it.unimi.dsi.test;

import it.unimi.dsi.util.XoRoShiRo128PlusRandomGenerator;

public class LeastSignificantBitSpeedTest  {

	   /**
     * The set of least-significant bits for a given <code>byte</code>.  <code>-1</code>
     * is used if no bits are set (so as to not be confused with "index of zero"
     * meaning that the least significant bit is the 0th (1st) bit).
     *
     * @see #leastSignificantBit(long)
     */
    private static final int[] LEAST_SIGNIFICANT_BIT = {
       -1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0
    };

    /**
     * Computes the least-significant bit of the specified <code>long</code>
     * that is set to <code>1</code>. Zero-indexed.
     *
     * @param  value the <code>long</code> whose least-significant bit is desired.
     * @return the least-significant bit of the specified <code>long</code>.
     *         <code>-1</code> is returned if there are no bits set.
     */
    // REF:  http://stackoverflow.com/questions/757059/position-of-least-significant-bit-that-is-set
    // REF:  http://www-graphics.stanford.edu/~seander/bithacks.html
    public static int leastSignificantBit(final long value) {
        if(value == 0L) return -1/*by contract*/;
        if((value & 0xFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>>  0) & 0xFF)] +  0;
        if((value & 0xFFFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>>  8) & 0xFF)] +  8;
        if((value & 0xFFFFFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>> 16) & 0xFF)] + 16;
        if((value & 0xFFFFFFFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>> 24) & 0xFF)] + 24;
        if((value & 0xFFFFFFFFFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>> 32) & 0xFF)] + 32;
        if((value & 0xFFFFFFFFFFFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>> 40) & 0xFF)] + 40;
        if((value & 0xFFFFFFFFFFFFFFL) != 0) return LEAST_SIGNIFICANT_BIT[(int)((value >>> 48) & 0xFF)] + 48;
        return LEAST_SIGNIFICANT_BIT[(int)((value >>> 56) & 0xFFL)] + 56;
    }

    public static int javaLsb(final long value) {
    	return value == 0 ? -1 : Long.numberOfTrailingZeros(value);
    }

	private final static byte[] LSB_TABLE = {
		0, 1, 56, 2, 57, 49, 28, 3, 61, 58, 42, 50, 38, 29, 17, 4, 62, 47, 59, 36, 45, 43, 51, 22, 53, 39, 33, 30, 24, 18, 12, 5, 63, 55, 48, 27, 60, 41, 37, 16, 46, 35, 44, 21, 52, 32, 23, 11, 54, 26, 40, 15, 34, 20, 31, 10, 25, 14, 19, 9, 13, 8, 7, 6
	};

	private static int deBrujin(final long x) {
		return LSB_TABLE[(int)(((x & -x) * 0x03f79d71b4ca8b09L) >>> 58)];
	}


	public static void main(final String a[]) {
		final int n = Integer.parseInt(a[0]);

		final XoRoShiRo128PlusRandomGenerator r = new XoRoShiRo128PlusRandomGenerator(1);
		long start, elapsed;

		int x = 42;

		final long value[] = new long[n];
		for(int i = n; i-- != 0;) value[i] = r.nextLong();

		for(int k = 10; k-- !=0;) {
			System.out.print("Java: ");

			start = System.nanoTime();
			for(int i = n; i-- != 0;) x ^= javaLsb(value[i]);
			elapsed = System.nanoTime() - start;

			System.out.println("elapsed " + elapsed + ", " + (double)elapsed / n + " ns/call");

			System.out.print("Test-based: ");

			start = System.nanoTime();
			for(int i = n; i-- != 0;) x ^= leastSignificantBit(value[i]);
			elapsed = System.nanoTime() - start;

			System.out.println("elapsed " + elapsed + ", " + (double)elapsed / n + " ns/call");

			System.out.print("De Brujin: ");

			start = System.nanoTime();
			for(int i = n; i-- != 0;) x ^= deBrujin(value[i]);
			elapsed = System.nanoTime() - start;

			System.out.println("elapsed " + elapsed + ", " + (double)elapsed / n + " ns/call");

		}

		if (x == 0) System.out.println(0);

	}
}
