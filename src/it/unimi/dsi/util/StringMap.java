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

package it.unimi.dsi.util;

import java.io.Serializable;

import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.ObjectList;

/** A map from strings to numbers (and possibly <i>vice versa</i>).
 *
 * <p>String maps represent mappings from strings (actually, any subclass of {@link CharSequence})
 * to numbers; they can support {@linkplain #list() reverse
 * mapping}, too. The latter has usually sense only if the map is minimal and perfect (e.g., a bijection of a set
 * of string with an initial segment of the natural numbers of the same size). String maps are useful for
 * terms of an <a href="http://mg4j.di.unimi.it/">MG4J</a>
 * inverted index, URLs of a <a href="http://webgraph.di.unimi.it/">WebGraph</a>-compressed
 * web snapshot, and so on.
 *
 * <p><strong>Warning</strong>: the return value of {@link #list()} is a <code>fastutil</code> {@link ObjectList}.
 * This in principle is not sensible, as string maps return longs (they extend
 * {@link Object2LongFunction}), and {@link ObjectList} has only integer index
 * support. If you need long indices, please consider using {@link it.unimi.dsi.big.util.StringMap}.
 *
 * @author Sebastiano Vigna
 * @since 0.2
 */

public interface StringMap<S extends CharSequence> extends Object2LongFunction<CharSequence>, Serializable {

	/** Returns a list view of the domain of this string map (optional operation).
	 *
	 * <p>Note that the list view acts as an inverse of the mapping implemented by this map.
	 *
	 * @return a list view of the domain of this string map, or {@code null} if this map does
	 * not support this operation.
	 */

	ObjectList<? extends S> list();
}
