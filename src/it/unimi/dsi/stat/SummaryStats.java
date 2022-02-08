/*
 * DSI utilities
 *
 * Copyright (C) 2011-2022 Sebastiano Vigna
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

import it.unimi.dsi.Util;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.doubles.DoubleList;

/** A simple class digesting a stream of numbers and providing basic statistics about the stream.
 * You just have to create an instance, call {@link #add(double)} or {@link #addAll(DoubleList)} to
 * add elements.
 *
 * <p>This class is thread-safe. All methods can be invoked concurrently.
 *
 * @author Sebastiano Vigna
 */

public class SummaryStats implements Size64 {
	/** Mean */
	private double a;
	/** A statistics used to compute the variance (see <a href="http://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods">here</a>). */
	private double q;
	/** The minimum value in the stream. */
	private double min = Double.POSITIVE_INFINITY;
	/** The maximum value in the stream. */
	private double max = Double.NEGATIVE_INFINITY;
	/** The number of elements in the stream. */
	private long size;

	/** Adds a value to the stream.
	 *
	 * @param x the new value.
	 */
	public synchronized void add(final double x) {
		final double oldA = a;
		a += (x - a) / ++size;
		q += (x - a) * (x - oldA);
		if (x < min) min = x ;
		if (x > max) max = x ;
	}

	/** Adds values to the stream.
	 *
	 * @param a an array of new values.
	 */
	public void addAll(final double[] a) {
		for(final double x: a) add(x);
	}

	/** Adds values to the stream.
	 *
	 * @param l a list of new values.
	 */
	public void addAll(final DoubleList l) {
		for(final double x: l) add(x);
	}

	/** Returns the mean of the values added so far.
	 *
	 * @return the mean of the values added so far.
	 */
	public synchronized double mean() {
		return a;
	}

	/** Returns the sum of the values added so far.
	 *
	 * @return the sum of the values added so far.
	 */
	public synchronized double sum() {
		return a * size;
	}

	/** Returns the <em>sample</em> variance of the values added so far.
	 *
	 * @return the sample variance of the values added so far.
	 * @see #variance()
	 */
	public synchronized double sampleVariance() {
		return q / (size - 1);
	}

	/** Returns the variance of the values added so far.
	 *
	 * @return the variance of the values added so far.
	 * @see #sampleVariance()
	 */
	public synchronized double variance() {
		return q / size;
	}

	/** Returns the <em>sample</em> standard deviation of the values added so far.
	 *
	 * @return the sample standard deviation of the values added so far.
	 * @see #standardDeviation()
	 */
	public synchronized double sampleStandardDeviation() {
		return Math.sqrt(sampleVariance());
	}

	/** Returns the standard deviation of the values added so far.
	 *
	 * @return the standard deviation of the values added so far.
	 * @see #sampleStandardDeviation()
	 */
	public synchronized double standardDeviation() {
		return Math.sqrt(variance());
	}

	/** Returns the <em>sample</em> relative standard deviation of the values added so far.
	 *
	 * @return the sample relative standard deviation of the values added so far.
	 * @see #relativeStandardDeviation()
	 */
	public synchronized double sampleRelativeStandardDeviation() {
		return Math.sqrt(sampleVariance()) / mean();
	}

	/** Returns the relative standard deviation of the values added so far.
	 *
	 * @return the relative standard deviation of the values added so far.
	 * @see #sampleRelativeStandardDeviation()
	 */
	public synchronized double relativeStandardDeviation() {
		return Math.sqrt(variance()) / mean();
	}

	/** Returns the minimum of the values added so far.
	 *
	 * @return the minimum of the values added so far.
	 */
	public synchronized double min() {
		return min;
	}

	/** Returns the maximum of the values added so far.
	 *
	 * @return the maximum of the values added so far.
	 */
	public synchronized double max() {
		return max;
	}

	/** Returns the number of values added so far.
	 *
	 * @return the number of values added so far.
	 */
	@Override
	public synchronized long size64() {
		return size;
	}

	@Override
	@Deprecated
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "[size: " + Util.format(size64()) + " min: " + min + " max: " + max + " \u03BC: " + mean() + " \u03C3: " + sampleStandardDeviation() + " (" + Util.format(100 * sampleRelativeStandardDeviation()) + " %)]";
	}
}
