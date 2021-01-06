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

import it.unimi.dsi.bits.BitVector;

/** A coder based on a set of prefix-free codewords.
 *
 * <P>Not all coders are codeword-based (for instance, arithmetic coding
 * is not codeword-based). However, coders that are based on prefix-free codewords are invited
 * to return by means of {@link it.unimi.dsi.compression.Codec#coder()} an
 * implementation of this interface.
 *
 * <p>Note that the {@linkplain PrefixCodec#coder() coder} returned by a {@link PrefixCodec} is
 * an implementation of this interface.
 */
public interface PrefixCoder extends Coder {

	/** Provides access to the codewords.
	 *
	 * <strong>Warning</strong>: bit 0 of each bit vector returned by {@link #codeWords()} is
	 * the <em>first (leftmost) bit</em> of the corresponding codeword: in other words, codewords are stored in
	 * right-to-left fashion.
	 *
	 * @return the codewords.
	 */

	BitVector[] codeWords();
}
