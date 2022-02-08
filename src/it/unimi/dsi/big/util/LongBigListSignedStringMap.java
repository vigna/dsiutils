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

package it.unimi.dsi.big.util;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.ObjectBigList;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.io.LineIterator;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.util.ByteBufferLongBigList;

/** A string map based on a function signed using a big list of longs.
 *
 * <p>The purpose of this map is identical to that of a {@link ShiftAddXorSignedStringMap}, but
 * Shift-Add-Xor signatures are 64-bit and stored in a {@link LongBigList}. This approach makes it possible to
 * store the signatures in a file and read them by memory mapping using a {@link ByteBufferLongBigList}.
 * If the map has a very large number of keys but the access pattern is strongly skewed towards a relatively
 * small number of entries, using memory mapping might be advantageous.
 *
 * <p>The intended usage pattern is as follows:
 *
 * <ul>
 *
 * <li>first, you {@linkplain #sign(Iterator, Object2LongFunction) generate a file of signatures} (note that
 * for this phase it might be necessary to keep the signatures in main memory; see {@link #sign(Iterator, String)} for some elaboration);
 *
 * <li>then, when you want to use the signed map you map the file using {@link ByteBufferLongBigList#map(java.nio.channels.FileChannel)}
 * and {@linkplain #LongBigListSignedStringMap(Object2LongFunction, LongBigList) create on the fly a signed map}.
 *
 * </ul>
 *
 * <p>To simplify the process, there is a {@linkplain #LongBigListSignedStringMap(Object2LongFunction, String) constructor} that will do the mapping for you.
 *
 * @author Sebastiano Vigna
 * @since 2.0.13
 * @deprecated There are much better and faster hash functions.
 */
@Deprecated
public class LongBigListSignedStringMap extends AbstractObject2LongFunction<CharSequence> implements StringMap<CharSequence>, Serializable {
	private static final long serialVersionUID = 0L;

	/** The underlying map. */
	protected final Object2LongFunction<? extends CharSequence> function;
	/** Signatures. */
	protected final LongBigList signatures;

	/** Generates a 64-bit signatures big array using a given function and string sequence.
	 *
	 * <p>The resulting big array can be saved using {@link BinIO#storeLongs(long[][], CharSequence)}
	 * or similar {@link BinIO} methods.
	 *
	 * @param iterator an iterator over a list of strings.
	 * @param function the function to be signed.
	 * @return a big array of 64-bit signatures.
	 */
	public static long[][] sign(final Iterator<? extends CharSequence> iterator, final Object2LongFunction<? extends CharSequence> function) {
		return sign(iterator, function, null);
	}

	/** Generates a 64-bit signatures big array using a given function and string sequence.
	 *
	 * <p>The resulting big array can be saved using {@link BinIO#storeLongs(long[][], CharSequence)}
	 * or similar {@link BinIO} methods.
	 *
	 * @param iterator an iterator over a list of strings.
	 * @param function the function to be signed.
	 * @param pl a progress logger, or {@code null}.
	 * @return a big array of 64-bit signatures.
	 */
	public static long[][] sign(final Iterator<? extends CharSequence> iterator, final Object2LongFunction<? extends CharSequence> function , final ProgressLogger pl) {
		final long n = function  instanceof Size64 ? ((Size64)function).size64() : function .size();
		final long[][] signature = LongBigArrays.newBigArray(n);
		if (pl != null) {
			pl.expectedUpdates = n;
			pl.start("Signing...");
		}
		CharSequence s;
		for(long i = 0; i < n; i++) {
			s = iterator.next();
			LongBigArrays.set(signature, function .getLong(s), signature(s));
			if (pl != null) pl.lightUpdate();
		}

		if (iterator.hasNext()) throw new IllegalStateException("Iterator provides more than " + n + " elements");

		if (pl != null) pl.done();
		return signature;
	}

	/** Creates a signed string map using a given hash map and a big list of 64-bit signatures.
	 *
	 * @param function a minimal perfect hash for the strings enumerated by <code>iterator</code>; it must support {@link Function#size() size()}.
	 * and have default return value -1.
	 * @param signatures a big list of 64-bit signatures generated by {@link #sign(Iterator, Object2LongFunction, ProgressLogger)}.
	 */
	public LongBigListSignedStringMap(final Object2LongFunction<? extends CharSequence> function, final LongBigList signatures) {
		final long n = function instanceof Size64 ? ((Size64)function).size64() : function.size();
		if (n != signatures.size64()) throw new IllegalStateException("The size of the function differs from that of the signature list: " + n + " != " + signatures.size64());
		this.function = function;
		this.signatures = signatures;
		defaultReturnValue(-1);
	}

	/** Generates an on-disk list 64-bit signatures big array using a given string sequence.
	 *
	 * <p>This methods generates on-disk signatures <em>in the same order of the strings returned by
	 * the provided iterator</em>. Thus, the signature file can be only used with a function that
	 * maps each string returned by the iterator in its ordinal position. This happens, for instance,
	 * if you have a sorted set of string and you use a <a href="http://sux4j.di.unimi.it/docs/it/unimi/dsi/sux4j/mph/package-summary.html">monotone minimal perfect hash function</a>.
	 *
	 * @param iterator an iterator over a list of strings.
	 * @param signatureFile the file name of the resulting signature file.
	 */
	public static void sign(final Iterator<? extends CharSequence> iterator, final String signatureFile) throws IOException {
		sign(iterator, signatureFile, null);
	}

	/** Generates an on-disk list 64-bit signatures big array using a given string sequence.
	 *
	 * @param iterator an iterator over a list of strings.
	 * @param signatureFile the file name of the resulting signature file.
	 * @param pl a progress logger, or {@code null}.
	 * @see #sign(Iterator, DataOutput, ProgressLogger)
	 */
	public static void sign(final Iterator<? extends CharSequence> iterator, final String signatureFile, final ProgressLogger pl) throws IOException {
		final DataOutputStream dos = new DataOutputStream(new FastBufferedOutputStream(new FileOutputStream(signatureFile)));
		sign(iterator, dos, pl);
		dos.close();
	}

	/** Generates an on-disk list 64-bit signatures big array using a given string sequence.
	 *
	 * <p>This methods generates on-disk signatures <em>in the same order of the strings returned by
	 * the provided iterator</em>. Thus, the generated signatures can be only used with a function that
	 * maps each string returned by the iterator in its ordinal position. This happens, for instance,
	 * if you have a sorted set of string and you use a
	 * <a href="http://sux4j.di.unimi.it/docs/it/unimi/dsi/sux4j/mph/package-summary.html">monotone minimal perfect hash function</a>.
	 *
	 * @param iterator an iterator over a list of strings.
	 * @param signatures a {@link DataOutput} where the signatures will be written.
	 * @param pl a progress logger, or {@code null}.
	 */
	public static void sign(final Iterator<? extends CharSequence> iterator, final DataOutput signatures, final ProgressLogger pl) throws IOException {
		if (pl != null) pl.start("Signing...");
		CharSequence s;
		while(iterator.hasNext()) {
			s = iterator.next();
			signatures.writeLong(signature(s));
			if (pl != null) pl.lightUpdate();
		}

		if (pl != null) pl.done();
	}


	/** Creates a signed string map using a given hash map and a big list of 64-bit signatures.
	 *
	 * @param function a minimal perfect hash for the strings enumerated by <code>iterator</code>; it must support {@link Function#size() size()}.
	 * and have default return value -1.
	 * @param signatures a file containing a list of 64-bit signatures
	 * generated by {@link #sign(Iterator, Object2LongFunction, ProgressLogger)} and stored in {@link DataOutput} format.
	 */
	@SuppressWarnings("resource")
	public LongBigListSignedStringMap(final Object2LongFunction<? extends CharSequence> function, final String signatures) throws FileNotFoundException, IOException {
		final long n = function instanceof Size64 ? ((Size64)function).size64() : function.size();
		final long signatureSize = new File(signatures).length() / Long.BYTES;
		if (n != signatureSize) throw new IllegalStateException("The size of the function differs from that of the signature list: " + n + " != " + signatureSize);
		this.function = function;
		this.signatures = ByteBufferLongBigList.map(new FileInputStream(signatures).getChannel());
		defaultReturnValue(-1);
	}

	private static long signature(final CharSequence s) {
		int i;
		final int l = s.length();
		long h = 42;

		for (i = l; i-- != 0;) h ^= (h << 5) + s.charAt(i) + (h >>> 2);
		return h;
	}

	private boolean checkSignature(final CharSequence s, final long index) {
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
	@Deprecated
	public int size() {
		return signatures.size();
	}

	@Override
	public long size64() {
		return signatures.size64();
	}

	@Override
	public ObjectBigList<CharSequence> list() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void main(final String[] arg) throws NoSuchMethodException, IOException, JSAPException, ClassNotFoundException {

		final SimpleJSAP jsap = new SimpleJSAP(LongBigListSignedStringMap.class.getName(), "Generates a 64-bit signature file by reading a newline-separated list of strings and a function built on the same list of strings.",
				new Parameter[] {
						new FlaggedOption("bufferSize", JSAP.INTSIZE_PARSER, "64Ki", JSAP.NOT_REQUIRED, 'b',  "buffer-size", "The size of the I/O buffer used to read strings."),
						new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The string file encoding."),
						new Switch("zipped", 'z', "zipped", "The string list is compressed in gzip format."),
						new UnflaggedOption("function", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the function to be signed, or - for writing signatures in the same order of the strings."),
						new UnflaggedOption("signatures", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the resulting signatures."),
						new UnflaggedOption("stringFile", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY, "Read strings from this file instead of standard input."),
		});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final int bufferSize = jsapResult.getInt("bufferSize");
		final String functionName = jsapResult.getString("function");
		final String signaturesName = jsapResult.getString("signatures");
		final String stringFile = jsapResult.getString("stringFile");
		final Charset encoding = (Charset)jsapResult.getObject("encoding");
		final boolean zipped = jsapResult.getBoolean("zipped");

		final InputStream inputStream = stringFile != null ? new FileInputStream(stringFile) : System.in;
		final Iterator<MutableString> iterator = new LineIterator(new FastBufferedReader(new InputStreamReader(zipped ? new GZIPInputStream(inputStream) : inputStream, encoding), bufferSize));
		final Object2LongFunction<CharSequence> function = "-".equals(functionName) ? null : (Object2LongFunction<CharSequence>)BinIO.loadObject(functionName);
		final Logger logger = LoggerFactory.getLogger(LongBigListSignedStringMap.class);
		final ProgressLogger pl = new ProgressLogger(logger);
		if (function != null) BinIO.storeLongs(sign(iterator, function, pl), signaturesName);
		else sign(iterator, signaturesName, pl);
		if (stringFile != null) inputStream.close();
	}
}
