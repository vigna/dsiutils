/*
 * DSI utilities
 *
 * Copyright (C) 2015-2021 Sebastiano Vigna
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


public class SplitMix64RandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final SplitMix64Random splitMixRandom = new SplitMix64Random(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final float d = splitMixRandom.nextFloat();
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
			final SplitMix64Random splitMixRandom = new SplitMix64Random(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = splitMixRandom.nextDouble();
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
			final SplitMix64Random splitMixRandom = new SplitMix64Random(seed);
			double avg = 0;
			for (int i = 100000000; i-- != 0;) {
				final int d = splitMixRandom.nextInt(101);
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
			final SplitMix64Random splitMixRandom = new SplitMix64Random(seed);
			final int[] count = new int[32];
			long change = 0;
			int prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final int d = splitMixRandom.nextInt();
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
			final SplitMix64Random splitMixRandom = new SplitMix64Random(seed);
			final int[] count = new int[64];
			long change = 0;
			long prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final long d = splitMixRandom.nextLong();
				change += Long.bitCount(d ^ prev);
				for (int b = 64; b-- != 0;)
					if ((d & (1L << b)) != 0) count[b]++;
				prev = d;
			}

			assertEquals(32 * 1000000L, change, 4000);
			for (int b = 64; b-- != 0;) assertEquals(500000, count[b], 1500);
		}
	}
}
