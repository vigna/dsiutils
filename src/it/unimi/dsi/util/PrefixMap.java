/*
 * DSI utilities
 *
 * Copyright (C) 2004-2023 Sebastiano Vigna
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

import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;

/** A map from prefixes to string intervals (and possibly <i>vice versa</i>).
 *
 * <p>Instances of this class provide the services of a {@link StringMap}, but by assuming
 * the strings are lexicographically ordered, they can provide further information by
 * exposing a {@linkplain #rangeMap() function from string prefixes to intervals} and a
 * {@linkplain #prefixMap() function from intervals to string prefixes}.
 *
 * <p>In the first case, given a prefix, we can ask for the range of strings starting
 * with that prefix, expressed as an {@link Interval}. This information is very useful to
 * satisfy prefix queries (e.g., <code>monitor*</code>) with a brute-force approach.
 *
 * <P>Optionally, a prefix map may provide the opposite service: given an interval of terms, it
 *  may provide the maximum common prefix. This feature can be checked for by calling
 *  {@link #prefixMap()}.
 *
 * @author Sebastiano Vigna
 * @since 0.9.2
 */

public interface PrefixMap<S extends CharSequence> extends StringMap<S> {
	/** Returns a function mapping prefixes to ranges of strings.
	 *
	 * @return a function mapping prefixes to ranges of strings.
	 */
	Object2ObjectFunction<CharSequence, Interval> rangeMap();

	/** Returns a function mapping ranges of strings to common prefixes (optional operation).
	 *
	 * @return a function mapping ranges of strings to common prefixes, or {@code null} if this
	 * map does not support prefixes.
	 */
	Object2ObjectFunction<Interval, S> prefixMap();
}
