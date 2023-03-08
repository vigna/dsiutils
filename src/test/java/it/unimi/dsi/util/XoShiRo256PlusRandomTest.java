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


public class XoShiRo256PlusRandomTest {
	private final static long seeds[] = { 0, 1, 1024, 0x5555555555555555L };

	@Test
	public void testNextFloat() {
		for (final long seed : seeds) {
			final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(seed);
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
			final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(seed);
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
			final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(seed);
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
			final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(seed);
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
			final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(seed);
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
		final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(0);
		for(int i = 0; i < 100; i++) assertTrue(xoroshiro.nextInt(16) < 16);
	}

	@Test
	public void testNextInt4() {
		final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(0);
		assertEquals(0, xoroshiro.nextInt(1));
	}


	@Test
	public void testNextLong() {
		for (final long seed : seeds) {
			final XoShiRo256PlusRandom xoroshiro = new XoShiRo256PlusRandom(seed);
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
		final long[] orbit = { 0xe77092bd078a0139L, 0x4ad92f33d3b2be9L, 0xf7019d33dcb99fb1L, 0x581cfcb4be4ed1ddL, 0xc29b3f4ee4526c3fL, 0xef66651c07e1618L, 0x1a6f8b43aa73f9c1L, 0xcd6599dac6e1c7ebL, 0x11e9832e7c8a9e7aL, 0x99a09cae77c38b7eL };
		final XoShiRo256PlusRandom xoshiro = new XoShiRo256PlusRandom();
		xoshiro.setState(init);
		for(int i = 0; i < 10; i++) assertEquals(orbit[i], xoshiro.nextLong());
		xoshiro.setState(init);
		xoshiro.jump();
		final long[] jump = { 0x4db287782e2b6213L, 0x8be468ceaac4e7dL, 0x76198eb1bd90279aL, 0x638b88335dcb517eL, 0xb441ed73feb34c0fL, 0x33f4be252f0d73d7L, 0x567891888a763a42L, 0x19bae72364c50a82L, 0x7ab315666ac37b54L, 0x50f2e3c8bd6699bfL };
		for(int i = 0; i < 10; i++) assertEquals(jump[i], xoshiro.nextLong());
		xoshiro.setState(init);
		xoshiro.longJump();
		final long[] longJump = { 0x303832d753c6a31fL, 0x1419941f89a6b9c2L, 0xf96518a2f1bfdd75L, 0x6aefa5ea3955a64aL, 0xf2b3fe98dee2e38fL, 0xce469025b5755e61L, 0xd5461bf1bfa403afL, 0xff1941b7cb77b5c1L, 0x56f9a8ed4d5962c3L, 0x9b2ba4be099eaf99L };
		for(int i = 0; i < 10; i++) assertEquals(longJump[i], xoshiro.nextLong());
	}

	private final static long[] JUMP32 = { 0x58120d583c112f69L, 0x7d8d0632bd08e6acL, 0x214fafc0fbdbc208L, 0x0e055d3520fdb9d7L };
	private final static long[] JUMP48 = { 0xf11fb4faea62c7f1L, 0xf825539dee5e4763L, 0x474579292f705634L, 0x5f728be2c97e9066L };
	private final static long[] JUMP64 = { 0xb13c16e8096f0754L, 0xb60d6c5b8c78f106L, 0x34faff184785c20aL, 0x12e4a2fbfc19bff9L };
	private final static long[] JUMP80 = { 0x6c1a4d1bee4cfb25L, 0x0355dab5aaada356L, 0x5d23c239088b488eL, 0x2c09ebb60b81941aL };
	private final static long[] JUMP96 = { 0x148c356c3114b7a9L, 0xcdb45d7def42c317L, 0xb27c05962ea56a13L, 0x31eebb6c82a9615fL };
	private final static long[] JUMP112 = { 0xff09f37df22eab9aL, 0xe903694ada9d6795L, 0x9a5475c8d2fb2d20L, 0x19809df824096ba1L };
	private final static long[] JUMP144 = { 0x3dcd32f39276a95fL, 0xc51212c8b1aa2787L, 0x962c90a866ea6719L, 0xb81875d0f4f6f253L };
	private final static long[] JUMP160 = { 0xc04b4f9c5d26c200L, 0x69e6e6e431a2d40bL, 0x4823b45b89dc689cL, 0xf567382197055bf0L };
	private final static long[] JUMP176 = { 0x053ff7e4e8581163L, 0x0b4df9e68366344aL, 0x259022fe05f4023eL, 0x2432aaa71d816e63L };

	@Test
	public void validateJumps() {
		final long[] init = { 0x7743a154e17a5e9bL, 0x7823a1cd9453899bL, 0x976589eefbb1c7f5L, 0x702cf168260fa29eL };
		final XoShiRo256PlusRandom xoshiro0 = new XoShiRo256PlusRandom();
		xoshiro0.setState(init);
		xoshiro0.jump(JUMP32);
		final XoShiRo256PlusRandom xoshiro1 = new XoShiRo256PlusRandom();
		xoshiro1.setState(init);
		for(long i = 0; i < (1L << 32); i++) xoshiro1.nextLong();
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP48);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP32);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP64);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP48);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP80);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP64);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP112);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP96);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump();
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP112);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP144);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump();
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP160);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP144);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.jump(JUMP176);
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP160);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());

		xoshiro0.longJump();
		for(long i = 0; i < (1L << 16); i++) xoshiro1.jump(JUMP176);
		assertEquals(xoshiro0.nextLong(), xoshiro1.nextLong());
	}
}
