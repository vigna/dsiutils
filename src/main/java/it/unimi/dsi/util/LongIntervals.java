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

import java.util.Comparator;


/** A class providing static methods and objects that do useful things with intervals.
 *
 * @see LongInterval
 */

public class LongIntervals {

	private LongIntervals() {}

	public static final LongInterval[] EMPTY_ARRAY = {};

	/** An empty (singleton) interval. */
	public static final LongInterval EMPTY_INTERVAL = new LongInterval(1, 0);

	/** A singleton located at &minus;&#8734;. */
	public static final LongInterval MINUS_INFINITY = new LongInterval(Integer.MIN_VALUE, Integer.MIN_VALUE);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval starts <em>after</em> the second one, that is,
	 * iff <var>a</var>' &lt; <var>a</var>.
	 */
	public static final Comparator<LongInterval> STARTS_AFTER = (i1, i2) -> Long.compare(i2.left, i1.left);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval starts <em>before</em> the second one, that is,
	 * iff <var>a</var> &lt; <var>a</var>'.
	 */
	public static final Comparator<LongInterval> STARTS_BEFORE = (i1, i2) -> Long.compare(i1.left, i2.left);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval ends <em>after</em> the second one, that is,
	 * iff <var>b</var>' &lt; <var>b</var>.
	 */
	public static final Comparator<LongInterval> ENDS_AFTER = (i1, i2) -> Long.compare(i2.right, i1.right);

	/** A comparator between intervals defined as follows:
	 * [<var>a</var>..<var>b</var>] is less than [<var>a</var>'..<var>b</var>']
	 * iff the first interval ends <em>before</em> the second one, that is,
	 * iff <var>b</var> &lt; <var>b</var>'.
	 */
	public static final Comparator<LongInterval> ENDS_BEFORE = (i1, i2) -> Long.compare(i1.right, i2.right);

	/** A comparator between intervals based on their length. */
	public static final Comparator<LongInterval> LENGTH_COMPARATOR = (i1, i2) -> Long.compare(i1.length(), i2.length());
}

