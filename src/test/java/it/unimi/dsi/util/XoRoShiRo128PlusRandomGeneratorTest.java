/*
 * DSI utilities
 *
 * Copyright (C) 2014-2023 Sebastiano Vigna
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


public class XoRoShiRo128PlusRandomGeneratorTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final float d = xoroshiro.nextFloat();
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
			final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = xoroshiro.nextDouble();
				assertTrue(d < 1);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(500000, avg, 1000);
		}
	}

	@Test
	public void testNextDoubleFast() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = xoroshiro.nextDoubleFast();
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
			final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(seed);
			double avg = 0;
			for (int i = 100000000; i-- != 0;) {
				final int d = xoroshiro.nextInt(101);
				assertTrue(d <= 100);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(5000000000L, avg, 1000000);
		}
	}

	@Test
	public void testNextInt2() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(seed);
			final int[] count = new int[32];
			long change = 0;
			int prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final int d = xoroshiro.nextInt();
				change += Long.bitCount(d ^ prev);
				for (int b = 32; b-- != 0;)
					if ((d & (1 << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 38000);
			for (int b = 32; b-- != 0;) assertEquals(500000, count[b], 2000);
		}
	}

	@Test
	public void testNextInt3() {
		final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(0);
		for(int i = 0; i < 100; i++) assertTrue(xoroshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(0);
		assertEquals(0, xoroshiro.nextInt(1));
	}

	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusRandomGenerator xoroshiro = new XoRoShiRo128PlusRandomGenerator(seed);
			final int[] count = new int[64];
			long change = 0;
			long prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final long d = xoroshiro.nextLong();
				change += Long.bitCount(d ^ prev);
				for (int b = 64; b-- != 0;)
					if ((d & (1L << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 7000);
			for (int b = 64; b-- != 0;) assertEquals(500000, count[b], 2000);
		}
	}

	@Test
	public void testRejection() {
		final XoRoShiRo128PlusRandomGenerator xoroshiroStar = new XoRoShiRo128PlusRandomGenerator(0);
		xoroshiroStar.nextLong((1L << 62) + 1);
		xoroshiroStar.nextLong((1L << 62) + 1);
		xoroshiroStar.nextLong((1L << 62) + 1);
		xoroshiroStar.nextLong((1L << 62) + 1);
	}

	@Test
	public void testSameAsRandom() {
		final XoRoShiRo128PlusRandom xoroshiroStarRandom = new XoRoShiRo128PlusRandom(0);
		final XoRoShiRo128PlusRandomGenerator xoroshiroStar = new XoRoShiRo128PlusRandomGenerator(0);
		for(int i = 1000000; i-- != 0;) {
			assertEquals(xoroshiroStar.nextLong(), xoroshiroStarRandom.nextLong());
			assertEquals(0, xoroshiroStar.nextDouble(), xoroshiroStarRandom.nextDouble());
			assertEquals(xoroshiroStar.nextInt(), xoroshiroStarRandom.nextInt());
			assertEquals(xoroshiroStar.nextInt(99), xoroshiroStarRandom.nextInt(99));
			assertEquals(xoroshiroStar.nextInt(128), xoroshiroStarRandom.nextInt(128));
		}
	}
}
