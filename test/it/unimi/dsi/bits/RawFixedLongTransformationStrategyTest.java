/*
 * DSI utilities
 *
 * Copyright (C) 2010-2021 Sebastiano Vigna
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RawFixedLongTransformationStrategyTest {

	@Test
	public void testGetBoolean() {
		final TransformationStrategy<Long> rawFixedLong = TransformationStrategies.rawFixedLong();
		BitVector p = rawFixedLong.toBitVector(Long.valueOf(0));
		for(int i = Long.SIZE; i-- != 0;) assertFalse(p.getBoolean(i));
		p = rawFixedLong.toBitVector(Long.valueOf(0xDEADBEEFDEADF00DL));
		for(int i = Long.SIZE; i-- != 0;) assertTrue(p.getBoolean(i) == ((0xDEADBEEFDEADF00DL & 1L << i) != 0));
	}

	@Test
	public void testGetLong() {
		final TransformationStrategy<Long> rawFixedLong = TransformationStrategies.rawFixedLong();
		final BitVector p = rawFixedLong.toBitVector(Long.valueOf(0xDEADBEEFDEADF00DL));
		for(int from = Long.SIZE; from-- != 0;)
			for(int to = Long.SIZE; from < to--;)
				assertTrue(p.getLong(from, to) == LongArrayBitVector.wrap(new long[] { 0xDEADBEEFDEADF00DL }).getLong(from, to));
	}

}
