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

import it.unimi.dsi.lang.MutableString;

public class RawUtf16TransformationStrategyTest {

	@Test
	public void testCharSequence() {
		String s = new String(new char[] { '\u0001', '\u0002' });
		assertEquals(32, TransformationStrategies.rawUtf16().toBitVector(s).length());
		assertEquals(0x00020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 32));
		assertTrue(TransformationStrategies.rawUtf16().toBitVector(s).getBoolean(0));
		assertFalse(TransformationStrategies.rawUtf16().toBitVector(s).getBoolean(1));
		assertTrue(TransformationStrategies.rawUtf16().toBitVector(s).getBoolean(17));

		s = new String(new char[] { '\u0001', '\u0002', '\u0003' });
		assertEquals(48, TransformationStrategies.rawUtf16().toBitVector(s).length());
		assertEquals(0x000300020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 48));
		assertEquals(0x00020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 32));
		s = new String(new char[] { '\u0001', '\u0002', '\u0003', '\u0004' });
		assertEquals(64, TransformationStrategies.rawUtf16().toBitVector(s).length());
		assertEquals(0x0004000300020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 64));
		assertEquals(0x000400030002L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(16, 64));

		assertEquals(0x1L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 1));
		assertEquals(0x00001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 17));
		assertEquals(0x20001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 18));

		s = new String(new char[] { '\u0001', '\u0002', '\u0003', '\u0004', '\u0005' });
		assertEquals(0, TransformationStrategies.rawUtf16().toBitVector(s).getLong(4, 4));
		assertEquals(0x005000400030002000L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(4, 68));
		assertEquals(0x000500040003000200L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(8, 72));
	}

	@Test
	public void testMutableString() {
		MutableString s = new MutableString(new char[] { '\u0001', '\u0002' });
		assertEquals(32, TransformationStrategies.rawUtf16().toBitVector(s).length());
		assertEquals(0x00020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 32));
		assertTrue(TransformationStrategies.rawUtf16().toBitVector(s).getBoolean(0));
		assertFalse(TransformationStrategies.rawUtf16().toBitVector(s).getBoolean(1));
		assertTrue(TransformationStrategies.rawUtf16().toBitVector(s).getBoolean(17));

		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003' });
		assertEquals(48, TransformationStrategies.rawUtf16().toBitVector(s).length());
		assertEquals(0x000300020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 48));
		assertEquals(0x00020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 32));
		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003', '\u0004' });
		assertEquals(64, TransformationStrategies.rawUtf16().toBitVector(s).length());
		assertEquals(0x0004000300020001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 64));
		assertEquals(0x000400030002L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(16, 64));

		assertEquals(0x1L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 1));
		assertEquals(0x00001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 17));
		assertEquals(0x20001L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(0, 18));

		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003', '\u0004', '\u0005' });
		assertEquals(0, TransformationStrategies.rawUtf16().toBitVector(s).getLong(4, 4));
		assertEquals(0x005000400030002000L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(4, 68));
		assertEquals(0x000500040003000200L, TransformationStrategies.rawUtf16().toBitVector(s).getLong(8, 72));
	}

}
