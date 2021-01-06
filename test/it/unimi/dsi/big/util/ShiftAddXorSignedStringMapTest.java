/*
 * DSI utilities
 *
 * Copyright (C) 2002-2021 Sebastiano Vigna
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.AbstractObjectBigList;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class ShiftAddXorSignedStringMapTest {

	@SuppressWarnings("deprecation")
	@Test
	public void testNumbers() throws IOException, ClassNotFoundException {

		for(int width = 16; width <= Long.SIZE; width += 8) {
			final String[] s = new String[1000];
			final long[] v = new long[s.length];
			for(int i = s.length; i-- != 0;) s[(int)(v[i] = i)] = Integer.toString(i);

			// Test with mph
			final Object2LongOpenHashMap<String> mph = new Object2LongOpenHashMap<>(s, v);
			ShiftAddXorSignedStringMap map = new ShiftAddXorSignedStringMap(Arrays.asList(s).iterator(), mph, width);

			for(int i = s.length; i-- != 0;) assertEquals(i, map.getLong(Integer.toString(i)));
			for(int i = s.length + 100; i-- != s.length;) assertEquals(-1, map.getLong(Integer.toString(i)));

			final File temp = File.createTempFile(getClass().getSimpleName(), "test");
			temp.deleteOnExit();
			BinIO.storeObject(map, temp);
			map = (ShiftAddXorSignedStringMap)BinIO.loadObject(temp);

			for(int i = s.length; i-- != 0;) assertEquals(i, map.getLong(Integer.toString(i)));
			for(int i = s.length + 100; i-- != s.length;) assertEquals(-1, map.getLong(Integer.toString(i)));

		}
	}

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
