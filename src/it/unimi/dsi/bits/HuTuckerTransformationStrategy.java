/*
 * DSI utilities
 *
 * Copyright (C) 2007-2023 Sebastiano Vigna
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

package it.unimi.dsi.bits;

import java.util.Iterator;

import it.unimi.dsi.compression.HuTuckerCodec;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;

/** A transformation strategy mapping strings to their {@linkplain HuTuckerCodec Hu-Tucker encoding}. The
 * encoding is guaranteed to preserve lexicographical ordering.
 */

public class HuTuckerTransformationStrategy extends PrefixCoderTransformationStrategy {
	private static final long serialVersionUID = 1;
	/** Creates a Hu-Tucker transformation strategy for the character sequences returned by the given iterable. The
	 * strategy will map a string to its Hu-Tucker encoding.
	 *
	 * @param iterable an iterable object returning character sequences.
	 * @param prefixFree if true, the resulting set of binary words will be prefix free.
	 */
	public HuTuckerTransformationStrategy(final Iterable<? extends CharSequence> iterable, final boolean prefixFree) {
		this(getCoder(iterable, prefixFree), prefixFree);
	}

	protected HuTuckerTransformationStrategy(final PrefixCoderTransformationStrategy huTuckerTransformationStrategy) {
		super(huTuckerTransformationStrategy);
	}

	protected HuTuckerTransformationStrategy(final Object[] a, final boolean prefixFree) {
		super((BitVector[])a[0], (Char2IntOpenHashMap)a[1], prefixFree);
	}

	private static Object[] getCoder(final Iterable<? extends CharSequence> iterable, final boolean prefixFree) {
		// First of all, we gather frequencies for all Unicode characters
		final long[] frequency = new long[Character.MAX_VALUE + 1];
		int maxWordLength = 0;
		CharSequence s;
		int n = 0;

		for(final Iterator<? extends CharSequence> i = iterable.iterator(); i.hasNext();) {
			s = i.next();
			maxWordLength = Math.max(s.length(), maxWordLength);
			for(int j = s.length(); j-- != 0;) frequency[s.charAt(j)]++;
			n++;
		}

		// Then, we compute the number of actually used characters. We count from the start the stop character.
		int count = prefixFree ? 1 : 0;
		for(int i = frequency.length; i-- != 0;) if (frequency[i] != 0) count++;

		/* Now we remap used characters in f, building at the same time the map from characters to symbols (except for the stop character). */
		final long[] packedFrequency = new long[count];
		final Char2IntMap char2symbol = new Char2IntOpenHashMap(count);

		for(int i = frequency.length, k = count; i-- != 0;) {
			if (frequency[i] != 0) {
				packedFrequency[--k] = frequency[i];
				char2symbol.put((char)i, k);
			}
		}

		if (prefixFree) packedFrequency[0] = n; // The stop character appears once in each string.

		// We now build the coder used to code the strings
		return new Object[] { new HuTuckerCodec(packedFrequency).coder().codeWords(), char2symbol };
	}

	@Override
	public PrefixCoderTransformationStrategy copy() {
		return new HuTuckerTransformationStrategy(this);
	}
}
