/*
 * DSI utilities
 *
 * Copyright (C) 2006-2022 Paolo Boldi and Sebastiano Vigna
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

/** A debugging wrapper for output bit streams.
 *
 * <P>This class can be used to wrap an output bit stream. The semantics of the
 * resulting write operations is unchanged, but each operation will be logged.
 *
 * <P>To simplify the output, some operations have a simplified representation. In particular:
 *
 * <dl>
 * <dt><code>|</code>
 * <dd>{@link OutputBitStream#flush() flush()};
 * <dt><code>-&gt;</code>
 * <dd>{@link OutputBitStream#position(long) position()};
 * <dt><code>[</code>
 * <dd>creation;
 * <dt><code>]</code>
 * <dd>{@link OutputBitStream#close() close()};
 * <dt><code>{<var>x</var>}</code>
 * <dd>explicit bits;
 * <dt><code>{<var>x</var>:<var>b</var>}</code>
 * <dd>minimal binary coding of <var>x</var> with bound <var>b</var>;
 * <dt><code>{<var>M</var>:<var>x</var>}</code>
 * <dd>write <var>x</var> with coding <var>M</var>; the latter can be U (unary), g (&gamma;), z (&zeta;), d (&delta;), G (Golomb), GS (skewed Golomb);
 * when appropriate, <var>x</var> is followed by an extra integer (modulus, etc.).
 * </dl>
 *
 * @author Paolo Boldi
 * @author Sebastiano Vigna
 * @since 0.7.1
 */

public class DebugOutputBitStream extends OutputBitStream {

	private final PrintStream pw;
	private final OutputBitStream obs;

	/** Creates a new debug output bit stream wrapping a given output bit stream and logging on a given writer.
	 *
	 * @param obs the output bit stream to wrap.
	 * @param pw a print stream that will receive the logging data.
	 */
	public DebugOutputBitStream(final OutputBitStream obs, final PrintStream pw) {
		this.obs = obs;
		this.pw = pw;
		pw.print("[");
	}

	/** Creates a new debug output bit stream wrapping a given output bit stream and logging on standard error.
	 *
	 * @param obs the output bit stream to wrap.
	 */
	public DebugOutputBitStream(final OutputBitStream obs) {
		this(obs, System.err);
	}

	@Override
	public void flush() throws IOException {
		pw.print(" |");
		obs.flush();
	}

	@Override
	public void close() throws IOException {
		pw.print(" |]");
		obs.close();
	}

	@Override
	public long writtenBits() {
		return obs.writtenBits();
	}

	@Override
	public void writtenBits(final long writtenBits) {
		obs.writtenBits(writtenBits);
	}

	@Override
	public int align() throws IOException {
		pw.print(" |");
		return obs.align();
	}

	@Override
    public void position(final long position) throws IOException {
		pw.print(" ->" + position);
		obs.position(position);
	}

	static MutableString byte2Binary(int x) {
		final MutableString s = new MutableString();
		for(int i = 0 ; i < 8; i++) {
			s.append((char)('0' + (x % 2)));
			x >>= 1;
		}
		return s.reverse();
	}

	static MutableString int2Binary(long x, final int len) {
		final MutableString s = new MutableString();
		for(int i = 0 ; i < 64; i++) {
			s.append((char)('0' + (x % 2)));
			x >>= 1;
		}
		return s.length(len).reverse();
	}

	@Override
	public long write(final byte bits[], final long len) throws IOException {
		if (len > Integer.MAX_VALUE) throw new IllegalArgumentException();
		final MutableString s = new MutableString(" {");
		for (final byte bit : bits) s.append(byte2Binary(bit));
		pw.print(s.length((int)len).append("}"));
		return obs.write(bits, len);
	}

	@Override
	public int writeBit(final boolean bit) throws IOException {
		pw.print(" {" + (bit ? '1' : '0') + "}");
		return obs.writeBit(bit);
	}

	@Override
	public int writeBit(final int bit) throws IOException {
		pw.print(" {" + bit + "}");
		return obs.writeBit(bit);
	}

	@Override
	public int writeInt(final int x, final int len) throws IOException {
		pw.print(" {" + int2Binary(x, len) + "}");
		return obs.writeInt(x, len);
	}

	@Override
	public int writeLong(final long x, final int len) throws IOException {
		pw.print(" {" + int2Binary(x, len) + "}");
		return obs.writeLong(x, len);
	}

	@Override
	public int writeUnary(final int x) throws IOException {
		pw.print(" {U:" + x + "}");
		return obs.writeUnary(x);
	}

	@Override
	public long writeLongUnary(final long x) throws IOException {
		pw.print(" {U:" + x + "}");
		return obs.writeLongUnary(x);
	}

	@Override
	public int writeGamma(final int x) throws IOException {
		pw.print(" {g:" + x + "}");
		return obs.writeGamma(x);
	}

	@Override
	public int writeLongGamma(final long x) throws IOException {
		pw.print(" {g:" + x + "}");
		return obs.writeLongGamma(x);
	}

	@Override
	public int writeDelta(final int x) throws IOException {
		pw.print(" {d:" + x + "}");
		return obs.writeDelta(x);
	}

	@Override
	public int writeLongDelta(final long x) throws IOException {
		pw.print(" {d:" + x + "}");
		return obs.writeLongDelta(x);
	}

	@Override
	public int writeMinimalBinary(final int x, final int b) throws IOException {
		pw.print(" {m:" + x + "<" + b + "}");
		return obs.writeMinimalBinary(x, b);
	}

	@Override
	public int writeMinimalBinary(final int x, final int b, final int log2b) throws IOException {
		pw.print(" {m:" + x + "<" + b + "}");
		return obs.writeMinimalBinary(x, b, log2b);
	}

	@Override
	public int writeLongMinimalBinary(final long x, final long b) throws IOException {
		pw.print(" {m:" + x + "<" + b + "}");
		return obs.writeLongMinimalBinary(x, b);
	}

	@Override
	public int writeLongMinimalBinary(final long x, final long b, final int log2b) throws IOException {
		pw.print(" {m:" + x + "<" + b + "}");
		return obs.writeLongMinimalBinary(x, b, log2b);
	}

	@Override
	public int writeGolomb(final int x, final int b) throws IOException {
		pw.print(" {G:" + x + ":" + b + "}");
		return obs.writeGolomb(x, b);
	}

	@Override
	public int writeGolomb(final int x, final int b, final int log2b) throws IOException {
		pw.print(" {G:" + x + ":" + b + "}");
		return obs.writeGolomb(x, b, log2b);
	}

	@Override
	public long writeLongGolomb(final long x, final long b) throws IOException {
		pw.print(" {G:" + x + ":" + b + "}");
		return obs.writeLongGolomb(x, b);
	}

	@Override
	public long writeLongGolomb(final long x, final long b, final int log2b) throws IOException {
		pw.print(" {G:" + x + ":" + b + "}");
		return obs.writeLongGolomb(x, b, log2b);
	}

	@Override
	public int writeSkewedGolomb(final int x, final int b) throws IOException {
		pw.print(" {SG:" + x + ":" + b + "}");
		return obs.writeSkewedGolomb(x, b);
	}

	@Override
	public long writeLongSkewedGolomb(final long x, final long b) throws IOException {
		pw.print(" {SG:" + x + ":" + b + "}");
		return obs.writeLongSkewedGolomb(x, b);
	}

	@Override
	public int writeZeta(final int x, final int k) throws IOException {
		pw.print(" {z" + k + ":" + x + "}");
		return obs.writeZeta(x, k);
	}

	@Override
	public int writeLongZeta(final long x, final int k) throws IOException {
		pw.print(" {z" + k + ":" + x + "}");
		return obs.writeLongZeta(x, k);
	}

	@Override
	public int writeNibble(final int x) throws IOException {
		pw.print(" {N:" + x + "}");
		return obs.writeNibble(x);
	}

	@Override
	public int writeLongNibble(final long x) throws IOException {
		pw.print(" {N:" + x + "}");
		return obs.writeLongNibble(x);
	}

}
