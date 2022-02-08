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

import it.unimi.dsi.lang.MutableString;

public class MutableStringReplaceSpeedTest {

	private MutableStringReplaceSpeedTest() {}

	public static void main(final String[] arg) throws IOException {

		String target = null;

		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		final MutableString ms = new MutableString();
		String line;
		while ((line = br.readLine()) != null) ms.append("\n").append(line);
		target = ms.toString();

		MutableString s;
		String st;
		final String searchString = arg[0];
		if (searchString.length() != 1) throw new IllegalArgumentException();
		final char searchChar = new MutableString(arg[0]).charAt(0);
		final String replaceString = arg[1];
		final MutableString replace = new MutableString(replaceString);
		final int n = Integer.parseInt(arg[2]);
		long start;

		for (int k = 10; k-- != 0;) {
			System.out.println();

			s = new MutableString(target).compact();
			start = -System.nanoTime();
			for(int i = n; i-- != 0;) s.replace(searchChar, replace);
			start += System.nanoTime();
			System.out.println("Called replace() " + n + " times on a compact string in " + start + " ns (" + start / (double)n + " ns/call)");

			s = new MutableString(target).loose();
			start = -System.nanoTime();
			for(int i = n; i-- != 0;) s.replace(searchChar, replace);
			start += System.nanoTime();
			System.out.println("Called replace() " + n + " times on a loose string in " + start + " ns (" + start / (double)n + " ns/call)");

			final StringBuilder sb = new StringBuilder(target);
			start = -System.nanoTime();

			for(int i = n; i-- != 0;) {
				int j = sb.length();
				for (;;) {
					j = sb.lastIndexOf(searchString, j);
					if (j == -1) break;
					sb.replace(j, j + 1, replaceString);
					j--;
				}
			}

			start += System.nanoTime();
			System.out.println("Called replace() " + n + " times on a string builder in " + start + " ns (" + start / (double)n + " ns/call)");
			assert sb.length() == s.length();
			assert s.toString().equals(sb.toString());

			st = new String(target);
			start = -System.nanoTime();
			for(int i = n; i-- != 0;) st = st.replaceAll(searchString, replaceString);
			start += System.nanoTime();
			System.out.println("Called replaceAll() " + n + " times on a string in " + start + " ns (" + start / (double)n + " ns/call)");
			assert sb.length() == st.length();
			assert st.equals(sb.toString());
		}
	}
}
