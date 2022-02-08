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

package it.unimi.dsi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unimi.dsi.fastutil.chars.CharArrayList;

public class TextPatternTest {
	@Test
	public void testSingleCharacterSearch() {
		final byte[] b = new byte[] { 1, (byte)'A', 2 };
		final String s = " A ";
		final TextPattern pattern = new TextPattern("A");

		assertEquals(-1, pattern.search(b, 0, 1));
		assertEquals(-1, pattern.search(s, 0, 1));
		assertEquals(-1, pattern.search(s.toCharArray(), 0, 1));
		assertEquals(-1, pattern.search(CharArrayList.wrap(s.toCharArray()), 0, 1));

		assertEquals(1, pattern.search(b));
		assertEquals(1, pattern.search(s));
		assertEquals(1, pattern.search(s.toCharArray()));
		assertEquals(1, pattern.search(CharArrayList.wrap(s.toCharArray())));
	}

	@Test
	public void testSearch() {
		final byte[] b = new byte[] { 1, (byte)'A', 'B', 2 };
		final String s = " AB ";
		final TextPattern pattern = new TextPattern("AB");

		assertEquals(-1, pattern.search(b, 0, 2));
		assertEquals(-1, pattern.search(s, 0, 2));
		assertEquals(-1, pattern.search(s.toCharArray(), 0, 2));
		assertEquals(-1, pattern.search(CharArrayList.wrap(s.toCharArray()), 0, 2));

		assertEquals(1, pattern.search(b));
		assertEquals(1, pattern.search(s));
		assertEquals(1, pattern.search(s.toCharArray()));
		assertEquals(1, pattern.search(CharArrayList.wrap(s.toCharArray())));

		TextPattern patternMeta = new TextPattern("<meta", TextPattern.CASE_INSENSITIVE);
		assertTrue(patternMeta.search(documentMetaIsutf_8.getBytes()) != -1);
		patternMeta = new TextPattern("<META", TextPattern.CASE_INSENSITIVE);
		assertTrue(patternMeta.search(documentMetaIsutf_8.getBytes()) != -1);

	}

	private static final String documentMetaIsutf_8 =
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n" +
		"\n" +
		"<html>\n" +
		"<head>\n" +
		"<style type=\"text/css\">\n" +
		"@import \"/css/content.php\";\n" +
		"@import \"/css/layout.php\";\n" +
		"</style>" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >" +
		"<title id=\"mamma\" special-type=\"li turchi\">Sebastiano Vigna</title>\n" +
		"</HEAD>\n" +
		"<boDY>\n" +
		"<div id=header>:::Sebastiano Vigna</div>" +
		"<div id=left>\n" +
		"<ul id=\"left-nav\">" +
		"<br>Bye bye baby\n" +
		"<img SRc=\"but I'm ignoring this one\"> and not this one\n" +
		"\n\n even whitespace counts \n\n" +
		"<frame SRC=\"http://www.GOOGLE.com/\">The frame source counts</frame>\n" +
		"<iframe SRC=\"http://www.GOOGLE.com/\">And so does the iframe source</iframe>\n" +
		"</body>\n" +
		"</html>";


}
