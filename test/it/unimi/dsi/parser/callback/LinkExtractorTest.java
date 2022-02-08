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

package it.unimi.dsi.parser.callback;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.ByteStreams;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.parser.BulletParser;

@Deprecated
@Ignore
public class LinkExtractorTest {

	@Test
	public void testExtractor() throws IOException {
		final char[] text = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(ByteStreams.toByteArray(this.getClass().getResourceAsStream("LinkExtractorTest.data")))).toString().toCharArray();

		final BulletParser parser = new BulletParser();
		final LinkExtractor linkExtractor = new LinkExtractor();
		parser.setCallback(linkExtractor);
		parser.parse(text);

		testExtractorResults(linkExtractor);
	}

	private void testExtractorResults(final LinkExtractor linkExtractor) {
		assertEquals(new ObjectLinkedOpenHashSet<>(new String[] { "manual.css", "http://link.com/", "http://anchor.com/", "http://badanchor.com/" }), linkExtractor.urls);
		assertEquals("http://base.com/", linkExtractor.base());
		assertEquals("http://refresh.com/", linkExtractor.metaRefresh());
		assertEquals("http://location.com/", linkExtractor.metaLocation());
	}
}
