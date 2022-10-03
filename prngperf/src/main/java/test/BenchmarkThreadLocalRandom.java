package test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkThreadLocalRandom {

    private ThreadLocalRandom random;

    @Setup(Level.Trial)
    public void doSetup() {
        random = ThreadLocalRandom.current();
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
