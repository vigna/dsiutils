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

package it.unimi.dsi.compression;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

public abstract class CodecTestCase {
	protected static void checkPrefixCodec(final PrefixCodec codec, final Random r) throws IOException {
		final int[] symbol = new int[100];
		final BooleanArrayList bits = new BooleanArrayList();
		for(int i = 0; i < symbol.length; i++) symbol[i] = r.nextInt(codec.size());
		for (final int element : symbol) {
			final BitVector word = codec.codeWords()[element];
			for(long j = 0; j < word.length(); j++) bits.add(word.getBoolean(j));
		}

		final BooleanIterator booleanIterator = bits.iterator();
		final Decoder decoder = codec.decoder();
		for (final int element : symbol) {
			assertEquals(element, decoder.decode(booleanIterator));
		}

		final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		@SuppressWarnings("resource")
		final
		OutputBitStream obs = new OutputBitStream(fbaos, 0);
		obs.write(bits.iterator());
		obs.flush();
		final InputBitStream ibs = new InputBitStream(fbaos.array);

		for (final int element : symbol) {
			assertEquals(element, decoder.decode(ibs));
		}
	}

	protected void checkLengths(final int[] frequency, final int[] codeLength, final BitVector[] codeWord) {
		for(int i = 0; i < frequency.length; i++)
			assertEquals(Integer.toString(i), codeLength[i], codeWord[i].length());
	}
}
