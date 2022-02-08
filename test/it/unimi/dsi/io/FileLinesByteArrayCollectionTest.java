/*
 * DSI utilities
 *
 * Copyright (C) 2016-2022 Sebastiano Vigna
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import it.unimi.dsi.fastutil.io.BinIO;

public class FileLinesByteArrayCollectionTest {

	@SuppressWarnings("deprecation")
	@Test
	public void test() throws IOException {
		final File file = File.createTempFile(FastBufferedReaderTest.class.getSimpleName(), "tmp");
		file.deleteOnExit();

		byte[] a = { '0', '\n', '1', '\n' };
		BinIO.storeBytes(a, file);
		it.unimi.dsi.big.io.FileLinesByteArrayCollection flbac = new it.unimi.dsi.big.io.FileLinesByteArrayCollection(file.toString());
		it.unimi.dsi.big.io.FileLinesByteArrayCollection.FileLinesIterator iterator = flbac.iterator();
		assertArrayEquals(new byte[] { '0' }, iterator.next());
		assertArrayEquals(new byte[] { '1' }, iterator.next());
		assertFalse(iterator.hasNext());
		assertEquals(2, flbac.size64());

		a = new byte[] { '0', '\n', '1' };
		BinIO.storeBytes(a, file);
		flbac = new it.unimi.dsi.big.io.FileLinesByteArrayCollection(file.toString());
		assertEquals(2, flbac.size64());
		iterator = flbac.iterator();
		assertArrayEquals(new byte[] { '0' }, iterator.next());
		assertTrue(iterator.hasNext());
		assertArrayEquals(new byte[] { '1' }, iterator.next());
		assertFalse(iterator.hasNext());
		assertFalse(iterator.hasNext());
		iterator.close();

		a = new byte[1000000];
		Arrays.fill(a, (byte)'A');
		BinIO.storeBytes(a, file);
		flbac = new it.unimi.dsi.big.io.FileLinesByteArrayCollection(file.toString());
		assertEquals(1, flbac.size64());
		iterator = flbac.iterator();
		assertArrayEquals(a, iterator.next());
		assertFalse(iterator.hasNext());

		file.delete();
	}
}
