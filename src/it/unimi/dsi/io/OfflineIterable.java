/*
 * DSI utilities
 *
 * Copyright (C) 2005-2023 Sebastiano Vigna
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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

/** An iterable that offers elements that were previously stored offline using specialized
 *  serialization methods. At construction, you provide a {@linkplain #OfflineIterable(it.unimi.dsi.io.OfflineIterable.Serializer, Object) serializer}
 *  that establishes how elements are written offline; after that, you can
 *  {@linkplain #add(Object) add elements} one at a time or in a {@linkplain #addAll(Iterable) bulk way}.
 *  At any moment, you can {@linkplain #iterator() get} an {@link OfflineIterable.OfflineIterator OfflineIterator}
 *  on this object that returns all the elements added so far. Note that the returned iterator caches the current number of elements,
 *  so each iterator will return just the elements added at the time of its creation.
 *
 *  <p><strong>Warning</strong>: The store object provided at {@linkplain OfflineIterable#OfflineIterable(it.unimi.dsi.io.OfflineIterable.Serializer, Object)
 *  construction time} is shared by all iterators.
 *
 *  <h2>Closing</h2>
 *
 *  <p>Both {@link OfflineIterable} and {@link OfflineIterable.OfflineIterator OfflineIterator} are {@link SafelyCloseable} (the latter will
 *  close its input stream when <code>hasNext()</code> returns false), but for better resource management you should close them after usage.
 *
 * @author Sebastiano Vigna
 * @since 0.9.2
 */
public class OfflineIterable<T,U extends T> implements Iterable<U>, SafelyCloseable, Size64 {
    public static final long serialVersionUID = 1L;

    /** An iterator returned by an {@link OfflineIterable}. */
	public static final class OfflineIterator<A, B extends A> implements ObjectIterator<B>, SafelyCloseable {
		/** The data input stream that accesses the file of the related {@link OfflineIterable}. */
		private final DataInputStream dis;
		/** The number of elements in the related {@link OfflineIterable}. */
		private final long size;
		/** The serializer used to store and read the elements of this iterable. */
		private final Serializer<? super A, B> serializer;
		/** An object that is (re)used by the iterator(s) iterating on this iterable. */
		private final B store;
		/** The number of elements read by this iterator. */
		private long read;
		/** Whether this iterator has been closed. */
		private boolean closed = false;

		private OfflineIterator(final DataInputStream dis, final Serializer<? super A, B> serializer, final B store, final long size) {
			this.dis = dis;
			this.serializer = serializer;
			this.store = store;
			this.size = size;
		}

		@Override
		public boolean hasNext() {
			if (read >= size) close();
			return read < size;
		}

		@Override
		public B next() {
			if (!hasNext()) throw new NoSuchElementException();
			try {
				serializer.read(dis, store);
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
			read++;
			return store;
		}

		@Override
		public void close() {
			if (!closed) {
				try {
					dis.close();
				}
				catch (final IOException e) {
					throw new RuntimeException(e);
				}
				closed = true;
			}
		}

		@Override
		protected void finalize() throws Throwable {
			try {
				if (! closed) {
					LoggerFactory.getLogger(this.getClass()).warn("This " + this.getClass().getName() + " [" + toString() + "] should have been closed.");
					close();
				}
			}
			finally {
				super.finalize();
			}
		}
	}

	/** Determines a strategy to serialize and deserialize elements.
	 */
	public interface Serializer<A, B extends A> {
		/** Writes out an element.
		 *
		 * @param x the element to be written.
		 * @param dos the stream where the element should be written.
		 * @throws IOException if an exception occurs while writing.
		 */
		public void write(A x, DataOutput dos) throws IOException;

		/** Reads an element.
		 *
		 * @param dis the stream whence the element should be read.
		 * @param x the object where the element will be read.
		 * @throws IOException if an exception occurs while reading.
		 */
		public void read(DataInput dis, B x) throws IOException;
	}

	/** The serializer used to store and read the elements of this iterable. */
	private final Serializer<? super T, U> serializer;
	/** The file where elements are serialized. */
	private final File file;
	/** A data output stream associated with {@link #file}. */
	private final DataOutputStream dos;
	/** An object that is (re)used by the iterator(s) iterating on this iterable. */
	private final U store;
	/** The number of elements written so far. */
	private long size;
	/** Whether this iterable has been closed. */
	private boolean closed;
	/** The fast buffered output stream associated with {@link #dos}. */
	private final FastBufferedOutputStream fbos;


	/** Creates an offline iterable with given serializer.
	 *
	 * @param serializer the serializer to be used.
	 * @param store an object that is (re)used by the iterator(s) iterating on this iterable.
	 * @throws IOException
	 */
	public OfflineIterable(final Serializer<? super T, U> serializer, final U store) throws IOException {
		this.serializer = serializer;
		this.store = store;
		file = File.createTempFile(OfflineIterable.class.getSimpleName(), "elmts");
		file.deleteOnExit();
		fbos = new FastBufferedOutputStream(new FileOutputStream(file));
		dos = new DataOutputStream(fbos);
	}


	/** Adds a new element at the end of this iterable.
	 *
	 * @param x the element to be added.
	 * @throws IOException
	 */
	public void add(final T x) throws IOException {
		serializer.write(x, dos);
		size++;
	}

	/** Adds all the elements of the given iterable at the end of this iterable.
	 *
	 * @param it the iterable producing the elements to be added.
	 * @throws IOException
	 */
	public void addAll(final Iterable<T> it) throws IOException {
		for (final T x: it) add(x);
	}

	@Override
	public OfflineIterator<T, U> iterator() {
		try {
			dos.flush();
			final DataInputStream dis = new DataInputStream(new FastBufferedInputStream(new FileInputStream(file)));
			return new OfflineIterator<>(dis, serializer, store, size);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void clear() throws IOException {
		if (closed) throw new IOException("This" + this.getClass().getName() + " [" + toString() + "] has been closed.");
 		size = 0;
		dos.flush();
		fbos.position(0);
	}


	@Override
	public void close() {
		if (!closed) {
			try {
				dos.close();
				file.delete();
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
			closed = true;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (! closed) {
				LoggerFactory.getLogger(this.getClass()).warn("This " + this.getClass().getName() + " [" + toString() + "] should have been closed.");
				close();
			}
		}
		finally {
			super.finalize();
		}
	}

	/** Returns the number of elements added so far, unless it is too big to fit in an integer (in which case this method will throw an
	 *  exception).
	 *
	 * @return the number of elements added so far.
	 * @deprecated Use {@link #size64()} instead.
	 */
	@Override
	@Deprecated
	public int size() {
		final long size64 = size64();
		if (size64 > Integer.MAX_VALUE) throw new IllegalStateException("The number of elements of this bit list (" + size64 + ") exceeds Integer.MAX_INT");
		return (int)size64;
	}

	/** Returns the number of elements added so far.
	 *
	 * @return the number of elements added so far.
	 */
	@Override
	public long size64() {
		return size;
	}
}
