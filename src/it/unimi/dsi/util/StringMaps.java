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

import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunctions;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

/** A class providing static methods and objects that do useful things with {@linkplain StringMap string maps}
 * and {@linkplain PrefixMap prefix maps}.
 *
 * @see StringMap
 * @see PrefixMap
 * @author Sebastiano Vigna
 */

public class StringMaps {
	private StringMaps() {}

	protected static class SynchronizedStringMap<S extends CharSequence> implements StringMap<S>, Serializable {
		private static final long serialVersionUID = 1L;
		protected final StringMap<S> stringMap;
		protected ObjectList<? extends S> list;

		public SynchronizedStringMap(final StringMap<S> stringMap) {
			this.stringMap = stringMap;
		}

		@Override
		public synchronized int size() {
			return stringMap.size();
		}

		@Override
		public synchronized ObjectList<? extends S> list() {
			if (list == null) {
				list = stringMap.list();
				if(list != null) list = ObjectLists.synchronize(list, this);
			}
			return list;
		}

		@Override
		public synchronized long getLong(final Object s) {
			return stringMap.getLong(s);
		}

		@SuppressWarnings("deprecation")
		@Override
		public synchronized Long get(final Object key) {
			return stringMap.get(key);
		}

		@Override
		public synchronized long put(final CharSequence key, final long value) {
			return stringMap.put(key, value);
		}

		@SuppressWarnings("deprecation")
		@Override
		public synchronized Long put(final CharSequence key, final Long value) {
			return stringMap.put(key, value);
		}

		@SuppressWarnings("deprecation")
		@Override
		public synchronized Long remove(final Object key) {
			return stringMap.remove(key);
		}

		@Override
		public synchronized long removeLong(final Object key) {
			return stringMap.removeLong(key);
		}

		@Override
		public synchronized void clear() {
			stringMap.clear();
		}

		@Override
		public synchronized boolean containsKey(final Object key) {
			return stringMap.containsKey(key);
		}

		@Override
		public synchronized long defaultReturnValue() {
			return stringMap.defaultReturnValue();
		}

		@Override
		public synchronized void defaultReturnValue(final long rv) {
			stringMap.defaultReturnValue(rv);
		}
	}


	protected static class SynchronizedPrefixMap<S extends CharSequence> extends SynchronizedStringMap<S> implements PrefixMap<S>, Serializable {
		private static final long serialVersionUID = 1L;
		protected final PrefixMap<S> map;
		protected Object2ObjectFunction<Interval, S> prefixMap;
		protected Object2ObjectFunction<CharSequence, Interval> rangeMap;

		public SynchronizedPrefixMap(final PrefixMap<S> map) {
			super(map);
			this.map = map;
		}

		@Override
		public synchronized Object2ObjectFunction<Interval, S> prefixMap() {
			if (prefixMap == null) {
				prefixMap = map.prefixMap();
				if (prefixMap != null) prefixMap = Object2ObjectFunctions.synchronize(prefixMap, this);
			}
			return prefixMap;
		}

		@Override
		public synchronized Object2ObjectFunction<CharSequence, Interval> rangeMap() {
			if (rangeMap == null) {
				rangeMap = map.rangeMap();
				if (rangeMap != null) rangeMap = Object2ObjectFunctions.synchronize(rangeMap, this);
			}
			return rangeMap;
		}


	}

	/** Returns a synchronized string map backed by the given string map.
     *
     * @param stringMap the string map to be wrapped in a synchronized map.
     * @return a synchronized view of the specified string map.
     */
	public static <T extends CharSequence> StringMap<T> synchronize(final StringMap<T> stringMap) {
		return stringMap instanceof PrefixMap ? new SynchronizedPrefixMap<>((PrefixMap<T>)stringMap) : new SynchronizedStringMap<>(stringMap);
	}

	/** Returns a synchronized prefix map backed by the given prefix map.
    *
    * @param prefixMap the prefix map to be wrapped in a synchronized map.
    * @return a synchronized view of the specified prefix map.
    */
	public static <T extends CharSequence> PrefixMap<T> synchronize(final PrefixMap<T> prefixMap) {
		return new SynchronizedPrefixMap<>(prefixMap);
	}
}
