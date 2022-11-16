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
 * A table summarizing timings is provided below. The timings were measured on a 12th Gen Intel&reg;
 * Core&trade; i7-12700KF @3.60GHz using
 * <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a>
 * <a href="https://github.com/vigna/dsiutils/tree/master/prngperf">microbenchmarks</a> on the
 * <a href="https://www.graalvm.org/">GraalVM</a> virtual machine for Java 19 (release 22.3.0).
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
 * <TD STYLE='text-align: right'>14.522
 * <TD STYLE='text-align: right'>0.699
 * <TD STYLE='text-align: right'>0.536
 * <TD STYLE='text-align: right'>0.518
 * <TD STYLE='text-align: right'>0.817
 * <TD STYLE='text-align: right'>0.937
 * <TD STYLE='text-align: right'>0.732
 * <TD STYLE='text-align: right'>0.911
 * <TD STYLE='text-align: right'>0.852
 * <TD STYLE='text-align: right'>0.760
 * <TD STYLE='text-align: right'>0.776
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextDouble()</code>
 * <TD STYLE='text-align: right'>14.513
 * <TD STYLE='text-align: right'>1.813
 * <TD STYLE='text-align: right'>1.609
 * <TD STYLE='text-align: right'>1.608
 * <TD STYLE='text-align: right'>1.607
 * <TD STYLE='text-align: right'>1.609
 * <TD STYLE='text-align: right'>1.608
 * <TD STYLE='text-align: right'>1.607
 * <TD STYLE='text-align: right'>1.610
 * <TD STYLE='text-align: right'>1.608
 * <TD STYLE='text-align: right'>1.609
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextInt(100000)</code>
 * <TD STYLE='text-align: right'>7.652
 * <TD STYLE='text-align: right'>1.329
 * <TD STYLE='text-align: right'>1.368
 * <TD STYLE='text-align: right'>1.194
 * <TD STYLE='text-align: right'>1.322
 * <TD STYLE='text-align: right'>1.428
 * <TD STYLE='text-align: right'>1.216
 * <TD STYLE='text-align: right'>1.751
 * <TD STYLE='text-align: right'>1.763
 * <TD STYLE='text-align: right'>1.990
 * <TD STYLE='text-align: right'>1.286
 *
 * <TR>
 * <TH STYLE='text-align: left'><code>nextInt(2<sup>30</sup>+1)</code>
 * <TD STYLE='text-align: right'>16.291
 * <TD STYLE='text-align: right'>10.722
 * <TD STYLE='text-align: right'>9.639
 * <TD STYLE='text-align: right'>1.260
 * <TD STYLE='text-align: right'>1.348
 * <TD STYLE='text-align: right'>1.471
 * <TD STYLE='text-align: right'>1.240
 * <TD STYLE='text-align: right'>1.851
 * <TD STYLE='text-align: right'>1.862
 * <TD STYLE='text-align: right'>1.890
 * <TD STYLE='text-align: right'>1.351
 *
 * </TABLE>
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
