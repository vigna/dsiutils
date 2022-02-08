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

import java.io.IOException;

import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.io.InputBitStream;

/** Decoding methods for a specific compression technique. */
public interface Decoder {

	/** Decodes the next symbol from the given boolean iterator.
	 *
	 * <P>Note that {@link InputBitStream} implements {@link BooleanIterator}.
	 *
	 * @param iterator a boolean iterator.
	 * @return the next symbol decoded from the bits emitted by <code>i</code>
	 * @throws java.util.NoSuchElementException if <code>iterator</code> terminates before a symbol has been decoded.
	 */
	int decode(BooleanIterator iterator);

	/** Decodes the next symbol from the given input bit stream.
	 *
	 * <P>Note that {@link InputBitStream} implements {@link BooleanIterator}.
	 *
	 * @param ibs an input bit stream.
	 * @return the next symbol decoded from <code>ibs</code>.
	 */
	int decode(InputBitStream ibs) throws IOException;
}
