/**
 * Miscellaneous utility classes
 *
 * <h2>Pseudorandom number generators</h2>
 *
 * <p><strong>Warning</strong>: before release 2.6.3, the {@code split()} method of all generators
 * would not alter the state of the caller, and it would return instances initialized in the same
 * way if called multiple times. This was a major mistake in the implementation and it has been fixed,
 * but as a consequence the output of the caller after a call to {@code split()} is
 * now different, and the result of {@code split()} is initialized in a different way.
 *
 * <p>We provide a number of fast, high-quality PRNGs with different features. You can get detailed
 * information about the generators at our <a href="http://prng.di.unimi.it/">PRNG page</a>, together
 * with a reasoned guide to the choice of the generator that's right for you.
 *
 * <p>A table summarizing timings is provided below. The timings were measured on an
 * Intel&reg; Core&trade; i7-8700B CPU @3.20GHz using
 * <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a> microbenchmarks. The JMH timings were decreased by 1ns, as
 * using the low-level {@code perfasm} profiler the JMH overhead was estimated at &approx;1ns per call.
 *
 * <TABLE BORDER=1>
 * <caption>Timings in nanoseconds for a few generators</caption>
 * <TR><TH>
 * <TH>{@link java.util.Random Random}
 * <TH>{@link java.util.concurrent.ThreadLocalRandom ThreadLocalRandom}
 * <TH>{@link java.util.SplittableRandom SplittableRandom}
 * <TH>{@link it.unimi.dsi.util.SplitMix64RandomGenerator <span style='font-variant: small-caps'>SplitMix64</span>}
 * <TH>{@link it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom <code>xoroshiro128++</code>}
 * <TH>{@link it.unimi.dsi.util.XoRoShiRo128StarStarRandom <code>xoroshiro128**</code>}
 * <TH>{@link it.unimi.dsi.util.XoRoShiRo128PlusRandom <code>xoroshiro128+</code>}
 * <TH>{@link it.unimi.dsi.util.XoShiRo256PlusPlusRandom <code>xoshiro256++</code>}
 * <TH>{@link it.unimi.dsi.util.XoShiRo256StarStarRandom <code>xoshiro256**</code>}
 * <TH>{@link it.unimi.dsi.util.XoShiRo256PlusRandom <code>xoshiro256+</code>}
 * <TH>{@link it.unimi.dsi.util.XorShift1024StarPhiRandom <code>xorshift1024*&phi;</code>}
 *
 * <TR><TH STYLE='text-align: left'>nextLong()                                 <TD STYLE='text-align: right'>14.419<TD STYLE='text-align: right'>1.252<TD STYLE='text-align: right'>1.283<TD STYLE='text-align: right'>1.241<TD STYLE='text-align: right'>1.428<TD STYLE='text-align: right'>1.574<TD STYLE='text-align: right'>1.295<TD STYLE='text-align: right'>1.738<TD STYLE='text-align: right'>1.884<TD STYLE='text-align: right'>1.653<TD STYLE='text-align: right'>1.901
 * <TR><TH STYLE='text-align: left'>nextInt(100000)                            <TD STYLE='text-align: right'>6.715<TD STYLE='text-align: right'>2.045<TD STYLE='text-align: right'>2.499<TD STYLE='text-align: right'>2.543<TD STYLE='text-align: right'>2.336<TD STYLE='text-align: right'>2.594<TD STYLE='text-align: right'>1.202<TD STYLE='text-align: right'>2.607<TD STYLE='text-align: right'>2.954<TD STYLE='text-align: right'>2.367<TD STYLE='text-align: right'>3.119
 * <TR><TH STYLE='text-align: left'>nextDouble()                               <TD STYLE='text-align: right'>14.458<TD STYLE='text-align: right'>1.876<TD STYLE='text-align: right'>2.161<TD STYLE='text-align: right'>2.176<TD STYLE='text-align: right'>1.918<TD STYLE='text-align: right'>2.219<TD STYLE='text-align: right'>1.853<TD STYLE='text-align: right'>2.304<TD STYLE='text-align: right'>2.503<TD STYLE='text-align: right'>2.112<TD STYLE='text-align: right'>2.755
 * </TABLE>
 *
 * <p>Note that generators that are <a href="http://prng.di.unimi.it/">extremely fast in C</a>, such as <code>xoshiro256+</code>, do not perform particularly well in Java, most likely
 * because of the cost of accessing variables, which rises as the size of the state space grows. Indeed,
 * smaller-state generators are faster. Moreover, generators based on the <code>++</code>
 * scrambler are slightly faster than those based on the <code>**</code> scrambler, contrarily to what happens in C.
 *
 * <p>For each generator, we provide a version that extends {@link java.util.Random}, overriding (as usual) the {@link java.util.Random#next(int) next(int)} method. Nonetheless,
 * since the generators are all inherently 64-bit also {@link java.util.Random#nextInt() nextInt()}, {@link java.util.Random#nextFloat() nextFloat()},
 * {@link java.util.Random#nextLong() nextLong()}, {@link java.util.Random#nextDouble() nextDouble()}, {@link java.util.Random#nextBoolean() nextBoolean()}
 * and {@link java.util.Random#nextBytes(byte[]) nextBytes(byte[])} have been overridden for speed (preserving, of course, {@link java.util.Random}'s semantics).
 *
 * <p>If you do not need an instance of {@link java.util.Random}, or if you need a {@link org.apache.commons.math3.random.RandomGenerator} to use
 * with <a href="http://commons.apache.org/math/">Commons Math</a>, there is for each generator a corresponding {@link org.apache.commons.math3.random.RandomGenerator RandomGenerator}
 * implementation, which indeed we suggest to use in general if you do not need a generator implementing {@link java.util.Random}.
 */

package it.unimi.dsi.util;
