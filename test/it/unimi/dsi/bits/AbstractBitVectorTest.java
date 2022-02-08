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

package it.unimi.dsi.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.booleans.BooleanBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

public class AbstractBitVectorTest {

	private final static class MinimalAlternatingBitVector extends AbstractBitVector {
		private long length = 129;

		@Override
		public boolean getBoolean(final long index) { return index % 2 != 0; }
		@Override
		public long length() { return length; }

		@Override
		public MinimalAlternatingBitVector length(final long newLength) {
			this.length  = newLength;
			return this;
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUnsupported() {
		final BitVector v = new MinimalAlternatingBitVector();

		v.getBoolean(0);
		v.length();

		boolean ok = false;
		try {
			v.removeBoolean(0);
		}
		catch(final UnsupportedOperationException e) {
			ok = true;
		}

		assertTrue(ok);

		ok = false;
		try {
			v.set(0, 0);
		}
		catch(final UnsupportedOperationException e) {
			ok = true;
		}

		assertTrue(ok);

		ok = false;
		try {
			v.add(0, 0);
		}
		catch(final UnsupportedOperationException e) {
			ok = true;
		}

		assertTrue(ok);

		v.length(1L<<32);

		ok = false;
		try {
			v.size();
		}
		catch(final IllegalStateException e) {
			ok = true;
		}

		assertTrue(ok);

		ok = false;
		try {
			v.asLongBigList(1).size();
		}
		catch(final IllegalStateException e) {
			ok = true;
		}

		assertTrue(ok);
	}

	@Test
	public void testCopy() {
		assertEquals(new MinimalAlternatingBitVector(), new MinimalAlternatingBitVector().copy());
		assertEquals(new MinimalAlternatingBitVector().subVector(2, 20), new MinimalAlternatingBitVector().subVector(2, 20).copy());
		assertEquals(new MinimalAlternatingBitVector().subVector(5, 12), new MinimalAlternatingBitVector().subVector(2, 20).subVector(3, 10));
		assertEquals(new MinimalAlternatingBitVector().subVector(5, 12), new MinimalAlternatingBitVector().subVector(2, 20).subVector(3, 10).copy());
		assertEquals(new MinimalAlternatingBitVector().subList(2, 20), new BooleanBigArrayBigList(new MinimalAlternatingBitVector().subList(2, 20)));
		assertEquals(new MinimalAlternatingBitVector().subList(5, 12), new MinimalAlternatingBitVector().subList(2, 20).subList(3, 10));
		assertEquals(new MinimalAlternatingBitVector().subList(5, 12), new BooleanBigArrayBigList(new MinimalAlternatingBitVector().subList(2, 20).subList(3, 10)));
	}

	@Test
	public void testCount() {
		final MinimalAlternatingBitVector v = new MinimalAlternatingBitVector();
		assertEquals(v.length() / 2, v.count());
	}

	@Test
	public void testRemove() {
		BitVectorTestCase.testRemove(new AbstractBitVector.SubBitVector(BooleanListBitVector.getInstance().length(1000), 10, 100));
	}

	@Test
	public void testAdd() {
		BitVectorTestCase.testAdd(new AbstractBitVector.SubBitVector(BooleanListBitVector.getInstance().length(1000), 10, 100));
	}

	@Test
	public void testCompareTo() {
		final MinimalAlternatingBitVector v = new MinimalAlternatingBitVector();
		LongArrayBitVector w = LongArrayBitVector.copy(v);
		assertEquals(0, w.compareTo(v));
		assertEquals(0, v.compareTo(w));
		w.set(100);
		assertEquals(1, w.compareTo(v));
		assertEquals(-1, v.compareTo(w));
		w = LongArrayBitVector.ofLength(10);
		assertEquals(-1, w.compareTo(v));
		assertEquals(1, v.compareTo(w));
		w = LongArrayBitVector.of(1);
		assertEquals(1, w.compareTo(v));
		assertEquals(-1, v.compareTo(w));
	}

	@Test
	public void testSize64() {
		final MinimalAlternatingBitVector v = new MinimalAlternatingBitVector();
		v.length(1L << 32);
		assertEquals(1L << 32, v.size64());
		assertEquals(1L << 31, ((Size64)v.asLongSet()).size64());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLongSetView() {
		final LongArrayBitVector v = LongArrayBitVector.ofLength(1000);
		assertTrue(v.asLongSet().add(1000));
		assertEquals(1001, v.length());
		v.set(1);
		final LongSortedSet s = v.subVector(1, 500).asLongSet();
		assertFalse(s.contains(500));
		assertTrue(s.contains(0));
		v.subVector(1, 500).asLongSet().add(500);
	}
}
