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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

@SuppressWarnings("resource")
public class InputBitStreamTest {

	@Test
	public void testReadAligned() throws IOException {
		final byte[] a = { 1 }, A = new byte[1];
		new InputBitStream(a).read(A, 8);
		assertTrue(Arrays.toString(a) + " != " + Arrays.toString(A), Arrays.equals(a, A));
		final byte[] b = { 1, 2 }, B = new byte[2];
		new InputBitStream(b).read(B, 16);
		assertTrue(Arrays.toString(b) + " != " + Arrays.toString(B), Arrays.equals(b, B));
		final byte[] c = { 1, 2, 3 }, C = new byte[3];
		new InputBitStream(c).read(C, 24);
		assertTrue(Arrays.toString(c) + " != " + Arrays.toString(C), Arrays.equals(c, C));
	}

	@Test
	public void testOverflow() throws IOException {
		final InputBitStream ibs = new InputBitStream(new byte[0]);
		ibs.readInt(0);
	}

	@Test
	public void testPosition() throws IOException {
		final InputBitStream ibs = new InputBitStream(new byte[100]);
		for(int i = 0; i < 800; i++) {
			ibs.position(i);
			assertEquals(i, ibs.position());
		}
		for(int i = 800; i-- != 0;) {
			ibs.position(i);
			assertEquals(i, ibs.position());
		}
	}

	@Test
	public void readWriteLongs() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Random random = new Random(0);
		final File tempFile = File.createTempFile("readWriteLongs", "os");
		tempFile.deleteOnExit();

		final String[] oneArgs = { "LongGamma", "LongShiftedGamma", "LongDelta", "LongNibble" };
		final String[] twoArgsLong = { "LongGolomb", "LongSkewedGolomb" };

		final LongArrayList longs = new LongArrayList();
		for (int i = 0; i < 10; i++) longs.add(5000000000L + random.nextInt());
		//for (int i = 24; i < 60; i++) longs.add(1L << i);
		final LongArrayList extraArgLong = new LongArrayList();
		for (int i = 0; i < 5; i++) extraArgLong.add(100000 + random.nextInt(1000000));

		// Write
		final OutputBitStream obs = new OutputBitStream(tempFile);

		for (final String methSuffix: oneArgs)
			for (final long longValue: longs)
				OutputBitStream.class.getMethod("write" + methSuffix, long.class).invoke(obs, Long.valueOf(longValue));
		for (final String methSuffix: twoArgsLong)
			for (final long longValue: longs)
				for (final long longXValue: extraArgLong)
					OutputBitStream.class.getMethod("write" + methSuffix, long.class, long.class).invoke(obs, Long.valueOf(longValue), Long.valueOf(longXValue));
		// Special methods
		for (final long longValue: longs) {
			obs.writeLong(longValue, Long.SIZE);
			obs.writeLongMinimalBinary(longValue, longValue + 5);
			for (int i = 3; i < 10; i++) obs.writeLongZeta(longValue, i);
		}
		obs.writeLongUnary(15 + 1L << 20);

		obs.close();

		// Read
		final InputBitStream ibs = new InputBitStream(tempFile);

		for (final String methSuffix: oneArgs)
			for (final long longValue: longs)
				assertEquals(Long.valueOf(longValue), InputBitStream.class.getMethod("read" + methSuffix).invoke(ibs));
		for (final String methSuffix: twoArgsLong)
			for (final long longValue: longs)
				for (final long longXValue: extraArgLong)
					assertEquals(Long.valueOf(longValue), InputBitStream.class.getMethod("read" + methSuffix, long.class).invoke(ibs, Long.valueOf(longXValue)));
		for (final long longValue: longs) {
			assertEquals(longValue, ibs.readLong(Long.SIZE));
			assertEquals(longValue, ibs.readLongMinimalBinary(longValue + 5));
			for (int i = 3; i < 10; i++) assertEquals(longValue, ibs.readLongZeta(i));
		}
		assertEquals(15 + 1L << 20, ibs.readLongUnary());

		ibs.close();

	}

	@Test
	public void testUnary() throws IOException {
		final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom(0);
		final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		final OutputBitStream obs = new OutputBitStream(fbaos);
		for(int i = 0; i < 100000000; i++) obs.writeUnary(Long.numberOfTrailingZeros(random.nextLong()));
		obs.flush();
		final InputBitStream ibs = new InputBitStream(fbaos.array);
		random.setSeed(0);
		for(int i = 0; i < 100000000; i++) assertEquals(Long.numberOfTrailingZeros(random.nextLong()), ibs.readUnary());
	}

	@Test
	public void testLongUnary() throws IOException {
		final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom(0);
		final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		final OutputBitStream obs = new OutputBitStream(fbaos);
		for(int i = 0; i < 100000000; i++) obs.writeLongUnary(Long.numberOfTrailingZeros(random.nextLong()));
		obs.flush();
		final InputBitStream ibs = new InputBitStream(fbaos.array);
		random.setSeed(0);
		for(int i = 0; i < 100000000; i++) assertEquals(Long.numberOfTrailingZeros(random.nextLong()), ibs.readLongUnary());
	}

	public static void main(final String arg[]) throws IOException {
		final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom(0);
		final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		final OutputBitStream obs = new OutputBitStream(fbaos);
		for(int i = 0; i < 100000000; i++) obs.writeUnary(Long.numberOfTrailingZeros(random.nextLong()));
		obs.flush();
		final InputBitStream ibs = new InputBitStream(fbaos.array);
		for(int i = 0; i < 100000000; i++) ibs.readUnary();

		ibs.position(0);
		for(int i = 0; i < 100000000; i++) ibs.readUnary();

		ibs.position(0);
		for(int i = 0; i < 100000000; i++) ibs.readUnary();
	}
}
