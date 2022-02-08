/*
 * DSI utilities
 *
 * Copyright (C) 2011-2022 Sebastiano Vigna
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

import java.util.ArrayList;

import org.junit.Test;


public class JackknifeTest {
	@Test
	public void test() {
		final ArrayList<double[]> samples = new ArrayList<>();
		samples.add(new double[] { 1 });
		samples.add(new double[] { 2 });
		samples.add(new double[] { 3 });
		// Linear statistics must pass through the jackknife without bias.
		final Jackknife average = Jackknife.compute(samples, Jackknife.IDENTITY);
		assertEquals(2, average.estimate[0], 1E-30);
		assertEquals(Math.sqrt(((1 - 2) * (1 - 2) + (3 - 2) * (3 - 2)) / 6.), average.standardError[0], 1E-30);
	}
}
