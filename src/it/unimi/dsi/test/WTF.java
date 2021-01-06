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

package it.unimi.dsi.test;

import java.util.Random;

public class WTF {
	/* From https://twitter.com/joshbloch/status/269478731238760448
	 *
	 * Note that ThreadLocalRandom uses the same algorithm as Random.
	 */
	public static void main(final String[] arg) {
		final int shift = arg.length == 0 ? 0 : Integer.parseInt(arg[0]);
		for (int i = 0; i < 1000; i++)
			System.out.println(new Random(i).nextInt(1 << shift));
	}
}
