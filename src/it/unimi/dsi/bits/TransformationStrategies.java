/*
 * DSI utilities
 *
 * Copyright (C) 2007-2023 Sebastiano Vigna
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

package it.unimi.dsi.bits;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.lang.MutableString;

/** A class providing static methods and objects that do useful things with transformation strategies.
 *
 * <p>This class provides several {@linkplain TransformationStrategy transformation strategies} that turn
 * strings or other objects into bit vectors. The transformations might optionally be:
 * <ul>
 * <li><strong>Lexicographical</strong>: for objects based on bytes or characters, such as strings
 * and byte arrays, this means that the first bit of the bit vector is the <em>most</em> significant
 * bit of the first byte or character, and so on. In other word, the lexicographical order between
 * bit vectors reflects the lexicographical byte-by-byte, char-by-char, etc. order. Thiss property
 * is necessary for some kind of static structure that depends on it, but it has some computational
 * cost, as after compacting byte or chars into a long we need to revert the bit order of each piece.
 * <li><strong>Prefix-free</strong>: no two bit vector returned by the transformation on two
 * different objects will be comparable in prefix order. Again, this might require to use more
 * linear (e.g., {@link #prefixFree()}) or constant (e.g., {@link #prefixFreeIso()}) additional space.
 * </ul>
 *
 * <p>As a general rule, transformations without additional naming are lexicographical.
 * Transformation that generate prefix-free bit vectors are marked as such.
 * Plain transformations that do not provide any guarantee are called <em>raw</em>. They should be
 * used only when performance is the main issue and the two properties above are not relevant.
 *
 * @see TransformationStrategy
 */


@SuppressWarnings("fallthrough")
public class TransformationStrategies {

	private final static TransformationStrategy<BitVector> IDENTITY = new TransformationStrategy<BitVector>() {
		private static final long serialVersionUID = 1L;

		@Override
		public BitVector toBitVector(final BitVector v) {
			return v;
		}

		@Override
		public long length(final BitVector v) {
			return v.length();
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<BitVector> copy() {
			return this;
		}

		public Object readResolve() {
			return IDENTITY;
		}
	};

	/** Reverses the bit order in the bytes of the provided word.
	 *
	 * @param word a word.
	 * @return {@code word}, with the bit order inside each byte reversed.
	 */
	private static final long reverseBytes(long word) {
		word = (word & 0x5555555555555555L) << 1 | (word >>> 1) & 0x5555555555555555L;
		word = (word & 0x3333333333333333L) << 2 | (word >>> 2) & 0x3333333333333333L;
		return (word & 0x0f0f0f0f0f0f0f0fL) << 4 | (word >>> 4) & 0x0f0f0f0f0f0f0f0fL;
	}

	/** Reverses the bit order in the characters of the provided word.
	 *
	 * @param word a word.
	 * @return {@code word}, with the bit order inside each 16-bit character reversed.
	 */
	private static final long reverseChars(long word) {
		word = (word & 0x5555555555555555L) << 1 | (word >>> 1) & 0x5555555555555555L;
		word = (word & 0x3333333333333333L) << 2 | (word >>> 2) & 0x3333333333333333L;
		word = (word & 0x0f0f0f0f0f0f0f0fL) << 4 | (word >>> 4) & 0x0f0f0f0f0f0f0f0fL;
		return (word & 0x00ff00ff00ff00ffL) << 8 | (word >>> 8) & 0x00ff00ff00ff00ffL;
	}

	/** A trivial transformation for data already in {@link BitVector} form. */
	@SuppressWarnings("unchecked")
	public static <T extends BitVector> TransformationStrategy<T> identity() {
		return (TransformationStrategy<T>)IDENTITY;
	}

	private static final TransformationStrategy<CharSequence> RAW_UTF32 = new RawUtf32TransformationStrategy();

	/**A trivial raw transformation from strings to bit vectors
	 * that turns the UTF-16 representation into a UTF-32 representation,
	 * decodes surrogate pairs and concatenates the bits of the UTF-32 representation.
	 *
	 * <p><strong>Warning</strong>: this transformation is not lexicographic.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> rawUtf32() {
		return (TransformationStrategy<T>)RAW_UTF32;
	}

	private static class RawUtf32TransformationStrategy implements TransformationStrategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public long length(final CharSequence cs) {
			return length(cs.toString());
		}

		private long length(final String s) {
			return s.codePointCount(0, s.length()) * (long)Integer.SIZE;
		}

		@Override
		public BitVector toBitVector(final CharSequence cs) {
			final String s = cs.toString();
			final int length = s.length();
			final LongArrayBitVector bitVector = LongArrayBitVector.getInstance(length(s));
			for (int i = 0, cp; i < length; i += Character.charCount(cp))
				bitVector.append(cp = s.codePointAt(i), Integer.SIZE);
			return bitVector;
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<CharSequence> copy() {
			return this;
		}

		private Object readResolve() {
			return RAW_UTF32;
		}
	}


	private static final TransformationStrategy<CharSequence> UTF32 = new Utf32TransformationStrategy(false);

	/** A transformation from strings to bit vectors that turns the UTF-16 representation into a UTF-32 representation,
	 *  decodes surrogate pairs and concatenates the bits of the UTF-32 representation.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> utf32() {
		return (TransformationStrategy<T>)UTF32;
	}

	private static final TransformationStrategy<CharSequence> PREFIX_FREE_UTF32 = new Utf32TransformationStrategy(true);

	/** A transformation from strings to bit vectors that turns the UTF-16 representation into a UTF-32 representation,
	 * decodes surrogate pairs, concatenates the bits of the UTF-32 representation and completes
	 * the representation with an NUL to guarantee lexicographical ordering and prefix-freeness.
	 *
	 * <p>Note that strings provided to this strategy <strong>must not</strong> contain NULs.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> prefixFreeUtf32() {
		return (TransformationStrategy<T>)PREFIX_FREE_UTF32;
	}

	private static class Utf32TransformationStrategy implements TransformationStrategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;
		/** Whether we should guarantee prefix-freeness by adding 0 to the end of each string. */
		private final boolean prefixFree;

		/** Creates a UTF32 transformation strategy. The strategy will map a string to its UTF32 bit sequence by decoding surrogate pairs.
		 *
		 * @param prefixFree if true, the resulting set of binary words will be made prefix free by adding a NUL at the end of the string.
		 */
		protected Utf32TransformationStrategy(final boolean prefixFree) {
			this.prefixFree = prefixFree;
		}

		@Override
		public long length(final CharSequence cs) {
			return length(cs.toString());
		}

		private long length(final String s) {
			return (s.codePointCount(0, s.length()) + (prefixFree ? 1 : 0)) * (long)Integer.SIZE;
		}

		@Override
		public BitVector toBitVector(final CharSequence cs) {
			final String s = cs.toString();
			final int length = s.length();
			final LongArrayBitVector bitVector = LongArrayBitVector.getInstance(length(s));
			for (int i = 0, cp; i < length; i += Character.charCount(cp))
				bitVector.append(Integer.reverse(cp = s.codePointAt(i)) & -1L >>> 32, Integer.SIZE);
			if (prefixFree) bitVector.append(0, Integer.SIZE);
			return bitVector;
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<CharSequence> copy() {
			return this;
		}

		private Object readResolve() {
			return prefixFree ? PREFIX_FREE_UTF32 : UTF32;
		}
	}

	private static final TransformationStrategy<CharSequence> RAW_UTF16 = new RawUtf16TransformationStrategy();

	/** A trivial, high-performance, raw transformation from strings to bit vectors that concatenates the bits of the UTF-16 representation.
	 *
	 * <p><strong>Warning</strong>: this transformation is not lexicographic.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> rawUtf16() {
		return (TransformationStrategy<T>)RAW_UTF16;
	}

	private static class RawUtf16TransformationStrategy implements TransformationStrategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public long length(final CharSequence s) {
			return s.length() * (long)Character.SIZE;
		}

		private static class RawUtf16MutableStringBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private static final int CHAR_MASK = Character.SIZE - 1;
			private static final int LOG2_CHAR_SIZE = 4;
			private final char[] a;
			private final long length;

			public RawUtf16MutableStringBitVector(final MutableString s) {
				this.a = s.array();
				length = s.length() * (long)Character.SIZE;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				final int charIndex = (int)(index >>> LOG2_CHAR_SIZE);
				return (a[charIndex] & 1 << (index & CHAR_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & CHAR_MASK);
				if (startBit == 0) {
					final char[] a = this.a;
					final int pos = (int)(from >>> LOG2_CHAR_SIZE);
					if (to == from + Long.SIZE)
						return (long)a[pos + 3] << 48 | (long)a[pos + 2] << 32 | (long)a[pos + 1] << 16 | a[pos];

					if ((to & CHAR_MASK) == 0) {
						long word = 0;
						switch ((int)((to - from) >>> LOG2_CHAR_SIZE)) {
						case 3:
							word |= (long)a[pos + 2] << 32;
						case 2:
							word |= (long)a[pos + 1] << 16;
						case 1:
							word |= a[pos + 0];
						}
						return word;
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		private static class RawUtf16CharSequenceBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private static final int CHAR_MASK = Character.SIZE - 1;
			private static final int LOG2_CHAR_SIZE = 4;
			private final CharSequence s;
			private final long length;

			public RawUtf16CharSequenceBitVector(final CharSequence s) {
				this.s = s;
				length = s.length() * (long)Character.SIZE;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				final int charIndex = (int)(index >>> LOG2_CHAR_SIZE);
				return (s.charAt(charIndex) & 1 << (index & CHAR_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & CHAR_MASK);
				if (startBit == 0) {
					final CharSequence s = this.s;
					final int pos = (int)(from >>> LOG2_CHAR_SIZE);
					if (to == from + Long.SIZE)
						return (long)s.charAt(pos + 3) << 48 | (long)s.charAt(pos + 2) << 32 | (long)s.charAt(pos + 1) << 16 | s.charAt(pos);

					if ((to & CHAR_MASK) == 0) {
						long word = 0;
						switch ((int)((to - from) >>> LOG2_CHAR_SIZE)) {
						case 3:
							word |= (long)s.charAt(pos + 2) << 32;
						case 2:
							word |= (long)s.charAt(pos + 1) << 16;
						case 1:
							word |= s.charAt(pos + 0);
						}
						return word;
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final CharSequence s) {
			return s instanceof MutableString ? new RawUtf16MutableStringBitVector((MutableString)s) : new RawUtf16CharSequenceBitVector(s);
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<CharSequence> copy() {
			return this;
		}

		private Object readResolve() {
			return RAW_UTF16;
		}
	}

	private static final TransformationStrategy<CharSequence> UTF16 = new Utf16TransformationStrategy(false);

	/** A trivial transformation from strings to bit vectors that concatenates the bits of the UTF-16 representation.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> utf16() {
		return (TransformationStrategy<T>)UTF16;
	}

	private static final TransformationStrategy<CharSequence> PREFIX_FREE_UTF16 = new Utf16TransformationStrategy(true);

	/** A trivial transformation from strings to bit vectors that concatenates the bits of the UTF-16 representation and completes
	 * the representation with an NUL to guarantee lexicographical ordering and prefix-freeness.
	 *
	 * <p>Note that strings provided to this strategy <strong>must not</strong> contain NULs.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> prefixFreeUtf16() {
		return (TransformationStrategy<T>)PREFIX_FREE_UTF16;
	}

	private static class Utf16TransformationStrategy implements TransformationStrategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;
		/** Whether we should guarantee prefix-freeness by adding 0 to the end of each string. */
		private final boolean prefixFree;

		/** Creates a UTF16 transformation strategy. The strategy will map a string to its natural UTF16 bit sequence.
		 *
		 * @param prefixFree if true, the resulting set of binary words will be made prefix free by adding a NUL at the end of the string.
		 */
		protected Utf16TransformationStrategy(final boolean prefixFree) {
			this.prefixFree = prefixFree;
		}

		@Override
		public long length(final CharSequence s) {
			return (s.length() + (prefixFree ? 1 : 0)) * (long)Character.SIZE;
		}

		private static class Utf16CharSequenceBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private static final int CHAR_MASK = Character.SIZE - 1;
			private static final int LOG2_CHAR_SIZE = 4;
			private final CharSequence s;
			private final long length;
			private final long actualEnd;

			public Utf16CharSequenceBitVector(final CharSequence s, final boolean prefixFree) {
				this.s = s;
				actualEnd = s.length() * (long)Character.SIZE;
				length = actualEnd + (prefixFree ? Character.SIZE : 0);
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				if (index >= actualEnd) return false;
				final int charIndex = (int)(index >>> LOG2_CHAR_SIZE);
				return (s.charAt(charIndex) & 0x8000 >>> (index & CHAR_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & CHAR_MASK);
				if (startBit == 0) {
					final CharSequence s = this.s;
					final int pos = (int)(from >>> LOG2_CHAR_SIZE);
					if (to == from + Long.SIZE)
						return reverseChars((to > actualEnd ? 0 : (long)s.charAt(pos + 3)) << 48 | (long)s.charAt(pos + 2) << 32 | (long)s.charAt(pos + 1) << 16 | s.charAt(pos));

					if ((to & CHAR_MASK) == 0) {
						long word = 0;
						switch ((int)((Math.min(to, actualEnd) - Math.min(from, actualEnd)) >>> LOG2_CHAR_SIZE)) {
						case 3:
							word |= (long)s.charAt(pos + 2) << 32;
						case 2:
							word |= (long)s.charAt(pos + 1) << 16;
						case 1:
							word |= s.charAt(pos);
						}
						return reverseChars(word);
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		private static class Utf16MutableStringBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private static final int CHAR_MASK = Character.SIZE - 1;
			private static final int LOG2_CHAR_SIZE = 4;
			private final  char[] a;
			private final long length;
			private final long actualEnd;

			public Utf16MutableStringBitVector(final MutableString s, final boolean prefixFree) {
				this.a = s.array();
				actualEnd = s.length() * (long)Character.SIZE;
				length = actualEnd + (prefixFree ? Character.SIZE : 0);
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				if (index >= actualEnd) return false;
				final int charIndex = (int)(index >>> LOG2_CHAR_SIZE);
				return (a[charIndex] & 0x8000 >>> (index & CHAR_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & CHAR_MASK);
				if (startBit == 0) {
					final char[] a = this.a;
					final int pos = (int)(from >>> LOG2_CHAR_SIZE);
					if (to == from + Long.SIZE)
						return reverseChars((to > actualEnd ? 0 : (long)a[pos + 3]) << 48 | (long)a[pos + 2] << 32 | (long)a[pos + 1] << 16 | a[pos]);
					if ((to & CHAR_MASK) == 0) {
						long word = 0;
						switch ((int)((Math.min(to, actualEnd) - Math.min(from, actualEnd)) >>> LOG2_CHAR_SIZE)) {
						case 3:
							word |= (long)a[pos + 2] << 32;
						case 2:
							word |= (long)a[pos + 1] << 16;
						case 1:
							word |= a[pos];
						}
						return reverseChars(word);
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final CharSequence s) {
			return s instanceof MutableString ? new Utf16MutableStringBitVector((MutableString)s, prefixFree) : new Utf16CharSequenceBitVector(s, prefixFree);
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<CharSequence> copy() {
			return this;
		}

		private Object readResolve() {
			return prefixFree ? PREFIX_FREE_UTF16 : UTF16;
		}
	}


	private static final TransformationStrategy<CharSequence> RAW_ISO = new RawISOTransformationStrategy();

	/** A trivial, high-performance, raw transformation from strings to bit vectors that concatenates the lower eight bits bits of the UTF-16 representation.
	 *
	 * <p><strong>Warning</strong>: this transformation is not lexicographic.
	 *
	 * <p>Note that this transformation is sensible only for strings that are known to be contain just characters in the ISO-8859-1 charset.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> rawIso() {
		return (TransformationStrategy<T>)RAW_ISO;
	}


	private static class RawISOTransformationStrategy implements TransformationStrategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;
		private static final int LOG2_BYTE_SIZE = 3;
		private static final long BYTE_MASK = Byte.SIZE - 1;

		@Override
		public long length(final CharSequence s) {
			return s.length() * (long)Byte.SIZE;
		}

		private static class RawISOCharSequenceBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private final CharSequence s;
			private final long length;

			public RawISOCharSequenceBitVector(final CharSequence s) {
				this.s = s;
				length = s.length() * (long)Byte.SIZE;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				final int byteIndex = (int)(index >>> LOG2_BYTE_SIZE);
				return (s.charAt(byteIndex) & 1 << (index & BYTE_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & BYTE_MASK);
				if (startBit == 0) {
					final CharSequence s = this.s;
					final int pos = (int)(from >>> LOG2_BYTE_SIZE);
					if (to == from + Long.SIZE)
						return (s.charAt(pos + 7) & 0xFFL) << 56 |
								(s.charAt(pos + 6) & 0xFFL) << 48 |
								(s.charAt(pos + 5) & 0xFFL) << 40 |
								(s.charAt(pos + 4) & 0xFFL) << 32 |
								(s.charAt(pos + 3) & 0xFFL) << 24 |
								(s.charAt(pos + 2) & 0xFFL) << 16 | (s.charAt(pos + 1) & 0xFFL) << 8 | (s.charAt(pos) & 0xFFL);

					if ((to & BYTE_MASK) == 0) {
						long word = 0;
						switch ((int)((to - from) >>> LOG2_BYTE_SIZE)) {
						case 7:
							word |= (s.charAt(pos + 6) & 0xFFL) << 48;
						case 6:
							word |= (s.charAt(pos + 5) & 0xFFL) << 40;
						case 5:
							word |= (s.charAt(pos + 4) & 0xFFL) << 32;
						case 4:
							word |= (s.charAt(pos + 3) & 0xFFL) << 24;
						case 3:
							word |= (s.charAt(pos + 2) & 0xFFL) << 16;
						case 2:
							word |= (s.charAt(pos + 1) & 0xFFL) << 8;
						case 1:
							word |= s.charAt(pos) & 0xFFL;
						}
						return word;
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		private static class RawISOMutableStringBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private final char[] a;
			private final long length;

			public RawISOMutableStringBitVector(final MutableString s) {
				this.a = s.array();
				length = s.length() * (long)Byte.SIZE;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				final int byteIndex = (int)(index >>> LOG2_BYTE_SIZE);
				return (a[byteIndex] & 1 << (index & BYTE_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & BYTE_MASK);
				if (startBit == 0) {
					final char[] a = this.a;
					final int pos = (int)(from >>> LOG2_BYTE_SIZE);
					if (to == from + Long.SIZE)
						return (a[pos + 7] & 0xFFL) << 56 |
								(a[pos + 6] & 0xFFL) << 48 |
								(a[pos + 5] & 0xFFL) << 40 |
								(a[pos + 4] & 0xFFL) << 32 |
								(a[pos + 3] & 0xFFL) << 24 |
								(a[pos + 2] & 0xFFL) << 16 | (a[pos + 1] & 0xFFL) << 8 | (a[pos] & 0xFFL);

					if ((to & BYTE_MASK) == 0) {
						long word = 0;
						switch ((int)((to - from) >>> LOG2_BYTE_SIZE)) {
						case 7:
							word |= (a[pos + 6] & 0xFFL) << 48;
						case 6:
							word |= (a[pos + 5] & 0xFFL) << 40;
						case 5:
							word |= (a[pos + 4] & 0xFFL) << 32;
						case 4:
							word |= (a[pos + 3] & 0xFFL) << 24;
						case 3:
							word |= (a[pos + 2] & 0xFFL) << 16;
						case 2:
							word |= (a[pos + 1] & 0xFFL) << 8;
						case 1:
							word |= a[pos] & 0xFFL;
						}
						return word;
					}
				}


				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final CharSequence s) {
			return s instanceof MutableString ? new RawISOMutableStringBitVector((MutableString)s) : new RawISOCharSequenceBitVector(s);
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<CharSequence> copy() {
			return this;
		}

		private Object readResolve() {
			return RAW_ISO;
		}
	}



	private static final TransformationStrategy<CharSequence> ISO = new ISOTransformationStrategy(false);

	/** A trivial transformation from strings to bit vectors that concatenates the lower eight bits of the UTF-16 representation.
	 *
	 * <p>Note that this transformation is sensible only for strings that are known to be contain just characters in the ISO-8859-1 charset.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> iso() {
		return (TransformationStrategy<T>)ISO;
	}

	private static final TransformationStrategy<CharSequence> PREFIX_FREE_ISO = new ISOTransformationStrategy(true);

	/** A trivial transformation from strings to bit vectors that concatenates the lower eight bits bits of the UTF-16 representation and completes
	 * the representation with an ASCII NUL to guarantee lexicographical ordering and prefix-freeness.
	 *
	 * <p>Note that this transformation is sensible only for strings that are known to be contain just characters in the ISO-8859-1 charset, and
	 * that strings provided to this strategy must not contain ASCII NULs.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> TransformationStrategy<T> prefixFreeIso() {
		return (TransformationStrategy<T>)PREFIX_FREE_ISO;
	}


	private static class ISOTransformationStrategy implements TransformationStrategy<CharSequence>, Serializable {
		private static final long serialVersionUID = 1L;
		/** Whether we should guarantee prefix-freeness by adding 0 to the end of each string. */
		private final boolean prefixFree;

		/** Creates an ISO transformation strategy. The strategy will map a string to the lowest eight bits of its natural UTF16 bit sequence.
		 *
		 * @param prefixFree if true, the resulting set of binary words will be made prefix free by adding a NUL at the end of the string.
		 */
		protected ISOTransformationStrategy(final boolean prefixFree) {
			this.prefixFree = prefixFree;
		}

		@Override
		public long length(final CharSequence s) {
			return (s.length() + (prefixFree ? 1 : 0)) * (long)Byte.SIZE;
		}

		private static class ISOCharSequenceBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private static final int LOG2_BYTE_SIZE = 3;
			private static final long BYTE_MASK = Byte.SIZE - 1;
			private final CharSequence s;
			private final long length;
			private final long actualEnd;

			public ISOCharSequenceBitVector(final CharSequence s, final boolean prefixFree) {
				this.s = s;
				actualEnd = s.length() * (long)Byte.SIZE;
				length = actualEnd + (prefixFree ? Byte.SIZE : 0);
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				if (index >= actualEnd) return false;
				final int byteIndex = (int)(index >>> LOG2_BYTE_SIZE);
				return (s.charAt(byteIndex) & 0x80 >>> (index & BYTE_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & BYTE_MASK);
				if (startBit == 0) {
					final CharSequence s = this.s;
					final int pos = (int)(from >>> LOG2_BYTE_SIZE);
					if (to == from + Long.SIZE)
						return reverseBytes((to > actualEnd ? 0 : (s.charAt(pos + 7) & 0xFFL)) << 56 |
								(s.charAt(pos + 6) & 0xFFL) << 48 |
								(s.charAt(pos + 5) & 0xFFL) << 40 |
								(s.charAt(pos + 4) & 0xFFL) << 32 |
								(s.charAt(pos + 3) & 0xFFL) << 24 |
								(s.charAt(pos + 2) & 0xFFL) << 16 | (s.charAt(pos + 1) & 0xFFL) << 8 | (s.charAt(pos) & 0xFFL));

					if ((to & BYTE_MASK) == 0) {
						long word = 0;
						switch ((int)((Math.min(to, actualEnd) - Math.min(from, actualEnd)) >>> LOG2_BYTE_SIZE)) {
						case 7:
							word |= (s.charAt(pos + 6) & 0xFFL) << 48;
						case 6:
							word |= (s.charAt(pos + 5) & 0xFFL) << 40;
						case 5:
							word |= (s.charAt(pos + 4) & 0xFFL) << 32;
						case 4:
							word |= (s.charAt(pos + 3) & 0xFFL) << 24;
						case 3:
							word |= (s.charAt(pos + 2) & 0xFFL) << 16;
						case 2:
							word |= (s.charAt(pos + 1) & 0xFFL) << 8;
						case 1:
							word |= s.charAt(pos) & 0xFFL;
						}
						return reverseBytes(word);
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		private static class ISOMutableStringBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private static final int LOG2_BYTE_SIZE = 3;
			private static final long BYTE_MASK = Byte.SIZE - 1;
			private final char[] a;
			private final long length;
			private final long actualEnd;

			public ISOMutableStringBitVector(final MutableString s, final boolean prefixFree) {
				this.a = s.array();
				actualEnd = s.length() * (long)Byte.SIZE;
				length = actualEnd + (prefixFree ? Byte.SIZE : 0);
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				if (index >= actualEnd) return false;
				final int byteIndex = (int)(index >>> LOG2_BYTE_SIZE);
				return (a[byteIndex] & 0x80 >>> (index & BYTE_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & BYTE_MASK);
				if (startBit == 0) {
					final char[] a = this.a;
					final int pos = (int)(from >>> LOG2_BYTE_SIZE);
					if (to == from + Long.SIZE)
						return reverseBytes((to > actualEnd ? 0 : (a[pos + 7] & 0xFFL)) << 56 |
								(a[pos + 6] & 0xFFL) << 48 |
								(a[pos + 5] & 0xFFL) << 40 |
								(a[pos + 4] & 0xFFL) << 32 |
								(a[pos + 3] & 0xFFL) << 24 |
								(a[pos + 2] & 0xFFL) << 16 | (a[pos + 1] & 0xFFL) << 8 | (a[pos] & 0xFFL));

					if ((to & BYTE_MASK) == 0) {
						long word = 0;
						switch ((int)((Math.min(actualEnd, to) - Math.min(actualEnd, from)) >>> LOG2_BYTE_SIZE)) {
						case 7:
							word |= (a[pos + 6] & 0xFFL) << 48;
						case 6:
							word |= (a[pos + 5] & 0xFFL) << 40;
						case 5:
							word |= (a[pos + 4] & 0xFFL) << 32;
						case 4:
							word |= (a[pos + 3] & 0xFFL) << 24;
						case 3:
							word |= (a[pos + 2] & 0xFFL) << 16;
						case 2:
							word |= (a[pos + 1] & 0xFFL) << 8;
						case 1:
							word |= a[pos] & 0xFFL;
						}
						return reverseBytes(word);
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final CharSequence s) {
			return s instanceof MutableString ? new ISOMutableStringBitVector((MutableString)s, prefixFree) : new ISOCharSequenceBitVector(s, prefixFree);
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<CharSequence> copy() {
			return this;
		}

		private Object readResolve() {
			return prefixFree ? PREFIX_FREE_ISO : ISO;
		}
	}


	private static final TransformationStrategy<byte[]> RAW_BYTE_ARRAY = new RawByteArrayTransformationStrategy();

	/** A trivial, high-performance, raw transformation from byte arrays to bit
	 * vectors that simply concatenates the bytes of the array.
	 *
	 * <p><strong>Warning</strong>: this transformation is not lexicographic.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original array. If the array
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 *
	 * @see TransformationStrategies
	 */
	public static TransformationStrategy<byte[]> rawByteArray() {
		return RAW_BYTE_ARRAY;
	}

	private static class RawByteArrayTransformationStrategy implements TransformationStrategy<byte[]>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public long length(final byte[] a) {
			return a.length * (long)Byte.SIZE;
		}

		private static class RawByteArrayBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 0L;
			private static final int LOG2_BYTE_SIZE = 3;
			private static final long BYTE_MASK = Byte.SIZE - 1;
			private final byte[] a;
			private final long length;

			public RawByteArrayBitVector(final byte[] a) {
				this.a = a;
				length = a.length * (long)Byte.SIZE;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				return (a[(int)(index >>> LOG2_BYTE_SIZE)] & 1 << (index & BYTE_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & BYTE_MASK);
				if (startBit == 0) {
					final byte[] a = this.a;
					if (to == from + Long.SIZE) {
						final int pos = (int)(from >>> LOG2_BYTE_SIZE);
						return (a[pos + 7] & 0xFFL) << 56 |
								(a[pos + 6] & 0xFFL) << 48 |
								(a[pos + 5] & 0xFFL) << 40 |
								(a[pos + 4] & 0xFFL) << 32 |
								(a[pos + 3] & 0xFFL) << 24 |
								(a[pos + 2] & 0xFFL) << 16 | (a[pos + 1] & 0xFFL) << 8 | (a[pos] & 0xFFL);
					}

					if ((to & BYTE_MASK) == 0) {
						final int pos = (int)(from >>> LOG2_BYTE_SIZE);
						long word = 0;
						switch ((int)((to - from) >>> LOG2_BYTE_SIZE)) {
						case 7:
							word |= (a[pos + 6] & 0xFFL) << 48;
						case 6:
							word |= (a[pos + 5] & 0xFFL) << 40;
						case 5:
							word |= (a[pos + 4] & 0xFFL) << 32;
						case 4:
							word |= (a[pos + 3] & 0xFFL) << 24;
						case 3:
							word |= (a[pos + 2] & 0xFFL) << 16;
						case 2:
							word |= (a[pos + 1] & 0xFFL) << 8;
						case 1:
							word |= (a[pos] & 0xFFL);
						}
						return word;
					}
				}

				// Actually, we should never get here as the transformation is not lexicographical.
				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final byte[] s) {
			return new RawByteArrayBitVector(s);
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<byte[]> copy() {
			return this;
		}

		private Object readResolve() {
			return RAW_BYTE_ARRAY;
		}
	}

	private static final TransformationStrategy<byte[]> BYTE_ARRAY = new ByteArrayTransformationStrategy(false);

	/** A lexicographical transformation from byte arrays to bit vectors.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original array. If the array
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 *
	 * @see TransformationStrategies
	 */
	public static TransformationStrategy<byte[]> byteArray() {
		return BYTE_ARRAY;
	}

	private static final TransformationStrategy<byte[]> PREFIX_FREE_BYTE_ARRAY = new ByteArrayTransformationStrategy(true);

	/**
	 * A lexicographical transformation from byte arrays to bit vectors that completes the
	 * representation with a zero to guarantee lexicographical ordering and prefix-freeness provided the
	 * byte arrays to not contain zeros.
	 *
	 * <p>
	 * This transformation is mainly intended for byte arrays representing ASCII strings in compact
	 * form.
	 *
	 * <p>
	 * <strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original
	 * array. If the array changes while the bit vector is being accessed, the results will be
	 * unpredictable.
	 *
	 * @see TransformationStrategies
	 */
	public static TransformationStrategy<byte[]> prefixFreeByteArray() {
		return PREFIX_FREE_BYTE_ARRAY;
	}

	private static class ByteArrayTransformationStrategy implements TransformationStrategy<byte[]>, Serializable {
		private static final long serialVersionUID = 1L;
		/** Whether we should guarantee prefix-freeness by adding 0 to the end of each byte array. */
		private final boolean prefixFree;

		/** Creates an ISO transformation strategy. The strategy will map a string to the lowest eight bits of its natural UTF16 bit sequence.
		 *
		 * @param prefixFree if true, the resulting set of binary words will be made prefix free by adding a NUL at the end of the string.
		 */
		protected ByteArrayTransformationStrategy(final boolean prefixFree) {
			this.prefixFree = prefixFree;
		}

		@Override
		public long length(final byte[] a) {
			return a.length * (long)Byte.SIZE;
		}

		private static class ByteArrayBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 0L;
			private static final int LOG2_BYTE_SIZE = 3;
			private static final long BYTE_MASK = Byte.SIZE - 1;
			private final byte[] a;
			private final long length;
			private final long actualEnd;

			public ByteArrayBitVector(final byte[] a, final boolean prefixFree) {
				this.a = a;
				actualEnd = a.length * (long)Byte.SIZE;
				length = actualEnd + (prefixFree ? Byte.SIZE : 0);
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index > length) throw new IndexOutOfBoundsException();
				if (index >= actualEnd) return false;
				return (a[(int)(index >>> LOG2_BYTE_SIZE)] & 0x80 >> (index & BYTE_MASK)) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				final int startBit = (int)(from & BYTE_MASK);
				if (startBit == 0) {
					final byte[] a = this.a;
					if (to == from + Long.SIZE) {
						final int pos = (int)(from >>> LOG2_BYTE_SIZE);
						return reverseBytes((to > actualEnd ? 0 : (a[pos + 7] & 0xFFL)) << 56 |
								(a[pos + 6] & 0xFFL) << 48 |
								(a[pos + 5] & 0xFFL) << 40 |
								(a[pos + 4] & 0xFFL) << 32 |
								(a[pos + 3] & 0xFFL) << 24 |
								(a[pos + 2] & 0xFFL) << 16 | (a[pos + 1] & 0xFFL) << 8 | (a[pos] & 0xFFL));
					}

					if ((to & BYTE_MASK) == 0) {
						final int pos = (int)(from >>> LOG2_BYTE_SIZE);
						long word = 0;
						switch ((int)((Math.min(actualEnd, to) - Math.min(actualEnd, from)) >>> LOG2_BYTE_SIZE)) {
						case 7:
							word |= (a[pos + 6] & 0xFFL) << 48;
						case 6:
							word |= (a[pos + 5] & 0xFFL) << 40;
						case 5:
							word |= (a[pos + 4] & 0xFFL) << 32;
						case 4:
							word |= (a[pos + 3] & 0xFFL) << 24;
						case 3:
							word |= (a[pos + 2] & 0xFFL) << 16;
						case 2:
							word |= (a[pos + 1] & 0xFFL) << 8;
						case 1:
							word |= (a[pos] & 0xFFL);
						}
						return reverseBytes(word);
					}
				}

				final long l = Long.SIZE - (to - from);
				final long startPos = from - startBit;
				if (l == Long.SIZE) return 0;

				if (startBit <= l) return getLong(startPos, Math.min(length, startPos + Long.SIZE)) << l - startBit >>> l;
				return getLong(startPos, startPos + Long.SIZE) >>> startBit | getLong(startPos + Long.SIZE, Math.min(length, startPos + 2 * Long.SIZE)) << l - startBit >>> l;
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final byte[] s) {
			return new ByteArrayBitVector(s, prefixFree);
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<byte[]> copy() {
			return this;
		}

		private Object readResolve() {
			return prefixFree ? PREFIX_FREE_BYTE_ARRAY : BYTE_ARRAY;
		}
	}


	private final static class IteratorWrapper<T> implements ObjectIterator<BitVector> {
		final Iterator<T> iterator;
		final TransformationStrategy<? super T> transformationStrategy;

		public IteratorWrapper(final Iterator<T> iterator, final TransformationStrategy<? super T> transformationStrategy) {
			this.iterator = iterator;
			this.transformationStrategy = transformationStrategy;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public BitVector next() {
			return transformationStrategy.toBitVector(iterator.next());
		}

	}


	/** Wraps a given iterator, returning an iterator that emits {@linkplain BitVector bit vectors}.
	 *
	 * @param iterator an iterator.
	 * @param transformationStrategy a strategy to transform the object returned by <code>iterator</code>.
	 * @return an iterator that emits the content of <code>iterator</code> passed through <code>transformationStrategy</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterator<BitVector> wrap(final Iterator<T> iterator, final TransformationStrategy<? super T> transformationStrategy) {
		return (Iterator<BitVector>)(transformationStrategy == IDENTITY ? iterator : new IteratorWrapper<>(iterator, transformationStrategy));
	}

	private final static class IterableWrapper<T> implements Iterable<BitVector> {
		private final TransformationStrategy<? super T> transformationStrategy;
		private final Iterable<T> collection;

		public IterableWrapper(final Iterable<T> collection, final TransformationStrategy<? super T> transformationStrategy) {
			this.collection = collection;
			this.transformationStrategy = transformationStrategy;
		}

		@Override
		public ObjectIterator<BitVector> iterator() {
			return new IteratorWrapper<>(collection.iterator(), transformationStrategy.copy());
		}
	}


	/** Wraps a given iterable, returning an iterable that contains {@linkplain BitVector bit vectors}.
	 *
	 * @param iterable an iterable.
	 * @param transformationStrategy a strategy to transform the object contained in <code>iterable</code>.
	 * @return an iterable that has the content of <code>iterable</code> passed through <code>transformationStrategy</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<BitVector> wrap(final Iterable<T> iterable, final TransformationStrategy<? super T> transformationStrategy) {
		return (Iterable<BitVector>)(transformationStrategy == IDENTITY ? iterable : new IterableWrapper<>(iterable, transformationStrategy));
	}

	private final static class ListWrapper<T> extends AbstractObjectList<BitVector> {
		private final TransformationStrategy<? super T> transformationStrategy;
		private final List<T> list;

		public ListWrapper(final List<T> list, final TransformationStrategy<? super T> transformationStrategy) {
			this.list = list;
			this.transformationStrategy = transformationStrategy;
		}

		@Override
		public BitVector get(final int index) {
			return transformationStrategy.toBitVector(list.get(index));
		}

		@Override
		public int size() {
			return list.size();
		}
	}


	/** Wraps a given list, returning a list that contains {@linkplain BitVector bit vectors}.
	 *
	 * @param list a list.
	 * @param transformationStrategy a strategy to transform the object contained in <code>list</code>.
	 * @return a list that has the content of <code>list</code> passed through <code>transformationStrategy</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<BitVector> wrap(final List<T> list, final TransformationStrategy<? super T> transformationStrategy) {
		return (List<BitVector>)(transformationStrategy == IDENTITY ? list : new ListWrapper<>(list, transformationStrategy));
	}

	private static final TransformationStrategy<? extends BitVector> PREFIX_FREE = new PrefixFreeTransformationStrategy();

	/** A transformation from bit vectors to bit vectors that guarantees that its results are prefix free.
	 *
	 * <p>More in detail, we map 0 to 10, 1 to 11, and we add a 0 at the end of all strings.
	 *
	 * <p><strong>Warning</strong>: bit vectors returned by this strategy are adaptors around the original string. If the string
	 * changes while the bit vector is being accessed, the results will be unpredictable.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BitVector> TransformationStrategy<T> prefixFree() {
		return (TransformationStrategy<T>)PREFIX_FREE;
	}


	private static class PrefixFreeTransformationStrategy implements TransformationStrategy<BitVector>, Serializable {
		private static final long serialVersionUID = 1L;

		private static class PrefixFreeBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private final BitVector v;
			private final long length;

			public PrefixFreeBitVector(final BitVector v) {
				this.v = v;
				length = (v.length() << 1) + 1;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index >= length) throw new IndexOutOfBoundsException();
				if (index == length - 1) return false;
				if ((index & 1) == 0) return true;
				return v.getBoolean(index >> 1);
			}

			@Override
			public long getLong(final long from, final long to) {
				// The following code is optimized for word-by-word reading.
				if (LongArrayBitVector.round(from)) {
					if (to == from + Long.SIZE) {
						long word = v.getLong(from >> 1, (from >> 1) + Long.SIZE / 2);
						word = (word | word << 16) & 0x0000FFFF0000FFFFL;
						word = (word | word << 8)  & 0x00FF00FF00FF00FFL;
						word = (word | word << 4)  & 0x0F0F0F0F0F0F0F0FL;
						word = (word | word << 2)  & 0x3333333333333333L;
						return word << 1 | word << 2 | 0x5555555555555555L;
					}

					if (to == length) {
						assert from < to; // As from is even and to is odd.
						long word = v.getLong(from >> 1, to >> 1);
						word = (word | word << 16) & 0x0000FFFF0000FFFFL;
						word = (word | word << 8)  & 0x00FF00FF00FF00FFL;
						word = (word | word << 4)  & 0x0F0F0F0F0F0F0F0FL;
						word = (word | word << 2)  & 0x3333333333333333L;
						return (word << 1 | word << 2 | 0x5555555555555555L) & (1L << to - from - 1) - 1;
					}
				}

				return super.getLong(from, to);
			}

			@Override
			public long length() {
				return length;
			}
		}

		@Override
		public BitVector toBitVector(final BitVector v) {
			return new PrefixFreeBitVector(v);
		}

		@Override
		public long length(final BitVector v) {
			return v.length() * 2 + 1;
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<BitVector> copy() {
			return this;
		}

		private Object readResolve() {
			return PREFIX_FREE;
		}
	}



	private static final FixedLongTransformationStrategy FIXED_LONG = new FixedLongTransformationStrategy(true);

	/**
	 * A transformation from longs to bit vectors that returns a fixed-size {@link Long#SIZE}-bit
	 * vector. Note that the bit vectors have as first bit the <em>most</em> significant bit of the
	 * underlying long integer, and that the first bit of the representation is flipped, so
	 * lexicographical and numerical order coincide.
	 *
	 * @implNote The flipping of the most significant bit was implemented in 2.6.18 to match
	 *           lexicographical and numerical order for negative numbers, too, and made it necessary to
	 *           bump the serial version of the strategy.
	 */
	public static TransformationStrategy<Long> fixedLong() {
		return FIXED_LONG;
	}

	private static final FixedLongTransformationStrategy RAW_FIXED_LONG = new FixedLongTransformationStrategy(false);

	/**
	 * A trivial, high-performance, raw transformation from longs to bit vectors that returns a
	 * fixed-size {@link Long#SIZE}-bit vector.
	 *
	 * @implNote Implementing {@link #fixedLong()} lexicographical order for all numbers in 2.6.18 made
	 *           it necessary to bump the serial version of this strategy, too.
	 */
	public static TransformationStrategy<Long> rawFixedLong() {
		return RAW_FIXED_LONG;
	}

	/** A transformation from longs to bit vectors that returns a fixed-size {@link Long#SIZE}-bit vector, possibly reversed
	 * to maintain lexicographical order. */
	private static class FixedLongTransformationStrategy implements TransformationStrategy<Long>, Serializable {
		private static final long serialVersionUID = 1L;
		private final boolean lexicographical;

		public FixedLongTransformationStrategy(final boolean lexicographical) {
			this.lexicographical = lexicographical;
		}

		private static class FixedLongBitVector extends AbstractBitVector implements Serializable {
			private static final long serialVersionUID = 1L;
			private final long v;

			public FixedLongBitVector(final long v) {
				this.v = v;
			}

			@Override
			public boolean getBoolean(final long index) {
				if (index >= Long.SIZE) throw new IndexOutOfBoundsException();
				return (v & 1L << index) != 0;
			}

			@Override
			public long getLong(final long from, final long to) {
				if (from == 0 && to == Long.SIZE) return v;
				return (v >>> from) & (1L << to - from) - 1;
			}

			@Override
			public long length() {
				return Long.SIZE;
			}
		}

		@Override
		public BitVector toBitVector(final Long v) {
			return new FixedLongBitVector(lexicographical ? Long.reverse(v.longValue()) ^ 1 : v.longValue());
		}

		@Override
		public long length(final Long v) {
			return Long.SIZE;
		}

		@Override
		public long numBits() { return 0; }

		@Override
		public TransformationStrategy<Long> copy() {
			return this;
		}

		private Object readResolve() {
			return FIXED_LONG;
		}
	}
}
