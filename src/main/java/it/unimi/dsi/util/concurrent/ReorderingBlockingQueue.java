/*
 * DSI utilities
 *
 * Copyright (C) 2017-2023 Sebastiano Vigna
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

package it.unimi.dsi.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import it.unimi.dsi.fastutil.HashCommon;

/** A blocking queue holding a fixed amount of <em>timestamped</em> items. A typical use
 * case is that of multiple threads analyzing an input divided in record, one record per
 * thread, and generating some output that must be written preserving the input order. The
 * threads {@linkplain #put(Object, long) enqueue} their output to an instance of this class,
 * and a flushing thread {@linkplain #take() dequeues} it in input order.
 *
 * <p>The {@link #put(Object, long)}
 * must be called with an object and a timestamp. Timestamps must be a contiguous interval of the
 * natural numbers starting at zero, and objects will be returned in timestamp order. Failure to
 * comply with the contract (i.e., missing timestamps) will cause the queue to block forever.
 *
 * <p>{@link #put(Object, long)} might block if there is not enough space to keep track of the
 * object (i.e., if its timestamp is too far in time w.r.t. the timestamp that would be
 * returned next by the queue). {@link #take()} might block if the object with the next
 * timestamp has not been {@link #put(Object, long)} yet.
 *
 * <p>The implementation is based on a circular, fixed-size buffer, so
 * all methods of this class complete in constant time.
 */
public class ReorderingBlockingQueue<E> {
    /** The backing array. */
    private final Object[] a;
    /** The length of {@link #a} minus one, cached. */
    private final int mask;
    /** The current position into {@link #a} (the position of the next object to be returned). */
    private int start;
    /** The timestamp of the next object to be returned. */
    private long timeStamp;
    /** The number of elements in the queue. */
    private int count;
    /** The main lock. */
    private final ReentrantLock lock;
    /** Condition for waiting takes. */
    private final Condition nextObjectReady;
    /** Condition for waiting puts. */
    private final Condition newSpaceAvailable;

    /** Creates a {@code ReorderingBlockingQueue} with the given fixed
     * capacity.
     *
     * @param capacity the capacity of this queue (will be rounded to the next power of two).
     */
	public ReorderingBlockingQueue(final int capacity) {
		if (capacity <= 0) throw new IllegalArgumentException();
		a = new Object[HashCommon.nextPowerOfTwo(capacity)];
		mask = a.length - 1;
		lock = new ReentrantLock(false);
		nextObjectReady = lock.newCondition();
		newSpaceAvailable = lock.newCondition();
	}

    /** Inserts an element with given timestamp, waiting for space to become available
     * if the timestamp of the element minus the current timestamp of the queue exceeds
     * the queue capacity.
     *
     * @param e an element.
     * @param timeStamp the timestamp of {@code e}.
     * @throws NullPointerException if {@code e} is null;
     */
	public void put(final E e, final long timeStamp) throws InterruptedException {
		if (e == null) throw new NullPointerException();
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			// mask is a.length - 1
			while(timeStamp - this.timeStamp > mask) newSpaceAvailable.await();
			final int timeOffset = (int)(timeStamp - this.timeStamp);
			assert a[start + timeOffset & mask] == null : a[start + timeOffset & mask];
			a[start + timeOffset & mask] = e;
			++count;
			if (timeOffset == 0) nextObjectReady.signal();
		}
		finally {
			lock.unlock();
		}
	}

    /** Returns the element with the next timestamp, waiting until it is available.
     *
     * <p>Note that because of the reordering semantics, an invocation of this method
     * on a {@linkplain #isEmpty() nonempty} queue might block nonetheless.
     *
     * @return the element with the next timestamp.
     */
	public E take() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (a[start] == null) nextObjectReady.await();
			@SuppressWarnings("unchecked")
			final E x = (E)a[start];
			a[start] = null;
			start = start + 1 & mask;
			--count;
			timeStamp++;
			newSpaceAvailable.signalAll();
			return x;
		}
		finally {
			lock.unlock();
		}
	}

    /** Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     * @see #isEmpty()
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    /** Returns whether this queue is empty.
     *
     * @return whether this queue is empty.
     */
    public boolean isEmpty() {
    	return size() == 0;
    }
}
