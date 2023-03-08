/*
 * DSI utilities
 *
 * Copyright (C) 2002-2023 Sebastiano Vigna
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

import org.junit.Test;

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.AbstractObjectBigList;

public class ShiftAddXorSignedStringMapSlowTest {
	private final class LargeFunction extends AbstractObject2LongFunction<String> implements Size64 {
		private static final long serialVersionUID = 1L;

		@Override
		public long getLong(final Object key) {
			try {
				final long l = Long.parseLong((String)key);
				return l < 1L << 31 ? l : -1;
			}
			catch(final Exception e) {
				return -1;
			}
		}

		@Override
		public boolean containsKey(final Object key) {
			try {
				final long l = Long.parseLong((String)key);
				return l < 1L << 31;
			}
			catch(final Exception e) {
				return false;
			}
		}

		@Override
		@Deprecated
		public int size() {
			return Integer.MAX_VALUE;
		}

		@Override
		public long size64() {
			return 1L << 31;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLarge() {
		new ShiftAddXorSignedStringMap(new AbstractObjectBigList<String>() {

			@Override
			public String get(final long index) {
				return Long.toString(index);
			}

			@Override
			public long size64() {
				return 1L << 31;
			}
		}.iterator(), new LargeFunction(), 1);
	}

}
