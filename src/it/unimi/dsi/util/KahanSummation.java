/*
 * DSI utilities
 *
 * Copyright (C) 2012-2021 Sebastiano Vigna
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

/** <a href="http://en.wikipedia.org/wiki/Kahan_summation_algorithm">Kahan's
 * summation algorithm</a> encapsulated in an object.  */

public class KahanSummation {
	/** The current value of the sum. */
	private double value;
	/** The current correction. */
	private double c;

	/** Adds a value.
	 * @param v the value to be added to the sum.
	 */
	public void add(final double v) {
		final double y = v - c;
		final double t = value + y;
		c = (t - value) - y;
		value = t;
	}

	/** Returns the sum computed so far.
	 * @return the sum computed so far.
	 */
	public double value() {
		return value;
	}

	/** Resets the current value and correction to zero. */
	public void reset() {
		value = c = 0;
	}
}
