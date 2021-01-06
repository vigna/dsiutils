/*
 * DSI utilities
 *
 * Copyright (C) 2003-2021 Sebastiano Vigna
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
import java.io.InputStream;

import it.unimi.dsi.fastutil.objects.ObjectArrays;

/** A multiple input stream.
 *
 * <p>Instances of this class encapsulate a sequence of input streams.
 * When one of the streams is exhausted, the multiple stream behaves as if on an end of file.
 * However, after calling {@link #reset()} the stream is again readable, and positioned
 * at the start of the following stream.
 */

public class MultipleInputStream extends InputStream {

	/** The sequence of input streams that will be returned. */
	private final InputStream[] inputStream;
	/** The first output stream in {@link #inputStream} to be used. */
	private final int from;
	/** The last element of {@link #inputStream} to be used plus one. */
	private final int to;
	/** The index of the current input stream in {@link #inputStream}. */
	private int curr;
	/** The current input stream. */
	private InputStream currStream;

	/** Creates a new multiple input stream by encapsulating a nonempty fragment of an array of input streams.
	 *
	 * @param inputStream an array of input streams, that will be encapsulated.
	 * @param offset the first input stream that will be encapsulated.
	 * @param length the number of input streams to be encapsulated; it <strong>must</strong> be positive.
	 */
	private MultipleInputStream(final InputStream[] inputStream, final int offset, final int length) {
		ObjectArrays.ensureOffsetLength(inputStream, offset, length);
		this.inputStream = inputStream;
		this.from = offset;
		this.to = offset + length;

		curr = offset;
		currStream = inputStream[curr];
	}

	/** Returns an input stream encapsulating a nonempty fragment of an array of input streams.
	 *
	 * @param inputStream an array of input streams, that will be encapsulated.
	 * @param offset the first input stream that will be encapsulated.
	 * @param length the number of input streams to be encapsulated.
	 * @return an input stream encapsulating the argument streams (the only argument, if length is 1).
	 */
	public static InputStream getStream(final InputStream[] inputStream, final int offset, final int length) {
		if (length == 0) return NullInputStream.getInstance();
		if (length == 1) return inputStream[offset];
		return new MultipleInputStream(inputStream, offset ,length);
	}


	/** Returns an input stream encapsulating a nonempty array of input streams.
	 *
	 * <p>Note that if <code>inputStream.length</code> is 1 this method will return the only stream
	 * that should be encapsulated.
	 *
	 * @param inputStream an array of input streams, that will be encapsulated.
	 * @return an input stream encapsulating the argument streams (the only argument, if the length is 1).
	 */
	public static InputStream getStream(final InputStream[] inputStream) {
		return getStream(inputStream, 0, inputStream.length);
	}

	@Override
	public int available() throws IOException {
		return currStream.available();
	}

	@Override
	public void close() throws IOException {
		for(int i = from; i < to; i++) inputStream[i].close();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read() throws IOException {
		return currStream.read();
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return currStream.read(b, off, len);
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return currStream.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		if (curr == to - 1) throw new IOException("The streams in this multiple input stream have been exhausted");
		currStream = inputStream[++curr];
	}

	@Override
	public long skip(final long n) throws IOException {
		return currStream.skip(n);
	}
}
