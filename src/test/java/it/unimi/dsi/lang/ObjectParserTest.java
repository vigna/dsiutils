/*
 * DSI utilities
 *
 * Copyright (C) 2010-2023 Sebastiano Vigna
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
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ObjectParserTest {

	@Test
	public void testObject() throws Exception {
		assertEquals(Object.class, ObjectParser.fromSpec("java.lang.Object").getClass());
		assertEquals(Object.class, ObjectParser.fromSpec("java.lang.Object()").getClass());

		assertEquals(Object.class, ObjectParser.fromSpec("Object", Object.class, new String[] { "java.lang" }).getClass());
		assertEquals(Object.class, ObjectParser.fromSpec("Object", Object.class, new String[] { "foo", "java.lang" }).getClass());
	}


	@Test
	public void testString() throws Exception {
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(foo)"));
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(\"foo\")"));
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(foo)"));
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(\"foo\")"));
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(foo)"));
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(\"f\\oo\")"));
		assertEquals("f\\oo", ObjectParser.fromSpec("java.lang.String(f\\oo)"));
		assertEquals("foo", ObjectParser.fromSpec("java.lang.String(\"foo\")"));
		assertEquals("fo\"o", ObjectParser.fromSpec("java.lang.String(\"fo\\\"o\")"));

		boolean error = false;
		try {
			ObjectParser.fromSpec("java.lang.String(\"fo\"o\")");
		}
		catch(final IllegalArgumentException thisIsWhatWeWant) {
			error = true;
		}

		assertTrue(error);

		error = false;
		try {
			ObjectParser.fromSpec("java.lang.String(fo");
		}
		catch(final IllegalArgumentException thisIsWhatWeWant) {
			error = true;
		}

		assertTrue(error);


		error = false;
		try {
			ObjectParser.fromSpec("java.lang.String()", Set.class);
		}
		catch(final ClassCastException thisIsWhatWeWant) {
			error = true;
		}

		assertTrue(error);

		assertEquals("", ObjectParser.fromSpec("java.lang.String()"));
		assertEquals("", ObjectParser.fromSpec("java.lang.String"));

		assertEquals(")foo", ObjectParser.fromSpec("java.lang.String()foo)"));
	}

	public static final class FactoryMethodAfter {
		public static FactoryMethodAfter getInstance(@SuppressWarnings("unused") final String a) {
			return new FactoryMethodAfter();
		}

		@Override
		public boolean equals(final Object o) {
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	@Test
	public void testTwoStrings() throws Exception {
		final Object context = new Object();

		assertEquals(new TwoStrings("foo", "bar"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(\"foo\", \"bar\")"));
		assertEquals(new TwoStrings("2", "2"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(\"foo\", \"bar\")", Object.class, null, new String[] { "getInstance" }));
		assertEquals(new FactoryMethodAfter(), ObjectParser.fromSpec("it.unimi.dsi.lang.ObjectParserTest$FactoryMethodAfter(unused)", Object.class, null, ObjectParser.DEFAULT_FACTORY_METHODS));
		assertEquals(new TwoStrings("foo", "bar"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(\"foo\", \"bar\")"));
		assertEquals(new TwoStrings("foo", "bar"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(foo, bar)"));
		assertEquals(new TwoStrings("foo", "bar"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(foo , bar)"));
		assertEquals(new TwoStrings("", ""), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(,)"));
		assertEquals(new TwoStrings("", ""), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(,)"));
		assertEquals(new TwoStrings("", ""), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(,\"\")"));

		assertEquals(new TwoStrings("foo", "foo"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(foo)", Object.class, null, new String[] { "getInstance" }));
		assertEquals(new TwoStrings("3", "3"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(foo,bar,boo)", Object.class, null, new String[] { "getInstance" }));
		assertEquals(new TwoStrings("foo", "3"), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(foo,bar,boo)", Object.class));

		assertEquals(new TwoStrings(context, "foo", "bar"), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(\"foo\", \"bar\")"));
		assertEquals(new TwoStrings(context, "foo", "bar"), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(foo, bar)"));
		assertEquals(new TwoStrings(context, "foo", "bar"), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(foo , bar)"));
		assertEquals(new TwoStrings(context, "", ""), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(,)"));
		assertEquals(new TwoStrings(context, "", ""), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(,)"));
		assertEquals(new TwoStrings(context, "", ""), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(,\"\")"));

		assertEquals(new TwoStrings(context, "foo", "foo"), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(foo)", Object.class, null, new String[] { "getInstance" }));
		assertEquals(new TwoStrings(context, "3", "3"), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(foo,bar,boo)", Object.class, null, new String[] { "getInstance" }));
		assertEquals(new TwoStrings(context, "foo", "3"), ObjectParser.fromSpec(context, "it.unimi.dsi.lang.TwoStrings(foo,bar,boo)", Object.class));
	}

	@Test
	public void testTwoStringsNull() throws Exception {
		assertEquals(new TwoStrings("foo", (String)null), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(\"foo\",null)"));
		assertEquals(new TwoStrings((String)null, (String)null), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(null,null)"));
		assertEquals(new TwoStrings("null", (String)null), ObjectParser.fromSpec("it.unimi.dsi.lang.TwoStrings(\"null\",null)"));
	}
}
