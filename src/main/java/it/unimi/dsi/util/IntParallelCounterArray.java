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

import static it.unimi.dsi.bits.LongArrayBitVector.word;

import java.io.Serializable;

import it.unimi.dsi.Util;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.stat.Ziggurat;

/**
 * An array of approximate sets each represented using a Parallel counter.
 *
 * <p>Parallel counters represent the number of elements of a set in an approximate way. They have been
 * introduced by Philippe Flajolet, &Eacute;ric Fusy, Olivier Gandouet, and Fre&eacute;de&eacute;ric Meunier in
 * &ldquo;Parallel: the analysis of a near-optimal cardinality estimation algorithm&rdquo;,
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
 * (either {@linkplain #IntParallelCounterArray(int, long, int, double) explicitly} or
 * {@linkplain #IntParallelCounterArray(int, long, int, double) choosing the number of registers per counter}).
 * Then, you can {@linkplain #add(int, int) add an element to a counter}. At any time, you can
 * {@linkplain #count(int) count} count (approximately) the number of distinct elements that have been added to a counter.
 *
 * @author Paolo Boldi
 * @author Sebastiano Vigna
 */

public class IntParallelCounterArray implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final boolean ASSERTS = false;
	private static final boolean DEBUG = false;

	public static final int MAX_EXPONENT = 2;

	/** The logarithm of the maximum size in registers of a bit vector. */
	public static final int CHUNK_SHIFT = 30;
	/** The maximum size in registers of a bit vector. */
	public static final long CHUNK_SIZE = 1L << CHUNK_SHIFT;
	public static final long CHUNK_MASK = CHUNK_SIZE - 1;

	/** A an array of bit vectors containing all registers. */
	protected final LongArrayBitVector bitVector[];

	/** {@link #registerSize}-bit views of {@link #bitVector}. */
	protected final LongBigList registers[];

	/** The number of registers. */
	protected final int m;

	/** The number of registers. */
	protected final int log2m;

	/** The number of registers minus one. */
	protected final int mMinus1;

	/** The size in bits of each register. */
	protected final int registerSize;

	/** The mask corresponding to a register. */
	protected final int registerMask;

	/** The shift that selects the chunk corresponding to a node. */
	protected final int nodeShift;

	private final Ziggurat ziggurat;
	protected double base;
	private final double logBase;
	private final int maxExponent;

	/**
	 * Returns the logarithm of the number of registers per counter that are necessary to attain a
	 * given relative standard deviation.
	 *
	 * @param rsd the relative standard deviation to be attained.
	 * @return the logarithm of the number of registers that are necessary to attain relative standard deviation <code>rsd</code>.
	 */
	public static int log2NumberOfRegisters(final double rsd) {
		return (int)Math.ceil(Fast.log2((1 / rsd) * (1 / rsd)));
	}


	/**
	 * Returns the relative standard deviation corresponding to a given logarithm of the number of registers per counter.
	 *
	 * @param log2m the logarithm of the number of registers.
	 * @return the resulting relative standard deviation.
	 */
	public static double relativeStandardDeviation(final int log2m) {
		return 1 / Math.sqrt(1 << log2m);
	}

	/**
	 * Returns the register size in bits, given an upper bound on the number of distinct elements.
	 *
	 * @param n an upper bound on the number of distinct elements.
	 * @return the register size in bits.
	 */

	public static int registerSize(final long n) {
		return Math.max(4, (int)Math.ceil(Math.log(Math.log(n) / Math.log(2)) / Math.log(2)));
	}

	/**
	 * Creates a new array of counters.
	 *
	 * @param arraySize the number of counters.
	 * @param n the expected number of elements.
	 * @param rsd the relative standard deviation.
	 * @param floatingPointPrecision the precision used for floating-point computations.
	 */
	public IntParallelCounterArray(final int arraySize, final long n, final double rsd, final double floatingPointPrecision) {
		this(arraySize, n, log2NumberOfRegisters(rsd), floatingPointPrecision);
	}

	/**
	 * Creates a new array of counters.
	 *
	 * @param arraySize the number of counters.
	 * @param n the expected number of elements.
	 * @param log2m the logarithm of the number of registers per counter.
 	 * @param floatingPointPrecision the precision used for floating-point computations.
	 */
	public IntParallelCounterArray(final int arraySize, final long n, final int log2m, final double floatingPointPrecision) {
		this(arraySize, n, log2m, floatingPointPrecision, Util.randomSeed());
	}

	/**
	 * Creates a new array of counters.
	 *
	 * @param arraySize the number of counters.
	 * @param n the expected number of elements.
	 * @param log2m the logarithm of the number of registers per counter.
	 * @param floatingPointPrecision the precision used for floating-point computations.
	 * @param seed the seed used to compute the hash function
	 */
	public IntParallelCounterArray(final int arraySize, final long n, final int log2m, final double floatingPointPrecision, final long seed) {
		this.m = 1 << (this.log2m = log2m);
		this.mMinus1 = m - 1;
		this.registerSize = (int)(registerSize(n) + Math.ceil(-Fast.log2(floatingPointPrecision)));
		registerMask = (1 << registerSize) - 1;
		nodeShift = CHUNK_SHIFT - log2m;

		base = (1 + floatingPointPrecision) / (1 - floatingPointPrecision);
		logBase = Math.log(base);
		maxExponent = (int)Math.ceil(Math.log(2) / logBase);

		// System.err.println(arraySize + " " + m + " " + registerSize);
		final long sizeInRegisters = (long)arraySize * m;
		final int numVectors = (int)((sizeInRegisters + CHUNK_MASK) >>> CHUNK_SHIFT);

		bitVector = new LongArrayBitVector[numVectors];
		registers = new LongBigList[numVectors];
		for(int i = 0; i < numVectors; i++) {
			this.bitVector[i] = LongArrayBitVector.ofLength(registerSize * Math.min(CHUNK_SIZE, sizeInRegisters - ((long)i << CHUNK_SHIFT)));
			this.registers[i] = bitVector[i].asLongBigList(registerSize);
		}
		ziggurat = new Ziggurat(new XoRoShiRo128PlusRandom(seed));
		if (DEBUG) System.err.println("Register size: " + registerSize + " log2m (b): " + log2m + " m: " + m + " base: " + base);
	}


	private final double[] maxz = new double[10000];
	private final long[] maxe = new long[10000];

	/** Adds an element to a counter.
	 *
	 * @param k the index of the counter.
	 * @param v the element to be added.
	 */
	public void add(final int k, final int v) {
		final int registerSize = this.registerSize;
		// Chunk of the first register
		int chunk = (int)((long)k >>> nodeShift);
		// Offset in bits of the first register inside the chunk
		final long offset = ((long)k << log2m & CHUNK_MASK) * registerSize;
		long[] bits = bitVector[chunk].bits();
		int length = bits.length;

		int word = word(offset) - 1;
		long curr = 0;
		// The number of bits still to be filled in curr.
		// TODO: This won't work unless offset is a multiple of Long.SIZE.
		int used = (int)(offset & ~-Long.SIZE);

		long ee[];

		if (ASSERTS) ee = new long[m];

		for (int i = 0; i < m; i++) {
			final double z = ziggurat.nextDouble();
			maxz[i] = Math.max(maxz[i], 1/z);
			final long e = Math.max(0, - Math.round(Math.log(z) / logBase) + maxExponent) & registerMask;
			maxe[i] = Math.max(maxe[i], e);
			//System.err.print(e + "\t");
			if (ASSERTS) ee[i] = e;
			if (ASSERTS) assert e < (1 << registerSize);
			if (ASSERTS) assert e >= 0;

			if (used < Long.SIZE - registerSize) {
				curr |= e << used;
				used += registerSize;
			}
			else {
				if (++word == length)  {
					word = 0;
					bits = bitVector[++chunk].bits();
					length = bits.length;
				}
				bits[word] = curr | e << used;
				curr = e >>> -used;
				used += registerSize - Long.SIZE;
			}
		}

		//System.err.println();

		if (ASSERTS) {
			for (int j = 0; j < m; j++) {
				assert ee[j] == registers[(int)((((long)k << log2m) + j) >> CHUNK_SHIFT)].getLong((((long)k << log2m) + j) & CHUNK_MASK) : "[" + j + "] " + ee[j] + "!=" + registers[(int)((((long)k << log2m) + j) >> CHUNK_SHIFT)].getLong((((long)k << log2m) + j) & CHUNK_MASK);
			}
		}

	}

	public void printMins() {
		for(int i = 0; i < m; i++) System.out.print(maxe[i] + "\t");
		System.err.println();
		for(int i = 0; i < m; i++) System.out.print(1 / maxz[i] + "\t");
		System.err.println();
		for(int i = 0; i < m; i++) System.out.print(maxz[i] + "\t");
		System.err.println();
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
		/* TODO: final LongBigList[] result = new LongBigList[registers.length];
		for(int i = result.length; i-- != 0;) result[i] = LongBigLists.unmodifiable(registers[i]);
		return result;*/
	}

	/** Estimates the number of distinct elements that have been added to a given counter so far.
	 *
	 * @param k the index of the counter.
	 * @return an approximation of the number of distinct elements that have been added to counter <code>k</code> so far.
	 */
	public double count(final int k) {
		double s = 0;

		final int registerSize = this.registerSize;
		// Chunk of the first register
		int chunk = (int)((long)k >>> nodeShift);
		// Offset in bits of the first register inside the chunk
		final long offset = ((long)k << log2m & CHUNK_MASK) * registerSize;
		long[] bits = bitVector[chunk].bits();
		int length = bits.length;

		int word = word(offset);
		long curr = bits[word] >>> offset;
		long r;
		int remaining = Long.SIZE - (int)(offset & ~-Long.SIZE);
		final int mask = (1 << registerSize) - 1;

		for (int j = 0; j < m; j++) {
			if (remaining >= registerSize) {
				r = curr & mask;
				curr >>>= registerSize;
				remaining -= registerSize;
			}
			else {
				if (++word == length)  {
					word = 0;
					bits = bitVector[++chunk].bits();
					length = bits.length;
				}
				r = (curr | bits[word] << remaining) & mask;
				curr = bits[word] >>> registerSize - remaining;
				remaining += Long.SIZE - registerSize;
			}

			if (ASSERTS) assert r == registers[(int)((((long)k << log2m) + j) >> CHUNK_SHIFT)].getLong((((long)k << log2m) + j) & CHUNK_MASK) : "[" + j + "] " + r + "!=" + registers[(int)((((long)k << log2m) + j) >> CHUNK_SHIFT)].getLong((((long)k << log2m) + j) & CHUNK_MASK);

			s += Math.pow(base, -r + maxExponent);
		}

		return m / s;
	}
}

