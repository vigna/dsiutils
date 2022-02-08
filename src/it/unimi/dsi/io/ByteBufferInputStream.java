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

package it.unimi.dsi.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import it.unimi.dsi.fastutil.io.MeasurableInputStream;
import it.unimi.dsi.fastutil.io.RepositionableStream;


/** A bridge between byte {@linkplain ByteBuffer buffers} and {@linkplain InputStream input streams}.
 *
 * <p>Java's {@linkplain FileChannel#map(MapMode, long, long) memory-mapping facilities} have
 * the severe limitation of mapping at most {@link Integer#MAX_VALUE} bytes, as they
 * expose the content of a file using a {@link MappedByteBuffer}. This class can {@linkplain  #map(FileChannel, FileChannel.MapMode) expose
 * a file of arbitrary length} as a {@linkplain RepositionableStream repositionable} {@link MeasurableInputStream}
 * that is actually based on an array of {@link MappedByteBuffer}s, each mapping
 * a <em>chunk</em> of {@link #CHUNK_SIZE} bytes.
 *
 * @author Sebastiano Vigna
 * @since 1.2
 */

public class ByteBufferInputStream extends MeasurableInputStream implements RepositionableStream {
	private static int CHUNK_SHIFT = 30;

	/** The size of a chunk created by {@link #map(FileChannel, FileChannel.MapMode)}. */
	public static final long CHUNK_SIZE = 1L << CHUNK_SHIFT;

	/** The underlying byte buffers. */
	private final ByteBuffer[] byteBuffer;

	/** An array parallel to {@link #byteBuffer} specifying which buffers do not need to be
	 * {@linkplain ByteBuffer#duplicate() duplicated} before being used. */
	private final boolean[] readyToUse;

	/** The number of byte buffers. */
	private final int n;

	/** The current buffer. */
	private int curr;

	/** The current mark as a position, or -1 if there is no mark. */
	private long mark;

	/** The overall size of this input stream. */
	private final long size;

	/** The capacity of the last buffer. */
	private final int lastBufferCapacity;

	/** Creates a new byte-buffer input stream from a single {@link ByteBuffer}.
	 *
	 * @param byteBuffer the underlying byte buffer.
	 */

	public ByteBufferInputStream(final ByteBuffer byteBuffer) {
		this(new ByteBuffer[] { byteBuffer }, byteBuffer.capacity(), 0, new boolean[1]);
	}

	/** Creates a new byte-buffer input stream.
	 *
	 * @param byteBuffer the underlying byte buffers.
	 * @param size the sum of the {@linkplain ByteBuffer#capacity() capacities} of the byte buffers.
	 * @param curr the current buffer (reading will start at this buffer from its current position).
	 * @param readyToUse an array parallel to <code>byteBuffer</code> specifying which buffers do not need to be
	 * {@linkplain ByteBuffer#duplicate() duplicated} before being used (the process will happen lazily); the array
	 * will be used internally by the newly created byte-buffer input stream.
	 */

	protected ByteBufferInputStream(final ByteBuffer[] byteBuffer, final long size, final int curr, final boolean[] readyToUse) {
		this.byteBuffer = byteBuffer;
		this.n = byteBuffer.length;
		this.curr = curr;
		this.size = size;
		this.readyToUse = readyToUse;

		mark = -1;

		for(int i = 0; i < n; i++) if (i < n - 1 && byteBuffer[i].capacity() != CHUNK_SIZE) throw new IllegalArgumentException();
		lastBufferCapacity = byteBuffer[n - 1].capacity();
	}

	/** Creates a new read-only byte-buffer input stream by mapping a given file channel.
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @return a new read-only byte-buffer input stream over the contents of <code>fileChannel</code>.
	 */
	public static ByteBufferInputStream map(final FileChannel fileChannel) throws IOException {
		return map(fileChannel, MapMode.READ_ONLY);
	}

	/** Creates a new byte-buffer input stream by mapping a given file channel.
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @param mapMode this must be {@link MapMode#READ_ONLY}.
	 * @return a new byte-buffer input stream over the contents of <code>fileChannel</code>.
	 */
	public static ByteBufferInputStream map(final FileChannel fileChannel, final MapMode mapMode) throws IOException {
		final long size = fileChannel.size();
		final int chunks = (int)((size + (CHUNK_SIZE - 1)) / CHUNK_SIZE);
		final ByteBuffer[] byteBuffer = new ByteBuffer[chunks];
		for(int i = 0; i < chunks; i++) byteBuffer[i] = fileChannel.map(mapMode, i * CHUNK_SIZE, Math.min(CHUNK_SIZE, size - i * CHUNK_SIZE));
		byteBuffer[0].position(0);
		final boolean[] readyToUse = new boolean[chunks];
		Arrays.fill(readyToUse, true);
		return new ByteBufferInputStream(byteBuffer, size, 0, readyToUse);
	}

	private ByteBuffer byteBuffer(final int n) {
		if (readyToUse[n]) return byteBuffer[n];
		readyToUse[n] = true;
		return byteBuffer[n] = byteBuffer[n].duplicate();
	}


	private long remaining() {
		return curr == n - 1 ? byteBuffer(curr).remaining() :
			byteBuffer(curr).remaining() + ((long)(n - 2 - curr) << CHUNK_SHIFT) + lastBufferCapacity;
	}

	@Override
	public int available() {
		final long available = remaining();
		return available <= Integer.MAX_VALUE ? (int)available : Integer.MAX_VALUE;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void mark(final int unused) {
		mark = position();
	}

	@Override
	public synchronized void reset() throws IOException {
		if (mark == -1) throw new IOException();
		position(mark);
	}

	@Override
	public long skip(final long n) throws IOException {
		final long toSkip = Math.min(remaining(), n);
		position(position() + toSkip);
		return toSkip;
	}

	@Override
	public int read() {
		if (! byteBuffer(curr).hasRemaining()) {
			if (curr < n - 1) byteBuffer(++curr).position(0);
			else return -1;
		}

		return byteBuffer[curr].get() & 0xFF;
	}

	@Override
	public int read(final byte[] b, final int offset, final int length) {
		if (length == 0) return 0;
		final long remaining = remaining();
		if (remaining == 0) return -1;
		final int realLength = (int)Math.min(remaining, length);
		int read = 0;
		while(read < realLength) {
			int rem = byteBuffer(curr).remaining();
			if (rem == 0) {
				byteBuffer(++curr).position(0);
				rem = byteBuffer(curr).remaining();
			}
			byteBuffer[curr].get(b, offset + read, Math.min(realLength - read, rem));
			read += Math.min(realLength - read, rem);
		}
		return realLength;
	}

	@Override
	public long length() {
		return size;
	}

	@Override
	public long position() {
		return ((long)curr << CHUNK_SHIFT) + byteBuffer(curr).position();
	}

	@Override
	public void position(long newPosition) {
		newPosition = Math.min(newPosition, length());
		if (newPosition == length()) {
			final ByteBuffer buffer = byteBuffer(curr = n - 1);
			buffer.position(buffer.capacity());
			return;
		}

		curr = (int)(newPosition >>> CHUNK_SHIFT);
		byteBuffer(curr).position((int)(newPosition - ((long)curr << CHUNK_SHIFT)));
	}

	public ByteBufferInputStream copy() {
		return new ByteBufferInputStream(byteBuffer.clone(), size, curr, new boolean[n]);
	}
}
