/*
 * DSI utilities
 *
 * Copyright (C) 2005-2021 Sebastiano Vigna
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

import java.io.Serializable;
import java.util.Arrays;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.ints.IntArrays;

/** An implementation of Huffman optimal prefix-free coding.
 *
 * <p>A Huffman coder is built starting from an array of frequencies corresponding to each
 * symbol. Frequency 0 symbols are allowed, but they will degrade the resulting code.
 *
 * <p>Instances of this class compute a <em>canonical</em> Huffman code
 * (Eugene S. Schwartz and Bruce Kallick, &ldquo;Generating a Canonical Prefix Encoding&rdquo;, <i>Commun. ACM</i> 7(3), pages 166&minus;169, 1964), which can
 * by {@linkplain CanonicalFast64CodeWordDecoder quickly decoded using table lookups}.
 * The construction uses the most efficient one-pass in-place codelength computation procedure
 * described by Alistair Moffat and Jyrki Katajainen in &ldquo;In-Place Calculation of Minimum-Redundancy Codes&rdquo;,
 * <i>Algorithms and Data Structures, 4th International Workshop</i>,
 * number 955 in Lecture Notes in Computer Science, pages 393&minus;402, Springer-Verlag, 1995.
 *
 * <p>We note by passing that this coded uses a {@link CanonicalFast64CodeWordDecoder}, which does not support codelengths above 64.
 * However, since the worst case for codelengths is given by Fibonacci numbers, and frequencies are to be provided as integers,
 * no codeword longer than the base-[(5<sup>1/2</sup> + 1)/2] logarithm of 5<sup>1/2</sup> &#x00B7; 2<sup>31</sup> (less than 47) will ever be generated. */

public class HuffmanCodec implements PrefixCodec, Serializable {
	private static final boolean DEBUG = false;
	private static final long serialVersionUID = 2L;

	/** The number of symbols of this coder. */
	public final int size;
	/** The codewords for this coder. */
	private final BitVector[] codeWord;
	/** A cached singleton instance of the coder of this codec. */
	private final Fast64CodeWordCoder coder;
	/** A cached singleton instance of the decoder of this codec. */
	private final CanonicalFast64CodeWordDecoder decoder;

	private static long[] intArray2LongArray(final int a[]) {
		final long[] b = new long[a.length];
		for(int i = a.length; i-- != 0;) b[i] = a[i];
		return b;
	}

	/** Creates a new Huffman codec using the given vector of frequencies.
	 *
	 * @param frequency a vector of nonnnegative frequencies.
	 */
	public HuffmanCodec(final int[] frequency) {
		this(intArray2LongArray(frequency));
	}

	/** Creates a new Huffman codec using the given vector of frequencies.
	 *
	 * @param frequency a vector of nonnnegative frequencies.
	 */
	public HuffmanCodec(final long[] frequency) {
		size = frequency.length;

		if (size == 0 || size == 1) {
			codeWord = new BitVector[size];
			if (size == 1) codeWord[0] = LongArrayBitVector.getInstance();
			coder = new Fast64CodeWordCoder(codeWord, new long[size]);
			decoder = new CanonicalFast64CodeWordDecoder(new int[size], new int[size]);
			return;
        }

        final long[] a = new long[size];
        for(int i = size; i-- != 0;) a[i] = frequency[i];
        // Sort frequencies (this is the only n log n step).
        Arrays.sort(a);

        // The following lines are from Moffat & Katajainen sample code. Please refer to their paper.

        // First pass, left to right, setting parent pointers.
		a[0] += a[1];
		int root = 0;
		int leaf = 2;
		for (int next = 1; next < size - 1; next++) {
			// Select first item for a pairing.
			if (leaf >= size || a[root] < a[leaf]) {
				a[next] = a[root];
				a[root++] = next;
			}
			else a[next] = a[leaf++];

			// Add on the second item.
			if (leaf >= size || (root < next && a[root] < a[leaf])) {
				a[next] += a[root];
				a[root++] = next;
			}
			else a[next] += a[leaf++];
		}

		// Second pass, right to left, setting internal depths.
		a[size - 2] = 0;
		for (int next = size - 3; next >= 0; next--) a[next] = a[(int)a[next]] + 1;

		// Third pass, right to left, setting leaf depths.
		int available = 1, used = 0, depth = 0;
		root = size - 2;
		int next = size - 1;
		while (available > 0) {
			while (root >= 0 && a[root] == depth) {
				used++;
				root--;
			}
			while (available > used) {
				a[next--] = depth;
				available--;
			}
			available = 2 * used;
			depth++;
			used = 0;
		}

		// Reverse the order of symbol lengths, and store them into an int array.
		final int[] length = new int[size];
		for(int i = size; i-- != 0;) length[i] = (int)a[size - 1 - i];

		// Sort symbols indices by decreasing frequencies (so symbols correspond to lengths).
		final int[] symbol = new int[size];
		for(int i = size; i-- != 0;) symbol[i] = i;
		IntArrays.quickSort(symbol, 0, size, (x,y) -> Long.compare(frequency[y], frequency[x]));

		// Assign codewords (just for the coder--the decoder needs just the lengths).
		int s = symbol[0];
		int l = length[0];
		long value = 0;
		BitVector v;
		codeWord = new BitVector[size];
		final long[] longCodeWord = new long[size];
		codeWord[s] = LongArrayBitVector.getInstance().length(l);

		for(int i = 1; i < size; i++) {
			s = symbol[i];
			if (length[i] == l) value++;
			else {
				value++;
				value <<= length[i] - l;
				assert length[i] > l;
				l = length[i];
			}
			v = LongArrayBitVector.getInstance().length(l);
			for(int j = l; j-- != 0;) if ((1L << j & value) != 0) v.set(l - 1 - j);
			codeWord[s] = v;
			longCodeWord[s] = value;
		}

		coder = new Fast64CodeWordCoder(codeWord, longCodeWord);
		decoder = new CanonicalFast64CodeWordDecoder(length, symbol);

		if (DEBUG) {
			final BitVector[] codeWord = codeWords();
			System.err.println("Codes: ");
			for(int i = 0; i < size; i++)
				System.err.println(i + " (" + codeWord[i].length() + " bits): " + codeWord[i]);

			long totFreq = 0;
			for(int i = size; i-- != 0;) totFreq += frequency[i];
			long totBits = 0;
			for(int i = size; i-- != 0;) totBits += frequency[i] * codeWord[i].length();
			System.err.println("Compression: " + totBits + " / " + totFreq * Character.SIZE + " = " + (double)totBits/(totFreq * Character.SIZE));
		}
}

	@Override
	public CodeWordCoder coder() {
		return coder;
	}

	@Override
	public Decoder decoder() {
		return decoder;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public BitVector[] codeWords() {
		return coder.codeWords();
	}
}
