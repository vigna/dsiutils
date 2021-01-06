/*
 * DSI utilities
 *
 * Copyright (C) 2002-2021 Sebastiano Vigna
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

package it.unimi.dsi.big.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

/**
 * @author Fabien Campagne
 * @author Sebastiano Vigna
 */
public class SemiExternalGammaBigListTest {

	private static InputBitStream buildInputStream(final LongList longs) throws IOException {
		final byte[] array = new byte[longs.size() * 4];
		@SuppressWarnings("resource")
		final
		OutputBitStream streamer = new OutputBitStream(array);
		for (int i = 0; i < longs.size(); i++) streamer.writeLongGamma(longs.getLong(i));
		final int size = (int)(streamer.writtenBits() / 8) + ((streamer.writtenBits() % 8) == 0 ? 0 : 1);
		final byte[] smaller = new byte[size];
		System.arraycopy(array, 0, smaller, 0, size);

		return new InputBitStream(smaller);

	}

	@Test
    public void testSemiExternalGammaBigListGammaCoding() throws IOException {

		final long[] longs = { 10, 300, 450, 650, 1000, 1290, 1699 };
		final LongList listLongs = new LongArrayList(longs);

		SemiExternalGammaBigList list = new SemiExternalGammaBigList(buildInputStream(listLongs), 1, listLongs.size());
		for (long i = 0; i < longs.length; ++i) {
			assertEquals(("test failed for index: " + i), longs[(int) i], list.getLong(i));
		}

		list = new SemiExternalGammaBigList(buildInputStream(listLongs), 2, listLongs.size());
		for (long i = 0; i < longs.length; ++i) {
			assertEquals(("test failed for index: " + i), longs[(int) i], list.getLong(i));
		}

		list = new SemiExternalGammaBigList(buildInputStream(listLongs), 4, listLongs.size());
		for (long i = 0; i < longs.length; ++i) {
			assertEquals(("test failed for index: " + i), longs[(int) i], list.getLong(i));
		}

		list = new SemiExternalGammaBigList(buildInputStream(listLongs), 7, listLongs.size());
		for (long i = 0; i < longs.length; ++i) {
			assertEquals(("test failed for index: " + i), longs[(int) i], list.getLong(i));
		}

		list = new SemiExternalGammaBigList(buildInputStream(listLongs), 8, listLongs.size());
		for (long i = 0; i < longs.length; ++i) {
			assertEquals(("test failed for index: " + i), longs[(int) i], list.getLong(i));
		}
    }

	@Test
    public void testEmptySemiExternalGammaBigListGammaCoding() throws IOException {

		final long[] longs = {  };
		final LongList listOffsets = new LongArrayList(longs);

		new SemiExternalGammaBigList(buildInputStream(listOffsets), 1, listOffsets.size());
		assertTrue(true);
    }

}
