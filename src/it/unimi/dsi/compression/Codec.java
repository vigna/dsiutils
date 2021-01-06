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

package it.unimi.dsi.compression;

/** An abstract factory corresponding to an instance of a specific compression technique.
 *
 * <P>An implementation of this interface provides coders and decoders. The
 * constructors must provide all data that is required to perform coding
 * and decoding.
 */

public interface Codec {
	/** Returns a coder for the compression technique represented by this coded.
	 *
	 * @return a coder for the compression technique represented by this codec. */
	public Coder coder();

	/** Returns a decoder for the compression technique represented by this coded.
	 *
	 * @return a decoder for the compression technique represented by this codec. */
	public Decoder decoder();

	/** Returns the number of symbols handled by this codec.
	 *
	 * @return the number of symbols handled by this codec.
	 */
	public int size();
}
