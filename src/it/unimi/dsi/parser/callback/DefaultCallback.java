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
 * A default, do-nothing-at-all callback.
 *
 * <P>
 * Callbacks can inherit from this class and forget about methods they are not interested in.
 *
 * <P>
 * This class has a protected constructor. If you need an instance of this class, use
 * {@link #getInstance()}.
 *
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */
@Deprecated
public class DefaultCallback implements Callback {
	private static final DefaultCallback SINGLETON = new DefaultCallback();

	protected DefaultCallback() {}

	/**
	 * Returns the singleton instance of the default callback.
	 *
	 * @return the singleton instance of the default callback.
	 */
	public static DefaultCallback getInstance() {
		return SINGLETON;
	}

	@Override
	public void configure(final BulletParser parserUnused) {}

	@Override
	public void startDocument() {}

	@Override
	public boolean startElement(final Element elementUnused, final Map<Attribute,MutableString> attrMapUnused) {
		return true;
	}

	@Override
	public boolean endElement(final Element elementUnused) {
		return true;
	}

	@Override
	public boolean characters(final char[] textUnused, final int offsetUnused, final int lengthUnused, final boolean flowBrokenUnused) {
		return true;
	}

	@Override
	public boolean cdata(final Element elementUnused, final char[] textUnused, final int offsetUnused, final int lengthUnused) {
		return true;
	}

	@Override
	public void endDocument() {}
}
