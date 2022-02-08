/*
 * DSI utilities
 *
 * Copyright (C) 2007-2022 Sebastiano Vigna
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

import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectFunction;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.lang.MutableString;

/**
 * An abstract implementation of a prefix map.
 *
 * <p>
 * This class provides the full services of a {@link PrefixMap} by implementing just
 * {@link #getInterval(CharSequence)} and {@link #getTerm(int, MutableString)}
 */

public abstract class AbstractPrefixMap extends AbstractObject2LongFunction<CharSequence> implements PrefixMap<MutableString>, Serializable {
	private static final long serialVersionUID = 1L;
	/** A cached view of the map as a range map. */
	protected Object2ObjectFunction<CharSequence, Interval> rangeMap;
	/** A cached view of the map as a prefix map. */
	protected Object2ObjectFunction<Interval, MutableString> prefixMap;
	/** A cached view of the map as a list of mutable strings. */
	protected ObjectList<MutableString> list;

	// We must guarantee that, unless the user says otherwise, the default return value is -1.
	{
		defaultReturnValue(-1);
	}
	/** Returns the range of strings having a given prefix.
	 *
	 * @param prefix a prefix.
	 * @return the corresponding range of strings as an interval.
	 */
	protected abstract Interval getInterval(CharSequence prefix);
	/** Writes a string specified by index into a {@link MutableString}.
	 *
	 * @param index the index of a string.
	 * @param string a mutable string.
	 * @return <code>string</code>.
	 */
	protected abstract MutableString getTerm(int index, MutableString string);

	@Override
	public Object2ObjectFunction<CharSequence, Interval> rangeMap() {
		if (rangeMap == null) rangeMap = new AbstractObject2ObjectFunction<CharSequence, Interval>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean containsKey(final Object o) {
				return get(o) != Intervals.EMPTY_INTERVAL;
			}

			@Override
			public int size() {
				return -1;
			}

			@Override
			public Interval get(final Object o) {
				return getInterval((CharSequence)o);
			}

		};

		return rangeMap;
	}

	@Override
	public Object2ObjectFunction<Interval, MutableString> prefixMap() {
		if (prefixMap == null) prefixMap = new AbstractObject2ObjectFunction<Interval, MutableString>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MutableString get(final Object o) {
				final Interval interval = (Interval)o;
				final MutableString prefix = new MutableString();
				if (interval == Intervals.EMPTY_INTERVAL || interval.left < 0 || interval.right < 0) throw new IllegalArgumentException();
				getTerm(interval.left, prefix);
				if (interval.length() == 1) return prefix;
				final MutableString s = getTerm(interval.right, new MutableString());
				final int l = Math.min(prefix.length(), s.length());
				int i;
				for(i = 0; i < l; i++) if (s.charAt(i) != prefix.charAt(i)) break;
				return prefix.length(i);
			}

			@Override
			public boolean containsKey(final Object o) {
				final Interval interval = (Interval)o;
				return interval != Intervals.EMPTY_INTERVAL && interval.left >= 0 && interval.right < AbstractPrefixMap.this.size();
			}

			@Override
			public int size() {
				return -1;
			}
		};

		return prefixMap;
	}

	@Override
	public ObjectList<MutableString> list() {
		if (list == null) list = new AbstractObjectList<MutableString>() {
			@Override
			public int size() {
				return AbstractPrefixMap.this.size();
			}
			@Override
			public MutableString get(final int index) {
				return getTerm(index, new MutableString());
			}
		};

		return list;
	}
}
