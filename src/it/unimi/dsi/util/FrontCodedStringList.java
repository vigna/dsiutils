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

package it.unimi.dsi.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ForNameStringParser;
import com.martiansoftware.jsap.stringparsers.IntSizeStringParser;

import it.unimi.dsi.fastutil.bytes.ByteArrayFrontCodedList;
import it.unimi.dsi.fastutil.chars.CharArrayFrontCodedList;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.io.LineIterator;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;

/**
 * Compact storage of strings using front-coding compression (also known as compression by prefix
 * omission).
 *
 * <P>
 * This class stores a list of strings using front-coding (also known as prefix-omission)
 * compression; the compression will be reasonable only if the list is sorted, but you could also
 * use instances of this class just as a handy way to manage a large amount of strings. It
 * implements an immutable {@link it.unimi.dsi.fastutil.objects.ObjectList} that returns the
 * <var>i</var>-th string (as a {@link MutableString}) when the {@link #get(int)} method is called
 * with argument <var>i</var>. The returned mutable string may be freely modified.
 *
 * <P>
 * As a commodity, this class provides a main method that reads from standard input a sequence of
 * newline-separated strings, and writes a corresponding serialized front-coded string list.
 *
 * <H2>Implementation Details</H2>
 *
 * <P>
 * To store the list of strings, we use either a UTF-8 coded {@link ByteArrayFrontCodedList}, or a
 * {@link CharArrayFrontCodedList}, depending on the value of the
 * {@link #FrontCodedStringList(Iterator,int,boolean) utf8} parameter at creation time. In the first
 * case, if the strings are ASCII-oriented the resulting array will be much smaller, but access
 * times will increase manifold, as each string must be UTF-8 decoded before being returned.
 */

public class FrontCodedStringList extends AbstractObjectList<MutableString> implements RandomAccess, Serializable {

	public static final long serialVersionUID = 1;

	/** The underlying {@link ByteArrayFrontCodedList}, or {@code null}.*/
	protected final ByteArrayFrontCodedList byteFrontCodedList;

	/** The underlying {@link CharArrayFrontCodedList}, or {@code null}.*/
	protected final CharArrayFrontCodedList charFrontCodedList;

	/** Whether this front-coded list is UTF-8 encoded. */
	protected final boolean utf8;


	/** Creates a new front-coded string list containing the character sequences returned by the given iterator.
	 *
	 * @param words an iterator returning {@linkplain CharSequence character sequences}.
	 * @param ratio the desired ratio.
	 * @param utf8 if true, the strings will be stored as UTF-8 byte arrays.
	 */

	public FrontCodedStringList(final Iterator<? extends CharSequence> words, final int ratio, final boolean utf8) {

		this.utf8 = utf8;
		if (utf8) {
			byteFrontCodedList = new ByteArrayFrontCodedList(
					new ObjectIterator<byte[]>() {
						@Override
						public boolean hasNext() { return words.hasNext(); }
						@SuppressWarnings("null")
						@Override
						public byte[] next() {
							return words.next().toString().getBytes(Charsets.UTF_8);
						}
					},
					ratio);
			charFrontCodedList = null;
		}
		else {
			charFrontCodedList = new CharArrayFrontCodedList(
					new ObjectIterator<char[]>() {
						@Override
						public boolean hasNext() { return words.hasNext(); }
						@Override
						public char[] next() {
							final CharSequence s = words.next();
							int i = s.length();
							final char[] a = new char[i];
							while(i-- != 0) a[i] = s.charAt(i);
							return a;
						}
					},
					ratio);
			byteFrontCodedList = null;
		}

	}

	/** Creates a new front-coded string list containing the character sequences contained in the given collection.
	 *
	 * @param c a collection containing {@linkplain CharSequence character sequences}.
	 * @param ratio the desired ratio.
	 * @param utf8 if true, the strings will be stored as UTF-8 byte arrays.
	 */
	public FrontCodedStringList(final Collection<? extends CharSequence> c, final int ratio, final boolean utf8) {
		this(c.iterator(), ratio, utf8);
	}

	/** Returns whether this front-coded string list is storing its strings as UTF-8 encoded bytes.
	 *
	 * @return true if this front-coded string list is keeping its data as an array of UTF-8 encoded bytes.
	 */
	public boolean utf8() {
		return utf8;
	}

	/** Returns the ratio of the underlying front-coded list.
	 *
	 * @return the ratio of the underlying front-coded list.
	 */
	public int ratio() {
		return utf8 ? byteFrontCodedList.ratio() : charFrontCodedList.ratio();
	}

	/**
	 * Returns the element at the specified position in this front-coded string list as a mutable
	 * string.
	 *
	 * @param index an index in the list.
	 * @return a {@link MutableString} that will contain the string at the specified position. The
	 *         string may be freely modified.
	 */
	@Override
	public MutableString get(final int index) {
		return MutableString.wrap(utf8
				? byte2Char(byteFrontCodedList.getArray(index), null)
						: charFrontCodedList.getArray(index));
	}

	/**
	 * Returns the element at the specified position in this front-coded string list by storing it in a
	 * mutable string.
	 *
	 * @param index an index in the list.
	 * @param s a mutable string that will contain the string at the specified position.
	 */
	public void get(final int index, final MutableString s) {
		if (utf8) {
			final byte[] a = byteFrontCodedList.getArray(index);
			s.length(countUTF8Chars(a));
			byte2Char(a, s.array());
		}
		else {
			s.length(s.array().length);
			int res = charFrontCodedList.get(index, s.array());
			if (res < 0) {
				s.length(s.array().length - res);
				res = charFrontCodedList.get(index, s.array());
			}
			else s.length(res);
		}
	}

	/* The following methods are highly optimized UTF-8 converters exploiting
	the fact that since it was ourselves in the first place who created the
	coding, we can be sure it is correct. */

	protected static int countUTF8Chars(final byte[] a) {
		final int length = a.length;
		int result = 0, b;
		for(int i = 0; i < length; i++) {
			b = (a[i] & 0xFF) >> 4;
		if (b < 8) result++;
		else if (b < 14) {
			result++;
			i++;
		}
		else if (b < 15) {
			result++;
			i += 2;
		}
		else {
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

		for(int i = 0, j = 0; i < length; i++) {
			b = a[i] & 0xFF;
			t = b >> 4;

		if (t < 8) s[j++] = (char)b;
		else if (t < 14) {
			c = a[++i] & 0xFF;
			if ((c & 0xC0) != 0x80) throw new IllegalStateException("Malformed internal UTF-8 encoding");
			s[j++] = (char)(((b & 0x1F) << 6) | (c & 0x3F));
		}
		else if (t < 15){
			c = a[++i] & 0xFF;
			d = a[++i];
			if ((c & 0xC0) != 0x80 || (d & 0xC0) != 0x80) throw new IllegalStateException("Malformed internal UTF-8 encoding");
			s[j++] = (char)(((b & 0x0F) << 12) | ((c & 0x3F) << 6) | ((d & 0x3F) << 0));
		}
		else {
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
	public ObjectListIterator<MutableString> listIterator(final int k) { return new ObjectListIterator<MutableString>() {
		ObjectListIterator<?> i = utf8 ? byteFrontCodedList.listIterator(k) : charFrontCodedList.listIterator(k);

		@Override
		public boolean hasNext() { return i.hasNext(); }
		@Override
		public boolean hasPrevious() { return i.hasPrevious(); }
		@Override
		public MutableString next() { return  MutableString.wrap(utf8 ? byte2Char((byte[])i.next(), null) : (char[])i.next()); }
		@Override
		public MutableString previous() { return MutableString.wrap(utf8 ? byte2Char((byte[])i.previous(), null) :(char[])i.previous()); }
		@Override
		public int nextIndex() { return i.nextIndex(); }
		@Override
		public int previousIndex() { return i.previousIndex(); }
	};
	}

	@Override
	public int size() { return utf8 ? byteFrontCodedList.size() : charFrontCodedList.size(); }

	public static void main(final String[] arg) throws IOException, JSAPException, NoSuchMethodException {

		final SimpleJSAP jsap = new SimpleJSAP(FrontCodedStringList.class.getName(), "Builds a front-coded string list reading from standard input a newline-separated ordered list of strings.",
				new Parameter[] {
						new FlaggedOption("bufferSize", IntSizeStringParser.getParser(), "64Ki", JSAP.NOT_REQUIRED, 'b',  "buffer-size", "The size of the I/O buffer used to read strings."),
						new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The file encoding."),
						new FlaggedOption("ratio", IntSizeStringParser.getParser(), "4", JSAP.NOT_REQUIRED, 'r',  "ratio", "The compression ratio."),
						new Switch("utf8", 'u', "utf8", "Store the strings as UTF-8 byte arrays."),
						new Switch("zipped", 'z', "zipped", "The string list is compressed in gzip format."),
						new UnflaggedOption("frontCodedList", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename for the serialised front-coded list.")
		});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final int bufferSize = jsapResult.getInt("bufferSize");
		final int ratio = jsapResult.getInt("ratio");
		final boolean utf8 = jsapResult.getBoolean("utf8");
		final boolean zipped = jsapResult.getBoolean("zipped");
		final String listName = jsapResult.getString("frontCodedList");
		final Charset encoding = (Charset)jsapResult.getObject("encoding");

		final Logger logger = LoggerFactory.getLogger(FrontCodedStringList.class);
		final ProgressLogger pl = new ProgressLogger(logger);
		pl.displayFreeMemory = true;
		pl.displayLocalSpeed = true;
		pl.itemsName = "strings";
		pl.start("Reading strings...");
		final FrontCodedStringList frontCodedStringList = new FrontCodedStringList(new LineIterator(new FastBufferedReader(
				new InputStreamReader(zipped ? new GZIPInputStream(System.in) : System.in, encoding), bufferSize), pl), ratio, utf8);
		pl.done();

		logger.info("Writing front-coded list to file...");
		BinIO.storeObject(frontCodedStringList, listName);
		logger.info("Completed.");
	}
}
