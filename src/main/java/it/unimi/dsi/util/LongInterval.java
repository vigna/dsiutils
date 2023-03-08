/*
 * DSI utilities
 *
 * Copyright (C) 2003-2023 Paolo Boldi and Sebastiano Vigna
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

import static it.unimi.dsi.util.LongIntervals.EMPTY_INTERVAL;

import java.io.Serializable;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.longs.AbstractLongSortedSet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;



/** An interval of longs. See {@link Interval} for details. */

public final class LongInterval extends AbstractLongSortedSet implements LongSortedSet, Serializable {
	private static final long serialVersionUID = 1L;
	/** One-point intervals between 0 (inclusive) and this number (exclusive) are generated
     * from a pre-computed array of instances. */
	private final static int MAX_SINGLE_POINT = 1024;
	/** The precomputed array of one-point intervals. */
	private final static LongInterval[] POINT_INTERVAL = new LongInterval[MAX_SINGLE_POINT];

	static {
		int i = MAX_SINGLE_POINT;
		while(i-- != 0) POINT_INTERVAL[i] = new LongInterval(i, i);
	}

	/** The left extreme of the interval. */
	public final long left;
	/** The right extreme of the interval. */
	public final long right;

	/** Builds an interval with given extremes.
	 *
	 * <P>You cannot generate an empty interval with this constructor. Use {@link Intervals#EMPTY_INTERVAL} instead.
	 *
	 *  @param left the left extreme.
	 *  @param right the right extreme (which must be greater than
	 *  or equal to the left extreme).
	 */
	protected LongInterval(final long left, final long right) {
		this.left = left;
		this.right = right;
	}

	/** Returns an interval with given extremes.
	 *
	 * <P>You cannot obtain an empty interval with this factory method. Use {@link Intervals#EMPTY_INTERVAL} instead.
	 *
	 *  @param left the left extreme.
	 *  @param right the right extreme (which must be greater than
	 *  or equal to the left extreme).
	 *  @return an interval with the given extremes.
	 */
	public static LongInterval valueOf(final long left, final long right) {
		if (left > right) throw new IllegalArgumentException("The left extreme (" + left + ") is greater than the right extreme (" + right + ")");
		if (left == right) return valueOf(left);
		return new LongInterval(left, right);
	}

	/** Returns a one-point interval.
	 *
	 * <P>You cannot obtain an empty interval with this factory method. Use {@link Intervals#EMPTY_INTERVAL} instead.
	 *
	 *  @param point a point.
	 *  @return a one-point interval
	 */
	public static LongInterval valueOf(final long point) {
		if (point >= 0 && point < MAX_SINGLE_POINT) return POINT_INTERVAL[(int)point];
		return new LongInterval(point, point);
	}

	/** Returns the interval length, that is, the number of integers
	 *  contained in the interval.
	 *
	 *  @return the interval length.
	 */
	public long length() {
		return right - left + 1;
	}

	/** An alias for {@link #length()} miminised with {@link Integer#MAX_VALUE}.
	 * @return the interval length minimised with {@link Integer#MAX_VALUE}.
	 */
	@Override
	public int size() {
		return (int)Math.min(length(), Integer.MAX_VALUE);
	}

	/** An alias for {@link #length()}.
	 * @return the interval length.
	 */
	public long size64() {
		return length();
	}

	/** Returns an iterator over the integers in this interval.
	 *
	 * @return an integer iterator over the elements in this interval.
	 */
	@Override
	public LongBidirectionalIterator iterator() {
		if (this == EMPTY_INTERVAL) return LongIterators.EMPTY_ITERATOR;
		// Note that fromTo() does NOT include the last integer.
		return LongIterators.fromTo(left, right + 1);
	}

	/** Returns an iterator over the integers in this interval larger than or equal to a given integer.
	 *
	 * @param from the starting integer.
	 * @return an integer iterator over the elements in this interval.
	 */
	@Override
	public LongBidirectionalIterator iterator(final long from) {
		if (this == EMPTY_INTERVAL) return LongIterators.EMPTY_ITERATOR;
		// Note that fromTo() does NOT include the last integer.
		final LongBidirectionalIterator i = LongIterators.fromTo(left, right + 1);
		if (from >= left) {
			long toSkip = Math.min(length(), from + 1 - left);
			while(toSkip > 0) toSkip -= i.skip((int)Math.min(1 << 30, toSkip));
		}
		return i;
	}

	/** Checks whether this interval contains the specified integer.
	 *
	 * @param x an integer.
	 * @return whether this interval contains <code>x</code>, that is,
	 * whether {@link #left} &le; <code>x</code> &le; {@link #right}.
	 */

	public boolean contains(final int x) {
		return x >= left && x <= right;
	}

	/** Checks whether this interval contains the specified interval.
	 *
	 * @param interval an interval.
	 * @return whether this interval contains (as a set) <code>interval</code>.
	 */

	public boolean contains(final LongInterval interval) {
		if (interval == EMPTY_INTERVAL) return true;
		if (this == EMPTY_INTERVAL) return false;
		return left <= interval.left && interval.right <= right;
	}

	/** Checks whether this interval would contain the specified integer if enlarged in both
	 * directions by the specified radius.
	 *
	 * @param x an integer.
	 * @param radius the radius.
	 * @return whether this interval enlarged by <code>radius</code> would contain <code>x</code>,
	 * e.g., whether {@link #left}&minus;<code>radius</code> &le; <code>x</code> &le; {@link #right}+<code>radius</code>.
	 */

	public boolean contains(final int x, final int radius) {
		if (this == EMPTY_INTERVAL) throw new IllegalArgumentException();
		return x >= left - radius && x <= right + radius;
	}

	/** Checks whether this interval would contain the specified integer if enlarged in each
	 * direction with the respective radius.
	 * directions by the specified radius.
	 *
	 * @param x an integer.
	 * @param leftRadius the left radius.
	 * @param rightRadius the right radius.
	 * @return whether this interval enlarged to the left by <code>leftRadius</code>
	 * and to the right by <code>rightRadius</code> would contain <code>x</code>,
	 * e.g., whether {@link #left}&minus;<code>leftRadius</code> &le; <code>x</code> &le; {@link #right}+<code>rightRadius</code>.
	 */

	public boolean contains(final int x, final int leftRadius, final int rightRadius) {
		if (this == EMPTY_INTERVAL) throw new IllegalArgumentException();
		return x >= left - leftRadius && x <= right + rightRadius;
	}



	/** Compares this interval to an integer.
	 *
	 * @param x an integer.
	 * @return  a negative integer, zero, or a positive integer as <code>x</code> is positioned
	 * at the left, belongs, or is positioned to the right of this interval, e.g.,
	 * as <code>x</code> &lt; {@link #left},
	 * {@link #left} &le; <code>x</code> &le; {@link #right} or
	 * {@link #right} &lt; <code>x</code>.
	 */

	public int compareTo(final int x) {
		if (this == EMPTY_INTERVAL) throw new IllegalArgumentException();
		if (x < left) return -1;
		if (x > right) return 1;
		return 0;
	}

	/** Compares this interval to an integer with a specified radius.
	 *
	 * @param x an integer.
	 * @param radius the radius.
	 * @return  a negative integer, zero, or a positive integer as <code>x</code> is positioned
	 * at the left, belongs, or is positioned to the right of this interval enlarged by <code>radius</code>, that is,
	 * as <code>x</code> &lt; {@link #left}&minus;<code>radius</code>,
	 * {@link #left}&minus;<code>radius</code> &le; <code>x</code> &le; {@link #right}+<code>radius</code> or
	 * {@link #right}+<code>radius</code> &lt; <code>x</code>.
	 */

	public int compareTo(final int x, final int radius) {
		if (this == EMPTY_INTERVAL) throw new IllegalArgumentException();
		if (x < left - radius) return -1;
		if (x > right + radius) return 1;
		return 0;
	}

	/** Compares this interval to an integer with specified left and right radii.
	 *
	 * @param x an integer.
	 * @param leftRadius the left radius.
	 * @param rightRadius the right radius.
	 * @return  a negative integer, zero, or a positive integer as <code>x</code> is positioned
	 * at the left, belongs, or is positioned to the right of this interval enlarged by <code>leftRadius</code>
	 * on the left and <code>rightRadius</code> in the right, that is,
	 * as <code>x</code> &lt; {@link #left}&minus;<code>leftRadius</code>,
	 * {@link #left}&minus;<code>leftRadius</code> &le; <code>x</code> &le; {@link #right}+<code>rightRadius</code> or
	 * {@link #right}+<code>rightRadius</code> &lt; <code>x</code>.
	 */

	public int compareTo(final int x, final int leftRadius, final int rightRadius) {
		if (this == EMPTY_INTERVAL) throw new IllegalArgumentException();
		if (x < left - leftRadius) return -1;
		if (x > right + rightRadius) return 1;
		return 0;
	}

	@Override
	public LongComparator comparator() {
		return null;
	}

	@Override
	public LongSortedSet headSet(final long to) {
		if (this == EMPTY_INTERVAL) return this;
		if (to > left) return to > right ? this : valueOf(left, to - 1);
		else return EMPTY_INTERVAL;
	}

	@Override
	public LongSortedSet tailSet(final long from) {
		if (this == EMPTY_INTERVAL) return this;
		if (from <= right) return from <= left ? this : valueOf(from, right);
		else return EMPTY_INTERVAL;
	}

	@Override
	public LongSortedSet subSet(final long from, final long to) {
		if (this == EMPTY_INTERVAL) return this;
		if (from > to) throw new IllegalArgumentException("Start element (" + from  + ") is larger than end element (" + to + ")");
		if (to <= left || from > right || from == to) return EMPTY_INTERVAL;
		if (from <= left && to > right) return this;
		return valueOf(Math.max(left, from), Math.min(right, to - 1));
	}

	@Override
	public long firstLong() {
		if (this == EMPTY_INTERVAL) throw new NoSuchElementException();
		return left;
	}

	@Override
	public long lastLong() {
		if (this == EMPTY_INTERVAL) throw new NoSuchElementException();
		return right;
	}

	@Override
	public String toString() {
		if (this == EMPTY_INTERVAL) return "\u2205";
		if (left == right) return "[" + left + "]";
		return "[" + left + ".." + right + "]"; // Hoare's notation.
	}

	@Override
	public int hashCode() {
		return (int)(left * 23 + right);
	}

	/** Checks whether this interval is equal to another set of integers.
	 *
	 * @param o an object.
	 * @return true if <code>o</code> is an ordered set of integer containing
	 * the same element of this interval in the same order, or if <code>o</code>
	 * is a set of integers containing the same elements of this interval.
	 */

	@Override
	public boolean equals(final Object o) {
		if (o instanceof LongInterval)
			return ((LongInterval)o).left == left && ((LongInterval)o).right == right;
		else if (o instanceof LongSortedSet) { // For sorted sets, we require the same order
			final LongSortedSet s = (LongSortedSet) o;
			if (s.size() != length()) return false;
			long n = length();
			final LongIterator i = iterator(), j = s.iterator();
			while(n-- != 0) if (i.nextLong() != j.nextLong()) return false;
			return true;
		}
		else if (o instanceof LongSet) { // For sets, we just require the same elements
			final LongSet s = (LongSet) o;
			if (s.size() != length()) return false;
			long n = length();
			final LongIterator i = iterator();
			while(n-- != 0) if (! s.contains(i.nextLong())) return false;
			return true;
		}
		else return false;
	}
}
