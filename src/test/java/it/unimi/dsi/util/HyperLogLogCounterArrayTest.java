/*
 * DSI utilities
 *
 * Copyright (C) 2010-2023 Paolo Boldi and Sebastiano Vigna
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HyperLogLogCounterArrayTest {

	@Test
	public void testSingle() {
		final int numTrials = 10;

		for(final int size: new int[] { 1, 10, 100, 1000, 100000 })
			for(final int log2m: new int[] { 6, 8, 12 }) {
				final double rsd = HyperLogLogCounterArray.relativeStandardDeviation(log2m);
				int correct = 0;
				for (int trial = 0; trial < numTrials; trial++) {
					final HyperLogLogCounterArray a = new HyperLogLogCounterArray(1, size, log2m, trial);
					final int incr = (int)((1L << 32) / size);
					int x = Integer.MIN_VALUE;
					for(int i = 0; i < size; i++) {
						a.add(0, x);
						x += incr;
					}

					//System.err.println("Trial " + trial + ", size " + size + ", error: " + (size - a.count(0)) / size + " " + (Math.abs(size - a.count(0)) < 2 * rsd * size ? "(+)" : "(-)"));
					if (Math.abs(size - a.count(0)) / size < 2 * rsd) correct++;
				}

				//System.err.println("Correct trials for size " + size + ", rsd " + rsd + ": " + correct);
				assertTrue(correct + " < " + 9, correct >= 9);
			}
	}

	@Test
	public void testDouble() {
		final int numTrials = 10;

		for(final int size: new int[] { 1, 10, 100, 1000, 100000 })
			for(final int log2m: new int[] { 4, 6, 8, 12 }) {
				final double rsd = HyperLogLogCounterArray.relativeStandardDeviation(log2m);
				int correct0 = 0, correct1 = 0;
				for (int trial = 0; trial < numTrials; trial++) {
					final HyperLogLogCounterArray a = new HyperLogLogCounterArray(2, size, log2m, trial);
					final int incr = (int)((1L << 32) / size);
					int x = Integer.MIN_VALUE;
					for(int i = 0; i < size; i++) {
						a.add(0, x);
						a.add(1, x);
						x += incr;
					}

					//System.err.println("Trial " + trial + " (0), size " + size + ", error: " + (size - a.count(0)) / size + " " + (Math.abs(size - a.count(0)) < 2 * rsd * size ? "(+)" : "(-)"));
					//System.err.println("Trial " + trial + " (1), size " + size + ", error: " + (size - a.count(1)) / size + " " + (Math.abs(size - a.count(1)) < 2 * rsd * size ? "(+)" : "(-)"));
					if (Math.abs(size - a.count(0)) / size < 2 * rsd) correct0++;
					if (Math.abs(size - a.count(1)) / size < 2 * rsd) correct1++;
				}

				//System.err.println("Correct trials (0) for size " + size + ", rsd " + rsd + ": " + correct0);
				//System.err.println("Correct trials (1) for size " + size + ", rsd " + rsd + ": " + correct1);
				assertTrue(correct0 + " < " + 9, correct0 >= 9);
				assertTrue(correct1 + " < " + 9, correct1 >= 9);
			}
	}
}
