/*
 * DSI utilities
 *
 * Copyright (C) 2006-2022 Sebastiano Vigna
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

package it.unimi.dsi.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import it.unimi.dsi.lang.MutableString;

/** A trivial {@link it.unimi.dsi.io.WordReader} that considers each line
 * of a document a single word.
 *
 * <p>The intended usage of this class is that of indexing stuff like lists of document
 * identifiers: if the identifiers contain nonalphabetical characters, the default
 * {@link it.unimi.dsi.io.FastBufferedReader} might do a poor job.
 *
 * <p>Note that the non-word returned by {@link #next(MutableString, MutableString)} is
 * always empty.
 */

public class LineWordReader implements WordReader, Serializable {
	private static final long serialVersionUID = 1L;
	/** An fast buffered reader wrapping the underlying reader. */
	private final FastBufferedReader fastBufferedReader = new FastBufferedReader();

	@Override
	public boolean next(final MutableString word, final MutableString nonWord) throws IOException {
		nonWord.length(0);
		return fastBufferedReader.readLine(word) != null;
	}

	@Override
	public LineWordReader setReader(final Reader reader) {
		fastBufferedReader.setReader(reader);
		return this;
	}

	@Override
	public LineWordReader copy() {
		return new LineWordReader();
	}
}
