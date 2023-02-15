/*
 * DSI utilities
 *
 * Copyright (C) 2014-2023 Sebastiano Vigna
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.util.SplitMix64Random;


public class ByteDiskQueueTest {

	@Test
	public void testSingleEnqueueDequeue() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		final ByteDiskQueue q = ByteDiskQueue.createNew(queue, 128, true);
		q.enqueue((byte)1);
		assertEquals(1, q.dequeue());
		q.close();
	}

	@Test
	public void testSomeEnqueueDequeue() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		final ByteDiskQueue q = ByteDiskQueue.createNew(queue, 128, true);
		for(int s = 1; s < 1 << 12; s *= 2) {
			q.clear();
			for(int i = 0; i < s; i++) q.enqueue((byte)(i + 64));
			for(int i = 0; i < s; i++) assertEquals((byte)(i + 64), q.dequeue());
		}
		q.close();
	}

	@Test
	public void testSomeEnqueueIntDequeueInt() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		final ByteDiskQueue q = ByteDiskQueue.createNew(queue, 128, true);
		for(int s = 1; s < 1 << 20; s *= 2) {
			q.clear();
			final SplitMix64Random random = new SplitMix64Random(0);
			for(int i = 0; i < s; i++) {
				q.enqueueInt(random.nextInt() & -1 >>> 1);
				q.enqueueInt(random.nextInt() & -1 >>> 16);
			}
			random.setSeed(0);
			for(int i = 0; i < s; i++) {
				assertEquals(random.nextInt() & -1 >>> 1, q.dequeueInt());
				assertEquals(random.nextInt() & -1 >>> 16, q.dequeueInt());
			}
		}
		q.close();

		queue.delete();
	}
	@Test
	public void testSingleArrayEnqueueDequeue() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		final ByteDiskQueue q = ByteDiskQueue.createNew(queue, 128, true);
		final byte[] a = new byte[] { 1 };
		q.enqueue(a);
		final byte [] b = new byte[1];
		q.dequeue(b);
		assertArrayEquals(a, b);
		q.close();
	}

	@Test
	public void testSomeArrayEnqueueDequeue() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();

		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, true);
			for(int s = 1; s < 1 << 13; s *= 2) {
				q.clear();
				final SplitMix64Random random = new SplitMix64Random(0);
				final ArrayList<byte[]> arrays = new ArrayList<>();
				for(int i = 0; i < s; i++) {
					final byte[] a = new byte[random.nextInt(3 * bufferSize / 2)];
					for(int j = a.length; j-- != 0;) a[j] = (byte)j;
					arrays.add(a);
				}
				for(int i = 0; i < s; i++) q.enqueue(arrays.get(i));
				for(int i = 0; i < s; i++) {
					final byte[] a = new byte[arrays.get(i).length];
					q.dequeue(a);
					assertArrayEquals(arrays.get(i), a);
				}
			}
			q.close();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testQueue() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		int bufferSize = 64;
		ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize , true);
		final ByteArrayList l = new ByteArrayList();
		final SplitMix64Random random = new SplitMix64Random(0);
		double threshold = .9;

		for (int r = 10000; r-- != 0;) {
			assertEquals(l.size(), q.size64());
			switch(random.nextInt(7)) {
			case 0:
				// Invert queue tendency
				if (random.nextFloat() < .01) threshold = 1 - threshold;
				break;
			case 1:
				if (random.nextFloat() < threshold) {
					final byte nextByte = (byte)random.nextInt();
					q.enqueue(nextByte);
					l.add(nextByte);
				}
				else {
					assertEquals(Boolean.valueOf(q.isEmpty()), Boolean.valueOf(l.isEmpty()));
					if (! q.isEmpty()) assertEquals(q.dequeue(), l.removeByte(0));
				}
				break;
			case 2:
				if (random.nextFloat() < threshold) {
					final byte[] a = new byte[random.nextInt(256)];
					for(int i = 0; i < a.length; i++) {
						a[i] = (byte)i;
						l.add((byte)i);
					}
					q.enqueue(a);
				}
				else {
					final byte[] a = new byte[(int)Math.min(q.size64(), random.nextInt(256))];
					q.dequeue(a);
					for (final byte element : a) assertEquals(element, l.removeByte(0));
				}
				break;
			case 3:
				if (random.nextFloat() < .001) {
					q.clear();
					l.clear();
				}
				else if (random.nextFloat() < .01) {
					q.suspend();
				}
				else if (random.nextFloat() < .01) {
					q.trim();
				}
				break;
			case 4:
				q.suspend();
				break;
			case 5:
				if (random.nextFloat() < 0.01) q.enlargeBuffer(bufferSize = Math.min(bufferSize + random.nextInt(bufferSize), 1024 * 1024));
				break;
			case 6:
				if (random.nextFloat() < 0.005) {
					q.freeze();
					// Let's try to unmap to avoid out-of-memory errors
					System.gc();
					System.gc();
					q = ByteDiskQueue.createFromFile(queue, bufferSize, true);
				}
				break;
			}
		}

		q.close();
		new File("testdiskqueue.serialised").delete();
	}

	@Test
	public void testDequeueHoleBeforeStart() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, false);
			q.enqueue((byte)1);
			// Move start to the end
			for(int i = bufferSize -2; i-- != 0;) {
				q.enqueue((byte)1);
				assertEquals(1, q.dequeue());
			}
			for(int i = 2 * bufferSize; i-- != 0;) q.enqueue((byte)i);
			assertEquals(1, q.dequeue());
			for(int i = 2 * bufferSize; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}

	}

	@Test
	public void testDumpTailHoleAfterEnd() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, false);
			q.enqueue((byte)1);
			q.enqueue((byte)1);
			assertEquals(1, q.dequeue());
			for(int i = 2 * bufferSize; i-- != 0;) q.enqueue((byte)i);
			assertEquals(1, q.dequeue());
			for(int i = 2 * bufferSize; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}
	}

	@Test
	public void testEnlarge() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		// start < end, no hole
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, false);
			q.enlargeBuffer(bufferSize); // To increase coverage
			q.enlargeBuffer(bufferSize * 2); // To increase coverage
			q.enlargeBuffer(bufferSize); // To increase coverage
			q.enqueue((byte)1);
			q.enqueue((byte)1);
			assertEquals(1, q.dequeue());
			for(int i = bufferSize / 2; i-- != 0;) q.enqueue((byte)i);
			q.enlargeBuffer(bufferSize * 2);
			assertEquals(1, q.dequeue());
			for(int i = bufferSize / 2; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}

		// start > end, no hole
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, false);
			q.enqueue((byte)1);
			// Move start to the end
			for(int i = bufferSize -2; i-- != 0;) {
				q.enqueue((byte)1);
				assertEquals(1, q.dequeue());
			}
			for(int i = bufferSize / 2; i-- != 0;) q.enqueue((byte)i);
			q.enlargeBuffer(bufferSize * 2);
			assertEquals(1, q.dequeue());
			for(int i = bufferSize / 2; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}

		// start < end, hole
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, true);
			q.enqueue((byte)1);
			q.enqueue((byte)1);
			assertEquals(1, q.dequeue());
			for(int i = 2 * bufferSize; i-- != 0;) q.enqueue((byte)i);
			q.enlargeBuffer(bufferSize * 2);
			assertEquals(1, q.dequeue());
			for(int i = 2 * bufferSize; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}

		// start > end, hole < start
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, true);
			q.enqueue((byte)1);
			// Move start to the end
			for(int i = bufferSize - 2; i-- != 0;) {
				q.enqueue((byte)1);
				assertEquals(1, q.dequeue());
			}
			for(int i = 2 * bufferSize; i-- != 0;) q.enqueue((byte)i);
			q.enlargeBuffer(bufferSize * 2);
			assertEquals(1, q.dequeue());
			for(int i = 2 * bufferSize; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}

		// start > end, hole >= start
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			final ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, true);
			q.enqueue((byte)1);
			// Move start to the end
			for(int i = bufferSize / 2 - 1; i-- != 0;) {
				q.enqueue((byte)1);
				assertEquals(1, q.dequeue());
			}
			for(int i = bufferSize; i-- != 0;) q.enqueue((byte)i);
			q.enlargeBuffer(bufferSize * 2);
			assertEquals(1, q.dequeue());
			for(int i = bufferSize; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}
}

	@Test
	public void testFreezeHoleEqualsEnd() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		for(int bufferSize = 8; bufferSize < 256; bufferSize *=2) {
			ByteDiskQueue q = ByteDiskQueue.createNew(queue, bufferSize, false);
			for(int i = 2 * bufferSize; i-- != 0;) q.enqueue((byte)i);
			q.freeze();
			q = ByteDiskQueue.createFromFile(queue, bufferSize, false);
			for(int i = 2 * bufferSize; i-- != 0;) assertEquals((byte)i, q.dequeue());
			q.close();
		}
	}


	@Ignore
	@Test
	public void testLarge() throws IOException {
		final File queue = File.createTempFile(this.getClass().getName(), ".queue");
		queue.deleteOnExit();
		final ByteDiskQueue q = ByteDiskQueue.createNew(queue, 128, true);
		final SplitMix64Random random = new SplitMix64Random(0);

		final long n = 3000000005L;
		for(long i = n; i -- != 0;) q.enqueue((byte)random.nextInt(4));
		assertEquals(n, q.size64());

		final SplitMix64Random random2 = new SplitMix64Random(0);
		for(long i = n; i -- != 0;) assertEquals((byte)random2.nextInt(4), q.dequeue());

		q.close();
	}

}
