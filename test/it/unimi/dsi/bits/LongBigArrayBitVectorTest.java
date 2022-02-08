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

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.util.SplitMix64Random;

public class LongBigArrayBitVectorTest {

	@Test
	public void testSetClearFlip() {
		final LongBigArrayBitVector v = LongBigArrayBitVector.getInstance();
		v.size(1);
		BitVectorTestCase.testSetClearFlip(v);
		v.size(64);
		BitVectorTestCase.testSetClearFlip(v);
		v.size(80);
		BitVectorTestCase.testSetClearFlip(v);
		v.size(150);
		BitVectorTestCase.testSetClearFlip(v);

		BitVectorTestCase.testSetClearFlip(v.subVector(0, 90));
		BitVectorTestCase.testSetClearFlip(v.subVector(5, 90));
	}

	@Test
	public void testFillFlip() {
		final LongBigArrayBitVector v = LongBigArrayBitVector.getInstance();
		v.size(100);
		BitVectorTestCase.testFillFlip(v);
	}

	@Test
	public void testAdd() {
		final LongBigArrayBitVector v = LongBigArrayBitVector.getInstance();
		v.clear();
		v.add(true);
		assertTrue(v.getBoolean(0));
		v.add(true);
		assertTrue(v.getBoolean(0));
		assertTrue(v.getBoolean(1));
		v.add(false);
		assertTrue(v.getBoolean(0));
		assertTrue(v.getBoolean(1));
		assertFalse(v.getBoolean(2));
		v.set(1, false);
		assertTrue(v.getBoolean(0));
		assertFalse(v.getBoolean(1));
		assertFalse(v.getBoolean(2));
		v.set(1, true);
		assertTrue(v.getBoolean(0));
		assertTrue(v.getBoolean(1));
		assertFalse(v.getBoolean(2));

		v.clear();
		v.append(1, 2);
		v.append(1, 2);
		v.append(3, 2);
		assertEquals(LongArrayBitVector.of(1, 0, 1, 0, 1, 1), v);

		v.clear();
		for(int i = 0; i < 80; i++) v.add(true);
		for(int i = 0; i < 80; i++) assertTrue(v.getBoolean(i));

		v.clear();
		for(int i = 0; i < 80; i++) v.add(false);
		for(int i = 0; i < 80; i++) assertFalse(v.getBoolean(i));
	}

	@Test
	public void testCopy() {
		BitVectorTestCase.testCopy(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testEquals2() {
		final LongBigArrayBitVector v = LongBigArrayBitVector.getInstance();
		v.clear();
		v.size(100);
		v.fill(5, 80, true);
		LongBigArrayBitVector w = v.copy();
		assertTrue(w.equals(v, 0, 100));
		assertTrue(w.equals(v, 0, 64));
		assertTrue(w.equals(v, 64, 100));

		v.clear();
		v.size(1000);
		v.fill(5, 800, true);
		w.replace(v);
		assertTrue(w.equals(v, 0, 1000));
		assertTrue(w.equals(v, 0, 64));
		assertTrue(w.equals(v, 0, 500));
		assertTrue(w.equals(v, 128, 900));

		v.clear();
		v.size(100);
		v.fill(5, 80, true);
		w = v.copy();
		w.clear(30);
		w.clear(70);
		w.set(90);
		assertFalse(w.equals(v, 0, 100));
		assertFalse(w.equals(v, 0, 64));
		assertFalse(w.equals(v, 64, 100));
		assertFalse(w.equals(v, 65, 100));

		v.clear();
		v.size(1000);
		v.fill(5, 800, true);
		w.replace(v);
		w.clear(63);
		w.clear(128);
		w.clear(500);
		assertFalse(w.equals(v, 0, 1000));
		assertFalse(w.equals(v, 0, 64));
		assertFalse(w.equals(v, 0, 500));
		assertFalse(w.equals(v, 128, 900));
		assertFalse(w.equals(v, 129, 900));
		assertFalse(w.equals(v, 129, 511));
	}

	@Test
	public void testBits() {
		BitVectorTestCase.testBits(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testLongBigListView() {
		BitVectorTestCase.testLongBigListView(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testLongSetView() {
		BitVectorTestCase.testLongSetView(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testFirstLastPrefix() {
		final LongBigArrayBitVector b = LongBigArrayBitVector.getInstance();
		b.clear();
		b.length(60);
		assertEquals(-1, b.firstOne());
		assertEquals(-1, b.lastOne());
		b.flip();
		assertEquals(-1, b.firstZero());
		assertEquals(-1, b.lastZero());
		b.flip();

		b.set(4, true);
		assertEquals(4, b.firstOne());
		assertEquals(4, b.lastOne());

		b.flip();
		assertEquals(4, b.firstZero());
		assertEquals(4, b.lastZero());
		b.flip();

		b.set(50, true);
		assertEquals(4, b.firstOne());
		assertEquals(50, b.nextOne(5));
		assertEquals(50, b.lastOne());
		assertEquals(4, b.previousOne(50));

		b.flip();
		assertEquals(4, b.firstZero());
		assertEquals(50, b.nextZero(5));
		assertEquals(50, b.lastZero());
		assertEquals(4, b.previousZero(50));
		b.flip();

		b.set(20, true);
		assertEquals(4, b.firstOne());
		assertEquals(20, b.nextOne(5));
		assertEquals(50, b.nextOne(21));
		assertEquals(50, b.lastOne());
		assertEquals(20, b.previousOne(50));
		assertEquals(4, b.previousOne(20));

		b.flip();
		assertEquals(4, b.firstZero());
		assertEquals(20, b.nextZero(5));
		assertEquals(50, b.nextZero(21));
		assertEquals(50, b.lastZero());
		assertEquals(20, b.previousZero(50));
		assertEquals(4, b.previousZero(20));
		b.flip();

		b.length(100);
		b.set(90, true);
		assertEquals(4, b.firstOne());
		assertEquals(90, b.lastOne());

		b.flip();
		assertEquals(4, b.firstZero());
		assertEquals(90, b.lastZero());
		b.flip();

		b.clear();
		b.length(100);
		assertEquals(-1, b.firstOne());
		assertEquals(-1, b.lastOne());

		b.flip();
		assertEquals(-1, b.firstZero());
		assertEquals(-1, b.lastZero());
		b.flip();

		b.set(4, true);
		assertEquals(4, b.firstOne());
		assertEquals(4, b.lastOne());

		b.flip();
		assertEquals(4, b.firstZero());
		assertEquals(4, b.lastZero());
		b.flip();

		b.set(90, true);
		assertEquals(4, b.firstOne());
		assertEquals(90, b.lastOne());

		b.flip();
		assertEquals(4, b.firstZero());
		assertEquals(90, b.lastZero());
		b.flip();


		b.length(60);
		BitVector c = b.copy();
		c.length(40);
		assertEquals(c.length(), b.longestCommonPrefixLength(c));
		c.flip(20);
		assertEquals(20, b.longestCommonPrefixLength(c));
		c.flip(0);
		assertEquals(0, b.longestCommonPrefixLength(c));

		b.length(128).fill(false);
		b.set(127);
		c.length(65).fill(false);
		assertEquals(65, b.longestCommonPrefixLength(c));


		b.length(100);
		c = b.copy();
		c.length(80);
		assertEquals(c.length(), b.longestCommonPrefixLength(c));
		assertEquals(c.length(), b.longestCommonPrefixLength(BooleanListBitVector.wrap(c)));
		c.flip(20);
		assertEquals(20, b.longestCommonPrefixLength(c));
		assertEquals(20, b.longestCommonPrefixLength(BooleanListBitVector.wrap(c)));

		c.flip(0);
		assertEquals(0, b.longestCommonPrefixLength(c));
		assertEquals(0, b.longestCommonPrefixLength(BooleanListBitVector.wrap(c)));

		c.clear();
		c.length(2);
		c.set(0, 0);
		assertFalse(c.getBoolean(0));
		c.set(0, 1);
		assertTrue(c.getBoolean(0));
		c.set(0, 2);
		assertTrue(c.getBoolean(0));
		c.set(0, 0);
		assertFalse(c.getBoolean(0));
	}

	@Test
	public void testLogicOperators() {
		BitVectorTestCase.testLogicOperators(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testCount() {
		BitVectorTestCase.testCount(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testSerialisation() throws IOException, ClassNotFoundException {
		BitVectorTestCase.testSerialisation(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testReplace() {
		BitVectorTestCase.testReplace(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testGarbageInReplace() {
		final LongBigArrayBitVector b = LongBigArrayBitVector.ofLength(128);
		b.set(64);
		b.replace(BooleanListBitVector.getInstance().length(64));
		assertEquals(0, b.bits[0][1]);
	}

	@Test
	public void testHashCodeConsistency() {
		LongBigArrayBitVector b = LongBigArrayBitVector.of(0, 1, 1, 0, 0, 1);
		assertEquals(BooleanListBitVector.getInstance().replace(b).hashCode(), b.hashCode());
		b = LongBigArrayBitVector.wrap(new long[][]{ { 0x234598729872983L, 0x234598729872983L, 0x234598729872983L, 0xFFFF } }, 222);
		assertEquals(BooleanListBitVector.getInstance().replace(b).hashCode(), b.hashCode());
		assertEquals(BitVectors.EMPTY_VECTOR.hashCode(), b.length(0).hashCode());
	}

	@Test
	public void testAppend() {
		BitVectorTestCase.testAppend(LongBigArrayBitVector.getInstance());
	}

	@Test
	public void testTrim() {
		assertTrue(LongBigArrayBitVector.getInstance(100).trim());
		assertFalse(LongBigArrayBitVector.getInstance(100).length(65).trim());
		assertFalse(LongBigArrayBitVector.getInstance(0).trim());
	}

	@Test
	public void testClone() throws CloneNotSupportedException {
		final LongBigArrayBitVector v = LongBigArrayBitVector.getInstance().length(100);
		for(int i = 0; i < 50; i++) v.set(i * 2);
		assertEquals(v, v.clone());
	}

	@Test
	public void testEquals() {
		final LongBigArrayBitVector v = LongBigArrayBitVector.getInstance().length(100);
		for(int i = 0; i < 50; i++) v.set(i * 2);
		final LongBigArrayBitVector w = v.copy();
		assertEquals(v, w);
		w.length(101);
		assertFalse(v.equals(w));
		w.length(100);
		w.set(3);
		assertFalse(v.equals(w));
	}

	@Test
	public void testConstructor() {
		final long bits[][] = { { 0, 1, 0 } };

		boolean ok = false;
		try {
			LongBigArrayBitVector.wrap(bits, 64);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}

		assertTrue(ok);

		LongBigArrayBitVector.wrap(bits, 65);
		LongBigArrayBitVector.wrap(bits, 128);

		ok = false;
		try {
			LongBigArrayBitVector.wrap(bits, 193);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}

		assertTrue(ok);

		bits[0][0] = 10;
		bits[0][1] = 0;

		ok = false;
		try {
			LongBigArrayBitVector.wrap(bits, 3);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}

		assertTrue(ok);

		LongBigArrayBitVector.wrap(bits, 4);

		bits[0][2] = 1;

		ok = false;
		try {
			LongBigArrayBitVector.wrap(bits, 4);
		}
		catch(final IllegalArgumentException e) {
			ok = true;
		}

		assertTrue(ok);

	}

	@Test
	public void testLongBig() {
		final LongBigArrayBitVector v =  LongBigArrayBitVector.getInstance(16 * 1024);
		final LongBigList l = v.asLongBigList(Short.SIZE);
		l.size(1);
		l.set(0, 511);
		assertEquals(511, v.bits()[0]);
	}

	@Test
	public void testCopyAnotherVector() {
		final Random r = new SplitMix64Random(1);
		LongBigArrayBitVector bv = LongBigArrayBitVector.getInstance(200);
		for(int i = 0; i < 100; i++) bv.add(r.nextBoolean());
		assertEquals(LongBigArrayBitVector.copy(bv), bv);
		bv = LongBigArrayBitVector.getInstance(256);
		for(int i = 0; i < 256; i++) bv.add(r.nextBoolean());
		assertEquals(LongBigArrayBitVector.copy(bv), bv);
		bv = LongBigArrayBitVector.getInstance(10);
		for(int i = 0; i < 10; i++) bv.add(r.nextBoolean());
		assertEquals(LongBigArrayBitVector.copy(bv), bv);
		BooleanListBitVector bbv = BooleanListBitVector.getInstance(200);
		for(int i = 0; i < 100; i++) bbv.add(r.nextBoolean());
		assertEquals(LongBigArrayBitVector.copy(bbv), bbv);
		bbv = BooleanListBitVector.getInstance(256);
		for(int i = 0; i < 256; i++) bbv.add(r.nextBoolean());
		assertEquals(LongBigArrayBitVector.copy(bbv), bbv);
		bbv = BooleanListBitVector.getInstance(10);
		for(int i = 0; i < 10; i++) bbv.add(r.nextBoolean());
		assertEquals(LongBigArrayBitVector.copy(bbv), bbv);
	}

	@Test
	public void testReplaceLongBigArrayBitVector() {
		final LongBigArrayBitVector b = LongBigArrayBitVector.of(0, 1, 1);
		assertEquals(b, LongBigArrayBitVector.getInstance().replace(b));
	}

	@Test
	public void testLengthClearsBits() {
		final LongBigArrayBitVector bv = LongBigArrayBitVector.getInstance().length(100);
		bv.fill(true);
		bv.length(0);
		bv.append(0, 1);
		assertFalse(bv.getBoolean(0));
	}

	@Test
	public void testNextOne() {
		final LongBigArrayBitVector bv = LongBigArrayBitVector.ofLength(Integer.MAX_VALUE + 3L);
		bv.set(Integer.MAX_VALUE + 2L);
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextOne(0));
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextOne(Integer.MAX_VALUE + 1L));
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextOne(Integer.MAX_VALUE + 2L));
	}


	@Test
	public void testPreviousOne() {
		final LongBigArrayBitVector bv = LongBigArrayBitVector.ofLength(Integer.MAX_VALUE + 70L);
		bv.set(Integer.MAX_VALUE + 1L);
		assertEquals(-1, bv.previousOne(Integer.MAX_VALUE + 1L));
		assertEquals(Integer.MAX_VALUE + 1L, bv.previousOne(Integer.MAX_VALUE + 2L));
		assertEquals(Integer.MAX_VALUE + 1L, bv.previousOne(Integer.MAX_VALUE + 69L));
	}

	@Test
	public void testNextZero() {
		final LongBigArrayBitVector bv = LongBigArrayBitVector.ofLength(Integer.MAX_VALUE + 3L);
		bv.set(Integer.MAX_VALUE + 2L);
		bv.flip();
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextZero(0));
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextZero(1));
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextZero(Integer.MAX_VALUE + 1L));
		assertEquals(Integer.MAX_VALUE + 2L, bv.nextZero(Integer.MAX_VALUE + 2L));
		assertEquals(-1, bv.nextZero(Integer.MAX_VALUE + 3L));
		bv.length(Integer.MAX_VALUE + 1L);
		assertEquals(-1, bv.nextZero(Integer.MAX_VALUE + 1L - 64));
	}


	@Test
	public void testPreviousZero() {
		final LongBigArrayBitVector bv = LongBigArrayBitVector.ofLength(Integer.MAX_VALUE + 70L);
		bv.set(Integer.MAX_VALUE + 1L);
		bv.flip();
		assertEquals(-1, bv.previousZero(0));
		assertEquals(-1, bv.previousZero(1));
		assertEquals(-1, bv.previousZero(Integer.MAX_VALUE + 1L));
		assertEquals(Integer.MAX_VALUE + 1L, bv.previousZero(Integer.MAX_VALUE + 2L));
		assertEquals(Integer.MAX_VALUE + 1L, bv.previousZero(Integer.MAX_VALUE + 69L));
	}
}
