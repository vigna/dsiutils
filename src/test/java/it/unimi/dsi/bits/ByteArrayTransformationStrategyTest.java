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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ByteArrayTransformationStrategyTest {

	@Test
	public void testGetLong() {
		byte[] a = new byte[] { 0x55, (byte)0xFF };
		assertEquals(16, TransformationStrategies.byteArray().toBitVector(a).length());
		assertEquals(0xFFAAL, TransformationStrategies.byteArray().toBitVector(a).getLong(0, 16));

		a = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0 };
		assertFalse(TransformationStrategies.byteArray().toBitVector(a).getBoolean(0));
		assertFalse(TransformationStrategies.byteArray().toBitVector(a).getBoolean(1));
		assertTrue(TransformationStrategies.byteArray().toBitVector(a).getBoolean(7));
		assertTrue(TransformationStrategies.byteArray().toBitVector(a).getBoolean(64));
		assertEquals(128, TransformationStrategies.byteArray().toBitVector(a).getLong(0, 56));
		assertEquals(128, TransformationStrategies.byteArray().toBitVector(a).getLong(0, 64));
		assertEquals(0xF000000000000008L, TransformationStrategies.byteArray().toBitVector(a).getLong(4, 68));
		assertEquals(-1L, TransformationStrategies.byteArray().toBitVector(a).getLong(64, 128));
		assertEquals(0, TransformationStrategies.byteArray().toBitVector(a).getLong(128, 136));

		for(int i = 1; i < 7; i++)
			assertEquals(0, TransformationStrategies.byteArray().toBitVector(a).getLong(0, i));

		for(int i = 8; i < 63; i++)
			assertEquals(128, TransformationStrategies.byteArray().toBitVector(a).getLong(0, i));
	}

	@Test
	public void testGetLongPrefixFree() {
		byte[] a = new byte[] { 0x55, (byte)0xFF };
		assertEquals(24, TransformationStrategies.prefixFreeByteArray().toBitVector(a).length());
		assertEquals(0x00FFAAL, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(0, 24));

		a = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0 };
		assertFalse(TransformationStrategies.prefixFreeByteArray().toBitVector(a).getBoolean(0));
		assertFalse(TransformationStrategies.prefixFreeByteArray().toBitVector(a).getBoolean(1));
		assertTrue(TransformationStrategies.prefixFreeByteArray().toBitVector(a).getBoolean(7));
		assertTrue(TransformationStrategies.prefixFreeByteArray().toBitVector(a).getBoolean(64));
		assertEquals(128, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(0, 56));
		assertEquals(128, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(0, 64));
		assertEquals(0xF000000000000008L, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(4, 68));
		assertEquals(-1L, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(64, 128));
		assertEquals(0, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(128, 136));
		assertEquals(0, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(136, 144));

		for (int i = 1; i < 7; i++) assertEquals(0, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(0, i));

		for (int i = 8; i < 63; i++) assertEquals(128, TransformationStrategies.prefixFreeByteArray().toBitVector(a).getLong(0, i));
	}
}
