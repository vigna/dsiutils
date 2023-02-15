/*
 * DSI utilities
 *
 * Copyright (C) 2011-2023 Sebastiano Vigna
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

import static it.unimi.dsi.test.XorShift.BITS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Too slow, never changes")
@Deprecated
public class XorShiftTest {

	@Test
	public void testMultiply64Bit() {
		final it.unimi.dsi.util.XorShift128PlusRandomGenerator random = new it.unimi.dsi.util.XorShift128PlusRandomGenerator(0);
		final long[] x = new long[64], y = new long[64], t = new long[64];

		for(int i = 64; i-- != 0;) {
			x[i] = random.nextLong();
			y[i] = random.nextLong();
		}

		for(int i = 64; i-- != 0;)
			for(int j = 64; j-- != 0;)
				for(int k = 64; k-- != 0;) t[i] ^= ((x[i] & 1L << k) != 0 ? 1L : 0) * ((y[k] & 1L << j) != 0 ? 1L : 0) << j;

		assertArrayEquals(t, XorShift.multiply(x, y));

	}

	@Test
	public void testWord() {
		final it.unimi.dsi.util.XorShift128PlusRandomGenerator random = new it.unimi.dsi.util.XorShift128PlusRandomGenerator(0);
		final long[][] m = XorShift.newMatrix(BITS);
		for(final long[] a : m) for(int i = a.length; i-- != 0;) a[i] = random.nextLong();

		for(int r = 0; r < BITS - 128; r++)
			for(int c = 0; c < BITS / 64; c++)
				if (r + 64 < BITS - 64 && c + 1 < 64) assertEquals(XorShift.word(m, r, c, BITS), XorShift.word(m, r + 64, c + 1, BITS));
	}

	//@Ignore
	@Test
	public void testMultiply() {
		final it.unimi.dsi.util.XorShift128PlusRandomGenerator random = new it.unimi.dsi.util.XorShift128PlusRandomGenerator(0);
		final long[][] m = XorShift.newMatrix(BITS);
		for(int i = 64; i-- != 0;) m[i][BITS / 64 - 1] = random.nextLong();
		for(int i = 64; i-- != 0;) m[BITS - i - 1][BITS / 64 - 1] = random.nextLong();
		for(int i = 64; i-- != 0;) m[BITS - 64 + i][BITS / 64 - 2] = 1L << i;
		for(int i = 64; i-- != 0;) m[64 + i][0] = 1L << i;

		long[][] n = m;

		for(int i = 0; i < 4; i++) {
			// final long start = - System.nanoTime();
			final long[][] p = XorShift.multiply(m, n);
			// System.err.println("Multiplication took " + (System.nanoTime() + start) / 1E9 + "s");

			for(int r = 0; r < BITS; r++) {
				final long t[] = new long[BITS / 64];
				for(int c = 0; c < BITS; c++)
					if ((XorShift.word(m, r, c / 64, BITS) & 1L << c) != 0)
						for(int w = BITS / 64; w-- != 0;) t[w] ^= XorShift.word(n, c, w, BITS);

				for(int w = BITS / 64; w-- != 0;) assertEquals(t[w], XorShift.word(p, r, w, BITS));
			}
			n = p;
		}
	}

	@Test
	public void testIdentity() {
		final long[][] identity = XorShift.identity();
		assertTrue(XorShift.isIdentity(identity));
		for(int r = identity.length; r-- != 0;)
			for(int c = identity[r].length; c-- != 0;)
				for(int b = 64; b-- != 0;) {
					identity[r][c] ^= 1L << b;
					assertFalse(XorShift.isIdentity(identity));
					identity[r][c] ^= 1L << b;
				}
	}

	@Test
	public void testMPow() {
		final it.unimi.dsi.util.XorShift128PlusRandomGenerator random = new it.unimi.dsi.util.XorShift128PlusRandomGenerator(0);
		final long[][] m = XorShift.newMatrix(BITS);
		for(int i = 64; i-- != 0;) m[i][BITS / 64 - 1] = random.nextLong();
		for(int i = 64; i-- != 0;) m[BITS - i - 1][BITS / 64 - 1] = random.nextLong();
		for(int i = 64; i-- != 0;) m[BITS - 64 + i][BITS / 64 - 2] = 1L << i;
		for(int i = 64; i-- != 0;) m[64 + i][0] = 1L << i;

		final long[][][] q = XorShift.quad(m);

		long[][] p = m;
		for(int i = 0; i < 12; i++) p = XorShift.multiply(p,  m);

		assertTrue(Arrays.deepEquals(p, XorShift.mPow(q, BigInteger.valueOf(13))));
	}

	/** Multiplies the given row vector by the given matrix.
	 *
	 * @param v a row vector.
	 * @param M a matrix.
	 * @param bits the number of bits in a row.
	 * @return the row vector {@code vM}.
	 */
	public static long[] multiply(final long[] v, final long[][] M, final int bits) {
		final long[] result = new long[v.length];
		for(int i = v.length; i-- != 0;)
			for(int b = 64; b-- != 0;)
				if ((v[i] & 1L << b) != 0)
					for(int w = v.length; w-- != 0;) result[w] ^= XorShift.word(M, i * 64 + b, w, bits);

		return result;
	}

	@Test
	public void testMarsagliaVsLoopVsMatrixMultiplication() {
		long x = 1, y = 2, z = 3, v = 4, t;
		final long[] s = { 4, 1, 2, 3 };
		long[] ss = { 1, 2, 3, 4 };
		final int a = 5, b = 18, c = 12; // Random
		final long[][] m = XorShift.makeABCMatrix(a, b, c, 256);
		int p = 0;
		for(int i = 0; i < 1000; i++) {
			// Marsaglia
			t = (x ^ (x << a));
			//System.err.println("t: " + t);
			x = y;
			y = z;
			z = v;
			v = (v ^ (v >>> c)) ^ (t ^ (t >>> b));

			// Our loop
			final long s0 = s[p];
			long s1 = s[p = (p + 1) & 3];
			s1 ^= s1 << a;
			s[p] = s1 ^ s0 ^ (s1 >>> b) ^ (s0 >>> c);

			assertEquals(Integer.toString(i), v, s[p]);

			// Matrix multiplication
			ss = multiply(ss, m, 256);

			assertEquals(Integer.toString(i), x, ss[0]);
			assertEquals(Integer.toString(i), y, ss[1]);
			assertEquals(Integer.toString(i), z, ss[2]);
			assertEquals(Integer.toString(i), v, ss[3]);

			assertEquals(Integer.toString(i), x, s[p + 1 & 3]);
			assertEquals(Integer.toString(i), y, s[p + 2 & 3]);
			assertEquals(Integer.toString(i), z, s[p + 3 & 3]);
			assertEquals(Integer.toString(i), v, s[p]);
		}
	}

	@Test
	public void test128SwitchVsMatrixMultiplication() {
		final long[] s = { 1, 2 };
		long[] ss = { 1, 2 };
		final int a = 5, b = 18, c = 12; // Random
		final long[][] m = XorShift.makeABCMatrix(a, b, c, 128);
		for(int i = 0; i < 1000; i++) {
			// Our loop
			long s1 = s[0];
			final long s0 = s[1];
			s[0] = s0;
			s1 ^= s1 << a;
			s[1] = (s1 ^ s0 ^ (s1 >>> b) ^ (s0 >>> c));

			// Matrix multiplication
			ss = multiply(ss, m, 128);

			assertEquals(Integer.toString(i), ss[0], s[0]);
			assertEquals(Integer.toString(i), ss[1], s[1]);
		}
	}
}
