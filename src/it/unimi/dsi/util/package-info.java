/**
 * Miscellaneous utility classes
 *
 * <h2>Pseudorandom number generators</h2>
 *
 * <p>
 * <strong>Warning</strong>: before release 2.6.3, the {@code split()} method of all generators
 * would not alter the state of the caller, and it would return instances initialized in the same
 * way if called multiple times. This was a major mistake in the implementation and it has been
 * fixed, but as a consequence the output of the caller after a call to {@code split()} is now
 * different, and the result of {@code split()} is initialized in a different way.
 *
 * <p>
 * We provide a number of fast, high-quality PRNGs with different features. You can get detailed
 * information about the generators at our <a href="http://prng.di.unimi.it/">PRNG page</a>,
 * together with a reasoned guide to the choice of the generator that's right for you.
 *
 * <p>
 * Note that starting with Java 17 <code>xoroshiro128++</code> and <code>xoshiro256++</code> are
 * part of the package <a href=
 * "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/random/package-summary.html"><code>java.util.random</code></a>.
 *
 * <p>
 * A table summarizing timings is provided below. The timings were measured on an Intel&reg;
 * Core&trade; i7-7700 CPU @3.60GHz using
 * <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a>
 * <a href="https://github.com/vigna/dsiutils/tree/master/prngperf">microbenchmarks</a> on the
 * <a href="https://www.graalvm.org/">GraalVM</a> virtual machine for Java 17 (release 22.2.0).
 *
 * <TABLE BORDER=1>
 * <caption>Timings in nanoseconds for a few generators</caption>
 * <TR>
 * <TH>
 * <TH>{@link java.util.Random Random}
 * <TH>{@link java.util.concurrent.ThreadLocalRandom ThreadLocalRandom}
 * <TH>{@link java.util.SplittableRandom SplittableRandom}
 * <TH>{@link it.unimi.dsi.util.SplitMix64RandomGenerator
 * <span style='font-variant: small-caps'>SplitMix64</span>}
 * <TH><code>{@link it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom xoroshiro128++}</code>
 * <TH><code>{@link it.unimi.dsi.util.XoRoShiRo128StarStarRandom xoroshiro128**}</code>
 * <TH><code>{@link it.unimi.dsi.util.XoRoShiRo128PlusRandom xoroshiro128+}</code>
 * <TH><code>{@link it.unimi.dsi.util.XoShiRo256PlusPlusRandom xoshiro256++}</code>
 * <TH><code>{@link it.unimi.dsi.util.XoShiRo256StarStarRandom xoshiro256**}</code>
 * <TH><code>{@link it.unimi.dsi.util.XoShiRo256PlusRandom xoshiro256+}</code>
 * <TH><code>{@link it.unimi.dsi.util.XorShift1024StarPhiRandom xorshift1024*&phi;}</code>
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextLong()</code>
 * <TD STYLE='text-align: right'>17.024
 * <TD STYLE='text-align: right'>1.059
 * <TD STYLE='text-align: right'>0.846
 * <TD STYLE='text-align: right'>0.981
 * <TD STYLE='text-align: right'>1.054
 * <TD STYLE='text-align: right'>1.117
 * <TD STYLE='text-align: right'>0.911
 * <TD STYLE='text-align: right'>1.617
 * <TD STYLE='text-align: right'>1.646
 * <TD STYLE='text-align: right'>1.601
 * <TD STYLE='text-align: right'>1.329
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextDouble()</code>
 * <TD STYLE='text-align: right'>16.611
 * <TD STYLE='text-align: right'>2.707
 * <TD STYLE='text-align: right'>1.938
 * <TD STYLE='text-align: right'>1.946
 * <TD STYLE='text-align: right'>1.945
 * <TD STYLE='text-align: right'>1.944
 * <TD STYLE='text-align: right'>1.936
 * <TD STYLE='text-align: right'>1.935
 * <TD STYLE='text-align: right'>1.940
 * <TD STYLE='text-align: right'>1.935
 * <TD STYLE='text-align: right'>1.932
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextInt(100000)</code>
 * <TD STYLE='text-align: right'>8.692
 * <TD STYLE='text-align: right'>2.164
 * <TD STYLE='text-align: right'>1.906
 * <TD STYLE='text-align: right'>2.344
 * <TD STYLE='text-align: right'>2.441
 * <TD STYLE='text-align: right'>2.451
 * <TD STYLE='text-align: right'>2.138
 * <TD STYLE='text-align: right'>3.521
 * <TD STYLE='text-align: right'>3.635
 * <TD STYLE='text-align: right'>3.433
 * <TD STYLE='text-align: right'>2.468
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextInt(2<sup>30</sup>+1)</code>
 * <TD STYLE='text-align: right'>19.370
 * <TD STYLE='text-align: right'>12.870
 * <TD STYLE='text-align: right'>11.828
 * <TD STYLE='text-align: right'>2.519
 * <TD STYLE='text-align: right'>2.369
 * <TD STYLE='text-align: right'>2.605
 * <TD STYLE='text-align: right'>2.247
 * <TD STYLE='text-align: right'>3.873
 * <TD STYLE='text-align: right'>3.760
 * <TD STYLE='text-align: right'>3.550
 * <TD STYLE='text-align: right'>2.594
 *
 * </TABLE>
 *
 * <p>
 * Note that generators that are <a href="http://prng.di.unimi.it/">extremely fast in C</a>, such as
 * <code>xoshiro256+</code>, do not perform particularly well in Java, most likely because of the
 * cost of accessing variables, which rises as the size of the state space grows. Indeed,
 * smaller-state generators are faster. Moreover, generators based on the <code>++</code> scrambler
 * are slightly faster than those based on the <code>**</code> scrambler, contrarily to what happens
 * in C.
 *
 * <p>
 * For each generator, we provide a version that extends {@link java.util.Random}, overriding (as
 * usual) the {@link java.util.Random#next(int) next(int)} method. Nonetheless, since the generators
 * are all inherently 64-bit also {@link java.util.Random#nextInt() nextInt()},
 * {@link java.util.Random#nextFloat() nextFloat()}, {@link java.util.Random#nextLong() nextLong()},
 * {@link java.util.Random#nextDouble() nextDouble()}, {@link java.util.Random#nextBoolean()
 * nextBoolean()} and {@link java.util.Random#nextBytes(byte[]) nextBytes(byte[])} have been
 * overridden for speed (preserving, of course, {@link java.util.Random}'s semantics).
 *
 * <p>
 * If you do not need an instance of {@link java.util.Random}, or if you need a
 * {@link org.apache.commons.math3.random.RandomGenerator} to use with
 * <a href="http://commons.apache.org/math/">Commons Math</a>, there is for each generator a
 * corresponding {@link org.apache.commons.math3.random.RandomGenerator RandomGenerator}
 * implementation, which indeed we suggest to use in general if you do not need a generator
 * implementing {@link java.util.Random}.
 */

package it.unimi.dsi.util;
