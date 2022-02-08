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
import java.io.Serializable;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import it.unimi.dsi.Util;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.io.TextIO;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.lang.MutableString;

/** A {@link it.unimi.dsi.util.FrontCodedStringList} whose indices are permuted.
 *
 * <P>It may happen that a list of strings compresses very well
 * using front coding, but unfortunately alphabetical order is <em>not</em>
 * the right order for the strings in the list. Instances of this class
 * wrap an instance of {@link it.unimi.dsi.util.FrontCodedStringList}
 * together with a permutation &pi;: inquiries with index <var>i</var> will
 * actually return the string with index &pi;<sub><var>i</var></sub>.
 *
 * <P>In case you start from a newline-delimited non-sorted list of
 * UTF-8 strings, the simplest way to build
 * an instance of this map is obtaining a front-coded string list and
 * a permutation with a simple UN*X pipe (which also avoids storing the sorted strings):
 * <pre>
 * nl -v0 -nln | sort -k2 | tee &gt;(cut -f1 &gt;perm.txt) \
 * 	| cut -f2 | java it.unimi.dsi.util.FrontCodedStringList tmp-lex.fcl
 * </pre>
 * The above command will read a list of strings from standard input,
 * output a their sorted index list in <code>perm.txt</code> and create a <code>tmp-lex.fcl</code> front-coded
 * string list containing the sorted list of strings.
 *
 * <p><strong>Important</strong>: you must be sure to be using the byte-by-byte collation order&mdash;in UN*X,
 * be sure that <code>LC_COLLATE=C</code>. Failure to do so will result in an order-of-magnitude-slower sorting and
 * worse compression.
 *
 * <P>Now, in <code>perm.txt</code> you will find the permutation that you have to pass to
 * this class (given that you will use the option <code>-i</code>). So the last step is just
 * <pre>
 * java it.unimi.dsi.util.PermutedFrontCodedStringList -i -t tmp-lex.fcl perm.txt your.fcl
 * </pre>
 */

public class PermutedFrontCodedStringList extends AbstractObjectList<CharSequence> implements Serializable {

	public static final long serialVersionUID = -7046029254386353130L;

	/** The underlying front-coded string list. */
	final protected FrontCodedStringList frontCodedStringList;
	/** The permutation. */
	final protected int[] permutation;

	/** Creates a new permuted front-coded string list using a given front-coded string list and permutation.
	 *
	 * @param frontCodedStringList the underlying front-coded string list.
	 * @param permutation the underlying permutation.
	 */

	public PermutedFrontCodedStringList(final FrontCodedStringList frontCodedStringList, final int[] permutation) {
		if (frontCodedStringList.size() != permutation.length) throw new IllegalArgumentException("The front-coded string list contains " + frontCodedStringList.size() + " strings, but the permutation is on " + permutation.length + " elements.");
		this.frontCodedStringList = frontCodedStringList;
		this.permutation = permutation;
	}

	@Override
	public MutableString get(final int index) {
		return frontCodedStringList.get(permutation[index]);
	}

	/** Returns the element at the specified position in this front-coded list by storing it in a mutable string.
	 *
	 * @param index an index in the list.
	 * @param s a mutable string that will contain the string at the specified position.
	 */
	public void get(final int index, final MutableString s) {
		frontCodedStringList.get(permutation[index], s);
	}

	@Override
	public int size() {
		return frontCodedStringList.size();
	}

	@Override
	public ObjectListIterator<CharSequence> listIterator(final int k) { return new ObjectListIterator<CharSequence>() {
			final IntListIterator i = IntIterators.fromTo(0, frontCodedStringList.size());

			@Override
			public boolean hasNext() { return i.hasNext(); }
			@Override
			public boolean hasPrevious() { return i.hasPrevious(); }
			@Override
			public CharSequence next() { return frontCodedStringList.get(permutation[i.nextInt()]); }
			@Override
			public CharSequence previous() { return frontCodedStringList.get(permutation[i.previousInt()]); }
			@Override
			public int nextIndex() { return i.nextIndex(); }
			@Override
			public int previousIndex() { return i.previousIndex(); }
		};
	}

	public static void main(final String[] arg) throws IOException, ClassNotFoundException, JSAPException {

		final SimpleJSAP jsap = new SimpleJSAP(PermutedFrontCodedStringList.class.getName(), "Builds a permuted front-coded list of strings using a given front-coded string list and a permutation (either in text or binary format).",
				new Parameter[] {
						new Switch("invert", 'i', "invert", "Invert permutation before creating the permuted list."),
						new Switch("text", 't', "text", "The permutation is a text file."),
						new UnflaggedOption("list", JSAP.STRING_PARSER, JSAP.REQUIRED, "A front-coded string list."),
						new UnflaggedOption("permutation", JSAP.STRING_PARSER, JSAP.REQUIRED, "A permutation for the indices of the list (ints in DataInput format, unless you specify --text)."),
						new UnflaggedOption("permutedList", JSAP.STRING_PARSER, JSAP.REQUIRED, "A the filename for the resulting permuted list."),
			});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final String permutationFile = jsapResult.getString("permutation");
		final int[] permutation = jsapResult.userSpecified("text")
				? IntIterators.unwrap(TextIO.asIntIterator(permutationFile))
				: BinIO.loadInts(permutationFile);
		if (jsapResult.getBoolean("invert")) Util.invertPermutationInPlace(permutation);

		BinIO.storeObject(
				new PermutedFrontCodedStringList((FrontCodedStringList)BinIO.loadObject(jsapResult.getString("list")), permutation),
				jsapResult.getString("permutedList")
		);
	}
}
