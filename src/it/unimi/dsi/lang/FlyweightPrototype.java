/*
 * DSI utilities
 *
 * Copyright (C) 2006-2022 Sebastiano Vigna
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

package it.unimi.dsi.lang;

/** A prototype providing flyweight copies.
 *
 * <p><em>Flyweight copies</em> are useful to implement multithreading on read-only
 * (but maybe stateful) classes. An instance of a class implementing this interface
 * is not necessarily thread safe,
 * but it can be (thread-) safely copied many times (i.e., it can be used as a prototype).
 * All copies will share as much as possible of the class read-only
 * state (so they are flyweight).
 *
 * <p>In the case an implementation is stateless, it can of course return always the same singleton
 * instance as a copy. At the other extreme, a stateful class may decide to synchronise its
 * methods and return itself as a copy instead. Note that in general the object returned
 * by {@link #copy()} must replicate the <em>current state</em> of the object, not
 * the object state at creation time. This might require some calls to methods that
 * modify the class internal state: in particular, one should always check whether such
 * methods are pointed out in the documentation of superclasses.
 *
 * <p><strong>Warning</strong>: if {@link #copy()} accesses mutable internal state, setters
 * and {@link #copy()} must be suitably synchronised.
 *
 * <p>Implementing subclasses are invited to use covariant return-type overriding to
 * make {@link #copy()} return the right type.
 */

public interface FlyweightPrototype<T extends FlyweightPrototype<T>> {

	/** Returns a copy of this object, sharing state with this object as much as possible.
	 *
	 * @return  a copy of this object, sharing state with this object as much as possible. */
	public T copy();
}
