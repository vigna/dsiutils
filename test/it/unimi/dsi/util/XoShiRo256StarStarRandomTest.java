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


public class XoShiRo256StarStarRandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(seed);
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
			final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(seed);
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
			final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(seed);
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
			final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(seed);
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
			final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(seed);
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
		final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(0);
		for(int i = 0; i < 100; i++) assertTrue(xoroshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(0);
		assertEquals(0, xoroshiro.nextInt(1));
	}


	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoShiRo256StarStarRandom xoroshiro = new XoShiRo256StarStarRandom(seed);
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

			assertEquals(32 * 1000000L, change, 6000);
			for (int b = 64; b-- != 0;) assertEquals(500000, count[b], 2000);
		}
	}

	@Test
	public void testRight() {
		final long[] init = { 0x7743a154e17a5e9bL, 0x7823a1cd9453899bL, 0x976589eefbb1c7f5L, 0x702cf168260fa29eL };
		final long[] orbit = { 0x21b891895798210cL, 0x7c9502085d7d8cdcL, 0x7c99b227f9a6720cL, 0x2a556d6f0d363aedL, 0x3a3427604dc90f2bL, 0x42b0082c8b2501fL, 0x5a8fd42f2adc4d16L, 0xf01b4c488b475bf8L, 0x6b7929a720b00f6aL, 0x95d1535589400dffL };
		final XoShiRo256StarStarRandom xoshiro = new XoShiRo256StarStarRandom();
		xoshiro.setState(init);
		for(int i = 0; i < 10; i++) assertEquals(orbit[i], xoshiro.nextLong());
		xoshiro.setState(init);
		xoshiro.jump();
 		final long[] jump = { 0x1fff777385ad4a4dL, 0x14326b4b6c6ec08dL, 0xd479714e0df0ce8L, 0x148339f8c0657e9cL, 0x5d342cb167b7cc1cL, 0x5ac702c66564d917L, 0x1d877ecc686e534bL, 0x27689bb981181ed6L, 0x98d0943e405f080aL, 0xe741e681352b6837L };
		for(int i = 0; i < 10; i++) assertEquals(jump[i], xoshiro.nextLong());
		xoshiro.setState(init);
		xoshiro.longJump();
		final long[] longJump = { 0x55eeaf2922eabd11L, 0xc9501950d424e125L, 0xfc67b86d1918bddcL, 0x6a7166e0812d9bebL, 0x2bd123a8a9f5624bL, 0x31d240b7f50f732eL, 0x3acd0340cd16c5a6L, 0x72108d99c3582dcdL, 0xaef2cb430252f246L, 0xc15fd019c241d797L };
		for(int i = 0; i < 10; i++) assertEquals(longJump[i], xoshiro.nextLong());
	}
}
