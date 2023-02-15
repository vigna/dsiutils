/*
 * DSI utilities
 *
 * Copyright (C) 2012-2023 Sebastiano Vigna
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

import java.io.EOFException;
import java.io.IOException;

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

public final class GeneratePrecomputedCodes {

	private static OutputBitStream outputBitStream;

	private GeneratePrecomputedCodes() {}

	private static int readUnary(final InputBitStream ibs) throws IOException {
		int x = 0;
		while(ibs.readBit() == 0) x++;
		return x;
	}

	public static void main(final String[] arg) throws IOException {

		final int length = Integer.parseInt(arg[0]);
		final int size = arg.length < 2 ? 1 << length : Integer.parseInt(arg[1]);

		final byte[] a = new byte[2];
		final InputBitStream inputBitStream = new InputBitStream(a);
		outputBitStream = new OutputBitStream(a);
		int v, l;

		//System.out.println("\tprivate final static int[] GAMMA = {");

		final int[] precomp = new int[size];

		int p1, p2;

		for(int i = 0; i < size; i++) {
			outputBitStream.flush();
			outputBitStream.position(0);
			outputBitStream.writeInt(i, 16);
			outputBitStream.flush();

			inputBitStream.flush();
			inputBitStream.position(16 - length);
			inputBitStream.readBits(0);
			v = l = 0;
			try {
				v = readUnary(inputBitStream);
				l = (int)inputBitStream.readBits();
				if (v > 0xFFF || l > 0x10) throw new IllegalStateException("v: " + v + " l:" + l);
			}
			catch(final EOFException e){}
			catch(final IllegalArgumentException e){} // excessive length
			precomp[i] = l << 16 | v;
			//if (i % sqrtLength == 0) System.out.print("\t\t");
			//String s = v == 0 && l == 0 ? "0" : l + " << 16 | " + v;
			//if (l > 7) s = "(short)("  + s + ")";
			//System.out.print(s + ((i + 1) % sqrtLength == 0 ? ",\n" : ", "));
		}

		BinIO.storeInts(precomp, "unary.in." + length);

		//System.out.println("\t};\n\n");

		//System.out.println("\tprivate final static int[] DELTA = {");


		for(int i = 0; i < size; i++) {
			outputBitStream.flush();
			outputBitStream.position(0);
			outputBitStream.writeInt(i, 16);
			outputBitStream.flush();

			inputBitStream.flush();
			inputBitStream.position(16 - length);
			inputBitStream.readBits(0);
			v = l = 0;
			try {
				p1 = readUnary(inputBitStream);
				v = (1 << p1 | inputBitStream.readInt(p1)) - 1;
				l = (int)inputBitStream.readBits();
				if (v > 0xFFF || l > 0x10) throw new IllegalStateException("v: " + v + " l:" + l);
			}
			catch(final EOFException e){}
			catch(final IllegalArgumentException e){} // excessive length
			precomp[i] = l << 16 | v;
			//if (i % sqrtLength == 0) System.out.print("\t\t");
			//String s = v == 0 && l == 0 ? "0" : l + " << 16 | " + v;
			//if (l > 7) s = "(short)("  + s + ")";
			//System.out.print(s + ((i + 1) % sqrtLength == 0 ? ",\n" : ", "));
		}

		BinIO.storeInts(precomp, "gamma.in." + length);

		//System.out.println("\t};\n\n");

		//System.out.println("\tprivate final static int[] DELTA = {");

		for(int i = 0; i < size; i++) {
			outputBitStream.flush();
			outputBitStream.position(0);
			outputBitStream.writeInt(i, 16);
			outputBitStream.flush();

			inputBitStream.flush();
			inputBitStream.position(16 - length);
			inputBitStream.readBits(0);
			v = l = 0;
			try {
				p1 = readUnary(inputBitStream);
				p2 = (1 << p1 | inputBitStream.readInt(p1)) - 1;
				v = (1 << p2 | inputBitStream.readInt(p2)) - 1;
				l = (int)inputBitStream.readBits();
				if (v > 0xFFF || l > 0x10) throw new IllegalStateException("v: " + v + " l:" + l);
			}
			catch(final EOFException e){}
			catch(final IllegalArgumentException e){} // excessive length

			precomp[i] = l << 16 | v;
			//if (i % sqrtLength == 0) System.out.print("\t\t");
			//String s = v == 0 && l == 0 ? "0" : l + " << 16 | " + v;
			//if (l > 7) s = "(short)("  + s + ")";
			//System.out.print(s + ((i + 1) % sqrtLength == 0 ? ",\n" : ", "));
		}

		//System.out.println("\t};\n\n");

		BinIO.storeInts(precomp, "delta.in." + length);

		for(int i = 0; i < size; i++) {
			outputBitStream.flush();
			outputBitStream.position(0);
			outputBitStream.writeInt(i, 16);
			outputBitStream.flush();

			inputBitStream.flush();
			inputBitStream.position(16 - length);
			inputBitStream.readBits(0);
			v = l = 0;
			try {
				p1 = readUnary(inputBitStream) - 1;
				v = p1 == -1 ? 0 : (1 << p1 | inputBitStream.readInt(p1));
				l = (int)inputBitStream.readBits();
				if (v > 0xFFF || l > 0x10) throw new IllegalStateException("v: " + v + " l:" + l);
			}
			catch(final EOFException e){}
			catch(final IllegalArgumentException e){} // excessive length
			precomp[i] = l << 16 | v;
			//if (i % sqrtLength == 0) System.out.print("\t\t");
			//String s = v == 0 && l == 0 ? "0" : l + " << 16 | " + v;
			//if (l > 7) s = "(short)("  + s + ")";
			//System.out.print(s + ((i + 1) % sqrtLength == 0 ? ",\n" : ", "));
		}

		BinIO.storeInts(precomp, "shiftedgamma.in." + length);

		//System.out.println("\tprivate final static int[] ZETA_3 = {");

		for(int i = 0; i < size; i++) {
			outputBitStream.flush();
			outputBitStream.position(0);
			outputBitStream.writeInt(i, 16);
			outputBitStream.flush();

			inputBitStream.flush();
			inputBitStream.position(16 - length);
			inputBitStream.readBits(0);
			v = l = 0;
			try {
				final int h = readUnary(inputBitStream);
				final int left = 1 << h * 3;
				final int m = inputBitStream.readInt(h * 3 + 3 - 1);
				if (m < left) v = m + left - 1;
				else v = (m << 1) + inputBitStream.readBit() - 1;
				l = (int)inputBitStream.readBits();
				if (v > 0xFFF || l > 0x10) throw new IllegalStateException("v: " + v + " l:" + l);
			}
			catch(final EOFException e){}
			catch(final IllegalArgumentException e){} // excessive length

			precomp[i] = l << 16 | v;
			//if (i % sqrtLength == 0) System.out.print("\t\t");
			//String s = v == 0 && l == 0 ? "0" : l + " << 16 | " + v;
			//if (l > 7) s = "(short)("  + s + ")";
			//System.out.print(s + ((i + 1) % sqrtLength == 0 ? ",\n" : ", "));
		}

		//System.out.println("\t};\n\n");
		BinIO.storeInts(precomp, "zeta3.in." + length);

	}
}
