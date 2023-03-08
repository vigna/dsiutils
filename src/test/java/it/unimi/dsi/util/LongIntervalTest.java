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

import static it.unimi.dsi.util.LongIntervals.EMPTY_INTERVAL;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.LongSortedSets;

public class LongIntervalTest {

	@Test
	public void testLength() {
		assertEquals(0, EMPTY_INTERVAL.length());
		assertEquals(1, LongInterval.valueOf(0).length());
		assertEquals(2, LongInterval.valueOf(0, 1).length());
	}

	@Test
	public void testContainsElement() {
		assertTrue(LongInterval.valueOf(0).contains(0));
		assertFalse(LongInterval.valueOf(0).contains(1));
		assertFalse(LongInterval.valueOf(0).contains(-1));

		assertTrue(LongInterval.valueOf(0, 1).contains(0));
		assertTrue(LongInterval.valueOf(0, 1).contains(1));
		assertFalse(LongInterval.valueOf(0).contains(2));
		assertFalse(LongInterval.valueOf(0).contains(-1));

		assertFalse(EMPTY_INTERVAL.contains(0));
		assertFalse(EMPTY_INTERVAL.contains(1));
		assertFalse(EMPTY_INTERVAL.contains(-1));
	}

	@Test
	public void testContainsInterval() {
		assertTrue(LongInterval.valueOf(0).contains(LongInterval.valueOf(0)));
		assertTrue(LongInterval.valueOf(0).contains(EMPTY_INTERVAL));
		assertFalse(LongInterval.valueOf(0).contains(LongInterval.valueOf(0, 1)));
		assertFalse(LongInterval.valueOf(0).contains(LongInterval.valueOf(-1, 0)));

		assertTrue(LongInterval.valueOf(0, 1).contains(LongInterval.valueOf(0)));
		assertTrue(LongInterval.valueOf(0, 1).contains(LongInterval.valueOf(1)));
		assertTrue(LongInterval.valueOf(0, 1).contains(LongInterval.valueOf(0, 1)));
		assertTrue(LongInterval.valueOf(0, 1).contains(EMPTY_INTERVAL));
		assertFalse(LongInterval.valueOf(0).contains(LongInterval.valueOf(-1, 0)));
		assertFalse(LongInterval.valueOf(0).contains(LongInterval.valueOf(1, 2)));
		assertFalse(LongInterval.valueOf(0).contains(LongInterval.valueOf(-1, 2)));

		assertTrue(EMPTY_INTERVAL.contains(EMPTY_INTERVAL));
		assertFalse(EMPTY_INTERVAL.contains(LongInterval.valueOf(0)));
		assertFalse(EMPTY_INTERVAL.contains(LongInterval.valueOf(1)));
		assertFalse(EMPTY_INTERVAL.contains(LongInterval.valueOf(0, 1)));
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
		assertTrue(LongInterval.valueOf(0).contains(1, 1));
		assertFalse(LongInterval.valueOf(0).contains(2, 1));

		ok = false;
		try {
			EMPTY_INTERVAL.contains(0, 1, 2);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertTrue(LongInterval.valueOf(0).contains(1, 1, 2));
		assertTrue(LongInterval.valueOf(0).contains(2, 1, 2));
		assertFalse(LongInterval.valueOf(0).contains(3, 1, 2));
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
		assertEquals(-1, LongInterval.valueOf(0).compareTo(-1));
		assertEquals(0, LongInterval.valueOf(0).compareTo(0));
		assertEquals(1, LongInterval.valueOf(0).compareTo(1));
		assertEquals(-1, LongInterval.valueOf(0, 1).compareTo(-1));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(0));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(1));
		assertEquals(1, LongInterval.valueOf(0, 1).compareTo(2));

		ok = false;
		try {
			EMPTY_INTERVAL.compareTo(0, 1);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertEquals(-1, LongInterval.valueOf(0).compareTo(-2, 1));
		assertEquals(0, LongInterval.valueOf(0).compareTo(-1, 1));
		assertEquals(0, LongInterval.valueOf(0).compareTo(0, 1));
		assertEquals(0, LongInterval.valueOf(0).compareTo(1, 1));
		assertEquals(1, LongInterval.valueOf(0).compareTo(2, 1));

		assertEquals(-1, LongInterval.valueOf(0, 1).compareTo(-2, 1));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(-1, 1));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(0, 1));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(1, 1));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(2, 1));
		assertEquals(1, LongInterval.valueOf(0, 1).compareTo(3, 1));

		ok = false;
		try {
			EMPTY_INTERVAL.compareTo(0, 1, 2);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}
		assertTrue(ok);
		assertEquals(-1, LongInterval.valueOf(0).compareTo(-2, 1, 2));
		assertEquals(0, LongInterval.valueOf(0).compareTo(-1, 1, 2));
		assertEquals(0, LongInterval.valueOf(0).compareTo(0, 1, 2));
		assertEquals(0, LongInterval.valueOf(0).compareTo(1, 1, 2));
		assertEquals(0, LongInterval.valueOf(0).compareTo(2, 1, 2));
		assertEquals(1, LongInterval.valueOf(0).compareTo(3, 1, 2));

		assertEquals(-1, LongInterval.valueOf(0, 1).compareTo(-2, 1, 2));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(-1, 1, 2));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(0, 1, 2));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(1, 1, 2));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(2, 1, 2));
		assertEquals(0, LongInterval.valueOf(0, 1).compareTo(3, 1, 2));
		assertEquals(1, LongInterval.valueOf(0, 1).compareTo(4, 1, 2));

	}

	private LongSortedSet toSortedSet(final LongInterval interval) {
		if (interval == EMPTY_INTERVAL) return LongSortedSets.EMPTY_SET;
		final LongSortedSet set = new LongRBTreeSet();
		for(long i = interval.left; i <= interval.right; i++) set.add(i);
		return set;
	}

	@Test
	public void testSubsets() {
		for(int i = 0; i < 10; i++)
			for(int j = i - 1; j < 10; j++) {
				final LongInterval interval = j < i ? EMPTY_INTERVAL : LongInterval.valueOf(i, j);
				final LongSortedSet set = toSortedSet(interval);
				assertEquals(set, interval);
				assertArrayEquals(LongIterators.unwrap(set.iterator()), LongIterators.unwrap(interval.iterator()));
				assertEquals(new LongOpenHashSet(set), interval);
				for(int k = j - 1; k <= i + 1; k++) {
					assertArrayEquals(LongIterators.unwrap(set.iterator(k)), LongIterators.unwrap(interval.iterator(k)));
					assertEquals(set.headSet(k), interval.headSet(k));
					assertEquals(set.tailSet(k), interval.tailSet(k));
					for(int l = k; l <= i + 1; l++)
						assertEquals(set.subSet(k, l), interval.subSet(k, l));
				}
			}
	}

}
