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
import it.unimi.dsi.io.OutputBitStream;

/** Coding methods for a specific compression technique. */
public interface Coder {
	/** Encodes a symbol.
	 *
	 * @param symbol a symbol.
	 * @return a boolean iterator returning the bits coding <code>symbol</code>.
	 */
	BooleanIterator encode(int symbol);

	/** Encodes a symbol.
	 *
	 * @param symbol a symbol.
	 * @param obs the output bit stream where the encoded symbol will be written.
	 * @return the number of bits written.
	 */
	int encode(int symbol, OutputBitStream obs) throws IOException;

	/** Flushes the coder.
	 *
	 * <strong>Warning</strong>: this method will <em>not</em> {@link OutputBitStream#flush() flush} <code>obs</code>.
	 *
	 * @param obs the output bit stream where the flushing bits will be written.
	 * @return the number of bits written to flush the coder.
	 */

	int flush(OutputBitStream obs);

	/** Flushes the coder.
	 *
	 * @return a boolean iterator returning the bits used to flush this coder.
	 */

	BooleanIterator flush();
}
