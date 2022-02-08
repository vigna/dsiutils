/*
 * DSI utilities
 *
 * Copyright (C) 2009-2022 Sebastiano Vigna
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

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
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

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigList;
import it.unimi.dsi.io.FileLinesMutableStringIterable;
import it.unimi.dsi.lang.MutableString;

/** A string map based on a function signed using the original list of strings.
 *
 * <p>A minimal perfect hash function maps a set of string to an initial segment of the natural
 * numbers, but will actually map <em>any</em> string to that segment. We can check that
 * a string is part of the key set by hashing it to a value <var>h</var>, and checking that the <var>h</var>-th
 * string of the original list does coincide. Since, moreover, this class implements {@link StringMap},
 * and thus {@linkplain #list() exposes the original list}, we have a two-way
 * dictionary. In other words, this is a <em>full</em> {@link StringMap} implementation.
 *
 * <p>Note that some care must be exercised: {@link CharSequence}'s contract does not
 * prescribe equality by content, so if your function behaves badly on some implementations of
 * {@link CharSequence} you might make the checks fail. To avoid difficulties, the
 * constructor checks that every string in the list is hashed correctly.
 *
 * <p>For the same reason, this class implements <code>StringMap&lt;MutableString&gt;</code>, and
 * requires that the list of strings provided at construction time is actually a list of
 * {@linkplain MutableString mutable strings}.
 *
 * <!-- <p>A typical usage of this class pairs a {@link it.unimi.dsi.util.FrontCodedStringList} with some kind
 * of succinct structure from <a href="http://sux4j.di.unimi.it/">Sux4J</a>. -->
 *
 * @author Sebastiano Vigna
 * @since 2.0
 */



public class LiterallySignedStringMap extends AbstractObject2LongFunction<CharSequence> implements StringMap<MutableString>, Serializable, Size64 {
	private static final long serialVersionUID = 0L;

	/** The underlying map. */
	protected final Object2LongFunction<? extends CharSequence> function;
	/** The underlying list. */
	protected final ObjectBigList<? extends MutableString> list;
	/** The size of {@link #list}. */
	protected final long size;

	/** Creates a new shift-add-xor signed string map using a given hash map.
	 *
	 * @param function a function mapping each string in <code>list</code> to its ordinal position.
	 * @param list a list of strings.
	 */

	public LiterallySignedStringMap(final Object2LongFunction<? extends CharSequence> function, final ObjectBigList<? extends MutableString> list) {
		this.function = function;
		this.list = list;
		size = list.size64();
		for(long i = 0; i < size; i++) if (function.getLong(list.get(i)) != i) throw new IllegalArgumentException("Function and list do not agree");
		defRetValue = -1;
	}

	@SuppressWarnings("null")
	@Override
	public long getLong(final Object o) {
		final CharSequence s = (CharSequence)o;
		final long index = function.getLong(s);
		return index >= 0 && index < size && list.get((int)index).equals(s) ? index : defRetValue;
	}

	@SuppressWarnings("null")
	@Deprecated
	@Override
	public Long get(final Object o) {
		final CharSequence s = (CharSequence)o;
		final long index = function.getLong(s);
		return index >= 0 && index < size && list.get((int)index).equals(s) ? Long.valueOf(index) : null;
	}

	@Override
	public long size64() {
		return list.size64();
	}

	@Override
	@Deprecated
	public int size() {
		return size64() > Integer.MAX_VALUE ? -1 : (int)size64();
	}

	@Override
	public boolean containsKey(final Object o) {
		return getLong(o) != -1;
	}

	@Override
	public ObjectBigList<? extends MutableString> list() {
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(final String[] arg) throws IOException, JSAPException, ClassNotFoundException, SecurityException, NoSuchMethodException {

		final SimpleJSAP jsap = new SimpleJSAP(LiterallySignedStringMap.class.getName(), "Builds a shift-add-xor signed string map by reading a newline-separated list of strings and a function built on the same list of strings.",
				new Parameter[] {
						new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The string file encoding."),
						new Switch("zipped", 'z', "zipped", "The string list is compressed in gzip format."),
						new Switch("text", 't', "text", "The string list actually a text file, with one string per line."),
						new UnflaggedOption("function", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the function to be signed."),
						new UnflaggedOption("list", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the serialised list of strings, or of a text file containing a list of strings, if -t is specified."),
						new UnflaggedOption("map", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename of the resulting map."),
		});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final String functionName = jsapResult.getString("function");
		final String listName = jsapResult.getString("list");
		final String mapName = jsapResult.getString("map");


		final Charset encoding = (Charset)jsapResult.getObject("encoding");
		final boolean zipped = jsapResult.getBoolean("zipped");
		final boolean text = jsapResult.getBoolean("text");

		final ObjectBigList<MutableString> list;
		if (text) {
			list = new ObjectBigArrayBigList<>();
			for (final MutableString s : new FileLinesMutableStringIterable(listName, encoding.toString(), zipped ? GZIPInputStream.class : null)) list.add(s.copy());
		} else list = (ObjectBigList<MutableString>)BinIO.loadObject(listName);

		final Logger logger = LoggerFactory.getLogger(LiterallySignedStringMap.class);
		logger.info("Signing...");
		BinIO.storeObject(new LiterallySignedStringMap((Object2LongFunction)BinIO.loadObject(functionName), list), mapName);
		logger.info("Completed.");
	}
}
