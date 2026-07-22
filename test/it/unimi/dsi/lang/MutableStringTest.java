/*
 * DSI utilities
 *
 * Copyright (C) 2010-2026 Sebastiano Vigna
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
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
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
		// Widen to OutputStream/InputStream: since fastutil 8.5.18 the fast streams also
		// implement ObjectOutput/ObjectInput, so the call would otherwise be ambiguous with
		// the DataOutput/DataInput overloads.
		final OutputStream os = fastByteArrayOutputStream;
		new MutableString("a").writeSelfDelimUTF8(os);
		new MutableString("b").writeSelfDelimUTF8(os);
		new MutableString("\u221E").writeSelfDelimUTF8(os);
		new MutableString("c").writeSelfDelimUTF8(os);
		fastByteArrayOutputStream.flush();
		final FastByteArrayInputStream fastByteArrayInputStream = new FastByteArrayInputStream(fastByteArrayOutputStream.array);
		final InputStream is = fastByteArrayInputStream;
		assertEquals("a", new MutableString().readSelfDelimUTF8(is).toString());
		assertEquals("b", new MutableString().readSelfDelimUTF8(is).toString());
		assertEquals(1, MutableString.skipSelfDelimUTF8(is));
		assertEquals("c", new MutableString().readSelfDelimUTF8(is).toString());
		fastByteArrayInputStream.position(0);
		assertEquals("a", new MutableString().readSelfDelimUTF8(is).toString());
		assertEquals(1, MutableString.skipSelfDelimUTF8(is));
		assertEquals("\uu221E", new MutableString().readSelfDelimUTF8(is).toString());
		assertEquals("c", new MutableString().readSelfDelimUTF8(is).toString());
	}

	@Test
	public void testIsEmpty() {
		assertTrue(new MutableString().compact().isEmpty());
		assertTrue(new MutableString().loose().isEmpty());
		assertFalse(new MutableString(" ").compact().isEmpty());
		assertFalse(new MutableString(" ").loose().isEmpty());
	}

	@Test
	public void testIndexOfAnyBut() {
		// Regression test: the no-argument overloads used to return the complement of the
		// intended result (they delegated to indexOfAnyOf()).
		assertEquals(3, new MutableString("aaab").indexOfAnyBut(new CharOpenHashSet(new char[] { 'a' })));
		assertEquals(3, new MutableString("aaab").indexOfAnyBut(new char[] { 'a' }));
		assertEquals(0, new MutableString("baaa").indexOfAnyBut(new CharOpenHashSet(new char[] { 'a' })));
		assertEquals(0, new MutableString("baaa").indexOfAnyBut(new char[] { 'a' }));
		assertEquals(-1, new MutableString("aaaa").indexOfAnyBut(new CharOpenHashSet(new char[] { 'a' })));
		assertEquals(-1, new MutableString("aaaa").indexOfAnyBut(new char[] { 'a' }));
	}

	@Test
	public void testLastIndexOfAnyBut() {
		// Regression test: the no-argument char[] overload used to search from position 0,
		// so it could only ever return 0 or -1.
		assertEquals(2, new MutableString("xxa").lastIndexOfAnyBut(new char[] { 'x' }));
		assertEquals(2, new MutableString("xxa").lastIndexOfAnyBut(new CharOpenHashSet(new char[] { 'x' })));
		assertEquals(-1, new MutableString("xxx").lastIndexOfAnyBut(new char[] { 'x' }));
	}

	@Test
	public void testAppendArraySlice() {
		// Regression test: the offset was ignored when copying (CharSequence[]) or when
		// filling the temporary array (Object[]), causing wrong results or exceptions.
		assertEquals(new MutableString("YY-ZZZ"), new MutableString().append(new CharSequence[] { "X", "YY", "ZZZ" }, 1, 2, "-"));
		assertEquals(new MutableString("YY-ZZZ"), new MutableString().append(new Object[] { "X", "YY", "ZZZ" }, 1, 2, "-"));
	}
}
