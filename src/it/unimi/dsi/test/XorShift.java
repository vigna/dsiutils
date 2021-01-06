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
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XorShift {

	private XorShift() {}

	/** The number of bits of state of the generator. */
	public static final int BITS = 4096;

	/** The period of the generator (2<sup>{@link #BITS}</sup> &minus; 1). */
	public static final BigInteger twoToBitsMinus1 = BigInteger.valueOf(2).pow(BITS).subtract(BigInteger.ONE);

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
	 * entries can be {@code null} if {@link #BITS} is less then 4096. */
	public static final BigInteger[] cofactor = new BigInteger[factor.length];

	/** The actual number of valid entries in {@link #cofactor}. */
	public static final int numCofactors;

	static {
		BigInteger result = BigInteger.ONE;
		int i;
		// Initialize cofactors.
		for(i = 0; i < factor.length; i++) {
			cofactor[i] = twoToBitsMinus1.divide(factor[i]);
			result = result.multiply(factor[i]);
			if (twoToBitsMinus1.equals(result)) break;
		}

		numCofactors = i + 1;
		// Safety check (you know, those numbers are LONG).
		if (! twoToBitsMinus1.equals(result)) throw new AssertionError();
	}

	/** Creates a bit matrix in compact representation: only the first and last 64 rows, and only the
	 * first 64 columns of the remaining rows are actually represented. The remaining entries are
	 * returned by {@link #word(long[][], int, int, int)} by extending the explicit values in a Toeplitz-like fashion.
	 * Each row is represented by an array of longs, each representing 64 bits. Bit in column {@code i} can
	 * be retrieved as {@code row[i / 64] & 1L << i}.
	 *
	 * @param bits the number of bits in a row.
	 * @return a new matrix as described above.
	 */
	public static long[][] newMatrix(final int bits) {
		final long[][] result = new long[bits][];
		for(int i = 0; i < 64; i++) {
			result[i] = new long[bits / 64];
			result[i + (bits - 64)] = new long[bits / 64];
		}
		for(int i = 64; i < bits- 64; i++) result[i] = new long[1];

		return result;
	}

	/** Returns a specified word from a matrix in compact representation.
	 *
	 * @param matrix a matrix in compact form.
	 * @param r the row, starting from 0.
	 * @param cw the column index of a word, starting from 0.
	 * @param bits the number of bits in a row.
	 * @return the specified word.
	 * @see #newMatrix(int)
	 */
	public static long word(final long[][] matrix, final int r, final int cw, final int bits) {
		if (r < 64 || r >= bits - 64) return matrix[r][cw];
		if (r - cw * 64 >= 0) return matrix[r - cw * 64][0];
		return matrix[r % 64][cw - r / 64];
	}

	/** Multiplies two 64x64 bit matrices represented as arrays of longs.
	 *
	 * @param x a 64x64 bit matrix.
	 * @param y a 64x64 bit matrix.
	 * @return the product of {@code x} and {@code y}.
	 */
	public static long[] multiply(final long[] x, final long[] y) {
		final long[] u = new long[64];
		for(int i = 64; i-- != 0;) {
			final long rx = x[i];
			long ra = 0;
			for(int j = 64; j-- != 0;) if ((rx & 1L << j) != 0) ra ^= y[j];
			u[i] = ra;
		}
		return u;
	}

	/** 64x64 bit matrices of the form I + R<sup><var>a</var></sup>. */
	public static long[][] right = new long[64][64];
	/** 64x64 bit matrices of the form I + L<sup><var>a</var></sup>. */
	public static long[][] left = new long[64][64];

	static {
		// Compute 64x64 powered shifts
		for (int i = 0; i < 63; i++) right[1][i + 1] |= 1L << i;
		for (int i = 0; i < 63; i++) left[1][i] |= 1L << (i + 1);
		for (int i = 2; i < 64; i++) {
			left[i] = multiply(left[i - 1], left[1]);
			right[i] = multiply(right[i - 1], right[1]);
		}

		// Add the identity
		for (int i = 64; i-- != 0;)
			for (int j = 64; j-- != 0;) {
				right[i][j] |= 1L << j;
				left[i][j] |= 1L << j;
			}

	}

	/** Multiplies two matrices in compact representation.
	 *
	 * @param x a matrix in compact representation.
	 * @param y a matrix in compact representation.
	 * @return the product of {@code x} and {@code y} in compact representation.
	 * @see #newMatrix(int)
	 */
	public static long[][] multiply(final long[][] x, final long[][] y) {
		final long[][] z = newMatrix(BITS);

		int r;
		// First 64 rows must be computed fully.
		for(r = 0; r < 64; r++) {
			final long[] xr = x[r];
			final long[] zr = z[r];
			for(int cw = BITS / 64; cw-- != 0;) {
				long t = xr[cw];
				final int offset = cw * 64 + 63;
				for(int b = 64; b-- != 0;) {
					if ((t & 1) != 0) for(int w = BITS / 64; w-- != 0;) zr[w] ^= word(y, offset - b, w, BITS);
					t >>>= 1;
				}
			}
		}

		// Next BITS - 128 rows need just computation of the first word.
		for(; r < BITS - 64; r++) {
			final long[] zr = z[r];
			for(int cw = BITS / 64; cw-- != 0;) {
				long t = word(x, r, cw, BITS);
				final int offset = cw * 64 + 63;
				for(int b = 64; b-- != 0;) {
					if ((t & 1) != 0) zr[0] ^= y[offset - b][0];
					t >>>= 1;
				}
			}
		}

		// Last 64 rows must be computed fully.
		for(; r < BITS; r++) {
			final long[] xr = x[r];
			final long[] zr = z[r];
			for(int cw = BITS / 64; cw-- != 0;) {
				long t = xr[cw];
				final int offset = cw * 64 + 63;
				for(int b = 64; b-- != 0;) {
					if ((t & 1) != 0) for(int w = BITS / 64; w-- != 0;) zr[w] ^= word(y, offset - b, w, BITS);
					t >>>= 1;
				}
			}
		}

		return z;
	}

	/** Computes the quadratures of a matrix in compact represention.
	 *
	 * @param x a matrix in compact representation.
	 * @return an array of matrices in compact representation; the {@code i}-th
	 * entry of the matrix is <code>x<sup>2<sup>i</sup></sup></code> (0 &le; {@code i} &le; {@link #BITS}).
	 * @see #newMatrix(int)
	 */
	public static long[][][] quad(final long[][] x) {
		final long[][][] result = new long[BITS + 1][][];
		result[0] = x;
		//ProgressLogger progressLogger = new ProgressLogger(LOGGER, "quadratures");
		//progressLogger.expectedUpdates = 4095;
		//progressLogger.displayLocalSpeed = true;
		//progressLogger.start("Starting quadratures...");
		for(int i = 1; i <= BITS; i++) {
			result[i] = multiply(result[i - 1], result[i - 1]);
			//progressLogger.update();
		}
		//progressLogger.done();
		return result;
	}


	/** Returns the identity matrix in compact representation.
	 *
	 * @return a compact representation of the identity.
	 * @see #newMatrix(int)
	 */
	public static long[][] identity() {
		final long[][] m = newMatrix(BITS);
		for(int i = 64; i-- != 0;) m[i][0] = 1L << i;
		for(int i = 64; i-- != 0;) m[BITS - 64 + i][BITS / 64 - 1] = 1L << i;
		return m;
	}

	/** Checks whether a specified matrix in compact representation is the identity.
	 *
	 * @return true if {@code m} is the identity matrix.
	 * @see #newMatrix(int)
	 */
	public static boolean isIdentity(final long[][] m) {
		for(int r = 64; r-- != 0;) if (m[r][0] != 1L << r) return false;

		for(int cw = 1; cw < BITS / 64; cw++)
			for(int r = 64; r-- != 0;) if (m[r][cw] != 0) return false;

		for(int r = 64; r < BITS - 64; r++) if (m[r][0] != 0) return false;

		for(int r = 64; r-- != 0;) if (m[BITS - 64 + r][BITS / 64 - 1] != 1L << r) return false;

		for(int cw = 0; cw < BITS / 64 - 1; cw++)
			for(int r = 64; r-- != 0;) if (m[BITS - 64 + r][cw] != 0) return false;

		return true;
	}

	/** Computes the power of a matrix to a given exponent, given the quadratures of the matrix.
	 *
	 * @param q the quadratures of some matrix as returned by {@link #quad(long[][])}.
	 * @param e an exponent smaller than or equal to 2<sup>{@link #BITS}</sup>.
	 * @return the matrix whose array of quadratures is {@code q} raised to exponent {@code e}.
	 * @see #newMatrix(int)
	 */
	public static long[][] mPow(final long[][][] q, BigInteger e) {
		long[][] r = identity();
		for(int i = 0; ! e.equals(BigInteger.ZERO); i++) {
			if (e.testBit(0)) r = multiply(r, q[i]);
			e = e.shiftRight(1);
		}
		return r;
	}

	/** Checks whether a specified matrix in compact representation has full period.
	 *
	 * @param m a matrix in compact representation.
	 * @return true of {@code m} has full period (i.e., 2<sup>{@link #BITS}</sup> &minus; 1).
	 * @see #newMatrix(int)
	 */
	public static boolean isFull(final long[][] m) {
		final long[][][] q = quad(m);
		if (! Arrays.deepEquals(m, q[BITS])) {
			System.err.println("Does not give the identity");
			return false;
		}
		for(int i = 0; i < numCofactors; i++) {
			if (isIdentity(mPow(q, cofactor[i]))){
				System.err.println("Gives the identity on cofactor " + cofactor[i]);
				return false;
			}
		}

		return true;
	}

	/** Creates a matrix in compact form representing a xorshift generator as suggested by Marsaglia
	 * in <a href="http://www.jstatsoft.org/v08/i14/paper/">&ldquo;Xorshift RNGs&rdquo;</a>,
	 * <i>Journal of Statistical Software</i>, 8:1&minus;6, 2003.
	 *
	 * @param a the first shift parameter.
	 * @param b the second shift parameter.
	 * @param c the third shift parameter.
	 * @param bits the number of bits in a row.
	 * @return a matrix representing a xorshift generator with specified parameters and number of bits.
	 * @see #newMatrix(int)
	 */
	public static long[][] makeABCMatrix(final int a, final int b , final int c, final int bits) {
		final long[][] m = XorShift.newMatrix(bits);
		final long[] top = multiply(left[a], right[b]);

		for(int i = 64; i-- != 0;) m[i][bits / 64 - 1] = top[i];
		for(int i = 64; i-- != 0;) m[bits - 64 + i][bits / 64 - 1] = right[c][i];
		for(int i = 64; i-- != 0;) m[bits - 64 + i][bits / 64 - 2] = 1L << i;
		for(int i = 64; i-- != 0;) m[64 + i][0] = 1L << i;
		return m;
	}

	public static final class Compute implements Runnable {
		final int a, b, c;

		public Compute(final int a, final int b, final int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public void run() {
			final long[][] m = makeABCMatrix(a, b, c, BITS);
			System.out.println(a + " " + b + " " + c + " " + isFull(m));
		}
	}

	public static void main(final String arg[]) {
		if (arg.length > 0) throw new IllegalArgumentException("This command takes no arguments (BITS=" + BITS + ")");
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for(int a = 1; a < 64; a++) {
			for(int b = 1; b <= 64 - a; b++) {
				// Only pairs a+b<=64 such that a and b are coprime.
				if (BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue() != 1) continue;
				for(int c = 1; c < 64; c++) exec.execute(new Compute(a, b, c));
			}
		}

		exec.shutdown();
	}
}
