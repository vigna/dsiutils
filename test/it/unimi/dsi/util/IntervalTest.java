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

import static it.unimi.dsi.util.Intervals.EMPTY_INTERVAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;

public class IntervalTest {

	@Test
	public void testLength() {
		assertEquals(0, EMPTY_INTERVAL.length());
		assertEquals(1, Interval.valueOf(0).length());
		assertEquals(2, Interval.valueOf(0, 1).length());
	}

	@Test
	public void testContainsElement() {
		assertTrue(Interval.valueOf(0).contains(0));
		assertFalse(Interval.valueOf(0).contains(1));
		assertFalse(Interval.valueOf(0).contains(-1));

		assertTrue(Interval.valueOf(0, 1).contains(0));
		assertTrue(Interval.valueOf(0, 1).contains(1));
		assertFalse(Interval.valueOf(0).contains(2));
		assertFalse(Interval.valueOf(0).contains(-1));

		assertFalse(EMPTY_INTERVAL.contains(0));
		assertFalse(EMPTY_INTERVAL.contains(1));
		assertFalse(EMPTY_INTERVAL.contains(-1));
	}

	@Test
	public void testContainsInterval() {
		assertTrue(Interval.valueOf(0).contains(Interval.valueOf(0)));
		assertTrue(Interval.valueOf(0).contains(EMPTY_INTERVAL));
		assertFalse(Interval.valueOf(0).contains(Interval.valueOf(0, 1)));
		assertFalse(Interval.valueOf(0).contains(Interval.valueOf(-1, 0)));

		assertTrue(Interval.valueOf(0, 1).contains(Interval.valueOf(0)));
		assertTrue(Interval.valueOf(0, 1).contains(Interval.valueOf(1)));
		assertTrue(Interval.valueOf(0, 1).contains(Interval.valueOf(0, 1)));
		assertTrue(Interval.valueOf(0, 1).contains(EMPTY_INTERVAL));
		assertFalse(Interval.valueOf(0).contains(Interval.valueOf(-1, 0)));
		assertFalse(Interval.valueOf(0).contains(Interval.valueOf(1, 2)));
		assertFalse(Interval.valueOf(0).contains(Interval.valueOf(-1, 2)));

		assertTrue(EMPTY_INTERVAL.contains(EMPTY_INTERVAL));
		assertFalse(EMPTY_INTERVAL.contains(Interval.valueOf(0)));
		assertFalse(EMPTY_INTERVAL.contains(Interval.valueOf(1)));
		assertFalse(EMPTY_INTERVAL.contains(Interval.valueOf(0, 1)));
	}

	@Test
	public void testContainsRadius() {
		boolean ok = false;
		try {
			EMPTY_INTERVAL.contains(0, 1);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertTrue(Interval.valueOf(0).contains(1, 1));
		assertFalse(Interval.valueOf(0).contains(2, 1));

		ok = false;
		try {
			EMPTY_INTERVAL.contains(0, 1, 2);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertTrue(Interval.valueOf(0).contains(1, 1, 2));
		assertTrue(Interval.valueOf(0).contains(2, 1, 2));
		assertFalse(Interval.valueOf(0).contains(3, 1, 2));
	}

	@Test
	public void testCompareInt() {
		boolean ok = false;
		try {
			EMPTY_INTERVAL.compareTo(0);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertEquals(-1, Interval.valueOf(0).compareTo(-1));
		assertEquals(0, Interval.valueOf(0).compareTo(0));
		assertEquals(1, Interval.valueOf(0).compareTo(1));
		assertEquals(-1, Interval.valueOf(0, 1).compareTo(-1));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(0));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(1));
		assertEquals(1, Interval.valueOf(0, 1).compareTo(2));

		ok = false;
		try {
			EMPTY_INTERVAL.compareTo(0, 1);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertEquals(-1, Interval.valueOf(0).compareTo(-2, 1));
		assertEquals(0, Interval.valueOf(0).compareTo(-1, 1));
		assertEquals(0, Interval.valueOf(0).compareTo(0, 1));
		assertEquals(0, Interval.valueOf(0).compareTo(1, 1));
		assertEquals(1, Interval.valueOf(0).compareTo(2, 1));

		assertEquals(-1, Interval.valueOf(0, 1).compareTo(-2, 1));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(-1, 1));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(0, 1));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(1, 1));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(2, 1));
		assertEquals(1, Interval.valueOf(0, 1).compareTo(3, 1));

		ok = false;
		try {
			EMPTY_INTERVAL.compareTo(0, 1, 2);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertEquals(-1, Interval.valueOf(0).compareTo(-2, 1, 2));
		assertEquals(0, Interval.valueOf(0).compareTo(-1, 1, 2));
		assertEquals(0, Interval.valueOf(0).compareTo(0, 1, 2));
		assertEquals(0, Interval.valueOf(0).compareTo(1, 1, 2));
		assertEquals(0, Interval.valueOf(0).compareTo(2, 1, 2));
		assertEquals(1, Interval.valueOf(0).compareTo(3, 1, 2));

		assertEquals(-1, Interval.valueOf(0, 1).compareTo(-2, 1, 2));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(-1, 1, 2));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(0, 1, 2));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(1, 1, 2));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(2, 1, 2));
		assertEquals(0, Interval.valueOf(0, 1).compareTo(3, 1, 2));
		assertEquals(1, Interval.valueOf(0, 1).compareTo(4, 1, 2));

	}

	private IntSortedSet toSortedSet(final Interval interval) {
		if (interval == EMPTY_INTERVAL) return IntSortedSets.EMPTY_SET;
		final IntSortedSet set = new IntRBTreeSet();
		for(int i = interval.left; i <= interval.right; i++) set.add(i);
		return set;
	}

	@Test
	public void testSubsets() {
		for(int i = 0; i < 10; i++)
			for(int j = i - 1; j < 10; j++) {
				final Interval interval = j < i ? EMPTY_INTERVAL : Interval.valueOf(i, j);
				final IntSortedSet set = toSortedSet(interval);
				assertEquals(set, interval);
				assertTrue(Arrays.equals(IntIterators.unwrap(set.iterator()), IntIterators.unwrap(interval.iterator())));
				assertEquals(new IntOpenHashSet(set), interval);
				for(int k = j - 1; k <= i + 1; k++) {
					assertTrue(Arrays.equals(IntIterators.unwrap(set.iterator(k)), IntIterators.unwrap(interval.iterator(k))));
					assertEquals(set.headSet(k), interval.headSet(k));
					assertEquals(set.tailSet(k), interval.tailSet(k));
					for(int l = k; l <= i + 1; l++)
						assertEquals(set.subSet(k, l), interval.subSet(k, l));
				}
			}
	}

}
