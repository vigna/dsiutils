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

import it.unimi.dsi.lang.MutableString;

public class RawISOTransformationStrategyTest {

	@Test
	public void testCharacterSequence() {
		String a = new String(new char[] { 0x55, 0xFF });
		assertEquals(16, TransformationStrategies.rawIso().toBitVector(a).length());
		//System.err.println(Long.toHexString(TransformationStrategies.rawIso().toBitVector(a).getLong(0, 16)));
		assertEquals(0xFF55L, TransformationStrategies.rawIso().toBitVector(a).getLong(0, 16));

		a = new String(new char[] { 1, 0, 0, 0, 0, 0, 0, 0, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, 0 });
		assertTrue(TransformationStrategies.rawIso().toBitVector(a).getBoolean(0));
		assertFalse(TransformationStrategies.rawIso().toBitVector(a).getBoolean(1));
		assertTrue(TransformationStrategies.rawIso().toBitVector(a).getBoolean(64));
		assertEquals(0x1L, TransformationStrategies.rawIso().toBitVector(a).getLong(0, 56));
		assertEquals(0x1L, TransformationStrategies.rawIso().toBitVector(a).getLong(0, 64));
		assertEquals(-1L, TransformationStrategies.rawIso().toBitVector(a).getLong(64, 128));

		for(int i = 1; i < 64; i++)
			assertEquals(1, TransformationStrategies.rawIso().toBitVector(a).getLong(0, i));
		for(int i = 0; i < 63; i++)
			assertEquals(0, TransformationStrategies.rawIso().toBitVector(a).getLong(1, 1 + i));
		for(int i = 64; i < 127; i++)
			assertEquals((1L << i - 64) - 1, TransformationStrategies.rawIso().toBitVector(a).getLong(64, i));

		a = new String(new char[] { 1, 0, 0, 0, 0, 0, 0, 0, 0x55 });
		assertEquals(0x55L << 57, TransformationStrategies.rawIso().toBitVector(a).getLong(7, 71));
		assertEquals(0x15L << 57, TransformationStrategies.rawIso().toBitVector(a).getLong(7, 70));
		assertEquals(0x15L << 57, TransformationStrategies.rawIso().toBitVector(a).getLong(7, 69));

	}

	@Test
	public void testMutableString() {
		MutableString a = new MutableString(new char[] { 0x55, 0xFF });
		assertEquals(16, TransformationStrategies.rawIso().toBitVector(a).length());
		assertEquals(0xFF55L, TransformationStrategies.rawIso().toBitVector(a).getLong(0, 16));

		a = new MutableString(new char[] { 1, 0, 0, 0, 0, 0, 0, 0, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, (char)-1, 0 });
		assertTrue(TransformationStrategies.rawIso().toBitVector(a).getBoolean(0));
		assertFalse(TransformationStrategies.rawIso().toBitVector(a).getBoolean(1));
		assertTrue(TransformationStrategies.rawIso().toBitVector(a).getBoolean(64));
		assertEquals(0x1L, TransformationStrategies.rawIso().toBitVector(a).getLong(0, 56));
		assertEquals(0x1L, TransformationStrategies.rawIso().toBitVector(a).getLong(0, 64));
		assertEquals(-1L, TransformationStrategies.rawIso().toBitVector(a).getLong(64, 128));

		for(int i = 1; i < 64; i++)
			assertEquals(1, TransformationStrategies.rawIso().toBitVector(a).getLong(0, i));
		for(int i = 0; i < 63; i++)
			assertEquals(0, TransformationStrategies.rawIso().toBitVector(a).getLong(1, 1 + i));
		for(int i = 64; i < 127; i++)
			assertEquals((1L << i - 64) - 1, TransformationStrategies.rawIso().toBitVector(a).getLong(64, i));

		a = new MutableString(new char[] { 1, 0, 0, 0, 0, 0, 0, 0, 0x55 });
		assertEquals(0x55L << 57, TransformationStrategies.rawIso().toBitVector(a).getLong(7, 71));
		assertEquals(0x15L << 57, TransformationStrategies.rawIso().toBitVector(a).getLong(7, 70));
		assertEquals(0x15L << 57, TransformationStrategies.rawIso().toBitVector(a).getLong(7, 69));

	}
}
