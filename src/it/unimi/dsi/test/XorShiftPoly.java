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

import java.math.BigInteger;

public class XorShiftPoly {

	private XorShiftPoly() {}

	/** The number of bits of state of the generator. */
	public static int bits;

	/** The period of the generator (2<sup>{@link #bits}</sup> &minus; 1). */
	public static BigInteger twoToBitsMinus1;

	/** Factors of the Fermat &ldquo;primes&rdquo; up to the eleventh (2<sup>2048</sup> + 1). */
	public static final BigInteger[] factor = {
			new BigInteger("3"),
			new BigInteger("5"),
			new BigInteger("17"),
			new BigInteger("257"),
			new BigInteger("65537"),
			new BigInteger("641"),
			new BigInteger("6700417"),
			new BigInteger("274177"),
			new BigInteger("67280421310721"),
			new BigInteger("59649589127497217"),
			new BigInteger("5704689200685129054721"),
			new BigInteger("1238926361552897"),
			new BigInteger("93461639715357977769163558199606896584051237541638188580280321"),
			new BigInteger("2424833"),
			new BigInteger("7455602825647884208337395736200454918783366342657"),
			new BigInteger("741640062627530801524787141901937474059940781097519023905821316144415759504705008092818711693940737"),
			new BigInteger("45592577"),
			new BigInteger("6487031809"),
			new BigInteger("4659775785220018543264560743076778192897"),
			new BigInteger("130439874405488189727484768796509903946608530841611892186895295776832416251471863574140227977573104895898783928842923844831149032913798729088601617946094119449010595906710130531906171018354491609619193912488538116080712299672322806217820753127014424577"),
			new BigInteger("319489"),
			new BigInteger("974849"),
			new BigInteger("167988556341760475137"),
			new BigInteger("3560841906445833920513"),
			new BigInteger("173462447179147555430258970864309778377421844723664084649347019061363579192879108857591038330408837177983810868451546421940712978306134189864280826014542758708589243873685563973118948869399158545506611147420216132557017260564139394366945793220968665108959685482705388072645828554151936401912464931182546092879815733057795573358504982279280090942872567591518912118622751714319229788100979251036035496917279912663527358783236647193154777091427745377038294584918917590325110939381322486044298573971650711059244462177542540706913047034664643603491382441723306598834177")
	};

	/** An array of cofactors. Entry 0 &le; {@code i} &lt; {@link #numCofactors} contains {@link #twoToBitsMinus1} divided by {@link #factor factor[i]}. Note that some
	 * entries can be {@code null} if {@link #bits} is less then 4096. */
	public static final BigInteger[] cofactor = new BigInteger[factor.length];

	/** The actual number of valid entries in {@link #cofactor}. */
	public static int numCofactors;

	/** Computes the power to a given exponent, given the quadratures.
	 *
	 * @param e an exponent smaller than or equal to 2<sup>{@link #bits}</sup>.
	 */
	public static void mPow(BigInteger e) {
		System.out.println("p := 1;");
		for(int i = 0; ! e.equals(BigInteger.ZERO); i++) {
			if (e.testBit(0)) System.out.println("p := *p * q[" + i + "];");
			e = e.shiftRight(1);
		}
	}

	public static void main(final String arg[]) {
		bits = Integer.parseInt(arg[0]);

		if (bits > 4096 || bits != Integer.highestOneBit(bits)) {
			System.err.println("The number of bits must be a power of two smaller than or equal to 4096");
			System.exit(1);
		}

		BigInteger result = BigInteger.ONE;
		twoToBitsMinus1 = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
		int n;
		// Initialize cofactors.
		for(n = 0; n < factor.length; n++) {
			cofactor[n] = twoToBitsMinus1.divide(factor[n]);
			result = result.multiply(factor[n]);
			if (twoToBitsMinus1.equals(result)) break;
		}

		numCofactors = n + 1;
		// Safety check (you know, those numbers are LONG).
		if (! twoToBitsMinus1.equals(result)) throw new AssertionError();

		System.out.println("Array q[" + (bits + 1) + "];");
		// Quadratures
		System.out.println("q[0] := x;");
		for(int i1 = 1; i1 <= bits; i1++) System.out.println("q[" + i1 + "] := q[" + (i1 - 1) + "] * q[" + (i1 - 1) + "];");
		System.out.println("!!('Check: ', if q[" + bits + "] = x then 1 else 0; &q fi);");
		// Exponentiation to cofactors
		for(int i = 0; i < numCofactors; i++) {
			mPow(cofactor[i]);
			System.out.println("!!('Result: ', if p = 1 then 0; &q else 1 fi);");
		}
	}
}
