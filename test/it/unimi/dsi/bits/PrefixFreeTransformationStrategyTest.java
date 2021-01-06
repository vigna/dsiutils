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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PrefixFreeTransformationStrategyTest {

	@Test
	public void testGetBoolean() {
		final LongArrayBitVector v = LongArrayBitVector.of(0, 1, 0);
		final TransformationStrategy<BitVector> prefixFree = TransformationStrategies.prefixFree();
		final BitVector p = prefixFree.toBitVector(v);
		assertTrue(p.getBoolean(0));
		assertFalse(p.getBoolean(1));
		assertTrue(p.getBoolean(2));
		assertTrue(p.getBoolean(3));
		assertTrue(p.getBoolean(4));
		assertFalse(p.getBoolean(5));
		assertFalse(p.getBoolean(6));
		assertEquals(LongArrayBitVector.of(1, 0, 1, 1, 1, 0, 0), p);
	}

	@Test
	public void testGetLong() {
		LongArrayBitVector v = LongArrayBitVector.getInstance();
		v.append(0xFFFFFFFFL, 32);
		final TransformationStrategy<BitVector> prefixFree = TransformationStrategies.prefixFree();
		final BitVector p = prefixFree.toBitVector(v);
		assertEquals(0xFFFFFFFFFFFFFFFFL, p.getLong(0, 64));
		assertFalse(p.getBoolean(64));
		assertEquals(0, p.getLong(64, 64));

		v.clear();
		v.append(0x0, 32);
		assertEquals(0x5555555555555555L, p.getLong(0, 64));
		assertEquals(0x5555555555555555L >>> 1, p.getLong(1, 64));
		assertFalse(p.getBoolean(64));

		v.clear();
		v.append(0x3, 32);
		assertEquals(0x555555555555555FL, p.getLong(0, 64));
		assertEquals(0x5FL, p.getLong(0, 7));

		v = LongArrayBitVector.of(0, 0, 0, 0, 1, 1, 1);
		assertEquals(LongArrayBitVector.of(1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0), prefixFree.toBitVector(v));
	}

}
