/*
 * DSI utilities
 *
 * Copyright (C) 2013-2022 Sebastiano Vigna
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.Size64;

/**
 * A queue of bytes partially stored on disk.
 *
 * <P>Instances of this class keep track of a queue of bytes using a circular
 * (and possibly {@linkplain ByteBuffer#allocateDirect(int) direct})
 * {@link ByteBuffer} that contains the head and the tail of the queue.
 * The central part of the queue, if necessary, is dumped on disk. Note that {@link #clear()}
 * does empty the queue, but it does not remove the dump file on disk: use {@link #trim()} to trim
 * the file at the smallest possible length. The dump file can be removed only with a call to
 * {@link #close()}, or when the instance is finalised.
 *
 * <P>Since file handles are a precious resource, this class provides a {@link #suspend()} method
 * that releases the file handle without deleting the dump file. All calls to methods accessing the
 * file handle will reinitialise it lazily (which implies that a certain number of
 * enqueues/dequeues can be performed on a suspended class without actually reopening
 * the dump file).
 *
 * <P>You can create a queue {@linkplain #createNew(File, int, boolean) using a new or empty file}, or
 * {@linkplain #createFromFile(File, int, boolean) using an existing file}, in which case the
 * content of the file will become the content of the queue. A {@link #freeze()} method closes
 * a queue but leaves behind a dump file containing the current content of the queue, so that it
 * can be {@linkplain #createFromFile(File, int, boolean) reopened} later.
 *
 * <p><strong>Warning</strong>: the {@link #close()} method will <em>delete</em> the queue file from
 * disk. This implies that if a file with the same name has been created in the meanwhile, it will
 * be deleted.
 */

public class ByteDiskQueue implements Closeable, Size64 {
	private static final boolean DEBUG = false;

	/** The file containing the disk-based part of the queue. */
	private final File file;
	/** Whether {@link #buffer} should be allocated using {@link ByteBuffer#allocateDirect(int)}. */
	private final boolean direct;
	/** The start of the {@linkplain #buffer circular buffer}. */
	private int start;
	/** The end of the {@linkplain #buffer circular buffer}. Might be equal
	 * to {@link #start} even if the queue is not empty, but in that case {@link #used} is not zero (actually, it will be
	 * equal to the {@linkplain ByteBuffer#capacity() capacity} of {@link #buffer}). */
	private int end;
	/** The number of bytes currently in the {@linkplain #buffer circular buffer}.
	 * Note that this number might be the {@linkplain ByteBuffer#capacity() capacity} of {@link #buffer} when {@link #start} is
	 * equal to {@link #end}. */
	private int used;
	/** The hole in the queue. If it is not -1, there are elements on
	 * disk that should be inserted at this position in the {@linkplain #buffer circular buffer}. */
	private int hole;
	/** The {@linkplain ByteBuffer#capacity() capacity} of the {@linkplain #buffer circular buffer} minus 1. */
	private int mask;
	/** The current read/write {@link FileChannel} open on {@link #file}; it is {@code null} while the queue is
	 * {@linkplain #suspend() suspended} or {@linkplain #close() closed}, but in the first case
	 * {@link #readPosition} and {@link #writePosition} will be nonnegative. */
	private FileChannel channel;
	/** The write position of {@link #channel}, or -1 if this queue has been {@linkplain #close() closed}.
	 * If it is equal to {@link #readPosition}, then {@link #buffer} contain all bytes in this queue. */
	private long writePosition;
	/** The current read position, or -1 if this queue has been closed.
	 * @see #writePosition */
	private long readPosition;
	/** The circular buffer. */
	private ByteBuffer buffer;

	/** Creates a new disk-based byte queue.
	 *
	 * @param file the file that will be used to dump the central part of the queue on disk.
	 * @param bufferSize the {@linkplain ByteBuffer#capacity() capacity} of the circular buffer (will be possibly decreased so to be a power of two).
	 * @param direct whether the circular {@link ByteBuffer} used by this queue should be {@linkplain ByteBuffer#allocateDirect(int) allocated directly}.
	 * @param useFileContent whether the queue is new or should use the content of an existing file; in the first case,
	 * we check that the file does not exists or has length zero.
	 */
	@SuppressWarnings("resource")
	protected ByteDiskQueue(final File file, final int bufferSize, final boolean direct, final boolean useFileContent) throws IOException {
		this.file = file;
		this.direct = direct;
		channel = new RandomAccessFile(file, "rw").getChannel();
		if (useFileContent) writePosition = channel.size();
		else channel.truncate(0);
		hole = readPosition == writePosition ? -1 : 0;
		buffer = direct ? ByteBuffer.allocateDirect(Integer.highestOneBit(bufferSize)) : ByteBuffer.allocate(Integer.highestOneBit(bufferSize));
		mask = buffer.capacity() - 1;
	}


	/** Creates a new empty disk-based byte queue.
	 *
	 * @param file the file that will be used to dump the central part of the queue on disk (must not exist
	 * or have length zero).
	 * @param bufferSize the {@linkplain ByteBuffer#capacity() capacity} of the circular buffer (will be possibly decreased so to be a power of two).
	 * @param direct whether the circular {@link ByteBuffer} used by this queue should be {@linkplain ByteBuffer#allocateDirect(int) allocated directly}.
	 */
	public static ByteDiskQueue createNew(final File file, final int bufferSize, final boolean direct) throws IOException {
		if (file.length() != 0) throw new IOException("File " + file + " is nonempty");
		return new ByteDiskQueue(file,  bufferSize, direct,  false);
	}

	/** Creates a new disk-based byte queue using the content of an existing file.
	 * The stream of bytes contained in the provided file will form the initial content of the queue.
	 *
	 * @param file the file that will be used to dump the central part of the queue on disk (must exist).
	 * @param bufferSize the {@linkplain ByteBuffer#capacity() capacity} of the circular buffer (will be possibly decreased so to be a power of two).
	 * @param direct whether the circular {@link ByteBuffer} used by this queue should be {@linkplain ByteBuffer#allocateDirect(int) allocated directly}.
	 */
	public static ByteDiskQueue createFromFile(final File file, final int bufferSize, final boolean direct) throws IOException {
		if (! file.exists()) throw new IOException("File " + file + " does not exist");
		return new ByteDiskQueue(file,  bufferSize, direct, true);
	}

	/** Throws an {@link IllegalStateException} if the queue has been {@linkplain #close() closed}. */
	private void ensureNotClosed() {
		if (readPosition == -1) throw new IllegalStateException();
	}

	/** Enqueues a byte to this queue.
	 *
	 * @param b the byte to be enqueued.
	 */
	public void enqueue(final byte b) throws IOException {
		if (DEBUG) System.err.println("[start = " + start + ", end = " + end + ", hole = " + hole + "] enqueue(" + b + ")");
		ensureNotClosed();
		if (used == buffer.capacity()) dumpTail();
		buffer.put(end++, b);
		end &= mask;
		used++;
	}

	/** Adds a vByte-coded natural number to this queue.
	 *
	 * @param x the natural number to be added to the queue.
	 */
	public void enqueueInt(final int x) throws IOException {
		assert x >= 0 : x;
		if (DEBUG) System.err.println("[start = " + start + ", end = " + end + ", hole = " + hole + "] enqueueInt(" + x + ")");
		switch(Fast.mostSignificantBit(x) / 7 + 1) {
		case 1:
			enqueue((byte)x);
			break;
		case 2:
			enqueue((byte)(x >>> 7 | 0x80));
			enqueue((byte)(x & 0x7F));
			break;
		case 3:
			enqueue((byte)(x >>> 14 | 0x80));
			enqueue((byte)(x >>> 7 | 0x80));
			enqueue((byte)(x & 0x7F));
			break;
		case 4:
			enqueue((byte)(x >>> 21 | 0x80));
			enqueue((byte)(x >>> 14 | 0x80));
			enqueue((byte)(x >>> 7 | 0x80));
			enqueue((byte)(x & 0x7F));
			break;
		case 5:
			enqueue((byte)(x >>> 28 | 0x80));
			enqueue((byte)(x >>> 21 | 0x80));
			enqueue((byte)(x >>> 14 | 0x80));
			enqueue((byte)(x >>> 7 | 0x80));
			enqueue((byte)(x & 0x7F));
		}
	}

	/** Enqueues a fragment of byte array to this queue.
	 *
	 * @param a a byte array.
	 * @param offset the first byte in {@code a} to be enqueued.
	 * @param length the number of bytes to enqueue.
	 */
	public void enqueue(final byte[] a, int offset, int length) throws IOException {
		if (DEBUG) System.err.println("[start = " + start + ", end = " + end + ", hole = " + hole + "] enqueue(" + offset + ", " + length + ")");
		ensureNotClosed();
		do {
			if (used == buffer.capacity()) dumpTail();
			assert used != buffer.capacity() : used + " == " + buffer.capacity();
			final int toBePut = Math.min(length, buffer.capacity() - used);

			buffer.clear();
			buffer.position(end);

			if (end < start) buffer.put(a, offset, toBePut);
			else {
				final int firstTransfer = Math.min(toBePut, buffer.capacity() - end);
				buffer.put(a, offset, firstTransfer);
				if (firstTransfer < toBePut) {
					buffer.clear();
					buffer.put(a, offset + firstTransfer, toBePut - firstTransfer);
				}
			}

			offset += toBePut;
			length -= toBePut;
			used += toBePut;
			end = end + toBePut & mask;
		} while(length != 0);

		buffer.clear();
	}

	/** Enqueues a byte array to this queue.
	 *
	 * @param a the array whose bytes have to be enqueued.
	 */
	public void enqueue(final byte[] a) throws IOException {
		enqueue(a, 0, a.length);
	}

	private void dumpTail() throws IOException {
		resume();
		if (DEBUG) System.err.println("Dumping at " + writePosition + ", start = " + start+  ", end = " + end + ", hole = " + hole);
		channel.position(writePosition);
		if (hole == -1) hole = (start + buffer.capacity() / 2) & mask;

		buffer.clear();
		buffer.position(hole);

		if (hole < end) {
			buffer.limit(end);
			channel.write(buffer);
			used -= end - hole;
		}
		else {
			channel.write(buffer);
			used -= buffer.capacity() - hole;
			buffer.position(0);
			buffer.limit(end);
			channel.write(buffer);
			used -= end;
		}

		buffer.clear();

		writePosition = channel.position();
		end = hole;
		if (DEBUG) System.err.println("start = " + start+  ", end = " + end + ", hole = " + hole + ", readPosition = " + readPosition + ", writePosition = " + writePosition);
	}

	private void loadHead() throws IOException {
		assert readPosition != writePosition : readPosition + " == " + writePosition;
		// There are items on disk. We must load them.
		if (DEBUG) System.err.println("Reading at " + readPosition + "...");
		// If there is not enough space, we dump the tail. It is large, anyway.
		if (used > buffer.capacity() / 2) dumpTail();
		resume();

		assert size64() > used : size64() + " <= " + used;
		final int toRead = (int)Math.min(writePosition - readPosition, buffer.capacity() / 2);
		start = hole - toRead & mask;

		channel.position(readPosition);

		buffer.clear();
		buffer.position(start);

		if (start < hole) {
			buffer.limit(hole);
			channel.read(buffer);
		}
		else {
			channel.read(buffer);
			buffer.position(0);
			buffer.limit(hole);
			channel.read(buffer);
		}

		buffer.clear();

		used += toRead;
		readPosition = channel.position();
		if (readPosition == writePosition) {
			readPosition = writePosition = 0;
			hole = -1;
		}
		assert start != hole : start + " == " + hole;
		if (DEBUG) System.err.println("Now at " + readPosition);
	}

	/** Dequeues a byte from the queue.
	 *
	 * @return the first available byte in this queue.
	 * @throws NoSuchElementException if there are no bytes in this queue.
	 */
	public byte dequeue() throws IOException {
		ensureNotClosed();
		if (DEBUG) System.err.print("[start = " + start + ", end = " + end + ", hole = " + hole + "] dequeue() = ");

		if (isEmpty()) throw new NoSuchElementException();
		if (start == hole) loadHead();
		final byte result = buffer.get(start++);
		start &= mask;
		used--;

		if (DEBUG) System.err.println(result);
		return result;
	}

	/** Dequeues a sequence of bytes from this queue into an array.
	 *
	 * @param a a byte array.
	 * @param offset the first position in {@code a} where dequeued bytes will be written.
	 * @param length the number of bytes to dequeue.
	 * @throws NoSuchElementException if there are not enough bytes in this queue.
	 */
	public void dequeue(final byte[] a, int offset, int length) throws IOException {
		if (DEBUG) System.err.println("[start = " + start + ", end = " + end + ", hole = " + hole + "] dequeue(" + offset + ", " + length + ")");
		ensureNotClosed();
		while(length != 0) {
			if (isEmpty()) throw new NoSuchElementException();
			else if (start == hole) loadHead();
			assert start != hole : start + " == " + hole;

			final int endOfTransfer = hole == -1 ? end : hole;
			final int toTransfer = Math.min(length, endOfTransfer == start ? used : endOfTransfer - start & mask);

			buffer.clear();
			buffer.position(start);

			if (start < endOfTransfer) buffer.get(a, offset, toTransfer);
			else {
				final int firstTransfer = Math.min(toTransfer, buffer.capacity() - start);
				buffer.get(a, offset, firstTransfer);
				if (firstTransfer < toTransfer) {
					buffer.clear();
					buffer.get(a, offset + firstTransfer, toTransfer - firstTransfer);
				}
			}

			offset += toTransfer;
			length -= toTransfer;
			used -= toTransfer;
			start = start + toTransfer & mask;
		}

		buffer.clear();
	}

	/** Dequeues a sequence of bytes from this queue into an array.
	 *
	 * @param a the array that will contain the dequeued bytes.
	 * @throws NoSuchElementException if there are not enough bytes in this queue.
	 */
	public void dequeue(final byte[] a) throws IOException {
		dequeue(a, 0, a.length);
	}


	/** Dequeues from this queue a vByte-coded natural number.
	 *
	 * @return the first available natural number in this queue.
	 */
	public int dequeueInt() throws IOException {
		if (DEBUG) System.err.println("[start = " + start + ", end = " + end + ", hole = " + hole + "] dequeueInt()");
		for(int x = 0; ;) {
			final byte b = dequeue();
			x |= b & 0x7F;
			if (b >= 0) return x;
			x <<= 7;
		}
	}


	@Deprecated
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long size64() {
		return writePosition - readPosition + used;
	}

	public boolean isEmpty() {
		return size64() == 0;
	}

	/** Deallocates all disk resources associated with this queue. After calling this method, calls
	 * to any methods that would modify the queue will cause an {@link IllegalStateException}. */
	@Override
	public void close() throws IOException {
		// If the queue is not suspended, we close the file handles.
		if (channel != null) {
			channel.close();
			channel = null;
		}
		writePosition = readPosition = -1;
		used = 0;
		file.delete();
	}


	public void freeze() throws IOException {
		if (DEBUG) System.err.println("[start = " + start + ", end = " + end + ", hole = " + hole + ", readPosition = " + readPosition + ", writePosition = " + writePosition + "] freeze()");
		resume();
		final File freezeFile = File.createTempFile(ByteDiskQueue.class.getSimpleName() + "-", ".freeze", file.getParentFile());
		final FileOutputStream fos = new FileOutputStream(freezeFile);
		final FileChannel freezeChannel = fos.getChannel();

		// Dump head
		buffer.clear();
		buffer.position(start);

		if (hole == -1) {
			if (start < end) {
				buffer.limit(end);
				freezeChannel.write(buffer);
			}
			else if (used != 0) {
				freezeChannel.write(buffer);
				buffer.position(0);
				buffer.limit(end);
				freezeChannel.write(buffer);
			}
		}
		else {
			if (start < hole) {
				buffer.limit(hole);
				freezeChannel.write(buffer);
			}
			else if (start != hole){
				freezeChannel.write(buffer);
				buffer.position(0);
				buffer.limit(hole);
				freezeChannel.write(buffer);
			}

			for(long position = readPosition; position < writePosition;) position += channel.transferTo(position, writePosition - position, freezeChannel);

			// Dump tail
			buffer.clear();
			buffer.position(hole);

			if (hole < end) {
				buffer.limit(end);
				freezeChannel.write(buffer);
			}
			else if (hole != end) {
				freezeChannel.write(buffer);
				buffer.position(0);
				buffer.limit(end);
				freezeChannel.write(buffer);
			}
		}

		assert size64() == freezeChannel.size() : size64() + " != " + freezeChannel.size();

		fos.close();
		channel.close();
		file.delete();
		if (! freezeFile.renameTo(file)) throw new IOException("Cannot rename freeze file " + freezeFile + " to " + file);
		channel = null;
		writePosition = readPosition = -1;
		used = 0;
	}

	/** Clears the queue. Note that we do not modify the dump file (use {@link #trim()} to that purpose). */
	public void clear() {
		ensureNotClosed();
		writePosition = readPosition = start = end = used = 0;
		hole = -1;
	}

	/** Trims the queue dump file at the current write position. */
	public void trim() throws IOException {
		ensureNotClosed();
		resume();
		channel.truncate(writePosition);
	}

	/** Suspends this queue, releasing the associated file handles. If the deque is already
	 * suspended, does nothing. The queue will lazily resume its operations when necessary. */
	public void suspend() throws IOException {
		ensureNotClosed();
		if (channel == null) return;
		channel.close();
		channel = null;
	}

	/** Resumes this queue, restoring the associated file handles. If the queue is already active, does nothing.
	 *
	 * @see #suspend()
	 */
	@SuppressWarnings("resource")
	private void resume() throws IOException {
		ensureNotClosed();
		if (channel != null) return;
		channel = new RandomAccessFile(file, "rw").getChannel();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void finalize() throws Throwable {
		try {
			if (channel != null || writePosition != -1) {
				System.err.println("WARNING: This " + this.getClass().getName() + " [" + toString() + "] should have been closed.");
				close();
			}
		}
		finally {
			super.finalize();
		}
	}

	/** Enlarge the size of the buffer of this queue to a given size.
	 *
	 * @param newBufferSize the required buffer size  (will be possibly decreased so to be a power of two).
	 * If the new buffer size is smaller than the current size, nothing happens.
	 */
	public void enlargeBuffer(int newBufferSize) {
		if (DEBUG) System.err.print("Enlarging to size " + newBufferSize + " [start = " + start+  ", end = " + end + ", hole = " + hole + "]... ");
		newBufferSize = Integer.highestOneBit(newBufferSize);
		if (newBufferSize <= buffer.capacity()) return;
		final ByteBuffer newByteBuffer = direct ? ByteBuffer.allocateDirect(newBufferSize) : ByteBuffer.allocate(newBufferSize);

		buffer.clear();

		if (start < end) {
			buffer.position(start);
			buffer.limit(end);
			newByteBuffer.put(buffer);
			end -= start;
			if (hole >= 0) hole -= start;
		}
		else if (used != 0) {
			buffer.position(start);
			newByteBuffer.put(buffer);
			buffer.position(0);
			buffer.limit(end);
			newByteBuffer.put(buffer);

			end += buffer.capacity() - start;
			if (hole >= 0)
				if (hole >= start) hole -= start;
				else hole += buffer.capacity() - start;
		}
		else { // This covers the case start == end, used == 0
			end = 0;
			if (hole > 0) hole = 0;
		}

		start = 0;
		buffer = newByteBuffer;
		mask = newBufferSize - 1;
		if (DEBUG) System.err.println("[start = " + start+  ", end = " + end + ", hole = " + hole + "]");
	}
}
