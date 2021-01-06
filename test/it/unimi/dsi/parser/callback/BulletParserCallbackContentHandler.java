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

package it.unimi.dsi.parser.callback;

import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.parser.Attribute;
import it.unimi.dsi.parser.BulletParser;
import it.unimi.dsi.parser.Element;

/** An adapter from callbacks of the bullet parser to standard
 * SAX content handler. Can be used to run, eg., <code>tagsoup</code>
 * and the bullet parser against a page and check that the
 * actual callback invocations are the same.
 */

@Deprecated
public class BulletParserCallbackContentHandler extends DefaultHandler {
	/** The delegated callback. */
	private final Callback callback;

	/** The corresponding parser. */
	private final BulletParser parser;

	/** The element enclosing the current CDATA section, or {@code null}
	 * if we're not in a CDATA section. */
	private Element inCdata;

	/** The set of tags enclosing CDATA sections. */
	private final ReferenceSet<Element> cdataElements = new ReferenceOpenHashSet<>(new Element[] { Element.SCRIPT, Element.STYLE });

	/** The map used to fake an attribute map. */
	private final Reference2ObjectOpenHashMap<Attribute,MutableString> attrMap = new Reference2ObjectOpenHashMap<>();

	public BulletParserCallbackContentHandler(final BulletParser parser, final Callback callback) {
		this.parser = parser;
		this.callback = callback;
	}

	@Override
	public void endDocument() {
		callback.endDocument();
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) {
	    final Element element = parser.factory.getElement(new MutableString(localName));
		if (cdataElements.contains(element) && element == inCdata) inCdata = null;
		callback.endElement(element);
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) {
		if (inCdata != null) callback.cdata(inCdata, ch, start, length);
		else callback.characters(ch, start, length, false);
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) {
		callback.characters(ch, start, length, false);
	}

	@Override
	public void startDocument() {
		callback.startDocument();
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
		attrMap.clear();
		String value;
		Attribute attribute;

		for (final Iterator<Attribute> i = parser.parsedAttributes.iterator(); i.hasNext();) {
			attribute = i.next();
			value = attributes.getValue(attribute.toString());
			if (value != null)
				attrMap.put(attribute, new MutableString(value));
		}

		final Element element = parser.factory.getElement(new MutableString(localName));
		if (cdataElements.contains(element)) inCdata = element;
		callback.startElement(element, attrMap);
	}
}
