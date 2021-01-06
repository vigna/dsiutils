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

package it.unimi.dsi.bits;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class BooleanListBitVectorTest {

	@Test
	public void testSetClearFlip() {
		final BooleanListBitVector v = BooleanListBitVector.getInstance();
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
		final BooleanListBitVector v = BooleanListBitVector.getInstance();
		v.size(100);
		BitVectorTestCase.testFillFlip(v);
		BitVectorTestCase.testFillFlip(v.subVector(0, 90));
		BitVectorTestCase.testFillFlip(v.subVector(5, 90));
	}

	@Test
	public void testRemove() {
		BitVectorTestCase.testRemove(BooleanListBitVector.getInstance());
	}

	@Test
	public void testAdd() {
		BitVectorTestCase.testAdd(BooleanListBitVector.getInstance());
	}

	@Test
	public void testCopy() {
		BitVectorTestCase.testCopy(BooleanListBitVector.getInstance());
	}

	@Test
	public void testEquals2() {
		final BooleanListBitVector v = BooleanListBitVector.getInstance();
		v.clear();
		v.size(100);
		v.fill(5, 80, true);
		BooleanListBitVector w = v.copy();
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
		BitVectorTestCase.testBits(BooleanListBitVector.getInstance());
	}

	@Test
	public void testLongBigListView() {
		BitVectorTestCase.testLongBigListView(BooleanListBitVector.getInstance());
	}

	@Test
	public void testLongSetView() {
		BitVectorTestCase.testLongSetView(BooleanListBitVector.getInstance());
	}

	@Test
	public void testFirstLast() {
		BitVectorTestCase.testFirstLastPrefix(BooleanListBitVector.getInstance());
	}

	@Test
	public void testLogicOperators() {
		BitVectorTestCase.testLogicOperators(BooleanListBitVector.getInstance());
	}

	@Test
	public void testCount() {
		BitVectorTestCase.testCount(BooleanListBitVector.getInstance());
	}

	@Test
	public void testSerialisation() throws IOException, ClassNotFoundException {
		BitVectorTestCase.testSerialisation(BooleanListBitVector.getInstance());
	}

	@Test
	public void testReplace() {
		BitVectorTestCase.testReplace(BooleanListBitVector.getInstance());
	}

	@Test
	public void testAppend() {
		BitVectorTestCase.testAppend(BooleanListBitVector.getInstance());
	}
}
