/*
 * DSI utilities
 *
 * Copyright (C) 2014-2021 Sebastiano Vigna
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

package it.unimi.dsi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


@SuppressWarnings("deprecation")
public class XorShift128PlusRandomGeneratorTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XorShift128PlusRandomGenerator xorShift = new XorShift128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final float d = xorShift.nextFloat();
				assertTrue(Float.toString(d), d < 1);
				assertTrue(Float.toString(d), d >= 0);
				avg += d;
			}

			assertEquals(500000, avg, 1000);
		}
	}

	@Test
	public void testNextDouble() {
		for (final long seed : seeds) {
			final XorShift128PlusRandomGenerator xorShift = new XorShift128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = xorShift.nextDouble();
				assertTrue(d < 1);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(500000, avg, 1000);
		}
	}

	@Test
	public void testNextInt() {
		for (final long seed : seeds) {
			final XorShift128PlusRandomGenerator xorShift = new XorShift128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 100000000; i-- != 0;) {
				final int d = xorShift.nextInt(101);
				assertTrue(d <= 100);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(5000000000L, avg, 300000);
		}
	}

	@Test
	public void testNextInt2() {
		for (final long seed : seeds) {
			final XorShift128PlusRandomGenerator xorShift = new XorShift128PlusRandomGenerator(seed);
			final int[] count = new int[32];
			long change = 0;
			int prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final int d = xorShift.nextInt();
				change += Long.bitCount(d ^ prev);
				for (int b = 32; b-- != 0;)
					if ((d & (1 << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 30000);
			for (int b = 32; b-- != 0;) assertEquals(500000, count[b], 1500);
		}
	}

	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XorShift128PlusRandomGenerator xorShift = new XorShift128PlusRandomGenerator(seed);
			final int[] count = new int[64];
			long change = 0;
			long prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final long d = xorShift.nextLong();
				change += Long.bitCount(d ^ prev);
				for (int b = 64; b-- != 0;)
					if ((d & (1L << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 6000);
			for (int b = 64; b-- != 0;) assertEquals(500000, count[b], 2000);
		}
	}

	@Test
	public void testSameAsRandom() {
		final XorShift128PlusRandom xorShiftStarRandom = new XorShift128PlusRandom(0);
		final XorShift128PlusRandomGenerator xorShiftStar = new XorShift128PlusRandomGenerator(0);
		for(int i = 1000000; i-- != 0;) {
			assertEquals(xorShiftStar.nextLong(), xorShiftStarRandom.nextLong());
			assertEquals(0, xorShiftStar.nextDouble(), xorShiftStarRandom.nextDouble());
			assertEquals(xorShiftStar.nextInt(), xorShiftStarRandom.nextInt());
			assertEquals(xorShiftStar.nextInt(99), xorShiftStarRandom.nextInt(99));
			assertEquals(xorShiftStar.nextInt(128), xorShiftStarRandom.nextInt(128));
		}
	}
}
