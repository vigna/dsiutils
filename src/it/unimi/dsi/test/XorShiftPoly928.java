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

import java.math.BigInteger;

public class XorShiftPoly928 {

	private XorShiftPoly928() {}

	/** The number of bits of state of the generator. */
	public static final int BITS = 928;

	/** The period of the generator (2<sup>{@value #BITS}</sup> &minus; 1). */
	public static BigInteger twoToBitsMinus1;

	/** Factors of 2<sup>{@value #BITS}</sup> &minus; - 1. */
	public static final BigInteger[] factor = {
		new BigInteger("3"),
		new BigInteger("5"),
		new BigInteger("17"),
		new BigInteger("59"),
		new BigInteger("233"),
		new BigInteger("257"),
		new BigInteger("929"),
		new BigInteger("1103"),
		new BigInteger("2089"),
		new BigInteger("5569"),
		new BigInteger("8353"),
		new BigInteger("59393"),
		new BigInteger("65537"),
		new BigInteger("3033169"),
		new BigInteger("39594977"),
		new BigInteger("107367629"),
		new BigInteger("536903681"),
		new BigInteger("748264961"),
		new BigInteger("2245984577"),
		new BigInteger("239686663718401"),
		new BigInteger("15929619591127520827829953"),
		new BigInteger("82280195167144119832390568177"),
		new BigInteger("6033312171721035031651315652130497"),
		new BigInteger("18774318450142955120650303957350521748903233"),
		new BigInteger("15694604006012505869851221169365594050637743819041")
	};

	/** An array of cofactors. Entry 0 &le; {@code i} &lt; {@link #numCofactors} contains {@link #twoToBitsMinus1} divided by {@link #factor factor[i]}. Note that some
	 * entries can be {@code null} if {@link #BITS} is less then 4096. */
	public static final BigInteger[] cofactor = new BigInteger[factor.length];

	/** The actual number of valid entries in {@link #cofactor}. */
	public static int numCofactors;

	/** Computes the power to a given exponent, given the quadratures.
	 *
	 * @param e an exponent smaller than or equal to 2<sup>{@link #BITS}</sup>.
	 */
	public static void mPow(BigInteger e) {
		System.out.println("p := 1;");
		for(int i = 0; ! e.equals(BigInteger.ZERO); i++) {
			if (e.testBit(0)) System.out.println("p := *p * q[" + i + "];");
			e = e.shiftRight(1);
		}
	}

	public static void main(final String arg[]) {
		// Check factors
		BigInteger prod = BigInteger.ONE;
		for(final BigInteger f : factor) prod = prod.multiply(f);
		if (!prod.equals(BigInteger.valueOf(2).pow(BITS).subtract(BigInteger.ONE))) {
			System.err.println("Factors do not match");
			return;
		}

		BigInteger result = BigInteger.ONE;
		twoToBitsMinus1 = BigInteger.valueOf(2).pow(BITS).subtract(BigInteger.ONE);
		int n;
		// Initialize cofactors.
		for(n = 0; n < factor.length; n++) {
			cofactor[n] = twoToBitsMinus1.divide(factor[n]);
			result = result.multiply(factor[n]);
		}

		// Safety check (you know, those numbers are LONG).
		if (! twoToBitsMinus1.equals(result)) throw new AssertionError();

		System.out.println("Array q[" + (BITS + 1) + "];");
		// Quadratures
		System.out.println("q[0] := x;");
		for(int i1 = 1; i1 <= BITS; i1++) System.out.println("q[" + i1 + "] := q[" + (i1 - 1) + "] * q[" + (i1 - 1) + "];");
		System.out.println("!!('Check: ', if q[" + BITS + "] = x then 1 else 0; &q fi);");
		// Exponentiation to cofactors
		for (final BigInteger element : cofactor) {
			mPow(element);
			System.out.println("!!('Result: ', if p = 1 then 0; &q else 1 fi);");
		}
	}
}
