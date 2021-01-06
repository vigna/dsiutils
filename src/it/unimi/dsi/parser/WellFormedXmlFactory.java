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

package it.unimi.dsi.parser;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.lang.MutableString;

/** A factory for well-formed XML documents.
 *
 * <p>This factory assumes that every new name of an element type or of an
 * attribute is new valid name. For entities, instead, resolution is
 * deferred to {@link it.unimi.dsi.parser.HTMLFactory}.
 *
 * @author Sebastiano Vigna
 * @since 1.0.2
 */

public class WellFormedXmlFactory implements ParsingFactory {
	/** The load factor for all maps. */
	private static final float ONE_HALF = .5f;

	/** A (quick) map from attribute names to attributes. */
    private final Object2ObjectOpenHashMap<CharSequence,Attribute> name2Attribute = new Object2ObjectOpenHashMap<>(Hash.DEFAULT_INITIAL_SIZE, ONE_HALF);

    /** A (quick) map from element-type names to element types. */
    private final Object2ObjectOpenHashMap<CharSequence,Element> name2Element = new Object2ObjectOpenHashMap<>(Hash.DEFAULT_INITIAL_SIZE, ONE_HALF);

	public WellFormedXmlFactory() {}

	@Override
	public Element getElement(final MutableString name) {
		Element element = name2Element.get(name);
		if (element == null) {
			element = new Element(name);
			name2Element.put(element.name, element);
		}
		return element;
	}

	@Override
	public Attribute getAttribute(final MutableString name) {
		Attribute attribute = name2Attribute.get(name);
		if (attribute == null) {
			attribute = new Attribute(name);
			name2Attribute.put(attribute.name, attribute);
		}
		return attribute;
	}

	@Override
	public Entity getEntity(final MutableString name) {
		return HTMLFactory.INSTANCE.getEntity(name);
	}
}
