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

package it.unimi.dsi.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;

import org.junit.Test;

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongIterators;

@SuppressWarnings("deprecation")
public class ByteBufferLongBigListTest {

	@Test
	public void testSetGetSmall() {
		final ByteBufferLongBigList b = new ByteBufferLongBigList(ByteBuffer.allocate(1000));
		b.set(0, 10);
		assertEquals(10, b.getLong(0));
	}

	@Test
	public void testSetGetBig() throws IOException {
		final File f = File.createTempFile(ByteBufferLongBigListTest.class.getSimpleName(), "buffer");
		f.deleteOnExit();
		BinIO.storeLongs(LongIterators.fromTo(0, 200000000), f);
		final RandomAccessFile c = new RandomAccessFile(f.toString(), "rw");
		final ByteBufferLongBigList b = ByteBufferLongBigList.map(c.getChannel(), ByteOrder.BIG_ENDIAN, MapMode.READ_WRITE);
		b.set(1, 10);
		assertEquals(10, b.getLong(1));
		b.set(190000000, 10);
		assertEquals(10, b.getLong(190000000));
		c.close();
	}
}
