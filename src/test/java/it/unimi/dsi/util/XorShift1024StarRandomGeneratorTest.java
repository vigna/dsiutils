/*
 * DSI utilities
 *
 * Copyright (C) 2013-2023 Sebastiano Vigna
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

@Deprecated
public class XorShift1024StarRandomGeneratorTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XorShift1024StarRandomGenerator xorShift = new XorShift1024StarRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final float d = xorShift.nextFloat();
				assertTrue(d < 1);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(500000, avg, 500);
		}
	}

	@Test
	public void testNextDouble() {
		for (final long seed : seeds) {
			final XorShift1024StarRandomGenerator xorShift = new XorShift1024StarRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = xorShift.nextDouble();
				assertTrue(d < 1);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(500000, avg, 500);
		}
	}

	@Test
	public void testNextInt() {
		for (final long seed : seeds) {
			final XorShift1024StarRandomGenerator xorShift = new XorShift1024StarRandomGenerator(seed);
			double avg = 0;
			for (int i = 100000000; i-- != 0;) {
				final int d = xorShift.nextInt(101);
				assertTrue(d <= 100);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(5000000000L, avg, 500000);
		}
	}

	@Test
	public void testNextInt2() {
		for (final long seed : seeds) {
			final XorShift1024StarRandomGenerator xorShift = new XorShift1024StarRandomGenerator(seed);
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

			assertEquals(32 * 1000000L, change, 60000);
			for (int b = 32; b-- != 0;) assertEquals(500000, count[b], 1600);
		}
	}

	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XorShift1024StarRandomGenerator xorShift = new XorShift1024StarRandomGenerator(seed);
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

			assertEquals(32 * 1000000L, change, 10000);
			for (int b = 64; b-- != 0;) assertEquals(500000, count[b], 1800);
		}
	}

	@Test
	public void testSameAsRandom() {
		final XorShift1024StarRandom xorShiftStarRandom = new XorShift1024StarRandom(0);
		final XorShift1024StarRandomGenerator xorShiftStar = new XorShift1024StarRandomGenerator(0);
		for(int i = 1000000; i-- != 0;) {
			assertEquals(xorShiftStar.nextLong(), xorShiftStarRandom.nextLong());
			assertEquals(0, xorShiftStar.nextDouble(), xorShiftStarRandom.nextDouble());
			assertEquals(xorShiftStar.nextInt(), xorShiftStarRandom.nextInt());
			assertEquals(xorShiftStar.nextInt(99), xorShiftStarRandom.nextInt(99));
			assertEquals(xorShiftStar.nextInt(128), xorShiftStarRandom.nextInt(128));
		}
	}

	@Test
	public void testJump() {
		final XorShift1024StarRandomGenerator xorShiftRandom0 = new XorShift1024StarRandomGenerator(0), xorShiftRandom1 = new XorShift1024StarRandomGenerator(0);
		xorShiftRandom0.nextLong();
		xorShiftRandom0.jump();
		xorShiftRandom1.jump();
		xorShiftRandom1.nextLong();
		assertEquals(xorShiftRandom0.nextLong(), xorShiftRandom1.nextLong());
	}
}
