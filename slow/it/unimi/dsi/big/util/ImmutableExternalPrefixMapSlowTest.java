/*
 * DSI utilities
 *
 * Copyright (C) 2002-2022 Sebastiano Vigna
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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class ImmutableExternalPrefixMapSlowTest {

	public void testBig(final int blockSize) throws IOException {
		final Iterable<String> p = new Iterable<String>() {
			private final static long INCREMENT= ((1L << 62) / 3000000000L);
			@Override
			public Iterator<String> iterator() {
				return new ObjectIterator<String>() {
					long curr = 0;
					@Override
					public boolean hasNext() {
						return curr < 3000000000L;
					}

					@Override
					public String next() {
						if (! hasNext()) throw new NoSuchElementException();
						final long v = curr++ * INCREMENT ;
						final char[] a = new char[4];
						a[0] = (char)(v >>> 48);
						a[1] = (char)(v >>> 32);
						a[2] = (char)(v >>> 16);
						a[3] = (char)v;
						return String.valueOf(a);
					}
				};
			}
		};

		final ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap(p, blockSize);

		int j = 0;
		for (final String s : p) {
			assertTrue(s, d.containsKey(s));
			assertEquals(s, d.list().get(j++).toString());
		}

		final Iterator<CharSequence> k = d.iterator();
		for(final Iterator<String> i = p.iterator(); i.hasNext();) {
			assertTrue(i.hasNext() == k.hasNext());
			assertEquals(i.next().toString(), k.next().toString());
		}

		// Test negatives
		for(long i = 1000000000000L; i < 1000000002000L; i++) assertEquals(-1, d.getLong(Long.toBinaryString(i)));

	}

	@Test
	public void testBig1024() throws IOException {
		testBig(1024);
	}

	@Test
	public void testBig16384() throws IOException {
		testBig(16384);
	}
}
