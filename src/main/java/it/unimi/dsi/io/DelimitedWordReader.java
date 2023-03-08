/*
 * DSI utilities
 *
 * Copyright (C) 2009-2023 Sebastiano Vigna
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

package it.unimi.dsi.io;

import java.io.Reader;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.chars.CharSets;
import it.unimi.dsi.lang.MutableString;

/** A word reader that breaks words on a given set of characters.
 *
 * <p>This class is a simple subclass of {@link FastBufferedReader}. It
 * overwrites {@link #isWordConstituent(char)} so that word constituents
 * are defined negatively by a set of <em>delimiters</em> defined at construction time.
 * There is a {@link DelimitedWordReader#DelimitedWordReader(String) constructor
 * accepting the delimiter set as a string}. Note that LF and CR are <em>always</em> considered to be delimiters.
 *
 */
public class DelimitedWordReader extends FastBufferedReader {
	private static final long serialVersionUID = 1L;

	/** The set of delimiters used to break the character stream into words. */
	private final CharOpenHashSet delimiters;

	@Override
	protected boolean isWordConstituent(final char c) {
		return ! delimiters.contains(c);
	}

	private void addCrLf() {
		delimiters.add('\n');
		delimiters.add('\r');
	}

	/** Creates a new delimited word reader with a given buffer size and set of delimiters.
	 * The wrapped reader will have to be set later using {@link #setReader(Reader)}.
	 *
	 * @param bufferSize the size in bytes of the internal buffer.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final int bufferSize, final CharSet delimiters) {
		super(bufferSize);
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	/** Creates a new delimited word reader with a buffer of {@link #DEFAULT_BUFFER_SIZE} characters.
	 * The wrapped reader will have to be set later using {@link #setReader(Reader)}.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final CharSet delimiters) {
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	/** Creates a new delimited word reader with a buffer of {@link #DEFAULT_BUFFER_SIZE} characters.
	 * The wrapped reader will have to be set later using {@link #setReader(Reader)}.
	 *
	 * <p><strong>Warning</strong>: it is easy to mistake this method for one whose semantics is
	 * the same as {@link FastBufferedReader#FastBufferedReader(MutableString)}, that is, wrapping the argument
	 * string in a reader.
	 *
	 * @param delimiters a set of characters that will be considered word delimiters, specified as a string.
	 */
	public DelimitedWordReader(final String delimiters) {
		this(new CharOpenHashSet(delimiters.toCharArray()));
	}

	/** Creates a new delimited word reader with a given buffer size and set of delimiters.
	 * The wrapped reader will have to be set later using {@link #setReader(Reader)}.
	 *
	 * @param bufferSize the size in bytes of the internal buffer, specified as a string.
	 * @param delimiters a set of characters that will be considered word delimiters, specified as a string.
	 */
	public DelimitedWordReader(final String bufferSize, final String delimiters) {
		this(Integer.parseInt(bufferSize), new CharOpenHashSet(delimiters.toCharArray()));
	}

	/** Creates a new delimited word reader by wrapping a given reader with a given buffer size and using a set of delimiters.
	 *
	 * @param r a reader to wrap.
	 * @param bufferSize the size in bytes of the internal buffer.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final Reader r, final int bufferSize, final CharSet delimiters) {
		super(r, bufferSize);
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	/** Creates a new delimited word reader by wrapping a given reader with a buffer of {@link #DEFAULT_BUFFER_SIZE} characters using a given set of delimiters.
	 *
	 * @param r a reader to wrap.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final Reader r, final CharSet delimiters) {
		super(r);
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	/** Creates a new delimited word reader by wrapping a given fragment of a character array and using a set delimiters.
	 *
	 * <p>The effect of {@link #setReader(Reader)} on a buffer created with
	 * this constructor is undefined.
	 *
	 * @param array the array that will be wrapped by the reader.
	 * @param offset the first character to be used.
	 * @param length the number of character to be used.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final char[] array, final int offset, final int length, final CharSet delimiters) {
		super(array, offset, length);
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	/** Creates a new delimited word reader by wrapping a given character array and using a set delimiters.
	 *
	 * <p>The effect of {@link #setReader(Reader)} on a buffer created with
	 * this constructor is undefined.
	 *
	 * @param array the array that will be wrapped by the reader.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final char[] array, final CharSet delimiters) {
		super(array);
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	/** Creates a new delimited word reader by wrapping a given mutable string and using a set of delimiters.
	 *
	 * <p>The effect of {@link #setReader(Reader)} on a buffer created with
	 * this constructor is undefined.
	 *
	 * @param s the mutable string that will be wrapped by the reader.
	 * @param delimiters a set of characters that will be considered word delimiters.
	 */
	public DelimitedWordReader(final MutableString s, final CharSet delimiters) {
		super(s, CharSets.EMPTY_SET);
		this.delimiters = new CharOpenHashSet(delimiters, Hash.VERY_FAST_LOAD_FACTOR);
		addCrLf();
	}

	@Override
	public DelimitedWordReader copy() {
		// TODO: improve this by sharing the backing set.
		return new DelimitedWordReader(bufferSize, delimiters);
	}

	@Override
	public String toSpec() {
		return toString();
	}

	@Override
	public String toString() {
		final String className = getClass().getName();
		final CharOpenHashSet additionalDelimiters = delimiters.clone();
		additionalDelimiters.remove('\n');
		additionalDelimiters.remove('\r');
		final String delimiters = new String(additionalDelimiters.toCharArray());
		if (bufferSize == DEFAULT_BUFFER_SIZE) return className + "(\"" + delimiters + "\")";
		return className + "(" + bufferSize + ",\"" + delimiters + "\")";
	}

}
