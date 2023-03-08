/*
 * DSI utilities
 *
 * Copyright (C) 2010-2023 Sebastiano Vigna
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import it.unimi.dsi.fastutil.io.BinIO;
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
}
