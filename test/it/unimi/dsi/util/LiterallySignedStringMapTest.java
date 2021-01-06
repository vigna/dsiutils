/*
 * DSI utilities
 *
 * Copyright (C) 2010-2021 Sebastiano Vigna
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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.lang.MutableString;

public class LiterallySignedStringMapTest {

	private final static class CharSequenceStrategy implements Hash.Strategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean equals(final CharSequence a, final CharSequence b) {
			if (a == null) return b == null;
			if (b == null) return false;
			return a.toString().equals(b.toString());
		}

		@Override
		public int hashCode(final CharSequence o) {
			return o.toString().hashCode();
		}
	}

	@Test
	public void testNumbers() throws IOException, ClassNotFoundException {
		for(int n = 10; n < 10000; n *= 10) {
			final String[] s = new String[n];
			for(int i = s.length; i-- != 0;) s[i] = Integer.toString(i);
			Collections.shuffle(Arrays.asList(s));

			final FrontCodedStringList fcl = new FrontCodedStringList(Arrays.asList(s), 8, true);
			// Test with mph
			final Object2LongOpenCustomHashMap<CharSequence> mph = new Object2LongOpenCustomHashMap<>(new CharSequenceStrategy());
			mph.defaultReturnValue(-1);
			for(int i = 0; i < s.length; i++) mph.put(new MutableString(s[i]),  i);

			LiterallySignedStringMap map = new LiterallySignedStringMap(mph, fcl);

			for(int i = s.length; i-- != 0;) assertEquals(i, map.getLong(s[i]));
			for(int i = s.length + n; i-- != s.length;) assertEquals(-1, map.getLong(Integer.toString(i)));

			final File temp = File.createTempFile(getClass().getSimpleName(), "test");
			temp.deleteOnExit();
			BinIO.storeObject(map, temp);
			map = (LiterallySignedStringMap)BinIO.loadObject(temp);

			for(int i = s.length; i-- != 0;) assertEquals(i, map.getLong(s[i]));
			for(int i = s.length + n; i-- != s.length;) assertEquals(-1, map.getLong(Integer.toString(i)));
		}
	}
}
