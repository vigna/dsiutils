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

import java.io.IOException;
import java.io.OutputStream;

import it.unimi.dsi.fastutil.io.RepositionableStream;

/** Throw-it-away output stream.
 *
 * <P>This stream discards whatever is written into it. Its usefulness is in
 * previewing the length of some coding by wrapping it in an {@link
 * OutputBitStream} (it is a good idea, in this case, {@linkplain
 * OutputBitStream#OutputBitStream(java.io.OutputStream,int) to specify a 0-length buffer}).
 *
 * <P>This class is a singleton. You cannot create a null output stream,
 * but you can obtain an instance of this class using {@link #getInstance()}.
 *
 * @author Sebastiano Vigna
 * @since 0.6
 */

public class NullOutputStream extends OutputStream implements RepositionableStream {

	private final static NullOutputStream SINGLETON = new NullOutputStream();

	private NullOutputStream() {}

	@Override
	public void write(final int discarded) {}

	/** Returns the only instance of this class. */
	public static NullOutputStream getInstance() {
		return SINGLETON;
	}

	@Override
	public long position() throws IOException {
		return 0;
	}

	@Override
	public void position(final long newPosition) throws IOException {}
}
