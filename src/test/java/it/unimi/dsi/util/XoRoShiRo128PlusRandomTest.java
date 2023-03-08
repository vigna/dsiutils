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


public class XoRoShiRo128PlusRandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(seed);
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
			final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(seed);
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
			final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(seed);
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
			final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(seed);
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
			final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(seed);
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
		final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(0);
		for(int i = 0; i < 100; i++) assertTrue(xoroshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(0);
		assertEquals(0, xoroshiro.nextInt(1));
	}


	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom(seed);
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
		final long[] orbit = { 0xef67432275cde836L, 0x52eff0255ea7cfL, 0xabc55625b7e89d7aL, 0xdef977dd59e55164L, 0x23f20f923ea91b7aL, 0x5c217d5792fb28d5L, 0xe6c8a093b4701591L, 0x1c36f8279ac503f8L, 0x694112875b35b28cL, 0xbbb7d4525cc25ecdL };
		final XoRoShiRo128PlusRandom xoroshiro = new XoRoShiRo128PlusRandom();
		xoroshiro.setState(init);
		for(int i = 0; i < 10; i++) assertEquals(orbit[i], xoroshiro.nextLong());
		xoroshiro.setState(init);
		xoroshiro.jump();
		final long[] jump = { 0x35f97b287185eca3L, 0x405b4a3314980a69L, 0x9e2df1203e76b5b3L, 0xf49110d6662950abL, 0xffc3952f56c8874eL, 0xe0da8e9f3508848aL, 0x8c0281d95ede36fbL, 0x934c3b9fd79157beL, 0xbfa7ae6e5abb1d49L, 0xc66c810eda29ec11L };
		for(int i = 0; i < 10; i++) assertEquals(jump[i], xoroshiro.nextLong());
		xoroshiro.setState(init);
		xoroshiro.longJump();
		final long[] longJump = { 0x6f0c2bc3ee39b4a3L, 0x936596b69fa15786L, 0x4f56e2157a6c3c02L, 0x3f0033ef481610b1L, 0xa347251682632303L, 0x59362e5b17a63723L, 0xb85ca0c9471fcc2fL, 0x9cb436e97c78480aL, 0x1bbf0c59202c967eL, 0x164b4c84bce8fe26L };
		for(int i = 0; i < 10; i++) assertEquals(longJump[i], xoroshiro.nextLong());
	}

	final long[] JUMP32 = { 0xfad843622b252c78L, 0xd4e95eef9edbdbc6L };
	final long[] JUMP48 = { 0xd769cfc9028deb78L, 0x9b19ba6b3752065aL };
	final long[] JUMP80 = { 0xe754db3fbc7536bcL, 0x2adca86fbefe1366L };

	@Test
	public void validateJumps() {
		final long[] init = { 0x7743a154e17a5e9bL, 0x7823a1cd9453899bL };
		final XoRoShiRo128PlusRandom xoroshiro0 = new XoRoShiRo128PlusRandom();
		xoroshiro0.setState(init);
		xoroshiro0.jump(JUMP32);
		final XoRoShiRo128PlusRandom xoroshiro1 = new XoRoShiRo128PlusRandom();
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
