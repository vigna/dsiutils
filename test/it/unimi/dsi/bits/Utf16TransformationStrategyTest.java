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

import it.unimi.dsi.lang.MutableString;

public class Utf16TransformationStrategyTest {

	@Test
	public void testCharacterSequence() {
		String s = new String(new char[] { '\u0001', '\u0002' });
		assertEquals(48, TransformationStrategies.prefixFreeUtf16().toBitVector(s).length());
		assertEquals(0x40008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 32));
		assertEquals(0x40008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 48));
		assertFalse(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(0));
		assertFalse(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(1));
		assertTrue(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(15));
		assertFalse(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(33));

		s = new String(new char[] { '\u0001', '\u0002', '\u0003' });
		assertEquals(64, TransformationStrategies.prefixFreeUtf16().toBitVector(s).length());
		assertEquals(0xC00040008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 48));
		assertEquals(0xC00040008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 64));
		s = new String(new char[] { '\u0001', '\u0002', '\u0003', '\u0004' });
		assertEquals(80, TransformationStrategies.prefixFreeUtf16().toBitVector(s).length());
		assertEquals(0x2000C00040008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 64));
		assertEquals(0, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(64, 80));
		//System.err.println(Long.toHexString(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(16, 80)));
		assertEquals(0x2000C0004000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(16, 80));


		s = new String(new char[] { '\u0001', '\u0002' });
		assertEquals(32, TransformationStrategies.utf16().toBitVector(s).length());
		assertEquals(0x40008000L, TransformationStrategies.utf16().toBitVector(s).getLong(0, 32));
		assertFalse(TransformationStrategies.utf16().toBitVector(s).getBoolean(0));
		assertFalse(TransformationStrategies.utf16().toBitVector(s).getBoolean(1));
		assertTrue(TransformationStrategies.utf16().toBitVector(s).getBoolean(15));

		s = new String(new char[] { '\u0001', '\u0002', '\u0003' });
		assertEquals(48, TransformationStrategies.utf16().toBitVector(s).length());
		assertEquals(0xC00040008000L, TransformationStrategies.utf16().toBitVector(s).getLong(0, 48));
		s = new String(new char[] { '\u0001', '\u0002', '\u0003', '\u0004' });
		assertEquals(64, TransformationStrategies.utf16().toBitVector(s).length());
		assertEquals(0x2000C00040008000L, TransformationStrategies.utf16().toBitVector(s).getLong(0, 64));
		assertEquals(0x2000C000400080L, TransformationStrategies.utf16().toBitVector(s).getLong(8, 62));
		assertEquals(0x000C000400080L, TransformationStrategies.utf16().toBitVector(s).getLong(8, 61));
	}

	@Test
	public void testMutableString() {
		MutableString s = new MutableString(new char[] { '\u0001', '\u0002' });
		assertEquals(48, TransformationStrategies.prefixFreeUtf16().toBitVector(s).length());
		assertEquals(0x40008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 32));
		assertEquals(0x40008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 48));
		assertFalse(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(0));
		assertFalse(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(1));
		assertTrue(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(15));
		assertFalse(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getBoolean(33));

		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003' });
		assertEquals(64, TransformationStrategies.prefixFreeUtf16().toBitVector(s).length());
		assertEquals(0xC00040008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 48));
		assertEquals(0xC00040008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 64));
		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003', '\u0004' });
		assertEquals(80, TransformationStrategies.prefixFreeUtf16().toBitVector(s).length());
		assertEquals(0x2000C00040008000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(0, 64));
		assertEquals(0, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(64, 80));
		//System.err.println(Long.toHexMutableString(TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(16, 80)));
		assertEquals(0x2000C0004000L, TransformationStrategies.prefixFreeUtf16().toBitVector(s).getLong(16, 80));


		s = new MutableString(new char[] { '\u0001', '\u0002' });
		assertEquals(32, TransformationStrategies.utf16().toBitVector(s).length());
		assertEquals(0x40008000L, TransformationStrategies.utf16().toBitVector(s).getLong(0, 32));
		assertFalse(TransformationStrategies.utf16().toBitVector(s).getBoolean(0));
		assertFalse(TransformationStrategies.utf16().toBitVector(s).getBoolean(1));
		assertTrue(TransformationStrategies.utf16().toBitVector(s).getBoolean(15));

		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003' });
		assertEquals(48, TransformationStrategies.utf16().toBitVector(s).length());
		assertEquals(0xC00040008000L, TransformationStrategies.utf16().toBitVector(s).getLong(0, 48));
		s = new MutableString(new char[] { '\u0001', '\u0002', '\u0003', '\u0004' });
		assertEquals(64, TransformationStrategies.utf16().toBitVector(s).length());
		assertEquals(0x2000C00040008000L, TransformationStrategies.utf16().toBitVector(s).getLong(0, 64));
		assertEquals(0x2000C000400080L, TransformationStrategies.utf16().toBitVector(s).getLong(8, 62));
		assertEquals(0x000C000400080L, TransformationStrategies.utf16().toBitVector(s).getLong(8, 61));
	}
}
