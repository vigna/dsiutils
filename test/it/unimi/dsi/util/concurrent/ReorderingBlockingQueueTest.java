/*
 * DSI utilities
 *
 * Copyright (C) 2017-2021 Sebastiano Vigna
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

package it.unimi.dsi.util.concurrent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.unimi.dsi.Util;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class ReorderingBlockingQueueTest {
	@Test
	public void testNoBlocking() throws InterruptedException {
		for(final int size: new int[] { 1, 10, 100, 128, 256 }) {
			final ReorderingBlockingQueue<Integer> q = new ReorderingBlockingQueue<>(size);
			final int[] perm = Util.identity(size);
			IntArrays.shuffle(perm, new XoRoShiRo128PlusRandom());
			for(int i = perm.length; i-- != 0;) q.put(Integer.valueOf(perm[i]), perm[i]);
			for(int i = 0; i < perm.length; i++) assertEquals(i, q.take().intValue());
			assertEquals(0, q.size());
		}
	}

	@Test
	public void testBlocking() throws InterruptedException {
		for(final int size: new int[] { 10, 100, 128, 256, 1024 }) {
			for(final int d: new int[] { 1, 2, 3, 4 }) {
				final ReorderingBlockingQueue<Integer> q = new ReorderingBlockingQueue<>(size / d);
				final int[] perm = Util.identity(size);
				IntArrays.shuffle(perm, new XoRoShiRo128PlusRandom());
				for(int i = perm.length; i-- != 0;) {
					final int t = perm[i];
					new Thread() {
						@Override
						public void run() {
							try {
								q.put(Integer.valueOf(t), t);
							}
							catch (final InterruptedException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
					}.start();
				}
				for(int i = 0; i < perm.length; i++) assertEquals(i, q.take().intValue());
				assertEquals(0, q.size());
			}
		}
	}
}
