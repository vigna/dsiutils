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

package it.unimi.dsi.big.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.SplittableRandom;

import org.junit.Test;

import it.unimi.dsi.lang.MutableString;

public class FrontCodedStringBigListSlowTest {
	@Test
	public void testLarge() {
		final long size = (1L << 31) + 10000;
		final FrontCodedStringBigList byteArrayFrontCodedBigList = new FrontCodedStringBigList(new Iterator<String>() {
			SplittableRandom r = new SplittableRandom(0);
			long i = 0;

			@Override
			public boolean hasNext() {
				return i < size;
			}

			@Override
			public String next() {
				i++;
				return new String(new byte[] { (byte)r.nextLong() }, StandardCharsets.ISO_8859_1);
			}
		}, 10, true);
		SplittableRandom r = new SplittableRandom(0);
		for (long i = 0; i < size; i++) {
			assertEquals(new String(new byte[] { (byte)r.nextLong() }, StandardCharsets.ISO_8859_1), byteArrayFrontCodedBigList.get(i).toString());
		}
		r = new SplittableRandom(0);
		final MutableString s = new MutableString();
		for (long i = 0; i < size; i++) {
			byteArrayFrontCodedBigList.get(i, s);
			assertEquals(new String(new byte[] { (byte)r.nextLong() }, StandardCharsets.ISO_8859_1), s.toString());
		}
	}

}
