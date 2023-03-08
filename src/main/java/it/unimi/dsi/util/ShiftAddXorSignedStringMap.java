/*
 * DSI utilities
 *
 * Copyright (C) 2008-2023 Sebastiano Vigna
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ForNameStringParser;

import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.io.LineIterator;
import it.unimi.dsi.lang.MutableString;

/** A string map based on a function signed using Shift-Add-Xor hashes.
 *
 * <p>A minimal perfect hash function maps a set of string to an initial segment of the natural
 * numbers, but will actually map <em>any</em> string to that segment. By signing
 * each output value with a hash of the string, we get a dictionary-like functionality with a rate
 * error that can be balanced with space occupancy (signatures can go from 1 to {@link Long#SIZE} bits).
 *
 * <p>For the kind of hash we use, see &ldquo;Performance in practice of string hashing functions&rdquo;, by
 * M.V. Ramakrishna and Justin Zobel, <i>Proc. of the Fifth International Conference on
 * Database Systems for Advanced Applications</i>, 1997, pages 215&minus;223.
 *
 * @author Sebastiano Vigna
 * @since 1.1.2
 * @deprecated There are much better and faster hash functions.
 */

@Deprecated
public class ShiftAddXorSignedStringMap extends AbstractObject2LongFunction<CharSequence> implements StringMap<CharSequence>, Serializable {
	private static final long serialVersionUID = 0L;

	/** The underlying map. */
	protected final Object2LongFunction<? extends CharSequence> function;
	/** Signatures. */
	protected final LongBigList signatures;
	/** The width in bits of each signature. */
	protected final int width;
	/** The left shift to get only {@link #width} nonzero bits. */
	protected final int shift;
	/** The mask to get only {@link #width} nonzero bits. */
	protected final long mask;

	/** Creates a new shift-add-xor signed string map using a given hash map and 32-bit signatures.
	 *
	 * @param iterator an iterator enumerating a set of strings.
	 * @param map a minimal perfect hash for the strings enumerated by <code>iterator</code>; it must support {@link Function#size() size()}
	 * and have default return value -1.
	 */

	public ShiftAddXorSignedStringMap(final Iterator<? extends CharSequence> iterator, final Object2LongFunction<? extends CharSequence> map) {
		this(iterator, map, 32);
	}

	/** Creates a new shift-add-xor signed string map using a given hash map.
	 *
	 * @param iterator an iterator enumerating a set of strings.
	 * @param map a minimal perfect hash for the strings enumerated by <code>iterator</code>; it must support {@link Function#size() size()}
	 * and have default return value -1.
	 * @param signatureWidth the width, in bits, of the signature of each string.
	 */

	public ShiftAddXorSignedStringMap(final Iterator<? extends CharSequence> iterator, final Object2LongFunction<? extends CharSequence> map, final int signatureWidth) {
		CharSequence s;
		this.function = map;
		this.width = signatureWidth;
		this.defRetValue = -1;
		shift = Long.SIZE - width;
		mask = width == Long.SIZE ? 0 : (1L << width) - 1;
		final int n = map.size();
		signatures = LongArrayBitVector.getInstance().asLongBigList(signatureWidth);
		signatures.size(n);

		for(int i = 0; i < n; i++) {
			s = iterator.next();
			signatures.set(map.getLong(s), signature(s));
		}

		if (iterator.hasNext()) throw new IllegalStateException("Iterator provides more than " + n + " elements");
	}

	private long signature(final CharSequence s) {
		int i;
		final int l = s.length();
		long h = 42;

		for (i = l; i-- != 0;) h ^= (h << 5) + s.charAt(i) + (h >>> 2);
		return (h >>> shift) ^ (h & mask);
	}

	private boolean checkSignature(final CharSequence s, final long index) {
		//System.err.println(s + ": " + signatures.getLong(index) + " ?= " + signature(s));
		return index >= 0 && index < function.size() && signatures.getLong(index) == signature(s);
	}

	@Override
	public long getLong(final Object o) {
		final CharSequence s = (CharSequence)o;
		final long index = function.getLong(s);
		return checkSignature(s, index) ? index : defRetValue;
	}

	@Override
	public Long get(final Object o) {
		final CharSequence s = (CharSequence)o;
		final long index = function.getLong(s);
		return checkSignature(s, index) ? Long.valueOf(index) : null;
	}

	@Override
	public boolean containsKey(final Object o) {
		final CharSequence s = (CharSequence)o;
		return checkSignature(s, function.getLong(s));
	}

	@Override
	public int size() {
		return signatures.size();
	}

	@Override
	public ObjectList<CharSequence> list() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void main(final String[] arg) throws NoSuchMethodException, IOException, JSAPException, ClassNotFoundException {

		final SimpleJSAP jsap = new SimpleJSAP(ShiftAddXorSignedStringMap.class.getName(), "Builds a shift-add-xor signed string map by reading a newline-separated list of strings and a function built on the same list of strings.",
				new Parameter[] {
			new FlaggedOption("bufferSize", JSAP.INTSIZE_PARSER, "64Ki", JSAP.NOT_REQUIRED, 'b',  "buffer-size", "The size of the I/O buffer used to read strings."),
			new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The string file encoding."),
			new Switch("zipped", 'z', "zipped", "The string list is compressed in gzip format."),
			new FlaggedOption("width", JSAP.INTEGER_PARSER, Integer.toString(Integer.SIZE), JSAP.NOT_REQUIRED, 'w', "width", "The signature width in bits."),
			new UnflaggedOption("function", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the function to be signed."),
			new UnflaggedOption("map", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the resulting serialised signed string map."),
			new UnflaggedOption("stringFile", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY, "Read strings from this file instead of standard input."),
		});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final int bufferSize = jsapResult.getInt("bufferSize");
		final String functionName = jsapResult.getString("function");
		final String mapName = jsapResult.getString("map");
		final String stringFile = jsapResult.getString("stringFile");
		final Charset encoding = (Charset)jsapResult.getObject("encoding");
		final int width = jsapResult.getInt("width");
		final boolean zipped = jsapResult.getBoolean("zipped");

		final InputStream inputStream = stringFile != null ? new FileInputStream(stringFile) : System.in;
		final Iterator<MutableString> iterator = new LineIterator(new FastBufferedReader(new InputStreamReader(zipped ? new GZIPInputStream(inputStream) : inputStream, encoding), bufferSize));
		final Object2LongFunction<CharSequence> function = (Object2LongFunction<CharSequence>)BinIO.loadObject(functionName);
		final Logger logger = LoggerFactory.getLogger(ShiftAddXorSignedStringMap.class);
		logger.info("Signing...");
		BinIO.storeObject(new ShiftAddXorSignedStringMap(iterator, function, width), mapName);
		if (stringFile != null) inputStream.close();
		logger.info("Completed.");
	}
}
