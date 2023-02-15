/*
 * DSI utilities
 *
 * Copyright (C) 2002-2023 Sebastiano Vigna
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

package it.unimi.dsi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.util.SplitMix64Random;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;

public class UtilTest {

	@Test
	public void testFormatBinarySize() {
		assertEquals("1", Util.formatBinarySize(1));
		assertEquals("2", Util.formatBinarySize(2));
		boolean ok = false;
		try {
			Util.formatBinarySize(6);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertEquals("128", Util.formatBinarySize(128));
		assertEquals("1Ki", Util.formatBinarySize(1024));
		assertEquals("2Ki", Util.formatBinarySize(2048));
		assertEquals("1Mi", Util.formatBinarySize(1024 * 1024));
		assertEquals("2Mi", Util.formatBinarySize(2 * 1024 * 1024));
		assertEquals("1Gi", Util.formatBinarySize(1024 * 1024 * 1024));
		assertEquals("2Gi", Util.formatBinarySize(2L * 1024 * 1024 * 1024));
		assertEquals("1Ti", Util.formatBinarySize(1024L * 1024 * 1024 * 1024));
		assertEquals("2Ti", Util.formatBinarySize(2L * 1024 * 1024 * 1024 * 1024));
	}

	@Test
	public void testFormatSize() {
		assertEquals("1", Util.formatSize(1));
		assertEquals("2", Util.formatSize(2));
		assertEquals("128", Util.formatSize(128));
		assertEquals("1.00K", Util.formatSize(1000));
		assertEquals("2.00K", Util.formatSize(2000));
		assertEquals("2.50K", Util.formatSize(2500));
		assertEquals("1.00M", Util.formatSize(1000 * 1000));
		assertEquals("2.00M", Util.formatSize(2 * 1000 * 1000));
		assertEquals("1.00G", Util.formatSize(1000 * 1000 * 1000));
		assertEquals("2.00G", Util.formatSize(2L * 1000 * 1000 * 1000));
		assertEquals("1.00T", Util.formatSize(1000L * 1000 * 1000 * 1000));
		assertEquals("2.00T", Util.formatSize(2L * 1000 * 1000 * 1000 * 1000));
	}


	@Test
	public void testInvertPermutation() {
		for(int k = 10; k-- != 0;) {
			final int[] p = Util.identity(k * 10);
			IntArrays.shuffle(p, new SplitMix64Random(0));
			int[] q = Util.invertPermutation(p);
			q = Util.invertPermutation(q);
			assertArrayEquals(q, p);
		}
	}

	@Test
	public void testInvertPermutationInPlace() {
		assertArrayEquals(new int[] { 0, 1, 2 }, Util.invertPermutationInPlace(new int[] { 0, 1, 2 }));
		assertArrayEquals(new int[] { 1, 0 }, Util.invertPermutationInPlace(new int[] { 1, 0 }));
		assertArrayEquals(new int[] { 0, 2, 1 }, Util.invertPermutationInPlace(new int[] { 0, 2, 1 }));
		assertArrayEquals(new int[] { 3, 0, 1, 2 }, Util.invertPermutationInPlace(new int[] { 1, 2, 3, 0 }));

		for(int k = 10; k-- != 0;) {
			final int[] p = Util.identity(k * 10);
			Collections.shuffle(IntArrayList.wrap(p));
			final int[] q = Util.invertPermutation(p);
			Util.invertPermutationInPlace(p);
			assertArrayEquals(q, p);
		}
	}

	@Test
	public void testInvertBigPermutation() {
		for(int k = 10; k-- != 0;) {
			final long[][] p = Util.identity(k * 10L);
			LongBigArrays.shuffle(p, new SplitMix64Random(0));
			long[][] q = Util.invertPermutation(p);
			q = Util.invertPermutation(q);
			assertArrayEquals(q, p);
		}
	}

	@Test
	public void testBigInvertPermutationInPlace() {
		assertArrayEquals(new long[][] { { 0, 1, 2 } }, Util.invertPermutationInPlace(new long[][] { { 0, 1, 2 } }));
		assertArrayEquals(new long[][] { { 1, 0 } }, Util.invertPermutationInPlace(new long[][] { { 1, 0 } }));
		assertArrayEquals(new long[][] { { 0, 2, 1 } }, Util.invertPermutationInPlace(new long[][] { { 0, 2, 1 } }));
		assertArrayEquals(new long[][] { { 3, 0, 1, 2 } }, Util.invertPermutationInPlace(new long[][] { { 1, 2, 3, 0 } }));

		for(int k = 10; k-- != 0;) {
			final long[][] p = Util.identity(k * 10L);
			LongBigArrays.shuffle(p, new SplitMix64Random(0));
			final long[][] q = Util.invertPermutation(p);
			Util.invertPermutationInPlace(p);
			assertArrayEquals(q, p);
		}
	}

	@Test
	public void testComposePermutation() {
		final XoRoShiRo128PlusPlusRandom r = new XoRoShiRo128PlusPlusRandom(0);
		for (final int s : new int[] { 10, 100, 1000 }) {
			final int[] identity = Util.identity(s);
			final int[] shuffle = Util.identity(s);
			IntArrays.shuffle(shuffle, r);
			assertArrayEquals(shuffle, Util.composePermutations(identity, shuffle));
			assertArrayEquals(shuffle, Util.composePermutations(shuffle, identity));
			assertArrayEquals(identity, Util.composePermutations(shuffle, Util.invertPermutation(shuffle)));
			assertArrayEquals(identity, Util.composePermutations(Util.invertPermutation(shuffle), shuffle));

			final int[] shuffle2 = Util.identity(s);
			IntArrays.shuffle(shuffle2, r);

			final int[] result = Util.composePermutations(shuffle, shuffle2);
			Util.composePermutationsInPlace(shuffle, shuffle2);
			assertArrayEquals(result, shuffle2);
		}
	}

	@Test
	public void testComposePermutationBig() {
		final XoRoShiRo128PlusPlusRandom r = new XoRoShiRo128PlusPlusRandom(0);
		for (final long s : new int[] { 10, 100, 1000 }) {
			final long[][] identity = Util.identity(s);
			final long[][] shuffle = Util.identity(s);
			LongBigArrays.shuffle(shuffle, r);
			assertArrayEquals(shuffle, Util.composePermutations(identity, shuffle));
			assertArrayEquals(shuffle, Util.composePermutations(shuffle, identity));
			assertArrayEquals(identity, Util.composePermutations(shuffle, Util.invertPermutation(shuffle)));
			assertArrayEquals(identity, Util.composePermutations(Util.invertPermutation(shuffle), shuffle));

			final long[][] shuffle2 = Util.identity(s);
			LongBigArrays.shuffle(shuffle2, r);

			final long[][] result = Util.composePermutations(shuffle, shuffle2);
			Util.composePermutationsInPlace(shuffle, shuffle2);
			assertArrayEquals(result, shuffle2);
		}
	}
}
