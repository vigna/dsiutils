/*
 * DSI utilities
 *
 * Copyright (C) 2012-2023 Sebastiano Vigna
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

package it.unimi.dsi.stat;

import java.util.Random;

import it.unimi.dsi.Util;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.util.SplitMix64Random;

public class Ziggurat {
	private final Random random;
	private static double[] x = new double[257];
	private static double[] x53 = new double[257];
	private static double[] y = new double[257];
	private static double[] h = new double[257];
	private static double[] h53 = new double[257];
	private static long[] threshold = new long[257];

	static {
		final double area = 3.949659822581572e-3;

		/* Warm-up. This is the point defining the bottom (tail) part.
		 * Note that 7.697117470131487 is the target for x[1]
		 * (from the original code). */
		x[0] = 8.69711747013488555703;
		y[0] = 0;
		h[0] = area / x[0]; // Never used, but necessary to compute the remaining data.

		for (int i = 1; i < 256; i++) {
			y[i] = y[i - 1] + h[i - 1];
			x[i] = -Math.log(y[i]);
			h[i] = area / x[i];
		}

		for (int i = 1; i < 256; i++) {
			threshold[i] = Math.round((1L << 53) * x[i + 1] / x[i]);
			// System.err.println(x[i] + " " + y[i] + " " + threshold[i - 1]);
		}

		for (int i = 0; i < 256; i++) {
			x53[i] = x[i] / (1L << 53);
			h53[i] = h[i] / (1L << 53);
		}

		y[256] = 1; // x[256] is 0.
	}

	public Ziggurat() {
		this.random = new SplitMix64Random();
	}

	public Ziggurat(final Random random) {
		this.random = random;
	}

	public double nextDouble() {
		for(;;) {
			final long r = random.nextLong();
			// We never extract 256 because it would fail anyway.
			final int block = (int)(r & 0xFF);
			// This is a discrete representation of a uniformly chosen random number in [0..x[block]).
			final long discreteCandidate = r >>> 11;
			if (discreteCandidate < threshold[block]) {
				return discreteCandidate * x53[block];
			}
			if (block == 0) {
				// Slow but very rare.
				return x[1] - Math.log(random.nextDouble());
			}
			final double candidate = discreteCandidate * x53[block];
			if (y[block] + (random.nextLong() >>> 11) * h53[block] < Math.exp(-candidate)) return candidate;
		}
	}

	public static void main(final String arg[]) {
		final Ziggurat ziggurat = new Ziggurat(new SplitMix64Random());
		final long time = - System.currentTimeMillis();
		final int n = Integer.parseInt(arg[0]);
		double sum = 0;
		final int numExp = 100;
		final int exponentBits = 6;
		final int maxExponent = 2;
		final int minExponent = - (1 << exponentBits) + maxExponent + 1;
		for(int t = numExp; t-- != 0;) {
			int x = Integer.MAX_VALUE;
			for(int k = n; k-- != 0;) {
				x = Math.min(x, Math.max(Math.min(maxExponent, Fast.approximateLog2(ziggurat.nextDouble())), minExponent));
			}
			sum += Fast.pow2(x);
		}

		System.err.println(1000000.0 * (time + System.currentTimeMillis()) / (n * numExp) + " ns/gen");
		System.out.println(numExp / sum + " (" + Util.format(100 * (numExp / sum - n) / n) + "%)");
	}
}
