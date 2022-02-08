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

		int i, n;

		final String p = arg[0];

		int k;
		long start;
		System.out.println("Searching for " + arg[0]);

		for(k = 10; k-- != 0;) {
			System.out.println();

			start = - System.nanoTime();
			n = 0;

			for(int r = 100; r-- != 0;) {
				i = -1;
				do {
					i = target.indexOf(p, i + 1);
					n++;
				} while(i != -1);

			}

			start += System.nanoTime();

			System.out.println("Called indexOf() " + n + " times on a string in " + start + " ns (" + Util.format(start / (double)n) + " ns/call)");
			final TextPattern tp = new TextPattern(p);
			final char a[] = ms.array();

			start = - System.nanoTime();
			n = 0;

			for(int r = 100; r-- != 0;) {
				i = -1;
				do {
					i = tp.search(a, i + 1);
					n++;
				} while(i != -1);
			}

			start += System.nanoTime();

			System.out.println("Called search() " + n + " times on a string in " + start + " ns (" + Util.format(start / (double)n) + " ns/call)");

			final MutableString pattern = new MutableString(p);
			start = - System.nanoTime();
			n = 0;

			for(int r = 100; r-- != 0;) {
				i = -1;
				do {
					i = ms.indexOf(pattern, i + 1);
					n++;
				} while(i != -1);
			}

			start += System.nanoTime();

			System.out.println("Called indexOf() " + n + " times on a mutable string in " + start + " ns (" + Util.format(start / (double)n) + " ns/call)");
		}
	}
}
