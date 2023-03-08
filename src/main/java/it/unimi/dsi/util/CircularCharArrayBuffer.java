/*
 * DSI utilities
 *
 * Copyright (C) 2006-2023 Paolo Boldi
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

package it.unimi.dsi.util;

import java.util.Arrays;

/** A circular char buffer that can be used to implement a sliding
 *  window over a text. The buffer has a maximum size, fixed at construction time. As new characters
 *  are {@linkplain #add(char[], int, int) added}, the oldest ones are discarded if necessary. At all
 *  times, the number of available characters present in the buffer is {@link #size()} (it will never be larger
 *  than the buffer size). The characters present in the buffer are provided through the {@link #toCharArray(char[], int, int)}
 *  method and alike.
 */
public class CircularCharArrayBuffer {
	private static final boolean DEBUG = false;

	/** The buffer. */
	private final char[] buffer;
	/** The number of character available in the buffer. */
	private int len;
	/** The index of the first character available in the buffer:
	 *  hence the available characters are <code>buffer[start]</code>, &hellip;,
	 *  <code>buffer[(start+len+buffer.length-1)%buffer.length]</code>
	 */
	private int start;

	/** Creates a new circular buffer.
	 *
	 * @param maxChar the buffer size.
	 */
	public CircularCharArrayBuffer(final int maxChar) {
		buffer = new char[maxChar];
		if (DEBUG) Arrays.fill(buffer, ' ');
	}

	/** Adds the characters <code>b[offset]</code>, &hellip; <code>b[offset+length-1]</code> to the
	 *  buffer (possibly just the last ones, and possibly discarding the oldest characters in the buffer).
	 *
	 * @param b the array whence characters are copied.
	 * @param offset the starting point.
	 * @param length the (maximum) number of characters to be copied.
	 */
	public void add(final char[] b, int offset, final int length) {
		if (buffer.length == 0) return;
		int toBeCopied = length;
		if (DEBUG) System.out.println("Called add(" + new String(b, offset, toBeCopied) + ")");
		if (toBeCopied > buffer.length)  {
			offset += toBeCopied - buffer.length;
			toBeCopied = buffer.length;
		}
		// Now length contains the number of bytes to be _really_ copied
		final int toEnd = Math.min(toBeCopied, buffer.length - (start + len) % buffer.length);
		// toEnd is the number of bytes that are free from the first free position to the end of the buffer
		System.arraycopy(b, offset, buffer, (start + len) % buffer.length, toEnd);
		if (toBeCopied > toEnd) {
			// length - toEnd is the number of other characters remaining
			System.arraycopy(b, offset + toEnd, buffer, 0, toBeCopied - toEnd);
		}
		if (toBeCopied > buffer.length - len) {
			start = (start + toBeCopied + len) % buffer.length;
		}
		// After filling, length is just buffer.length
		len = Math.min(buffer.length, len + toBeCopied);

	}

	/** Copies the content of the buffer to an array.
	 *
	 * @param b the buffer where the content is to be copied.
	 * @param offset the starting index where copy should start from.
	 * @param length the maximum number of characters to be copied.
	 */
	public void toCharArray(final char[] b, final int offset, final int length) {
		if (buffer.length == 0) return;
		final int t = Math.min(length, len);
		final int toEnd = Math.min(t, buffer.length - start);
		System.arraycopy(buffer, start, b, offset, toEnd);
		if (toEnd < t) System.arraycopy(buffer, 0, b, offset + toEnd, t - toEnd);
	}

	/** Copies the content of the buffer to an array.
	 *
	 * @param b the buffer where the content is to be copied.
	 */
	public void toCharArray(final char[] b) {
		toCharArray(b, 0, b.length);
	}

	/** Returns a new array containing a copy of the buffer content.
	 *
	 * @return a copy of the buffer content.
	 */
	public char[] toCharArray() {
		final char[] b = new char[size()];
		toCharArray(b);
		return b;
	}

	/** The number of characters present in the buffer.
	 *
	 * @return the number of characters present in the buffer.
	 */
	public int size() {
		return len;
	}

	/** Clears the buffer content, before reuse. */
	public void clear() {
		len = 0;
	}

}
