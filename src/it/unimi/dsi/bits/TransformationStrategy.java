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

import java.io.Serializable;

/** A generic transformation from objects of a given type to bit vector. Most useful
 * when adding strings, etc. to a trie.
 */

public interface TransformationStrategy<T> extends Serializable {
	/** Returns a bit vector representation of the given object.
	 *
	 * @param object the object to be turned into a bit-vector representation.
	 * @return a bit-vector representation of <code>object</code>.
	 */
	BitVector toBitVector(T object);

	/** The (approximate) number of bits occupied by this transformation.
	 *
	 * @return the (approximate) number of bits occupied by this transformation.
	 */
	long numBits();

	/** Returns a copy of this transformation strategy.
	 *
	 * @return a copy of this transformation strategy.
	 */
	TransformationStrategy<T> copy();

	/** Returns the length of the bit vector that would be computed by {@link #toBitVector(Object)}.
	 *
	 * <p>The <i>raison d'&ecirc;tre</i> of this method is that it is often easy to know
	 * the length of the representation without actually computing the representation.
	 *
	 * @param object the object whose representation length is to be known.
	 * @return the length of the bit-vector representation of <code>object</code> (the one that would be returned by {@link #toBitVector(Object)}).
	 */
	long length(T object);
}
