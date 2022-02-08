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

import java.util.Iterator;
import java.util.Random;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class CircularCharArrayBufferTest {

	static Random r = new SplitMix64Random(0);
	static int[] sizes = { 1, 5, 10, 100, 500, 1000 };

	@SuppressWarnings("null")
	private static void copyInto(final CircularFifoQueue<Character> cfb, final char[] c, final int offset, final int length) {
		final int howMany = Math.min(length, cfb.size());
		final Iterator<?> it = cfb.iterator();
		for (int i = 0; i < howMany; i++)
			c[offset + i] = ((Character)it.next()).charValue();
	}

	@Test
	public void testAdd() {
		for (final int size: sizes) {
			// System.out.printf("CIRCULAR BUFFER OF SIZE %d: ", size);
			final CircularFifoQueue<Character> cfb = new CircularFifoQueue<>(size);
			final CircularCharArrayBuffer ccab = new CircularCharArrayBuffer(size);
			final int times = r.nextInt(50);
			for (int j = 0; j < times; j++) {
				final char[] c = new char[1 + r.nextInt(1 + size * 10 / 2)];
				final int offset = r.nextInt(c.length);
				final int len = r.nextInt(c.length - offset);
				System.arraycopy(RandomStringUtils.randomAlphanumeric(c.length).toCharArray(), 0, c, 0, c.length);
				for (int i = offset; i < offset + len; i++)
					cfb.add(Character.valueOf(c[i]));
				ccab.add(c, offset, len);
				final char[] res = new char[cfb.size()];
				copyInto(cfb, res, 0, cfb.size());
				final char[] res2 = new char[cfb.size()];
				ccab.toCharArray(res2, 0, cfb.size());
				assertEquals(new String(res), new String(res2));
			}
		}
	}

}
