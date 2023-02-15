/*
 * DSI utilities
 *
 * Copyright (C) 2012-2023 Sebastiano Vigna
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import it.unimi.dsi.Util;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.util.TextPattern;

public class TextPatternSpeedTest  {

	private TextPatternSpeedTest() {}

	public static void main(final String[] arg) {

		String target = null;
		final MutableString ms = new MutableString();

		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = br.readLine()) != null) ms.append("\n").append(line);
			ms.compact();
			target = ms.toString();
		} catch (final IOException e) {
			System.out.println("Problems while reading target");
			e.printStackTrace(System.out);
			System.exit(1);
		}

		int u = 0;

		final String p = arg[0];

		int k;
		long elapsed;
		System.out.println("Searching for " + p);
		final int n = 10000;

		for(k = 10; k-- != 0;) {
			System.out.println();

			elapsed = -System.nanoTime();

			for (int r = n; r-- != 0;) {
				int i = -1;
				do u ^= (i = target.indexOf(p, i + 1)); while (i != -1);
			}

			elapsed += System.nanoTime();

			System.out.println("Called indexOf() " + n + " times on a string in " + elapsed + " ns (" + Util.format(elapsed / (double)n) + " ns/call)");
			final TextPattern tp = new TextPattern(p);
			final char a[] = ms.array();

			elapsed = -System.nanoTime();

			for (int r = n; r-- != 0;) {
				int i = -1;
				do u ^= (i = tp.search(a, i + 1)); while (i != -1);
			}

			elapsed += System.nanoTime();

			System.out.println("Called search() " + n + " times on a string in " + elapsed + " ns (" + Util.format(elapsed / (double)n) + " ns/call)");

			final MutableString pattern = new MutableString(p);
			elapsed = -System.nanoTime();

			for (int r = n; r-- != 0;) {
				int i = -1;
				do u ^= (i = ms.indexOf(pattern, i + 1)); while (i != -1);
			}

			elapsed += System.nanoTime();

			System.out.println("Called indexOf() " + n + " times on a mutable string in " + elapsed + " ns (" + Util.format(elapsed / (double)n) + " ns/call)");
		}

		if (u == 0) System.out.println((char)0);
	}
}
