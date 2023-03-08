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

public class RawByteArrayTransformationStrategyTest {

	@Test
	public void testGetLong() {
		byte[] a = new byte[] { 0x55, (byte)0xFF };
		assertEquals(16, TransformationStrategies.rawByteArray().toBitVector(a).length());
		assertEquals(0xFF55L, TransformationStrategies.rawByteArray().toBitVector(a).getLong(0, 16));

		a = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0 };
		assertTrue(TransformationStrategies.rawByteArray().toBitVector(a).getBoolean(0));
		assertFalse(TransformationStrategies.rawByteArray().toBitVector(a).getBoolean(1));
		assertTrue(TransformationStrategies.rawByteArray().toBitVector(a).getBoolean(64));
		assertEquals(0x1L, TransformationStrategies.rawByteArray().toBitVector(a).getLong(0, 56));
		assertEquals(0x1L, TransformationStrategies.rawByteArray().toBitVector(a).getLong(0, 64));
		assertEquals(-1L, TransformationStrategies.rawByteArray().toBitVector(a).getLong(64, 128));

		for(int i = 1; i < 64; i++)
			assertEquals(1, TransformationStrategies.rawByteArray().toBitVector(a).getLong(0, i));
		for(int i = 0; i < 63; i++)
			assertEquals(0, TransformationStrategies.rawByteArray().toBitVector(a).getLong(1, 1 + i));
		for(int i = 64; i < 127; i++)
			assertEquals((1L << i - 64) - 1, TransformationStrategies.rawByteArray().toBitVector(a).getLong(64, i));

		a = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0x55 };
		assertEquals(0x55L << 57, TransformationStrategies.rawByteArray().toBitVector(a).getLong(7, 71));
		assertEquals(0x15L << 57, TransformationStrategies.rawByteArray().toBitVector(a).getLong(7, 70));
		assertEquals(0x15L << 57, TransformationStrategies.rawByteArray().toBitVector(a).getLong(7, 69));

	}

}
