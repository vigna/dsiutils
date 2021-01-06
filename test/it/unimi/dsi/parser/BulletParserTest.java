/*
 * DSI utilities
 *
 * Copyright (C) 2010-2021 Sebastiano Vigna
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

package it.unimi.dsi.parser;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.ByteStreams;

import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.parser.callback.Callback;
import it.unimi.dsi.parser.callback.DefaultCallback;

@Deprecated
@Ignore
public class BulletParserTest {

	@Test
	public void testParser() throws FileNotFoundException, IOException {
		final char[] text = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(ByteStreams.toByteArray(this.getClass().getResourceAsStream("test.data")))).toString().toCharArray();

		final Callback mockCallback = (Callback)Proxy.newProxyInstance(Callback.class.getClassLoader(), new Class<?>[] {
				Callback.class }, new InvocationHandler() {
					int call = 0;

					String[] methods = { "configure", "startDocument", "endDocument" };

					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
						if (call < methods.length)
							assertEquals(method.getName(), methods[call++]);
						return Boolean.TRUE;
					}
				});

		new BulletParser().setCallback(mockCallback).parse(text, 0, text.length);
	}

	private final static class VisibleBulletParser extends BulletParser {
		@Override
		public int scanEntity(final char[] a, final int offset, final int length, final boolean loose, final MutableString entity) {
			return super.scanEntity(a, offset, length, loose, entity);
		}
	}

	@Test
	public void testScanEntityAtEndOfArray() {
		final VisibleBulletParser parser = new VisibleBulletParser();

		char[] test = "&test".toCharArray();
		assertEquals(-1, parser.scanEntity(test, 0, test.length, false, new MutableString()));
		assertEquals(-1, parser.scanEntity(test, 0, test.length, true, new MutableString()));
		test = "&apos".toCharArray();
		assertEquals(-1, parser.scanEntity(test, 0, test.length, false, new MutableString()));
		assertEquals(5, parser.scanEntity(test, 0, test.length, true, new MutableString()));
	}

	@Test
	public void testCdata() {
		final BulletParser parser = new BulletParser();
		final Callback callback = new DefaultCallback() {
			@Override
			public boolean cdata(final Element element, final char[] text, final int offset, final int length) {
				assertEquals("Test > 0", new String(text, offset, length));
				return true;
			}

		};
		parser.setCallback(callback);
		parser.parseCDATA(true);
		parser.parse("<tag><![CDATA[Test > 0]]></tag>".toCharArray());
		parser.parse("<tag><![CDATA[Test > 0".toCharArray());
	}
}
