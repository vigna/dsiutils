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

package it.unimi.dsi.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.util.SplitMix64Random;

public class BitVectorTestCase {

	public static void testSetClearFlip(final BitVector v) {
		final long length = v.length();
		for(long i = length; i-- != 0;) {
			v.set(i);
			assertTrue(v.getBoolean(i));
		}

		for(long i = length; i-- != 0;) {
			v.clear(i);
			assertFalse(v.getBoolean(i));
		}

		for(long i = length; i-- != 0;) {
			v.set(i);
			v.flip(i);
			assertFalse(v.getBoolean(i));
			v.flip(i);
			assertTrue(v.getBoolean(i));
		}

		v.set(0, true);
		assertTrue(v.set(0, true));
		assertTrue(v.set(0, false));

		assertFalse(v.set(0, false));
		assertFalse(v.set(0, true));
	}


	public static void testRemove(final BitVector v) {
		v.clear();
		v.size(100);
		v.set(0, true);
		assertTrue(v.removeBoolean(0));
		assertFalse(v.getBoolean(0));

		v.clear();
		v.size(100);
		v.set(63, true);
		v.set(64, true);
		assertTrue(v.removeBoolean(63));
		assertTrue(v.getBoolean(63));
		assertFalse(v.getBoolean(64));
		assertFalse(v.getBoolean(0));
	}

	public static void testAdd(final BitVector v) {
		v.clear();
		v.size(100);
		v.add(0, true);
		assertTrue(v.getBoolean(0));
		v.add(0, true);
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
		v.size(100);
		v.add(0, 1);
		assertEquals(1, v.getInt(0));
		v.add(0, 2);
		assertEquals(1, v.getInt(0));
		assertEquals(1, v.getInt(1));
		v.add(0, 0);
		assertEquals(0, v.getInt(0));
		assertEquals(1, v.getInt(1));
		assertEquals(1, v.getInt(2));
		v.add(0);
		assertEquals(0, v.getInt(0));
		assertEquals(1, v.getInt(1));
		assertEquals(1, v.getInt(2));
		assertEquals(0, v.getInt(3));
		v.set(1, 0);
		assertEquals(0, v.getInt(0));
		assertEquals(0, v.getInt(1));
		assertEquals(1, v.getInt(2));
		assertEquals(0, v.getInt(3));
		v.set(1, 1);
		assertEquals(0, v.getInt(0));
		assertEquals(1, v.getInt(1));
		assertEquals(1, v.getInt(2));
		assertEquals(0, v.getInt(3));

		v.clear();
		v.append(1, 2);
		v.append(1, 2);
		v.append(3, 2);
		assertEquals(LongArrayBitVector.of(1, 0, 1, 0, 1, 1), v);


		v.clear();
		for(int i = 0; i < 80; i++) v.add(0, true);
		for(int i = 0; i < 80; i++) assertTrue(v.getBoolean(i));

		v.clear();
		for(int i = 0; i < 80; i++) v.add(0, false);
		for(int i = 0; i < 80; i++) assertFalse(v.getBoolean(i));
	}

	public static void testFillFlip(final BitVector v) {

		v.clear();
		v.size(100);
		v.fill(true);
		for(long i = v.length(); i-- != 0;) assertTrue(v.getBoolean(i));
		v.fill(false);
		for(long i = v.length(); i-- != 0;) assertFalse(v.getBoolean(i));
		v.flip();
		for(long i = v.length(); i-- != 0;) assertTrue(v.getBoolean(i));

		v.clear();
		v.size(100);
		v.fill(1);
		for(long i = v.length(); i-- != 0;) assertTrue(v.getBoolean(i));
		v.fill(0);
		for(long i = v.length(); i-- != 0;) assertFalse(v.getBoolean(i));

		v.clear();
		v.size(100);
		v.fill(5, 70, true);
		for(long i = v.length(); i-- != 0;) assertTrue(Long.toString(i), (i >= 5 && i < 70) == v.getBoolean(i));
		v.fill(true);
		v.fill(5, 70, false);
		for(long i = v.length(); i-- != 0;) assertTrue(Long.toString(i), (i < 5 || i >= 70) == v.getBoolean(i));

		v.clear();
		v.size(100);
		v.fill(5, 70, 1);
		for(long i = v.length(); i-- != 0;) assertTrue(Long.toString(i), (i >= 5 && i < 70) == v.getBoolean(i));
		v.fill(true);
		v.fill(5, 70, 0);
		for(long i = v.length(); i-- != 0;) assertTrue((i < 5 || i >= 70) == v.getBoolean(i));

		v.clear();
		v.size(100);
		v.flip(5, 70);
		for(long i = v.length(); i-- != 0;) assertTrue(Long.toString(i), (i >= 5 && i < 70) == v.getBoolean(i));
		v.fill(true);
		v.flip(5, 70);
		for(long i = v.length(); i-- != 0;) assertTrue((i < 5 || i >= 70) == v.getBoolean(i));
		v.fill(false);
		v.flip(5, 30);
		for(long i = v.length(); i-- != 0;) assertTrue((i >= 5 && i < 30) == v.getBoolean(i));

		v.clear();
		v.size(200);
		v.flip(5, 190);
		for(long i = v.length(); i-- != 0;) assertTrue(Long.toString(i), (i >= 5 && i < 190) == v.getBoolean(i));
		v.fill(true);
		v.flip(5, 190);
		for(long i = v.length(); i-- != 0;) assertTrue((i < 5 || i >= 190) == v.getBoolean(i));
	}


	public static void testCopy(final BitVector v) {
		v.clear();
		v.size(100);
		v.fill(5, 80, true);
		BitVector w = v.copy(0, 85);
		assertEquals(w, v.subVector(0, 85).copy());

		for(long i = w.length(); i-- != 0;) assertTrue((i >= 5 && i < 80) == w.getBoolean(i));
		w = v.copy(5, 85);
		assertEquals(w, v.subVector(5, 85).copy());
		for(long i = w.length(); i-- != 0;) assertTrue((i < 75) == w.getBoolean(i));


		v.clear();
		int[] bits = { 0,0,0,0,1,1,1,0,0,0,0,1,1,0,0 };
		for (final int bit : bits) v.add(bit);

		LongArrayBitVector c = LongArrayBitVector.getInstance();
		for(int i = 5; i < bits.length; i++) c.add(bits[i]);

		assertEquals(c, v.copy(5, 15));
		assertEquals(c, v.subVector(5, 15).copy());

		v.clear();
		bits = new int[] { 0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0 };
		for (final int bit : bits) v.add(bit);
		c = LongArrayBitVector.getInstance();
		for(int i = 5; i < bits.length - 2; i++) c.add(bits[i]);

		assertEquals(c, v.copy(5, bits.length - 2));

		assertEquals(v, v.copy());

		final long[] words = new long[] { 0xDEADBEEFDEADF00DL, 0xDEADBEEFDEADF00DL, 0xDEADBEEFDEADF00DL, };
		final long[] copyWords16 = new long[] { 0xF00DDEADBEEFDEADL, 0xF00DDEADBEEFDEADL, 0xBEEFDEADL };
		final long[] copyWords32 = new long[] { 0xDEADF00DDEADBEEFL, 0xDEADF00DDEADBEEFL };

		LongArrayBitVector.wrap(copyWords16, 64 * 2 + 32).equals(LongArrayBitVector.wrap(words, 64 * 3).copy(16, 16 + 64 * 2 + 32));
		assertEquals(LongArrayBitVector.wrap(copyWords16, 64 * 2 + 32), LongArrayBitVector.wrap(words, 64 * 3).copy(16, 16 + 64 * 2 + 32));

		copyWords16[2] &= 0xFFFF;
		assertEquals(LongArrayBitVector.wrap(copyWords16, 64 * 2 + 16), LongArrayBitVector.wrap(words, 64 * 3).copy(16, 16 + 64 * 2 + 16));
		copyWords16[2] &= 0xFF;
		assertEquals(LongArrayBitVector.wrap(copyWords16, 64 * 2 + 8), LongArrayBitVector.wrap(words, 64 * 3).copy(16, 16 + 64 * 2 + 8));
		copyWords16[2] &= 0x1F;
		assertEquals(LongArrayBitVector.wrap(copyWords16, 64 * 2 + 5), LongArrayBitVector.wrap(words, 64 * 3).copy(16, 16 + 64 * 2 + 5));
		copyWords16[2] &= 0x1;
		assertEquals(LongArrayBitVector.wrap(copyWords16, 64 * 2 + 1), LongArrayBitVector.wrap(words, 64 * 3).copy(16, 16 + 64 * 2 + 1));


		copyWords32[1] &= 0xFFFFFFFFFFFFL;
		assertEquals(LongArrayBitVector.wrap(copyWords32, 64 * 1 + 32 + 16), LongArrayBitVector.wrap(words, 64 * 3).copy(32, 32 + 64 + 32 + 16));
		copyWords32[1] &= 0xFFFFFFFFFFL;
		assertEquals(LongArrayBitVector.wrap(copyWords32, 64 * 1 + 32 + 8), LongArrayBitVector.wrap(words, 64 * 3).copy(32, 32 + 64 + 32 + 8));
		copyWords32[1] &= 0x1FFFFFFFFFL;
		assertEquals(LongArrayBitVector.wrap(copyWords32, 64 * 1 + 32 + 5), LongArrayBitVector.wrap(words, 64 * 3).copy(32, 32 + 64 + 32 + 5));
		copyWords32[1] &= 0x1FFFFFFFFL;
		assertEquals(LongArrayBitVector.wrap(copyWords32, 64 * 1 + 32 + 1), LongArrayBitVector.wrap(words, 64 * 3).copy(32, 32 + 64 + 32 + 1));
	}

	public static void testBits(final BitVector b) {
		for(int i = 0; i < 100; i++) b.add(i % 2);
		assertTrue(LongArrayBitVector.wrap(b.bits(), b.length()).toString(), Arrays.equals(new long[] { 0xAAAAAAAAAAAAAAAAL, 0x0000000AAAAAAAAAL }, b.bits()));
	}

	public static void testLongBigListView(final BitVector b) {
		final LongBigList list = b.asLongBigList(10);
		for(int i = 0; i < 100; i++) list.add(i);
		for(int i = 0; i < 100; i++) assertEquals(i, list.getLong(i));

		assertTrue(b.getBoolean(10));
		assertTrue(b.getBoolean(21));

		for(int i = 0; i < 100; i++) list.add(i);
		for(int i = 0; i < 100; i++) {
			assertEquals(i, list.set(i, i + 1));
			for(int j = i + 1; j < 100; j++) assertEquals("" + i , j, list.getLong(j));
		}
		for(int i = 0; i < 100; i++) assertEquals(i + 1, list.getLong(i));

		assertTrue(b.getBoolean(0));
		assertTrue(b.getBoolean(11));

		list.size(100);
		int k = 0;
		final LongBigListIterator i = list.listIterator();
		while(i.hasNext()) {
			assertEquals(k, i.nextIndex());
			assertEquals(++k, i.nextLong());
		}
		while(i.hasPrevious()) {
			assertEquals(k - 1, i.previousIndex());
			assertEquals(k--, i.previousLong());
		}
	}

	public static void testLongSetView(final BitVector b) {
		final LongSortedSet s = b.asLongSet();
		assertNull(s.comparator());

		for(int i = 0; i < 100; i++) s.add(i * 2);
		for(int i = 0; i < 100; i++) assertTrue(s.contains(i * 2));
		for(int i = 0; i < 100; i++) assertFalse(s.contains(i * 2 + 1));

		LongBidirectionalIterator iterator = s.iterator();
		for(int i = 0; i < 100; i++) assertEquals(i * 2, iterator.nextLong());
		assertFalse(iterator.hasNext());
		for(int i = 100; i-- != 0;) assertEquals(i * 2, iterator.previousLong());
		assertFalse(iterator.hasPrevious());

		assertEquals(100, s.size());
		assertFalse(s.remove(1));
		assertEquals(100, s.size());
		assertTrue(s.remove(0));
		assertEquals(99, s.size());
		assertTrue(s.remove(60));
		assertEquals(98, s.size());
		assertFalse(s.remove(1000));
		assertEquals(98, s.size());
		assertFalse(s.add(2));
		assertEquals(98, s.size());

		assertEquals(2, s.firstLong());
		assertEquals(198, s.lastLong());

		assertEquals(18, s.subSet(3, 40).size());
		assertEquals(19, s.headSet(40).size());
		assertEquals(5, s.tailSet(190).size());

		iterator = s.iterator();
		assertEquals(2, iterator.nextLong());
		iterator.remove();
		assertFalse(s.contains(2));

		s.clear();
		assertNull(s.comparator());

		for(int i = 0; i < 100; i++) s.add(i);
		for(int i = 0; i < 100; i++) assertTrue(s.contains(i));

		iterator = s.iterator();
		for(int i = 0; i < 100; i++) assertEquals(i, iterator.nextLong());
		assertFalse(iterator.hasNext());
		for(int i = 100; i-- != 0;) assertEquals(i, iterator.previousLong());
		assertFalse(iterator.hasPrevious());

	}

	public static void testFirstLastPrefix(final BitVector b) {
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
		c.add(0, 0);
		assertFalse(c.getBoolean(0));
		c.add(0, 1);
		assertTrue(c.getBoolean(0));
	}

	public static void testLogicOperators(final BitVector b) {
		b.clear();
		b.length(100);
		final BitVector c = b.copy();
		for(int i = 0; i < 50; i++) b.set(2 * i);
		for(int i = 0; i < 50; i++) c.set(2 * i + 1);
		BitVector r;

		r = b.copy().and(c);
		for(int i = 0; i < 100; i++) assertFalse(r.getBoolean(i));
		r = b.copy().or(c);
		for(int i = 0; i < 100; i++) assertTrue(r.getBoolean(i));
		r = b.copy().xor(c);
		for(int i = 0; i < 100; i++) assertTrue(r.getBoolean(i));
		r.xor(r);
		for(int i = 0; i < 100; i++) assertFalse(r.getBoolean(i));

		r = b.copy().and(BooleanListBitVector.wrap(c));
		for(int i = 0; i < 100; i++) assertFalse(r.getBoolean(i));
		r = b.copy().or(BooleanListBitVector.wrap(c));
		for(int i = 0; i < 100; i++) assertTrue(r.getBoolean(i));
		r = b.copy().xor(BooleanListBitVector.wrap(c));
		for(int i = 0; i < 100; i++) assertTrue(r.getBoolean(i));
		r.xor(BooleanListBitVector.wrap(r));
		for(int i = 0; i < 100; i++) assertFalse(r.getBoolean(i));
	}

	public static void testCount(final BitVector b) {
		b.clear();
		b.length(100);
		for(int i = 0; i < 50; i++) b.set(i * 2);
		assertEquals(50, b.count());
	}

	public static void testSerialisation(final BitVector b) throws IOException, ClassNotFoundException {
		final File file = File.createTempFile(BitVectorTestCase.class.getSimpleName(), "test");
		file.deleteOnExit();

		b.clear();

		BinIO.storeObject(b, file);
		assertEquals(b, BinIO.loadObject(file));

		b.length(1000);

		BinIO.storeObject(b, file);
		assertEquals(b, BinIO.loadObject(file));

		b.fill(true);

		BinIO.storeObject(b, file);
		assertEquals(b, BinIO.loadObject(file));
	}

	public static void testReplace(final BitVector b) {
		final Random r = new SplitMix64Random(1);
		LongArrayBitVector bv = LongArrayBitVector.getInstance(200);
		for(int i = 0; i < 100; i++) bv.add(r.nextBoolean());
		assertEquals(b.replace(bv), bv);
		bv = LongArrayBitVector.getInstance(256);
		for(int i = 0; i < 256; i++) bv.add(r.nextBoolean());
		assertEquals(b.replace(bv), bv);
		bv = LongArrayBitVector.getInstance(10);
		for(int i = 0; i < 10; i++) bv.add(r.nextBoolean());
		assertEquals(b.replace(bv), bv);
	}


	public static void testAppend(final BitVector b) {
		b.clear();
		final LongArrayBitVector v = LongArrayBitVector.ofLength(200);
		for(int i = 0; i < 60; i++) v.set(i * 3);
		b.append(v);
		assertEquals(b, v);
		b.clear();
		b.add(true);
		b.append(v);
		final LongArrayBitVector w = LongArrayBitVector.ofLength(201);
		for(int i = 0; i < 60; i++) w.set(i * 3 + 1);
		w.set(0);
		assertEquals(w, b);

		b.clear();
		for (int i = 0; i < 128; i++) b.append(1, 1);
		assertEquals(128, b.length());
		for (int i = 0; i < 128; i++) assertTrue(b.getBoolean(i));
	}

}
