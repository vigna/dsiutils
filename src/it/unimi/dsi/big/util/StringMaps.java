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

import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunctions;
import it.unimi.dsi.fastutil.objects.ObjectBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigLists;
import it.unimi.dsi.util.Interval;
import it.unimi.dsi.util.LongInterval;

/** A class providing static methods and objects that do useful things with {@linkplain StringMap string maps}
 * and {@linkplain PrefixMap prefix maps}.
 *
 * @see StringMap
 * @see PrefixMap
 * @author Sebastiano Vigna
 * @since 2.0
 */

public class StringMaps {
	private StringMaps() {}

	protected static class SynchronizedStringMap<S extends CharSequence> implements StringMap<S>, Serializable {
		private static final long serialVersionUID = 1L;
		protected final StringMap<S> stringMap;
		protected ObjectBigList<? extends S> list;

		public SynchronizedStringMap(final StringMap<S> stringMap) {
			this.stringMap = stringMap;
		}

		@Override
		public synchronized long size64() {
			return stringMap.size64();
		}

		@Override
		public synchronized ObjectBigList<? extends S> list() {
			if (list == null) {
				list = stringMap.list();
				if(list != null) list = ObjectBigLists.synchronize(list, this);
			}
			return list;
		}

		@Override
		public synchronized long getLong(final Object s) {
			return stringMap.getLong(s);
		}

		@Override
		@SuppressWarnings("deprecation")
		public synchronized Long get(final Object key) {
			return stringMap.get(key);
		}

		@Override
		public synchronized long put(final CharSequence key, final long value) {
			return stringMap.put(key, value);
		}

		@Override
		@SuppressWarnings("deprecation")
		public synchronized Long put(final CharSequence key, final Long value) {
			return stringMap.put(key, value);
		}

		@Override
		@SuppressWarnings("deprecation")
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
		protected Object2ObjectFunction<LongInterval, S> prefixMap;
		protected Object2ObjectFunction<CharSequence, LongInterval> rangeMap;

		public SynchronizedPrefixMap(final PrefixMap<S> map) {
			super(map);
			this.map = map;
		}

		@Override
		public synchronized Object2ObjectFunction<LongInterval, S> prefixMap() {
			if (prefixMap == null) {
				prefixMap = map.prefixMap();
				if (prefixMap != null) prefixMap = Object2ObjectFunctions.synchronize(prefixMap, this);
			}
			return prefixMap;
		}

		@Override
		public synchronized Object2ObjectFunction<CharSequence, LongInterval> rangeMap() {
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

	protected static class StringMapWrapper<T extends CharSequence> extends AbstractObject2LongFunction<CharSequence> implements StringMap<T> {
		private static final long serialVersionUID = 1L;
		private final it.unimi.dsi.util.StringMap<T> stringMap;

		public StringMapWrapper(final it.unimi.dsi.util.StringMap<T> stringMap) {
			this.stringMap = stringMap;
		}

		@Override
		public long getLong(final Object key) {
			return stringMap.getLong(key);
		}

		@Override
		public boolean containsKey(final Object key) {
			return stringMap.containsKey(key);
		}

		@Override
		public long size64() {
			return stringMap.size();
		}

		@Override
		public ObjectBigList<? extends T> list() {
			return ObjectBigLists.asBigList(stringMap.list());
		}
	}

	/** Returns an immutable (big) {@link StringMap} view of a standard {@link it.unimi.dsi.util.StringMap}.
	 *
	 * @param stringMap a string map.
	 * @return a {@link StringMap} view of {@code stringMap}.
	 */

	public static <T extends CharSequence> StringMap<T> wrap(final it.unimi.dsi.util.StringMap<T> stringMap) {
		return new StringMapWrapper<>(stringMap);
	}

	protected static class PrefixMapWrapper<T extends CharSequence> extends StringMapWrapper<T> implements PrefixMap<T> {
		private static final long serialVersionUID = 1L;
		private final Object2ObjectFunction<CharSequence, LongInterval> rangeMap;

		public PrefixMapWrapper(final it.unimi.dsi.util.PrefixMap<T> prefixMap) {
			super(prefixMap);
			rangeMap = new AbstractObject2ObjectFunction<CharSequence, LongInterval>() {
				private static final long serialVersionUID = 1L;
				private final Object2ObjectFunction<CharSequence, Interval> prefixMapRangeMap = prefixMap.rangeMap();

				@Override
				public LongInterval get(final Object key) {
					final Interval interval = prefixMapRangeMap.get(key);
					return LongInterval.valueOf(interval.left, interval.right);
				}

				@Override
				public boolean containsKey(final Object key) {
					return prefixMapRangeMap.containsKey(key);
				}

				@Override
				public int size() {
					return prefixMapRangeMap.size();
				}
			};
		}

		@Override
		public Object2ObjectFunction<CharSequence, LongInterval> rangeMap() {
			return rangeMap;
		}

		@Override
		public Object2ObjectFunction<LongInterval, T> prefixMap() {
			return null;
		}
	}

	/** Returns an immutable (big) {@link PrefixMap} view of a standard {@link it.unimi.dsi.util.PrefixMap}. Note that
	 * the returned prefix map does not implement {@link PrefixMap#prefixMap()}.
	 *
	 * @param prefixMap a prefix map.
	 * @return a {@link PrefixMap} view of {@code prefixMap}.
	 */

	public static <T extends CharSequence> PrefixMap<T> wrap(final it.unimi.dsi.util.PrefixMap<T> prefixMap) {
		return new PrefixMapWrapper<>(prefixMap);
	}
}
