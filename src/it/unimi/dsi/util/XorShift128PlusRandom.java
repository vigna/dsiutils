/*
 * DSI utilities
 *
 * Copyright (C) 2013-2022 Sebastiano Vigna
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
import java.util.SplittableRandom;

import it.unimi.dsi.Util;

/** A fast, high-quality {@linkplain Random pseudorandom number generator} that
 * returns the sum of consecutive outputs of a Marsaglia Xorshift generator (described in <a
 * href="http://www.jstatsoft.org/v08/i14/paper/">&ldquo;Xorshift RNGs&rdquo;</a>, <i>Journal of
 * Statistical Software</i>, 8:1&minus;6, 2003) with 128 bits of state.
 * It is presently used in the JavaScript engines of
 * <a href="http://v8project.blogspot.com/2015/12/theres-mathrandom-and-then-theres.html">Chrome</a>,
 * <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=322529#c99">Firefox</a>,
 * <a href="https://bugs.webkit.org/show_bug.cgi?id=151641">Safari</a> and <a href="https://github.com/Microsoft/ChakraCore/commit/dbda0182dc0a983dfb37d90c05000e79b6fc75b0">Edge</a>.
 *
 * <p>By using the supplied {@link #jump()} method it is possible to generate non-overlapping long sequences
 * for parallel computations. This class provides also a {@link #split()} method to support recursive parallel computations, in the spirit of
 * {@link SplittableRandom}.
 *
 * <p><strong>Warning</strong>: before release 2.6.3, the {@link #split()} method
 * would not alter the state of the caller, and it would return instances initialized in the same
 * way if called multiple times. This was a major mistake in the implementation and it has been fixed,
 * but as a consequence the output of the caller after a call to {@link #split()} is
 * now different, and the result of {@link #split()} is initialized in a different way.
 *
 * @see it.unimi.dsi.util
 * @see Random
 * @see XoRoShiRo128PlusRandom
 * @see XorShift128PlusRandomGenerator
 * @deprecated Please use {@link XoRoShiRo128PlusRandom} instead.
 */
@Deprecated
public class XorShift128PlusRandom extends Random {
	private static final long serialVersionUID = 1L;

	/** The internal state of the algorithm. */
	private long s0, s1;

	/** Creates a new generator seeded using {@link Util#randomSeed()}. */
	public XorShift128PlusRandom() {
		this(Util.randomSeed());
	}

	/** Creates a new generator using a given seed.
	 *
	 * @param seed a seed for the generator.
	 */
	public XorShift128PlusRandom(final long seed) {
		setSeed(seed);
	}

	@Override
	public long nextLong() {
		long s1 = this.s0;
		final long s0 = this.s1;
		this.s0 = s0;
		s1 ^= s1 << 23;
		return (this.s1 = (s1 ^ s0 ^ (s1 >>> 18) ^ (s0 >>> 5))) + s0;
	}

	@Override
	public int nextInt() {
		return (int)nextLong();
	}

	@Override
	public int nextInt(final int n) {
		return (int)nextLong(n);
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

	private static final long[] JUMP = { 0x8a5cd789635d2dffL, 0x121fd2155c472f96L };

	/** The jump function for this generator. It is equivalent to 2<sup>64</sup>
	 * calls to {@link #nextLong()}; it can be used to generate 2<sup>64</sup>
	 * non-overlapping subsequences for parallel computations. */

	public void jump() {
		long s0 = 0;
		long s1 = 0;
		for (final long element : JUMP)
			for(int b = 0; b < 64; b++) {
				if ((element & 1L << b) != 0) {
					s0 ^= this.s0;
					s1 ^= this.s1;
				}
				nextLong();
			}

		this.s0 = s0;
		this.s1 = s1;
	}

	/**
	 * Returns a new instance that shares no mutable state
	 * with this instance. The sequence generated by the new instance
	 * depends deterministically from the state of this instance,
	 * but the probability that the sequence generated by this
	 * instance and by the new instance overlap is negligible.
	 *
	 * <p><strong>Warning</strong>: before release 2.6.3, this method
	 * would not alter the state of the caller, and it would return instances initialized in the same
	 * way if called multiple times. This was a major mistake in the implementation and it has been fixed,
	 * but as a consequence the output of this instance after a call to this method is
	 * now different, and the returned instance is initialized in a different way.
	 *
	 * @return the new instance.
	 */
	public XorShift128PlusRandom split() {
		nextLong();
		final XorShift128PlusRandom split = new XorShift128PlusRandom(0);

		long h0 = s0;
		long h1 = s1;
		long h2 = s0 + 0x55a650a4c1dac3e9L; // Random constants
		long h3 = s1 + 0xb39ae98dfa439b73L;

		// A round of SpookyHash ShortMix
		h2 = Long.rotateLeft(h2, 50);
		h2 += h3;
		h0 ^= h2;
		h3 = Long.rotateLeft(h3, 52);
		h3 += h0;
		h1 ^= h3;
		h0 = Long.rotateLeft(h0, 30);
		h0 += h1;
		h2 ^= h0;
		h1 = Long.rotateLeft(h1, 41);
		h1 += h2;
		h3 ^= h1;
		h2 = Long.rotateLeft(h2, 54);
		h2 += h3;
		h0 ^= h2;
		h3 = Long.rotateLeft(h3, 48);
		h3 += h0;
		h1 ^= h3;
		h0 = Long.rotateLeft(h0, 38);
		h0 += h1;
		h2 ^= h0;
		h1 = Long.rotateLeft(h1, 37);
		h1 += h2;
		h3 ^= h1;
		h2 = Long.rotateLeft(h2, 62);
		h2 += h3;
		h0 ^= h2;
		h3 = Long.rotateLeft(h3, 34);
		h3 += h0;
		h1 ^= h3;
		h0 = Long.rotateLeft(h0, 5);
		h0 += h1;
		h2 ^= h0;
		h1 = Long.rotateLeft(h1, 36);
		h1 += h2;
		//h3 ^= h1;

		split.s0 = h0;
		split.s1 = h1;

		return split;
	}

	/** Sets the seed of this generator.
	 *
	 * <p>The argument will be used to seed a {@link SplitMix64RandomGenerator}, whose output
	 * will in turn be used to seed this generator. This approach makes &ldquo;warmup&rdquo; unnecessary,
	 * and makes the probability of starting from a state
	 * with a large fraction of bits set to zero astronomically small.
	 *
	 * @param seed a seed for this generator.
	 */
	@Override
	public void setSeed(final long seed) {
		final SplitMix64RandomGenerator r = new SplitMix64RandomGenerator(seed);
		s0 = r.nextLong();
		s1 = r.nextLong();
	}


	/** Sets the state of this generator.
	 *
	 * <p>The internal state of the generator will be reset, and the state array filled with the provided array.
	 *
	 * @param state an array of 2 longs; at least one must be nonzero.
	 */
	public void setState(final long[] state) {
		if (state.length != 2) throw new IllegalArgumentException("The argument array contains " + state.length + " longs instead of " + 2);
		s0 = state[0];
		s1 = state[1];
	}
}
