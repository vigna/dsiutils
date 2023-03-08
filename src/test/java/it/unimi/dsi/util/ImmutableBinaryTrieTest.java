/*
 * DSI utilities
 *
 * Copyright (C) 2010-2023 Sebastiano Vigna
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

import java.util.List;

import org.junit.Test;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public class ImmutableBinaryTrieTest {

	public void testImmutableBinaryTrie(final List<String> strings) {
		final ObjectArrayList<BitVector> vectors = new ObjectArrayList<>();
		for (final String string : strings) {
			final BitVector v = LongArrayBitVector.ofLength(string.length());
			for(int j = 0; j < string.length(); j++) if (string.charAt(j) == '1') v.set(j);
			vectors.add(v);
		}

		final ImmutableBinaryTrie<BitVector> t = new ImmutableBinaryTrie<>(vectors, TransformationStrategies.identity());

		assertEquals(vectors.size(), t.size());
		for(int i = 0; i < vectors.size(); i++) assertEquals(vectors.get(i).toString(), i, t.getLong(vectors.get(i)));
	}

	@Test
	public void testEmptyImmutableBinaryTrie() {
		testImmutableBinaryTrie(ObjectLists.emptyList());
	}

	@Test
	public void testSingletonImmutableBinaryTrie() {
		testImmutableBinaryTrie(ObjectLists.singleton(STRINGS[0]));
	}

	@Test
	public void testEmptyStringSingletonImmutableBinaryTrie() {
		testImmutableBinaryTrie(ObjectLists.singleton(""));
	}

	@Test
	public void testDoubletonImmutableBinaryTrie() {
		testImmutableBinaryTrie(ObjectArrayList.wrap(STRINGS, 2));
	}

	@Test
	public void testImmutableBinaryTrie() {
		testImmutableBinaryTrie(ObjectArrayList.wrap(STRINGS));
	}


	public static final String[] STRINGS = {
	"000000",
	"000011100001100",
	"000110001010011000110010100011001",
	"000111001100011111110011",
	"00100000100101011",
	"0010001111011110111101100011",
	"001010001100111111110101010010100",
	"0010101001100001010001",
	"0010110101000011000010100010110000111101010",
	"0010110101100101101010",
	"001101001101011001101001100011",
	"00110111111100100110011100100100111100100",
	"0100000111011111110111101100011101",
	"010011111010101000011111011111110011101010010100",
	"0101001110000101001101",
	"0101100001100001100001010011000110001",
	"01011001111110110111100001",
	"010111111101000110101100",
	"0111001111001001100100101001110",
	"1000010001110111101000101110011111011100100",
	"1000011111110110010110101101",
	"100010100110111011010",
	"1000111110100001111001111010100110011111001111111110011",
	"1001010011111100110110000101001110111001001100",
	"1010111011100111000101111111110111111100001",
	"10110010011001101101010010001100001011111111101111111000011110011",
	"10110101010000101100101111111011",
	"1011011000100111110011110101110101001011",
	"101101111000100001001000010110010101111100001",
	"110001000010101001001110101110101001011",
	"110001111010100110011111000011110011",
	"11010010101001000011110011",
	"11010100110010110010010011110011",
	"11011001000110000010100",
	"110111100001100110011010",
	"11011111010101001011",
	"11100100110111100001",
	"111011110111011101010",
	"11111001001000011010001010100100010100100111100100",
	"111110101010000111100001",

	};

}
