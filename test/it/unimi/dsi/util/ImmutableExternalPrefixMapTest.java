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

package it.unimi.dsi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;


public class ImmutableExternalPrefixMapTest {

	public void test(final int blockSize) throws IOException {
		final Collection<String> c = Arrays.asList(TernaryIntervalSearchTreeTest.WORDS);
		TernaryIntervalSearchTree t = new TernaryIntervalSearchTree(c);
		ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap(c, blockSize);

		for (final String element : TernaryIntervalSearchTreeTest.WORDS) assertTrue(element, d.containsKey(element));
		for(int i = 0; i < TernaryIntervalSearchTreeTest.WORDS.length; i++) assertEquals(TernaryIntervalSearchTreeTest.WORDS[i], d.list().get(i).toString());
		for (final String element : TernaryIntervalSearchTreeTest.WORDS) for(int j = 0; j < element.length(); j++) {
			String s = element.substring(0, j + 1);
			assertEquals(s, t.rangeMap().get(s), d.getInterval(s));
			s = s + " ";
			assertEquals(s, t.rangeMap().get(s), d.getInterval(s));
			s = s.substring(0, s.length() - 1) + "~";
			assertEquals(s, t.rangeMap().get(s), d.getInterval(s));
		}

		// Similar tests, using all prefixes of all strings in WORDS.
		final Collection<String> p = new ObjectRBTreeSet<>();
		for (final String element : TernaryIntervalSearchTreeTest.WORDS) for(int j = 0; j < element.length(); j++)
			p.add(element.substring(0, j + 1));

		d = new ImmutableExternalPrefixMap(p, blockSize);
		t = new TernaryIntervalSearchTree(p);

		int j = 0;
		for (final String s : p) {
			assertTrue(s, d.containsKey(s));
			assertTrue(s, d.containsKey(s));
			assertEquals(s, d.list().get(j++).toString());
			assertEquals(s, t.rangeMap().get(s), d.getInterval(s));
		}

		final Iterator<CharSequence> k = d.iterator();
		for(final Iterator<String> i = p.iterator(); i.hasNext();) {
			assertTrue(i.hasNext() == k.hasNext());
			assertEquals(i.next().toString(), k.next().toString());
		}

		// Test negatives
		for(long i = 1000000000000L; i < 1000000002000L; i++) assertEquals(-1, d.getLong(Long.toBinaryString(i)));

	}

	@Test
	public void test64() throws IOException {
		test(64);
	}

	@Test
	public void test128() throws IOException {
		test(128);
	}

	@Test
	public void test256() throws IOException {
		test(256);
	}

	@Test
	public void test1024() throws IOException {
		test(1024);
	}

	@Test
	public void test16384() throws IOException {
		test(16384);
	}

	@Test
	public void testPrefixes() throws IOException {
		final ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap(new ObjectLinkedOpenHashSet<CharSequence>(new String[] { "ab", "ba", "bb" }));
		assertEquals(Interval.valueOf(1, 2), d.getInterval("b"));
	}

	@Test
	public void testLargeRootPrefixes() throws IOException {
		final ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap(new ObjectLinkedOpenHashSet<CharSequence>(new String[] { "aab", "aac", "aad" }), 2);
		assertEquals(Interval.valueOf(0, 2), d.getInterval(""));
		assertEquals(Interval.valueOf(0, 2), d.getInterval("aa"));
		assertEquals(Interval.valueOf(0, 2), d.getInterval("aa"));
	}

	@Test
	public void testSingleton() throws IOException {
		final ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap(ObjectSets.singleton("a"), 1024);
		assertTrue(d.containsKey("a"));
		assertFalse(d.containsKey("b"));
		assertFalse(d.containsKey("0"));
	}

	@Test
	public void testPrefixOutOfRange() throws IOException {
		final ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap(new ObjectLinkedOpenHashSet<CharSequence>(new String[] { "ab", "ac" }));
		assertEquals(Intervals.EMPTY_INTERVAL, d.getInterval("b"));
		assertEquals(Interval.valueOf(0, 1), d.getInterval("a"));
	}
}
