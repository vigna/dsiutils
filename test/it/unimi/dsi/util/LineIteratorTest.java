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

import java.io.StringReader;

import org.junit.Test;

import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.io.LineIterator;
import it.unimi.dsi.logging.ProgressLogger;


public class LineIteratorTest {

	private static final String TEXT = "0\n1\n2\n3";
	private static final CharSequence[] LINES = TEXT.split("\n");

	@Test
	public void testLineIteratorProgressLogger() {
		testLineIterator(new ProgressLogger());
	}

	@Test
	public void testLineIterator() {
		testLineIterator(null);
	}

	public void testLineIterator(final ProgressLogger pl) {
		final LineIterator lineIterator = new LineIterator(new FastBufferedReader(new StringReader(TEXT)), pl);
		int i = 0;
		while(lineIterator.hasNext())
			assertEquals(LINES[i++].toString(), lineIterator.next().toString());

		assertEquals(i, LINES.length);
	}

}
