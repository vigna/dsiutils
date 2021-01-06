/*
 * DSI utilities
 *
 * Copyright (C) 2005-2021 Sebastiano Vigna
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
 * A callback extracting text and titles.
 *
 * <P>
 * This callbacks extracts all text in the page, and the title. The resulting text is available
 * through {@link #text}, and the title through {@link #title}.
 *
 * <P>
 * Note that {@link #text} and {@link #title} are never trimmed.
 *
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */


@Deprecated
public class TextExtractor extends DefaultCallback {

	/** The text resulting from the parsing process. */
	public final MutableString text = new MutableString();
	/** The title resulting from the parsing process. */
	public final MutableString title = new MutableString();
	/** True if we are in the middle of the title. */
	private boolean inTitle;

	/**
	 * Configure the parser to parse text.
	 */

	@Override
	public void configure(final BulletParser parser) {
		parser.parseText(true);
		// To get the title.
		parser.parseTags(true);
	}

	@Override
	public void startDocument() {
		text.length(0);
		title.length(0);
		inTitle = false;
	}

	@Override
	public boolean characters(final char[] characters, final int offset, final int length, final boolean flowBroken) {
		text.append(characters, offset, length);
		if (inTitle) title.append(characters, offset, length);
		return true;
	}

	@Override
	public boolean endElement(final Element element) {
		// No element is allowed inside a title.
		inTitle = false;
		if (element.breaksFlow) {
			if (inTitle) title.append(' ');
			text.append(' ');
		}
		return true;
	}

	@Override
	public boolean startElement(final Element element, final Map<Attribute,MutableString> attrMapUnused) {
		// No element is allowed inside a title.
		inTitle = element == Element.TITLE;
		if (element.breaksFlow) {
			if (inTitle) title.append(' ');
			text.append(' ');
		}
		return true;
	}

}
