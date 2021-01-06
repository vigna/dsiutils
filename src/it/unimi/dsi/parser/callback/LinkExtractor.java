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
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.parser.Attribute;
import it.unimi.dsi.parser.BulletParser;
import it.unimi.dsi.parser.Element;
import it.unimi.dsi.util.TextPattern;

/**
 * A callback extracting links.
 *
 * <P>This callbacks extracts links existing in the web page. The
 * links are then accessible in {@link #urls} (a set of {@link String}s). Note that
 * we guarantee that the iteration order in the set is exactly
 * the order in which links have been met (albeit copies appear
 * just once).
 */


public class LinkExtractor extends DefaultCallback {
	/** The pattern prefixing the URL in a <code>META </code> <code>HTTP-EQUIV </code> element of refresh type. */
	private static final TextPattern URLEQUAL_PATTERN = new TextPattern("URL=", TextPattern.CASE_INSENSITIVE);

	/** The URLs resulting from the parsing process. */
	public final Set<String> urls = new ObjectLinkedOpenHashSet<>();

	/** The URL contained in the first <code>META </code> <code>HTTP-EQUIV </code> element of refresh type (if any). */
	private String metaRefresh = null;

	/** The URL contained in the first <code>META </code> <code>HTTP-EQUIV </code> element of location type (if any). */
	private String metaLocation = null;

	/** The URL contained in the first <code>BASE </code> element (if any). */
	private String base = null;


	/**
	 * Configure the parser to parse elements and certain attributes.
	 *
	 * <p>
	 * The required attributes are <code>SRC </code>, <code>HREF </code>, <code>HTTP-EQUIV </code>, and <code>CONTENT
	 * </code>.
	 *
	 */

	@Override
	public void configure(final BulletParser parser) {
		parser.parseTags(true);
		parser.parseAttributes(true);
		parser.parseAttribute(Attribute.SRC);
		parser.parseAttribute(Attribute.HREF);
		parser.parseAttribute(Attribute.HTTP_EQUIV);
		parser.parseAttribute(Attribute.CONTENT);
	}

	@Override
	public void startDocument() {
		urls.clear();
		base = metaLocation = metaRefresh = null;
	}

	@Override
	public boolean startElement(final Element element, final Map<Attribute,MutableString> attrMap) {
		Object s;

		// TODO: what about IMG?

		if (element == Element.A || element == Element.AREA || element == Element.LINK) {
			s = attrMap.get(Attribute.HREF);
			if (s != null)
				urls.add(s.toString());
		}

		// IFRAME or FRAME + SRC
		if (element == Element.IFRAME || element == Element.FRAME || element == Element.EMBED) {
			s = attrMap.get(Attribute.SRC);
			if (s != null)
				urls.add(s.toString());
		}

		// BASE + HREF (change context!)
		if (element == Element.BASE && base == null) {
			s = attrMap.get(Attribute.HREF);
			if (s != null)
				base = s.toString();
		}

		// META REFRESH/LOCATION
		if (element == Element.META) {
			final MutableString equiv = attrMap.get(Attribute.HTTP_EQUIV);
			final MutableString content = attrMap.get(Attribute.CONTENT);
			if (equiv != null && content != null) {
				equiv.toLowerCase();

				// http-equiv="refresh" content="0;URL=http://foo.bar/..."
				if (equiv.equals("refresh") && (metaRefresh == null)) {

					final int pos = URLEQUAL_PATTERN.search(content);
					if (pos != -1)
						metaRefresh = content.substring(pos + URLEQUAL_PATTERN.length()).toString();
				}

				// http-equiv="location" content="http://foo.bar/..."
				if (equiv.equals("location") && (metaLocation == null))
					metaLocation = attrMap.get(Attribute.CONTENT).toString();
			}
		}

		return true;
	}

	/**
	 * Returns the URL specified by <code>META </code> <code>HTTP-EQUIV </code> elements of location type. More
	 * precisely, this method returns a non- {@code null} result iff there is at least one <code>META HTTP-EQUIV
	 * </code> element specifying a location URL (if there is more than one, we keep the first one).
	 *
	 * @return the first URL specified by a <code>META </code> <code>HTTP-EQUIV </code> elements of location type, or
	 *         {@code null}.
	 */
	public String metaLocation() {
		return metaLocation;
	}

	/**
	 * Returns the URL specified by the <code>BASE </code> element. More precisely, this method returns a non-
	 * {@code null} result iff there is at least one <code>BASE </code> element specifying a derelativisation URL
	 * (if there is more than one, we keep the first one).
	 *
	 * @return the first URL specified by a <code>BASE </code> element, or {@code null}.
	 */
	public String base() {
		return base;
	}

	/**
	 * Returns the URL specified by <code>META </code> <code>HTTP-EQUIV </code> elements of refresh type. More
	 * precisely, this method returns a non- {@code null} result iff there is at least one <code>META HTTP-EQUIV
	 * </code> element specifying a refresh URL (if there is more than one, we keep the first one).
	 *
	 * @return the first URL specified by a <code>META </code> <code>HTTP-EQUIV </code> elements of refresh type, or
	 *         {@code null}.
	 */
	public String metaRefresh() {
		return metaRefresh;
	}
}
