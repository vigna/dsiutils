/*
 * DSI utilities
 *
 * Copyright (C) 2016-2023 Sebastiano Vigna
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

import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

/**
 * A {@link com.martiansoftware.jsap.StringParser StringParser} that makes the user choose among
 * items of a Java {@code enum}.
 *
 * <p>Optionally, parsed strings can be normalized to upper case.
 * Thus, if the enum elements are defined in uppercase, the parser will be in practice
 * case-independent.
 *
 * <p>A typical usage example for an {@code ExampleEnum} with an item {@code A} that is going to be the default:
 * <pre>
 * new FlaggedOption("example",
 *     EnumStringParser.getParser(ExampleEnum.class, true),
 *     ExampleEnum.A.name(), JSAP.NOT_REQUIRED, 'e', "example",
 *     Arrays.toString(ExampleEnum.values()))
 * </pre>
 */

public class EnumStringParser<E extends Enum<E>> extends StringParser {
	private final Class<E> enumClass;
	private final boolean toUpper;

	/** Returns the enum item obtained by passing the argument to {@link Enum#valueOf(Class, String)}.
	 *
	 * @param s an enum item name.
	 * @return the enum item returned by {@link Enum#valueOf(Class, String)} (possibly
	 * after upper casing {@code s}).
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public E parse(final String s) throws ParseException {
		try {
			return (E) enumClass.getMethod("valueOf", String.class).invoke(null, toUpper ? s.toUpperCase() : s);
		} catch (final Exception e) {
			throw (new ParseException("Unknown value '" + s + "'.", e));
		}
	}

	private EnumStringParser(final Class<E> enumClass, final boolean toUpper) {
		this.enumClass = enumClass;
		this.toUpper = toUpper;
	}

	/**
	 * Returns an enum parser.
	 *
	 * @param enumClass an {@code enum} class whose values
	 * @param toUpper tells the parser to upper case the strings to be parsed.
	 */
	public static <E extends Enum<E>> EnumStringParser<E> getParser(final Class<E> enumClass, final boolean toUpper) throws IllegalArgumentException {
		return new EnumStringParser<>(enumClass, toUpper);
	}

	/**
	 * Returns an enum parser that does not normalize to upper case.
	 *
	 * @param enumClass an {@code enum} class whose values
	 */
	public static <E extends Enum<E>> EnumStringParser<E> getParser(final Class<E> enumClass) throws IllegalArgumentException {
		return getParser(enumClass, false);
	}
}
