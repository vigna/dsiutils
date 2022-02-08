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

package it.unimi.dsi.bits;

import static it.unimi.dsi.bits.LongArrayBitVector.bit;
import static it.unimi.dsi.bits.LongArrayBitVector.word;
import static it.unimi.dsi.bits.LongArrayBitVector.words;

import java.io.Serializable;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.booleans.AbstractBooleanBigList;
import it.unimi.dsi.fastutil.longs.AbstractLongBigList;
import it.unimi.dsi.fastutil.longs.AbstractLongSortedSet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

/** An abstract implementation of a {@link BitVector}.
 *
 * <P>This abstract implementation provides almost all methods: you have to provide just
 * {@link it.unimi.dsi.fastutil.booleans.BooleanList#getBoolean(int)} and
 * {@link BitVector#length()}. No attributes are defined.
 *
 * <P>Note that the integer-set view provided by {@link #asLongSet()} is not cached: if you
 * want to cache the result of the first call, you must do your own caching.
 *
 * <p><strong>Warning</strong>: this class has several optimised methods
 * that assume that {@link #getLong(long, long)} is implemented efficiently when its
 * arguments are multiples of {@link Long#SIZE} (see, e.g., the implementation
 * of {@link #compareTo(BitVector)} and {@link #longestCommonPrefixLength(BitVector)}).
 * If you want speed up the processing of your own {@link BitVector} implementations,
 * just implement {@link #getLong(long, long)} so that it is fast under the above conditions.
 */
public abstract class AbstractBitVector extends AbstractBooleanBigList implements BitVector {

	@Override
	protected void ensureRestrictedIndex(final long index) {
		if (index < 0)  throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index >= length()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to length (" + (length()) + ")");
	}

	@Override
	protected void ensureIndex(final long index) {
		if (index < 0)  throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index > length()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than length (" + (length()) + ")");
	}

	/** @implSpec This implementation delegates to {@link #set(long, boolean)}. */
	public void set(final int index) { set(index, true); }

	/** @implSpec This implementation delegates to {@link #set(long, boolean)}. */
	public void clear(final int index) { set(index, false); }

	/** @implSpec This implementation delegates to {@link #flip(long)}. */
	public void flip(final int index) { set(index, ! getBoolean(index)); }

	@Override
	public void set(final long index) { set(index, true); }
	@Override
	public void clear(final long index) { set(index, false); }
	@Override
	public void flip(final long index) { set(index, ! getBoolean(index)); }

	@Override
	public void fill(final boolean value) { for(long i = length(); i-- != 0;) set(i, value); }
	@Override
	public void fill(final int value) { fill(value != 0); }
	@Override
	public void flip() { for(long i = length(); i-- != 0;) flip(i); }

	@Override
	public void fill(final long from, final long to, final boolean value) { BitVectors.ensureFromTo(length(), from, to); for(long i = to; i-- != from;) set(i, value); }
	@Override
	public void fill(final long from, final long to, final int value) { fill(from, to, value != 0); }
	@Override
	public void flip(final long from, final long to) { BitVectors.ensureFromTo(length(), from, to); for(long i = to; i-- != from;) flip(i); }

	@Override
	public int getInt(final long index) { return getBoolean(index) ? 1 : 0; }
	@Override
	public long getLong(final long from, final long to) {
		if (to - from > 64) throw new IllegalArgumentException("Range too large for a long: [" + from + ".." + to + ")");
		long result = 0;
		for(long i = from; i < to; i++) if (getBoolean(i)) result |= 1L << i - from;
		return result;
	}

	/** @implSpec This implementation delegates to {@link #getBoolean(long)}. */
	public boolean getBoolean(final int index) { return getBoolean((long)index); }

	/** @implSpec This implementation delegates to {@link #removeBoolean(long)}. */
	public boolean removeBoolean(final int index) { return removeBoolean((long)index); }

	/** @implSpec This implementation delegates to {@link #set(long, boolean)}. */
	public boolean set(final int index, final boolean value) { return set((long)index, value); }

	/** @implSpec This implementation delegates to {@link #add(long, boolean)}. */
	public void add(final int index, final boolean value) { add((long)index, value); }

	@Override
	public boolean removeBoolean(final long index) { throw new UnsupportedOperationException(); }
	@Override
	public boolean set(final long index, final boolean value) { throw new UnsupportedOperationException(); }
	@Override
	public void add(final long index, final boolean value) { throw new UnsupportedOperationException(); }

	@Override
	public void set(final long index, final int value) { set(index, value != 0); }
	@Override
	public void add(final long index, final int value) { add(index, value != 0); }
	@Override
	public boolean add(final boolean value) { add(length(), value); return true; }
	@Override
	public void add(final int value) { add(value != 0); }

	@Override
	public BitVector append(final long value, final int k) {
		for(int i = 0; i < k; i++) add((value & 1L << i) != 0);
		return this;
	}

	@Override
	public BitVector append(final BitVector bv) {
		final long length = bv.length();
		final long l = length & -Long.SIZE;

		long i;
		for(i = 0; i < l; i += Long.SIZE) append(bv.getLong(i, i + Long.SIZE), Long.SIZE);
		if (i < length) append(bv.getLong(i, length), (int)(length - i));
		return this;
	}

	@Override
	public BitVector copy() { return copy(0, length()); }

	@Override
	public BitVector copy(final long from, final long to) {
		BitVectors.ensureFromTo(length(), from, to);
		final long length = to - from;
		final long l = length & -Long.SIZE;
		final long bits[] = new long[words(length)];
		long i;
		for (i = 0; i < l; i += Long.SIZE) bits[word(i)] = getLong(from + i, from + i + Long.SIZE);
		if (i < length) bits[word(i)] = getLong(from + i, to);
		return LongArrayBitVector.wrap(bits, length);
	}

	/** Returns an instance of {@link LongArrayBitVector} containing a copy of this bit vector.
	 *
	 * @return an instance of {@link LongArrayBitVector} containing a copy of this bit vector.
	 */

	@Override
	public BitVector fast() {
		return copy();
	}

	@Override
	public long count() {
		long c = 0;
		for(long i = length(); i-- != 0;) c += getInt(i);
		return c;
	}

	@Override
	public long firstOne() {
		return nextOne(0);
	}

	@Override
	public long lastOne() {
		return previousOne(length());
	}

	@Override
	public long firstZero() {
		return nextZero(0);
	}

	@Override
	public long lastZero() {
		return previousZero(length());
	}

	@Override
	public long nextOne(final long index) {
		final long length = length();
		for(long i = index; i < length; i++) if (getBoolean(i)) return i;
		return -1;
	}

	@Override
	public long previousOne(final long index) {
		for (long i = index; i-- != 0;) if (getBoolean(i)) return i;
		return -1;
	}

	@Override
	public long nextZero(final long index) {
		final long length = length();
		for(long i = index; i < length; i++) if (! getBoolean(i)) return i;
		return -1;
	}

	@Override
	public long previousZero(final long index) {
		for (long i = index; i-- != 0;) if (! getBoolean(i)) return i;
		return -1;
	}

	@Override
	public long longestCommonPrefixLength(final BitVector v) {
		final long minLength = Math.min(length(), v.length());
		final long l = minLength & -Long.SIZE;
		long w0, w1;

		long i;
		for(i = 0; i < l; i += Long.SIZE) {
			w0 = getLong(i, i + Long.SIZE);
			w1 = v.getLong(i, i + Long.SIZE);
			if (w0 != w1) return i + Long.numberOfTrailingZeros(w0 ^ w1);
		}

		w0 = getLong(i, minLength);
		w1 = v.getLong(i, minLength);

		if (w0 != w1) return i + Long.numberOfTrailingZeros(w0 ^ w1);
		return minLength;
	}

	@Override
	public boolean isPrefix(final BitVector v) {
		return longestCommonPrefixLength(v) == length();
	}

	@Override
	public boolean isProperPrefix(final BitVector v) {
		return length() < v.length() && isPrefix(v);
	}

	@Override
	public BitVector and(final BitVector v) {
		for(long i = Math.min(length(), v.length()); i-- != 0;) if (! v.getBoolean(i)) clear(i);
		return this;
	}

	@Override
	public BitVector or(final BitVector v) {
		for(long i = Math.min(length(), v.length()); i-- != 0;) if (v.getBoolean(i)) set(i);
		return this;
	}

	@Override
	public BitVector xor(final BitVector v) {
		for(long i = Math.min(length(), v.length()); i-- != 0;) if (v.getBoolean(i)) flip(i);
		return this;
	}

	@Deprecated
	@Override
	public int size() {
		final long length = length();
		if (length > Integer.MAX_VALUE) throw new IllegalStateException("The number of bits of this bit vector (" + length + ") exceeds Integer.MAX_INT");
		return (int)length;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation just returns {@link #length()}.
	 */
	@Override
	public long size64() {
		return length();
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		length(0);
	}

	/** {@inheritDoc} */
	@Override
	public BitVector replace(final BitVector bv) {
		clear();
		final long fullBits = bv.length() & -Long.SIZE;
		for(long i = 0; i < fullBits; i += Long.SIZE) append(bv.getLong(i, i + Long.SIZE), Long.SIZE);
		if (!LongArrayBitVector.round(bv.length())) append(bv.getLong(fullBits, bv.length()), (int)(bv.length() - fullBits));
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (! (o instanceof BitVector)) return false;
		final BitVector v = (BitVector)o;
		final long length = length();
		if (length != v.length()) return false;
		final long fullLength = length & -Long.SIZE;
		for(long i = 0; i < fullLength; i += Long.SIZE) if (getLong(i, i + Long.SIZE) != v.getLong(i, i + Long.SIZE)) return false;
		return getLong(fullLength, length) == v.getLong(fullLength, length);
	}

	@Override
	public boolean equals(final BitVector v, final long start, final long end) {
		long startFull = start & -Long.SIZE;
		final long endFull = end & -Long.SIZE;
		final int startBit = bit(start);
		final int endBit = bit(end);

		if (startFull == endFull)
			return ((getLong(startFull, Math.min(length(), startFull + Long.SIZE))
					^ v.getLong(startFull, Math.min(v.length(), startFull + Long.SIZE)))
					& ((1L << (endBit - startBit)) - 1) << startBit) == 0;

		if (((getLong(startFull, startFull + Long.SIZE) ^ v.getLong(startFull, startFull += Long.SIZE)) & (-1L << startBit)) != 0) return false;

		while(startFull < endFull) if (getLong(startFull, startFull + Long.SIZE) != v.getLong(startFull, startFull += Long.SIZE)) return false;

		return ((getLong(startFull, Math.min(length(), startFull + Long.SIZE))
				^ v.getLong(startFull, Math.min(v.length(), startFull + Long.SIZE)))
				& (1L << endBit) - 1) == 0;
	}



	@Override
	public int hashCode() {
		final long length = length();
		final long fullLength = length & -Long.SIZE;
		long h = 0x9e3779b97f4a7c13L ^ length;

		for(long i = 0; i < fullLength; i += Long.SIZE) h ^= (h << 5) + getLong(i, i + Long.SIZE) + (h >>> 2);
		if (length != fullLength) h ^= (h << 5) + getLong(fullLength, length) + (h >>> 2);

		return (int)((h >>> 32) ^ h);
	}

	@Override
	public long[] bits() {
		final long[] bits = new long[words(length())];
		final long length = length();
		for (long i = 0; i < length; i++) if (getBoolean(i)) bits[word(i)] |= 1L << i;
		return bits;
	}

	/**
	 * A view of a bit vector as a sorted set of long integers.
	 *
	 * <p>
	 * This class implements a view of a subvector as a sorted set of long integers using the subvector
	 * as a bit mask. If the end position is {@link Long#MAX_VALUE}, the vector is enlarged as needed
	 * (i.e., when a one beyond the current size is set), but it is never shrunk.
	 *
	 * <p>
	 * Outside of the specified range {{@link #contains(long)} will return false and {@link #add(long)}
	 * will cause an {@link UnsupportedOperationException}.
	 */

	public static class LongSetView extends AbstractLongSortedSet implements LongSet, Serializable, Size64 {
		private static final long serialVersionUID = 1L;
		/** The underlying bit vector. */
		protected final BitVector bitVector;
		/** The starting position (included). */
		private final long from;
		/**
		 * The ending position (not included); use {@link Long#MAX_VALUE} to get an extendable bit vector.
		 */
		private final long to;

		/**
		 * Creates a new view on a subvector.
		 *
		 * @param bitVector the underlying bit vector.
		 * @param from the first index (inclusive).
		 * @param to the last index (not inclusive).
		 */
		public LongSetView(final BitVector bitVector, final long from, final long to) {
			if (from > to) throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
			this.bitVector = bitVector;
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean contains(final long index) {
			if (index < 0) throw new IllegalArgumentException("The provided index (" + index + ") is negative");
			if (index < from || index >= to) return false;
			return index < bitVector.length() && bitVector.getBoolean(index);
		}

		@Override
		public boolean add(final long index) {
			if (index < 0) throw new IllegalArgumentException("The provided index (" + index + ") is negative");
			if (index < from || index >= to) throw new UnsupportedOperationException();

			final long length = bitVector.length();
			if (index >= length) bitVector.length(index + 1);
			final boolean oldValue = bitVector.getBoolean(index);
			bitVector.set(index);
			return ! oldValue;
		}

		@Override
		public boolean remove(final long index) {
			final long length = bitVector.length();
			if (index >= length) return false;
			final boolean oldValue = bitVector.getBoolean(index);
			bitVector.clear(index);
			return oldValue;
		}

		@Override
		public void clear() {
			bitVector.clear();
		}

		@Override
		public long size64() {
			return bitVector.count();
		}

		@Override
		@Deprecated
		public int size() {
			// This minimisation is necessary for implementations not supporting long indices.
			final long size = bitVector.subVector(from, Math.min(to, bitVector.length())).count();
			if (size > Integer.MAX_VALUE) throw new IllegalStateException("Set is too large to return an integer size");
			return (int)size;
		}

		@Override
		public LongBidirectionalIterator iterator() {
			return iterator(0);
		}

		private final class LongSetViewIterator implements LongBidirectionalIterator {
			long pos, last = -1, nextPos = -1, prevPos = -1;

			private LongSetViewIterator(final long from) {
				pos = from;
			}

			@Override
			public boolean hasNext() {
				if (nextPos == -1 && pos < bitVector.length()) nextPos = bitVector.nextOne(pos);
				return nextPos != -1;
			}

			@Override
			public boolean hasPrevious() {
				if (prevPos == -1 && pos > 0) prevPos = bitVector.previousOne(pos);
				return prevPos != -1;
			}

			@Override
			public long nextLong() {
				if (! hasNext()) throw new NoSuchElementException();
				last = nextPos;
				pos = nextPos + 1;
				nextPos = -1;
				return last;
			}

			@Override
			public long previousLong() {
				if (! hasPrevious()) throw new NoSuchElementException();
				pos = prevPos;
				prevPos = -1;
				return last = pos;
			}

			@Override
			public void remove() {
				if (last == -1) throw new IllegalStateException();
				bitVector.clear(last);
			}
		}

		@Override
		public LongBidirectionalIterator iterator(final long from) {
			return new LongSetViewIterator(from);
		}

		@Override
		public long firstLong() {
			return bitVector.nextOne(from);
		}

		@Override
		public long lastLong() {
			return bitVector.previousOne(Math.min(bitVector.length(), to));
		}

		@Override
		public LongComparator comparator() {
			return null;
		}

		@Override
		public LongSortedSet headSet(final long to) {
			return to < this.to ? new LongSetView(bitVector, from, to) : this;
		}

		@Override
		public LongSortedSet tailSet(final long from) {
			return from > this.from ? new LongSetView(bitVector, from, to) : this;
		}

		@Override
		public LongSortedSet subSet(long from, long to) {
			to = to < this.to ? to : this.to;
			from = from > this.from ? from : this.from;
			if (from == this.from && to == this.to) return this;
			return new LongSetView(bitVector, from, to);
		}
	}

	/** A list-of-integers view of a bit vector.
	 *
	 * <P>This class implements in the obvious way a view
	 * of a bit vector as a list of integers of given width. The vector is enlarged as needed (i.e., when
	 * adding new elements), but it is never shrunk.
	 */

	public static class LongBigListView extends AbstractLongBigList implements LongBigList, Serializable {
		private static final long serialVersionUID = 1L;
		/** The underlying bit vector. */
		protected final BitVector bitVector;
		/** The width in bit of an element of this list view. */
		protected final int width;
		/** A bit mask containing {@link #width} bits set to one. */
		protected final long fullMask;

		/**
		 * Returns a list-of-integers view of a bit vector.
		 *
		 * @param bitVector a bit vector.
		 * @param width the bit width of the integers.
		 */
		public LongBigListView(final BitVector bitVector, final int width) {
			this.width = width;
			this.bitVector = bitVector;
			fullMask = width == Long.SIZE ? -1 : (1L << width) - 1;
		}

		@Override
		@Deprecated
		public int size() {
			final long length = length();
			if (length > Integer.MAX_VALUE) throw new IllegalStateException("The number of elements of this bit list (" + length + ") exceeds Integer.MAX_INT");
			return (int)length;
		}

		@Override
		public long size64() {
			return width == 0 ? 0 : bitVector.length() / width;
		}

		/**
		 * @return {@link #size64()}
		 * @deprecated Please use {@link #size64()}.
		 */
		@Deprecated
		public long length() {
			return size64();
		}

		/**
		 * Resizes this list, enlarging the underying bit vector as necessary.
		 *
		 * @param newSize the new size.
		 */
		@Override
		public void size(final long newSize) {
			bitVector.length(newSize * width);
		}

		/** @deprecated Please use {@link #size(long)}. */
		@Deprecated
		public LongBigList length(final long newSize) {
			size(newSize);
			return this;
		}

		// TODO: implement set()/remove()
		private final class LongBigListIteratorView implements LongBigListIterator {
			private long pos = 0;
			@Override
			public boolean hasNext() { return pos < length(); }
			@Override
			public boolean hasPrevious() { return pos > 0; }

			@Override
			public long nextLong() {
				if (! hasNext()) throw new NoSuchElementException();
				return getLong(pos++);
			}

			@Override
			public long previousLong() {
				if (! hasPrevious()) throw new NoSuchElementException();
				return getLong(--pos);
			}

			@Override
			public long nextIndex() {
				return pos;
			}

			@Override
			public long previousIndex() {
				return pos - 1;
			}
		}

		@Override
		public it.unimi.dsi.fastutil.longs.LongBigListIterator listIterator() {
			return new LongBigListIteratorView();
		}

		/** @implSpec This implementation delegates to {@link #add(long, long)}. */
		public void add(final int index, final long value) {
			add((long)index, value);
		}

		@Override
		public void add(final long index, final long value) {
			if (width != Long.SIZE && value > fullMask) throw new IllegalArgumentException();
			for(int i = 0; i < width; i++) bitVector.add((value & 1L << i) != 0);
		}

		@Override
		public long getLong(final long index) {
			final long start = index * width;
			return bitVector.getLong(start, start + width);
		}

		/** @implSpec This implementation delegates to {@link #getLong(long)}. */
		public long getLong(final int index) {
			return getLong((long)index);
		}

		// TODO
		@Override
		public long removeLong(final long index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long set(final long index, final long value) {
			if (width != Long.SIZE && value > fullMask) throw new IllegalArgumentException();
			final long oldValue = getLong(index);
			final long start = index * width;
			for(int i = width; i-- != 0;) bitVector.set(i + start, (value & 1L << i) != 0);
			return oldValue;
		}

		@Override
		public LongBigList subList(final long from, final long to) {
			return bitVector.subVector(from * width, to * width).asLongBigList(width);
		}
	}

	@Override
	public BitVector length(final long newLength) {
		final long length = length();
		if (length < newLength) for(long i = newLength - length; i-- != 0;) add(false);
		else for(long i = length; i-- != newLength;) removeBoolean(i);
		return this;
	}

	@Override
	public void size(final long newSize) {
		length(newSize);
	}

	@Override
	public LongSortedSet asLongSet() {
		return new LongSetView(this, 0, Long.MAX_VALUE);
	}

	@Override
	public LongBigList asLongBigList(final int width) {
		return new LongBigListView(this, width);
	}

	@Override
	public BitVector subVector(final long from, final long to) {
		return new SubBitVector(this, from, to);
	}

	@Override
	public BitVector subVector(final long from) {
		return subVector(from, length());
	}

	@Override
	public int compareTo(final BigList<? extends Boolean> list) {
		if (list instanceof BitVector) return compareTo((BitVector)list);
		return super.compareTo(list);
	}

	/**
	 * Compares lexicographically this bit vector with another bit vector.
	 *
	 * @implNote This method compares the two bit vectors by extracting a 64-bit word at a time using
	 *           {@link BitVector#getLong(long, long)}.
	 *
	 * @param v another bit vector.
	 * @return -1, 0, or 1, depending on the lexicographical order of the two vectors.
	 */
	public int compareTo(final BitVector v) {
		final long minLength = Math.min(length(), v.length());
		final long l = minLength & -Long.SIZE;
		long w0, w1, xor;

		long i;
		for(i = 0; i < l; i += Long.SIZE) {

			w0 = getLong(i, i + Long.SIZE);
			w1 = v.getLong(i, i + Long.SIZE);
			xor = w0 ^ w1;
			if (xor != 0) return (xor & -xor & w0) == 0 ? -1 : 1;
		}

		w0 = getLong(i, minLength);
		w1 = v.getLong(i, minLength);
		xor = w0 ^ w1;
		if (xor != 0) return (xor & -xor & w0) == 0 ? -1 : 1;

		return Long.signum(length() - v.length());
	}


	/** Returns a string representation of this vector.
	 *
	 * <P>Note that this string representation shows the bit of index 0 at the leftmost position.
	 * @return a string representation of this vector, with the bit of index 0 on the left.
	 */

	@Override
	public String toString() {
		final StringBuffer s = new StringBuffer();
		final long size = size64();
		for(long i = 0; i < size; i++) s.append(getInt(i));
		return s.toString();
	}

	/** A subvector of a given bit vector, specified by an initial and a final bit. */

	public static class SubBitVector extends AbstractBitVector implements BitVector {
		/** The underlying bit vector. */
		protected final BitVector bitVector;
		/** The initial bit (inclusive). */
		protected final long from;
		/** The final bit (not inclusive). */
		protected long to;

		public SubBitVector(final BitVector l, final long from, final long to) {
			BitVectors.ensureFromTo(l.length(), from, to);
			this.from = from;
			this.to = to;
			bitVector = l;
		}

		@Override
		public boolean getBoolean(final long index) {
			ensureIndex(index);
			return bitVector.getBoolean(from + index);
		}

		@Override
		public int getInt(final long index) { return getBoolean(index) ? 1 : 0; }

		@Override
		public boolean set(final long index, final boolean value) {
			ensureIndex(index);
			return bitVector.set(from + index, value);
		}

		@Override
		public void set(final long index, final int value) { set(index, value != 0); }

		@Override
		public void add(final long index, final boolean value) {
			ensureIndex(index);
			bitVector.add(from + index, value); to++;
		}

		@Override
		public void add(final long index, final int value) { add(index, value != 0); to++; }
		@Override
		public void add(final int value) { bitVector.add(to++, value); }

		@Override
		public boolean removeBoolean(final long index) {
			ensureIndex(index);
			to--;
			return bitVector.removeBoolean(from + index);
		}

		@Override
		public BitVector copy(final long from, final long to) {
			BitVectors.ensureFromTo(length(), from, to);
			return bitVector.copy(this.from + from, this.from + to);
		}

		@Override
		public BitVector subVector(final long from, final long to) {
			BitVectors.ensureFromTo(length(), from, to);
			return new SubBitVector(bitVector, this.from + from, this.from + to);
		}

		@Override
		public long getLong(final long from, final long to) {
			BitVectors.ensureFromTo(length(), from, to);
			return bitVector.getLong(from + this.from, to + this.from);
		}

		@Override
		public LongSortedSet asLongSet() {
			return new LongSetView(this, 0, length());
		}

		@Override
		public long length() {
			return to - from;
		}

		@Override
		public long size64() {
			return length();
		}
	}
}
