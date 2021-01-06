/*
 * DSI utilities
 *
 * Copyright (C) 2007-2021 Sebastiano Vigna
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

package it.unimi.dsi.bits;

import java.io.Serializable;

import it.unimi.dsi.fastutil.booleans.BooleanBigArrayBigList;
import it.unimi.dsi.fastutil.booleans.BooleanBigList;
import it.unimi.dsi.fastutil.booleans.BooleanBigLists;
import it.unimi.dsi.fastutil.booleans.BooleanList;

/** A boolean-list based implementation of {@link BitVector}.
 *
 * <P>This implementation of a bit vector is based on a backing
 * list of booleans. It is rather inefficient, but useful for
 * wrapping purposes, for covering completely the code in
 * {@link AbstractBitVector} and for creating mock objects.
 */
public class BooleanListBitVector extends AbstractBitVector implements Serializable {
	private static final long serialVersionUID = 1L;
	/** The backing list. */
	private final BooleanBigList list;

	protected static final void ensureIntegerIndex(final long index) {
		if (index > Integer.MAX_VALUE) throw new IllegalArgumentException("This BitVector implementation accepts integer indices only");
	}

	public static BooleanListBitVector getInstance(final long capacity) {
		if (capacity > Integer.MAX_VALUE) throw new IllegalArgumentException("This BitVector implementation accepts integer indices only");
		return new BooleanListBitVector((int)capacity);
	}

	/** Creates a new empty bit vector. */
	public static BooleanListBitVector getInstance() {
		return new BooleanListBitVector(0);
	}

	/** Creates a new bit vector with given bits. */
	public static BooleanListBitVector of(final int... bit) {
		final BooleanListBitVector bitVector = BooleanListBitVector.getInstance(bit.length);
		for(final int b : bit) bitVector.add(b);
		return bitVector;
	}


	protected BooleanListBitVector(final BooleanBigList list) { this.list = list; }

	protected BooleanListBitVector(final int capacity) {
		this(new BooleanBigArrayBigList(capacity));
	}

	public static BooleanListBitVector wrap(final BooleanList list) {
		return new BooleanListBitVector(BooleanBigLists.asBigList(list));
	}

	public static BooleanListBitVector wrap(final BooleanBigList list) {
		return new BooleanListBitVector(list);
	}

	@Override
	public long length() {
		return list.size64();
	}

	@Override
	public boolean set(final long index, final boolean value) {
		ensureIntegerIndex(index);
		return list.set((int)index, value);
	}

	@Override
	public boolean getBoolean(final long index) {
		ensureIntegerIndex(index);
		return list.getBoolean((int)index);
	}

	@Override
	public void add(final long index, final boolean value) {
		ensureIntegerIndex(index);
		list.add((int)index, value);
	}

	@Override
	public boolean removeBoolean(final long index) {
		ensureIntegerIndex(index);
		return list.removeBoolean((int)index);
	}

	@Override
	public BooleanListBitVector copy(final long from, final long to) {
		BitVectors.ensureFromTo(length(), from, to);
		return new BooleanListBitVector(list.subList((int)from, (int)to));
	}

	@Override
	public BooleanListBitVector copy() {
		return new BooleanListBitVector(new BooleanBigArrayBigList(list));
	}

	public BitVector ensureCapacity(final long numBits) {
		if (numBits > Integer.MAX_VALUE) throw new IllegalArgumentException("This BitVector implementation accepts integer indices only");
		list.size((int)numBits);
		return this;
	}

	@Override
	public BitVector length(final long numBits) {
		if (numBits > Integer.MAX_VALUE) throw new IllegalArgumentException("This BitVector implementation accepts integer indices only");
		list.size((int)numBits);
		return this;
	}
}
