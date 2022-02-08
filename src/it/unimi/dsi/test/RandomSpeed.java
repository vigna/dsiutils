/*
 * DSI utilities
 *
 * Copyright (C) 2012-2022 Sebastiano Vigna
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

import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.util.SplitMix64RandomGenerator;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandomGenerator;
import it.unimi.dsi.util.XoRoShiRo128StarStarRandomGenerator;
import it.unimi.dsi.util.XoShiRo256PlusPlusRandomGenerator;
import it.unimi.dsi.util.XoShiRo256StarStarRandomGenerator;

@SuppressWarnings({ "unused" })
public class RandomSpeed {

	private static long testNextInt(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextInt(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextInt(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextInt(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextInt(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextInt(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextInt(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextInt()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextLong(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextLong()...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong();
		pl.done(n);
		return x;
	}

	private static long testNextDouble(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("ThreadLocalRandom.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextDouble(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("SplitMixRandomGenerator.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextDouble(final SplittableRandom r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("SplittableRandom.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextDouble(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextDouble(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextDouble(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextDouble(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		double x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextDouble()...");
		for (long i = n; i-- != 0;)
			x = r.nextDouble();
		pl.done(n);
		return (long) x;
	}

	private static long testNextInt1000000(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt1000000(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt1000000(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt1000000(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt1000000(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt1000000(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt1000000(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextInt(1000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1000000);
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt229228(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextInt(2^29+2^28)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 29) + (1 << 28));
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt230(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextInt(2^30)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt(1 << 30);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt2301(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextInt(2^30+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + 1);
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextInt230229(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextInt(2^30+2^29)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextInt((1 << 30) + (1 << 29));
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong1000000000000(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextLong(1000000000000)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(1000000000000L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final ThreadLocalRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("ThreadLocalRandom.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final SplitMix64RandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplitMixRandomGenerator.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final SplittableRandom r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("SplittableRandom.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final XoShiRo256StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256StarStarRandomGenerator.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final XoShiRo256PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoShiRo256PlusPlusRandomGenerator.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final XoRoShiRo128PlusPlusRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128PlusPlusRandomGenerator.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	private static long testNextLong2621(final XoRoShiRo128StarStarRandomGenerator r, final long n, final ProgressLogger pl) {
		long x = 0;
		pl.start("XoRoShiRo128StarStarRandomGenerator.nextLong(2^62+1)...");
		for (long i = n; i-- != 0;)
			x ^= r.nextLong(4611686018427387905L);
		pl.done(n);
		return x;
	}

	public static void main(final String arg[]) {
		final ProgressLogger pl = new ProgressLogger();
		final ThreadLocalRandom threadLocal = ThreadLocalRandom.current();
		final SplittableRandom splittable = new SplittableRandom(1);
		final SplitMix64RandomGenerator splitMix = new SplitMix64RandomGenerator(1);
		final XoRoShiRo128PlusPlusRandomGenerator xoroshiro128plusplus = new XoRoShiRo128PlusPlusRandomGenerator(1);
		final XoShiRo256PlusPlusRandomGenerator xoshiro256plusplus = new XoShiRo256PlusPlusRandomGenerator(1);
		final XoRoShiRo128StarStarRandomGenerator xoroshiro128starstar = new XoRoShiRo128StarStarRandomGenerator(1);
		final XoShiRo256StarStarRandomGenerator xoshiro256starstar = new XoShiRo256StarStarRandomGenerator(1);
		final long n = Long.parseLong(arg[0]);
		long x = 0;

		for (int k = 4; k-- != 0;) {
			x ^= testNextInt(threadLocal, n, pl);
			x ^= testNextInt(splittable, n, pl);
			x ^= testNextInt(splitMix, n, pl);
			x ^= testNextInt(xoroshiro128plusplus, n, pl);
			x ^= testNextInt(xoshiro256plusplus, n, pl);
			x ^= testNextInt(xoroshiro128starstar, n, pl);
			x ^= testNextInt(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextLong(threadLocal, n, pl);
			x ^= testNextLong(splittable, n, pl);
			x ^= testNextLong(splitMix, n, pl);
			x ^= testNextLong(xoroshiro128plusplus, n, pl);
			x ^= testNextLong(xoshiro256plusplus, n, pl);
			x ^= testNextLong(xoroshiro128starstar, n, pl);
			x ^= testNextLong(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextDouble(threadLocal, n, pl);
			x ^= testNextDouble(splittable, n, pl);
			x ^= testNextDouble(splitMix, n, pl);
			x ^= testNextDouble(xoroshiro128plusplus, n, pl);
			x ^= testNextDouble(xoshiro256plusplus, n, pl);
			x ^= testNextDouble(xoroshiro128starstar, n, pl);
			x ^= testNextDouble(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextInt1000000(threadLocal, n, pl);
			x ^= testNextInt1000000(splittable, n, pl);
			x ^= testNextInt1000000(splitMix, n, pl);
			x ^= testNextInt1000000(xoroshiro128plusplus, n, pl);
			x ^= testNextInt1000000(xoshiro256plusplus, n, pl);
			x ^= testNextInt1000000(xoroshiro128starstar, n, pl);
			x ^= testNextInt1000000(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextInt229228(threadLocal, n, pl);
			x ^= testNextInt229228(splittable, n, pl);
			x ^= testNextInt229228(splitMix, n, pl);
			x ^= testNextInt229228(xoroshiro128plusplus, n, pl);
			x ^= testNextInt229228(xoshiro256plusplus, n, pl);
			x ^= testNextInt229228(xoroshiro128starstar, n, pl);
			x ^= testNextInt229228(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextInt230(threadLocal, n, pl);
			x ^= testNextInt230(splittable, n, pl);
			x ^= testNextInt230(splitMix, n, pl);
			x ^= testNextInt230(xoroshiro128plusplus, n, pl);
			x ^= testNextInt230(xoshiro256plusplus, n, pl);
			x ^= testNextInt230(xoroshiro128starstar, n, pl);
			x ^= testNextInt230(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextInt2301(threadLocal, n, pl);
			x ^= testNextInt2301(splittable, n, pl);
			x ^= testNextInt2301(splitMix, n, pl);
			x ^= testNextInt2301(xoroshiro128plusplus, n, pl);
			x ^= testNextInt2301(xoshiro256plusplus, n, pl);
			x ^= testNextInt2301(xoroshiro128starstar, n, pl);
			x ^= testNextInt2301(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextInt230229(threadLocal, n, pl);
			x ^= testNextInt230229(splittable, n, pl);
			x ^= testNextInt230229(splitMix, n, pl);
			x ^= testNextInt230229(xoroshiro128plusplus, n, pl);
			x ^= testNextInt230229(xoshiro256plusplus, n, pl);
			x ^= testNextInt230229(xoroshiro128starstar, n, pl);
			x ^= testNextInt230229(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextLong1000000000000(threadLocal, n, pl);
			x ^= testNextLong1000000000000(splittable, n, pl);
			x ^= testNextLong1000000000000(splitMix, n, pl);
			x ^= testNextLong1000000000000(xoroshiro128plusplus, n, pl);
			x ^= testNextLong1000000000000(xoshiro256plusplus, n, pl);
			x ^= testNextLong1000000000000(xoroshiro128starstar, n, pl);
			x ^= testNextLong1000000000000(xoshiro256starstar, n, pl);
			System.err.println();

			x ^= testNextLong2621(threadLocal, n, pl);
			x ^= testNextLong2621(splittable, n, pl);
			x ^= testNextLong2621(splitMix, n, pl);
			x ^= testNextLong2621(xoroshiro128plusplus, n, pl);
			x ^= testNextLong2621(xoshiro256plusplus, n, pl);
			x ^= testNextLong2621(xoroshiro128starstar, n, pl);
			x ^= testNextLong2621(xoshiro256starstar, n, pl);
			System.err.println();
		}

		if (x == 0) System.err.println('*');
	}
}
