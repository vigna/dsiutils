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

package it.unimi.dsi.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;

/** An adapter that exposes a fast buffered reader as an iterator
 * over the returned lines. Since we just actually read a line to know
 * whether {@link #hasNext()} should return true, the last line read
 * from the underlying fast buffered reader has to be cached.
 * Mixing calls to this adapter and to the underlying fast buffered
 * reader will not usually give the expected results.
 *
 * <p>Since this class allocates no resource, it is not {@link Closeable} and
 * it will not close the underlying {@link FastBufferedReader} under any circumstances.
 * If you need resource handling, try {@link FileLinesCollection}.
 *
 * <p>This class reuses the same mutable strings. As a result,
 * the comments for {@link FileLinesCollection}
 * apply here. If you want just get all the remaining lines, use {@link #allLines()}.
 */

public class LineIterator implements ObjectIterator<MutableString> {
	/** The underlying fast buffered reader. */
	private final FastBufferedReader fastBufferedReader;
	/** The mutable strings returned by this iterator. */
	private final MutableString[] s = { new MutableString(), new MutableString() };
	/** An optional progress meter. */
	private final ProgressLogger pl;
	/** Whether we must still advance to the next line. */
	private boolean toAdvance = true;
	/** In case {@link #toAdvance} is false, whether there is a next line. */
	private boolean hasNext;
	/** Which string in {@link #s} should be returned next. */
	private int k;

	/** Creates a new line iterator over a specified fast buffered reader.
	 *
	 * @param fastBufferedReader the underlying buffered reader.
	 * @param pl an optional progress logger, or {@code null}.
	 * */
	public LineIterator(final FastBufferedReader fastBufferedReader, final ProgressLogger pl) {
		this.fastBufferedReader = fastBufferedReader;
		this.pl = pl;
	}

	/** Creates a new line iterator over a specified fast buffered reader.
	 *
	 * @param fastBufferedReader the underlying buffered reader.
	 */
	public LineIterator(final FastBufferedReader fastBufferedReader) {
		this(fastBufferedReader, null);
	}


	@Override
	public boolean hasNext() {
		if (toAdvance) {
			try {
				k = 1 - k;
				hasNext = fastBufferedReader.readLine(s[k]) != null;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			toAdvance = false;
		}

		return hasNext;
	}

	@Override
	public MutableString next() {
		if (! hasNext()) throw new NoSuchElementException();
		toAdvance = true;
		if (pl != null) pl.update();
		return s[k];
	}

	/** Returns all lines remaining in this iterator as a list.
	 *
	 * @return all lines remaining in this iterator as a list.
	 */

	public List<MutableString> allLines() {
		final ObjectArrayList<MutableString> result = new ObjectArrayList<>();
		while(hasNext()) result.add(next().copy());
		return result;
	}
}
