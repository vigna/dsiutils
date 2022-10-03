/*
 * DSI utilities
 *
 * Copyright (C) 2012-2022 Sebastiano Vigna
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

import java.io.Serializable;

import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import it.unimi.dsi.Util;

/** A fast, high-quality 64-bit {@linkplain RandomGenerator pseudorandom number generator} described in &ldquo;Some long-period random number generators using shift
 * and xors&rdquo;, <i>ANZIAM Journal</i> 48, C188&minus;C202, 2007. */

public class XorGensRandomGenerator extends AbstractRandomGenerator implements Serializable {
	private static final long serialVersionUID = 0L;
	private static final int WLEN = 64;
	private static final int R = 64;
	private static final int S = 53;
	private static final int A = 33;
	private static final int B = 26;
	private static final int C = 27;
	private static final int D = 29;
	private static final long WEYL = 0x61c8864680b583ebL;
	/** State of the Xorshift generator. */
	private final long[] x = new long[R];
	private long weyl;
	private int i;

	/** Creates a new generator, initializing its seed with {@link Util#randomSeed()}. */
	public XorGensRandomGenerator() {
		this(Util.randomSeed());
	}

	/** Creates a new generator using a given seed.
	 *
	 * @param seed a nonzero seed for the generator (if zero, the generator will be seeded with -1).
	 */
	public XorGensRandomGenerator(final long seed) {
		setSeed(seed);
	}

	@Override
	public void setSeed(final long seed) {
		long v = seed == 0 ? -1 : seed; /* v must be nonzero */
		for (int k = WLEN; k > 0; k--) { /* Avoid correlations for close seeds */
			v ^= v << 10;
			v ^= v >>> 15; /* Recurrence has period 2**wlen-1 */
			v ^= v << 4;
			v ^= v >>> 13; /* for wlen = 32 or 64 */
		}

		for (int k = 0; k < R; k++) { /* Initialise circular array */
			v ^= v << 10;
			v ^= v >>> 15;
			v ^= v << 4;
			v ^= v >>> 13;
			x[k] = v;
		}

		i = R - 1;

		long t;
		for (int k = 4 * R; k > 0; k--) { /* Discard first 4*r results */
			t = x[i = (i + 1) & (R - 1)];
			t ^= t << A;
			t ^= t >>> B;
			v = x[(i + (R - S)) & (R - 1)];
			v ^= v << C;
			v ^= v >>> D;
			x[i] = t ^ v;
		}
	}

	@Override
	public long nextLong() {
		long t, v;
		t = x[i = (i + 1) & (R - 1)]; /* Assumes that r is a power of two */
		v = x[(i + (R - S)) & (R - 1)]; /* Index is (i-s) mod r */
		t ^= t << A;
		v ^= v << C;
		t ^= t >>> B; /* (I + L^a)(I + R^b) */
		v ^= v >>> D; /* (I + L^c)(I + R^d) */
		x[i] = (v ^= t); /* Update circular array */
		weyl += WEYL;
		return (v + (weyl ^ (weyl >>> 27)));
	}

	@Override
	public int nextInt() {
		return (int)nextLong();
	}

	@Override
	public int nextInt(final int n) {
		return (int)nextLong(n);
	}

	public long nextLong(final long n) {
        if (n <= 0) throw new IllegalArgumentException();
		for(;;) {
			final long bits = nextLong() >>> 1;
			final long value = bits % n;
			if (bits - value + (n - 1) >= 0) return value;
		}
	}

	@Override
	public double nextDouble() {
		return (nextLong() >>> 11) * 0x1.0p-53;
	}

	@Override
	public float nextFloat() {
		return (nextLong() >>> 40) * 0x1.0p-24f;
	}

	@Override
	public boolean nextBoolean() {
		return nextLong() < 0;
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		for (int i = bytes.length; i != 0;)
			for (long bits = nextLong(), n = Math.min(i, 8); n-- != 0; bits >>= 8) bytes[--i] = (byte)bits;
	}
}
