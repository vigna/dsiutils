/*
 * DSI utilities
 *
 * Copyright (C) 2012-2023 Sebastiano Vigna
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

package it.unimi.dsi.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.logging.ProgressLogger;

public class InputBitStreamSpeedTest  {

	private InputBitStreamSpeedTest() {}

    public static void main(final String[] arg) throws IOException {
		int k;
		final int n = Integer.parseInt(arg[0]);
		int i;
		final java.util.Random r = new java.util.Random();
		final ProgressLogger pl = new ProgressLogger();
		final int data1[] = new int[1000000];
		final int data2[] = new int[1000000];
		i = 1000000;
		while(i-- != 0) data2[i] = (data1[i] = r.nextInt(100)) + 1;


		k = 10;
		while(k-- != 0) {

			i = n;
			pl.start();
			final OutputBitStream bos = new OutputBitStream(new FileOutputStream("test "), 16*1024);
			while(i-- != 0) bos.writeGamma(data1[i % 1000000]);
			bos.close();
			pl.done();

			System.err.println("Written " + n + " integers on OutputBitStream in " + pl.millis() + " ms (" + (1000.0 * n) / pl.millis() + " int/s)");


			pl.start();
			final InputBitStream bis = new InputBitStream(new FileInputStream("test "), 16*1024);
			i = n;
			while(i-- != 0) bis.readGamma();
			bis.close();
			pl.stop();

			System.err.println("Read " + n + " integers from InputBitStream in " + pl.millis() + " ms (" + (1000.0 * n) / pl.millis() + " int/s)");

		}

/*		k = 10;
		while(k-- != 0) {

			i = n;
			pl.reset();
			pl.start();
			BitOutputStream bos = new BitOutputStream(new FileOutputStream("test "));
			while(i-- != 0) bos.writeGamma(data2[i % 1000000]);
			bos.close();
			pl.stop();

			System.err.println("Written " + n + " integers on BitOutputStream in " + pl.millis() + " ms (" + (1000.0 * n) / pl.millis() + " int/s)");


			pl.reset();
			pl.start();
			BitInputStream bis = new BitInputStream(new FileInputStream("test "));
			i = n;
			while(i-- != 0) bis.readGamma();
			bis.close();
			pl.stop();

			System.err.println("Read " + n + " integers from BitInputStream in " + pl.millis() + " ms (" + (1000.0 * n) / pl.millis() + " int/s)");

		}*/
    }


}

