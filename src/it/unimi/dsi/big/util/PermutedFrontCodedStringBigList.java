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
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.io.TextIO;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigListIterator;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.util.FrontCodedStringList;
import it.unimi.dsi.util.PermutedFrontCodedStringList;

/**
 * A {@link it.unimi.dsi.big.util.FrontCodedStringBigList} whose indices are permuted.
 *
 * <P>
 * This class is functionally identical to {@link PermutedFrontCodedStringList}, except for the
 * larger size allowed.
 *
 * @see FrontCodedStringList
 * @see PermutedFrontCodedStringList
 */

public class PermutedFrontCodedStringBigList extends AbstractObjectBigList<CharSequence> implements Serializable {

	public static final long serialVersionUID = 1;

	/** The underlying front-coded string list. */
	final protected FrontCodedStringBigList frontCodedStringBigList;
	/** The permutation. */
	final protected long[][] permutation;

	/**
	 * Creates a new permuted front-coded string list using a given front-coded string list and
	 * permutation.
	 *
	 * @param frontCodedStringBihList the underlying front-coded string big list.
	 * @param permutation the underlying permutation (a {@linkplain BigArrays big array} of longs).
	 */

	public PermutedFrontCodedStringBigList(final FrontCodedStringBigList frontCodedStringBihList, final long[][] permutation) {
		if (frontCodedStringBihList.size64() != BigArrays.length(permutation)) throw new IllegalArgumentException("The front-coded string big list contains " + frontCodedStringBihList.size64() + " strings, but the permutation is on " + BigArrays.length(permutation) + " elements.");
		this.frontCodedStringBigList = frontCodedStringBihList;
		this.permutation = permutation;
	}

	@Override
	public MutableString get(final long index) {
		return frontCodedStringBigList.get(BigArrays.get(permutation, index));
	}

	/** Returns the element at the specified position in this front-coded list by storing it in a mutable string.
	 *
	 * @param index an index in the list.
	 * @param s a mutable string that will contain the string at the specified position.
	 */
	public void get(final long index, final MutableString s) {
		frontCodedStringBigList.get(BigArrays.get(permutation, index), s);
	}

	@Override
	public long size64() {
		return frontCodedStringBigList.size64();
	}

	@Override
	public ObjectBigListIterator<CharSequence> listIterator(final long k) {
		return new ObjectBigListIterator<CharSequence>() {
			final LongBidirectionalIterator i = LongIterators.fromTo(0, frontCodedStringBigList.size64());
			long p = 0;

			@Override
			public boolean hasNext() { return i.hasNext(); }
			@Override
			public boolean hasPrevious() { return i.hasPrevious(); }
			@Override
			public CharSequence next() {
				p++;
				return frontCodedStringBigList.get(BigArrays.get(permutation, i.nextLong()));
			}
			@Override
			public CharSequence previous() {
				p--;
				return frontCodedStringBigList.get(BigArrays.get(permutation, i.previousLong()));
			}
			@Override
			public long nextIndex() {
				return p;
			}
			@Override
			public long previousIndex() {
				return p - 1;
			}
		};
	}

	public static void main(final String[] arg) throws IOException, ClassNotFoundException, JSAPException {

		final SimpleJSAP jsap = new SimpleJSAP(PermutedFrontCodedStringList.class.getName(), "Builds a permuted front-coded list of strings using a given front-coded string list and a permutation (either in text or binary format).",
				new Parameter[] {
						new Switch("invert", 'i', "invert", "Invert permutation before creating the permuted list."),
						new Switch("text", 't', "text", "The permutation is a text file."),
						new UnflaggedOption("list", JSAP.STRING_PARSER, JSAP.REQUIRED, "A front-coded string big list."),
						new UnflaggedOption("permutation", JSAP.STRING_PARSER, JSAP.REQUIRED, "A permutation for the indices of the list (longs in DataInput format, unless you specify --text)."),
						new UnflaggedOption("permutedList", JSAP.STRING_PARSER, JSAP.REQUIRED, "A the filename for the resulting permuted big list."),
			});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final String permutationFile = jsapResult.getString("permutation");
		final long[][] permutation = jsapResult.userSpecified("text") ? LongIterators.unwrapBig(TextIO.asLongIterator(permutationFile)) : BinIO.loadLongsBig(permutationFile);
		if (jsapResult.getBoolean("invert")) Util.invertPermutationInPlace(permutation);

		BinIO.storeObject(
				new PermutedFrontCodedStringBigList((FrontCodedStringBigList)BinIO.loadObject(jsapResult.getString("list")), permutation),
				jsapResult.getString("permutedList")
		);
	}
}
