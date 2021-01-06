/*
 * DSI utilities
 *
 * Copyright (C) 2011-2021 Sebastiano Vigna
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;


//RELEASE-STATUS: DIST

public class KahanSummationTest {
	@Test
	public void testSum() {
		final KahanSummation sum = new KahanSummation();
		sum.add(1);
		sum.add(2);
		sum.add(3);
		assertEquals(6, sum.value(), 0);
	}

	@Test
	public void testDifficult() {
		final KahanSummation sum = new KahanSummation();
		sum.add(Double.MIN_NORMAL);
		sum.add(Double.MIN_NORMAL);
		sum.add(-Double.MIN_NORMAL);
		assertEquals(Double.MIN_NORMAL, sum.value(), 0);
	}
}
