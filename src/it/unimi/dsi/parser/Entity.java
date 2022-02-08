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

package it.unimi.dsi.parser;

import it.unimi.dsi.lang.MutableString;

/**
 * An SGML character entity.
 *
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */

@Deprecated
public final class Entity {

    /** The name of this entity. */
	public final CharSequence name;
	/** The Unicode character corresponding to this entity. */
	public final char character;

	/** Creates a new entity with the specified name and character.
	 *
	 * @param name the name of the new entity.
	 * @param character its character value.
	 */
	public Entity(final CharSequence name, final char character) {
		this.name = new MutableString(name);
		this.character = character;
	}

	/** Returns the name of this entity.
	 * @return the name of this entity.
	 */

	@Override
	public String toString() {
		return name.toString();
	}
}
