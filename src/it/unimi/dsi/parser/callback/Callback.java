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
 * A callback for the {@linkplain it.unimi.dsi.parser.BulletParser bullet parser}.
 *
 * <P>
 * This interface is very loosely inspired to the SAX2 interface. However, it strives to be simple,
 * and to be StringFree&trade;.
 *
 * <P>
 * By contract, all implementations of this interface are bound to be <em>reusable</em>: by calling
 * {@link #startDocument()}, a callback can be used again. It <strong>must</strong> be safe to call
 * {@link #startDocument()} any number of times.
 *
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */

@Deprecated
public interface Callback {

	/** A singleton empty callback array. */
	Callback[] EMPTY_CALLBACK_ARRAY = new Callback[0];

	/** Configure the parser for usage with this callback.
	 *
	 * <P>When a callback is registered with a parser, it needs to set up
	 * the parser so that all data required by the callback is actually parsed.
	 * The configuration <strong>must</strong> be a monotone process&mdash;you
	 * can only <em>set</em> properties and <em>add</em> attribute types to
	 * be parsed.
	 */
	void configure(BulletParser parser);

	/** Receive notification of the beginning of the document.
	 *
	 * <P>The callback must use this method to reset its internal state so
	 * that it can be resued. It <strong>must</strong> be safe to invoke this method
	 * several times.
	 */
	void startDocument();

	/** Receive notification of the start of an element.
	 *
	 * <P>For simple elements, this is the only notification that the
	 * callback will ever receive.
	 *
	 * @param element the element whose opening tag was found.
	 * @param attrMap a map from {@link it.unimi.dsi.parser.Attribute}s to {@link MutableString}s.
	 * @return true to keep the parser parsing, false to stop it.
	 */
	boolean startElement(Element element, Map<Attribute,it.unimi.dsi.lang.MutableString> attrMap);

	/** Receive notification of the end of an element.
	 *
	 * <strong>Warning</strong>: unless specific decorators are used, in
	 * general a callback will just receive notifications for elements
	 * whose closing tag appears <em>explicitly</em> in the document.
	 *
	 * <P>This method will never be called for element without closing tags,
	 * even if such a tag is found.
	 *
	 * @param element the element whose closing tag was found.
	 * @return true to keep the parser parsing, false to stop it.
	 */
	boolean endElement(Element element);

	/** Receive notification of character data inside an element.
	 *
	 * <p>You must not write into <code>text</code>, as it could be passed
	 * around to many callbacks.
	 *
	 * <P><code>flowBroken</code> will be true iff
	 * the flow was broken before <code>text</code>. This feature makes it possible
	 * to extract quickly the text in a document without looking at the elements.
	 *
	 * @param text an array containing the character data.
	 * @param offset the start position in the array.
     * @param length the number of characters to read from the array.
	 * @param flowBroken whether the flow is broken at the start of <code>text</code>.
	 * @return true to keep the parser parsing, false to stop it.
	 */
	boolean characters(char[] text, int offset, int length, boolean flowBroken);

	/** Receive notification of the content of a CDATA section.
	 *
	 * <p>CDATA sections in an HTML document are the result of meeting
	 * a <code>STYLE</code> or <code>SCRIPT</code> element. In that case, the element
	 * will be passed as first argument.
	 *
	 * <p>You must not write into <code>text</code>, as it could be passed
	 * around to many callbacks.
	 *
	 * @param element the element enclosing the CDATA section, or {@code null} if the
	 * CDATA section was created with explicit markup.
	 * @param text an array containing the character data.
	 * @param offset the start position in the array.
     * @param length the number of characters to read from the array.
	 * @return true to keep the parser parsing, false to stop it.
	 */
	boolean cdata(Element element, char[] text, int offset, int length);

	/**    Receive notification of the end of the document. */

	void endDocument();
}
