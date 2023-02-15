/*
 * DSI utilities
 *
 * Copyright (C) 2007-2023 Sebastiano Vigna
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

import java.io.EOFException;
import java.io.IOException;

import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

/** Provides semi-external random access to a list of {@linkplain OutputBitStream#writeGamma(int) &gamma;-encoded} integers.
 *
 * <p>This class is a semi-external {@link it.unimi.dsi.fastutil.longs.LongList} that
 * MG4J uses to access files containing frequencies.
 *
 * <p>Instead, this class accesses frequencies in their
 * compressed forms, and provides entry points for random access to each long. At construction
 * time, entry points are computed with a certain <em>step</em>, which is the number of longs
 * accessible from each entry point, or, equivalently, the maximum number of longs that will
 * be necessary to read to access a given long.
 *
 * <p><strong>Warning:</strong> This class is not thread safe, and needs to be synchronised to be used in a
 * multithreaded environment.
 *
 * @author Fabien Campagne
 * @author Sebastiano Vigna
 */
public class SemiExternalGammaList extends AbstractLongList {
	public static final int DEFAULT_STEP = 128;

	/** Position in the offset stream for each random access entry point (one each {@link #step} elements). */
	private final long[] position;
	/** Stream over the compressed offset information. */
	private final InputBitStream ibs;
	/** Maximum number of longs to skip. */
	private final int step;
	/** The number of longs. */
	private final int numLongs;

	/** Creates a new semi-external list.
	 *
	 * @param longs a bit stream containing &gamma;-encoded longs.
	 * @param step the step used to build random-access entry points, or -1 to get {@link #DEFAULT_STEP}.
	 * @param numLongs the overall number of offsets (i.e., the number of terms).
	 */

	public SemiExternalGammaList(final InputBitStream longs, final int step, final int numLongs) throws IOException {
		this.step = step == -1 ? DEFAULT_STEP : step;
		final int slots = (numLongs + this.step - 1) / this.step;
		this.position = new long[slots];
		this.numLongs = numLongs;
		this.ibs = longs;
		ibs.position(0);
		ibs.readBits(0);
		final int lastSlot = position.length - 1;
		for (int i = 0; i <= lastSlot; i++) {
			position[i] = ibs.readBits();
			if (i != lastSlot) ibs.skipGammas(this.step);
		}
	}


	/** Creates a new semi-external list.
	 *
	 * <p>This quick-and-dirty constructor estimates the number of longs by checking
	 * for an {@link EOFException}.
	 *
	 * @param longs a bit stream containing &gamma;-encoded longs.
	 */

	public SemiExternalGammaList(final InputBitStream longs) throws IOException {
		this(longs, DEFAULT_STEP, estimateNumberOfLongs(longs));
	}

	private static int estimateNumberOfLongs(final InputBitStream longs) {
		int numLongs = 0;
		try {
			longs.position(0);
			for(;;) {
				longs.readLongGamma();
				numLongs++;
			}
		}
		catch(final EOFException e) {
			return numLongs;
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public final long getLong(final int index) {
		if (index < 0 || index >= numLongs) throw new IndexOutOfBoundsException(Integer.toString(index));
		final int slotNumber = index / step;
		final int k = index % step;
		try {
			ibs.position(position[slotNumber]);
			ibs.skipGammas(k);
			return ibs.readLongGamma();
		}
		catch(final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int size() {
		return numLongs;
	}
}
