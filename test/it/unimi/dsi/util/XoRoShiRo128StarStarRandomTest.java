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


public class XoRoShiRo128StarStarRandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(seed);
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
			final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(seed);
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
			final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(seed);
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
			final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(seed);
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
			final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(seed);
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
		final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(0);
		for(int i = 0; i < 100; i++) assertTrue(xoroshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(0);
		assertEquals(0, xoroshiro.nextInt(1));
	}


	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom(seed);
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
		final long[] init = { 0x7743a154e17a5e9bL, 0x7823a1cd9453899bL };
		final long[] orbit = { 0x71adf5d14150a0faL, 0x9d66700ad01029fbL, 0xfacd55e7af04c477L, 0x2810e7bfc231a50aL, 0x46d683a7e8d9dab5L, 0x883abe697b5826d5L, 0xf360b389346e6e0bL, 0x3bfc9974bbaeecbL, 0xea007f11cc230d26L, 0x8e739693b9778fe3L };
		final XoRoShiRo128StarStarRandom xoroshiro = new XoRoShiRo128StarStarRandom();
		xoroshiro.setState(init);
		for(int i = 0; i < 10; i++) assertEquals(orbit[i], xoroshiro.nextLong());
		xoroshiro.setState(init);
		xoroshiro.jump();
 		final long[] jump = { 0x6619852cf600a0e5L, 0x2013c65404d30fbaL, 0xdf5f56c16fada5a4L, 0xadcae0287fc1d5ccL, 0xe3adbcca5c815fb9L, 0x71aed32afe1f3dd8L, 0x4427075f5e353af0L, 0x35b0d641e7f9a9b7L, 0x6e56032f90976203L, 0x2cc4b29740bf925L };
		for(int i = 0; i < 10; i++) assertEquals(jump[i], xoroshiro.nextLong());
		xoroshiro.setState(init);
		xoroshiro.longJump();
		final long[] longJump = { 0xca9c176d99c86e65L, 0x13cdf5f06c1b41dfL, 0xb96e7805bedf45c1L, 0x325a4871f0987203L, 0xa5e67ee6495d0dd6L, 0xf75027892081f329L, 0xde79859f39eebbf2L, 0xc450ce2015e9a51dL, 0xd2c9688efe74b330L, 0x11198849e1421a1aL };
		for(int i = 0; i < 10; i++) assertEquals(longJump[i], xoroshiro.nextLong());
	}
}
