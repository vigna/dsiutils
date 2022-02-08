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

package it.unimi.dsi.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.io.OfflineIterable;

public class BitVectorsTest {

	@Test
	public void testReadWriteFast() throws IOException {
		final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		final DataOutputStream dos = new DataOutputStream(fbaos);
		final LongArrayBitVector labv = LongArrayBitVector.getInstance();
		final BitVector[] a = new BitVector[] { BitVectors.ZERO, BitVectors.ONE, BitVectors.EMPTY_VECTOR,
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAAL }, 64),
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAL }, 60),
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAAL, 0xAAAAAAAAAAAAAAAAL }, 128),
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAAL, 0xAAAAAAAAAAAAAAAL }, 124) };

		for(final BitVector bv: a) {
			BitVectors.writeFast(bv, dos);
			dos.close();
			assertEquals(bv, BitVectors.readFast(new DataInputStream(new FastByteArrayInputStream(fbaos.array))));
			fbaos.reset();
		}

		for(final BitVector bv: a) {
			BitVectors.writeFast(bv, dos);
			dos.close();
			assertEquals(bv, BitVectors.readFast(new DataInputStream(new FastByteArrayInputStream(fbaos.array)), labv));
			fbaos.reset();
		}
	}

	@Test
	public void testMakeOffline() throws IOException {
		final BitVector[] a = new BitVector[] { BitVectors.ZERO, BitVectors.ONE, BitVectors.EMPTY_VECTOR,
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAAL }, 64),
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAL }, 60),
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAAL, 0xAAAAAAAAAAAAAAAAL }, 128),
				LongArrayBitVector.wrap(new long[] { 0xAAAAAAAAAAAAAAAAL, 0xAAAAAAAAAAAAAAAL }, 124) };

		final OfflineIterable<BitVector,LongArrayBitVector> iterable = new OfflineIterable<>(BitVectors.OFFLINE_SERIALIZER, LongArrayBitVector.getInstance());
		iterable.addAll(Arrays.asList(a));

		final Iterator<LongArrayBitVector> iterator = iterable.iterator();
		for (final BitVector element : a) assertEquals(element, iterator.next());
		assertFalse(iterator.hasNext());
		iterable.close();
	}
}
