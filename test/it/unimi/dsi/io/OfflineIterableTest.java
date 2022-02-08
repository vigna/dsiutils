/*
 * DSI utilities
 *
 * Copyright (C) 2010-2022 Sebastiano Vigna
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

package it.unimi.dsi.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.lang.MutableString;

public class OfflineIterableTest {
	public void doIt(final String[] strings) throws IOException {
		final OfflineIterable.Serializer<MutableString,MutableString> stringSerializer = new OfflineIterable.Serializer<MutableString,MutableString>() {
			@Override
			public void read(final DataInput dis, final MutableString x) throws IOException {
				x.readSelfDelimUTF8((InputStream)dis);
			}
			@Override
			public void write(final MutableString x, final DataOutput dos) throws IOException {
				x.writeSelfDelimUTF8((OutputStream)dos);
			}
		};
		final OfflineIterable<MutableString,MutableString> stringIterable = new OfflineIterable<>(stringSerializer, new MutableString());
		for (final String s: strings)
			stringIterable.add(new MutableString(s));
		ObjectIterator<String> shouldBe = ObjectIterators.wrap(strings);
		for (final MutableString m: stringIterable)
			assertEquals(new MutableString(shouldBe.next()), m);
		assertFalse(shouldBe.hasNext());

		// Let's do it again.
		stringIterable.clear();
		for (final String s: strings)
			stringIterable.add(new MutableString(s));
		shouldBe = ObjectIterators.wrap(strings);
		for (final MutableString m: stringIterable)
			assertEquals(new MutableString(shouldBe.next()), m);
		assertFalse(shouldBe.hasNext());

		stringIterable.close();
		stringIterable.close(); // Twice, to test for safety
	}

	@Test
	public void testSimple() throws IOException {
		doIt(new String[] { "this", "is", "a", "test" });
	}

	@Test
	public void testEmpty() throws IOException {
		doIt(new String[0]);
	}
}
