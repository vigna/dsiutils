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

package it.unimi.dsi.parser.callback;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.parser.Attribute;
import it.unimi.dsi.parser.BulletParser;
import it.unimi.dsi.parser.Element;

/**
 * A builder for composed callbacks.
 *
 * <P>
 * To compose a series of callbacks, you must first create an instance of this class,
 * {@linkplain #add(Callback) add all required callbacks}, and finally {@linkplain #compose() get
 * the composed callback}, which will invoke (in order) the callbacks.
 *
 * @deprecated This class is obsolete and kept around for backward compatibility only.
 */

@Deprecated
public class ComposedCallbackBuilder {

	/** A sequence of callbacks to be called int turn. */

	private static final class ComposedCallback implements Callback {
		/** The number of callbacks in this composer. */
		final int size;

		/** The callback array. */
		private final Callback[] callback;

		/** An array of boolean representing continuation of the corresponding callback in {@link #callback}. */
		private final boolean[] cont;

		private final ObjectArrayList<Callback> callbacks;

		private ComposedCallback(final ObjectArrayList<Callback> callbacks) {
			super();
			this.callbacks = callbacks;
			this.size = callbacks.size();
			this.cont = new boolean[size];
			this.callback = new Callback[size];
			this.callbacks.toArray(callback);
		}

		@Override
		public void configure(final BulletParser parser) {
			for(int i = 0; i < size; i++) callback[i].configure(parser);
		}

		@Override
		public void startDocument() {
			for(int i = 0; i < size; i++) {
				callback[i].startDocument();
				cont[i] = true;
			}
		}

		@Override
		public boolean startElement(final Element tag, final Map<Attribute,MutableString> attrList) {
			boolean retValue = false;
			for(int i = 0; i < size; i++) {
				if (cont[i] && ! callback[i].startElement(tag, attrList)) cont[i] = false;
				retValue |= cont[i];
			}
			return retValue;
		}

		@Override
		public boolean endElement(final Element tag) {
			boolean retValue = false;
			for(int i = 0; i < size; i++) {
				if (cont[i] && ! callback[i].endElement(tag)) cont[i] = false;
				retValue |= cont[i];
			}
			return retValue;
		}

		@Override
		public boolean characters(final char[] text, final int offset, final int length, final boolean flowBroken) {
			boolean retValue = false;
			for(int i = 0; i < size; i++) {
				if (cont[i] && ! callback[i].characters(text, offset, length, flowBroken)) cont[i] = false;
				retValue |= cont[i];
			}
			return retValue;
		}

		@Override
		public boolean cdata(final Element element, final char[] text, final int offset, final int length) {
			boolean retValue = false;
			for(int i = 0; i < size; i++) {
				if (cont[i] && ! callback[i].cdata(element, text, offset, length)) cont[i] = false;
				retValue |= cont[i];
			}
			return retValue;
		}

		@Override
		public void endDocument() {
			for(int i = 0; i < size; i++) callback[i].endDocument();
		}
	}

	/** The current list of callbacks. */
	private final ObjectArrayList<Callback> callbacks = ObjectArrayList.wrap(Callback.EMPTY_CALLBACK_ARRAY);

	/** Creates a new, empty callback composer. */
	public ComposedCallbackBuilder()  {}

	/** Adds a new callback to this builder at a specified position.
	 *
	 * @param position a position in the current callback list.
	 * @param callback a callback.
	 */
	public void add(final int position, final Callback callback) {
		callbacks.add(position, callback);
	}

	/** Adds a new callback to this builder.
	 *
	 * @param callback a callback.
	 */

	public void add(final Callback callback) {
		callbacks.add(callback);
	}

	/** Checks whether this callback builder is empty.
	 *
	 * @return true if this callback builder is empty.
	 */

	public boolean isEmpty() {
		return callbacks.isEmpty();
	}

	/** Returns the number of callbacks in this builder.
	 *
	 * @return the number of callbacks in this composer.
	 */
	public int size() {
		return callbacks.size();
	}

	/** Returns the composed callback produced by this builder.
	 *
	 * @return a composed callback.
	 */
	public Callback compose() {
		if (isEmpty()) return DefaultCallback.getInstance();
		if (size() == 1) return callbacks.get(0);

		return new ComposedCallback(callbacks);
	}
}
