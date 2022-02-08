/*
 * DSI utilities
 *
 * Copyright (C) 2012-2022 Sebastiano Vigna
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

import it.unimi.dsi.lang.MutableString;

public class MutableStringLengthSpeedTest {

	private MutableStringLengthSpeedTest() {}

	public static void main(final String[] arg) {

		long i, n;

		n = Long.parseLong(arg[0]);

		final MutableString s = new MutableString("foobar0");
		final MutableString t = new MutableString("foobar1");
		final String u = new String("foobar2");
		final StringBuffer v = new StringBuffer("foobar3");
		final StringBuilder w = new StringBuilder("foobar4");

		int k = 10;
		int x = 0;

		while (k-- != 0) {
			long start;

			System.out.println();

			start = -System.nanoTime();

			i = n;
			while (i-- != 0) x ^= u.length();

			start += System.nanoTime();

			System.out.println("Called length() " + n + " times on a string in " + start + " ns (" + start / (double)n + " ns/call)");


			start = -System.nanoTime();

			i = n;
			while (i-- != 0) x ^= t.length();

			start += System.nanoTime();

			System.out.println("Called length() " + n + " times on a compact string in " + start + " ns (" + start / (double)n + " ns/call)");

			start = -System.nanoTime();

			i = n;
			s.loose();
			while (i-- != 0) x ^= s.length();

			start += System.nanoTime();

			System.out.println("Called length() " + n + " times on a loose string in " + start + " ns (" + start / (double)n + " ns/call)");

			start = -System.nanoTime();

			i = n;
			while (i-- != 0) x ^= v.length();

			start += System.nanoTime();

			System.out.println("Called length() " + n + " times on a string buffer in " + start + " ns (" + start / (double)n + " ns/call)");

			start = -System.nanoTime();

			i = n;
			while (i-- != 0) x ^= w.length();

			start += System.nanoTime();
			if (x == 0) System.out.println();
			System.out.println("Called length() " + n + " times on a string builder in " + start + " ns (" + start / (double)n + " ns/call)");
		}

	}

}
