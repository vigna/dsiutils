/*
 * DSI utilities
 *
 * Copyright (C) 2003-2021 Paolo Boldi and Sebastiano Vigna
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

import java.util.Comparator;


/** A class providing static methods and objects that do useful things with intervals.
 *
 * @see Interval
 */

public class Intervals {

	private Intervals() {}

	public static final Interval[] EMPTY_ARRAY = {};

	/** An empty (singleton) interval. */
	public static final Interval EMPTY_INTERVAL = new Interval(1, 0);

	/** A singleton located at &minus;&#8734;. */
	public static final Interval MINUS_INFINITY = new Interval(Integer.MIN_VALUE, Integer.MIN_VALUE);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>'] iff
	 * the first interval starts before or prolongs the second one, that is,
	 * iff <var>a</var> &lt; <var>a</var>' or <var>a</var>=<var>a</var>' and <var>b</var>' &lt; <var>b</var>.
	 */
	public static final Comparator<Interval> STARTS_BEFORE_OR_PROLONGS = (i1, i2) -> {
		final int t = Integer.compare(i1.left, i2.left);
		if (t != 0) return t;
		return Integer.compare(i2.right, i1.right);
	};

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>'] iff
	 * the first interval ends before or is a suffix of the second one, that is,
	 * iff <var>b</var> &lt; <var>b</var>' or <var>b</var>=<var>b</var>' and <var>a</var>' &lt; <var>a</var>.
	 */
	public static final Comparator<Interval> ENDS_BEFORE_OR_IS_SUFFIX = (i1, i2) -> {
		final int t = Integer.compare(i1.right, i2.right);
		if (t != 0) return t;
		return Integer.compare(i2.left, i1.left);
	};

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval starts <em>after</em> the second one, that is,
	 * iff <var>a</var>' &lt; <var>a</var>.
	 */
	public static final Comparator<Interval> STARTS_AFTER = (i1, i2) -> Integer.compare(i2.left, i1.left);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval starts <em>before</em> the second one, that is,
	 * iff <var>a</var> &lt; <var>a</var>'.
	 */
	public static final Comparator<Interval> STARTS_BEFORE = (i1, i2) -> Integer.compare(i1.left, i2.left);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval ends <em>after</em> the second one, that is,
	 * iff <var>b</var>' &lt; <var>b</var>.
	 */
	public static final Comparator<Interval> ENDS_AFTER = (i1, i2) -> Integer.compare(i2.right, i1.right);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval ends <em>before</em> the second one, that is,
	 * iff <var>b</var> &lt; <var>b</var>'.
	 */
	public static final Comparator<Interval> ENDS_BEFORE = (i1, i2) -> Integer.compare(i1.right, i2.right);

	/** A comparator between intervals based on their length. */
	public static final Comparator<Interval> LENGTH_COMPARATOR = (i1, i2) -> Integer.compare(i1.length(), i2.length());
}

