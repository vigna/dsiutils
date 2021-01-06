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

package it.unimi.dsi.big.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SplittableRandom;

import org.junit.Ignore;
import org.junit.Test;

import it.unimi.dsi.lang.MutableString;

public class FrontCodedStringBigListTest {

	@Test
	public void testLargeSet() {
		final List<String> c = Arrays.asList(TernaryIntervalSearchTreeTest.WORDS.clone());
		final MutableString s = new MutableString();
		for(int p = 0; p < 2; p++) {
			for(final boolean utf8: new boolean[] { false, true })
				for(int ratio = 1; ratio < 8; ratio++) {
					final FrontCodedStringBigList fcl = new FrontCodedStringBigList(c.iterator(), ratio, utf8);
					for (int i = 0; i < fcl.size64(); i++) {
						assertEquals(Integer.toString(i), c.get(i), fcl.get(i).toString());
						fcl.get(i, s);
						assertEquals(Integer.toString(i), c.get(i), s.toString());
					}
				}

			Collections.sort(c);
		}
	}

	@Test
	public void testSurrogatePairs() {
		final List<String> c = Arrays.asList(new String[] { "a", "AB\uE000AB", "\uD800\uDF02", "\uD800\uDF03", "b" });
		for(final boolean utf8: new boolean[] { false, true })
			for(int ratio = 1; ratio < 8; ratio++) {
				final FrontCodedStringBigList fcl = new FrontCodedStringBigList(c.iterator(), ratio, utf8);
				for (int i = 0; i < fcl.size64(); i++) {
					assertEquals(Integer.toString(i), c.get(i), fcl.get(i).toString());
				}
			}
	}

	@Ignore("Needs a lot of memory")
	@Test
	public void testbig() {
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
			assertEquals(new String(new byte[] { (byte)r.nextLong() }, StandardCharsets.ISO_8859_1), byteArrayFrontCodedBigList.get(i));
		}
		r = new SplittableRandom(0);
		final MutableString s = new MutableString();
		for (long i = 0; i < size; i++) {
			byteArrayFrontCodedBigList.get(i, s);
			assertEquals(new String(new byte[] { (byte)r.nextLong() }, StandardCharsets.ISO_8859_1), s);
		}
	}

}
