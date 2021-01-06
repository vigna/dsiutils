/*
 * DSI utilities
 *
 * Copyright (C) 2020-2021 Sebastiano Vigna
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream.LineTerminator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * A wrapper exhibiting the lines of a file as an {@link Iterable} of byte arrays.
 *
 * <P>
 * An instance of this class makes it possible to access the lines of a file as an {@link Iterable}
 * of byte arrays. Reading is performed using
 * {@link FastBufferedInputStream#readLine(byte[], EnumSet)}, and follows the rules defined therein.
 * No decoding is performed.
 *
 * <P>
 * The result of a call to {@link #iterator()} can be used to scan the file; each call will open an
 * independent input stream. The returned iterator type
 * ({@link it.unimi.dsi.io.FileLinesByteArrayIterable.FileLinesIterator FileLinesIterator}) is
 * {@link java.io.Closeable}, and should be closed after usage. Exhausted iterators, however, will
 * be closed automagically.
 *
 * <p>
 * Using a suitable {@linkplain #FileLinesByteArrayIterable(String, long, Class) constructor} it is
 * possible to specify a decompression class, which must extend {@link InputStream} and provide a
 * constructor accepting an {@link InputStream} (e.g., {@link GZIPInputStream} if the file is
 * compressed in <code>gzip</code> format).
 *
 * <p>
 * Convenience {@linkplain #iterator(InputStream, Class, EnumSet) static methods} makes it possible
 * to build on the fly an iterator over an input stream using the same conventions.
 *
 * <p>
 * This class implements {@link #size64()}, which will return the number of lines of the file,
 * computed with a full scan at the first invocation. However, it is also possible to specify at
 * construction time the number of lines in the file to skip the first scan. It is responsibility of
 * the caller that the specified size and the actual number of lines in the file do match.
 *
 * @author Sebastiano Vigna
 * @since 2.6.17
 */
public class FileLinesByteArrayIterable implements Iterable<byte[]>, Size64 {
	/** The filename upon which this file-lines collection is based. */
	private final String filename;
	/**
	 * A constructor for a stream decompressor for this iterable, or {@code null} for no compression.
	 */
	private final Constructor<? extends InputStream> decompressor;
	/** A set of terminators for the underlying {@link FastBufferedInputStream}. */
	private final EnumSet<LineTerminator> terminators;
	/** The cached size of this iterable. */
	private long size = -1;

	/**
	 * Creates a file-lines byte-array iterable for the specified filename.
	 *
	 * @param filename a filename.
	 */
	public FileLinesByteArrayIterable(final String filename) {
		this(filename, -1);
	}

	/**
	 * Creates a file-lines byte-array iterable for the specified filename and size.
	 *
	 * @param filename a filename.
	 * @param size the number of lines in the file.
	 */
	public FileLinesByteArrayIterable(final String filename, final long size) {
		this(filename, size, FastBufferedInputStream.ALL_TERMINATORS);
	}

	/**
	 * Creates a file-lines byte-array iterable for the specified filename and size using the given line
	 * terminators.
	 *
	 * @param filename a filename.
	 * @param size the number of lines in the file.
	 * @param terminators line terminators for the underlying {@link FastBufferedInputStream}.
	 */
	public FileLinesByteArrayIterable(final String filename, final long size, final EnumSet<LineTerminator> terminators) {
		this.filename = filename;
		this.size = size;
		this.terminators = terminators;
		this.decompressor = null;
	}

	/**
	 * Creates a file-lines byte-array iterable for the specified filename, optionally assuming that the
	 * file is compressed.
	 *
	 * @param filename a filename.
	 * @param decompressor a class extending {@link InputStream} that will be used as a decompressor, or
	 *            {@code null} for no decompression.
	 */
	public FileLinesByteArrayIterable(final String filename, final Class<? extends InputStream> decompressor) throws NoSuchMethodException, SecurityException {
		this(filename, -1, decompressor);
	}

	/**
	 * Creates a file-lines byte-array iterable for the specified filename and size, optionally assuming
	 * that the file is compressed.
	 *
	 * @param filename a filename.
	 * @param size the number of lines in the file.
	 * @param decompressor a class extending {@link InputStream} that will be used as a decompressor, or
	 *            {@code null} for no decompression.
	 */
	public FileLinesByteArrayIterable(final String filename, final long size, final Class<? extends InputStream> decompressor) throws NoSuchMethodException, SecurityException {
		this(filename, size, FastBufferedInputStream.ALL_TERMINATORS, decompressor);
	}

	/**
	 * Creates a file-lines byte-array iterable for the specified filename and size using the given line
	 * terminators and optionally assuming that the file is compressed.
	 *
	 * @param filename a filename.
	 * @param size the number of lines in the file.
	 * @param terminators line terminators for the underlying {@link FastBufferedInputStream}.
	 * @param decompressor a class extending {@link InputStream} that will be used as a decompressor, or
	 *            {@code null} for no decompression.
	 */
	public FileLinesByteArrayIterable(final String filename, final long size, final EnumSet<LineTerminator> terminators, final Class<? extends InputStream> decompressor) throws NoSuchMethodException, SecurityException {
		this.filename = filename;
		this.size = size;
		this.terminators = terminators;
		this.decompressor = decompressor != null ? decompressor.getConstructor(InputStream.class) : null;
	}


	/**
	 * An iterator over the lines of a {@link FileLinesByteArrayIterable}.
	 *
	 * <p>
	 * Instances of this class open an {@link java.io.InputStream}, and thus should be
	 * {@linkplain Closeable#close() closed} after usage. A &ldquo;safety-net&rdquo; finaliser tries to
	 * take care of the cases in which closing an instance is impossible. An exhausted iterator,
	 * however, will be closed automagically.
	 */

	public static final class FileLinesIterator implements Iterator<byte[]>, SafelyCloseable {
		private final EnumSet<LineTerminator> terminators;
		private FastBufferedInputStream fbis;
		private byte[] buffer = new byte[1024];
		private boolean ready;
		private int read;

		private FileLinesIterator(final InputStream ibs, final EnumSet<LineTerminator> terminators) {
			this.fbis = new FastBufferedInputStream(ibs);
			this.terminators = terminators;
		}

		@Override
		public boolean hasNext() {
			if (ready) return true;
			if (fbis == null) return false;
			ready = false;
			try {
				read = 0;
				int len;
				while((len = fbis.readLine(buffer, read, buffer.length - read, terminators)) == buffer.length - read) {
					ready = true;
					read += len;
					buffer = ByteArrays.grow(buffer, buffer.length + 1);
				}
				if (len != -1) {
					ready = true;
					read += len;
				}
				if (len == -1) close();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

			return ready;
		}

		@Override
		public byte[] next() {
			if (! hasNext()) throw new NoSuchElementException();
			ready = false;
			return Arrays.copyOf(buffer, read);
		}

		@Override
		public synchronized void close() {
			if (fbis == null) return;
			try {
				fbis.close();
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				fbis = null;
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		protected synchronized void finalize() throws Throwable {
			try {
				if (fbis != null) close();
			}
			finally {
				super.finalize();
			}
		}
	}

	@Override
	public FileLinesIterator iterator() {
		try {
			final InputStream inputStream = decompressor == null ? new FileInputStream(filename) : decompressor.newInstance(new FileInputStream(filename));
			return new FileLinesIterator(inputStream, terminators);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A convenience method returning a one-off {@link FileLinesIterator} reading from an input stream.
	 *
	 * @param inputStream an input stream.
	 * @return an iterator returning the lines contained in the provided input stream.
	 */
	public static FileLinesIterator iterator(final InputStream inputStream) {
		return iterator(inputStream, null);
	}

	/**
	 * A convenience method returning a one-off {@link FileLinesIterator} reading from an input stream.
	 *
	 * @param inputStream an input stream.
	 * @param decompressor a class extending {@link InputStream} that will be used as a decompressor, or
	 *            {@code null} for no decompression.
	 * @return an iterator returning the lines contained in the provided input stream.
	 */
	public static FileLinesIterator iterator(final InputStream inputStream, final Class<? extends InputStream> decompressor) {
		return iterator(inputStream, decompressor, FastBufferedInputStream.ALL_TERMINATORS);
	}

	/**
	 * A convenience method returning a one-off {@link FileLinesIterator} reading from an input stream.
	 *
	 * @param inputStream an input stream.
	 * @param terminators line terminators for the underlying {@link FastBufferedInputStream}.
	 * @param decompressor a class extending {@link InputStream} that will be used as a decompressor, or
	 *            {@code null} for no decompression.
	 * @return an iterator returning the lines contained in the provided input stream.
	 */
	public static FileLinesIterator iterator(InputStream inputStream, final Class<? extends InputStream> decompressor, final EnumSet<LineTerminator> terminators) {
		try {
			if (decompressor != null) inputStream = decompressor.getConstructor(InputStream.class).newInstance(inputStream);
			return new FileLinesIterator(inputStream, terminators);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized long size64() {
		if (size == -1) size = ObjectIterables.size(this);
		return size;
	}

	/**
	 * Returns all lines as a {@linkplain java.util.List list}.
	 *
	 * @implSpec This method iterates over the lines of the file and accumulates the resulting byte
	 *           arrays in a standard list. Thus, it will throw an exception on files with more than
	 *           {@link Integer#MAX_VALUE} lines.
	 *
	 * @return all lines of the file wrapped by this file-lines byte-array iterable.
	 * @see #allLinesBig()
	 */
	public ObjectList<byte[]> allLines() {
		final ObjectArrayList<byte[]> result = new ObjectArrayList<>();
		for (final byte[] a : this) result.add(a);
		return result;
	}

	/**
	 * Returns all lines as a {@linkplain BigList big list}.
	 *
	 * @implSpec This method iterates over the lines of the file and accumulates the resulting byte
	 *           arrays. in a {@linkplain BigList big list}. Thus, it supports files with more than
	 *           {@link Integer#MAX_VALUE} lines.
	 *
	 * @return all lines of the file wrapped by this file-lines byte-array iterable.
	 * @see #allLines()
	 */
	public ObjectBigArrayBigList<byte[]> allLinesBig() {
		final ObjectBigArrayBigList<byte[]> result = new ObjectBigArrayBigList<>();
		for (final byte[] a : this) result.add(a);
		return result;
	}
}
