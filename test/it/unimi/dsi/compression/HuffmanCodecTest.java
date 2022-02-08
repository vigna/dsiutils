/*
 * DSI utilities
 *
 * Copyright (C) 2007-2022 Sebastiano Vigna
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

package it.unimi.dsi.compression;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.util.SplitMix64Random;

public class HuffmanCodecTest extends CodecTestCase {
	@Test
	public void testZeroSymbols() {
		final HuffmanCodec codec = new HuffmanCodec(new int[0]);
		assertEquals(0, codec.codeWords().length);
	}

	@Test
	public void testOneSymbol() throws IOException {
		final HuffmanCodec codec = new HuffmanCodec(new int[] { 1 });
		assertEquals(1, codec.codeWords().length);
		assertEquals(LongArrayBitVector.ofLength(0), codec.codeWords()[0]);
		final Random r = new SplitMix64Random(0);
		checkPrefixCodec(codec, r);
	}

	@Test
	public void testTwoEquiprobableSymbols() throws IOException {
		final HuffmanCodec codec = new HuffmanCodec(new int[] { 1, 1 });
		assertEquals(2, codec.codeWords().length);
		final BitVector v = LongArrayBitVector.ofLength(1);
		assertEquals(v, codec.codeWords()[0]);
		v.set(0);
		assertEquals(v, codec.codeWords()[1]);
		final Random r = new SplitMix64Random(0);
		checkPrefixCodec(codec, r);
	}

	@Test
	public void testThreeNonequiprobableSymbols() throws IOException {
		final HuffmanCodec codec = new HuffmanCodec(new int[] { 1, 2, 4 });
		assertEquals(3, codec.codeWords().length);
		final BitVector v = LongArrayBitVector.ofLength(2);
		v.set(0);
		assertEquals(v, codec.codeWords()[1]);
		v.set(1);
		assertEquals(v, codec.codeWords()[0]);
		assertEquals(LongArrayBitVector.ofLength(1), codec.codeWords()[2]);
		final Random r = new SplitMix64Random(0);
		checkPrefixCodec(codec, r);
	}

	@Test
	public void testJumpInLengths() throws IOException {
		final HuffmanCodec codec = new HuffmanCodec(new int[] { 10, 1, 1, 1, 1 });
		final Random r = new SplitMix64Random(0);
		checkPrefixCodec(codec, r);
	}


	@Test
	public void testRandomFrequencies() throws IOException {
		final Random r = new SplitMix64Random(0);
		final int[] frequency = new int[100];
		for(int i = 0; i < frequency.length; i++) frequency[i] = r.nextInt(1000);
		final HuffmanCodec codec = new HuffmanCodec(frequency);
		checkPrefixCodec(codec, r);
	}

	@Test
	public void testRandomCodeLengths() throws IOException {
		final int[] frequency = { 805, 1335, 6401, 7156, 7333, 10613, 10951, 11708, 12710, 12948, 13237, 13976, 20355, 20909, 22398, 26303, 26400, 28380, 28865, 30152, 31693, };
		final int[] codeLength = { 7, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 3, 3 };
		final HuffmanCodec codec = new HuffmanCodec(frequency);
		checkLengths(frequency, codeLength, codec.codeWords());
		checkPrefixCodec(codec, new SplitMix64Random(0));
	}

	@Test
	public void testExponentialCodeLengths() throws IOException {
		final int[] frequency = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824 };
		final int[] codeLength = { 30, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
		final HuffmanCodec codec = new HuffmanCodec(frequency);
		checkLengths(frequency, codeLength, codec.codeWords());
		checkPrefixCodec(codec, new SplitMix64Random(0));
	}

	@Test
	public void testFibonacciCodeLengths() throws IOException {
		final int[] frequency = new int[44];
		frequency[0] = frequency[1] = 1;
		for(int i = 2; i < frequency.length; i++) frequency[i] = frequency[i - 1] + frequency[i - 2];
		final int[] codeLength = { 43, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
		final HuffmanCodec codec = new HuffmanCodec(frequency);
		checkLengths(frequency, codeLength, codec.codeWords());
		checkPrefixCodec(codec, new SplitMix64Random(0));
	}
}
