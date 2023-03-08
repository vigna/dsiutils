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

import it.unimi.dsi.logging.ProgressLogger;

public class HyperLogLogCounterArraySlowTest {
	@Test
	public void testLarge() {
		final int numTrials = 10;

		for(final long size: new long[] { 1000000000, 4000000000L })
			for(final int log2m: new int[] { 6, 8, 12 }) {
				final double rsd = HyperLogLogCounterArray.relativeStandardDeviation(log2m);
				int correct = 0;
				for (int trial = 0; trial < numTrials; trial++) {
					final HyperLogLogCounterArray a = new HyperLogLogCounterArray(1, size, log2m, trial);
					final long incr = (1L << 60) / size;
					long x = Long.MIN_VALUE;
					for(long i = size; i-- != 0;) {
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
	public void testExtreme() {
		final int numTrials = 20, mustBeCorrect = 18;
		final ProgressLogger pl = new ProgressLogger();

		for(final int log2m: new int[] { 6, 8, 12 }) {
			final double rsd = HyperLogLogCounterArray.relativeStandardDeviation(log2m);
			int correct = 0;
			final long size = 3L * 1024 * 1024 * 1024;
			for (int trial = 0; trial < numTrials; trial++) {
				final HyperLogLogCounterArray a = new HyperLogLogCounterArray(1, size, log2m, trial);
				pl.start();
				for(int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) if (i % 4 != 0) a.add(0, i);
				pl.stop();
				pl.count = size;
				//System.err.println(pl);
				//System.err.println("Trial " + trial + ", error: " + (size - a.count(0)) / size + " " + (Math.abs(size - a.count(0)) < 2 * rsd * size ? "(+)" : "(-)"));
				if (Math.abs(size - a.count(0)) < 2 * rsd * size) correct++;
			}

			//System.err.println("Correct trials for size " + size + ", rsd " + rsd + ": " + correct);
			assertTrue(correct + " < " + mustBeCorrect, correct >= mustBeCorrect);
		}

		for(final int log2m: new int[] { 6, 8, 12 }) {
			final double rsd = HyperLogLogCounterArray.relativeStandardDeviation(log2m);
			int correct = 0;
			final long size = 7L * 512 * 1024 * 1024;
			for (int trial = 0; trial < numTrials; trial++) {
				final HyperLogLogCounterArray a = new HyperLogLogCounterArray(1, size, log2m, trial);
				pl.start();
				for(int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) if (i % 8 != 0) a.add(0, i);
				pl.stop();
				pl.count = size;
				//System.err.println(pl);
				//System.err.println("Trial " + trial + ", error: " + (size - a.count(0)) / size + " " + (Math.abs(size - a.count(0)) < 2 * rsd * size ? "(+)" : "(-)"));
				if (Math.abs(size - a.count(0)) / size < 2 * rsd) correct++;
			}

			//System.err.println("Correct trials for size " + size + ", rsd " + rsd + ": " + correct);
			assertTrue(correct + " < " + mustBeCorrect, mustBeCorrect >= 9);
		}

		for(final int log2m: new int[] { 6, 8, 12 }) {
			final double rsd = HyperLogLogCounterArray.relativeStandardDeviation(log2m);
			int correct = 0;
			final long size = 8L * 1024 * 1024 * 1024;
			for (int trial = 0; trial < numTrials; trial++) {
				final HyperLogLogCounterArray a = new HyperLogLogCounterArray(1, size, log2m, trial);
				pl.start();
				for(int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) if (i % 8 != 0) a.add(0, i);
				pl.stop();
				pl.count = size;
				//System.err.println(pl);
				//System.err.println("Trial " + trial + ", error: " + (size - a.count(0)) / size + " " + (Math.abs(size - a.count(0)) < 2 * rsd * size ? "(+)" : "(-)"));
				if (Math.abs(size - a.count(0)) / size < 2 * rsd) correct++;
			}

			//System.err.println("Correct trials for size " + size + ", rsd " + rsd + ": " + correct);
			assertTrue(correct + " < " + mustBeCorrect, mustBeCorrect >= 9);
		}
	}
}
