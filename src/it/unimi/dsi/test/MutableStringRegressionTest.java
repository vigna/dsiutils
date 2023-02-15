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



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.util.TextPattern;
import it.unimi.dsi.util.XoRoShiRo128PlusRandomGenerator;


/**
 * A class for torture-testing {@link MutableString}s.
 *
 * @author Paolo Boldi
 * @author Sebastiano Vigna
 * @since 0.3
 *
 */

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MutableStringRegressionTest  {

	private MutableStringRegressionTest() {}

    /** Maximum integer to be generated. */
    private static final int MAXINT = 100;
    /** Maximum short to be generated. */
    private static final int MAXSHORT = 10000;
    /** Minimum/maximum character to be generated. */
    private static final int MINCHAR = 'A', MAXCHAR = 'Z';
	/** About one each <code>SPACE_FREQ</code> characters will be spaces. */
	private static final int SPACE_FREQ = 8;
    /** Maximum length of a char array / String etc. */
    private static final int MAXLENGTH = 100;

    /** Methods declared by StringBuffer. */
    private static Method sbMethod[];
    /** Methods declared by MutableString. */
    private static Method msMethod[];
    /** How many times method <code>sbMethod[i]</code> (<code>msMethod[i]</code>) has been tested. */
    private static int sbTimes[], msTimes[];
    /** How many times method <code>sbMethod[i]</code> (<code>msMethod[i]</code>) has thrown an exception. */
    private static int sbExc[], msExc[];
    /** The Random object used by the test. */
    private static XoRoShiRo128PlusRandomGenerator rand;

    /** A map that sends Classes to Sets of alternative Classes... */
	private static Map<Class,Set<Class>> alternativeType;

    /** Index of the lastly generated method in <code>sbMethod</code> (<code>msMethod</code>). */
    private static int sbIdx, msIdx;
    /** An array of alternative types to be used for {@link StringBuffer}. Usually, this is {@code null}. */
    private static Class alternativeParameterType[];

    static {
		sbMethod = StringBuffer.class.getDeclaredMethods();
		msMethod = MutableString.class.getDeclaredMethods();

		sbTimes = new int[sbMethod.length];
		msTimes = new int[msMethod.length];
		sbExc = new int[sbMethod.length];
		msExc = new int[msMethod.length];

		alternativeType = new HashMap<>();
		alternativeType.put(MutableString.class, new ObjectOpenHashSet<>(new Class[] { String.class, StringBuffer.class }));
		alternativeType.put(String.class, ObjectSets.singleton((Class)StringBuffer.class));
		alternativeType.put(StringBuffer.class, ObjectSets.singleton((Class)String.class));
		alternativeType.put(CharSequence.class, ObjectSets.singleton((Class)String.class));
		alternativeType.put(char.class, ObjectSets.singleton((Class)String.class));

		alternativeParameterType = null;
    }

    private static String a2s(final Object o[]) {
		String res = "[";
		for (int i = 0; i < o.length; i++) res = res + (i>0? "," : "") + (o[i] instanceof char[]? (CharArrayList.wrap((char []) o[i])).toString() : String.valueOf(o[i]));
		return res + "]";
    }

    private static MutableString s2i(final MutableString s) {
		final MutableString res = new MutableString().append("[");
		for (int i = 0; i < s.length(); i++) res.append(i != 0 ? ", " : "").append((int)s.charAt(i));
		return res.append("]");
    }

    /** An array of {@link Set} is given. This method considers all possible arrays where <code>i</code>-th entry is
		an element of the <code>i</code>-th Set, and produces an {@link Iterator} returning such arrays in a fixed
		order, starting from a random position. */
    private static Iterator possibleComb(final Set o[]) {
		final int n = o.length;
		int c = 1;
		for (int i = 0; i < n; i++) c *= o[i].size();
		final int m = c;

		return new Iterator() {
				int emitted = 0, nextEmit = rand.nextInt(m);
				@Override
				public boolean hasNext() {  return emitted < m; }
				@Override
				public void remove() { throw new UnsupportedOperationException(); }
				@Override
				public Object next() {
					if (emitted >= m) throw new NoSuchElementException();
					final Object res[] = new Object[n];
					int residual = nextEmit;
					for (int i = 0; i < n; i++) {
						final int j = residual % o[i].size();
						residual /= o[i].size();
						final Object a[] = o[i].toArray();
						res[i] = a[j];
					}
					emitted++;
					nextEmit = (nextEmit + 1) % m;
					return res;
				}
			};
    }

    /** Extract a method at random that belongs to both classes. Sets <code>msIdx</code>, <code>sbIdx</code>. */
    public static Method randomMethod() {
		Method msm, sbm;
		alternativeParameterType = null;
		do {
			// Generate a method at random from MutableString
			msm = msMethod[rand.nextInt(msMethod.length)];
			if (! Modifier.isPublic(msm.getModifiers())) continue;
			for (msIdx = 0; msIdx < msMethod.length; msIdx++) if (msMethod[msIdx].equals(msm)) break;
			if (msIdx == msMethod.length) {
				throw new IllegalStateException("I was looking for " + msm);
			}
			// Test that the method belongs to both; if so, return it
			try {
				sbm = StringBuffer.class.getDeclaredMethod(msm.getName(), msm.getParameterTypes());
				if (! Modifier.isPublic(sbm.getModifiers())) continue;
				for (sbIdx = 0; sbIdx < sbMethod.length; sbIdx++) if (sbMethod[sbIdx].equals(sbm)) break;
				if (sbIdx == sbMethod.length) {
					throw new IllegalStateException("I was looking for " + sbm);
				}
				return msm;
			} catch (final NoSuchMethodException e) {
				// The method was not found; try to change its parameters in every possible way and see whether
				// it can be matched anyway, modulo a parameter change
				final Class type[] = msm.getParameterTypes();
				final Set<Class> alternat[] = new Set[type.length];
				for (int i = 0; i < type.length; i++) {
					alternat[i] = new HashSet<>(); alternat[i].add(type[i]);
					if (alternativeType.containsKey(type[i]))
						alternat[i].addAll(alternativeType.get(type[i]));
				}
				final Iterator it = possibleComb(alternat);
				while (it.hasNext()) {
					final Object o[] = (Object [])it.next();
					final Class altPar[] = new Class[o.length];
					System.arraycopy(o, 0, altPar, 0, o.length);
					try {
						sbm = StringBuffer.class.getDeclaredMethod(msm.getName(), altPar);
						if (! Modifier.isPublic(sbm.getModifiers())) continue;
					} catch (final NoSuchMethodException e1) { continue; }
					// A method was found! Look for its index...
					for (sbIdx = 0; sbIdx < sbMethod.length; sbIdx++) if (sbMethod[sbIdx].equals(sbm)) break;
					if (sbIdx == sbMethod.length) {
						throw new IllegalStateException("I was looking for " + sbm);
					}
					// Now copy the altPar
					alternativeParameterType = new Class[altPar.length];
					System.arraycopy(altPar, 0, alternativeParameterType, 0, altPar.length);
					//System.err.println("Using " + sbm + " as an alternative to " + msm);
					return msm;
				}
			}
		} while (true);
    }

    /** Generate and return an array of Objects at random suitable for method m. */
    public static Object[] params(final Method m) {
		final Class types[] = m.getParameterTypes();
		final Object res[] = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			if (types[i].getName().equals("byte")) res[i] = Byte.valueOf((byte) rand.nextInt(256));
			else if (types[i].getName().equals("char")) res[i] = Character.valueOf((char) (MINCHAR + rand.nextInt(MAXCHAR-MINCHAR+1)));
			else if (types[i].getName().equals("double")) res[i] = Double.valueOf(rand.nextDouble());
			else if (types[i].getName().equals("float")) res[i] = Float.valueOf(rand.nextFloat());
			else if (types[i].getName().equals("int")) res[i] = Integer.valueOf(rand.nextInt(MAXINT));
			else if (types[i].getName().equals("long")) res[i] = Long.valueOf(rand.nextInt(MAXINT));
			else if (types[i].getName().equals("short")) res[i] = Short.valueOf((short) rand.nextInt(MAXSHORT));
			else if (types[i].getName().equals("boolean")) res[i] = Boolean.valueOf(rand.nextBoolean());
			else if (types[i].getName().equals("[C")) res[i] = generateCharArray();
			else if (types[i].getName().equals("java.lang.String")) res[i] = generateString();
			else if (types[i].getName().equals("java.lang.StringBuffer")) res[i] = generateStringBuffer();
			else if (types[i].getName().equals("java.lang.CharSequence")) res[i] = generateString();
			else if (types[i].getName().equals("it.unimi.dsi.lang.MutableString")) res[i] = generateMutableString();
			else if (types[i].getName().equals("java.lang.Object")) res[i] = new Object();
			else throw new IllegalArgumentException("Type " + types[i] + " not (yet) implemented");
		}
		return res;
    }

    /** Generates a char[] at random. */
    public static char[] generateCharArray() {
		return generateCharArray(MAXLENGTH);
    }

    /** Generates a char[] at random with given its maximum length. */
    public static char[] generateCharArray(final int m) {
		final int n = rand.nextInt(m + 1);
		final char res[] = new char[n];
		for (int i = 0; i < n; i++) res[i] = rand.nextInt(SPACE_FREQ) == 0 ? ' ' : (char) (MINCHAR + rand.nextInt(MAXCHAR - MINCHAR + 1));
		return res;
    }

    /** Generates a char[] at random. */
    public static char[] generateUnicodeCharArray(final int n) {
		final char res[] = new char[n];
		for (int i = 0; i < n; i++) while (! Character.isLetterOrDigit(res[i] = (char)rand.nextInt()));
		return res;
    }

    /** Generates a String at random. */
    public static String generateString() {
		return new String(generateCharArray());
    }

    /** Generates a StringBuffer at random. */
    public static StringBuffer generateStringBuffer() {
		return new StringBuffer(new String(generateCharArray()));
    }

    /** Generates a MutableString at random. */
    public static MutableString generateMutableString() {
		return new MutableString(generateCharArray());
    }


    /** Converts Object <code>o</code> from class <code>c1</code> to class <code>c2</code>. */
    public static Object convert(final Object o, final Class<? extends Object> c1, final Class c2) {
		if (c1.equals(c2)) return o;
		if (Number.class.isAssignableFrom(c1)) return o;
		if (c1.equals(MutableString.class)) {
			if (c2.equals(String.class)) return ((MutableString)o).toString();
			else if (c2.equals(StringBuffer.class)) return new StringBuffer(((MutableString)o).toString());
		}
		else if (c1.equals(StringBuffer.class)) {
			if (c2.equals(String.class)) return ((StringBuffer)o).toString();
		}
		else if (c1.equals(CharSequence.class)) {
			if (c2.equals(String.class)) return ((CharSequence)o).toString();
		}
		else if (c1.equals(Character.class)) {
			if (c2.equals(String.class)) return o.toString();
		}
		System.err.println("Don't know how to convert " + c1 + " to " + c2);
		System.err.println("(Object to convert is " + o + ")");
		System.exit(1);
		return null;
    }

    /** Given an array of Objects, it converts their types using <code>alternativeParameterType</code> array, unless
     *  the latter is {@code null}, in which case it just returns a copy of the argument. */
    public static Object[] convert(final Object arg[]) {
		final int n = arg.length;
        final Object res[] = new Object[n];
		if (alternativeParameterType == null) {
			System.arraycopy(arg, 0, res, 0, n);
			return res;
		}
		assert n == alternativeParameterType.length;
		for (int i = 0; i < n; i++)
			res[i] = convert(arg[i], arg[i].getClass(), alternativeParameterType[i]);
		return res;
    }

    private static void print(final String type, final String name, final CharSequence s) {
        System.out.println(type + " content of " + name + " was");
    	System.out.println(s + " [" + s.length() + " chars]");
    	System.out.println(CharArrayList.wrap(new MutableString(s).toCharArray()));
    	System.out.print("[");
    	for(int i = 0; i < s.length(); i++) System.out.print((int)s.charAt(i) + (i == 0 ? "" : ", "));
    	System.out.println("]");
    }

    private static void print2(final String type, final String sb, final String ms) {
    	print(type, "StringBuffer", sb);
    	print(type, "MutableString", ms);
    }

    final static int NUMBER_OF_SPECIAL_TESTS = 23; // Number of special tests, to be updated manually.
    final static int NUMBER_OF_TESTS = 100; // Number of public methods, to be updated manually.

    /** Special tests... */
    public static void specialTest(final StringBuffer sb, MutableString ms) {
		Object msRes = null, sbRes = null;
		Exception msThrow = null, sbThrow = null;
		Method msm = null;
		int which, choice, from;

		String ssb = new String(sb.toString());
		final String sms = new String(ms.toString());
		Object o;
		StringBuffer b;
		char c, d, a[];
		String s;
		boolean compact = ms.isCompact();

		which = rand.nextInt(NUMBER_OF_SPECIAL_TESTS);
		try {
			switch (which) {
			case 0:
				// toLowerCase
				try { ms.toLowerCase(); } catch (final Exception e) { msThrow = e; }
				try { sb.setLength(0); sb.append(ssb.toString().toLowerCase()); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("toLowerCase", new Class[] {});
				break;
			case 1:
				// toUpperCase
				try { ms.toUpperCase(); } catch (final Exception e) { msThrow = e; }
				try { sb.setLength(0); sb.append(ssb.toString().toUpperCase()); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("toUpperCase", new Class[] {});
				break;
			case 2:
				// trim
				try { ms.trim(); } catch (final Exception e) { msThrow = e; }
				try { sb.setLength(0); sb.append(ssb.toString().trim()); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("trim", new Class[] {});
				break;
			case 20:
				// trimLeft
				try { ms.trimLeft(); } catch (final Exception e) { msThrow = e; }
				try { sb.setLength(0); sb.append((ssb.toString() + "X").trim()); sb.deleteCharAt(sb.length() - 1); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("trimLeft", new Class[] {});
				break;
			case 21:
				// trimRight
				try { ms.trimRight(); } catch (final Exception e) { msThrow = e; }
				try { sb.setLength(0); sb.append(("X" + ssb.toString()).trim().substring(1)); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("trimRight", new Class[] {});
				break;
			case 3:
				// lastChar
				try { msRes = Character.valueOf(ms.lastChar()); } catch (final Exception e) { msThrow = e; }
				try { sbRes = Character.valueOf(sb.toString().charAt(sb.length()-1)); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("lastChar", new Class[] {});
				break;
			case 4:
				// firstChar
				try { msRes = Character.valueOf(ms.firstChar()); } catch (final Exception e) { msThrow = e; }
				try { sbRes = Character.valueOf(sb.toString().charAt(0)); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("firstChar", new Class[] {});
				break;
			case 5:
				// loose
				try { ms.loose(); } catch (final Exception e) {}
				msm = MutableString.class.getDeclaredMethod("loose", new Class[] {});
				break;
			case 6:
				// compact
				try { ms.compact(); } catch (final Exception e) {}
				if (ms.capacity() != ms.length()) {
					System.err.println("After compact, MutableString does not appear to be compact");
					print("Current", "MutableString", ms);
					print("Previous", "MutableString", sms);
					System.err.println("Capacity=" + ms.capacity() + ", length=" + ms.length());
					System.exit(1);
				}
				msm = MutableString.class.getDeclaredMethod("compact", new Class[] {});
				break;
			case 7:
				// array
				try { msRes = (CharArrayList.wrap(ms.array())).subList(0, ms.length()); } catch (final Exception e) { msThrow = e; }
				try { sbRes = CharArrayList.wrap(sb.toString().toCharArray()); } catch (final Exception e) { sbThrow = e; }
				msm = MutableString.class.getDeclaredMethod("array", new Class[] {});
				break;
			case 8:
				// changed
				ms.compact();
				compact = true;
				a = ms.array();
				msm = MutableString.class.getDeclaredMethod("changed", new Class[] {});
				if (sb.length() == 0) break;
				final int changeHowMany = rand.nextInt(sb.length());
				int pos;
				for (int i = 0; i < changeHowMany; i++) {
					c = a[pos = rand.nextInt(sb.length())] = (char) (MINCHAR + rand.nextInt(MAXCHAR - MINCHAR + 1));
					sb.setCharAt(pos, c);
				}
				ms.changed();
				break;
			case 9:
				// wrap
				if (ms.isCompact()) {
					try {
						ms = MutableString.wrap(ms.array());
					} catch (final Exception e) {}
					msm = MutableString.class.getDeclaredMethod("wrap", new Class[] { char[].class });
				}
				else {
					try {
						ms = MutableString.wrap(ms.array(), ms.length());
					} catch (final Exception e) {}
					msm = MutableString.class.getDeclaredMethod("wrap", new Class[] { char[].class, int.class });
				}
				break;
			case 10:
				// equals
				if (! ms.equals(new MutableString(sb))) {
					System.err.println("equals(MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(sb));
					System.exit(1);
				}
				if (! ms.equals(new MutableString(ssb.toCharArray()))) {
					System.err.println("equals(char[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(ssb.toCharArray()));
					System.exit(1);
				}
				if (! ms.equals(sb.toString())) {
					System.err.println("equals(String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + sb);
					System.exit(1);
				}
				ssb = ssb.intern();
				if (! ms.equals(ssb)) {
					System.err.println("equals(String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + ssb);
					System.exit(1);
				}

				o = sb;
				if (! ms.equals(o)) {
					System.err.println("equals(Object=MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(sb));
					System.exit(1);
				}
				o = sb.toString();
				if (! ms.equals(o)) {
					System.err.println("equals(Object=String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + sb);
					System.exit(1);
				}
				o = ssb.intern();
				if (! ms.equals(o)) {
					System.err.println("equals(Object=String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + ssb);
					System.exit(1);
				}

				b = new StringBuffer(sb.toString()).append(' ');
				if (ms.equals(new MutableString(b))) {
					System.err.println("! equalsMutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(b));
					System.exit(1);
				}
				if (ms.equals(new MutableString(b.toString().toCharArray()))) {
					System.err.println("! equalschar[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(b.toString().toCharArray()));
					System.exit(1);
				}
				if (ms.equals(b.toString())) {
					System.err.println("! equalsString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + b);
					System.exit(1);
				}
				s = b.toString().intern();
				if (ms.equals(s)) {
					System.err.println("! equalsString.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + s);
					System.exit(1);
				}

				o = b;
				if (ms.equals(o)) {
					System.err.println("! equals(Object=MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + o);
					System.exit(1);
				}
				o = b.toString();
				if (ms.equals(o)) {
					System.err.println("! equals(Object=String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + o);
					System.exit(1);
				}
				o = b.toString().intern();
				if (ms.equals(o)) {
					System.err.println("! equals(Object=String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + o);
					System.exit(1);
				}
				msm = MutableString.class.getDeclaredMethod("equals", new Class[] { Object.class });
				break;
			case 11:
				// equalsIgnoreCase
				b = new StringBuffer(new String(sb).toUpperCase());
				s = b.toString();
				if (! ms.equalsIgnoreCase(new MutableString(b))) {
					System.err.println("equalsIgnoreCase(MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(b));
					System.exit(1);
				}
				if (! ms.equalsIgnoreCase(new MutableString(s.toCharArray()))) {
					System.err.println("equalsIgnoreCase(char[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(s.toCharArray()));
					System.exit(1);
				}
				if (! ms.equalsIgnoreCase(s.toString())) {
					System.err.println("equalsIgnoreCase(String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + s);
					System.exit(1);
				}
				ssb = ssb.intern();
				if (! ms.equalsIgnoreCase(ssb)) {
					System.err.println("equalsIgnoreCase(String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + ssb);
					System.exit(1);
				}

				b = new StringBuffer(new String(sb).toUpperCase()).append(' ');
				if (ms.equalsIgnoreCase(new MutableString(b))) {
					System.err.println("! equalsIgnoreCaseMutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(b));
					System.exit(1);
				}
				if (ms.equalsIgnoreCase(new MutableString(b.toString().toCharArray()))) {
					System.err.println("! equalsIgnoreCasechar[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(b.toString().toCharArray()));
					System.exit(1);
				}
				if (ms.equalsIgnoreCase(b.toString())) {
					System.err.println("! equalsIgnoreCaseString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + b);
					System.exit(1);
				}
				s = b.toString().intern();
				if (ms.equalsIgnoreCase(s)) {
					System.err.println("! equalsIgnoreCaseString.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + s);
					System.exit(1);
				}

				msm = MutableString.class.getDeclaredMethod("equalsIgnoreCase", new Class[] { MutableString.class });
				break;
			case 12:
				// compareTo
				if (0 != ms.compareTo(new MutableString(sb))) {
					System.err.println("compareTo(MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(sb));
					System.exit(1);
				}
				if (0 != ms.compareTo(new MutableString(ssb.toCharArray()))) {
					System.err.println("compareTo(char[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(ssb.toCharArray()));
					System.exit(1);
				}
				if (0 != ms.compareTo(sb.toString())) {
					System.err.println("compareTo(String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + sb);
					System.exit(1);
				}
				ssb = ssb.intern();
				if (0 != ms.compareTo(ssb)) {
					System.err.println("compareTo(String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + ssb);
					System.exit(1);
				}

				o = sb;

				b = new StringBuffer(sb.toString());
				if (b.length() == 0) {
					b.append(d = 'A');
					c = 0;
				}
				else {
					final int p = rand.nextInt(b.length());
					c = b.charAt(p);
					d = (char)(c + rand.nextInt(2) * 2 - 1);
				}
				if (0 < (d - c) * ms.compareTo(new MutableString(b))) {
					System.err.println("! compareTo(MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(b));
					System.exit(1);
				}
				if (0 < (d - c) * ms.compareTo(new MutableString(b.toString().toCharArray()))) {
					System.err.println("! compareTo(char[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(b.toString().toCharArray()));
					System.exit(1);
				}
				if (0 < (d - c) * ms.compareTo(b.toString())) {
					System.err.println("! compareTo(String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + b);
					System.exit(1);
				}
				s = b.toString().intern();
				if (0 < (d - c) * ms.compareTo(s)) {
					System.err.println("! compareTo(String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + s);
					System.exit(1);
				}

				msm = MutableString.class.getDeclaredMethod("compareTo", new Class[] { MutableString.class });
				break;
			case 13:
				// compareToIgnoreCase
				if (0 != ms.compareToIgnoreCase(new MutableString(sb))) {
					System.err.println("compareToIgnoreCase(MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(sb));
					System.exit(1);
				}
				if (0 != ms.compareToIgnoreCase(new MutableString(ssb.toCharArray()))) {
					System.err.println("compareToIgnoreCase(char[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(ssb.toCharArray()));
					System.exit(1);
				}
				if (0 != ms.compareToIgnoreCase(sb.toString())) {
					System.err.println("compareToIgnoreCase(String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + sb);
					System.exit(1);
				}
				ssb = ssb.intern();
				if (0 != ms.compareToIgnoreCase(ssb)) {
					System.err.println("compareToIgnoreCase(String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + ssb);
					System.exit(1);
				}

				b = new StringBuffer(sb.toString());
				if (b.length() == 0) {
					b.append(d = 'A');
					c = 0;
				}
				else {
					final int p = rand.nextInt(b.length());
					c = b.charAt(p);
					d = (char)(c + rand.nextInt(2) * 2 - 1);
				}
				if (0 < (d - c) * ms.compareToIgnoreCase(new MutableString(b))) {
					System.err.println("! compareToIgnoreCase(MutableString) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + new MutableString(b));
					System.exit(1);
				}
				if (0 < (d - c) * ms.compareToIgnoreCase(new MutableString(b.toString().toCharArray()))) {
					System.err.println("! compareToIgnoreCase(char[]) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + CharArrayList.wrap(b.toString().toCharArray()));
					System.exit(1);
				}
				if (0 < (d - c) * ms.compareToIgnoreCase(b.toString())) {
					System.err.println("! compareToIgnoreCase(String) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + b);
					System.exit(1);
				}
				s = b.toString().intern();
				if (0 < (d - c) * ms.compareToIgnoreCase(s)) {
					System.err.println("! compareToIgnoreCase(String.intern()) failed");
					System.err.println("MutableString is " + ms);
					System.err.println("Argument is      " + s);
					System.exit(1);
				}

				msm = MutableString.class.getDeclaredMethod("compareToIgnoreCase", new Class[] { MutableString.class });
				break;
			case 14:
				// replace
				choice = rand.nextInt(9);

				int l = choice > 4 ? 1 : rand.nextInt(40);
				a = new char[l];
				for(int i = 0; i < l; i++) a[i] = (char)('@' + i);

				final MutableString as[] = new MutableString[a.length];

				b = new StringBuffer();
				for(int i = 0; i < a.length; i++) as[i] = generateMutableString().length(choice == 3 || choice == 4 || choice == 8 ? 1 : 1 + rand.nextInt(4));
				for(int i = 0; i < sb.length(); i++) {
					int j;
					for(j = 0; j < a.length; j++) {
						if (sb.charAt(i) == a[j]) {
							b.append(as[j]);
							break;
						}
					}
					if (j == a.length) b.append(sb.charAt(i));
				}
				sb.setLength(0);
				sb.append(b);

				switch(choice) {
				case 0:	ms.replace(a, as);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char[].class, MutableString[].class }); break;
				case 1: final String badString[] = new String[as.length]; for(int i = 0; i < as.length; i++) badString[i] = as[i].toString(); ms.replace(a, badString);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char[].class, String[].class }); break;
				case 2: final CharSequence badCS[] = new CharSequence[as.length]; for(int i = 0; i < as.length; i++) badCS[i] = as[i]; ms.replace(a, badCS);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char[].class, CharSequence[].class }); break;
				case 3: final char badChar[] = new char[as.length]; for(int i = 0; i < as.length; i++) badChar[i] = as[i].charAt(0); ms.replace(a, badChar);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char[].class, char[].class }); break;
				case 4: final Char2CharOpenHashMap m = new Char2CharOpenHashMap(); for(int i = 0; i < as.length; i++) m.put(a[i], as[i].charAt(0)); ms.replace(m);
				msm = MutableString.class.getDeclaredMethod("replace", new Class[] { Char2CharMap.class }); break;
				case 5:	ms.replace(a[0], as[0]);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char.class, MutableString.class }); break;
				case 6: final String badString1 = as[0].toString(); ms.replace(a[0], badString1);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char.class, String.class }); break;
				case 7: final CharSequence badCS1 = as[0]; ms.replace(a[0], badCS1);
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char.class, CharSequence.class }); break;
				case 8: ms.replace(a[0], as[0].charAt(0));
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { char.class, char.class }); break;
				}

				break;

			case 15:
				//
				choice = rand.nextInt(6);
				l = rand.nextInt(ms.length() + 1);
				s = sb.toString().substring(0, l);
				if (choice >= 3 && choice < 6) s = s.toLowerCase();
				if (choice > 5) s = new String(generateCharArray(l));

				switch(choice % 6) {
				case 0:
					try { msRes = Boolean.valueOf(ms.startsWith(ms.substring(0, l))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().startsWith(s)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("startsWith", new Class[] { MutableString.class });
					break;

				case 1:
					try { msRes = Boolean.valueOf(ms.startsWith(ms.substring(0, l).toString())); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().startsWith(s)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("startsWith", new Class[] { String.class });
					break;

				case 2:
					try { msRes = Boolean.valueOf(ms.startsWith(new StringBuffer(ms.substring(0, l).toString()))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().startsWith(s)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("startsWith", new Class[] { CharSequence.class });
					break;

				case 3:
					try { msRes = Boolean.valueOf(ms.startsWithIgnoreCase(ms.substring(0, l))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().toLowerCase().startsWith(s.toLowerCase())); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("startsWithIgnoreCase", new Class[] { MutableString.class });
					break;

				case 4:
					try { msRes = Boolean.valueOf(ms.startsWithIgnoreCase(ms.substring(0, l).toString())); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().toLowerCase().startsWith(s.toLowerCase())); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("startsWithIgnoreCase", new Class[] { String.class });
					break;

				case 5:
					try { msRes = Boolean.valueOf(ms.startsWithIgnoreCase(new StringBuffer(ms.substring(0, l).toString()))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().toLowerCase().startsWith(s.toLowerCase())); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("startsWithIgnoreCase", new Class[] { CharSequence.class });
					break;

				}
				break;

			case 16:
				//
				choice = rand.nextInt(6);
				l = rand.nextInt(ms.length() + 1);
				s = sb.toString().substring(sb.length() - l);
				if (choice >= 3 && choice < 6) s = s.toLowerCase();
				if (choice > 5) s = new String(generateCharArray(l));

				switch(choice % 6) {
				case 0:
					try { msRes = Boolean.valueOf(ms.endsWith(ms.substring(l))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().endsWith(s)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("endsWith", new Class[] { MutableString.class });
					break;

				case 1:
					try { msRes = Boolean.valueOf(ms.endsWith(ms.substring(l).toString())); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().endsWith(s)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("endsWith", new Class[] { String.class });
					break;

				case 2:
					try { msRes = Boolean.valueOf(ms.endsWith(new StringBuffer(ms.substring(l).toString()))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().endsWith(s)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("endsWith", new Class[] { CharSequence.class });
					break;

				case 3:
					try { msRes = Boolean.valueOf(ms.endsWithIgnoreCase(ms.substring(l))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().toLowerCase().endsWith(s.toLowerCase())); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("endsWithIgnoreCase", new Class[] { MutableString.class });
					break;

				case 4:
					try { msRes = Boolean.valueOf(ms.endsWithIgnoreCase(ms.substring(l).toString())); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().toLowerCase().endsWith(s.toLowerCase())); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("endsWithIgnoreCase", new Class[] { String.class });
					break;

				case 5:
					try { msRes = Boolean.valueOf(ms.endsWithIgnoreCase(new StringBuffer(ms.substring(l).toString()))); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Boolean.valueOf(sb.toString().toLowerCase().endsWith(s.toLowerCase())); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("endsWithIgnoreCase", new Class[] { CharSequence.class });
					break;

				}
				break;

			case 17:
				//
				l = rand.nextInt(sb.length()/2 + 1);
				if (rand.nextBoolean()) s = new String(generateCharArray(l));
				else s = sb.toString().substring(l, l + rand.nextInt(sb.length()/4 + 1));

				from = rand.nextInt() % (sb.length() * 2 + 1);

				switch(rand.nextInt(8)) {
				case 0:
					try { msRes = Integer.valueOf(ms.indexOf(s, from)); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Integer.valueOf(new TextPattern(s).search(sb, from)); } catch (final Exception e) { sbThrow = e; }
					// This is a fake, we really checked TextPattern
					msm = MutableString.class.getDeclaredMethod("indexOf", new Class[] { MutableString.class });
					break;
				case 1:
					try { msRes = Integer.valueOf(ms.indexOf(s, from)); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Integer.valueOf(new TextPattern(s).search(ms, from)); } catch (final Exception e) { sbThrow = e; }
					// This is a fake, we really checked TextPattern
					msm = MutableString.class.getDeclaredMethod("indexOf", new Class[] { MutableString.class });
					break;
				case 2:
					a = s.toCharArray();
					try { msRes = Integer.valueOf(ms.indexOfAnyOf(a, from)); } catch (final Exception e) { msThrow = e; }
					try {
						int temp = Integer.MAX_VALUE;
						for (final char element : a) if ((pos = sms.indexOf(element, from)) != -1) temp = Math.min(temp, pos);
						sbRes = Integer.valueOf(temp == Integer.MAX_VALUE ?  -1 : temp);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("indexOfAnyOf", new Class[] { char[].class });
					break;
				case 3:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.indexOfAnyBut(a, from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = 0;
						if (from > ms.length()) from = ms.length();
						for (i = from; i < ms.length(); i++) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j == a.length) break;
						}
						sbRes = Integer.valueOf(i < ms.length() ? i : -1);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("indexOfAnyBut", new Class[] { char[].class });
					break;

				case 4:
					a = s.toCharArray();
					try { msRes = Integer.valueOf(ms.indexOfAnyOf(new CharOpenHashSet(a), from)); } catch (final Exception e) { msThrow = e; }
					try {
						int temp = Integer.MAX_VALUE;
						for (final char element : a) if ((pos = sms.indexOf(element, from)) != -1) temp = Math.min(temp, pos);
						sbRes = Integer.valueOf(temp == Integer.MAX_VALUE ?  -1 : temp);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("indexOfAnyOf", new Class[] { CharSet.class });
					break;

				case 5:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.indexOfAnyBut(new CharOpenHashSet(a), from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = 0;
						if (from > ms.length()) from = ms.length();
						for (i = from; i < ms.length(); i++) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j == a.length) break;
						}
						sbRes = Integer.valueOf(i < ms.length() ? i : -1);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("indexOfAnyBut", new Class[] { CharSet.class });
					break;

				case 6:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.span(a, from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = 0;
						if (from > ms.length()) from = ms.length();
						for (i = from; i < ms.length(); i++) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j == a.length) break;
						}
						sbRes = Integer.valueOf(i - from);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("span", new Class[] { char[].class });
					break;
				case 7:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.span(new CharOpenHashSet(a), from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = 0;
						if (from > ms.length()) from = ms.length();
						for (i = from; i < ms.length(); i++) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j == a.length) break;
						}
						sbRes = Integer.valueOf(i - from);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("span", new Class[] { CharSet.class });
					break;
				}
				break;

			case 18:
				String search;
				String replace;
				main: for(;;) {
					search = new String(generateCharArray(rand.nextInt(10)));
					replace = new String(generateCharArray(rand.nextInt(10)));
					if (search.length() == 0) search = search + "X";
					if (replace.length() <= search.length()) break;
					/* Skip search strings with overlapping matches if replace text is longer than search text, as the semantics is
					 * slightly different (see replace()'s documentation). */
					int i = -1;
					int prev = -search.length();
					for(;;) {
						i = ms.indexOf(search, i + 1);
						if (i == -1) break main;
						if (prev + search.length() > i) break;
						prev = i;
					}
				}

				try { s = new String(sb).replaceAll(search, replace); sb.setLength(0); sb.append(s); } catch (final Exception e) { sbThrow = e; }

				switch(rand.nextInt(3)) {
				case 0:
					try { ms.replace(new MutableString(search), new MutableString(replace)); } catch (final Exception e) { msThrow = e; }
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { MutableString.class, MutableString.class });
					break;
				case 1:
					try { ms.replace(search, replace); } catch (final Exception e) { msThrow = e; }
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { String.class, String.class });
					break;
				case 2:
					final CharSequence s1 = search, r1 = replace;
					try { ms.replace(s1, r1); } catch (final Exception e) { msThrow = e; }
					msm = MutableString.class.getDeclaredMethod("replace", new Class[] { CharSequence.class, CharSequence.class });
					break;
				}

			break;

			case 19:
				//
				l = rand.nextInt(sb.length()/2 + 1);
				if (rand.nextBoolean()) s = new String(generateCharArray(l));
				else s = sb.toString().substring(l, l + rand.nextInt(sb.length()/4 + 1));

				from = rand.nextInt() % (sb.length() * 2 + 1);

				switch(rand.nextInt(7)) {
				case 0:
					try { msRes = Integer.valueOf(ms.lastIndexOf(s, from)); } catch (final Exception e) { msThrow = e; }
					try { sbRes = Integer.valueOf(sb.lastIndexOf(s ,from)); } catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("lastIndexOf", new Class[] { MutableString.class });
					break;
				case 1:
					a = s.toCharArray();
					try { msRes = Integer.valueOf(ms.lastIndexOfAnyOf(a, from)); } catch (final Exception e) { msThrow = e; }
					try {
						int temp = Integer.MIN_VALUE;
						for (final char element : a) {
							if ((pos = sms.lastIndexOf(element, from)) != -1) temp = Math.max(temp, pos);
						}
						sbRes = Integer.valueOf(temp == Integer.MIN_VALUE ?  -1 : temp);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("lastIndexOfAnyOf", new Class[] { char[].class });
					break;
				case 2:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.lastIndexOfAnyBut(a, from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = -1;
						if (from >= ms.length()) from = ms.length() - 1;
						for (i = from; i >= 0; i--) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j == a.length) break;
						}
						sbRes = Integer.valueOf(i);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("lastIndexOfAnyBut", new Class[] { char[].class });
					break;

				case 3:
					a = s.toCharArray();
					try { msRes = Integer.valueOf(ms.lastIndexOfAnyOf(new CharOpenHashSet(a), from)); } catch (final Exception e) { msThrow = e; }
					try {
						int temp = Integer.MIN_VALUE;
						for (final char element : a) {
							if ((pos = sms.lastIndexOf(element, from)) != -1) temp = Math.max(temp, pos);
						}
						sbRes = Integer.valueOf(temp == Integer.MIN_VALUE ?  -1 : temp);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("lastIndexOfAnyOf", new Class[] { CharSet.class });
					break;

				case 4:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.lastIndexOfAnyBut(new CharOpenHashSet(a), from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = -1;
						if (from >= ms.length()) from = ms.length() - 1;
						for (i = from; i >= 0; i--) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j == a.length) break;
						}
						sbRes = Integer.valueOf(i);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("lastIndexOfAnyBut", new Class[] { CharSet.class });
					break;
				case 5:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.cospan(a, from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = 0;
						if (from > ms.length()) from = ms.length();
						for (i = from; i < ms.length(); i++) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j != a.length) break;
						}
						sbRes = Integer.valueOf(i - from);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("cospan", new Class[] { char[].class });
					break;
				case 6:
					a = s.toCharArray();
					//System.err.println(CharArrayList.wrap(a) + " " + from);
					try { msRes = Integer.valueOf(ms.cospan(new CharOpenHashSet(a), from)); } catch (final Exception e) { msThrow = e; }
					try {
						int i;
						if (from < 0) from = 0;
						if (from > ms.length()) from = ms.length();
						for (i = from; i < ms.length(); i++) {
							int j;
							for(j = 0; j < a.length; j++) if (ms.charAt(i) == a[j]) break;
							if (j != a.length) break;
						}
						sbRes = Integer.valueOf(i - from);
					} catch (final Exception e) { sbThrow = e; }
					msm = MutableString.class.getDeclaredMethod("cospan", new Class[] { CharSet.class });
					break;
				}
				break;

			case 22:
				// delete characters
				choice = rand.nextInt(3);

				l = choice == 0 ? 1 : rand.nextInt(40);
				a = new char[l];
				for(int i = 0; i < l; i++) a[i] = (char)('@' + rand.nextInt('Z' - '@'));
				final CharOpenHashSet charSet = new CharOpenHashSet(a);

				for(int i = 0; i < sb.length();) if (charSet.contains(sb.charAt(i))) sb.delete(i, i + 1); else i++;

				switch(choice) {
				case 0:	ms.delete(a[0]);
					msm = MutableString.class.getDeclaredMethod("delete", new Class[] { char.class }); break;
				case 1: ms.delete(a);
					msm = MutableString.class.getDeclaredMethod("delete", new Class[] { char[].class }); break;
				case 2: ms.delete(charSet);
					msm = MutableString.class.getDeclaredMethod("delete", new Class[] { CharSet.class }); break;
				}
				break;

			default:
				System.err.println("Special test number " + which + " not (yet) defined");
				System.exit(1);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (sbThrow == null && msThrow != null) {
			System.out.println("Method " + msm + " threw exception " + msThrow);
			System.out.println("No exception was thrown by StringBuffer");
			print2("Previous", ssb, sms);
			System.exit(1);
		}

		if (sbThrow != null && msThrow == null) {
			System.out.println("Method " + msm + " threw no exception ");
			System.out.println("StringBuffer threw " + sbThrow);
			print2("Previous", ssb, sms);
			System.exit(1);
		}

		if (sbThrow != null && msThrow != null && ! (msThrow.getClass().equals(msThrow.getClass()))) {
			System.out.println("Method " + msm + " threw " + msThrow);
			System.out.println("StringBuffer threw " + sbThrow);
			print2("Previous", ssb, sms);
			System.exit(1);
		}

		for (msIdx = 0; msIdx < msMethod.length; msIdx++) if (msMethod[msIdx].equals(msm)) break;
		if (msIdx == msMethod.length) {
			throw new IllegalStateException("I was looking for " + msm + " (which = " + which + ")");
		}
		msTimes[msIdx]++;

		if (sbThrow != null) {
			msExc[msIdx]++;
		}

		if (which != 6 && which != 5 && ms.isCompact() != compact) {
			System.out.println("On method " + msm);
			System.out.println("Previously MutableString was " + (compact ? "compact" : "loose") + "; now it is " + (ms.isCompact() ? "compact" : "loose"));
			if (sbThrow != null) System.out.println("The method produced an " + sbThrow);
			System.exit(1);
		}

		if ((msRes == null && sbRes != null) || (msRes != null && sbRes == null) || (msRes != null && ! (msRes.equals(sbRes)))) {
			System.out.println("On method " + msm + " returned value was " + msRes);
			System.out.println("StringBuffer returned " + sbRes + " instead");
			System.out.println("Class of first result: " + sbRes.getClass() + " hashcode=" + sbRes.hashCode());
			System.out.println("Class of second result: " + msRes.getClass() + " hashcode=" + msRes.hashCode());
			System.out.println("The two results are equal? " + msRes.equals(sbRes));
			System.out.println("The two results (as Strings) are equal? " + msRes.toString().equals(sbRes.toString()));
			print2("Previous", ssb, sms);
			if (msThrow != null) System.out.println("The MutableString method produced an " + msThrow);
			if (sbThrow != null) System.out.println("The StringBuffer method produced an " + sbThrow);
			System.exit(1);
		}

		if (! (sb.toString().equals(ms.toString()))) {
			System.out.println("After call, values are different");
			System.out.println("Method " + msm);
			print2("Previous", ssb, sms);
			print("Current", "StringBuffer", sb);
			print("Current", "MutableString", ms);
			int i;
			for(i = 0; i < Math.min(sb.length(), ms.length()); i++) if (sb.charAt(i) != ms.charAt(i)) break;
			System.out.println("First different character has index " + i);
			if (msThrow != null) System.out.println("The MutableString method produced an " + msThrow);
			if (sbThrow != null) System.out.println("The StringBuffer method produced an " + sbThrow);
			System.exit(1);
		}



    }


    /** Tests I/O methods. */
    @SuppressWarnings("deprecation")
	public static void testIO(final int n) throws IOException {
		final MutableString a[] = new MutableString[n], t = new MutableString();
		final int l[] = new int[n];

		// I/O on data input/output
		DataOutputStream dbos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("regressionTest")));
		for(int i = 0; i < n; i++) {
			if (rand.nextBoolean()) l[i] = rand.nextInt(100);
			else l[i] = rand.nextInt(100000);
			(a[i] = new MutableString(generateUnicodeCharArray(l[i]))).writeSelfDelimUTF8((DataOutput)dbos);
		}
		dbos.close();

		DataInputStream dbis = new DataInputStream(new BufferedInputStream(new FileInputStream("regressionTest")));
		for(int i = 0; i < n; i++) if (! a[i].equals(t.readSelfDelimUTF8((DataInput)dbis))) {
			System.out.println("On I/O with self-delimiting UTF-8 (" + i + ")");
			System.out.println("Written was " + s2i(a[i]));
			System.out.println("Read is     " + s2i(t));
			System.exit(1);
		}
		dbis.close();

		dbos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("regressionTest")));
		for(int i = 0; i < n; i++) {
			if (rand.nextBoolean()) l[i] = rand.nextInt(100);
			else l[i] = rand.nextInt(100000);
			(a[i] = new MutableString(generateUnicodeCharArray(l[i]))).writeUTF8((DataOutput)dbos);
		}
		dbos.close();

		dbis = new DataInputStream(new BufferedInputStream(new FileInputStream("regressionTest")));
		for(int i = 0; i < n; i++) if (! a[i].equals(t.readUTF8((DataInput)dbis, l[i]))) {
			System.out.println("On I/O with UTF-8 (" + i + ")");
			System.out.println("Written was " + s2i(a[i]));
			System.out.println("Read is     " + s2i(t));
			System.exit(1);
		}
		dbis.close();


		// I/O on input/output streams
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("regressionTest"));
		for(int i = 0; i < n; i++) {
			if (rand.nextBoolean()) l[i] = rand.nextInt(100);
			else l[i] = rand.nextInt(100000);
			(a[i] = new MutableString(generateUnicodeCharArray(l[i]))).writeSelfDelimUTF8(bos);
		}
		bos.close();

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream("regressionTest"));
		for(int i = 0; i < n; i++) if (! a[i].equals(t.readSelfDelimUTF8(bis))) {
			System.out.println("On I/O with self-delimiting UTF-8 (" + i + ")");
			System.out.println("Written was " + s2i(a[i]));
			System.out.println("Read is     " + s2i(t));
			System.exit(1);
		}
		bis.close();

		bos = new BufferedOutputStream(new FileOutputStream("regressionTest"));
		for(int i = 0; i < n; i++) {
			if (rand.nextBoolean()) l[i] = rand.nextInt(100);
			else l[i] = rand.nextInt(100000);
			(a[i] = new MutableString(generateUnicodeCharArray(l[i]))).writeUTF8(bos);
		}
		bos.close();

		bis = new BufferedInputStream(new FileInputStream("regressionTest"));
		for(int i = 0; i < n; i++) if (! a[i].equals(t.readUTF8(bis, l[i]))) {
			System.out.println("On I/O with UTF-8 (" + i + ")");
			System.out.println("Written was " + s2i(a[i]));
			System.out.println("Read is     " + s2i(t));
			System.exit(1);
		}
		bis.close();





		final OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("regressionTest")), "UTF-16BE");
		for(int i = 0; i < n; i++) {
			if (rand.nextBoolean()) l[i] = rand.nextInt(100);
			else l[i] = rand.nextInt(100000);
			(a[i] = new MutableString(generateUnicodeCharArray(l[i]))).write(osw);
		}
		osw.close();

		final InputStreamReader isr = new InputStreamReader(new BufferedInputStream(new FileInputStream("regressionTest")), "UTF-16BE");
		int u;
		for(int i = 0; i < n; i++) {
			if ((u = t.read(isr, l[i])) != l [i]) {
				System.out.println("I/O error on " + t + ": " + l[i] + " characters read, " + u + " characters written");
			}
			if (! a[i].equals(t)) {
				System.out.println("On I/O with Reader (" + i + ")");
				System.out.println("Written was " + s2i(a[i]));
				System.out.println("Read is     " + s2i(t));
				System.exit(1);
			}
		}
		isr.close();
    }


    /** Tests a method at random on <code>sb</code> and <code>ms</code>. */
    public static void test(final StringBuffer sb, final MutableString ms) throws IllegalArgumentException, IllegalAccessException {
		if (rand.nextInt(NUMBER_OF_TESTS) < NUMBER_OF_SPECIAL_TESTS) {
			specialTest(sb, ms);
			return;
		}
		final String ssb = sb.toString();
		final String sms = ms.toString();
		final Method m = randomMethod();
		final Object msargs[] = params(m);
		final Object sbargs[] = convert(msargs);
		Object sbRes = null;
		Object msRes = null;
		Throwable sbThrow = null;
		Throwable msThrow = null;
		final boolean compact = ms.isCompact();

		try {
			sbRes = sbMethod[sbIdx].invoke(sb, sbargs);
		} catch (final InvocationTargetException e) {
			//System.err.println("Exception " + e + " on target StringBuffer");
			sbThrow = e.getTargetException();
		}

		try {
			msRes = msMethod[msIdx].invoke(ms, msargs);
		} catch (final InvocationTargetException e) {
			//System.err.println("Exception " + e + " on target MutableString");
			msThrow = e.getTargetException();
		}

		if (sbThrow == null && msThrow != null) {
			System.out.println("On method " + sbMethod[sbIdx] + " with args " + a2s(sbargs) + " threw no Exception");
			System.out.println("On method " + msMethod[msIdx] + " with args " + a2s(msargs) + " threw " + msThrow);
			print2("Previous", ssb, sms);
			System.exit(1);
		}

		if (sbThrow != null && msThrow == null) {
			System.out.println("On method " + sbMethod[sbIdx] + " with args " + a2s(sbargs) + " threw " + sbThrow);
			System.out.println("On method " + msMethod[msIdx] + " with args " + a2s(msargs) + " threw no Exception");
			print2("Previous", ssb, sms);
			System.exit(1);
		}

		if (sbThrow != null && msThrow != null && ! (msThrow.getClass().equals(msThrow.getClass()))) {
			System.out.println("On method " + sbMethod[sbIdx] + " with args " + a2s(sbargs) + " threw " + sbThrow);
			System.out.println("On method " + msMethod[msIdx] + " with args " + a2s(msargs) + " threw " + msThrow);
			print2("Previous", ssb, sms);
			System.exit(1);
		}

		sbTimes[sbIdx]++;
		msTimes[msIdx]++;

		if (sbThrow != null) {
			sbExc[sbIdx]++;
			msExc[msIdx]++;
		}

		if (m.getName().equals("capacity")) {
			//System.err.println("Capacity of StringBuffer / MutableString: " + sbRes + " / " + msRes);
			return;
		}

		if (ms.isCompact() != compact && ! msMethod[msIdx].getName().equals("ensureCapacity")) {
			System.out.println("On method " + msMethod[msIdx] + " with args " + a2s(msargs) + " returned " + msRes);
			System.out.println("Previously MutableString was " + (compact ? "compact" : "loose") + "; now it is " + (ms.isCompact() ? "compact" : "loose"));
			if (sbThrow != null) System.out.println("The method produced an " + sbThrow);
			System.exit(1);
		}

		if (sbMethod[sbIdx].getReturnType() != void.class && msMethod[msIdx].getReturnType() != void.class) {

			if ((msRes == null && sbRes != null) || (msRes != null && sbRes == null) || (msRes != null && ! (msRes.equals(sbRes)))) {
				System.out.println("On method " + sbMethod[sbIdx] + " with args " + a2s(sbargs) + " returned " + sbRes);
				System.out.println("On method " + msMethod[msIdx] + " with args " + a2s(msargs) + " returned " + msRes);
				System.out.println("Class of first result: " + sbRes.getClass() + " hashcode=" + sbRes.hashCode());
				System.out.println("Class of second result: " + msRes.getClass() + " hashcode=" + msRes.hashCode());
				System.out.println("The two results are equal? " + msRes.equals(sbRes));
				System.out.println("The two results (as Strings) are equal? " + msRes.toString().equals(sbRes.toString()));
				print2("Previous", ssb, sms);
				if (sbThrow != null) System.out.println("The method produced an " + sbThrow);
				System.exit(1);
			}

			if (! (sb.toString().equals(ms.toString()))) {
				System.out.println("After call, values are different");
				System.out.println("Method " + sbMethod[sbIdx] + " with args " + a2s(sbargs));
				System.out.println("Method " + msMethod[msIdx] + " with args " + a2s(msargs));
				print2("Previous", ssb, sms);
				print("Current", "StringBuffer", sb);
				print("Current", "MutableString", ms);
				if (sbThrow != null) System.out.println("The call produced an " + sbThrow);
				System.exit(1);
			}

		}

		return;
    }

    public static void main(final String[] arg) throws IllegalArgumentException, IllegalAccessException, IOException {
		final int noOfTest = Integer.parseInt(arg[0]);
		final int noOfIOTests = arg.length < 2 ? 100 : Integer.parseInt(arg[1]);
		final long seed = arg.length < 3 ? System.currentTimeMillis() : Long.parseLong(arg[2]);
		rand = new XoRoShiRo128PlusRandomGenerator(seed);

		System.out.println("Seed = " + seed);

		final char c[] = generateCharArray();
		final StringBuffer sb = new StringBuffer(new String(c));
		final MutableString ms = new MutableString(new String(c));

		for(int i = 0; i < noOfTest; i++) test(sb, ms);
		testIO(noOfIOTests);


		System.out.println("\nStringBuffer Methods");
		for (int i = 0; i < sbMethod.length; i++)
			System.out.println("\t" + sbMethod[i] + " called " + sbTimes[i] + " (exc. " + sbExc[i] + ")");
		System.out.println("\nMutableString Methods");
		for (int i = 0; i < msMethod.length; i++)
			System.out.println("\t" + msMethod[i] + " called " + msTimes[i] + " (exc. " + msExc[i] + ")");
    }

}
