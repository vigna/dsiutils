/*
 * DSI utilities
 *
 * Copyright (C) 2003-2021 Sebastiano Vigna
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

package it.unimi.dsi.io;

import java.io.Reader;
import java.io.Serializable;


/** End-of-stream-only reader.
 *
 * <P>This reader will always return end-of-file on any read attempt.
 *
 * <P>This class is a singleton. You cannot create a null reader,
 * but you can obtain an instance of this class using {@link #getInstance()}.
 *
 * @author Sebastiano Vigna
 * @since 0.9.2
 */

public class NullReader extends Reader implements Serializable {
	private static final long serialVersionUID = 1L;

	private final static NullReader INSTANCE = new NullReader();

	private NullReader() {}

	/** Returns the only instance of this class.
	 *
	 * @return the only instance of this class.
	 */
	public static NullReader getInstance() {
		return INSTANCE;
	}

	@Override
	public void close() {}

	@Override
	public int read(final char[] cbuf, final int off, final int len) {
		return -1;
	}
}
