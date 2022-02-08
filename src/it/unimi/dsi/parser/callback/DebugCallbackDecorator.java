/*
 * DSI utilities
 *
 * Copyright (C) 2005-2022 Sebastiano Vigna
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

package it.unimi.dsi.parser.callback;

import java.util.Map;

import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.parser.Attribute;
import it.unimi.dsi.parser.BulletParser;
import it.unimi.dsi.parser.Element;

/**
 * A decorator that prints on standard error all calls to the underlying callback.
 *
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */
@Deprecated
public class DebugCallbackDecorator implements Callback {

	/** The underlying callback. */
	private final Callback callback;

	public DebugCallbackDecorator(final Callback callback) {
		this.callback = callback;
	}

	@Override
	public boolean cdata(final Element element, final char[] text, final int offset, final int length) {
		System.err.println("cdata(" + new String(text, offset, length) + ")");
		return callback.cdata(element, text, offset, length);
	}


	@Override
	public boolean characters(final char[] text, final int offset, final int length, final boolean flowBroken) {
		System.err.println("characters(" + new String(text, offset, length) + ", " + flowBroken + ")");
		return callback.characters(text, offset, length, flowBroken);
	}


	@Override
	public void configure(final BulletParser parser) {
		System.err.println("configure()");
		callback.configure(parser);
	}


	@Override
	public void endDocument() {
		System.err.println("endDocument()");
		callback.endDocument();
	}

	@Override
	public boolean endElement(final Element element) {
		System.err.println("endElement(" + element + ")");
		return callback.endElement(element);
	}

	@Override
	public boolean equals(final Object obj) {
		return callback.equals(obj);
	}

	@Override
	public int hashCode() {
		return callback.hashCode();
	}

	@Override
	public void startDocument() {
		System.err.println("startDocument()");
		callback.startDocument();
	}

	@Override
	public boolean startElement(final Element element, final Map<Attribute,MutableString> attrMap) {
		System.err.println("endElement(" + element + ", " + attrMap + ")");
		return callback.startElement(element, attrMap);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "(" + callback.toString() + ")";
	}
}
