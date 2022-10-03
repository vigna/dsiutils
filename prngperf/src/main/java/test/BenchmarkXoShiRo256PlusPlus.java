/*
 * DSI utilities
 *
 * Copyright (C) 2022 Sebastiano Vigna
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

package test;

import org.apache.commons.math3.random.RandomGenerator;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkXoShiRo256PlusPlus {

    private it.unimi.dsi.util.XoShiRo256PlusPlusRandomGenerator random;

    @Setup(Level.Trial)
    public void doSetup() {
        random = new it.unimi.dsi.util.XoShiRo256PlusPlusRandomGenerator();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
	@OperationsPerInvocation(10)
    public void nextLong(Blackhole blackhole) {
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
        blackhole.consume(random.nextLong());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
	@OperationsPerInvocation(10)
    public void nextDouble(Blackhole blackhole) {
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
        blackhole.consume(random.nextDouble());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
	@OperationsPerInvocation(10)
    public void nextInt100000(Blackhole blackhole) {
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
        blackhole.consume(random.nextInt(100000));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
	@OperationsPerInvocation(10)
    public void nextInt2301(Blackhole blackhole) {
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
        blackhole.consume(random.nextInt((1 << 30) + 1));
    }
}
