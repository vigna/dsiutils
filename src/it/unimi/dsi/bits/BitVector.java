/*
 * DSI utilities
 *
 * Copyright (C) 2007-2021 Sebastiano Vigna
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

import java.util.List;
import java.util.RandomAccess;

import it.unimi.dsi.fastutil.booleans.BooleanBigList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

/**
 * A vector of bits, a&#46;k&#46;a&#46; bit sequence, bit string, binary word, etc.
 *
 * <P>
 * This interface define several operations on finite sequences of bits. Efficient implementations,
 * such as {@link LongArrayBitVector}, use approximately one bit of memory for each bit in the
 * vector, but this is not enforced.
 *
 * <P>
 * Operation of a bit vector are partially of boolean nature (e.g., logical operations between
 * vectors), partially of language-theoretical nature (e.g., concatenation), and partially of
 * set-theoretical nature (e.g., asking which bits are set to one). To accomodate all these points
 * of view, this interface extends {@link it.unimi.dsi.fastutil.booleans.BooleanBigList}, but also
 * provides an {@link #asLongSet()} method that exposes a {@link java.util.BitSet}-like view and a
 * {@link #asLongBigList(int)} method that provides integer-like access to blocks of bits of given
 * width.
 *
 * <P>
 * Most, if not all, classical operations on bit vectors can be seen as standard operations on these
 * two views: for instance, the number of bits set to one is just the number of elements of the set
 * returned by {@link #asLongSet()} (albeit a direct {@link #count()} method is provided, too). The
 * standard {@link java.util.Collection#addAll(java.util.Collection)} method can be used to
 * concatenate bit vectors, and
 * {@linkplain it.unimi.dsi.fastutil.booleans.BooleanBigList#subList(long, long) sublist views} make
 * it easy performing any kind of logical operation on subvectors.
 *
 * <P>
 * The only <i>caveat</i> is that sometimes the standard interface naming clashes slightly with
 * standard usage: for instance, {@link #clear()} will <em>not</em> set to zero all bits (use
 * {@link #fill(int) fill(0)} for that purpose), but rather will set the vector length to zero.
 * Also, {@link #add(long, int)} will not add logically a value at the specified index, but rather
 * will insert a new bit with the specified value at the specified position.
 *
 * <P>
 * To increase clarity, this interface provides methods {@link #length()} and {@link #length(long)}
 * which have a semantics similar to {@link #size64()} and {@link #size(long)}.
 *
 * <P>
 * The {@link AbstractBitVector} class provides a fairly complete abstract implementation that
 * provides all methods except for the most basic operations. Of course, the methods of
 * {@link AbstractBitVector} are sometimes very inefficient, but implementations such as
 * {@link LongArrayBitVector} have their own optimised implementations.
 */
public interface BitVector extends RandomAccess, BooleanBigList {

	/**
	 * Sets a bit in this bit vector (optional operation).
	 *
	 * @param index the index of a bit.
	 */
	void set(long index);

	/**
	 * Clears a bit in this bit vector (optional operation).
	 *
	 * @param index the index of a bit.
	 */
	void clear(long index);

	/**
	 * Flips a bit in this bit vector (optional operation).
	 *
	 * @param index the index of a bit.
	 */
	void flip(long index);

	/**
	 * Fills a range of bits in this bit vector (optional operation).
	 *
	 * @param from the first index (inclusive).
	 * @param to the last index (not inclusive).
	 * @param value the value (true or false).
	 */
	void fill(long from, long to, boolean value);

	/**
	 * Clears a range of bits in this bit vector (optional operation).
	 *
	 * @param from the first index (inclusive).
	 * @param to the last index (not inclusive).
	 * @param value the value (zero or nonzero).
	 */
	void fill(long from, long to, int value);

	/**
	 * Sets all bits this bit vector to the given boolean value (optional operation).
	 *
	 * @param value the value (true or false).
	 */
	void fill(boolean value);

	/**
	 * Sets all bits this bit vector to the given integer value (optional operation).
	 *
	 * @param value the value (zero or nonzero).
	 */
	void fill(int value);

	/**
	 * Flips a range of bits in this bit vector (optional operation).
	 *
	 * @param from the first index (inclusive).
	 * @param to the last index (not inclusive).
	 */
	void flip(long from, long to);

	/** Flips all bits in this bit vector (optional operation). */
	void flip();

	/**
	 * Replaces the content of this bit vector with another bit vector.
	 *
	 * @param bitVector a bit vector.
	 * @return this bit vector.
	 */
	BitVector replace(BitVector bitVector);

	/**
	 * Returns a subvector view specified by initial and final index.
	 *
	 * <p>
	 * The object returned by this method is a bit vector representing a <em>view</em> of this bit
	 * vector restricted to the given indices. Changes to the subvector will be reflected in the main
	 * vector.
	 *
	 * @param from the first index (inclusive).
	 * @param to the last index (not inclusive).
	 */
	BitVector subVector(long from, long to);

	/**
	 * Returns a subvector view specified by initial index and running up to the end of this vector.
	 *
	 * @param from the first index (inclusive).
	 * @see #subVector(long, long)
	 */
	BitVector subVector(long from);

	/**
	 * Returns a view of this bit vector as a sorted set of long integers.
	 *
	 * <P>
	 * More formally, this bit vector is infinitely extended to the left with zeros (e.g., all bits
	 * beyond {@link #length(long)} are considered zeroes). The resulting infinite string is interpreted
	 * as the characteristic function of a set of integers.
	 *
	 * <P>
	 * Note that, in particular, the resulting string representation is exactly that of a
	 * {@link java.util.BitSet}.
	 *
	 */
	LongSortedSet asLongSet();

	/**
	 * Returns a view of this bit vector as a list of nonnegative integers of specified width.
	 *
	 * <P>
	 * More formally, {@link LongBigList#getLong(long) getLong(p)} will return the nonnegative integer
	 * defined by the bits starting at <code>p * width</code> (bit 0, inclusive) and ending at
	 * <code>(p + 1) * width</code> (bit <code>width</code> &minus; 1, exclusive).
	 *
	 * @param width a bit width.
	 */
	LongBigList asLongBigList(int width);

	/**
	 * Returns the value of the specified bit as an integer.
	 *
	 * <P>
	 * This method is a useful synonym for {@link #getBoolean(long)}.
	 *
	 * @param index the index of a bit.
	 * @return the value of the specified bit as an integer (0 or 1).
	 */
	int getInt(long index);

	/**
	 * Returns the specified bit range as a long.
	 *
	 * <P>
	 * Note that bit 0 of the returned long will be bit <code>from</code> of this bit vector.
	 *
	 * <P>
	 * Implementations are invited to provide high-speed implementations for the case in which
	 * <code>from</code> is a multiple of {@link Long#SIZE} and <code>to</code> is <code>from</code> +
	 * {@link Long#SIZE} (or less, in case the vector length is exceeded). This behaviour make it
	 * possible to implement high-speed hashing, copies, etc.
	 *
	 * @param from the starting bit (inclusive).
	 * @param to the ending bit (exclusive).
	 * @return the long value contained in the specified bits.
	 */
	long getLong(long from, long to);

	/**
	 * Sets the value of the specified bit as an integer (optional operation).
	 *
	 * <P>
	 * This method is a useful synonym for {@link #set(long, boolean)}.
	 *
	 * @param index the index of a bit.
	 * @param value the new value (any nonzero integer for setting the bit, zero for clearing the bit).
	 */
	void set(long index, int value);

	/**
	 * Adds a bit with specified integer value at the specified index (optional operation).
	 *
	 * <P>
	 * This method is a useful synonym for {@link #add(long, boolean)}.
	 *
	 * @param index the index of a bit.
	 * @param value the value that will be inserted at position <code>index</code> (any nonzero integer
	 *            for a true bit, zero for a false bit).
	 */
	void add(long index, int value);

	/**
	 * Adds a bit with specified value at the end of this bit vector.
	 *
	 * <P>
	 * This method is a useful synonym for {@link BooleanList#add(boolean)}.
	 *
	 * @param value the new value (any nonzero integer for a true bit, zero for a false bit).
	 */

	void add(int value);

	/**
	 * Appends the less significant bits of a long integer to this bit vector.
	 *
	 * @param value a value to be appended
	 * @param k the number of less significant bits to be added to this bit vector.
	 * @return this bit vector.
	 */

	BitVector append(long value, int k);

	/**
	 * Appends another bit vector to this bit vector.
	 *
	 * @param bitVector a bit vector to be appended.
	 * @return this bit vector.
	 */

	BitVector append(BitVector bitVector);

	/**
	 * Returns the number of bits in this bit vector.
	 *
	 * <p>
	 * If the number of bits in this vector is smaller than or equal to {@link Integer#MAX_VALUE}, this
	 * method is semantically equivalent to {@link List#size()}. In any case, this method is
	 * semantically equivalent to {@link BooleanBigList#size64()}, but it is prefererred.
	 *
	 * @return the number of bits in this bit vector.
	 */
	long length();

	/**
	 * Sets the number of bits in this bit vector.
	 *
	 * <p>
	 * It is expected that this method will try to allocate exactly the necessary space.
	 *
	 * <p>
	 * If the argument fits an integer, this method has the same side effects of
	 * {@link BooleanList#size(int)}. In any case, this method has the same side effects of
	 * {@link BooleanBigList#size(long)}, but it is preferred, as it has the advantage of returning this
	 * bit vector, thus making it possible to chain methods.
	 *
	 * @param newLength the new length in bits for this bit vector.
	 * @return this bit vector.
	 */
	BitVector length(long newLength);

	/**
	 * Counts the number of bits set to true in this bit vector.
	 *
	 * @return the number of bits set to true in this bit vector.
	 */
	long count();

	/**
	 * Performs a logical and between this bit vector and another one, leaving the result in this
	 * vector.
	 *
	 * @param v a bit vector.
	 * @return this bit vector.
	 */
	BitVector and(BitVector v);

	/**
	 * Performs a logical or between this bit vector and another one, leaving the result in this vector.
	 *
	 * @param v a bit vector.
	 * @return this bit vector.
	 */
	BitVector or(BitVector v);

	/**
	 * Performs a logical xor between this bit vector and another one, leaving the result in this
	 * vector.
	 *
	 * @param v a bit vector.
	 * @return this bit vector.
	 */
	BitVector xor(BitVector v);

	/**
	 * Returns the position of the first bit set in this vector.
	 *
	 * @return the first bit set, or -1 for a vector of zeroes.
	 */
	long firstOne();

	/**
	 * Returns the position of the last bit set in this vector.
	 *
	 * @return the last bit set, or -1 for a vector of zeroes.
	 */
	long lastOne();

	/**
	 * Returns the position of the first bit set at of after the given position.
	 *
	 * @param index a bit position.
	 * @return the position of the first bit set at or after position <code>index</code>, or -1 if no
	 *         such bit exists.
	 */
	long nextOne(long index);

	/**
	 * Returns the position of the first bit set strictly before the given position.
	 *
	 * @param index a bit position.
	 * @return the position of the first bit set strictly before position <code>index</code>, or -1 if
	 *         no such bit exists.
	 */
	long previousOne(long index);

	/**
	 * Returns the position of the first bit unset in this vector.
	 *
	 * @return the first bit unset, or -1 for a vector of ones.
	 */
	long firstZero();

	/**
	 * Returns the position of the last bit unset in this vector.
	 *
	 * @return the last bit unset, or -1 for a vector of ones.
	 */
	long lastZero();

	/**
	 * Returns the position of the first bit unset after the given position.
	 *
	 * @param index a bit position.
	 * @return the first bit unset after position <code>index</code> (inclusive), or -1 if no such bit
	 *         exists.
	 */
	long nextZero(long index);

	/**
	 * Returns the position of the first bit unset before or at the given position.
	 *
	 * @param index a bit position.
	 * @return the first bit unset before or at the given position, or -1 if no such bit exists.
	 */
	long previousZero(long index);

	/**
	 * Returns the length of the greatest common prefix between this and the specified vector.
	 *
	 * @param v a bit vector.
	 * @return the length of the greatest common prefix.
	 */
	long longestCommonPrefixLength(BitVector v);

	/**
	 * Returns true if this vector is a prefix of the specified vector.
	 *
	 * @param v a bit vector.
	 * @return true if this vector is a prefix of <code>v</code>.
	 */
	boolean isPrefix(BitVector v);

	/**
	 * Returns true if this vector is a proper prefix of the specified vector.
	 *
	 * @param v a bit vector.
	 * @return true if this vector is a proper prefix of <code>v</code> (i.e., it is a prefix but not
	 *         equal).
	 */
	boolean isProperPrefix(BitVector v);

	/**
	 * Checks for equality with a segment of another vector.
	 *
	 * @param v a bit vector.
	 * @param from the starting bit, inclusive.
	 * @param to the ending bit, not inclusive.
	 * @return true if this vector and v are equal in the range of positions
	 *         [<code>from</code>..<code>to</code>).
	 */
	boolean equals(final BitVector v, final long from, final long to);

	/**
	 * Returns a copy of a part of this bit vector.
	 *
	 * @param from the starting bit, inclusive.
	 * @param to the ending bit, not inclusive.
	 * @return a copy of the part of this bit vector going from bit <code>from</code> (inclusive) to bit
	 *         <code>to</code> (not inclusive)
	 */
	BitVector copy(long from, long to);

	/**
	 * Returns a copy of this bit vector.
	 *
	 * @return a copy of this bit vector.
	 */
	BitVector copy();

	/**
	 * Returns the bits in this bit vector as an array of longs, not to be modified.
	 *
	 * @return an array of longs whose first {@link #length()} bits contain the bits of this bit vector.
	 *         The array cannot be modified.
	 */
	long[] bits();

	/**
	 * Returns a hash code for this bit vector.
	 *
	 * <p>
	 * Hash codes for bit vectors are defined as follows:
	 *
	 * <pre>
	 * final long length = length();
	 * long fullLength = length &amp; -Long.SIZE;
	 * long h = 0x9e3779b97f4a7c13L ^ length;
	 * for(long i = 0; i &lt; fullLength; i += Long.SIZE) h ^= (h &lt;&lt; 5) + getLong(i, i + Long.SIZE) + (h &gt;&gt;&gt; 2);
	 * if (length != fullLength) h ^= (h &lt;&lt; 5) + getLong(fullLength, length) + (h &gt;&gt;&gt; 2);
	 * (int)((h &gt;&gt;&gt; 32) ^ h);
	 * </pre>
	 *
	 * <p>
	 * The last value is the hash code of the bit vector. This hashing is based on shift-add-xor hashing
	 * (M.V. Ramakrishna and Justin Zobel, &ldquo;Performance in practice of string hashing
	 * functions&rdquo;, <i>Proc. of the Fifth International Conference on Database Systems for Advanced
	 * Applications</i>, 1997, pages 215&minus;223).
	 *
	 * <p>
	 * The returned value is not a high-quality hash such as <a href=
	 * "http://sux4j.di.unimi.it/docs/it/unimi/dsi/sux4j/mph/Hashes.html#jenkins(it.unimi.dsi.bits.BitVector)">Jenkins's</a>,
	 * but it can be computed very quickly; in any case, 32 bits are too few for a high-quality hash to
	 * be used in large-scale applications.
	 *
	 * <p>
	 * <strong>Important</strong>: all bit vector implementations are required to return the value
	 * defined here. The simplest way to obtain this result is to subclass {@link AbstractBitVector}.
	 *
	 * @return a hash code for this bit vector.
	 */
	@Override
	int hashCode();

	/**
	 * Returns a fast version of this bit vector.
	 *
	 * <p>
	 * Different implementations of this interface might provide different level of efficiency. For
	 * instance, <em>views</em> on other data structures (e.g., strings) might implement
	 * {@link #getLong(long, long)} efficiently on multiples of {@link Long#SIZE}, but might fail to
	 * provide a generic, truly efficient random access.
	 *
	 * <p>
	 * This method returns a (possibly immutable) bit vector with the same content as that of this bit
	 * vector. However, the returned bit vector is guaranteed to provide fast random access.
	 *
	 * @return a fast version of this bit vector.
	 */
	BitVector fast();

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #size64()} instead.
	 */
	@Deprecated
	@Override
	default int size() {
		return (int)Math.min(Integer.MAX_VALUE, size64());
	}
}
