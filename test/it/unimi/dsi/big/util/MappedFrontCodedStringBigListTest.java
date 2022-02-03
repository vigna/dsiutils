/*
 * DSI utilities
 *
 * Copyright (C) 2010-2021 Sebastiano Vigna
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

package it.unimi.dsi.big.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import it.unimi.dsi.lang.MutableString;

public class MappedFrontCodedStringBigListTest {

	@Test
	public void test() throws IOException, ConfigurationException {
		final String basename = File.createTempFile(this.getClass().getName(), ".basename").toString();
		final List<String> c = Arrays.asList(TernaryIntervalSearchTreeTest.WORDS.clone());
		final MutableString s = new MutableString();
		for (int p = 0; p < 2; p++) {
			for (int ratio = 1; ratio < 8; ratio++) {
				final FrontCodedStringBigList fcl = new FrontCodedStringBigList(c.iterator(), ratio, true);

				fcl.dump(basename);
				final MappedFrontCodedStringBigList mfcl = MappedFrontCodedStringBigList.load(basename);
				for (int i = 0; i < fcl.size64(); i++) {
					assertEquals(Integer.toString(i), c.get(i), mfcl.get(i).toString());
					assertEquals(Integer.toString(i), c.get(i), mfcl.getString(i));
					assertEquals(Integer.toString(i), c.get(i), new String(mfcl.getBytes(i), StandardCharsets.UTF_8));
					fcl.get(i, s);
					assertEquals(Integer.toString(i), c.get(i), s.toString());
				}
			}
		}
		Collections.sort(c);

		new File(basename + MappedFrontCodedStringBigList.PROPERTIES_EXTENSION).delete();
		new File(basename + MappedFrontCodedStringBigList.BYTE_ARRAY_EXTENSION).delete();
		new File(basename + MappedFrontCodedStringBigList.POINTERS_EXTENSION).delete();
	}
}
