/*
 * DSI utilities
 *
 * Copyright (C) 2012-2022 Sebastiano Vigna
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

package it.unimi.dsi.util;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import it.unimi.dsi.fastutil.longs.AbstractLongBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.MappedLongBigList;
import it.unimi.dsi.lang.FlyweightPrototype;


/**
 * A bridge between byte {@linkplain ByteBuffer buffers} and {@linkplain LongBigList long big
 * lists}.
 *
 * <p>
 * Java's {@linkplain FileChannel#map(MapMode, long, long) memory-mapping facilities} have the
 * severe limitation of mapping at most {@link Integer#MAX_VALUE} bytes, as they expose the content
 * of a file using a {@link MappedByteBuffer}. This class can
 * {@linkplain #map(FileChannel, ByteOrder, FileChannel.MapMode) expose a file of longs of arbitrary
 * length} as a {@link LongBigList} that is actually based on an array of {@link MappedByteBuffer}s,
 * each mapping a <em>chunk</em> of {@link #CHUNK_SIZE} longs.
 *
 * <p>
 * Instances of this class are not thread safe, but the {@link #copy()} method provides a
 * lightweight duplicate that can be accessed independently by another thread. Only chunks that are
 * actually used will be {@linkplain ByteBuffer#duplicate() duplicated} lazily.
 *
 * @author Sebastiano Vigna
 * @deprecated Use {@link MappedLongBigList} instead.
 */
@Deprecated
public class ByteBufferLongBigList extends AbstractLongBigList implements FlyweightPrototype <ByteBufferLongBigList> {
	private static int CHUNK_SHIFT = 27;

	/** The size in longs of a chunk created by {@link #map(FileChannel, ByteOrder, FileChannel.MapMode)}. */
	public static final long CHUNK_SIZE = 1L << CHUNK_SHIFT;

	/** The mask used to compute the offset in the chunk in longs. */
	private static final long CHUNK_MASK = CHUNK_SIZE - 1;

	/** The underlying byte buffers. */
	private final ByteBuffer[] byteBuffer;

	/** An array parallel to {@link #byteBuffer} specifying which buffers do not need to be
	 * {@linkplain ByteBuffer#duplicate() duplicated} before being used. */
	private final boolean[] readyToUse;

	/** The number of byte buffers. */
	private final int n;

	/** The overall size in longs. */
	private final long size;

	/** Creates a new byte-buffer long big list from a single {@link ByteBuffer}.
	 *
	 * @param byteBuffer the underlying byte buffer.
	 */
	public ByteBufferLongBigList(final ByteBuffer byteBuffer) {
		this(new ByteBuffer[] { byteBuffer }, byteBuffer.capacity() / 8, new boolean[1]);
	}

	/** Creates a new byte-buffer long big list.
	 *
	 * @param byteBuffer the underlying byte buffers.
	 * @param size the overall number of longs in the underlying byte buffers (i.e., the
	 * sum of the {@linkplain ByteBuffer#capacity() capacities} of the byte buffers divided by eight).
	 * @param readyToUse an array parallel to <code>byteBuffer</code> specifying which buffers do not need to be
	 * {@linkplain ByteBuffer#duplicate() duplicated} before being used (the process will happen lazily); the array
	 * will be used internally by the newly created byte-buffer long big list.
	 */

	protected ByteBufferLongBigList(final ByteBuffer[] byteBuffer, final long size, final boolean[] readyToUse) {
		this.byteBuffer = byteBuffer;
		this.n = byteBuffer.length;
		this.size = size;
		this.readyToUse = readyToUse;

		for(int i = 0; i < n; i++) if (i < n - 1 && byteBuffer[i].capacity() / 8 != CHUNK_SIZE) throw new IllegalArgumentException();
	}

	/** Creates a new byte-buffer long big list by read-only mapping a given file channel using the standard Java (i.e., {@link DataOutput}) byte order ({@link ByteOrder#BIG_ENDIAN}).
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @return a new read-only byte-buffer long big list over the contents of <code>fileChannel</code>.
	 *
	 * @see #map(FileChannel, ByteOrder, MapMode)
	 */

	public static ByteBufferLongBigList map(final FileChannel fileChannel) throws IOException {
		return map(fileChannel, ByteOrder.BIG_ENDIAN);
	}

	/** Creates a new byte-buffer long big list by read-only mapping a given file channel.
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @param byteOrder a prescribed byte order.
	 * @return a new read-only byte-buffer long big list over the contents of <code>fileChannel</code>.
	 *
	 * @see #map(FileChannel, ByteOrder, MapMode)
	 */

	public static ByteBufferLongBigList map(final FileChannel fileChannel, final ByteOrder byteOrder) throws IOException {
		return map(fileChannel, byteOrder, MapMode.READ_ONLY);
	}

	/** Creates a new byte-buffer long big list by mapping a given file channel.
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @param byteOrder a prescribed byte order.
	 * @param mapMode the mapping mode: usually {@link MapMode#READ_ONLY}, but if intend to make the list
	 * {@linkplain #set(long, long) mutable}, you have to pass {@link MapMode#READ_WRITE}.
	 * @return a new byte-buffer long big list over the contents of <code>fileChannel</code>.
	 */

	public static ByteBufferLongBigList map(final FileChannel fileChannel, final ByteOrder byteOrder, final MapMode mapMode) throws IOException {
		final long size = fileChannel.size() / 8;
		final int chunks = (int)((size + (CHUNK_SIZE - 1)) / CHUNK_SIZE);
		final ByteBuffer[] byteBuffer = new ByteBuffer[chunks];
		for(int i = 0; i < chunks; i++) byteBuffer[i] = fileChannel.map(mapMode, i * CHUNK_SIZE * 8, Math.min(CHUNK_SIZE, size - i * CHUNK_SIZE) * 8).order(byteOrder);
		final boolean[] readyToUse = new boolean[chunks];
		Arrays.fill(readyToUse, true);
		return new ByteBufferLongBigList(byteBuffer, size, readyToUse);
	}

	private ByteBuffer byteBuffer(final int n) {
		if (readyToUse[n]) return byteBuffer[n];
		readyToUse[n] = true;
		return byteBuffer[n] = byteBuffer[n].duplicate().order(byteBuffer[n].order());
	}

	@Override
	public ByteBufferLongBigList copy() {
		return new ByteBufferLongBigList(byteBuffer.clone(), size, new boolean[n]);
	}

	@Override
	public long getLong(final long index) {
		return byteBuffer((int)(index >>> CHUNK_SHIFT)).getLong((int)(index & CHUNK_MASK) << 3);
	}

	@Override
	public long set(final long index, final long value) {
		final ByteBuffer b = byteBuffer((int)(index >>> CHUNK_SHIFT));
		final int i = (int)(index & CHUNK_MASK) << 3;
		final long previousValue = b.getLong(i);
		b.putLong(i, value);
		return previousValue;
	}

	@Override
	public long size64() {
		return size;
	}
}
