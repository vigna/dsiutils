/*
 * DSI utilities
 *
 * Copyright (C) 2005-2023 Sebastiano Vigna
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

package it.unimi.dsi.big.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import com.google.common.io.ByteStreams;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ForNameStringParser;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.PrefixCoderTransformationStrategy;
import it.unimi.dsi.compression.Decoder;
import it.unimi.dsi.compression.HuTuckerCodec;
import it.unimi.dsi.compression.PrefixCodec;
import it.unimi.dsi.compression.PrefixCoder;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.io.FileLinesMutableStringIterable;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.util.LongInterval;
import it.unimi.dsi.util.LongIntervals;

// TODO: implement interfaces correctly (e.g., using the iterator)

/** An immutable prefix map mostly stored in external memory.
 *
 * @author Sebastiano Vigna
 * @since 2.0
 * @see it.unimi.dsi.util.ImmutableExternalPrefixMap
 */
public class ImmutableExternalPrefixMap extends AbstractPrefixMap implements Serializable {
	private final static boolean DEBUG = false;

	public static final long serialVersionUID = 1L;

	/** The standard block size (in bytes). */
	public static final int STD_BLOCK_SIZE = 1024;

	/** The in-memory data structure used to approximate intervals.. */
	final protected ImmutableBinaryTrie<CharSequence> intervalApproximator;
	/** The block size of this  (in bits). */
	final protected long blockSize;
	/** A decoder used to read data from the dump stream. */
	final protected Decoder decoder;
	/** A map (given by an array) from symbols in the coder to characters. */
	final protected char[] symbol2char;
	/** A map from characters to symbols of the coder. */
	final protected Char2IntOpenHashMap char2symbol;
	/** The number of terms in this map. */
	final protected long size;
	/** The index of the first word in each block, plus an additional entry containing {@link #size}. */
	final protected long[][] blockStart;
	/** A big array array parallel to {@link #blockStart} giving the offset in blocks in the dump file
	 * of the corresponding word in {@link #blockStart}. If there are no overflows, this will just
	 * be an initial segment of the natural numbers, but overflows cause jumps. */
	final protected long[][] blockOffset;
	/** Whether this map is self-contained. */
	final protected boolean selfContained;
	/** The length in bytes of the dump stream, both for serialisation purposes and for minimal checks. */
	private final long dumpStreamLength;
	/** The filename of the temporary dump stream, or of the dump stream created by the constructor or by readObject(). */
	private transient String tempDumpStreamFilename;
	/** If true, the creation of the last <code>DumpStreamIterator</code> was not
	 * followed by a call to any get method. */
	protected transient boolean iteratorIsUsable;
	/** A reference to the dump stream. */
	protected transient InputBitStream dumpStream;

	/**
	 * Creates an external prefix map with specified block size and dump stream.
	 *
	 * <P>
	 * This constructor does not assume that {@link CharSequence} instances returned by
	 * <code>terms.iterator()</code> will be distinct. Thus, it can be safely used with
	 * {@link FileLinesMutableStringIterable}.
	 *
	 * @param terms an iterable whose iterator will enumerate in lexicographical order the terms for the
	 *            map.
	 * @param blockSizeInBytes the block size (in bytes).
	 * @param dumpStreamFilename the name of the dump stream, or {@code null} for a self-contained map.
	 */
	public ImmutableExternalPrefixMap(final Iterable<? extends CharSequence> terms, final int blockSizeInBytes, final CharSequence dumpStreamFilename) throws IOException {
		this.blockSize = blockSizeInBytes * 8L;
		this.selfContained = dumpStreamFilename == null;
		// First of all, we gather frequencies for all Unicode characters
		long[] frequency = new long[Character.MAX_VALUE + 1];
		int maxWordLength = 0;
		CharSequence s;
		int count = 0;

		final MutableString prevTerm = new MutableString();

		for(final Iterator<? extends CharSequence> i = terms.iterator(); i.hasNext();) {
			s = i.next();
			maxWordLength = Math.max(s.length(), maxWordLength);
			for(int j = s.length(); j-- != 0;) frequency[s.charAt(j)]++;
			final int cmp = prevTerm.compareTo(s);
			if (count > 0 && cmp >= 0) throw new IllegalArgumentException("The provided term collection " + (cmp == 0 ? "contains duplicates" : "is not sorted") + " [" + prevTerm + ", " + s + "]");
			count++;
			prevTerm.replace(s);
		}

		size = count;

		if (DEBUG) System.err.println("Frequency computation completed.");

		// Then, we compute the number of actually used characters
		count = 0;
		for(int i = frequency.length; i-- != 0;) if (frequency[i] != 0) count++;

		/* Now we remap used characters in f, building at the same time maps from
		 * symbol to characters and from characters to symbols. */

		long[] packedFrequency = new long[count];
		symbol2char = new char[count];
		char2symbol = new Char2IntOpenHashMap(count);
		char2symbol.defaultReturnValue(-1);

		for(int i = frequency.length, k = count; i-- != 0;) {
			if (frequency[i] != 0) {
				packedFrequency[--k] = frequency[i];
				symbol2char[k] = (char)i;
				char2symbol.put((char)i, k);
			}
		}

		char2symbol.trim();

		// We now build the coder used to code the strings

		final PrefixCoder prefixCoder;
		final PrefixCodec codec;
		final BitVector[] codeWord;

		if (packedFrequency.length != 0) {
			codec = new HuTuckerCodec(packedFrequency);
			prefixCoder = codec.coder();
			decoder = codec.decoder();
			codeWord = prefixCoder.codeWords();
		}
		else {
			// This handles the case of a collection without words
			codec = null;
			prefixCoder = null;
			decoder = null;
			codeWord = null;
		}

		packedFrequency = frequency = null;

		// We now compress all strings using the given codec mixed with front coding
		final OutputBitStream output;
		if (selfContained) {
			final File temp = File.createTempFile(this.getClass().getName(), ".dump");
			temp.deleteOnExit();
			tempDumpStreamFilename = temp.toString();
			output = new OutputBitStream(temp, blockSizeInBytes);
		}
		else output = new OutputBitStream(tempDumpStreamFilename = dumpStreamFilename.toString(), blockSizeInBytes);

		// This array will contain the delimiting words (the ones at the start of each block)
		boolean isDelimiter;

		int length, prevTermLength = 0, bits;
		int prefixLength = 0, termCount = 0;
		int currBuffer = 0;

		final LongBigArrayBigList blockStarts = new LongBigArrayBigList();
		final LongBigArrayBigList blockOffsets = new LongBigArrayBigList();
		final ObjectArrayList<MutableString> delimiters = new ObjectArrayList<>();
		prevTerm.length(0);

		for (final Object term : terms) {
			s = (CharSequence) term;
			length = s.length();

			isDelimiter = false;

			// We compute the common prefix and the number of bits that are necessary to code the next term.
			bits = 0;
			for(prefixLength = 0; prefixLength < length && prefixLength < prevTermLength && prevTerm.charAt(prefixLength) == s.charAt(prefixLength); prefixLength++);
			for(int j = prefixLength; j < length; j++) bits += codeWord[char2symbol.get(s.charAt(j))].length();

			//if (bits + length + 1 > blockSize) throw new IllegalArgumentException("The string \"" + s + "\" is too long to be encoded with block size " + blockSizeInBytes);

			// If the next term would overflow the block, and we are not at the start of a block, we align.
			if (output.writtenBits() % blockSize != 0 && output.writtenBits() / blockSize != (output.writtenBits() + (length - prefixLength + 1) + (prefixLength + 1) + bits - 1) / blockSize) {
				// We align by writing 0es.
				if (DEBUG) System.err.println("Aligning away " + (blockSize - output.writtenBits() % blockSize) + " bits...");
				for(int j = (int)(blockSize - output.writtenBits() % blockSize); j-- != 0;) output.writeBit(0);
				assert output.writtenBits() % blockSize == 0;
			}

			if (output.writtenBits() % blockSize == 0) {
				isDelimiter = true;
				prefixLength = 0;
				blockOffsets.add((int)(output.writtenBits() / blockSize));
			}

			// Note that delimiters do not get the prefix length, as it's 0.
			if (! isDelimiter) output.writeUnary(prefixLength);
			output.writeUnary(length - prefixLength);

			// Write the next coded suffix on output.
			for(int j = prefixLength; j < length; j++) {
				final BitVector c = codeWord[char2symbol.get(s.charAt(j))];
				for(long k = 0; k < c.length(); k++) output.writeBit(c.getBoolean(k));
			}

			if (isDelimiter) {
				if (DEBUG) System.err.println("First string of block " + blockStarts.size64() + ": " + termCount + " (" + s + ")");
				// The current word starts a new block
				blockStarts.add(termCount);
				// We do not want to rely on s being immutable.
				delimiters.add(new MutableString(s));
			}

			currBuffer = 1 - currBuffer;
			prevTerm.replace(s);
			prevTermLength = length;
			termCount++;
		}

		output.align();
		dumpStreamLength = output.writtenBits() / 8;
		output.close();

		intervalApproximator = prefixCoder == null ? null : new ImmutableBinaryTrie<>(delimiters, new PrefixCoderTransformationStrategy(prefixCoder, char2symbol, false));

		blockStarts.add(size);
		blockStarts.trim();
		blockStart = blockStarts.elements();
		blockOffsets.trim();
		blockOffset = blockOffsets.elements();

		// We use a buffer of the same size of a block, hoping in fast I/O. */
		dumpStream = new InputBitStream(tempDumpStreamFilename, blockSizeInBytes);
	}

	/**
	 * Creates an external prefix map with block size {@link #STD_BLOCK_SIZE} and specified dump stream.
	 *
	 * <P>
	 * This constructor does not assume that {@link CharSequence} instances returned by
	 * <code>terms.iterator()</code> will be distinct. Thus, it can be safely used with
	 * {@link FileLinesMutableStringIterable}.
	 *
	 * @param terms a collection whose iterator will enumerate in lexicographical order the terms for
	 *            the map.
	 * @param dumpStreamFilename the name of the dump stream, or {@code null} for a self-contained map.
	 */

	public ImmutableExternalPrefixMap(final Iterable<? extends CharSequence> terms, final CharSequence dumpStreamFilename) throws IOException {
		this(terms, STD_BLOCK_SIZE, dumpStreamFilename);
	}

	/**
	 * Creates an external prefix map with specified block size.
	 *
	 * <P>
	 * This constructor does not assume that {@link CharSequence} instances returned by
	 * <code>terms.iterator()</code> will be distinct. Thus, it can be safely used with
	 * {@link FileLinesMutableStringIterable}.
	 *
	 * @param blockSizeInBytes the block size (in bytes).
	 * @param terms a collection whose iterator will enumerate in lexicographical order the terms for
	 *            the map.
	 */

	public ImmutableExternalPrefixMap(final Iterable<? extends CharSequence> terms, final int blockSizeInBytes) throws IOException {
		this(terms, blockSizeInBytes, null);
	}

	/**
	 * Creates an external prefix map with block size {@link #STD_BLOCK_SIZE}.
	 *
	 * <P>
	 * This constructor does not assume that strings returned by <code>terms.iterator()</code> will be
	 * distinct. Thus, it can be safely used with {@link FileLinesMutableStringIterable}.
	 *
	 * @param terms a collection whose iterator will enumerate in lexicographical order the terms for
	 *            the map.
	 */

	public ImmutableExternalPrefixMap(final Iterable<? extends CharSequence> terms) throws IOException {
		this(terms, null);
	}

	private void safelyCloseDumpStream() {
		try {
			if (this.dumpStream != null) this.dumpStream.close();
		}
		catch (final IOException ignore) {}
	}

	private void ensureNotSelfContained() {
		if (selfContained) throw new IllegalStateException("You cannot set the dump file of a self-contained external prefix map");
	}

	private boolean isEncodable(final CharSequence s) {
		for(int i = s.length(); i-- != 0;) if (! char2symbol.containsKey(s.charAt(i))) return false;
		return true;
	}



	/** Sets the dump stream of this external prefix map to a given filename.
	 *
	 * <P>This method sets the dump file used by this map, and should be only
	 * called after deserialisation, providing exactly the file generated at
	 * creation time. Essentially anything can happen if you do not follow the rules.
	 *
	 * <P>Note that this method will attempt to close the old stream, if present.
	 *
	 * @param dumpStreamFilename the name of the dump file.
	 * @see #setDumpStream(InputBitStream)
	 */

	public void setDumpStream(final CharSequence dumpStreamFilename) throws FileNotFoundException{
		ensureNotSelfContained();
		safelyCloseDumpStream();
		iteratorIsUsable = false;
		final long newLength = new File(dumpStreamFilename.toString()).length();
		if (newLength != dumpStreamLength)
			throw new IllegalArgumentException("The size of the new dump file (" + newLength + ") does not match the original length (" + dumpStreamLength + ")");
		dumpStream = new InputBitStream(dumpStreamFilename.toString(), (int)(blockSize / 8));
	}


	/** Sets the dump stream of this external prefix map to a given input bit stream.
	 *
	 * <P>This method sets the dump file used by this map, and should be only
	 * called after deserialisation, providing a repositionable stream containing
	 * exactly the file generated at
	 * creation time. Essentially anything can happen if you do not follow the rules.
	 *
	 * <P>Using this method you can load an external prefix map in core memory, enjoying
	 * the compactness of the data structure, but getting much more speed.
	 *
	 * <P>Note that this method will attemp to close the old stream, if present.
	 *
	 * @param dumpStream a repositionable input bit stream containing exactly the dump stream generated
	 * at creation time.
	 * @see #setDumpStream(CharSequence)
	 */
	public void setDumpStream(final InputBitStream dumpStream) {
		ensureNotSelfContained();
		safelyCloseDumpStream();
		iteratorIsUsable = false;
		this.dumpStream = dumpStream;
	}

	private void ensureStream() {
		if (dumpStream == null) throw new IllegalStateException("This external prefix map has been deserialised, but no dump stream has been set");
	}

	@Override
	public LongInterval getInterval(final CharSequence prefix) {
		ensureStream();
		// If prefix contains any character not coded by the prefix coder, we can return the empty interval.
		if (! isEncodable(prefix)) return LongIntervals.EMPTY_INTERVAL;

		// We recover the left extremes of the intervals where extensions of prefix could possibly lie.
		final LongInterval interval = intervalApproximator.getApproximatedInterval(prefix);
		// System.err.println("Approximate interval: " + interval + " , terms: [" + blockStart[interval.left] + ", " + blockStart[interval.right] + "]");

		if (interval == LongIntervals.EMPTY_INTERVAL) return interval;
		try {
			dumpStream.position(BigArrays.get(blockOffset, interval.left) * blockSize);
			dumpStream.readBits(0);
			iteratorIsUsable = false;
			final MutableString s = new MutableString();
			int suffixLength, prefixLength = -1;
			long count = BigArrays.get(blockStart, interval.left), blockEnd = BigArrays.get(blockStart,interval.left + 1), start = -1, end = -1;

			/* We scan the dump file, stopping if we exhaust the block */
			while(count < blockEnd) {
				if (prefixLength < 0) prefixLength = 0;
				else prefixLength = dumpStream.readUnary();
				suffixLength = dumpStream.readUnary();
				s.delete(prefixLength, s.length());
				s.length(prefixLength + suffixLength);
				for(int i = 0; i < suffixLength; i++) s.charAt(i + prefixLength, symbol2char[decoder.decode(dumpStream)]);
				if (s.startsWith(prefix)) {
					start = count;
					break;
				}
				count++;
			}

			/* If we did not find our string, there are two possibilities: if the
			 * interval contains one point, there is no string extending prefix. But
			 * if  the interval  is larger, the first string of the second block in the
			 * interval must be an extension of prefix. */
			if (start < 0 && interval.length() == 1) return LongIntervals.EMPTY_INTERVAL;
			else start = count;

			end = start + 1;
			//assert dumpStream.readBits() <= blockSize;

			/* If the interval contains more than one point, the last string with
			 * given prefix is necessarily contained in the last block, and we
			 * must restart the search process. */
			if (interval.length() > 1) {
				dumpStream.position(BigArrays.get(blockOffset, interval.right) * blockSize);
				dumpStream.readBits(0);
				s.length(0);
				end = BigArrays.get(blockStart, interval.right);
				blockEnd = BigArrays.get(blockStart, interval.right + 1);
				prefixLength = -1;
			}


			while(end < blockEnd) {
				if (prefixLength < 0) prefixLength = 0;
				else prefixLength = dumpStream.readUnary();
				suffixLength = dumpStream.readUnary();
				s.delete(prefixLength, s.length());
				s.length(prefixLength + suffixLength);
				for(int i = 0; i < suffixLength; i++) s.charAt(i + prefixLength, symbol2char[decoder.decode(dumpStream)]);
				if (! s.startsWith(prefix)) break;
				end++;
			}

			return LongInterval.valueOf(start, end - 1);
		} catch (final IOException rethrow) {
			throw new RuntimeException(rethrow);
		}

	}

	@Override
	protected MutableString getTerm(final long index, final MutableString s) {
		ensureStream();
		// We perform a binary search to find the  block to which s could possibly belong.
		int block = 0;
		for(int segment = blockStart.length; segment-- != 0;) {
			block = Arrays.binarySearch(blockStart[segment], index);
			if (block != -1) { // block == -1 means that index is strictly smaller than blockStart[segment][0]
				if (block < 0) block = - block - 2;
				block += segment * BigArrays.SEGMENT_SIZE;
				break;
			}
		}

		try {
			dumpStream.position(BigArrays.get(blockOffset, block) * blockSize);
			dumpStream.readBits(0);
			iteratorIsUsable = false;
			int suffixLength, prefixLength = -1;

			for(long i = index - BigArrays.get(blockStart, block) + 1; i-- != 0;) {
				if (prefixLength < 0) prefixLength = 0;
				else prefixLength = dumpStream.readUnary();
				suffixLength = dumpStream.readUnary();
				s.delete(prefixLength, s.length());
				s.length(prefixLength + suffixLength);
				for(int j = 0; j < suffixLength; j++) s.charAt(j + prefixLength, symbol2char[decoder.decode(dumpStream)]);
			}

			return s;
		}
		catch(final IOException rethrow) {
			throw new RuntimeException(rethrow);
		}
	}

	private long getIndex(final Object o) {
		final CharSequence term = (CharSequence)o;
		ensureStream();
		// If term contains any character not coded by the prefix coder, we can return -1
		if (! isEncodable(term)) return -1;

		/* If term is in the map, any string extending term must follow term. Thus,
		 * term can be in the map only if it can be found in the left block
		 * of an approximated interval for itself. */
		final LongInterval interval = intervalApproximator.getApproximatedInterval(term);
		if (interval == LongIntervals.EMPTY_INTERVAL) return -1;
		try {
			dumpStream.position(BigArrays.get(blockOffset, interval.left) * blockSize);
			dumpStream.readBits(0);
			iteratorIsUsable = false;
			final MutableString s = new MutableString();
			int suffixLength, prefixLength = -1;
			long count = BigArrays.get(blockStart, interval.left);
			final long blockEnd = BigArrays.get(blockStart, interval.left + 1);

			/* We scan the dump file, stopping if we exhaust the block */
			while(count < blockEnd) {
				if (prefixLength < 0) prefixLength = 0;
				else prefixLength = dumpStream.readUnary();
				suffixLength = dumpStream.readUnary();
				s.delete(prefixLength, s.length());
				s.length(prefixLength + suffixLength);
				for(int i = 0; i < suffixLength; i++) s.charAt(i + prefixLength, symbol2char[decoder.decode(dumpStream)]);
				if (s.equals(term)) return count;
				count++;
			}

			return -1;
		}
		catch (final IOException rethrow) {
			throw new RuntimeException(rethrow);
		}
	}


	@Override
	public boolean containsKey(final Object term) {
		return getIndex(term) != -1;
	}

	@Override
	public long getLong(final Object o) {
		final long result = getIndex(o);
		return result == -1 ? defRetValue : result;
	}

	/** An iterator over the dump stream. It does not use the interval approximator&mdash;it just scans the file. */

	private final class DumpStreamIterator implements ObjectIterator<CharSequence> {
		/** The current block being enumerated. */
		private int currBlock = -1;
		/** The index of next term that will be returned. */
		private int index;
		/** The mutable string used to return the result. */
		final MutableString s = new MutableString();

		private DumpStreamIterator() {
			try {
				dumpStream.position(0);
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
			dumpStream.readBits(0);
			iteratorIsUsable = true;
		}

		@Override
		public boolean hasNext() {
			if (! iteratorIsUsable) throw new IllegalStateException("Get methods of this map have caused a stream repositioning");
			return index < size;
		}

		@Override
		public CharSequence next() {
			if (! hasNext()) throw new NoSuchElementException();
			try {
				final int prefixLength;
				if (index == BigArrays.get(blockStart, currBlock + 1)) {
					if (dumpStream.readBits() % blockSize != 0) dumpStream.skip(blockSize - dumpStream.readBits() % blockSize);
					currBlock++;
					prefixLength = 0;
				}
				else prefixLength = dumpStream.readUnary();
				final int suffixLength = dumpStream.readUnary();
				s.delete(prefixLength, s.length());
				s.length(prefixLength + suffixLength);
				for (int i = 0; i < suffixLength; i++)
					s.charAt(i + prefixLength, symbol2char[decoder.decode(dumpStream)]);
				index++;
				return s;
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/** Returns an iterator over the map.
	 *
	 * <P>The iterator returned by this method scans directly the dump stream.
	 *
	 * <P>Note that the returned iterator uses <em>the same stream</em> as all get methods. Calling such methods while
	 * the iterator is being used will produce an {@link IllegalStateException}.
	 *
	 * @return an iterator over the map that just scans the dump stream.
	 */

	public ObjectIterator<CharSequence> iterator() {
		return new DumpStreamIterator();
	}

	@Override
	public long size64() {
		return size;
	}

	private void writeObject(final ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		if (selfContained) {
			final FileInputStream fis = new FileInputStream(tempDumpStreamFilename);
			ByteStreams.copy(fis, s);
			fis.close();
		}
	}

	private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		if (selfContained) {
			final File temp = File.createTempFile(this.getClass().getName(), ".dump");
			temp.deleteOnExit();
			tempDumpStreamFilename = temp.toString();
			// TODO: propose Jakarta CopyUtils extension with length control and refactor.
			final FileOutputStream fos = new FileOutputStream(temp);
			final byte[] b = new byte[64 * 1024];
			int len;
			while((len = s.read(b)) >= 0) fos.write(b, 0, len);			fos.close();
			dumpStream = new InputBitStream(temp, (int)(blockSize / 8));
		}
	}

	@SuppressWarnings("unchecked")
	public static void main(final String[] arg) throws ClassNotFoundException, IOException, JSAPException, SecurityException, NoSuchMethodException {

		final SimpleJSAP jsap = new SimpleJSAP(ImmutableExternalPrefixMap.class.getName(), "Builds an external prefix map reading from standard input a newline-separated list of sorted terms or a serialised term list. If the dump stream name is not specified, the map will be self-contained.\n\n" + "Note that if you read terms from stdin or from a serialized object all terms will have to be loaded in memory.", new Parameter[] {
				new FlaggedOption("blockSize", JSAP.INTSIZE_PARSER, (STD_BLOCK_SIZE / 1024) + "Ki", JSAP.NOT_REQUIRED, 'b', "block-size", "The size of a block in the dump stream."),
				new Switch("serialised", 's', "serialised", "The data source (file or standard input) provides a serialised java.util.List of terms."),
				new Switch("zipped", 'z', "zipped", "The term list is compressed in gzip format."),
				new FlaggedOption("decompressor", JSAP.CLASS_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'd', "decompressor", "Use this extension of InputStream to decompress the terms."),
				new FlaggedOption("termFile", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'o', "offline", "Read terms from this file instead of standard input."),
				new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The term list encoding."),
				new UnflaggedOption("map", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename for the serialised map."),
				new UnflaggedOption("dump", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY, "An optional dump stream (the resulting map will not be self-contained).") });

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		Iterable<? extends CharSequence> termList;

		final String termFile = jsapResult.getString("termFile");
		final Charset encoding = (Charset)jsapResult.getObject("encoding");
		final boolean zipped = jsapResult.getBoolean("zipped");
		Class<? extends InputStream> decompressor = jsapResult.getClass("decompressor");
		final boolean serialised = jsapResult.getBoolean("serialised");

		if (zipped && decompressor != null) throw new IllegalArgumentException("The zipped and decompressor options are incompatible");
		if ((zipped || decompressor != null) && serialised) throw new IllegalArgumentException("The zipped/decompressor and serialised options are incompatible");

		if (zipped) decompressor = GZIPInputStream.class;

		if (serialised) termList = (List<? extends CharSequence>) (termFile != null ? BinIO.loadObject(termFile) : BinIO.loadObject(System.in));
		else if (termFile != null) termList = new FileLinesMutableStringIterable(termFile, encoding, decompressor);
		else {
			final ObjectArrayList<String> list = new ObjectArrayList<>();
			termList = list;
			FileLinesMutableStringIterable.iterator(System.in, encoding, decompressor).forEachRemaining(s -> list.add(s.toString()));
		}

		BinIO.storeObject(new ImmutableExternalPrefixMap(termList, jsapResult.getInt("blockSize"), jsapResult.getString("dump")), jsapResult.getString("map"));
	}
}
