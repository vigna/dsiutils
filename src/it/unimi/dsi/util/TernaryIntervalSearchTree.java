/*
 * DSI utilities
 *
 * Copyright (C) 2005-2022 Sebastiano Vigna
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

package it.unimi.dsi.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ForNameStringParser;

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;

/** Ternary interval search trees.
 *
 * <p><em>Ternary search trees</em> are a data structure used to store words over an alphabet; they are
 * a useful alternatives to tries when the alphabet is large.
 *
 * <p>Ternary <em>interval</em> search trees have the additional properties of being able
 * to locate quickly intervals of words extending a given prefix (where &ldquo;quickly&rdquo; means
 * that no more successful character comparisons than the prefix length are performed). They do so
 * by storing at each node the number of words covered by that node.
 *
 * <p>This implementation exposes a number of interfaces: in particular, the set of words is
 * seen as a lexicographically ordered {@link it.unimi.dsi.fastutil.objects.ObjectList}.
 *
 * <p>This class is mutable, but for the time it implements only {@link #add(CharSequence)}. Words cannot
 * be removed.
 */

public class TernaryIntervalSearchTree extends AbstractPrefixMap implements Serializable {
	private static final long serialVersionUID = 1L;

	/** A node of the tree. */
	private final static class Node implements Serializable {
		private static final long serialVersionUID = 1L;
		/** A pointer to the left subtree. */
		public Node left;
		/** A pointer to the middle subtree. */
		public Node middle;
		/** A pointer to the right subtree. */
		public Node right;
		/** The nonempty path compressed at this node. */
		public char[] path;
		/** Whether this node represents a word. */
		public boolean isWord;
		/** The number of words covered by this node (including the word possibly represented by this node). */
		public int numNodes;

		/** Creates a new node containing a path specified by a character-sequence fragment.
		 *
		 * @param s a character sequence contaning the path of the node.
		 * @param offset the starting character of the path.
		 * @param length the length of the path.
		 * @param isWord whether this node represents a word.
		 * @param numNodes the number of words covered by this node.
		 */
		public Node(final CharSequence s, final int offset, final int length, final boolean isWord, final int numNodes) {
			path = new char[length];
			MutableString.getChars(s, offset, offset + length, path, 0);
			this.isWord = isWord;
			this.numNodes = numNodes;
		}

		/** Creates a new node containing a path specified by a character-array fragment.
		 *
		 * @param a a character array contaning the path of the node.
		 * @param offset the starting character of the path.
		 * @param length the length of the path.
		 * @param isWord whether this node represents a word.
		 * @param numNodes the number of words covered by this node.
		 */
		public Node(final char[] a, final int offset, final int length, final boolean isWord, final int numNodes) {
			path = new char[length];
			System.arraycopy(a, offset, path, 0, length);
			this.isWord = isWord;
			this.numNodes = numNodes;
		}

		/** Removes a prefix from the path of this node.
		 *
		 * @param length the length of the prefix to be removed
		 */

		public void removePathPrefix(final int length) {
			final char[] a = new char[path.length - length];
			System.arraycopy(path, length, a, 0, a.length);
			path = a;
		}
	}

	/** The root of the tree. */
	private Node root;

	/** The number of nodes in the tree. */
	private int size;

	/** Creates a new empty ternary search tree. */
	public TernaryIntervalSearchTree() {
		defRetValue = -1;
	}

	/** Creates a new empty ternary search tree and populates it with a given collection of character sequences.
	 *
	 * @param c a collection of character sequences.
	 * */
	public TernaryIntervalSearchTree(final Collection<? extends CharSequence> c) {
		int n = c.size();
		final Iterator<? extends CharSequence> i = c.iterator();
		while(n-- != 0) add(i.next());
		defRetValue = -1;
	}


	@Override
	protected Interval getInterval(final CharSequence s) {
		final int l = s.length();

		Node e = root;
		int i;
		int offset = 0;
		int wordsAtLeft = 0;
		char c;
		char[] path;

		while(e != null) {
			path = e.path;
			for(i = 0; i < path.length - 1 && offset + i < l && s.charAt(offset + i) == path[i]; i++);
			if (offset + i == l) return Interval.valueOf(wordsAtLeft, wordsAtLeft + e.numNodes - 1);
			if (i < path.length - 1) return Intervals.EMPTY_INTERVAL;
			offset += i;

			c = s.charAt(offset);
			if (c < path[i]) e = e.left;
			else if (c > path[i]) {
				if (e.left != null) wordsAtLeft += e.left.numNodes;
				if (e.middle != null) wordsAtLeft += e.middle.numNodes;
				if (e.isWord) wordsAtLeft++;
				e = e.right;
			}
			else {
				offset++;
				if (e.left != null) wordsAtLeft += e.left.numNodes;
				if (offset == l) return Interval.valueOf(wordsAtLeft, wordsAtLeft + (e.isWord ? 1 : 0) + (e.middle == null ? 0 : e.middle.numNodes) - 1);
				if (e.isWord) wordsAtLeft++;
				e = e.middle;
			}
		}

		return Intervals.EMPTY_INTERVAL;
	}


	public Interval getApproximatedInterval(final CharSequence s) {
		final int l = s.length();

		Node e = root;
		int i;
		int offset = 0;
		int wordsAtLeft = 0;
		char c;
		char[] path;

		while(e != null) {
			path = e.path;
			for(i = 0; i < path.length - 1 && offset + i < l && s.charAt(offset + i) == path[i]; i++);
			if (offset + i == l) {
				// Our sequence is a proper prefix of path.
				return wordsAtLeft > 0 ? Interval.valueOf(wordsAtLeft - 1, wordsAtLeft + e.numNodes - 1) : Interval.valueOf(wordsAtLeft, wordsAtLeft + e.numNodes - 1);
			}
			if (i < path.length - 1) {
				// We stopped the loop prematurely.

				if (s.charAt(offset + i) < path[i]) return wordsAtLeft > 0 ? Interval.valueOf(wordsAtLeft -1) : Intervals.EMPTY_INTERVAL;
				else return Interval.valueOf(wordsAtLeft + e.numNodes  - 1);
			}

			offset += i;

			c = s.charAt(offset);
			if (c < path[i]) e = e.left;
			else if (c > path[i]) {
				if (e.left != null) wordsAtLeft += e.left.numNodes;
				if (e.middle != null) wordsAtLeft += e.middle.numNodes;
				if (e.isWord) wordsAtLeft++;
				e = e.right;
			}
			else {
				offset++;
				if (e.left != null) wordsAtLeft += e.left.numNodes;
				if (offset == l) return Interval.valueOf(wordsAtLeft - (e.isWord ? 0 : 1), wordsAtLeft + (e.isWord ? 1 : 0) + (e.middle == null ? 0 : e.middle.numNodes) - 1);
				if (e.isWord) wordsAtLeft++;
				e = e.middle;
			}
		}

		return wordsAtLeft > 0 ? Interval.valueOf(wordsAtLeft - 1) : Intervals.EMPTY_INTERVAL;
	}

	@Override
	protected MutableString getTerm(int index, final MutableString s) {
		Node e = root;

		for(;;) {

			if (e.left != null) {
				if (index < e.left.numNodes) {
					s.append(e.path, 0, e.path.length - 1);
					e = e.left;
					continue;
				}

				index -= e.left.numNodes;
			}

			if (e.isWord) {
				if (index == 0) return s.append(e.path).compact();
				index--;
			}


			if (e.middle != null) {
				if (index < e.middle.numNodes) {
					s.append(e.path);
					e = e.middle;
					continue;
				}

				index -= e.middle.numNodes;
			}

			s.append(e.path, 0, e.path.length - 1);
			e = e.right;
		}
	}

	protected long getIndex(final CharSequence s) {
		final int l = s.length();

		Node e = root;
		int i;
		int offset = 0;
		int wordsAtLeft = 0;
		char c;
		char[] path;

		while(e != null) {
			path = e.path;
			for(i = 0; i < path.length - 1; i++)
				if (offset + i == l || s.charAt(offset + i) != path[i]) return -1;

			offset += i;
			if (offset == l) return -1;

			c = s.charAt(offset);
			if (c < e.path[i]) e = e.left;
			else if (c > e.path[i]) {
				if (e.left != null) wordsAtLeft += e.left.numNodes;
				if (e.middle != null) wordsAtLeft += e.middle.numNodes;
				if (e.isWord) wordsAtLeft++;
				e = e.right;
			}
			else {
				offset++;
				if (e.left != null) wordsAtLeft += e.left.numNodes;
				if (offset == l) return e.isWord ? wordsAtLeft : -1;
				if (e.isWord) wordsAtLeft++;
				e = e.middle;
			}
		}

		return -1;
	}

	@Override
	public boolean containsKey(final Object o) {
		return getIndex((CharSequence)o) != -1;
	}

	@Override
	public long getLong(final Object o) {
		final CharSequence s = (CharSequence)o;
		final long result = getIndex(s);
		return result == -1 ? defRetValue : result;
	}
	/** True if the last {@link #add(CharSequence)} modified the tree. */
	private boolean modified;

	public boolean add(final CharSequence s) {
		modified = false;
		root = addRec(s, 0, s.length(), root);
		return modified;
	}

	/** Inserts the given character sequence, starting at the given position, in the given subtree.
	 *
	 * @param s the character sequence containing the characters to be inserted.
	 * @param offset the first character to be inserted.
	 * @param length the number of characters to be inserted.
	 * @param e the subtree in which the characters should be inserted, or {@code null} if
	 * a new node should be created.
	 * @return the new node at the top of the subtree.
	 */

	private Node addRec(final CharSequence s, final int offset, final int length, final Node e) {

		if (e == null) {
			// We create a new node containing all the characters and return it.
			modified = true;
			size++;
			return new Node(s, offset, length, true, 1);
		}

		/* We start scanning the path contained in the current node, up to
		 * the last character excluded. If we find a mismatch, or if we exhaust our
		 * characters, we must fork this node. */

		char c;
		int i;
		Node n = null;
		final char[] path = e.path;

		for (i = 0; i < path.length - 1; i++) {
			c = s.charAt(offset + i);

			if (c < path[i]) {
				/* We fork on the left, keeping just the first i + 1 characters (this is necessary
				 * as at least one character must be present in every node). The new
				 * node will cover one word more than e.
				 */
				n = new Node(path, 0, i + 1, false, e.numNodes + 1);

				n.middle = e;
				e.removePathPrefix(i + 1);

				n.left = addRec(s, offset + i, length - i, null);
				break;
			}
			else if (c > path[i]) {
				// As before, but on the right.
				n = new Node(path, 0, i + 1, false, e.numNodes + 1);

				n.middle = e;
				e.removePathPrefix(i + 1);

				n.right = addRec(s, offset + i, length - i, null);
				break;
			}
			else {
				if (i == length - 1) {
					/* We exhausted the character sequence. We fork in the middle,
					 * keeping length characters and marking the new node as
					 * containing one work. Again, the new code will cover one word
					 * more than e. */
					n = new Node(s, offset, length, true, e.numNodes + 1);
					n.middle = e;
					e.removePathPrefix(length);
					size++;
					modified = true;
					break;
				}
			}
		}

		if (i < path.length - 1) return n;

		/* We are positioned on the last character of the path. In this case our
		 * behaviour is different, as if we must fork we must not perform any
		 * splitting. Moreover, if we exhaust the characters we either found
		 * the new sequence in the tree, or we just have to mark the node. */

		c = s.charAt(offset + i);

		if (c < path[i]) {
			/** We fork on the left. The number of words under this node will
			 * increase only if the structure is modified. */
			e.left = addRec(s, offset + i, length - i, e.left);
			if (modified) e.numNodes++;
		}
		else if (c > path[i]) {
			e.right = addRec(s, offset + i, length - i, e.right);
			if (modified) e.numNodes++;
		}
		else {
			if (i == length - 1) {
				// This is the node.
				if (modified = !e.isWord) {
					e.numNodes++;
					size++;
				}
				e.isWord = true;
			}
			else {
				// We add a node in the middle, completing the sequence.
				e.middle = addRec(s, offset + i + 1, length - i - 1, e.middle);
				if (modified) e.numNodes++;
			}
		}

		return e;
	}

	@Override
	public int size() {
		return size;
	}

	public static void main(final String[] arg) throws IOException, JSAPException, NoSuchMethodException {

		final SimpleJSAP jsap = new SimpleJSAP(TernaryIntervalSearchTree.class.getName(), "Builds a ternary interval search tree reading from standard input a newline-separated list of terms.",
			new Parameter[] {
				new FlaggedOption("bufferSize", JSAP.INTSIZE_PARSER, "64Ki", JSAP.NOT_REQUIRED, 'b',  "buffer-size", "The size of the I/O buffer used to read terms."),
				new FlaggedOption("encoding", ForNameStringParser.getParser(Charset.class), "UTF-8", JSAP.NOT_REQUIRED, 'e', "encoding", "The term file encoding."),
				new UnflaggedOption("tree", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The filename for the serialised tree.")
		});

		final JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted()) return;

		final TernaryIntervalSearchTree tree = new TernaryIntervalSearchTree();

		final MutableString term = new MutableString();
		final ProgressLogger pl = new ProgressLogger();
		pl.itemsName = "terms";
		@SuppressWarnings("resource")
		final FastBufferedReader terms = new FastBufferedReader(new InputStreamReader(System.in, (Charset)jsapResult.getObject("encoding")), jsapResult.getInt("bufferSize"));

		pl.start("Reading terms...");

		while(terms.readLine(term) != null) {
			pl.update();
			tree.add(term);
		}

		pl.done();

		BinIO.storeObject(tree, jsapResult.getString("tree"));
	}

}
