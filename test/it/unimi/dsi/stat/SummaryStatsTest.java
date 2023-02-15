/*
 * DSI utilities
 *
 * Copyright (C) 2011-2023 Sebastiano Vigna
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

package it.unimi.dsi.stat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SummaryStatsTest {
	@Test
	public void test() {
		final SummaryStats summaryStats = new SummaryStats();
		summaryStats.add(0);
		assertEquals(0, summaryStats.sum(), 0);
		assertEquals(0, summaryStats.mean(), 0);
		assertEquals(0, summaryStats.variance(), 0);
		assertEquals(0, summaryStats.min(), 0);
		assertEquals(0, summaryStats.max(), 0);
		assertEquals(1, summaryStats.size64());

		summaryStats.add(1);
		assertEquals(1, summaryStats.sum(), 0);
		assertEquals(.5, summaryStats.mean(), 0);
		assertEquals(.25, summaryStats.variance(), 0);
		assertEquals(0, summaryStats.min(), 0);
		assertEquals(1, summaryStats.max(), 0);
		assertEquals(2, summaryStats.size64());
	}
}
