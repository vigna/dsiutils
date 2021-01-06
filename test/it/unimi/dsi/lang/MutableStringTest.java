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

package it.unimi.dsi.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class MutableStringTest {
	@Test
	public void testSqueezeSpace() {
		final MutableString s = new MutableString(new char[] { 32, 13, 10, 32, 32, 32, 13, 10, 32, 32, 32, 13, 10, 32, 32, 32, 32, 32 });

		assertEquals(new MutableString(" \r\n \r\n \r\n "), s.squeezeSpace());
		assertEquals(new MutableString(" "), s.squeezeWhitespace());
	}

	@Test
	public void testSubsequence() {
		final MutableString s = new MutableString("abc");
		final CharSequence ss = s.subSequence(1, 3);
		assertEquals(new MutableString("bc"), ss);
		assertEquals(1, ss.subSequence(1, 2).length());
	}

	@Test
	public void testSkipSelfDelimUTF8() throws IOException {
		final FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream();
		new MutableString("a").writeSelfDelimUTF8(fastByteArrayOutputStream);
		new MutableString("b").writeSelfDelimUTF8(fastByteArrayOutputStream);
		new MutableString("\u221E").writeSelfDelimUTF8(fastByteArrayOutputStream);
		new MutableString("c").writeSelfDelimUTF8(fastByteArrayOutputStream);
		fastByteArrayOutputStream.flush();
		final FastByteArrayInputStream fastByteArrayInputStream = new FastByteArrayInputStream(fastByteArrayOutputStream.array);
		assertEquals("a", new MutableString().readSelfDelimUTF8(fastByteArrayInputStream).toString());
		assertEquals("b", new MutableString().readSelfDelimUTF8(fastByteArrayInputStream).toString());
		assertEquals(1, MutableString.skipSelfDelimUTF8(fastByteArrayInputStream));
		assertEquals("c", new MutableString().readSelfDelimUTF8(fastByteArrayInputStream).toString());
		fastByteArrayInputStream.position(0);
		assertEquals("a", new MutableString().readSelfDelimUTF8(fastByteArrayInputStream).toString());
		assertEquals(1, MutableString.skipSelfDelimUTF8(fastByteArrayInputStream));
		assertEquals("\uu221E", new MutableString().readSelfDelimUTF8(fastByteArrayInputStream).toString());
		assertEquals("c", new MutableString().readSelfDelimUTF8(fastByteArrayInputStream).toString());
	}

	@Test
	public void testIsEmpty() {
		assertTrue(new MutableString().compact().isEmpty());
		assertTrue(new MutableString().loose().isEmpty());
		assertFalse(new MutableString(" ").compact().isEmpty());
		assertFalse(new MutableString(" ").loose().isEmpty());
	}
}
