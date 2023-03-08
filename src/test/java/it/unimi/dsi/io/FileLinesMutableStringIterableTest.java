/*
 * DSI utilities
 *
 * Copyright (C) 2020-2023 Sebastiano Vigna
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import com.google.common.base.Charsets;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBigLists;
import it.unimi.dsi.io.FileLinesMutableStringIterable.FileLinesIterator;

public class FileLinesMutableStringIterableTest {

	@Test
	public void test() throws IOException, SecurityException {
		final File file = File.createTempFile(FastBufferedReaderTest.class.getSimpleName(), "tmp");
		file.deleteOnExit();
		final List<String> l = new ObjectArrayList<>(new String[] { "ciao", "mamma", "guarda" });
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), Charsets.US_ASCII);
		for (final String s : l) outputStreamWriter.append(s + "\n");
		outputStreamWriter.close();

		final FileLinesMutableStringIterable fileLinesIterable = new FileLinesMutableStringIterable(file.toString(), Charsets.US_ASCII);
		assertEquals(l.size(), fileLinesIterable.size64());
		assertEquals(l.size(), fileLinesIterable.size64());
		final FileLinesIterator iterator = fileLinesIterable.iterator();
		final Iterator<String> listIterator = l.iterator();
		assertEquals(listIterator.next(), iterator.next().toString());
		assertEquals(listIterator.next(), iterator.next().toString());
		assertTrue(iterator.hasNext());
		assertTrue(iterator.hasNext());
		assertEquals(listIterator.next(), iterator.next().toString());
		assertFalse(iterator.hasNext());
		assertFalse(iterator.hasNext());
		assertEquals(l.size(), fileLinesIterable.size64());

		assertEquals(l, fileLinesIterable.allLines());
		assertEquals(ObjectBigLists.asBigList(new ObjectArrayList<>(l)), fileLinesIterable.allLinesBig());
	}

	@Test
	public void testZipped() throws IOException, NoSuchMethodException, SecurityException {
		final File file = File.createTempFile(FastBufferedReaderTest.class.getSimpleName(), "tmp");
		file.deleteOnExit();
		final List<String> l = new ObjectArrayList<>(new String[] { "ciao", "mamma", "guarda" });
		final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(file));
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(gzipOutputStream, Charsets.US_ASCII);
		for(final String s: l) outputStreamWriter.append(s + "\n");
		outputStreamWriter.close();

		final FileLinesMutableStringIterable fileLinesIterable = new FileLinesMutableStringIterable(file.toString(), Charsets.US_ASCII, GZIPInputStream.class);
		assertEquals(l.size(), fileLinesIterable.size64());
		assertEquals(l.size(), fileLinesIterable.size64());
		final FileLinesIterator iterator = fileLinesIterable.iterator();
		final Iterator<String> listIterator = l.iterator();
		assertEquals(listIterator.next(), iterator.next().toString());
		assertEquals(listIterator.next(), iterator.next().toString());
		assertTrue(iterator.hasNext());
		assertTrue(iterator.hasNext());
		assertEquals(listIterator.next(), iterator.next().toString());
		assertFalse(iterator.hasNext());
		assertFalse(iterator.hasNext());
		assertEquals(l.size(), fileLinesIterable.size64());

		assertEquals(l, fileLinesIterable.allLines());
		assertEquals(ObjectBigLists.asBigList(new ObjectArrayList<>(l)), fileLinesIterable.allLinesBig());
	}
}
