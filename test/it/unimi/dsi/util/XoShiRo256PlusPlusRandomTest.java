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


public class XoShiRo256PlusPlusRandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final float d = xoshiro.nextFloat();
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
			final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = xoshiro.nextDouble();
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
			final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(seed);
			double avg = 0;
			for (int i = 1000000; i-- != 0;) {
				final double d = xoshiro.nextDoubleFast();
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
			final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(seed);
			double avg = 0;
			for (int i = 100000000; i-- != 0;) {
				final int d = xoshiro.nextInt(101);
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
			final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(seed);
			final int[] count = new int[32];
			long change = 0;
			int prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final int d = xoshiro.nextInt();
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
		final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(0);
		for(int i = 0; i < 100; i++) assertTrue(xoshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(0);
		assertEquals(0, xoshiro.nextInt(1));
	}


	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom(seed);
			final int[] count = new int[64];
			long change = 0;
			long prev = 0;
			for (int i = 1000000; i-- != 0;) {
				final long d = xoshiro.nextLong();
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
		final long[] orbit = { 0xd5c766557e6e16e4L, 0xf8eb8f8747a8cc67L, 0xfc18365710a653eeL, 0xc698a193593f232L, 0xa44ddeaac93b1be7L, 0x678dcd0e0516c741L, 0x351d94668d7c35eL, 0x73160e24fc8768daL, 0x562ca11220c31698L, 0x3dba336235c48913L };
		final XoShiRo256PlusPlusRandom xoshiro = new XoShiRo256PlusPlusRandom();
		xoshiro.setState(init);
		for(int i = 0; i < 10; i++) assertEquals(orbit[i], xoshiro.nextLong());
		xoshiro.setState(init);
		xoshiro.jump();
 		final long[] jump = { 0x39c4396d8759c874L, 0x4b948d9de69752ecL, 0x871591604b03d9a6L, 0x444d6d471322d17bL, 0xb0a9eb9383bf0803L, 0x481f6c796c1d0ecaL, 0xb89a346b480341bfL, 0x1494bad1d1b19126L, 0xa2f5ca0a0ab0805L, 0x75a4de1da308cc8fL };
		for(int i = 0; i < 10; i++) assertEquals(jump[i], xoshiro.nextLong());
		xoshiro.setState(init);
		xoshiro.longJump();
		final long[] longJump = { 0xea14aabc151743b5L, 0x8941269998260040L, 0x4cf6a8517950ced8L, 0x2c657736be0af94cL, 0x9ce62322d721ea8fL, 0x6f2e0950267a4252L, 0x5ac6463cf03b2904L, 0x630400b3a65ac6c0L, 0xa220c012240ddeb0L, 0x9b1b712840162f46L };
		for(int i = 0; i < 10; i++) assertEquals(longJump[i], xoshiro.nextLong());
	}
}
