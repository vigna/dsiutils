/*
 * DSI utilities
 *
 * Copyright (C) 2011-2022 Sebastiano Vigna
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

import java.util.Random;

import it.unimi.dsi.Util;
import it.unimi.dsi.fastutil.HashCommon;

/** A fast, good-quality {@linkplain Random pseudorandom number generator}
 * that combines George Marsaglia's Xorshift
 * generators (described in <a href="http://www.jstatsoft.org/v08/i14/paper/">&ldquo;Xorshift RNGs&rdquo;</a>,
 * <i>Journal of Statistical Software</i>, 8:1&minus;6, 2003) with a multiplication.
 *
 * @deprecated Use {@link SplitMix64Random} instead.
 */
@Deprecated
public class XorShift64StarRandom extends Random {
	private static final long serialVersionUID = 1L;

	/** The internal state of the algorithm. */
	private long x;

	/** Creates a new generator seeded using {@link Util#randomSeed()}. */
	public XorShift64StarRandom() {
		this(Util.randomSeed());
	}

	/** Creates a new generator using a given seed.
	 *
	 * @param seed a nonzero seed for the generator (if zero, the generator will be seeded with {@link Long#MIN_VALUE}).
	 */
	public XorShift64StarRandom(final long seed) {
		super(seed);
	}

	@Override
	protected int next(final int bits) {
		return (int)(nextLong() & (1L << bits) - 1);
	}

	@Override
	public long nextLong() {
		x ^= x >>> 12;
		x ^= x << 25;
		return 2685821657736338717L * (x ^= (x >>> 27));
	}

	@Override
	public int nextInt() {
		return (int)nextLong();
	}

	/** Returns a pseudorandom, approximately uniformly distributed {@code int} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.
     *
     * <p>The hedge &ldquo;approximately&rdquo; is due to the fact that to be always
     * faster than <a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadLocalRandom.html"><code>ThreadLocalRandom</code></a>
     * we return
     * the upper 63 bits of {@link #nextLong()} modulo {@code n} instead of using
     * {@link Random}'s fancy algorithm (which {@link #nextLong(long)} uses though).
     * This choice introduces a bias: the numbers from 0 to 2<sup>63</sup> mod {@code n}
     * are slightly more likely than the other ones. In the worst case, &ldquo;more likely&rdquo;
     * means 1.00000000023 times more likely, which is in practice undetectable (actually,
     * due to the abysmally low quality of {@link Random}'s generator, the result is statistically
     * better in any case than {@link Random#nextInt(int)}'s) .
     *
     * <p>If for some reason you need truly uniform generation, just use {@link #nextLong(long)}.
     *
     * @param n the positive bound on the random number to be returned.
     * @return the next pseudorandom {@code int} value between {@code 0} (inclusive) and {@code n} (exclusive).
     */
	@Override
	public int nextInt(final int n) {
        if (n <= 0) throw new IllegalArgumentException();
		// No special provision for n power of two: all our bits are good.
        return (int)((nextLong() >>> 1) % n);
	}

	/** Returns a pseudorandom uniformly distributed {@code long} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence. The algorithm used to generate
     * the value guarantees that the result is uniform, provided that the
     * sequence of 64-bit values produced by this generator is.
     *
     * @param n the positive bound on the random number to be returned.
     * @return the next pseudorandom {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive).
     */
	public long nextLong(final long n) {
        if (n <= 0) throw new IllegalArgumentException();
		// No special provision for n power of two: all our bits are good.
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
		int i = bytes.length, n = 0;
		while(i != 0) {
			n = Math.min(i, 8);
			for (long bits = nextLong(); n-- != 0; bits >>= 8) bytes[--i] = (byte)bits;
		}
	}


	/** Sets the seed of this generator.
	 *
	 * <p>The seed will be passed through {@link HashCommon#murmurHash3(long)}. In this way, if the
	 * user passes a small value we will avoid the short irregular transient associated
	 * with states with a very small number of bits set.
	 *
	 * @param seed a nonzero seed for this generator (if zero, the generator will be seeded with {@link Long#MIN_VALUE}).
	 */
	@Override
	public void setSeed(final long seed) {
		x = HashCommon.murmurHash3(seed == 0 ? Long.MIN_VALUE : seed);
	}


	/** Sets the state of this generator.
	 *
	 * @param state the new state for this generator (must be nonzero).
	 */
	public void setState(final long state) {
		x = (state == 0 ? -1 : state);
	}
}
