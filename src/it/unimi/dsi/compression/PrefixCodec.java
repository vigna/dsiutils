/*
 * DSI utilities
 *
 * Copyright (C) 2005-2022 Sebastiano Vigna
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

/** A codec based on a set of prefix-free codewords.
 *
 * <p>Prefix codec work by building a vector of prefix-free codewords, one for each symbol. The
 * method {@link #codeWords()} returns that vector. Moreover, this interface
 * strengthens the return type of {@link #coder()} to {@link PrefixCoder}.
 */
public interface PrefixCodec extends Codec {
	/** Returns the vector of prefix-free codewords used by this prefix coder.
	 *
	 * @return the vector of prefix-free codewords used by this prefix coder.
	 */
	public BitVector[] codeWords();

	@Override
	public PrefixCoder coder();
}
