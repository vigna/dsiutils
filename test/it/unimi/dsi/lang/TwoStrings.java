/*
 * DSI utilities
 *
 * Copyright (C) 2010-2022 Sebastiano Vigna
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

import java.util.Objects;

public class TwoStrings {
	private final String a;
	private final String b;
	private final Object context;
	public void test() {}

	public TwoStrings(final String a, final String b) {
		this(null, a, b);
	}

	public TwoStrings(final String... a) {
		this(null, a);
	}

	public static TwoStrings getInstance(final String a) {
		return new TwoStrings(a, a);
	}

	public static TwoStrings getInstance(final String... a) {
		return getInstance(Integer.toString(a.length));
	}

	public TwoStrings(final Object context, final String a, final String b) {
		this.a = a;
		this.b = b;
		this.context = context;
	}

	public TwoStrings(final Object context, final String... a) {
		this.a = a[0];
		this.b = Integer.toString(a.length);
		this.context = context;
	}

	public static TwoStrings getInstance(final Object context, final String a) {
		return new TwoStrings(context, a, a);
	}

	public static TwoStrings getInstance(final Object context, final String... a) {
		return getInstance(context, Integer.toString(a.length));
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final TwoStrings other = (TwoStrings)obj;
		if (a == null) {
			if (other.a != null) return false;
		}
		else if (!a.equals(other.a)) return false;
		if (b == null) {
			if (other.b != null) return false;
		}
		else if (!b.equals(other.b)) return false;
		if (context == null) {
			if (other.context != null) return false;
		}
		else if (!context.equals(other.context)) return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + context + ", " + a + ", " + b + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, context);
	}
}
