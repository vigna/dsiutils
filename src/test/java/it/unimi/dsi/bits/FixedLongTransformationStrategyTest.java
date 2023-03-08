/*
 * DSI utilities
 *
 * Copyright (C) 2010-2023 Sebastiano Vigna
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FixedLongTransformationStrategyTest {

	@Test
	public void testGetBoolean() {
		final TransformationStrategy<Long> fixedLong = TransformationStrategies.fixedLong();
		BitVector p = fixedLong.toBitVector(Long.valueOf(0));
		for (int i = Long.SIZE; i-- != 1;) assertFalse(p.getBoolean(i));

		// Flipped bit
		assertTrue(p.getBoolean(0));
		p = fixedLong.toBitVector(Long.valueOf(0xDEADBEEFDEADF00DL));
		for (int i = Long.SIZE; i-- != 0;) assertTrue(p.getBoolean(i) == (((0xDEADBEEFDEADF00DL ^ 1L << 63) & 1L << Long.SIZE - 1 - i) != 0));
	}

	@Test
	public void testGetLong() {
		final TransformationStrategy<Long> fixedLong = TransformationStrategies.fixedLong();
		final BitVector p = fixedLong.toBitVector(Long.valueOf(0xDEADBEEFDEADF00DL));
		for(int from = Long.SIZE; from-- != 0;)
			for (int to = Long.SIZE; from < to--;) 
				assertEquals(LongArrayBitVector.wrap(new long[] {
						Long.reverse(0xDEADBEEFDEADF00DL) ^ 1 }).getLong(from, to), p.getLong(from, to));
	}

}
