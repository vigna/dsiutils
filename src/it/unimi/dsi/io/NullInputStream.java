/*
 * DSI utilities
 *
 * Copyright (C) 2003-2023 Sebastiano Vigna
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

import java.io.IOException;
import java.io.Serializable;

import it.unimi.dsi.fastutil.io.MeasurableInputStream;
import it.unimi.dsi.fastutil.io.RepositionableStream;

/** End-of-stream-only input stream.
 *
 * <P>This stream has length 0, and will always return end-of-file on any read attempt.
 *
 * <P>This class is a singleton. You cannot create a null input stream,
 * but you can obtain an instance of this class using {@link #getInstance()}.
 *
 * @author Sebastiano Vigna
 * @since 0.8
 */

public class NullInputStream extends MeasurableInputStream implements RepositionableStream, Serializable {
	private static final long serialVersionUID = 1L;
	private final static NullInputStream INSTANCE = new NullInputStream();

	private NullInputStream() {}

	@Override
	public int read() { return -1; }

	/** Returns the only instance of this class.
	 *
	 * @return  the only instance of this class.
	 */
	public static NullInputStream getInstance() {
		return INSTANCE;
	}

	private Object readResolve() {
		return INSTANCE;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public long position() {
		return 0;
	}

	@Override
	public void position(final long position) throws IOException {
		// TODO: we should specify the semantics out of bounds
		return;
	}
}
