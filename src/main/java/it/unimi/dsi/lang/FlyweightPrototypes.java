/*
 * DSI utilities
 *
 * Copyright (C) 2006-2023 Paolo Boldi and Sebastiano Vigna
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

import java.lang.reflect.Array;

/** A class providing static methods and objects that do useful things
 * with {@linkplain FlyweightPrototype flyweight protoypes}.
 */

public class FlyweightPrototypes {

	protected FlyweightPrototypes() {}

	/** Creates a flyweight copy of an array of {@linkplain FlyweightPrototype flyweight prototypes}.
	 *
	 * @param <T> the type of {@link FlyweightPrototype} you want to copy, that is, the
	 * type of the elements of <code>prototype</code>.
	 * @param prototype an array of prototypes.
	 * @return a flyweight copy of <code>prototype</code>, obtained by invoking
	 * {@link FlyweightPrototype#copy()} on each element.
	 */

	@SuppressWarnings("unchecked")
	public static <T extends FlyweightPrototype<T>> T[] copy(final T[] prototype) {
		final T[] result = (T[])Array.newInstance(prototype.getClass().getComponentType(), prototype.length);
		for(int i = 0; i < result.length; i++) result[i] = prototype[i].copy();
		return result;
	}

	/** Creates a flyweight copy of the given object, or returns {@code null} if the given object is {@code null}.
	 *
	 * @param <T> the type of {@link FlyweightPrototype} you want to copy, that is, the
	 * type of <code>prototype</code>.
	 * @param prototype a prototype to be copied, or {@code null}.
	 * @return {@code null}, if <code>prototype</code> is {@code null};
	 * otherwise,a flyweight copy of <code>prototype</code>.
	 */
	@SuppressWarnings("null")
	public static <T extends FlyweightPrototype<T>> T copy(final T prototype) {
		return prototype != null ? prototype.copy() : null;
	}
}
