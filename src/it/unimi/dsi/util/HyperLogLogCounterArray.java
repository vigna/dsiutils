/*
 * DSI utilities
 *
 * Copyright (C) 2010-2021 Paolo Boldi and Sebastiano Vigna
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

import static it.unimi.dsi.bits.LongArrayBitVector.bit;
import static it.unimi.dsi.bits.LongArrayBitVector.bits;
import static it.unimi.dsi.bits.LongArrayBitVector.word;

import java.io.Serializable;
import java.util.Arrays;

import it.unimi.dsi.Util;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;

/**
 * An array of approximate sets each represented using a HyperLogLog counter.
 *
 * <p>HyperLogLog counters represent the number of elements of a set in an approximate way. They have been
 * introduced by Philippe Flajolet, &Eacute;ric Fusy, Olivier Gandouet, and Fre&eacute;de&eacute;ric Meunier in
 * &ldquo;HyperLogLog: the analysis of a near-optimal cardinality estimation algorithm&rdquo;,
 * <em>Proceedings of the 13th conference on analysis of algorithm (AofA 07)</em>, pages
 * 127&minus;146, 2007. They are an improvement over the basic idea of <em>loglog counting</em>, introduced by
 * Marianne Durand and Philippe Flajolet in &ldquo;Loglog counting of large cardinalities&rdquo;,
 * <i>ESA 2003, 11th Annual European Symposium</i>, volume 2832 of Lecture Notes in Computer Science, pages 605&minus;617, Springer, 2003.
 *
 * <p>Each counter is composed by {@link #m} registers, and each register is made of {@link #registerSize} bits.
 * The first number depends on the desired relative standard deviation, and its logarithm can be computed using {@link #log2NumberOfRegisters(double)},
 * whereas the second number depends on an upper bound on the number of distinct elements to be counted, and it can be computed
 * using {@link #registerSize(long)}.
 *
 * <p>Actually, this class implements an <em>array</em> of counters. Each counter is completely independent, but they all use the same hash function.
 * The reason for this design is that in our intended applications hundred of millions of counters are common, and the JVM overhead to create such a number of objects
 * would be unbearable. This class allocates an array of {@link LongArrayBitVector}s, each containing {@link #CHUNK_SIZE} registers,
 * and can thus handle billions of billions of registers efficiently (in turn, this means being able to
 * handle an array of millions of billions of high-precision counters).
 *
 * <p>When creating an instance, you can choose the size of the array (i.e., the number of counters) and the desired relative standard deviation
 * (either {@linkplain #HyperLogLogCounterArray(long, long, double) explicitly} or
 * {@linkplain #HyperLogLogCounterArray(long, long, int) choosing the number of registers per counter}).
 * Then, you can {@linkplain #add(long, long) add an element to a counter}. At any time, you can
 * {@linkplain #count(long) count} count (approximately) the number of distinct elements that have been added to a counter.
 *
 * <p>If you need to reuse this class multiple times, you can {@linkplain #clear() clear all registers}, possibly {@linkplain #clear(long) setting a new seed}.
 * The seed is used to compute the hash function used by the HyperLogLog counters.
 *
 *
 * <h2>Utility methods</h2>
 *
 * <p>This class provides a number of utility methods that make it possible to {@linkplain #getCounter(long, long[]) extract a counter as an array of longs},
 * {@linkplain #setCounter(long[], long) set the contents of a counter given an array of longs}, and
 * {@linkplain #max(long[], long[]) maximize quickly two counters given as arrays of longs}.
 *
 * @author Paolo Boldi
 * @author Sebastiano Vigna
 */

public class HyperLogLogCounterArray implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final boolean ASSERTS = false;
	private static final boolean DEBUG = false;

	/** The logarithm of the maximum size in registers of a bit vector. */
	public static final int CHUNK_SHIFT = 30;
	/** The maximum size in registers of a bit vector. */
	public static final long CHUNK_SIZE = 1L << CHUNK_SHIFT;
	/** The mask used to obtain an register offset in a chunk. */
	public static final long CHUNK_MASK = CHUNK_SIZE - 1;

	/** The correct value for &alpha;, multiplied by {@link #m}<sup>2</sup> (see the paper). */
	private double alphaMM;
	/** The number of registers minus one. */
	protected final int mMinus1;
	/** An array of arrays of longs containing all registers. */
	protected final long bits[][];
	/** {@link #registerSize}-bit views of {@link #bits}. */
	protected final LongBigList registers[];
	/** The shift that selects the chunk corresponding to a counter. */
	protected final int counterShift;
	/** A seed for hashing. */
	protected long seed;
	/** The mask OR'd with the output of the hash function so that {@link Long#numberOfTrailingZeros(long)} does not return too large a value. */
	private final long sentinelMask;
	/** Whether counters are aligned to longwords. */
	protected final boolean longwordAligned;
	/** A mask for the residual bits of a counter (the {@link #counterSize} <code>%</code> {@link Long#SIZE} lowest bits). */
	protected final long counterResidualMask;
	/** A mask containing a one in the most significant bit of each register (i.e., in positions of the form {@link #registerSize registerSize * (i + 1) - 1}). */
	protected final long[] msbMask;
	/** A mask containing a one in the least significant bit of each register (i.e., in positions of the form {@link #registerSize registerSize * i}). */
	protected final long[] lsbMask;
	/** The logarithm of the number of registers per counter (at most 30). */
	public final int log2m;
	/** The number of registers per counter. */
	public final int m;
	/** The size in bits of each register. */
	public final int registerSize;
	/** The size in bits of each counter ({@link #registerSize} <code>*</code> {@link #m}). */
	public final int counterSize;
	/** The size of a counter in longwords (ceiled if there are less then {@link Long#SIZE} registers per counter). */
	public final int counterLongwords;

	/**
	 * Returns the logarithm of the number of registers per counter that are necessary to attain a
	 * given relative standard deviation.
	 *
	 * @param rsd the relative standard deviation to be attained.
	 * @return the logarithm of the number of registers that are necessary to attain relative standard deviation <code>rsd</code>.
	 */
	public static int log2NumberOfRegisters(final double rsd) {
		// 1.106 is valid for 16 registers or more.
		return (int)Math.ceil(Fast.log2((1.106 / rsd) * (1.106 / rsd)));
	}

	/**
	 * Returns the relative standard deviation corresponding to a given logarithm of the number of registers per counter.
	 *
	 * @param log2m the logarithm of the number of registers per counter (at most 30).
	 * @return the resulting relative standard deviation.
	 */
	public static double relativeStandardDeviation(final int log2m) {
		return (log2m == 4 ? 1.106 : log2m == 5 ? 1.070 : log2m == 6 ? 1.054 : log2m == 7 ? 1.046 : 1.04) / Math.sqrt(1 << log2m);
	}

	/**
	 * Returns the register size in bits, given an upper bound on the number of distinct elements.
	 *
	 * @param n an upper bound on the number of distinct elements.
	 * @return the register size in bits.
	 */

	public static int registerSize(final long n) {
		return Math.max(5, (int)Math.ceil(Math.log(Math.log(n) / Math.log(2)) / Math.log(2)));
	}

	/** Returns the chunk of a given counter.
	 *
	 * @param counter a counter.
	 * @return its chunk.
	 */
	public int chunk(final long counter) {
		return (int)(counter >>> counterShift);
	}

	/** Returns the bit offset of a given counter in its chunk.
	 *
	 * @param counter a counter.
	 * @return the starting bit of the given counter in its chunk.
	 */
	public long offset(final long counter) {
		return (counter << log2m & CHUNK_MASK) * registerSize;
	}

	/**
	 * Creates a new array of counters.
	 *
	 * @param arraySize the number of counters.
	 * @param n the expected number of elements.
	 * @param rsd the relative standard deviation.
	 */
	public HyperLogLogCounterArray(final long arraySize, final long n, final double rsd) {
		this(arraySize, n, log2NumberOfRegisters(rsd));
	}

	/**
	 * Creates a new array of counters.
	 *
	 * @param arraySize the number of counters.
	 * @param n the expected number of elements.
	 * @param log2m the logarithm of the number of registers per counter (at most 30).
	 */
	public HyperLogLogCounterArray(final long arraySize, final long n, final int log2m) {
		this(arraySize, n, log2m, Util.randomSeed());
	}

	/**
	 * Creates a new array of counters.
	 *
	 * @param arraySize the number of counters.
	 * @param n the expected number of elements.
	 * @param log2m the logarithm of the number of registers per counter (at most 30).
	 * @param seed the seed used to compute the hash function.
	 */
	public HyperLogLogCounterArray(final long arraySize, final long n, final int log2m, final long seed) {
		if (log2m > Integer.SIZE - 2) throw new IllegalArgumentException("The logarithm of the number of register per counter (" + log2m + ") is too large");
		this.m = 1 << (this.log2m = log2m);
		this.mMinus1 = m - 1;
		this.registerSize = registerSize(n);
		this.counterSize = registerSize << log2m;

		counterShift = CHUNK_SHIFT - log2m;
		sentinelMask = 1L << (1 << registerSize) - 2;

		final long sizeInRegisters = arraySize * m;
		final int numVectors = (int)((sizeInRegisters + CHUNK_MASK) >>> CHUNK_SHIFT);

		bits = new long[numVectors][];
		registers = new LongBigList[numVectors];

		for(int i = 0; i < numVectors; i++) {
			final LongArrayBitVector bitVector = LongArrayBitVector.ofLength(registerSize * Math.min(CHUNK_SIZE, sizeInRegisters - ((long)i << CHUNK_SHIFT)));
			bits[i] = bitVector.bits();
			registers[i] = bitVector.asLongBigList(registerSize);
		}

		counterLongwords = LongArrayBitVector.words(counterSize);
		counterResidualMask = (1L << counterSize) - 1;
		longwordAligned = LongArrayBitVector.round(counterSize);

		// We initialize the masks for the broadword code in max().
		msbMask = new long[counterLongwords];
		lsbMask = new long[counterLongwords];
		for (int i = registerSize - 1; i < bits(msbMask.length); i += registerSize) msbMask[word(i)] |= 1L << i;
		for (int i = 0; i < bits(lsbMask.length); i += registerSize) lsbMask[word(i)] |= 1L << i;

		this.seed = seed;
		if (DEBUG) System.err.println("Register size: " + registerSize + " log2m (b): " + log2m + " m: " + m);
		// See the paper.
		switch (log2m) {
		case 4:
			alphaMM = 0.673 * m * m; break;
		case 5:
			alphaMM = 0.697 * m * m; break;
		case 6:
			alphaMM = 0.709 * m * m; break;
		default:
			alphaMM = (0.7213 / (1 + 1.079 / m)) * m * m;
		}
	}

	/** Clears all registers and sets a new seed (e.g., using {@link Util#randomSeed()}).
	 *
	 * @param seed the new seed used to compute the hash function
	 */
	public void clear(final long seed) {
		clear();
		this.seed = seed;
	}

	/** Clears all registers. */
	public void clear() {
		for(final long[] a: bits) Arrays.fill(a, 0);
	}

	private final static long jenkins(final long x, final long seed) {
		long a, b, c;

		/* Set up the internal state */
		a = seed + x;
		b = seed;
		c = 0x9e3779b97f4a7c13L; /* the golden ratio; an arbitrary value */

		a -= b; a -= c; a ^= (c >>> 43);
		b -= c; b -= a; b ^= (a << 9);
		c -= a; c -= b; c ^= (b >>> 8);
		a -= b; a -= c; a ^= (c >>> 38);
		b -= c; b -= a; b ^= (a << 23);
		c -= a; c -= b; c ^= (b >>> 5);
		a -= b; a -= c; a ^= (c >>> 35);
		b -= c; b -= a; b ^= (a << 49);
		c -= a; c -= b; c ^= (b >>> 11);
		a -= b; a -= c; a ^= (c >>> 12);
		b -= c; b -= a; b ^= (a << 18);
		c -= a; c -= b; c ^= (b >>> 22);

		return c;
	}


	/** Adds an element to a counter.
	 *
	 * @param k the index of the counter.
	 * @param v the element to be added.
	 */
	public void add(final long k, final long v) {
		final long x = jenkins(v, seed);
		final int j = (int)(x & mMinus1);
		final int r = Long.numberOfTrailingZeros(x >>> log2m | sentinelMask);
		assert r < (1 << registerSize) - 1;
		assert r >= 0;
		final LongBigList l = registers[chunk(k)];
		final long offset = ((k << log2m) + j) & CHUNK_MASK;
		l.set(offset, Math.max(r + 1, l.getLong(offset)));
	}

	/** Returns the array of big lists of registers underlying this array of counters.
	 *
	 * <p>The main purpose of this method is debugging, as it makes comparing
	 * the evolution of the state of two implementations easy.
	 *
	 * @return the array of big lists of registers underlying this array of counters.
	 */

	public LongBigList[] registers() {
		return registers;
	}


	/** Estimates the number of distinct elements that have been added to a given counter so far.
	 *
	 * <p>This is an low-level method that should be used only after having understood in detail
	 * the inner workings of this class.
	 *
	 * @param bits the bit array containing the counter.
	 * @param offset the starting bit position of the counter in <code>bits</code>.
	 * @return an approximation of the number of distinct elements that have been added to counter so far.
	 */
	public double count(final long[] bits, final long offset) {
		int remaining = Long.SIZE - (int)(offset & ~-Long.SIZE);
		int word = word(offset);
		long curr = bits[word] >>> offset;

		final int registerSize = this.registerSize;
		final int mask = (1 << registerSize) - 1;

		double s = 0;
		int zeroes = 0;
		long r;

		for (int j = m; j-- != 0;) {
			if (remaining >= registerSize) {
				r = curr & mask;
				curr >>>= registerSize;
				remaining -= registerSize;
			}
			else {
				r = (curr | bits[++word] << remaining) & mask;
				curr = bits[word] >>> registerSize - remaining;
				remaining += Long.SIZE - registerSize;
			}

			if (r == 0) zeroes++;
			s += 1. / (1L << r);
		}

		s = alphaMM / s;
		if (DEBUG) System.err.println("Zeroes: " + zeroes);
		if (zeroes != 0 && s < 5. * m / 2) {
			if (DEBUG) System.err.println("Small range correction");
			return m * Math.log((double)m / zeroes);
		}
		else return s;
	}

	/** Estimates the number of distinct elements that have been added to a given counter so far.
	 *
	 * @param k the index of the counter.
	 * @return an approximation of the number of distinct elements that have been added to counter <code>k</code> so far.
	 */
	public double count(final long k) {
		return count(bits[chunk(k)], offset(k));
	}


	/** Sets the contents of a counter of this {@link HyperLogLogCounterArray} using a provided array of longs.
	 *
	 * <p><strong>Warning</strong>: this is a low-level method. You must know what you're doing.
	 *
	 * @param source an array of at least {@link #counterLongwords} longs containing a counter.
	 * @param chunkBits the array where the counter will be stored.
	 * @param index the index of the counter that will be filled with the provided array.
	 * @see #getCounter(long[], long, long[])
	 */
	public final void setCounter(final long[] source, final long[] chunkBits, final long index) {
		if (longwordAligned) System.arraycopy(source, 0, chunkBits, word(offset(index)), counterLongwords);
		else {
			// Offset in bits
			final long offset = offset(index);
			// Offsets in elements in the array
			final int longwordOffset = word(offset);
			// Offset in bits in the word of index longwordOffset
			final int bitOffset = bit(offset);
			final int last = counterLongwords - 1;

			if (bitOffset == 0) {
				for(int i = last; i-- != 0;) chunkBits[longwordOffset + i] = source[i];
				chunkBits[longwordOffset + last] &= ~counterResidualMask;
				chunkBits[longwordOffset + last] |= source[last] & counterResidualMask;
			}
			else {
				chunkBits[longwordOffset] &= (1L << bitOffset) - 1;
				chunkBits[longwordOffset] |= source[0] << bitOffset;

				for (int i = 1; i < last; i++) chunkBits[longwordOffset + i] = source[i - 1] >>> -bitOffset | source[i] << bitOffset;

				final int remaining = (counterSize & ~-Long.SIZE) + bitOffset;

				final long mask = -1L >>> Math.min(0, -remaining);
				chunkBits[longwordOffset + last] &= ~mask;
				chunkBits[longwordOffset + last] |= mask & (source[last - 1] >>> -bitOffset | source[last] << bitOffset);

				// Note that it is impossible to enter in this conditional unless you use 7 or more bits per register, which is unlikely.
				if (remaining > Long.SIZE) {
					final long mask2 = (1L << remaining) - 1;
					chunkBits[longwordOffset + last + 1] &= ~mask2;
					chunkBits[longwordOffset + last + 1] |= mask2 & (source[last] >>> -bitOffset);
				}
			}

			if (ASSERTS) {
				final LongArrayBitVector l = LongArrayBitVector.wrap(chunkBits);
				for (int i = 0; i < counterSize; i++) assert l.getBoolean(offset + i) == ((source[word(i)] & (1L << i)) != 0);
			}
		}
	}

	/** Sets the contents of a counter of this {@link HyperLogLogCounterArray} using a provided array of longs.
	 *
	 * <p><strong>Warning</strong>: this is a low-level method. You must know what you're doing.
	 *
	 * @param source an array of at least {@link #counterLongwords} longs containing a counter.
	 * @param index the index of the counter that will be filled with the provided array.
	 * @see #setCounter(long[], long[], long)
	 */
	public void setCounter(final long[] source, final long index) {
		setCounter(source, bits[chunk(index)], index);
	}

	/** Extracts a counter from this {@link HyperLogLogCounterArray} and writes it into an array of longs.
	 *
	 * <p><strong>Warning</strong>: this is a low-level method. You must know what you're doing.
	 *
	 * @param chunkBits the array storing the counter.
	 * @param index the index of the counter to be extracted.
	 * @param dest an array of at least {@link #counterLongwords} longs where the counter of given index will be written.
	 * @see #setCounter(long[], long[], long)
	 */
	public final void getCounter(final long[] chunkBits, final long index, final long[] dest) {
		if (longwordAligned) System.arraycopy(chunkBits, word(offset(index)), dest, 0, counterLongwords);
		else {
			// Offset in bits
			final long offset = offset(index);
			// Offsets in elements in the array
			final int longwordOffset = word(offset);
			// Offset in bits in the word of index longwordOffset
			final int bitOffset = bit(offset);
			final int last = counterLongwords - 1;

			if (bitOffset == 0) {
				for(int i = last; i-- != 0;) dest[i] = chunkBits[longwordOffset + i];
				dest[last] = chunkBits[longwordOffset + last] & counterResidualMask;
			}
			else {
				for (int i = 0; i < last; i++) dest[i] = chunkBits[longwordOffset + i] >>> bitOffset | chunkBits[longwordOffset + i + 1] << -bitOffset;
				dest[last] = chunkBits[longwordOffset + last] >>> bitOffset & counterResidualMask;
			}
		}
	}


	/** Extracts a counter from this {@link HyperLogLogCounterArray} and writes it into an array of longs.
	 *
	 * <p><strong>Warning</strong>: this is a low-level method. You must know what you're doing.
	 *
	 * @param index the index of the counter to be extracted.
	 * @param dest an array of at least {@link #counterLongwords} longs where the counter of given index will be written.
	 * @see #getCounter(long[], long, long[])
	 */
	public final void getCounter(final long index, final long[] dest) {
		getCounter(bits[chunk(index)], index, dest);
	}

	/** Transfers the content of a counter between two parallel array of longwords.
	 *
	 * <p><strong>Warning</strong>: this is a low-level method. You must know what you're doing.
	 *
	 * @param source the source array.
	 * @param dest the destination array.
	 * @param node the node number.
	 */
	public final void transfer(final long[] source, final long[] dest, final long node) {
		if (longwordAligned) {
			final int longwordOffset = word(offset(node));
			System.arraycopy(source, longwordOffset, dest, longwordOffset, counterLongwords);
		}
		else {
			// Offset in bits in the array
			final long offset = offset(node);
			// Offsets in elements in the array
			final int longwordOffset = word(offset);
			// Offset in bits in the word of index longwordOffset
			final int bitOffset = bit(offset);
			final int last = counterLongwords - 1;

			if (bitOffset == 0) {
				for(int i = last; i-- != 0;) dest[longwordOffset + i] = source[longwordOffset + i];
				dest[longwordOffset + last] &= ~counterResidualMask;
				dest[longwordOffset + last] |= source[longwordOffset + last] & counterResidualMask;
			}
			else {
				final long mask = -1L << bitOffset;
				dest[longwordOffset] &= ~mask;
				dest[longwordOffset] |= source[longwordOffset] & mask;

				for(int i = 1; i < last; i++) dest[longwordOffset + i] = source[longwordOffset + i];

				final int remaining = (counterSize + bitOffset) & ~-Long.SIZE;
				if (remaining == 0) dest[longwordOffset + last] = source[longwordOffset + last];
				else {
					final long mask2 = (1L << remaining) - 1;
					dest[longwordOffset + last] &= ~mask2;
					dest[longwordOffset + last] |= mask2 & source[longwordOffset + last];
				}
			}

			if (ASSERTS) {
				final LongArrayBitVector aa = LongArrayBitVector.wrap(source);
				final LongArrayBitVector bb = LongArrayBitVector.wrap(dest);
				for(int i = 0; i < counterSize; i++) assert aa.getBoolean(offset + i) == bb.getBoolean(offset + i);
			}
		}
	}


	/** Performs a multiple precision subtraction, leaving the result in the first operand.
	 *
	 * @param x an array of longs.
	 * @param y an array of longs that will be subtracted from <code>x</code>.
	 * @param l the length of <code>x</code> and <code>y</code>.
	 */
	private static final void subtract(final long[] x, final long[] y, final int l) {
		boolean borrow = false;

		for(int i = 0; i < l; i++) {
			if (! borrow || x[i]-- != 0) borrow = x[i] < y[i] ^ x[i] < 0 ^ y[i] < 0; // This expression returns the result of an unsigned strict comparison.
			x[i] -= y[i];
		}
	}

	/** Computes the register-by-register maximum of two counters.
	 *
	 * <p>This method will allocate two temporary arrays. To reduce object creation, use {@link #max(long[], long[], long[], long[])}.
	 *
	 * @param x a first array of at least {@link #counterLongwords} longs containing a counter.
	 * @param y a second array of at least {@link #counterLongwords} longs containing a counter.
	 */
	public final void max(final long[] x, final long[] y) {
		max(x, y, new long[x.length], new long[y.length]);
	}

	/** Computes the register-by-register maximum of two counters.
	 *
	 * @param x a first array of at least {@link #counterLongwords} longs containing a counter.
	 * @param y a second array of at least {@link #counterLongwords} longs containing a counter.
	 * @param accumulator a support array of at least {@link #counterLongwords} longs.
	 * @param mask a support array of at least {@link #counterLongwords} longs.
	 */
	public final void max(final long[] x, final long[] y, final long[] accumulator, final long[] mask) {
		final int l = x.length;
		final long[] msbMask = this.msbMask;

		/* We work in two phases. Let H_r (msbMask) by the mask with the
		 * highest bit of each register (of size r) set, and L_r (lsbMask)
		 * be the mask with the lowest bit of each register set.
		 * We describe the algorithm on a single word.
		 *
		 * If the first phase we perform an unsigned strict register-by-register
		 * comparison of x and y, using the formula
		 *
		 * z = ((((y | H_r) - (x & ~H_r)) | (y ^ x))^ (y | ~x)) & H_r
		 *
		 * Then, we generate a register-by-register mask of all ones or
		 * all zeroes, depending on the result of the comparison, using the
		 * formula
		 *
		 * (((z >> r-1 | H_r) - L_r) | H_r) ^ z
		 *
		 * At that point, it is trivial to select from x and y the right values.
		 */

		// We load y | H_r into the accumulator.
		for(int i = l; i-- != 0;) accumulator[i] = y[i] | msbMask[i];
		// We subtract x & ~H_r, using mask as temporary storage
		for(int i = l; i-- != 0;) mask[i] = x[i] & ~msbMask[i];
		subtract(accumulator, mask, l);

		// We OR with x ^ y, XOR with (x | ~y), and finally AND with H_r.
		for(int i = l; i-- != 0;) accumulator[i] = ((accumulator[i] | (y[i] ^ x[i])) ^ (y[i] | ~x[i])) & msbMask[i];

		if (ASSERTS) {
			final LongBigList a = LongArrayBitVector.wrap(x).asLongBigList(registerSize);
			final LongBigList b = LongArrayBitVector.wrap(y).asLongBigList(registerSize);
			for(int i = 0; i < m; i++) {
				final long pos = (i + 1) * (long)registerSize - 1;
				assert (b.getLong(i) < a.getLong(i)) == ((accumulator[word(pos)] & 1L << pos) != 0);
			}
		}

		// We shift by registerSize - 1 places and put the result into mask.
		final int rMinus1 = registerSize - 1;
		for(int i = l - 1; i-- != 0;) mask[i] = accumulator[i] >>> rMinus1 | accumulator[i + 1] << -rMinus1 | msbMask[i];
		mask[l - 1] = accumulator[l - 1] >>> rMinus1 | msbMask[l - 1];

		// We subtract L_r from mask.
		subtract(mask, lsbMask, l);

		// We OR with H_r and XOR with the accumulator.
		for(int i = l; i-- != 0;) mask[i] = (mask[i] | msbMask[i]) ^ accumulator[i];

		if (ASSERTS) {
			final long[] t = x.clone();
			final LongBigList a = LongArrayBitVector.wrap(t).asLongBigList(registerSize);
			final LongBigList b = LongArrayBitVector.wrap(y).asLongBigList(registerSize);
			for(int i = 0; i < Long.SIZE * l / registerSize; i++) a.set(i, Math.max(a.getLong(i), b.getLong(i)));
			// Note: this must be kept in sync with the line computing the result.
			for(int i = l; i-- != 0;) assert t[i] == (~mask[i] & x[i] | mask[i] & y[i]);
		}

		// Finally, we use mask to select the right bits from x and y and store the result.
		for(int i = l; i-- != 0;) x[i] ^= (x[i] ^ y[i]) & mask[i];

	}
}
