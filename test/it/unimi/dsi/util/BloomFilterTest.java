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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.hash.Funnels;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class BloomFilterTest {

	@Test
	public void testAdd() {
		final BloomFilter<Void> bloomFilter = BloomFilter.create(10, 20); // High precision
		assertTrue(bloomFilter.add("test"));
		assertFalse(bloomFilter.add("test"));
		assertTrue(bloomFilter.add("foo"));
		assertTrue(bloomFilter.add("bar"));
		assertEquals(3, bloomFilter.size64());

		assertTrue(bloomFilter.contains("test"));
		assertTrue(bloomFilter.contains("foo"));
		assertTrue(bloomFilter.contains("bar"));

		assertFalse(bloomFilter.contains("42"));
		assertFalse(bloomFilter.contains("42"));

		bloomFilter.clear();
		assertTrue(bloomFilter.add(new byte[] { 0, 1 }));
		assertFalse(bloomFilter.add(new byte[] { 0, 1 }));
		assertTrue(bloomFilter.add(new byte[] { 1, 2 }));
		assertTrue(bloomFilter.add(new byte[] { 1, 0 }));
		assertEquals(3, bloomFilter.size64());
	}

	@Test
	public void testConflictsStrings() {
		final BloomFilter<Void> bloomFilter = BloomFilter.create(1000, 8);
		final LongOpenHashSet longs = new LongOpenHashSet();
		final SplitMix64RandomGenerator random = new SplitMix64RandomGenerator(1);

		for(int i = 1000; i-- != 0;) {
			final long l = random.nextLong();
			longs.add(l);
			final String s = Long.toBinaryString(l);
			bloomFilter.add(s);
			assertTrue(bloomFilter.contains(s));
		}

		assertEquals(longs.size(), bloomFilter.size64());
	}

	@Test
	public void testConflictsLongStrings() {
		final BloomFilter<Void> bloomFilter = BloomFilter.create(1000, 8);
		final ObjectOpenHashSet<String> strings = new ObjectOpenHashSet<>();
		final SplitMix64RandomGenerator random = new SplitMix64RandomGenerator(2);

		for(int i = 1000; i-- != 0;) {
			final StringBuilder s = new StringBuilder();
			for(int j = 0; j < 100; j++) s.append(Long.toBinaryString(random.nextLong()));
			strings.add(s.toString());
			bloomFilter.add(s);
			assertTrue(bloomFilter.contains(s));
		}

		assertEquals(strings.size(), bloomFilter.size64());
	}

	@Test
	public void testConflictsLongs() {
		final BloomFilter<Long> bloomFilter = new BloomFilter<>(1000, 8, Funnels.longFunnel());
		final LongOpenHashSet longs = new LongOpenHashSet();
		final SplitMix64RandomGenerator random = new SplitMix64RandomGenerator(2);

		for(int i = 1000; i-- != 0;) {
			final long l = random.nextLong();
			longs.add(l);
			final Long o = Long.valueOf(l);
			bloomFilter.add(o);
			assertTrue(bloomFilter.contains(o));
		}

		assertEquals(longs.size(), bloomFilter.size64());
	}

	@Test
	public void testConflictsByteArrays() {
		final BloomFilter<Void> bloomFilter = BloomFilter.create(1000, 8);
		final LongOpenHashSet longs = new LongOpenHashSet();
		final SplitMix64RandomGenerator random = new SplitMix64RandomGenerator(4);

		for(int i = 1000; i-- != 0;) {
			final long l = random.nextLong();
			longs.add(l);
			final byte[] o = Longs.toByteArray(l);
			bloomFilter.add(o);
			assertTrue(bloomFilter.contains(o));
		}

		assertEquals(longs.size(), bloomFilter.size64());
	}

	@Test
	public void testConflictsHashes() {
		final BloomFilter<Void> bloomFilter = BloomFilter.create(1000, 8);
		final LongOpenHashSet longs = new LongOpenHashSet();
		final SplitMix64RandomGenerator random = new SplitMix64RandomGenerator(1);

		for(int i = 1000; i-- != 0;) {
			final long l = random.nextLong();
			final long m = random.nextLong();
			longs.add(l ^ m);
			final byte[] o = Bytes.concat(Longs.toByteArray(l), Longs.toByteArray(m));
			bloomFilter.addHash(o);
			assertTrue(bloomFilter.containsHash(o));
		}

		assertEquals(longs.size(), bloomFilter.size64());
	}

	@Test
	public void testNegativeSeed() {
		final BloomFilter<Long> bloomFilter = new BloomFilter<>(1000, 8, Funnels.longFunnel());
		assertTrue(bloomFilter.add("test"));
		assertFalse(bloomFilter.add("test"));
	}

	@Ignore
	@Test
	public void testConflictsBig() {
		final BloomFilter<Long> bloomFilter = new BloomFilter<>(1000000000, 30, Funnels.longFunnel());
		final LongOpenHashSet longs = new LongOpenHashSet();
		final SplitMix64RandomGenerator random = new SplitMix64RandomGenerator(5);

		for(int i = 10000000; i-- != 0;) {
			final long l = random.nextLong();
			longs.add(l);
			final Long o = Long.valueOf(l);
			bloomFilter.add(o);
				}

		assertEquals(longs.size(), bloomFilter.size64());
	}

	@Test
	public void testZeroFunctions() {
		final BloomFilter<Void> bloomFilter = BloomFilter.create(10, 0);
		assertFalse(bloomFilter.add("test"));
		assertEquals(0, bloomFilter.size64());
	}
}
