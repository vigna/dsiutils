/*
 * DSI utilities
 *
 * Copyright (C) 2015-2022 Sebastiano Vigna
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


public class SplitMix64RandomGeneratorTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final SplitMix64RandomGenerator splitMix = new SplitMix64RandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final float d = splitMix.nextFloat();
				assertTrue(d < 1);
				assertTrue(d >= 0);
				avg += d;
			}

			assertEquals(500000, avg, 1000);
		}
	}

	@Test
	public void testNextDouble() {
		for (final long seed : seeds) {
			final SplitMix64RandomGenerator splitMix = new SplitMix64RandomGenerator(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = splitMix.nextDouble();
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
			final SplitMix64RandomGenerator splitMix = new SplitMix64RandomGenerator(seed);
			double avg = 0;
			for (int i = 100000000; i-- != 0;) {
				final int d = splitMix.nextInt(101);
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
			final SplitMix64RandomGenerator splitMix = new SplitMix64RandomGenerator(seed);
			final int[] count = new int[32];
			long change = 0;
			int prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final int d = splitMix.nextInt();
				change += Long.bitCount(d ^ prev);
				for (int b = 32; b-- != 0;)
					if ((d & (1 << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 23000);
			for (int b = 32; b-- != 0;) assertEquals(500000, count[b], 1500);
		}
	}

	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final SplitMix64RandomGenerator splitMix = new SplitMix64RandomGenerator(seed);
			final int[] count = new int[64];
			long change = 0;
			long prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final long d = splitMix.nextLong();
				change += Long.bitCount(d ^ prev);
				for (int b = 64; b-- != 0;)
					if ((d & (1L << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 4000);
			for (int b = 64; b-- != 0;) assertEquals(500000, count[b], 1500);
		}
	}

	@Test
	public void testSameAsRandom() {
		final SplitMix64Random splitMixStarRandom = new SplitMix64Random(0);
		final SplitMix64RandomGenerator splitMixStar = new SplitMix64RandomGenerator(0);
		for(int i = 1000000; i-- != 0;) {
			assertEquals(splitMixStar.nextLong(), splitMixStarRandom.nextLong());
			assertEquals(0, splitMixStar.nextDouble(), splitMixStarRandom.nextDouble());
			assertEquals(splitMixStar.nextInt(), splitMixStarRandom.nextInt());
			assertEquals(splitMixStar.nextInt(99), splitMixStarRandom.nextInt(99));
			assertEquals(splitMixStar.nextInt(128), splitMixStarRandom.nextInt(128));
		}
	}
}
