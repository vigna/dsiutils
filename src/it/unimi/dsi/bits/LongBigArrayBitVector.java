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

import static it.unimi.dsi.bits.LongArrayBitVector.LOG2_BITS_PER_WORD;
import static it.unimi.dsi.bits.LongArrayBitVector.bit;
import static it.unimi.dsi.bits.LongArrayBitVector.mask;
import static it.unimi.dsi.bits.LongArrayBitVector.round;
import static it.unimi.dsi.fastutil.BigArrays.SEGMENT_SIZE;
import static it.unimi.dsi.fastutil.BigArrays.displacement;
import static it.unimi.dsi.fastutil.BigArrays.grow;
import static it.unimi.dsi.fastutil.BigArrays.segment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.booleans.BooleanBigList;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.fastutil.longs.LongBigList;

/**
 * A bit vector implementation based on a {@linkplain LongBigArrays big arrays of longs}.
 *
 * <P>
 * The main goal of this class is to be able to accommodate very large bit vectors. With respect to
 * {@link LongArrayBitVector}, many optimized methods are missing and rely on the generic
 * implementations in {@link AbstractBitVector}. Instances of this class represent a bit vector
 * using a {@linkplain LongBigArrays big array of longs} that is enlarged as needed when new entries
 * are created (using {@link LongBigArrays#grow(long[][], long, long)}), but is <em>never</em> made
 * smaller (even on a {@link #clear()}). Use {@link #trim()} for that purpose.
 *
 * <p>
 * Besides usual methods for setting and getting bits, this class provides <em>views</em> that make
 * it possible to access comfortably the bit vector in different ways: for instance,
 * {@link #asLongBigList(int)} provide access as a list of longs, whereas {@link #asLongSet()}
 * provides access in setwise form.
 *
 * <p>
 * When enlarging the underlying array (e.g., for {@link #append(long, int)} operations or add
 * operations on the {@linkplain #asLongBigList(int) big list view}), or when invoking
 * {@link #ensureCapacity(long)}, this class calls {@link LongBigArrays#grow(long[][], long, long)},
 * which could enlarge the array more than expected. On the contrary, {@link #length(long)} (and the
 * corresponding method in the {@linkplain #asLongBigList(int) big list view}) sizes the underlying
 * array in an exact manner.
 *
 * <P>
 * Bit numbering follows the right-to-left convention: bit <var>k</var> (counted from the right) of
 * word <var>w</var> is bit 64<var>w</var> + <var>k</var> of the overall bit vector.
 *
 * <P>
 * If {@link #CHECKS} is true at compile time, boundary checks for all bit operations will be
 * compiled in. For maximum speed, you may want to recompile this class with {@link #CHECKS} set to
 * false. {@link #CHECKS} is public, so you can check from your code whether you're being provided a
 * version with checks or not. In any case, many checks happen when you enable assertions.
 *
 * <p>
 * <strong>Warning</strong>: Several optional methods have still to be implemented (e.g., adding an
 * element at an arbitrary position using the {@link BooleanBigList} methods).
 *
 * <p>
 * <strong>Warning</strong>: The {@link #bits()} method uses the {@link AbstractBitVector}
 * implementation, which will fail for bit vectors that cannot be stored in a single long array
 * (i.e., more than {@link Arrays#MAX_ARRAY_SIZE}) * {@link Long#SIZE}).
 */

public class LongBigArrayBitVector extends AbstractBitVector implements Cloneable, Serializable {
	private static final long serialVersionUID = 2L;

	/** Whether this class has been compiled with index checks or not. */
	public static final boolean CHECKS = false;

	/** The number of bits in this vector. */
	protected long length;
	/**
	 * The backing big array of this vector. Bit 0 of the first element of the first array contains bit
	 * 0 of the bit vector, bit 0 of the second element contains bit {@link Long#SIZE} of the bit vector
	 * and so on.
	 */
	protected transient long[][] bits;

	/**
	 * Returns the number of words that are necessary to hold the given number of bits.
	 *
	 * @param size a number of bits.
	 * @return the number of words that are necessary to hold the given number of bits.
	 */

	public static final long words(final long size) {
		return (size + Long.SIZE - 1) >>> LOG2_BITS_PER_WORD;
	}

	/**
	 * Returns the number of bits in the given number of words.
	 *
	 * @param word a word position.
	 * @return {@link Long#SIZE} * {@code word}.
	 */
	public static final long bits(final long word) {
		assert word >= 0;
		return word << LOG2_BITS_PER_WORD;
	}

	/**
	 * Return the index of the word that holds a bit of specified index.
	 *
	 * @param index the index of a bit, or -1.
	 * @return the index of the word that holds the bit of given index, or -1 if {@code index} is -1.
	 */
	public static final long word(final long index) {
		return index >>> LOG2_BITS_PER_WORD;
	}

	protected LongBigArrayBitVector(final long capacity) {
		this.bits = capacity > 0 ? LongBigArrays.newBigArray(words(capacity)) : LongBigArrays.EMPTY_BIG_ARRAY;
	}

	/**
	 * Creates a new empty bit vector of given capacity. The resulting vector will be able to contain
	 * {@code capacity} bits without reallocations of the backing array.
	 *
	 * <P>
	 * Note that this constructor creates an <em>empty</em> bit vector. If you want a cleared bit vector
	 * of a specified size, please use the {@link #ofLength(long)} factory method.
	 *
	 * @param capacity the capacity (in bits) of the new bit vector.
	 * @return a new bit vector of given capacity.
	 */
	public static LongBigArrayBitVector getInstance(final long capacity) {
		return new LongBigArrayBitVector(capacity);
	}

	/**
	 * Creates a new empty bit vector. No allocation is actually performed.
	 *
	 * @return a new bit vector with no capacity.
	 */
	public static LongBigArrayBitVector getInstance() {
		return new LongBigArrayBitVector(0);
	}

	/**
	 * Creates a new empty bit vector of given length.
	 *
	 * @param length the size (in bits) of the new bit vector.
	 */
	public static LongBigArrayBitVector ofLength(final long length) {
		final LongBigArrayBitVector bv = new LongBigArrayBitVector(length);
		bv.length = length;
		return bv;
	}

	/**
	 * Creates a new bit vector with given bits.
	 *
	 * @param bit a list of bits that will be set in the newly created bit vector.
	 */
	public static LongBigArrayBitVector of(final int... bit) {
		final LongBigArrayBitVector bitVector = new LongBigArrayBitVector(bit.length);
		for (final int b : bit) {
			if (b != 0 && b != 1) throw new IllegalArgumentException("Illegal bit value: " + b);
			bitVector.add(b);
		}
		return bitVector;
	}

	@Override
	public long length() {
		return length;
	}

	/**
	 * Returns the underlying big array.
	 *
	 * @return the underlying big array.
	 */
	public long[][] bigBits() {
		return bits;
	}

	/**
	 * Ensures that this bit vector can hold the specified number of bits.
	 *
	 * <p>
	 * This method uses {@link LongBigArrays#grow(long[][], long, long)} to ensure that there is enough
	 * space for the given number of bits. As a consequence, the actual length of the long array
	 * allocated might be larger than expected.
	 *
	 * @param numBits the number of bits that this vector must be able to contain.
	 * @return this bit vector.
	 */

	public LongBigArrayBitVector ensureCapacity(final long numBits) {
		bits = grow(bits, words(numBits), words(length));
		return this;
	}

	@Override
	public LongBigArrayBitVector length(final long newLength) {
		bits = BigArrays.ensureCapacity(bits, words(newLength), words(length));
		final long oldLength = length;
		if (newLength < oldLength) fill(newLength, oldLength, false);
		length = newLength;
		return this;
	}

	@Override
	public void fill(final boolean value) {
		final long fullWords = word(length);
		BigArrays.fill(bits, 0, fullWords, value ? 0xFFFFFFFFFFFFFFFFL : 0L);
		if (!round(length)) if (value) BigArrays.set(bits, fullWords, (1L << length) - 1);
		else BigArrays.set(bits, fullWords, 0);
	}

	/**
	 * Reduces as must as possible the size of the backing array.
	 *
	 * @return true if some trimming was actually necessary.
	 */

	public boolean trim() {
		if (BigArrays.length(bits) == words(length)) return false;
		bits = BigArrays.setLength(bits, words(length));
		return true;
	}

	/**
	 * Sets the size of this bit vector to 0.
	 * <P>
	 * Note that this method does not try to reallocate that backing array. If you want to force that
	 * behaviour, call {@link #trim()} afterwards.
	 */
	@Override
	public void clear() {
		if (length != 0) BigArrays.fill(bits, 0, words(length), 0);
		length = 0;
	}

	@Override
	public LongBigArrayBitVector copy() {
		final LongBigArrayBitVector copy = new LongBigArrayBitVector(length);
		copy.length = length;
		BigArrays.copy(bits, 0, copy.bits, 0, words(length));
		return copy;
	}

	/**
	 * Returns this bit vector.
	 *
	 * @return this bit vector.
	 */
	@Override
	public LongBigArrayBitVector fast() {
		return this;
	}

	/**
	 * Returns a copy of the given bit vector.
	 *
	 * <p>
	 * This method uses {@link BitVector#getLong(long, long)} on {@link Long#SIZE} boundaries to copy at
	 * high speed.
	 *
	 * @param bv a bit vector.
	 * @return an instance of this class containing a copy of the given vector.
	 */
	public static LongBigArrayBitVector copy(final BitVector bv) {
		final long length = bv.length();
		final LongBigArrayBitVector copy = new LongBigArrayBitVector(length);
		final long fullBits = length & -Long.SIZE;
		for (long i = 0; i < fullBits; i += Long.SIZE) BigArrays.set(copy.bits, word(i), bv.getLong(i, i + Long.SIZE));
		if (!round(length)) BigArrays.set(copy.bits, word(fullBits), bv.getLong(fullBits, length));
		copy.length = length;
		return copy;
	}

	@Override
	public boolean getBoolean(final long index) {
		assert index >= 0;
		assert index < length;
		if (CHECKS) ensureRestrictedIndex(index);
		return (BigArrays.get(bits, word(index)) & mask(index)) != 0;
	}

	@Override
	public boolean set(final long index, final boolean value) {
		assert index >= 0;
		assert index < length;
		if (CHECKS) ensureRestrictedIndex(index);
		final long word = word(index);
		final long mask = mask(index);
		final int segment = BigArrays.segment(word);
		final int displacement = BigArrays.displacement(word);
		final boolean oldValue = (bits[segment][displacement] & mask) != 0;
		if (value != oldValue) bits[segment][displacement] ^= mask;
		return oldValue;
	}

	@Override
	public void set(final long index) {
		assert index >= 0;
		assert index < length;
		if (CHECKS) ensureRestrictedIndex(index);
		final long word = word(index);
		bits[BigArrays.segment(word)][BigArrays.displacement(word)] |= mask(index);
	}

	@Override
	public void clear(final long index) {
		assert index >= 0;
		assert index < length;
		if (CHECKS) ensureRestrictedIndex(index);
		final long word = word(index);
		bits[BigArrays.segment(word)][BigArrays.displacement(word)] &= ~mask(index);
	}

	@Override
	public LongBigArrayBitVector append(final long value, final int width) {
		if (width == 0) return this;
		assert width == Long.SIZE || (value & -1L << width) == 0;
		if (CHECKS) if (width < Long.SIZE && (value & -1L << width) != 0) throw new IllegalArgumentException("The specified value (" + value + ") is larger than the maximum value for the given width (" + width + ")");
		final long length = this.length;
		final long startWord = word(length);
		int segment = segment(startWord);
		int displacement = displacement(startWord);
		final int startBit = bit(length);
		ensureCapacity(length + width);

		if (startBit + width <= Long.SIZE) bits[segment][displacement] |= value << startBit;
		else {
			bits[segment][displacement] |= value << startBit;
			if (++displacement == SEGMENT_SIZE) {
				displacement = 0;
				segment++;
			}
			bits[segment][displacement] = value >>> -startBit;
		}

		this.length += width;
		return this;
	}

	@Override
	public boolean add(final boolean value) {
		final long length = this.length;
		final long startWord = word(length);
		final int segment = segment(startWord);
		final int displacement = displacement(startWord);
		final int startBit = bit(length);
		ensureCapacity(length + 1);

		if (value) bits[segment][displacement] |= mask(startBit);
		this.length++;
		return true;
	}

	@Override
	public long getLong(final long from, final long to) {
		assert 0 <= from;
		assert from <= to;
		assert to <= length;
		if (CHECKS) BitVectors.ensureFromTo(length, from, to);
		final long l = Long.SIZE - (to - from);
		final long startWord = word(from);
		int segment = segment(startWord);
		int displacement = displacement(startWord);
		final int startBit = bit(from);
		if (l == Long.SIZE) return 0;
		if (startBit <= l) return bits[segment][displacement] << l - startBit >>> l;
		final long result = bits[segment][displacement] >>> startBit;
		if (++displacement == SEGMENT_SIZE) {
			displacement = 0;
			segment++;
		}
		return result | bits[segment][displacement] << l - startBit >>> l;
	}

	/**
	 * Wraps the given big array of longs in a bit vector for the given number of bits.
	 *
	 * <p>
	 * Note that all bits in {@code array} beyond that of index {@code size} must be unset, or an
	 * exception will be thrown.
	 *
	 * @param array a big array of longs.
	 * @param size the number of bits of the newly created bit vector.
	 * @return a bit vector of size {@code size} using {@code array} as backing big array.
	 */
	public static LongBigArrayBitVector wrap(final long[][] array, final long size) {
		if (size > bits(BigArrays.length(array))) throw new IllegalArgumentException("The provided array is too short (" + BigArrays.length(array) + " elements) for the given size (" + size + ")");
		final LongBigArrayBitVector result = new LongBigArrayBitVector(0);
		result.length = size;
		result.bits = array;

		final long arrayLength = BigArrays.length(array);
		final long lastWord = word(size);
		if (lastWord < arrayLength && (BigArrays.get(array, lastWord) & ~((1L << size) - 1)) != 0) throw new IllegalArgumentException("Garbage beyond size in bit array");
		for (long i = lastWord + 1; i < arrayLength; i++) if (BigArrays.get(array, i) != 0) throw new IllegalArgumentException("Garbage beyond size in bit array");
		return result;
	}

	/**
	 * Wraps the given array of longs in a bit vector.
	 *
	 * @param array an array of longs.
	 * @return a bit vector of size {@code Long.SIZE} times the length of {@code array} using
	 *         {@code array} as backing array.
	 */
	public static LongBigArrayBitVector wrap(final long[][] array) {
		return wrap(array, BigArrays.length(array) * Long.SIZE);
	}

	/**
	 * Returns a cloned copy of this bit vector.
	 *
	 * <P>
	 * This method is functionally equivalent to {@link #copy()}, except that {@link #copy()} trims the
	 * backing array.
	 *
	 * @return a copy of this bit vector.
	 */
	@Override
	public LongBigArrayBitVector clone() throws CloneNotSupportedException {
		final LongBigArrayBitVector copy = (LongBigArrayBitVector)super.clone();
		copy.bits = bits.clone();
		return copy;
	}

	@Override
	public int hashCode() {
		long h = 0x9e3779b97f4a7c13L ^ length;
		final long numWords = words(length);
		for (long i = 0; i < numWords; i++) h ^= (h << 5) + BigArrays.get(bits, i) + (h >>> 2);
		assert (int)((h >>> 32) ^ h) == super.hashCode();
		return (int)((h >>> 32) ^ h);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof LongBigArrayBitVector) return equals((LongBigArrayBitVector)o);
		return super.equals(o);
	}

	public boolean equals(final LongBigArrayBitVector v) {
		if (length != v.length()) return false;
		long i = words(length);
		while (i-- != 0) if (BigArrays.get(bits, i) != BigArrays.get(v.bits, i)) return false;
		return true;
	}

	/**
	 * A list-of-integers view of a bit vector.
	 *
	 * <P>
	 * This class implements in the obvious way a view of a bit vector as a list of integers of given
	 * width. The vector is enlarged as needed (i.e., when adding new elements), but it is never shrunk.
	 */

	protected static class LongBigListView extends AbstractBitVector.LongBigListView {
		private static final long serialVersionUID = 1L;
		@SuppressWarnings("hiding")
		private final LongBigArrayBitVector bitVector;

		public LongBigListView(final LongBigArrayBitVector bitVector, final int width) {
			super(bitVector, width);
			this.bitVector = bitVector;
		}

		@Override
		public boolean add(final long value) {
			bitVector.append(value, width);
			return true;
		}

		@Override
		public long getLong(final long index) {
			final long start = index * width;
			return bitVector.getLong(start, start + width);
		}

		@Override
		public void clear() {
			bitVector.clear();
		}

		@Override
		public long set(final long index, final long value) {
			if (width == 0) return 0;
			if (width != Long.SIZE && value > fullMask) throw new IllegalArgumentException("Value too large: " + value);
			ensureRestrictedIndex(index);
			final long[][] bits = bitVector.bits;
			final long start = index * width;
			final long startWord = word(start);
			final long endWord = word(start + width - 1);
			final int startBit = bit(start);
			final long oldValue;

			if (startWord == endWord) {
				final int segment = segment(startWord);
				final int displacement = displacement(startWord);
				oldValue = bits[segment][displacement] >>> startBit & fullMask;
				bits[segment][displacement] &= ~(fullMask << startBit);
				bits[segment][displacement] |= value << startBit;

				assert value == (bits[segment][displacement] >>> startBit & fullMask);
			} else {
				// Here startBit > 0.
				final int startSegment = segment(startWord);
				final int startDisplacement = displacement(startWord);
				final int endSegment = segment(endWord);
				final int endDisplacement = displacement(endWord);
				oldValue = bits[startSegment][startDisplacement] >>> startBit | bits[endSegment][endDisplacement] << -startBit & fullMask;
				bits[startSegment][startDisplacement] &= (1L << startBit) - 1;
				bits[startSegment][startDisplacement] |= value << startBit;
				bits[endSegment][endDisplacement] &= -(1L << width + startBit);
				bits[endSegment][endDisplacement] |= value >>> -startBit;

				assert value == (bits[startSegment][startDisplacement] >>> startBit | bits[endSegment][endDisplacement] << -startBit & fullMask);
			}
			return oldValue;
		}
	}

	@Override
	public LongBigList asLongBigList(final int width) {
		return new LongBigListView(this, width);
	}

	private void writeObject(final ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		final long numWords = words(length);
		for (long i = 0; i < numWords; i++) s.writeLong(BigArrays.get(bits, i));
	}

	private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		final long numWords = words(length);
		bits = LongBigArrays.newBigArray(numWords);
		for (long i = 0; i < numWords; i++) BigArrays.set(bits, i, s.readLong());
	}

}
