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

import java.io.IOException;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.io.OutputBitStream;

/** A fast coder based on a set of codewords of length at most 64. */

public final class Fast64CodeWordCoder extends CodeWordCoder {
	private static final long serialVersionUID = 1L;
	/** An array parallel to {@link #codeWord} containing the codewords as longs (right aligned). */
	private final long[] longCodeWord;
	/** A cached array, parallel to {@link #longCodeWord}, of codewords length. */
	private final int[] length;

	/** Creates a new codeword-based coder using the given vector of codewords. The
	 * coder will be able to encode symbols numbered from 0 to <code>codeWord.length-1</code>, included.
	 *
	 * @param codeWord a vector of codewords.
	 * @param longCodeWord the same codewords as those specified in <code>codeWord</code>, but
	 * as right-aligned longs written in left-to-right fashion.
	 */
	public Fast64CodeWordCoder(final BitVector[] codeWord, final long[] longCodeWord) {
		super(codeWord);
		this.longCodeWord = longCodeWord;
		length = new int[codeWord.length];
		for(int i = length.length; i-- != 0;) length[i] = (int) codeWord[i].length();
	}

	@Override
	public int encode(final int symbol, final OutputBitStream obs) throws IOException {
		return obs.writeLong(longCodeWord[symbol], length[symbol]);
	}
}
