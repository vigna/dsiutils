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

import java.io.IOException;

import org.apache.commons.math3.distribution.ZipfDistribution;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.util.XoShiRo256PlusRandomGenerator;

public class InputBitStreamSpeedTest  {

	private InputBitStreamSpeedTest() {}

	@SuppressWarnings("resource")
	public static void main(final String[] arg) throws IOException {
		final int n = Integer.parseInt(arg[0]);
		final XoShiRo256PlusRandomGenerator r = new XoShiRo256PlusRandomGenerator(0);
		final ProgressLogger pl = new ProgressLogger();
		final ZipfDistribution zipf = new ZipfDistribution(r, 1_000_000_000, 2);
		final int data[] = new int[n];

		final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		InputBitStream ibs;
		OutputBitStream obs;
		long u = 0;

		for (int k = 10; k-- != 0;) {
			fbaos.reset();
			obs = new OutputBitStream(fbaos);
			pl.start("Writing ɣ...");
			for (final int x : data)
				obs.writeLongGamma(x);
			obs.flush();
			pl.done(n);

			ibs = new InputBitStream(fbaos.array);
			pl.start("Reading ɣ...");
			for (int i = n; i-- != 0;)
				u += ibs.readLongGamma();
			pl.done(n);

			fbaos.reset();
			obs = new OutputBitStream(fbaos);
			pl.start("Writing δ..");
			for (final int x : data)
				obs.writeLongDelta(x);
			obs.flush();
			pl.done(n);

			ibs = new InputBitStream(fbaos.array);
			pl.start("Reading δ...");
			for (int i = n; i-- != 0;)
				u += ibs.readLongDelta();
			pl.done(n);
		}

		if (u == 0)
			System.err.println();
	}

}
