/*
 * DSI utilities
 *
 * Copyright (C) 2008-2021 Sebastiano Vigna
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

package it.unimi.dsi.big.util;

import java.io.Serializable;

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.ObjectBigList;

/** A map from strings to longs (and possibly <i>vice versa</i>).
 *
 * <p>String maps represent mappings from strings (actually, any subclass of {@link CharSequence})
 * to numbers; they can support {@linkplain #list() reverse
 * mapping}, too. The latter has usually sense only if the map is minimal and perfect (e.g., a bijection of a set
 * of string with an initial segment of the natural numbers of the same size). String maps are useful for
 * terms of an <a href="http://mg4j.di.unimi.it/">MG4J</a>
 * inverted index, URLs of a <a href="http://webgraph.di.unimi.it/">WebGraph</a>-compressed
 * web snapshot, and so on.
 *
 * @author Sebastiano Vigna
 * @since 2.0
 */

public interface StringMap<S extends CharSequence> extends Object2LongFunction<CharSequence>, Size64, Serializable {
	public static final long serialVersionUID = 0L;

	/** Returns a list view of the domain of this string map (optional operation).
	 *
	 * <p>Note that the list view acts as an inverse of the mapping implemented by this map.
	 *
	 * @return a list view of the domain of this string map, or {@code null} if this map does
	 * not support this operation.
	 */

	ObjectBigList<? extends S> list();

	/** Returns the intended number of keys in this function, or -1 if no such number exists.
	 *
	 * <p>Most function implementations will have some knowledge of the intended number of keys
	 * in their domain. In some cases, however, this might not be possible. This default
	 * implementation, in particular, returns -1.
	 *
	 *  @return the intended number of keys in this function, or -1 if that number is not available.
	 */
	@Override
	default long size64() {
		return -1;
	}

	/** {@inheritDoc}
	 * @deprecated Please use {@link #size64()} instead. */
	@Deprecated
	@Override
	default int size() {
		return (int) Math.min(Integer.MAX_VALUE, size64());
	}
}
