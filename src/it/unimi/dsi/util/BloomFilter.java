/*
 * DSI utilities
 *
 * Copyright (C) 2004-2022 Sebastiano Vigna
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ForNameStringParser;
import com.martiansoftware.jsap.stringparsers.IntSizeStringParser;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;

/** A Bloom filter.
 *
 * <P>Instances of this class represent a set of elements (with false positives) using a Bloom
 * filter (Burton H. Bloom, &ldquo;Space/time trade-offs in hash coding with allowable
 * errors&rdquo;, <i>Comm. ACM</i> 13(7):422&minus;426, 1970). Because of the way Bloom filters
 * work, you cannot remove elements.
 *
 * <P>Given a maximum number of elements,
 * Bloom filters have an expected error rate that depends on the number of hash functions used.
 * More precisely, a
 * Bloom filter for at most <var>n</var> elements with <var>d</var> hash functions will use ln 2
 * <var>d</var><var>n</var> &#8776; 1.44 <var>d</var><var>n</var> bits; false positives will happen
 * with probability 2<sup>-<var>d</var></sup>. Adding more than <var>n</var> elements will result
 * in a higher rate of false positives. You can conveniently build a filter by
 * {@linkplain #create(long, int, Funnel) specifying the number of hash function},
 * by {@linkplain #create(long, double, Funnel) specifying the expected false positive rate},
 * or {@linkplain #create(long, Funnel) just requesting essentially no false positives}.
 *
 * <p>The maximum number of bits supported is
 * {@value #MAX_BITS}, which makes it possible to store with high precision several dozens billion elements.
 *
 * <P>This class exports access methods that are similar to those of {@link java.util.Set}, but it
 * does not implement that interface, as too many non-optional methods would be unimplementable
 * (e.g., iterators). To store generic objects of type <code>T</code>, we rely on {@linkplain Hashing#murmur3_128(int) MurmurHash3}
 * and Google Guava's <em>{@linkplain Funnel funnels}</em>, which are strategies turning an object into a sequence of bytes.
 * There are predefined methods for storing {@linkplain #add(CharSequence) character sequences},
 * {@linkplain #add(byte[]) byte} and {@linkplain #add(char[]) character arrays},
 * {@linkplain #add(int) integers} and {@linkplain #add(long) longs}; they use the ready-made funnels available in {@link Funnels}.
 * You can define your own {@link Funnel} and store objects correspondingly.
 *
 * <p>If you intend to storage sequences of bytes which are already random looking (e.g., MD5 digests) you can
 * use the {@link #addHash(byte[])}/{@link #containsHash(byte[])} methods, which use the first 16 bytes
 * of the argument byte array directly, without further hashing.
 *
 * <p>If you plan to use predefined methods only, the suggested way to instantiate this class is to use the
 * factory methods without {@link Funnel} arguments (e.g., {@link #create(long, int)}). They
 * return a filter of generic type {@link Void} using a {@code null} funnel
 * that will be never invoked (unless you circument the generics type-safety mechanisms).
 *
 * <P>A main method makes it easy to create serialized Bloom filters starting from a list of strings.
 *
 * <h2>Implementation details</h2>
 *
 * <P>To generate several hash functions we use the technique suggested by Adam Kirsch and Michael Mitzenmacher in &ldquo;Less hashing,
 * same performance: Building a better Bloom filter&rdquo;, <i>Random Structures &amp;
 * Algorithms</i>, 33(2):187&minus;218, John Wiley &amp; Sons, 2008. Two 64-bit hashes <var>h</var><sub>0</sub> and
 * <var>h</var><sub>1</sub> are generated using {@link Hashing#murmur3_128()} (or taken from the argument,
 * in case of {@link #addHash(byte[])}/{@link #containsHash(byte[])}). Then, the hash
 * function of index <var>i</var> is simply <var>h</var><sub>0</sub> + <var>ih</var><sub>1</sub> (<var>i</var> &ge; 0). The
 * paper proves that this choice does not worsen the rate of false positives.
 *
 * @author Sebastiano Vigna
 */

public class BloomFilter<T> implements Serializable, Size64 {
	private static final long serialVersionUID = 4L;
    private static final long LOG2_LONG_SIZE = 6;
    /** {@link Funnels#byteArrayFunnel()}. */
	public static final Funnel<byte[]> BYTE_ARRAY_FUNNEL = Funnels.byteArrayFunnel();
    /** {@link Funnels#unencodedCharsFunnel()}. */
	public static final Funnel<CharSequence> STRING_FUNNEL = Funnels.unencodedCharsFunnel();
    /** {@link Funnels#integerFunnel()}. */
	public static final Funnel<Integer> INTEGER_FUNNEL = Funnels.integerFunnel();
	/** {@link Funnels#longFunnel()}. */
	public static final Funnel<Long> LONG_FUNNEL = Funnels.longFunnel();

	/** The maximum number of bits in a filter (limited by array size and bits in a long). */
	public static final long MAX_BITS = (long)Long.SIZE * Integer.MAX_VALUE;

	/** The number of bits in this filter. */
	private final long m;
	/** The number of hash functions used by this filter. */
	private final int d;
	/** The underlying bit vector. */
	private final long[] bits;
	/** The hash function. */
	private final HashFunction hashFunction;
	/** The number of elements currently in this filter. It may be
     * smaller than the actual number of additions because of false positives. */
    private long size;
    /** The funnel used to store object of generic type. */
	private final Funnel<T> funnel;

	/** Creates a new Bloom filter with given number of hash functions and expected number of elements of given type.
	 *
	 * @param n the expected number of elements.
	 * @param d the number of hash functions; if no more than <code>n</code> elements are added to this filter,
	 * false positives will happen with probability 2<sup>-<var>d</var></sup>.
	 * @param funnel a funnel for the elements of this filter.
	 */
	protected BloomFilter(final long n, final int d, final Funnel<T> funnel) {
		this.d = d;
		this.funnel = funnel;
		final long wantedNumberOfBits = (long)Math.ceil(n * (d / Math.log(2)));
		if (wantedNumberOfBits > MAX_BITS) throw new IllegalArgumentException("The wanted number of bits (" + wantedNumberOfBits + ") is larger than " + MAX_BITS);
		bits = new long[LongArrayBitVector.words(wantedNumberOfBits)];
		m = Math.max(1, bits.length) * (long)Long.SIZE; // To avoid divisions by zero
		hashFunction = Hashing.murmur3_128();
	}

	/** Creates a new high-precision Bloom filter a given expected number of elements of given type.
	 *
	 * <p>This constructor uses a number of hash functions that is logarithmic in the number
	 * of expected elements. This usually results in no false positives at all.
	 *
	 * @param n the expected number of elements.
	 * @param funnel a funnel for the elements of this filter (use {@link #create(long)} if you
	 * plan on using only the predefined methods).
	 */
	public static <T> BloomFilter<T> create(final long n, final Funnel<T> funnel) {
		return new BloomFilter<>(n, Fast.ceilLog2(n), funnel);
	}

	/** Creates a new Bloom filter with given number of hash functions and expected number of elements of given type.
	 *
	 * @param n the expected number of elements.
	 * @param d the number of hash functions; if no more than <code>n</code> elements are added to this filter,
	 * false positives will happen with probability 2<sup>-<var>d</var></sup>.
	 * @param funnel a funnel for the elements of this filter (use {@link #create(long, int)} if you
	 * plan on using only the predefined methods).
	 */
	public static <T> BloomFilter<T> create(final long n, final int d, final Funnel<T> funnel) {
		return new BloomFilter<>(n, d, funnel);
	}

	/** Creates a new Bloom filter on {@link Void} with given precision and expected number of elements of given type.
	 *
	 * @param n the expected number of elements.
	 * @param precision the expected fraction of false positives; if no more than <code>n</code> elements are added to this filter,
	 * false positives will happen with no more than this probability.
	 * plan on using only the predefined methods).
	 * @param funnel a funnel for the elements of this filter (use {@link #create(long, double)} if you
	 * plan on using only the predefined methods).
	 */
	public static <T> BloomFilter<T> create(final long n, final double precision, final Funnel<T> funnel) {
		return new BloomFilter<>(n, Math.max(0, (int)Math.ceil(- Fast.log2(precision))), funnel);
	}

	/** Creates a new high-precision Bloom filter a given expected number of elements.
	 *
	 * <p>Filters created using this method will be accessible using predefined methods only.
	 * Use {@link #create(long, Funnel)} if you need a generic filter.
	 *
	 * <p>This constructor uses a number of hash functions that is logarithmic in the number
	 * of expected elements. This usually results in no false positives at all.
	 *
	 * @param n the expected number of elements.
	 */
	public static BloomFilter<Void> create(final long n) {
		return create(n, null);
	}

	/** Creates a new Bloom filter with given number of hash functions and expected number of elements.
	 *
	 * <p>Filters created using this method will be accessible using predefined methods only.
	 * Use {@link #create(long, int, Funnel)} if you need a generic filter.
	 *
	 * @param n the expected number of elements.
	 * @param d the number of hash functions; if no more than <code>n</code> elements are added to this filter,
	 * false positives will happen with probability 2<sup>-<var>d</var></sup>.
	 */
	public static BloomFilter<Void> create(final long n, final int d) {
		return create(n, d, null);
	}

	/** Creates a new Bloom filter on {@link Void} with given precision and expected number of elements.
	 *
	 * <p>Filters created using this method will be accessible using predefined methods only.
	 * Use {@link #create(long, double, Funnel)} if you need a generic filter.
	 *
	 * @param n the expected number of elements.
	 * @param precision the expected fraction of false positives; if no more than <code>n</code> elements are added to this filter,
	 * false positives will happen with no more than this probability.
	 * plan on using only the predefined methods).
	 * @see #BloomFilter(long, int, Funnel)
	 */
	public static BloomFilter<Void> create(final long n, final double precision) {
		return create(n, precision, null);
	}

	/** Returns the value of the bit with the specified index in the specified array.
     *
     * <p>This method (and its companion {@link #set(long[], long)}) are static
     * so that the bit array can be cached by the caller in a local variable.
     *
     * @param index the bit index.
     * @return the value of the bit of index <code>index</code>.
     */
    private static boolean get(final long[] bits, final long hash, final long m) {
    	final long index = (hash & -1L >>> 1) % m;
		return (bits[(int)(index >> LOG2_LONG_SIZE)] & 1L << index) != 0;
	}

    /** Sets the bit with specified index in the specified array.
     *
     * @param index the bit index.
     * @see #get(long[], long, long)
     */
    private static boolean set(final long[] bits, final long hash, final long m) {
    	final long index = (hash & -1L >>> 1) % m;
    	final int unit = (int)(index >> LOG2_LONG_SIZE);
    	final long mask = 1L << index;
    	final boolean result = (bits[unit] & mask) != 0;
    	bits[unit] |= mask;
    	return result;
	}

	/** Adds a character sequence to this filter.
	 *
	 * @param s a character sequence.
	 * @return true if this filter was modified.
	 */

	public boolean add(final CharSequence s) {
		return add(s, STRING_FUNNEL);
	}

	/** Adds a byte array to this filter.
	 *
	 * @param a a byte array.
	 * @return true if this filter was modified.
	 */

	public boolean add(final byte[] a) {
		return add(a, BYTE_ARRAY_FUNNEL);
	}

	/** Adds a character array to this filter.
	 *
	 * @param a a character array.
	 * @return true if this filter was modified.
	 */
	public boolean add(final char[] a) {
		return add(new String(a), STRING_FUNNEL);
	}

	/** Adds an integer to this filter.
	 *
	 * @param x an integer.
	 * @return true if this filter was modified.
	 */
	public boolean add(final int x) {
		return add(Integer.valueOf(x), INTEGER_FUNNEL);
	}

	/** Adds a long to this filter.
	 *
	 * @param x a long.
	 * @return true if this filter was modified.
	 */
	public boolean add(final long x) {
		return add(Long.valueOf(x), LONG_FUNNEL);
	}

	/** Adds an object of generic type to this filter using the funnel specified at construction time.
	 *
	 * @param e an object.
	 * @return true if this filter was modified.
	 */
	public boolean add(final T e) {
		return add(e, funnel);
	}

	/** Adds an object to this filter using a specified funnel.
	 *
	 * @param e an object.
	 * @param funnel a funnel for {@code object}.
	 * @return true if this filter was modified.
	 */
	public <V> boolean add(final V e, final Funnel<V> funnel) {
		return addHash(hashFunction.newHasher().putObject(e, funnel).hash().asBytes());
	}

	/** Adds a hash code to this filter.
	 *
	 * <p>This method uses the first 16 bytes of a byte array to build two 64-bit hashes. The intended usage
	 * is storing digests and similar already-hashed values.
	 *
	 * @param hash a byte array of at least 16 elements containing a hash code.
	 * @return true if this filter was modified.
	 * @throws ArrayIndexOutOfBoundsException if {@code hash} is shorter than 16.
	 * @see #containsHash(byte[])
	 */
	public boolean addHash(final byte[] hash) {
		final long hash0 = Longs.fromBytes(hash[0], hash[1], hash[2], hash[3], hash[4], hash[5], hash[6], hash[7]);
		final long hash1 = Longs.fromBytes(hash[8], hash[9], hash[10], hash[11], hash[12], hash[13], hash[14], hash[15]);
		final long bits[] = this.bits;
		boolean alreadySet = true;
		final long m = this.m;
		for(int i = d; i-- != 0;) alreadySet &= set(bits, hash0 + i * hash1, m);
		if (! alreadySet) size++;
		return !alreadySet;
	}

	/** Checks whether the given character sequence is in this filter.
	 *
	 * <P>Note that this method may return true on a character sequence that has
	 * never been added to this filter. This will happen with probability 2<sup>-<var>d</var></sup>,
	 * where <var>d</var> is the number of hash functions specified at creation time, if
	 * the number of the elements in this filter is less than <var>n</var>, the number
	 * of expected elements specified at creation time.
	 *
	 * @param s a character sequence.
	 * @return true if <code>s</code> is in this filter.
	 */
	public boolean contains(final CharSequence s) {
		return contains(s, STRING_FUNNEL);
	}

	/** Checks whether the given byte array is in this filter.
	 *
	 * @param a a byte array.
	 * @return true if <code>a</code> is in this filter.
	 * @see #contains(CharSequence)
	 */

	public boolean contains(final byte[] a) {
		return contains(a, BYTE_ARRAY_FUNNEL);
	}

	/** Checks whether the given character array is in this filter.
	 *
	 * @param a a character array.
	 * @return true if <code>a</code> is in this filter.
	 * @see #contains(CharSequence)
	 */

	public boolean contains(final char[] a) {
		return contains(new String(a), STRING_FUNNEL);
	}

	/** Adds an integer is in this filter.
	 *
	 * @param x an integer.
	 * @return true if <code>x</code> is in this filter.
	 * @see #contains(CharSequence)
	 */
	public boolean contains(final int x) {
		return contains(Integer.valueOf(x), INTEGER_FUNNEL);
	}

	/** Checks whether the given long is in this filter.
	 *
	 * @param x a long.
	 * @return true if <code>x</code> is in this filter.
	 * @see #contains(CharSequence)
	 */
	public boolean contains(final long x) {
		return contains(Long.valueOf(x), LONG_FUNNEL);
	}

	/** Checks whether an object of generic type is in this filter using the funnel specified at construction time.
	 *
	 * @param e an element.
	 * @return true if <code>e</code> is in this filter.
	 * @see #contains(CharSequence)
	 */
	public boolean contains(final T e) {
		return contains(e, funnel);
	}

	/** Checks whether an object is in this filter using a specified funnel.
	 *
	 * @param e an object.
	 * @param funnel a funnel for {@code e}.
	 * @return true if {@code e} is in this filter.
	 * @see #contains(CharSequence)
	 */
	private <V> boolean contains(final V e, final Funnel<V> funnel) {
		return containsHash(hashFunction.newHasher().putObject(e, funnel).hash().asBytes());
	}

	/** Checks whether a hash code is in this filter.
	 *
	 * <p>This method uses the first 16 bytes of a byte array to build two 64-bit hashes. The intended usage
	 * is storing digests and similar already-hashed values.
	 *
	 * @param hash a byte array of at least 16 elements containing a hash code.
	 * @return true if {@code hash} is in this filter.
	 * @throws ArrayIndexOutOfBoundsException if {@code hash} is shorter than 16.
	 * @see #addHash(byte[])
	 */
	public boolean containsHash(final byte[] hash) {
		final long hash0 = Longs.fromBytes(hash[0], hash[1], hash[2], hash[3], hash[4], hash[5], hash[6], hash[7]);
		final long hash1 = Longs.fromBytes(hash[8], hash[9], hash[10], hash[11], hash[12], hash[13], hash[14], hash[15]);
		final long bits[] = this.bits;
		final long m = this.m;
		for(int i = d; i-- != 0;) if (! get(bits, hash0 + i * hash1, m)) return false;
		return true;
	}

	/** Clears this filter. */
	public void clear() {
		Arrays.fill(bits, 0);
		size = 0;
	}

	/** Returns the size of this filter.
	 *
	 * <p>Note that the size of a Bloom filter is only a <em>lower bound</em>
	 * for the number of distinct elements that have been added to this filter.
	 * False positives might make the number returned by this method smaller
	 * than it should be.
	 *
	 * @return the size of this filter. */
	@Override
	public long size64() {
		return size;
	}

	@Override
	@Deprecated
	public int size() {
		return (int)Math.min(Integer.MAX_VALUE, size);
	}

	public static void main(final String[] arg) throws IOException, JSAPException, NoSuchMethodException {

		final SimpleJSAP jsap = new SimpleJSAP(BloomFilter.class.getName(), "Creates a Bloom filter reading from standard input a newline-separated list of terms.",
				new Parameter[] {
					new FlaggedOption("bufferSize", IntSizeStringParser.getParser(), "64Ki", JSAP.NOT_REQUIRED, 'b',  "buffer-size", "The size of the I/O buffer used to read terms."),
					new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The term file encoding."),
					new UnflaggedOption("bloomFilter", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename for the serialised front-coded list."),
					new UnflaggedOption("size", JSAP.INTSIZE_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The size of the filter (i.e., the expected number of elements in the filter; usually, the number of terms)."),
					new UnflaggedOption("precision", JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The precision of the filter.")
		});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final int bufferSize = jsapResult.getInt("bufferSize");
		final String filterName = jsapResult.getString("bloomFilter");
		final Charset encoding = (Charset)jsapResult.getObject("encoding");

		final BloomFilter<Void> filter = BloomFilter.create(jsapResult.getInt("size"), jsapResult.getInt("precision"));
		final ProgressLogger pl = new ProgressLogger();
		pl.itemsName = "terms";
		pl.start("Reading terms...");
		final MutableString s = new MutableString();
		final FastBufferedReader reader = new FastBufferedReader(new InputStreamReader(System.in, encoding), bufferSize);
		while(reader.readLine(s) != null) {
			filter.add(s);
			pl.lightUpdate();
		}
		pl.done();
		reader.close();

		BinIO.storeObject(filter, filterName);
	}
}
