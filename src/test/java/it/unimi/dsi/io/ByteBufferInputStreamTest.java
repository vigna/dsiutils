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

package it.unimi.dsi.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Random;

import org.junit.Test;

import it.unimi.dsi.util.SplitMix64Random;

/**
 * Note: this test has little meaning unless you change ByteBufferInputStream.CHUNK_SHIFT to 16.
 */

@SuppressWarnings("resource")
public class ByteBufferInputStreamTest {

	private static final boolean DEBUG = false;

	@Test
	public void testStream() throws FileNotFoundException, IOException {
		final File f = File.createTempFile(ByteBufferInputStreamTest.class.getName(), "tmp");
		f.deleteOnExit();
		final int l = 100000;
		final long seed = System.currentTimeMillis();
		if (DEBUG) System.err.println("Seed: " + seed);
		final Random random = new SplitMix64Random(seed);

		for(int n = 1; n < 8; n++) {
			final FileOutputStream fos = new FileOutputStream(f);
			for(int i = 0; i < l * n; i++) fos.write(random.nextInt() & 0xFF);
			fos.close();

			final FileChannel channel = new FileInputStream(f).getChannel();
			ByteBufferInputStream bis = ByteBufferInputStream.map(channel, MapMode.READ_ONLY);
			if (n % 2 == 0) bis = bis.copy();

			FileInputStream test = new FileInputStream(f);
			FileChannel fc = test.getChannel();
			int a1, a2, off, len, pos;
			final byte b1[] = new byte[32768];
			final byte b2[] = new byte[32768];

			for(int k = 0; k < l / 10; k++) {

				switch (random.nextInt(6)) {

				case 0:
					if (DEBUG) System.err.println("read()");
					a1 = bis.read();
					a2 = test.read();
					assertEquals(a2, a1);
					break;

				case 1:
					off = random.nextInt(b1.length);
					len = random.nextInt(b1.length - off + 1);
					a1 = bis.read(b1, off, len);
					a2 = test.read(b2, off, len);
					if (DEBUG) System.err.println("read(b, " + off + ", " + len + ")");

					assertEquals(a2, a1);

					for (int i = off; i < off + len; i++)
						assertEquals(b2[i], b1[i]);
					break;

				case 2:
					if (DEBUG) System.err.println("available()");
					assertEquals(test.available(), bis.available());
					break;

				case 3:
					pos = (int)bis.position();
					if (DEBUG) System.err.println("position()=" + pos);
					assertEquals((int)fc.position(), pos);
					break;

				case 4:
					pos = random.nextInt(l * n);
					bis.position(pos);
					if (DEBUG) System.err.println("position(" + pos + ")");
					(test = new FileInputStream(f)).skip(pos);
					fc = test.getChannel();
					break;

				case 5:
					pos = random.nextInt((int)(l * n - bis.position() + 1));
					if (DEBUG) System.err.println("skip(" + pos + ")");
					a1 = (int)bis.skip(pos);
					a2 = (int)test.skip(pos);
					assertEquals(a2, a1);
					break;
				}
			}

			test.close();
			bis = null;
			System.gc(); // Try to get rid of mapped buffers.
			channel.close();
		}
	}

	@Test
	public void testEmpty() throws IOException {
		final File f = File.createTempFile(ByteBufferInputStreamTest.class.getName(), "tmp");
		f.deleteOnExit();
		final FileChannel channel = new FileInputStream(f).getChannel();
		final ByteBufferInputStream s = ByteBufferInputStream.map(channel);
		final byte b[] = new byte[1];
		assertEquals(-1, s.read());
		assertEquals(-1, s.read(b));
		assertEquals(0, s.available());
		assertEquals(0, s.skip(1));
		assertEquals(0, s.length());
		assertEquals(0, s.position());
		s.position(1);
		assertEquals(-1, s.read());
		assertEquals(-1, s.read(b));
		assertEquals(0, s.available());
		assertEquals(0, s.skip(1));
		assertEquals(0, s.length());
		assertEquals(0, s.position());
		s.position(1);
		final ByteBufferInputStream t = s.copy();
		assertEquals(-1, t.read());
		assertEquals(-1, t.read(b));
		assertEquals(0, t.available());
		assertEquals(0, t.skip(1));
		assertEquals(0, t.length());
		assertEquals(0, t.position());
		t.position(1);
		assertEquals(-1, t.read());
		assertEquals(-1, t.read(b));
		assertEquals(0, t.available());
		assertEquals(0, t.skip(1));
		assertEquals(0, t.length());
		assertEquals(0, t.position());
	}
}
