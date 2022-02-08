/*
 * DSI utilities
 *
 * Copyright (C) 2006-2022 Paolo Boldi and Sebastiano Vigna
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

package it.unimi.dsi.lang;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** A parser for simple object specifications based on strings.
 *
 * <p>Whenever a particular instance of a class (not a singleton) has to be specified in textual format,
 * one faces the difficulty of having {@link Class#forName(String)} but no analogous method for instances. This
 * class provides a method {@link #fromSpec(String, Class, String[], String[])} that will generate object instances
 * starting from a specification of the form
 * <pre style="text-align: center; padding: .5em">
 * <var>class</var>(<var>arg</var>,&hellip;)
 * </pre>
 *
 * <p>The format of the specification is rather loose, to ease use on the command line: each argument may or may not
 * be quote-delimited, with the proviso that inside quotes you have the usual escape rules, whereas without quotes the
 * end of the parameter is marked by the next comma or closed parenthesis, and surrounding space is trimmed. For empty constructors,
 * parentheses can be omitted. Valid examples are, for instance,
 * <pre style="text-align: center; padding: .5em">
 * java.lang.Object
 * java.lang.Object()
 * java.lang.String(foo)
 * java.lang.String("foo")
 * </pre>
 *
 * <p>After parsing, we search for a constructor accepting as many strings as specified arguments, or possibly
 * a string varargs constructor. The second optional argument will be used to check
 * that the generated object is of the correct type, and the last argument is a list of packages that
 * will be prepended in turn to the specified class name. Finally, the last argument is an optional list of static factory method
 * names that will be tried before resorting to constructors (lacking such a list,
 * {@link #DEFAULT_FACTORY_METHODS} will be used). Several polymorphic versions make it possible to specify
 * just a subset of the arguments. Note that if you provide a <em>specific</em> list of factory methods they
 * will be tried <em>before</em> constructors, whereas  {@linkplain #DEFAULT_FACTORY_METHODS <em>default</em>} factory methods will be tried
 * <em>after</em> constructors.
 *
 * <p>Alternatively, a specification starting with <code>file:</code> will be interpreted as the filename of a
 * serialized object, which will be deserialized and returned. This approach makes it possible to have a single
 * string-based constructor for both serialized objects and textually-described objects, which is often convenient.
 *
 * <p>Additionally, it is possible to specify a {@linkplain #fromSpec(Object, String, Class, String[], String[]) <em>context object</em>}
 * that will be passed to the construction or factory method used to generate the new instance. The context is class dependent, and must
 * be correctly understood by the target class. In this case, the resolution process described above proceed similarly, but
 * the signatures searched for contain an additional {@link Object} argument before the string arguments.
 *
 * <p>Note that this arrangement requires some collaboration from the specified class, which must provide string-based constructors.
 * If additionally you plan on saving parseable representations which require more than just the class name, you are invited
 * to follow the {@link #toSpec(Object)} conventions.
 *
 * <p>This class is a <a href="http://www.martiansoftware.com/jsap/">JSAP</a>
 * {@link StringParser}, and can be used in a JSAP parameter
 * specifications to build easily objects on the command line. Several constructors make it possible
 * to generate parsers that will check for type compliance, and possibly attempt to prepend package names.
 */

public class ObjectParser extends StringParser {
	/** A marker object used to denote lack of a context. */
	private final static Object NO_CONTEXT = new Object();
	/** Standard names for factory methods. */
	public static final String[] DEFAULT_FACTORY_METHODS = { "getInstance", "newInstance", "valueOf" };
	/** A list of package names that will be prepended to specifications, or {@code null}. */
	private final String[] packages;
	/** A list of factory methods that will be used before trying constructors, or {@code null}. */
	private final String[] factoryMethod;
	/** A type that will be used to check instantiated objects. */
	private final Class<?> type;
	/** The context for this parser, or {@code null}. */
	private final Object context;

	/** Creates a new object parser with given control type, list of packages and factory methods.
	 *
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 * @param factoryMethod a list of factory methods that will be used before trying constructors, or {@code null}.
	 */

	public ObjectParser(final Class<?> type, final String[] packages, final String[] factoryMethod) {
		this(NO_CONTEXT, type, packages, factoryMethod);
	}

	/** Creates a new object parser with given control type and list of packages.
	 *
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 */

	public ObjectParser(final Class<?> type, final String[] packages) {
		this(type, packages, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new object parser with given control type.
	 *
	 * @param type a type that will be used to check instantiated objects.
	 */
	public ObjectParser(final Class<?> type) {
		this(type, (String[])null);
	}

	/** Creates a new object parser. */
	public ObjectParser() {
		this(Object.class);
	}

	/** Creates a new object parser with given context, control type, list of packages and factory methods.
	 *
	 * @param context the context for this parser (will be passed on to instantiated objects)&mdash;possibly {@code null}.
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 * @param factoryMethod a list of factory methods that will be used before trying constructors, or {@code null}.
	 */

	public ObjectParser(final Object context, final Class<?> type, final String[] packages, final String[] factoryMethod) {
		this.context = context;
		this.type = type;
		this.packages = packages;
		this.factoryMethod = factoryMethod;
	}

	/** Creates a new object parser with given context, control type and list of packages.
	 *
	 * @param context the context for this parser (will be passed on to instantiated objects)&mdash;possibly {@code null}.
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 */

	public ObjectParser(final Object context, final Class<?> type, final String[] packages) {
		this(context, type, packages, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new object parser with given context and control type.
	 *
	 * @param context the context for this parser (will be passed on to instantiated objects)&mdash;possibly {@code null}.
	 * @param type a type that will be used to check instantiated objects.
	 */
	public ObjectParser(final Object context, final Class<?> type) {
		this(context, type, null);
	}

	/** Creates a new object parser with given context.
	 * @param context the context for this parser (will be passed on to instantiated objects)&mdash;possibly {@code null}.
	 */
	public ObjectParser(final Object context) {
		this(context, Object.class);
	}

	@Override
	public Object parse(final String spec) throws ParseException {
		try {
			return fromSpec(context, spec, type, packages, factoryMethod);
		}
		catch (final Exception e) {
			throw new ParseException(e);
		}
	}

	/** Creates a new instance from a specification.
	 *
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @return an instance generated using the given specification and no ancillary data.
	 */
	public static Object fromSpec(final String spec) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(NO_CONTEXT, spec, Object.class, null, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new instance from a specification using a given control type.
	 *
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @param type a type that will be used to check instantiated objects.
	 * @return an instance generated using the given specification.
	 */
	public static <S> S fromSpec(final String spec, final Class<S> type) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(NO_CONTEXT, spec, type, null, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new instance from a specification using a given control type, list of packages and factory methods.
	 *
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 * @return an instance generated using the given specification and ancillary data.
	 */
	public static <S> S fromSpec(final String spec, final Class<S> type, final String[] packages) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(NO_CONTEXT, spec, type, packages, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new instance from a context and a specification.
	 *
	 * @param context a context object, or {@code null}.
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @return an instance generated using the given specification and ancillary data.
	 */
	public static Object fromSpec(final Object context, final String spec) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(context, spec, Object.class, null, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new instance from a context and a specification using a given control type.
	 *
	 * @param context a context object, or {@code null}.
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @param type a type that will be used to check instantiated objects.
	 * @return an instance generated using the given specification and ancillary data.
	 */
	public static <S> S fromSpec(final Object context, final String spec, final Class<S> type) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(context, spec, type, null, DEFAULT_FACTORY_METHODS);
	}

	/** Creates a new instance from a context and a specification using a given control type, list of packages and factory methods.
	 *
	 * @param context a context object, or {@code null}.
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 * @return an instance generated using the given specification and ancillary data.
	 */
	public static <S> S fromSpec(final Object context, final String spec, final Class<S> type, final String[] packages) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(context, spec, type, packages, DEFAULT_FACTORY_METHODS);
	}


	/** Creates a new instance from a specification using a given control type and list of packages.
	 *
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 * @param factoryMethod a list of factory methods that will be used before trying constructors, or {@code null}.
	 * @return an instance generated using the given specification and ancillary data.
	 */
	public static <S> S fromSpec(final String spec, final Class<S> type, final String[] packages, final String[] factoryMethod) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		return fromSpec(NO_CONTEXT, spec, type, packages, factoryMethod);
	}

	/** Creates a new instance from a context and a specification using a given control type and list of packages.
	 *
	 * @param context a context object, or {@code null}.
	 * @param spec the object specification (see the {@linkplain ObjectParser class documentation}).
	 * @param type a type that will be used to check instantiated objects.
	 * @param packages a list of package names that will be prepended to the specification, or {@code null}.
	 * @param factoryMethod a list of factory methods that will be used before trying constructors, or {@code null}.
	 * @return an instance generated using the given specification and ancillary data.
	 */
	@SuppressWarnings("unchecked")
	public static <S> S fromSpec(final Object context, String spec, final Class<S> type, final String[] packages, final String[] factoryMethod) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
		spec = spec.trim();
		final boolean contextualised = context != NO_CONTEXT;

		if (spec.startsWith("file:")) // Easy case--we load an object
			return (S)BinIO.loadObject(spec.substring(5));

		int endOfName = spec.indexOf('(');
		final int length = spec.length();
		if (endOfName < 0) endOfName = length;
		Class<? extends S> klass = null;
		final String className = spec.substring(0, endOfName).trim();
		try {
			klass = (Class<? extends S>)Class.forName(className);
		}
		catch(final ClassNotFoundException e) {
			// We try by prefixing with the given packages
			if (packages != null) for (final String p : packages) {
				try {
					klass = (Class<? extends S>)Class.forName(p + "." + className);
				}
				catch (final ClassNotFoundException niceTry) {}
				if (klass != null) break;
			}
		}

		if (klass == null) throw new ClassNotFoundException(className);
		if (! type.isAssignableFrom(klass)) throw new ClassCastException("Class " + klass.getSimpleName() + " is not assignable to " + type);

		final ObjectArrayList<Object> args = new ObjectArrayList<>();
		if (contextualised) args.add(context);

		if (endOfName < length) {
			boolean inQuotes, escaped;
			final MutableString arg = new MutableString();
			if (spec.charAt(length - 1) != ')') throw new IllegalArgumentException("\")\" missing at the end of argument list");

			int pos = endOfName;

			while(pos < length) {
				// Skip the current delimiter ('(', ',' or ')').
				pos++;
				// Skip whitespace before next argument
				while(pos < length && Character.isWhitespace(spec.charAt(pos))) pos++;
				// We are at the end of the specification.
				if (pos == length || args.size() == 0 && pos == length - 1 && spec.charAt(pos) == ')') break;

				arg.setLength(0);
				// If we find quotes, we skip then and go into quote mode.
				if (inQuotes = spec.charAt(pos) == '"') pos++;
				escaped = false;
				char c;
				for(;;) {
					c = spec.charAt(pos);
					if (! inQuotes) {
						if (c == ',' || pos == length - 1 && c == ')') break;
						arg.append(c);
					}
					else {
						if (c == '"' && ! escaped) {
							do pos++; while(pos < length && Character.isWhitespace(spec.charAt(pos)));
							if (pos == length || (spec.charAt(pos) != ')' && spec.charAt(pos) != ',')) throw new IllegalArgumentException();
							break;
						}
						if (c == '\\' && ! escaped) escaped = true;
						else {
							arg.append(c);
							escaped = false;
						}
					}

					pos++;
				}

				if (inQuotes) args.add(arg.toString());
				else {
					final String s = arg.trim().toString();
					if ("null".equals(s)) args.add(null);
					else args.add(s);
				}
			}
		}

		final Object[] argArray = args.toArray();
		final String[] stringArgArray;
		final Class<?>[] argTypes;

		if (contextualised) {
			argTypes = new Class<?>[args.size()];
			stringArgArray = new String[args.size() - 1];
			argTypes[0] = Object.class;
			for (int i = 1; i < argTypes.length; i++) {
				argTypes[i] = String.class;
				stringArgArray[i - 1] = (String)args.get(i);
			}
		}
		else {
			argTypes = new Class<?>[args.size()];
			stringArgArray = new String[args.size()];
			for (int i = 0; i < argTypes.length; i++) {
				argTypes[i] = String.class;
				stringArgArray[i] = (String)args.get(i);
			}
		}

		Method method = null;
		@SuppressWarnings("null")
		S instance = null;

		// First try with provided, non-default factory methods
		if (factoryMethod != null && factoryMethod != DEFAULT_FACTORY_METHODS)
			for(final String f: factoryMethod) {
				// Exact match
				try {
					method = klass.getMethod(f, argTypes);
					if (Modifier.isStatic(method.getModifiers())) instance = (S)method.invoke(null, argArray);
				}
				catch (final NoSuchMethodException niceTry) {}

				if (instance != null) return instance;

				// Varargs
				try {
					if (contextualised) {
						method = klass.getMethod(f, Object.class, String[].class);
						if (Modifier.isStatic(method.getModifiers())) instance = (S)method.invoke(null, context, stringArgArray);
					}
					else {
						method = klass.getMethod(f, String[].class);
						if (Modifier.isStatic(method.getModifiers())) instance = (S)method.invoke(null, (Object)stringArgArray);
					}
				}
				catch (final NoSuchMethodException niceTry) {}

				if (instance != null) return instance;
			}

		Constructor<? extends S> constr;
		// Exact match
		try {
			constr = klass.getConstructor(argTypes);
			instance = constr.newInstance(argArray);
		}
		catch (final NoSuchMethodException niceTry) {}

		if (instance != null) return instance;

		// Varargs
		try {
			if (contextualised) {
				constr = klass.getConstructor(Object.class, String[].class);
				return constr.newInstance(context, stringArgArray);
			}
			else {
				constr = klass.getConstructor(String[].class);
				return constr.newInstance((Object)stringArgArray);
			}
		}
		catch (final NoSuchMethodException e) {
			// Second try with default factory methods (copy-and-paste code)
			assert factoryMethod == null || factoryMethod == DEFAULT_FACTORY_METHODS;
			if (factoryMethod == DEFAULT_FACTORY_METHODS)
				for(final String f: factoryMethod) {
					// Exact match
					try {
						method = klass.getMethod(f, argTypes);
						if (Modifier.isStatic(method.getModifiers())) instance = (S)method.invoke(null, argArray);
					}
					catch (final NoSuchMethodException niceTry) {}

					if (instance != null) return instance;

					// Varargs
					try {
						if (contextualised) {
							method = klass.getMethod(f, Object.class, String[].class);
							if (Modifier.isStatic(method.getModifiers())) instance = (S)method.invoke(null, context, stringArgArray);
						}
						else {
							method = klass.getMethod(f, String[].class);
							if (Modifier.isStatic(method.getModifiers())) instance = (S)method.invoke(null, (Object)stringArgArray);
						}
					}
					catch (final NoSuchMethodException niceTry) {}

					if (instance != null) return instance;
				}

			throw new NoSuchMethodException(contextualised
					? "No contextual constructor " + (factoryMethod != null ? "or factory method " : "") + "with " + stringArgArray.length + " strings as argument for class " + klass.getName()
					: "No constructor " + (factoryMethod != null ? "or factory method " : "") + " with " + stringArgArray.length + " strings as argument for class " + klass.getName());
		}
	}

	/** Generates a parseable representation of an object fetching by reflection a <code>toSpec()</code> method, or using the class name.
	 *
	 * <p>The standard approach to generate a parseable representation would be to have some interface specifying a no-arg <code>toSpec()</code>
	 * method returning a {@link String}. Since most of the typically parsed objects are singletons, and often one does not need to save a parseable
	 * representation, we rather fetch such a method if available, but we will otherwise return just the class name.
	 *
	 * @param o an object.
	 * @return hopefully, a parseable representation of the object.
	 * @see #fromSpec(String, Class, String[], String[])
	 */

	public static String toSpec(final Object o) {
		Method toSpec = null;
		try {
			toSpec = o.getClass().getMethod("toSpec");
		}
		catch (final Exception e) {}

		if (toSpec != null) try {
			return (String)toSpec.invoke(o);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}

		return o.getClass().getName();
	}
}
