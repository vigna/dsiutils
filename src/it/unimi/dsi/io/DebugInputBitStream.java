/*
 * DSI utilities
 *
 * Copyright (C) 2006-2022 Sebastiano Vigna
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
import java.io.PrintStream;

import it.unimi.dsi.lang.MutableString;


/** A debugging wrapper for input bit streams.
 *
 * <P>This class can be used to wrap an input bit stream. The semantics of the
 * resulting read operations is unchanged, but each operation will be logged. The
 * conventions are the same as those of {@link it.unimi.dsi.io.DebugOutputBitStream},
 * with the following additions:
 *
 * <dl>
 * <dt><code>!</code>
 * <dd>{@link InputBitStream#reset() reset()};
 * <dt><code>+&gt;</code>
 * <dd>{@link InputBitStream#skip(long) skip()}.
 * </dl>
 *
 * @author Sebastiano Vigna
 * @since 1.1
 */

public class DebugInputBitStream extends InputBitStream {

	private final PrintStream pw;
	private final InputBitStream ibs;

	/** Creates a new debug input bit stream wrapping a given input bit stream and logging on a given writer.
	 *
	 * @param ibs the input bit stream to wrap.
	 * @param pw a print stream that will receive the logging data.
	 */
	public DebugInputBitStream(final InputBitStream ibs, final PrintStream pw) {
		this.ibs = ibs;
		this.pw = pw;
		pw.print("[");
	}

	/** Creates a new debug input bit stream wrapping a given input bit stream and logging on standard error.
	 *
	 * @param ibs the input bit stream to wrap.
	 */
	public DebugInputBitStream(final InputBitStream ibs) {
		this(ibs, System.err);
	}


	@Override
	public void align() {
		pw.print(" |");
		ibs.align();
	}

	@Override
	public long available() throws IOException {
		return ibs.available();
	}

	@Override
	public void close() throws IOException {
		pw.print(" |]");
		ibs.close();
	}

	@Override
	public void flush() {
		pw.print(" |");
		ibs.flush();
	}

	@Override
	public void position(final long position) throws IOException {
		pw.print(" ->" + position);
		ibs.position(position);
	}

	@Override
	public void read(final byte[] bits, final int len) throws IOException {
		ibs.read(bits, len);
		final MutableString s = new MutableString(" {");
		for (final byte bit : bits) s.append(DebugOutputBitStream.byte2Binary(bit));
		pw.print(s.length(len).append("}"));
	}

	@Override
	public int readBit() throws IOException {
		final int bit = ibs.readBit();
		pw.print(" {" + bit + "}");
		return bit;
	}

	@Override
	public long readBits() {
		return ibs.readBits();
	}

	@Override
	public void readBits(final long readBits) {
		ibs.readBits(readBits);
	}

	@Override
	public int readDelta() throws IOException {
		final int x = ibs.readDelta();
		pw.print(" {d:" + x + "}");
		return x;
	}

	@Override
	public int readGamma() throws IOException {
		final int x = ibs.readGamma();
		pw.print(" {g:" + x + "}");
		return x;
	}

	@Override
	public int readGolomb(final int b, final int log2b) throws IOException {
		final int x = ibs.readGolomb(b, log2b);
		pw.print(" {G:" + x + ":" + b + "}");
		return x;
	}

	@Override
	public int readGolomb(final int b) throws IOException {
		final int x = ibs.readGolomb(b);
		pw.print(" {G:" + x + ":" + b + "}");
		return x;
	}

	@Override
	public int readInt(final int len) throws IOException {
		final int x = ibs.readInt(len);
		pw.print(" {" + DebugOutputBitStream.int2Binary(x, len) + "}");
		return x;
	}

	@Override
	public long readLong(final int len) throws IOException {
		final long x = ibs.readLong(len);
		pw.print(" {" + DebugOutputBitStream.int2Binary(x, len) + "}");
		return x;
	}

	@Override
	public long readLongDelta() throws IOException {
		final long x = ibs.readLongDelta();
		pw.print(" {d:" + x + "}");
		return x;
	}

	@Override
	public long readLongGamma() throws IOException {
		final long x = ibs.readLongGamma();
		pw.print(" {g:" + x + "}");
		return x;
	}

	@Override
	public long readLongGolomb(final long b, final int log2b) throws IOException {
		final long x = ibs.readLongGolomb(b, log2b);
		pw.print(" {G:" + x + ":" + b + "}");
		return x;
	}

	@Override
	public long readLongGolomb(final long b) throws IOException {
		final long x = ibs.readLongGolomb(b);
		pw.print(" {G:" + x + ":" + b + "}");
		return x;
	}

	@Override
	public long readLongMinimalBinary(final long b, final int log2b) throws IOException {
		final long x = ibs.readLongMinimalBinary(b, log2b);
		pw.print(" {m:" + x + "<" + b + "}");
		return x;
	}

	@Override
	public long readLongMinimalBinary(final long b) throws IOException {
		final long x = ibs.readLongMinimalBinary(b);
		pw.print(" {m:" + x + "<" + b + "}");
		return x;
	}

	@Override
	public long readLongNibble() throws IOException {
		final long x = ibs.readLongNibble();
		pw.print(" {N:" + x + "}");
		return x;
	}

	@Override
	public long readLongSkewedGolomb(final long b) throws IOException {
		final long x = ibs.readLongSkewedGolomb(b);
		pw.print(" {SG:" + x + ":" + b + "}");
		return x;
	}

	@Override
	public long readLongUnary() throws IOException {
		final long x = ibs.readLongUnary();
		pw.print(" {U:" + x + "}");
		return x;
	}

	@Override
	public long readLongZeta(final int k) throws IOException {
		final long x = ibs.readLongZeta(k);
		pw.print(" {z" + k + ":" + x + "}");
		return x;
	}

	@Override
	public int readMinimalBinary(final int b, final int log2b) throws IOException {
		final int x = ibs.readMinimalBinary(b, log2b);
		pw.print(" {m:" + x + "<" + b + "}");
		return x;

	}

	@Override
	public int readMinimalBinary(final int b) throws IOException {
		final int x = ibs.readMinimalBinary(b);
		pw.print(" {m:" + x + "<" + b + "}");
		return x;
	}

	@Override
	public int readNibble() throws IOException {
		final int x = ibs.readNibble();
		pw.print(" {N:" + x + "}");
		return x;
	}

	@Override
	public int readSkewedGolomb(final int b) throws IOException {
		final int x = ibs.readSkewedGolomb(b);
		pw.print(" {SG:" + x + ":" + b + "}");
		return x;
	}

	@Override
	public int readUnary() throws IOException {
		final int x = ibs.readUnary();
		pw.print(" {U:" + x + "}");
		return x;
	}

	@Override
	public int readZeta(final int k) throws IOException {
		final int x = ibs.readZeta(k);
		pw.print(" {z" + k + ":" + x + "}");
		return x;
	}

	@Override
	public void reset() throws IOException {
		pw.print(" {!}");
		ibs.reset();
	}

	@Override
	@Deprecated
	public int skip(final int n) {
		pw.print(" {+>" + n + "}");
		return ibs.skip(n);
	}

	@Override
	public long skip(final long n) throws IOException {
		pw.print(" {+>" + n + "}");
		return ibs.skip(n);
	}

}
