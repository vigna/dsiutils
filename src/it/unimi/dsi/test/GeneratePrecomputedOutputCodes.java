/*
 * DSI utilities
 *
 * Copyright (C) 2012-2021 Sebastiano Vigna
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

package it.unimi.dsi.test;

import java.io.IOException;
import java.util.Arrays;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

public final class GeneratePrecomputedOutputCodes {

	private GeneratePrecomputedOutputCodes() {}

	public static int writeUnary(final int x, final OutputBitStream obs) throws IOException {
		for(int i = 0; i < x; i++) obs.writeBit(0);
		obs.writeBit(1);
		return x + 1;
	}

	public static int writeGamma(int x, final OutputBitStream obs) throws IOException {
		final int msb = Fast.mostSignificantBit(++x);
		final int l = writeUnary(msb, obs);
		return l + (msb != 0 ? obs.writeInt(x, msb) : 0);
	}

	public static int writeDelta(int x, final OutputBitStream obs) throws IOException {
		final int msb = Fast.mostSignificantBit(++x);
		final int l = writeGamma(msb, obs);
		return l + (msb != 0 ? obs.writeInt(x, msb) : 0);
	}

	public static int writeZeta(int x, final int k, final OutputBitStream obs) throws IOException {
		final int msb = Fast.mostSignificantBit(++x);
		final int h = msb / k;
		final int l = writeUnary(h, obs);
		final int left = 1 << h * k;
		return l + (x - left < left
			? obs.writeInt(x - left, h * k + k - 1)
			: obs.writeInt(x, h * k + k));
	}

	public static int writeShiftedGamma(final int x, final OutputBitStream obs) throws IOException {
		final int msb = Fast.mostSignificantBit(x);
		final int l = writeUnary(msb + 1, obs);
		return l + (msb != -1 ? obs.writeInt(x, msb) : 0);
	}


	public static void main(final String[] arg) throws IOException {

		final int length = Integer.parseInt(arg[0]);
		final int size = 1 << length;

		final byte[] a = new byte[4];
		@SuppressWarnings("resource")
		final InputBitStream inputBitStream = new InputBitStream(a);
		final OutputBitStream outputBitStream= new OutputBitStream(a);
		int l, v;

		final int[] precomp = new int[size];

		for(int i = 0; i < size; i++) {
			Arrays.fill(a, (byte)0);
			outputBitStream.flush();
			outputBitStream.position(0);
			l = writeGamma(i, outputBitStream);
			outputBitStream.flush();
			inputBitStream.flush();
			inputBitStream.position(0);
			v = inputBitStream.readInt(l);
			if (l > 26) throw new IllegalArgumentException();
			if (Fast.mostSignificantBit(l) > 5) throw new IllegalArgumentException();
			precomp[i] = l << 26 | v;
		}

		BinIO.storeInts(precomp, "gamma.out." + length);

		for(int i = 0; i < size; i++) {
			Arrays.fill(a, (byte)0);
			outputBitStream.flush();
			outputBitStream.position(0);
			l = writeDelta(i, outputBitStream);
			outputBitStream.flush();
			inputBitStream.flush();
			inputBitStream.position(0);
			v = inputBitStream.readInt(l);
			if (l > 26) throw new IllegalArgumentException();
			if (Fast.mostSignificantBit(l) > 5) throw new IllegalArgumentException();
			precomp[i] = l << 26 | v;
		}

		BinIO.storeInts(precomp, "delta.out." + length);

		for(int i = 0; i < size; i++) {
			Arrays.fill(a, (byte)0);
			outputBitStream.flush();
			outputBitStream.position(0);
			l = writeZeta(i, 3, outputBitStream);
			outputBitStream.flush();
			inputBitStream.flush();
			inputBitStream.position(0);
			v = inputBitStream.readInt(l);
			if (l > 26) throw new IllegalArgumentException();
			if (Fast.mostSignificantBit(l) > 5) throw new IllegalArgumentException();
			precomp[i] = l << 26 | v;
		}

		BinIO.storeInts(precomp, "zeta3.out." + length);

		for(int i = 0; i < size; i++) {
			Arrays.fill(a, (byte)0);
			outputBitStream.flush();
			outputBitStream.position(0);
			l = writeShiftedGamma(i, outputBitStream);
			outputBitStream.flush();
			inputBitStream.flush();
			inputBitStream.position(0);
			v = inputBitStream.readInt(l);
			if (l > 26) throw new IllegalArgumentException();
			if (Fast.mostSignificantBit(l) > 5) throw new IllegalArgumentException();
			precomp[i] = l << 26 | v;
		}

		BinIO.storeInts(precomp, "shiftedgamma.out." + length);

	}
}
