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

package it.unimi.dsi.big.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import it.unimi.dsi.lang.MutableString;

public class FrontCodedStringBigListTest {

	@Test
	public void test() {
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
}
