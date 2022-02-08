/*
 * DSI utilities
 *
 * Copyright (C) 2010-2022 Sebastiano Vigna
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

package it.unimi.dsi.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.martiansoftware.jsap.ParseException;

public class EnumParserTest {
	public enum TestEnum {
		A,
		b,
		C
	}

	@Test
	public void test() throws Exception {
		final EnumStringParser<TestEnum> enumStringParser = EnumStringParser.getParser(TestEnum.class);
		assertEquals(TestEnum.A, enumStringParser.parse("A"));
		assertEquals(TestEnum.b, enumStringParser.parse("b"));
		assertEquals(TestEnum.C, enumStringParser.parse("C"));
	}

	@Test(expected=ParseException.class)
	public void testNoMatchBecauseOfCase() throws Exception {
		final EnumStringParser<TestEnum> enumStringParser = EnumStringParser.getParser(TestEnum.class);
		enumStringParser.parse("a");
	}

	@Test(expected=ParseException.class)
	public void testNoMatchBecauseWrong() throws Exception {
		final EnumStringParser<TestEnum> enumStringParser = EnumStringParser.getParser(TestEnum.class);
		enumStringParser.parse("D");
	}

	@Test
	public void testNorm() throws Exception {
		final EnumStringParser<TestEnum> enumStringParser = EnumStringParser.getParser(TestEnum.class, true);
		assertEquals(TestEnum.A, enumStringParser.parse("a"));
		assertEquals(TestEnum.C, enumStringParser.parse("c"));
	}
}
