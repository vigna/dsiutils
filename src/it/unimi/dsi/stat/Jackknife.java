/*
 * DSI utilities
 *
 * Copyright (C) 2011-2023 Sebastiano Vigna
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

package it.unimi.dsi.stat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/** Applies the jackknife to generic statistics.
 *
 * <p>This class applies the <em>jackknife</em> method (see, e.g., &ldquo;A leisurely look at the bootstrap, the jackknife, and cross-validation&rdquo;, by Bradley Efron and Gail Gong,
 * <i>The American Statistician</i>, 37(1):36&minus;48, 1983) to reduce the bias in the estimation of a nonlinear
 * statistic of interest (linear statistics, such as the mean, pass through the jackknife without change).
 * The {@linkplain Statistic statistic} must take a sample (an array of {@linkplain BigDecimal big decimals}) and return
 * corresponding values (again as an array of {@linkplain BigDecimal big decimals}). In case high-precision
 * arithmetic is not required, an instance of {@link AbstractStatistic} just takes an array of doubles and returns an
 * array of doubles, handling all necessary type conversions.
 *
 * <p>The static method {@link #compute(List, Statistic, MathContext)} takes a list
 * of samples (arrays of doubles of the same length) and returns an instance of this class containing
 * {@linkplain #estimate estimates} and {@linkplain #standardError standard errors} for every
 * value computed by the statistic (estimates of the statistic are available both as
 * {@linkplain #bigEstimate an array of big decimals} and as {@linkplain #estimate an array of doubles},
 * whereas {@linkplain #standardError estimates of standard errors} are provided in double format, only).
 *
 * <p>All computations are performed internally using {@link BigDecimal} and a provided {@link MathContext}.
 * The method {@link #compute(List, Statistic)} uses {@linkplain #DEFAULT_MATH_CONTEXT 100 decimal digits}.
 *
 * <p>The {@linkplain #IDENTITY identical} statistic can be used to compute the (pointwise) empirical mean
 * and standard error of a sample.
 *
 * @author Sebastiano Vigna
 */

public class Jackknife {
	/** The default {@link MathContext} used by {@link #compute(List, Statistic)}: 100 digits and {@link RoundingMode#HALF_EVEN}. */
	public static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(100, RoundingMode.HALF_EVEN);

	/** A vector of high-precision estimates for a statistic of interest. */
	public final BigDecimal[] bigEstimate;
	/** A vector of estimates for a statistic of interest (obtained by invoking {@link BigDecimal#doubleValue()} on {@link #bigEstimate}). */
	public final double[] estimate;
	/** A vector of (estimates of the) standard error parallel to {@link #bigEstimate}/{@link #estimate}. */
	public final double[] standardError;

	public static double[] bigDecimalArray2DoubleArray(final BigDecimal[] input) {
		final double[] output = new double[input.length];
		for(int i = input.length; i-- != 0;) output[i] = input[i].doubleValue();
		return output;
	}

	public static BigDecimal[] doubleArray2BigDecimalArray(final double[] input) {
		final BigDecimal[] output = new BigDecimal[input.length];
		for(int i = input.length; i-- != 0;) output[i] = BigDecimal.valueOf(input[i]);
		return output;
	}

	private Jackknife(final BigDecimal[] estimate, final double[] standardError) {
		this.standardError = standardError;
		this.estimate = bigDecimalArray2DoubleArray(estimate);
		this.bigEstimate = estimate;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for(int i = estimate.length; i++ != 0;) s.append(estimate[i]).append('\t').append(standardError[i]).append(System.getProperty("\n"));
		return s.toString();
	}

	/** A statistic to be estimated using the jackknife on a set of samples. */
	public static interface Statistic {
		/** Computes the statistic.
		 *
		 * <p>Note that the {@link BigDecimal} instances passed to this method are guaranteed to
		 * have a {@linkplain BigDecimal#scale() scale} set by the caller. If you have to perform divisions,
		 * please use the supplied {@link MathContext}.
		 *
		 * @param sample the samples over which the statistic must be computed.
		 * @param mc the mathematical context to be used when dividing {@linkplain BigDecimal big decimals}.
		 * @return the resulting statistic.
		 */
		public BigDecimal[] compute(BigDecimal[] sample, MathContext mc);
	}

	/** A statistic that returns the sample. Useful to compute the average and the empirical standard error. */
	public static Jackknife.Statistic IDENTITY = (sample, unused) -> sample;

	/** An abstract statistic with a {@linkplain #compute(double[]) template method} that
	 * accepts an array of doubles, returns an array of doubles and handles the data conversions that
	 * are necessary to call {@link Statistic#compute(BigDecimal[], MathContext)}. Useful if you do not
	 * want to fiddle with {@link BigDecimal}. */
	public abstract static class AbstractStatistic implements Statistic {
		public abstract double[] compute(final double[] sample);

		@Override
		public BigDecimal[] compute(final BigDecimal[] bigSample, final MathContext unused) {
			return doubleArray2BigDecimalArray(compute(bigDecimalArray2DoubleArray(bigSample)));
		}
	}


	/** Applies the jackknife to a statistic of interest using a list of samples using {@link #DEFAULT_MATH_CONTEXT} as context.
	 *
	 * @param samples a list of samples (arrays of doubles of the same length).
	 * @param f a statistic of interest.
	 * @return an instance of this class containing estimates of <code>f</code> and corresponding standard errors
	 * obtained by the jackknife on the given set of samples.
	 */
	public static Jackknife compute(final List<double[]> samples, final Statistic f) {
		return compute(samples, f, DEFAULT_MATH_CONTEXT);
	}

	/** Applies the jackknife to a statistic of interest using a list of samples.
	 *
	 * @param samples a list of samples (arrays of doubles of the same length).
	 * @param f a statistic of interest.
	 * @param mc the mathematical context to be used when dividing {@linkplain BigDecimal big decimals}.
	 * @return an instance of this class containing estimates of <code>f</code> and corresponding standard errors
	 * obtained by the jackknife on the given set of samples.
	 */
	public static Jackknife compute(final List<double[]> samples, final Statistic f, final MathContext mc) {
		final int n = samples.size();
		final BigDecimal big1OverN =  BigDecimal.ONE.divide(BigDecimal.valueOf(n), mc);
		final BigDecimal big1OverNMinus1 =  BigDecimal.ONE.divide(BigDecimal.valueOf(n - 1), mc);
		final BigDecimal bigNMinus1OverN =  BigDecimal.valueOf(n - 1).divide(BigDecimal.valueOf(n), mc);
		final int l = samples.get(0).length;
		final BigDecimal[] sum = new BigDecimal[l];
		for(int p = l; p-- != 0;) sum[p] = BigDecimal.ZERO;
		// Gather all samples
		for(final double[] sample: samples) {
			if (sample.length != l) throw new IllegalArgumentException("Samples have different sizes: " + sample.length + " != " + l);
			for(int p = l; p-- != 0;) sum[p] = sum[p].add(BigDecimal.valueOf(sample[p]), mc);
		}

		final BigDecimal[] averagedSample = new BigDecimal[l];
		for(int p = l; p-- != 0;) averagedSample[p] = sum[p].multiply(big1OverN, mc);
		final BigDecimal[] naiveStatistics = f.compute(averagedSample, mc);
		final int k = naiveStatistics.length;

		final BigDecimal[][] leaveOneOutStatistic = new BigDecimal[n][];
		// Compute leave-one-out statistics
		for(int s = 0; s < n; s++) {
			final BigDecimal[] leaveOneOutSample = new BigDecimal[l];
			// Leave-one-out sample
			final double[] t = samples.get(s);
			for(int p = l; p-- != 0;) leaveOneOutSample[p] = sum[p].subtract(BigDecimal.valueOf(t[p]), mc).multiply(big1OverNMinus1, mc);
			// Leave-one-out statistic
			leaveOneOutStatistic[s] = f.compute(leaveOneOutSample, mc);
			if (leaveOneOutStatistic[s].length != k) throw new IllegalArgumentException("Statistics have different sizes: " + leaveOneOutStatistic[s].length + " != " + k);
		}

		final BigDecimal[] estimate = new BigDecimal[k];
		final double[] standardError = new double[k];
		for(int i = k; i-- != 0;) {
			BigDecimal e = BigDecimal.valueOf(n).multiply(naiveStatistics[i], mc);
			for(int s = n; s-- != 0;) e = e.subtract(leaveOneOutStatistic[s][i].multiply(bigNMinus1OverN, mc), mc);
			estimate[i] = e;

			BigDecimal variance = BigDecimal.ZERO;
			for(int s = n; s-- != 0;) {
				final BigDecimal t = naiveStatistics[i].subtract(leaveOneOutStatistic[s][i], mc);
				variance = variance.add(t.multiply(t, mc), mc);
			}
			standardError[i] = Math.sqrt(variance.multiply(bigNMinus1OverN, mc).doubleValue());
		}

		return new Jackknife(estimate, standardError);
	}
}
