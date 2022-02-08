/*
 * DSI utilities
 *
 * Copyright (C) 2014-2022 Sebastiano Vigna
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


public class XoRoShiRo128PlusPlusRandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(seed);
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
			final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(seed);
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
			final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(seed);
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
			final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(seed);
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
			final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(seed);
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
		final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(0);
		for(int i = 0; i < 100; i++) assertTrue(xoroshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(0);
		assertEquals(0, xoroshiro.nextInt(1));
	}


	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom(seed);
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
		final long[] orbit = { 0xfd888cf0b1e83d69L, 0x72a1fc11037a8789L, 0x8ee71e6bcf64049eL, 0x403571d0c2e52246L, 0x58bae6b88f95bed0L, 0xc07dee980894e44L, 0xa68b503fb6b6175eL, 0xd855829c8095bdafL, 0xaa452f19139d5479L, 0x65cca0b422a6614L };
		final XoRoShiRo128PlusPlusRandom xoroshiro = new XoRoShiRo128PlusPlusRandom();
		xoroshiro.setState(init);
		for(int i = 0; i < 10; i++) assertEquals(orbit[i], xoroshiro.nextLong());
		xoroshiro.setState(init);
		xoroshiro.jump();
		final long[] jump = { 0x4a0f0bf4c5d2849bL, 0x55a650a4c1dac3e9L, 0xb39ae98dfa439b70L, 0xae4e15c27032f655L, 0xb809d9851be9a361L, 0xdb0637af4f0fb298L, 0x47396236cc0be131L, 0x62e5daddb5631c80L, 0xe08285c97c64b959L, 0xfe19a2c9099fd005L };
		for(int i = 0; i < 10; i++) assertEquals(jump[i], xoroshiro.nextLong());
		xoroshiro.setState(init);
		xoroshiro.longJump();
		final long[] longJump = { 0x6076f989b46f623cL, 0xe7e51ffa73c8ce0bL, 0x336f420fd869d395L, 0x95455ce58c579ad3L, 0x7981659844005777L, 0xe084823b2c5164d2L, 0xfd9c2f3660816611L, 0x7b9d031b3d57cbddL, 0x90c3fc7b6f588dfaL, 0x514a755401b70e85L };
		for(int i = 0; i < 10; i++) assertEquals(longJump[i], xoroshiro.nextLong());
	}

	final long[] JUMP32 = { 0xfcceec21d5c306d9L, 0x2e1bcf52f1051044L };
	final long[] JUMP48 = { 0x99030a888c867939L, 0xc8462a08ab3d7f9bL };
	final long[] JUMP80 = { 0x38c70073805418e8L, 0x05759cda152a1664L };

	@Test
	public void validateJumps() {
		final long[] init = { 0x7743a154e17a5e9bL, 0x7823a1cd9453899bL };
		final XoRoShiRo128PlusPlusRandom xoroshiro0 = new XoRoShiRo128PlusPlusRandom();
		xoroshiro0.setState(init);
		xoroshiro0.jump(JUMP32);
		final XoRoShiRo128PlusPlusRandom xoroshiro1 = new XoRoShiRo128PlusPlusRandom();
		xoroshiro1.setState(init);
		for(long i = 0; i < (1L << 32); i++) xoroshiro1.nextLong();
		assertEquals(xoroshiro0.nextLong(), xoroshiro1.nextLong());

		xoroshiro0.jump(JUMP48);
		for(long i = 0; i < (1L << 16); i++) xoroshiro1.jump(JUMP32);
		assertEquals(xoroshiro0.nextLong(), xoroshiro1.nextLong());

		xoroshiro0.jump();
		for(long i = 0; i < (1L << 16); i++) xoroshiro1.jump(JUMP48);
		assertEquals(xoroshiro0.nextLong(), xoroshiro1.nextLong());

		xoroshiro0.jump(JUMP80);
		for(long i = 0; i < (1L << 16); i++) xoroshiro1.jump();
		assertEquals(xoroshiro0.nextLong(), xoroshiro1.nextLong());

		xoroshiro0.longJump();
		for(long i = 0; i < (1L << 16); i++) xoroshiro1.jump(JUMP80);
		assertEquals(xoroshiro0.nextLong(), xoroshiro1.nextLong());
	}
}
