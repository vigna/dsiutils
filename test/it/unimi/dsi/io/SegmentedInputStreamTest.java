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

package it.unimi.dsi.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

public class SegmentedInputStreamTest {

	private final FastByteArrayInputStream stream = new FastByteArrayInputStream(
			new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 }
	);

	private SegmentedInputStream sis;

	@Before
	public void setUp() throws IllegalArgumentException, IOException {
		sis = new SegmentedInputStream(stream);
		sis.addBlock(0, 1, 2);
		sis.addBlock(2, 3, 4);
		sis.addBlock(6, 7, 8);
		sis.addBlock(8, 11, 14);
	}

	@Test
	public void testResetClose() throws IOException {
		assertEquals(0, sis.read());
		sis.reset();
		assertEquals(1, sis.read());
		sis.reset();
		assertEquals(-1, sis.read());

		sis.close();
		assertEquals(2, sis.read());
		sis.reset();
		assertEquals(3, sis.read());
		sis.reset();
		assertEquals(-1, sis.read());

		sis.close();
		assertEquals(6, sis.read());
		sis.reset();
		assertEquals(7, sis.read());
		sis.reset();
		assertEquals(-1, sis.read());
	}

	@Test
	public void testRead() throws IOException {
		final byte[] b = new byte[11];
		assertEquals(1, sis.read(b, 0, 10));
		assertEquals(0, b[0]);
		sis.reset();
		assertEquals(1, sis.read(b, 1, 10));
		assertEquals(1, b[1]);

		sis.close();
		assertEquals(1, sis.read(b, 5, 5));
		assertEquals(2, b[5]);
	}

	@Test
	public void testSkip() throws IOException {
		assertEquals(1, sis.skip(1));
		sis.reset();
		assertEquals(1, sis.skip(10));
		sis.reset();
		assertEquals(0, sis.skip(10));

		sis.close();
		sis.close();
		sis.close();

		assertEquals(2, sis.skip(2));
		assertEquals(1, sis.skip(2));
		sis.reset();
		assertEquals(3, sis.skip(10));

	}
}
