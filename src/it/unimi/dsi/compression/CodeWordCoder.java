/*
 * DSI utilities
 *
 * Copyright (C) 2005-2023 Sebastiano Vigna
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
import java.io.Serializable;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanIterators;
import it.unimi.dsi.io.OutputBitStream;

/** A coder based on a set of codewords. */

public class CodeWordCoder implements PrefixCoder, Serializable {
	private static final long serialVersionUID = 1L;
	/** The array of codewords of this coder. */
	protected final BitVector[] codeWord;

	/** Creates a new codeword-based coder using the given vector of codewords. The
	 * coder will be able to encode symbols numbered from 0 to <code>codeWord.length-1</code>, included.
	 *
	 * @param codeWord a vector of codewords.
	 */
	public CodeWordCoder(final BitVector[] codeWord) {
		this.codeWord = codeWord;
	}

	@Override
	public BooleanIterator encode(final int symbol) {
		return codeWord[symbol].iterator();
	}

	@Override
	public int encode(final int symbol, final OutputBitStream obs) throws IOException {
		final BitVector w = codeWord[symbol];
		final int length = (int) w.length();
		for(int i = 0; i < length; i++) obs.writeBit(w.getBoolean(i));
		return length;
	}

	@Override
	public int flush(final OutputBitStream unused) { return 0; }

	@Override
	public BooleanIterator flush() { return BooleanIterators.EMPTY_ITERATOR; }

	@Override
	public BitVector[] codeWords() { return codeWord; }
}
