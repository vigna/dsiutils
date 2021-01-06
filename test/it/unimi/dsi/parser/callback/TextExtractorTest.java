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

package it.unimi.dsi.parser.callback;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unimi.dsi.parser.BulletParser;

public class TextExtractorTest {

	@Test
	public void testBRBreaksFlow() {
		final char a[] = "ciao<BR>mamma<BR>".toCharArray();
		final BulletParser bulletParser = new BulletParser();
		final TextExtractor textExtractor = new TextExtractor();
		bulletParser.setCallback(textExtractor);
		bulletParser.parse(a);
		assertTrue(textExtractor.text.toString(), textExtractor.text.indexOf(' ') != -1);
	}

}
