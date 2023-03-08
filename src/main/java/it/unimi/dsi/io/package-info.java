/** I/O classes
 *
 * <p>Classes in this package fulfill needs that are not satisfied by the
 * standard I/O classes available.
 *
 * <h2>Reading text</h2>
 * 
 * <p>We provide replacement classes such as {@link
 * it.unimi.dsi.io.FastBufferedReader} and classes exposing the lines of
 * a file as an {@linkplain
 * it.unimi.dsi.io.FileLinesMutableStringIterable Iterable}. The general
 * {@link it.unimi.dsi.io.WordReader} interface is used by <a href="http://mg4j.di.unimi.it/">MG4J</a>
 * to provide customizable word segmentation.
 *
 * <h2>Bit-level I/O</h2>
 * 
 * <P>The standard Java API lacks bit-level I/O classes: to this purpose, we
 * provide {@link it.unimi.dsi.io.InputBitStream} and {@link
 * it.unimi.dsi.io.OutputBitStream}, which can wrap any standard Java
 * corresponding stream and make it work at the bit level; moreover, they
 * provide support for several useful formats (such as unary, binary, minimal
 * binary, &gamma;, &delta; and Golomb encoding).

 * <P>Bit input and output streams offer also efficient buffering and a way to
 * reposition the bit stream in case the underlying byte stream is a
 * file-based stream or a {@link it.unimi.dsi.fastutil.io.RepositionableStream}.
 *
 * <h2>Conventions</h2>
 * 
 * <p><strong>All coding methods work on natural numbers</strong>. The
 * encoding of zero is very natural for some techniques, and much less natural
 * for others. To keep methods rationally organized, all methods are able to
 * encode any natural number. If, for instance, you want to write positive
 * numbers in unary encoding and you do not want to waste a bit, you have to
 * decrement them first (i.e., instead of <var>p</var> you must encode
 * <var>p</var>&nbsp;&minus;&nbsp;1).
 */

package it.unimi.dsi.io;
