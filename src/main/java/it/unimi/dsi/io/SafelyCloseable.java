/*
 * DSI utilities
 *
 * Copyright (C) 2006-2023 Sebastiano Vigna
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

import java.io.Closeable;

/** A marker interface for a closeable resource that implements safety measures to
 * make resource tracking easier.
 *
 * <p>Classes implementing this interface must provide a <em>safety-net finaliser</em>&mdash;a
 * finaliser that closes the resource and logs that resource should have been closed.
 *
 * <p>When the implementing class is abstract, concrete subclasses <strong>must</strong>
 * call <code>super.close()</code> in their own {@link Closeable#close()} method
 * to let the abstract class track correctly the resource. Moreover,
 * they <strong>must</strong> run <code>super.finalize()</code> in
 * their own finaliser (if any), as finalisation chaining is not automatic.
 *
 * <p>Note that if a concrete subclass implements <code>readResolve()</code>, it must
 * call <code>super.close()</code>, or actually return <code>this</code> (i.e., the deserialised
 * instance); otherwise, a spurious log could be generated when the deserialised instance is collected.
 *
 * @author Sebastiano Vigna
 * @since 1.1
 */

public interface SafelyCloseable extends Closeable {}
