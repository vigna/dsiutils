/*
 * DSI utilities
 *
 * Copyright (C) 2002-2022 Sebastiano Vigna
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.RandomAccess;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ForNameStringParser;
import com.martiansoftware.jsap.stringparsers.IntSizeStringParser;

import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.bytes.ByteBigList;
import it.unimi.dsi.fastutil.bytes.MappedByteBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.MappedLongBigList;
import it.unimi.dsi.fastutil.objects.AbstractObjectBigList;
import it.unimi.dsi.io.FileLinesMutableStringIterable;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.util.Properties;

/**
 * A memory-mapped version of {@link FrontCodedStringBigList}.
 *
 * <P>
 * This class is functionally identical to {@link FrontCodedStringBigList}, but its data is
 * memory-mapped from disk. Only {@linkplain FrontCodedStringBigList#utf8() UTF-8 encoding} is
 * supported.
 *
 * <P>
 * To use this class, one first build a UTF-8 {@link FrontCodedStringBigList}, and then invokes the
 * {@link FrontCodedStringBigList#dump(String)} method to generate a
 * {@linkplain #PROPERTIES_EXTENSION property file} containing metadata, and two files containing
 * {@linkplain #BYTE_ARRAY_EXTENSION strings} and string {@linkplain #POINTERS_EXTENSION pointers},
 * respectively. Then, the {@link #load(String)} method (invoked with the same basename) will return
 * an instance of this class accessing strings and pointers by memory mapping.
 *
 * <P>
 * Note that for consistency with other classes in this package this class implements a
 * {@linkplain BigList big list} of {@linkplain MutableString mutable strings}; however, for greater
 * flexibility it also implements a {@link #getString(long)} method and a {@link #getBytes(long)}
 * method.
 *
 * @see FrontCodedStringBigList
 */

public class MappedFrontCodedStringBigList extends AbstractObjectBigList<MutableString> implements RandomAccess, Closeable {
	public static final long serialVersionUID = 1;
	public static final String PROPERTIES_EXTENSION = ".properties";
	public static final String BYTE_ARRAY_EXTENSION = ".bytearray";
	public static final String POINTERS_EXTENSION = ".pointers";

	public static enum PropertyKeys {
		/** The {@linkplain #size64() number} of strings. */
		N,
		/** The compression {@linkplain FrontCodedStringBigList#ratio() ratio}. */
		RATIO
	}

	/** The number of strings in the list. */
	protected final long n;
	/** The ratio of this front-coded list. */
	protected final int ratio;
	/** The underlying byte array. */
	protected ByteBigList byteList;
	/** The pointers to entire arrays in the list. */
	protected LongBigList pointers;
	/** The file channel used for memory-mapping. */
	private final FileChannel fileChannel;

	protected MappedFrontCodedStringBigList(final long n, final int ratio, final String byteBigList, final String pointers) throws IOException {
		this.n = n;
		this.ratio = ratio;
		this.pointers = MappedLongBigList.map(FileChannel.open(new File(pointers).toPath()));
		fileChannel = FileChannel.open(new File(byteBigList).toPath());
		this.byteList = MappedByteBigList.map(fileChannel);
	}

	/**
	 * Maps in memory a front-coded string big list starting from a basename.
	 *
	 * @param basename the basename of a memory-mapped front-coded string big list.
	 * @return a memory-mapped front-coded string big list.
	 */
	public static MappedFrontCodedStringBigList load(final String basename) throws ConfigurationException, IOException {
		final Properties properties = new Properties(basename + PROPERTIES_EXTENSION);
		return new MappedFrontCodedStringBigList(properties.getLong(PropertyKeys.N), properties.getInt(PropertyKeys.RATIO), basename + BYTE_ARRAY_EXTENSION, basename + POINTERS_EXTENSION);
	}

	/**
	 * Returns the element at the specified position in this front-coded big list as a mutable string.
	 *
	 * @param index an index in the list.
	 * @return a {@link MutableString} that will contain the string at the specified position. The
	 *         string may be freely modified.
	 */
	@Override
	public MutableString get(final long index) {
		return MutableString.wrap(byte2Char(getArray(index), null));
	}

	/**
	 * Returns the element at the specified position in this front-coded big list by storing it in a
	 * mutable string.
	 *
	 * @param index an index in the list.
	 * @param s a mutable string that will contain the string at the specified position.
	 */
	public void get(final long index, final MutableString s) {
		final byte[] a = getArray(index);
		s.length(countUTF8Chars(a));
		byte2Char(a, s.array());
	}

	/**
	 * Returns the element at the specified position in this front-coded big list as a string.
	 *
	 * @param index an index in the list.
	 * @return a {@link String} that will contain the string at the specified position.
	 */
	public String getString(final long index) {
		return new String(getArray(index), StandardCharsets.UTF_8);
	}

	/**
	 * Returns the element at the specified position in this front-coded big list as an UTF-8-coded byte
	 * array.
	 *
	 * @param index an index in the list.
	 * @return a byte array representing in UTF-8 encoding the string at the specified position.
	 */
	public byte[] getBytes(final long index) {
		return getArray(index);
	}

	/**
	 * Computes the number of elements coding a given length.
	 *
	 * @param length the length to be coded.
	 * @return the number of elements coding {@code length}.
	 */
	static int count(final int length) {

		if (length < (1 << 7)) return 1;
		if (length < (1 << 14)) return 2;
		if (length < (1 << 21)) return 3;
		if (length < (1 << 28)) return 4;
		return 5;

	}

	/**
	 * Reads a coded length.
	 *
	 * @param a the data big array.
	 * @param pos the starting position.
	 * @return the length coded at {@code pos}.
	 */
	static int readInt(final ByteBigList a, final long pos) {
		final byte b0 = a.getByte(pos);
		if (b0 >= 0) return b0;
		final byte b1 = a.getByte(pos + 1);
		if (b1 >= 0) return (-b0 - 1) << 7 | b1;
		final byte b2 = a.getByte(pos + 2);
		if (b2 >= 0) return (-b0 - 1) << 14 | (-b1 - 1) << 7 | b2;
		final byte b3 = a.getByte(pos + 3);
		if (b3 >= 0) return (-b0 - 1) << 21 | (-b1 - 1) << 14 | (-b2 - 1) << 7 | b3;
		return (-b0 - 1) << 28 | (-b1 - 1) << 21 | (-b2 - 1) << 14 | (-b3 - 1) << 7 | a.getByte(pos + 4);

	}

	/**
	 * Computes the length of the array at the given index.
	 *
	 * <p>
	 * This private version of {@link #arrayLength(int)} does not check its argument.
	 *
	 * @param array the data array.
	 * @param index an index.
	 * @return the length of the {@code index}-th array.
	 */
	private int length(final long index) {
		final ByteBigList array = this.byteList;
		final int delta = (int)(index % ratio); // The index into the p array, and the delta inside the block.

		long pos = pointers.getLong(index / ratio); // The position into the array of the first entire word before the
													// index-th.
		int length = readInt(array, pos);

		if (delta == 0) return length;

		// First of all, we recover the array length and the maximum amount of copied elements.
		int common;
		pos += count(length) + length;
		length = readInt(array, pos);
		common = readInt(array, pos + count(length));

		for (int i = 0; i < delta - 1; i++) {
			pos += count(length) + count(common) + length;
			length = readInt(array, pos);
			common = readInt(array, pos + count(length));
		}

		return length + common;
	}

	/**
	 * Computes the length of the array at the given index.
	 *
	 * @param index an index.
	 * @return the length of the {@code index}-th array.
	 */
	public int arrayLength(final int index) {
		ensureRestrictedIndex(index);
		return length(index);
	}

	/**
	 * Extracts the array at the given index.
	 *
	 * @param index an index.
	 * @param a the array that will store the result (we assume that it can hold the result).
	 * @param offset an offset into {@code a} where elements will be store.
	 * @param length a maximum number of elements to store in {@code a}.
	 * @return the length of the extracted array.
	 */
	private int extract(final long index, final byte a[], final int offset, final int length) {
		final ByteBigList array = this.byteList;
		final int delta = (int)(index % ratio); // The delta inside the block.
		final long startPos = pointers.getLong(index / ratio); // The position into the array of the first entire word
																// before the
		// index-th.
		long pos, prevArrayPos;
		int arrayLength = readInt(array, pos = startPos), currLen = 0, actualCommon;

		if (delta == 0) {
			pos = pointers.getLong(index / ratio) + count(arrayLength);
			copyFromBig(array, pos, a, offset, Math.min(length, arrayLength));
			return arrayLength;
		}

		int common = 0;

		for (int i = 0; i < delta; i++) {
			prevArrayPos = pos + count(arrayLength) + (i != 0 ? count(common) : 0);
			pos = prevArrayPos + arrayLength;

			arrayLength = readInt(array, pos);
			common = readInt(array, pos + count(arrayLength));

			actualCommon = Math.min(common, length);
			if (actualCommon <= currLen) currLen = actualCommon;
			else {
				copyFromBig(array, prevArrayPos, a, currLen + offset, actualCommon - currLen);
				currLen = actualCommon;
			}
		}

		if (currLen < length) copyFromBig(array, pos + count(arrayLength) + count(common), a, currLen + offset, Math.min(arrayLength, length - currLen));

		return arrayLength + common;
	}

	private static void copyFromBig(final ByteBigList array, final long pos, final byte[] a, final int offset, final int length) {
		// TODO: this should use buffers and multibyte transfers
		for (int i = 0; i < length; i++) a[offset + i] = array.getByte(pos + i);
	}

	/**
	 * Returns an array stored in this front-coded list.
	 *
	 * @param index an index.
	 * @return the corresponding array stored in this front-coded list.
	 */
	public byte[] getArray(final long index) {
		ensureRestrictedIndex(index);
		final int length = length(index);
		final byte a[] = new byte[length];
		extract(index, a, 0, length);
		return a;
	}

	/*
	 * The following methods are highly optimized UTF-8 converters exploiting the fact that since it was
	 * ourselves in the first place who created the coding, we can be sure it is correct.
	 */

	protected static int countUTF8Chars(final byte[] a) {
		final int length = a.length;
		int result = 0, b;
		for (int i = 0; i < length; i++) {
			b = (a[i] & 0xFF) >> 4;
			if (b < 8) result++;
			else if (b < 14) {
				result++;
				i++;
			} else if (b < 15) {
				result++;
				i += 2;
			} else {
				// Surrogate pair (yuck!)
				result += 2;
				i += 4;
			}
		}

		return result;
	}

	protected static char[] byte2Char(final byte[] a, char[] s) {
		final int length = a.length;
		if (s == null) s = new char[countUTF8Chars(a)];
		int b, c, d, t;

		for (int i = 0, j = 0; i < length; i++) {
			b = a[i] & 0xFF;
			t = b >> 4;

			if (t < 8) s[j++] = (char)b;
			else if (t < 14) {
				c = a[++i] & 0xFF;
				if ((c & 0xC0) != 0x80) throw new IllegalStateException("Malformed internal UTF-8 encoding");
				s[j++] = (char)(((b & 0x1F) << 6) | (c & 0x3F));
			} else if (t < 15) {
				c = a[++i] & 0xFF;
				d = a[++i];
				if ((c & 0xC0) != 0x80 || (d & 0xC0) != 0x80) throw new IllegalStateException("Malformed internal UTF-8 encoding");
				s[j++] = (char)(((b & 0x0F) << 12) | ((c & 0x3F) << 6) | ((d & 0x3F) << 0));
			} else {
				// Surrogate pair (yuck!)
				final String surrogatePair = new String(a, i, 4, Charsets.UTF_8);
				s[j++] = surrogatePair.charAt(0);
				s[j++] = surrogatePair.charAt(1);
				i += 3;
			}
		}

		return s;
	}

	@Override
	public long size64() {
		return n;
	}

	@Override
	public void close() throws IOException {
		fileChannel.close();
	}

	public static void main(final String[] arg) throws IOException, JSAPException, NoSuchMethodException, ConfigurationException {

		final SimpleJSAP jsap = new SimpleJSAP(MappedFrontCodedStringBigList.class.getName(), "Dumps the files of a memory-mapped front-coded string big list reading from standard input a newline-separated ordered list of strings.", new Parameter[] {
				new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The file encoding."),
				new FlaggedOption("ratio", IntSizeStringParser.getParser(), "4", JSAP.NOT_REQUIRED, 'r', "ratio", "The compression ratio."),
				new FlaggedOption("decompressor", JSAP.CLASS_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'd', "decompressor", "Use this extension of InputStream to decompress the strings (e.g., java.util.zip.GZIPInputStream)."),
				new UnflaggedOption("basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the files associated with the memory-mapped front-coded string list.") });

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final int ratio = jsapResult.getInt("ratio");
		final Charset encoding = (Charset)jsapResult.getObject("encoding");
		final Class<? extends InputStream> decompressor = jsapResult.getClass("decompressor");
		final String basename = jsapResult.getString("basename");

		final Logger logger = LoggerFactory.getLogger(FrontCodedStringBigList.class);
		logger.info("Reading strings...");
		final FrontCodedStringBigList frontCodedStringBigList = new FrontCodedStringBigList(FileLinesMutableStringIterable.iterator(System.in, encoding, decompressor), ratio, true);
		logger.info("Dumping files...");
		frontCodedStringBigList.dump(basename);
		logger.info("Completed.");
	}
}
