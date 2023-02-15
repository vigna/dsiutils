/*
 * DSI utilities
 *
 * Copyright (C) 2005-2023 Sebastiano Vigna
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

import it.unimi.dsi.lang.MutableString;

/**
 * A set of coherent methods to turn element-type, attribute and entity names to unique interned
 * instances.
 *
 * <p>
 * The {@link it.unimi.dsi.parser.BulletParser} needs a way to turn a name (for an element type,
 * attribute, or entity) into a corresponding object of type {@link it.unimi.dsi.parser.Element},
 * {@link it.unimi.dsi.parser.Attribute} or {@link it.unimi.dsi.parser.Entity}, respectively. The
 * returned element must be an interned, unique representation.
 *
 * <P>
 * For instance, the {@linkplain it.unimi.dsi.parser.HTMLFactory standard factory for HTML} parsing
 * has ready-made interned versions of all names in the (X)HTML specification, and returns them upon
 * request, but other policies are possible. For instance, instances of {@link WellFormedXmlFactory}
 * intern every seen name, without reference to a data type (except for entities, in which case the
 * HTML set is used).
 *
 * <P>
 * The idea of factoring out the creation of interned counterparts of SGML/XML syntactical objects
 * is due to Fabien Campagne.
 *
 * @author Sebastiano Vigna
 * @since 1.0.2
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */

@Deprecated
public interface ParsingFactory {

	/** Returns the {@link it.unimi.dsi.parser.Element} associated
	 * to a name.
	 * @param name the name of an element type.
	 * @return the corresponding interned {@link Element} object.
	 */
	public Element getElement(final MutableString name);

	/** Returns the {@link it.unimi.dsi.parser.Attribute} associated
	 * to a name.
	 * @param name the name of an attribute.
	 * @return the corresponding interned {@link Attribute} object.
	 */
	public Attribute getAttribute(final MutableString name);

	/** Returns the {@link it.unimi.dsi.parser.Entity} associated
	 * to a name.
	 * @param name the name of an entity.
	 * @return the corresponding interned {@link Entity} object.
	 */
	public Entity getEntity(final MutableString name);
}
