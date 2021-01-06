/*
 * DSI utilities
 *
 * Copyright (C) 2005-2021 Sebastiano Vigna
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

package it.unimi.dsi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

/**
 * An extension of {@link org.apache.commons.configuration2.PropertiesConfiguration} providing
 * setters for primitive types, a simpler {@linkplain #save(CharSequence) way to save preferences}
 * and transparent handling of {@link java.lang.Enum} lowercased keys.
 *
 * <p>
 * All accessors defined in {@link org.apache.commons.configuration2.PropertiesConfiguration} have a
 * polymorphic counterpart taking an {@link java.lang.Enum} instead of a string:
 * {@link java.lang.Enum#name()} <strong>and {@link java.lang.String#toLowerCase()}</strong> are
 * applied before delegating to the corresponding string-based method. (This apparently wierd choice
 * is due to the need to accommodate the upper-case standard for {@link java.lang.Enum} elements and
 * the lower-case standard for property keys.)
 *
 * <p>
 * Additionally, instances of this class can be serialised.
 */
public class Properties extends PropertiesConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	/** A delimiter mimicking the pre-version 2 list behavior. */
	private static final DefaultListDelimiterHandler COMMA_LIST_DELIMITER_HANDLER = new DefaultListDelimiterHandler(',');
	/** A file handler for input/output. */
	private transient FileHandler fileHandler;

	public Properties() {
		getLayout().setGlobalSeparator("=");
		setListDelimiterHandler(COMMA_LIST_DELIMITER_HANDLER);
		fileHandler = new FileHandler(this);
		fileHandler.setEncoding(StandardCharsets.UTF_8.toString());
	}

	public Properties(final String filename) throws ConfigurationException {
		this();
		fileHandler.setFileName(filename);
		fileHandler.load();
	}

	public Properties(final File file) throws ConfigurationException {
		this();
		fileHandler.setFile(file);
		fileHandler.load();
	}

	public Properties(final URL url) throws ConfigurationException {
		this();
		fileHandler.setURL(url);
		fileHandler.load();
	}

	public Properties(final InputStream inputStream) throws ConfigurationException {
		this();
		fileHandler.load(inputStream);
	}

	/** Saves the configuration to the specified file.
	 *
	 * @param filename a file name.
	 */

	public void save(final CharSequence filename) throws ConfigurationException, IOException {
		final FileOutputStream os = new FileOutputStream(filename.toString());
		fileHandler.save(os);
		os.close();
	}

	/**
	 * Saves the configuration to the specified file.
	 *
	 * @param file a file.
	 */

	public void save(final File file) throws ConfigurationException, IOException {
		save(file.toString());
	}

	/**
	 * Saves the configuration to an output stream.
	 *
	 * @param os an output stream.
	 */
	public void save(final OutputStream os) throws ConfigurationException {
		fileHandler.save(os);
	}

	/** Loads a configuration from a specified file.
	 *
	 * @param filename a file name.
	 */

	public void load(final CharSequence filename) throws ConfigurationException, IOException {
		final FileInputStream is = new FileInputStream(filename.toString());
		fileHandler.load(is);
		is.close();
	}

	/**
	 * Loads a configuration from a specified file.
	 *
	 * @param file a file.
	 */

	public void load(final File file) throws ConfigurationException, IOException {
		load(file.toString());
	}

	/**
	 * Loads a configuration from an input stream.
	 *
	 * @param is an input stream.
	 */
	public void load(final InputStream is) throws ConfigurationException {
		fileHandler.load(is);
	}

	/** Adds all properties from the given configuration.
	 *
	 * <p>Properties from the new configuration will clear properties from the first one.
	 *
	 * @param configuration a configuration.
	 * */
	public void addAll(final Configuration configuration) {
		new ConfigurationMap(this).putAll(new ConfigurationMap(configuration));
	}

	// Methods to add properties represented by primitive types easily

	public void addProperties(final String key, final String[] s) {
		for (final String element : s) super.addProperty(key, element);
	}

	public void addProperty(final String key, final boolean b) {
		super.addProperty(key, Boolean.valueOf(b));
	}

	public void setProperty(final String key, final boolean b) {
		super.setProperty(key, Boolean.valueOf(b));
	}

	public void addProperty(final String key, final byte b) {
		super.addProperty(key, Byte.valueOf(b));
	}

	public void setProperty(final String key, final byte b) {
		super.setProperty(key, Byte.valueOf(b));
	}

	public void addProperty(final String key, final short s) {
		super.addProperty(key, Short.valueOf(s));
	}

	public void setProperty(final String key, final short s) {
		super.setProperty(key, Short.valueOf(s));
	}

	public void addProperty(final String key, final char c) {
		super.addProperty(key, Character.valueOf(c));
	}

	public void setProperty(final String key, final char b) {
		super.setProperty(key, Character.valueOf(b));
	}

	public void addProperty(final String key, final int i) {
		super.addProperty(key, Integer.valueOf(i));
	}

	public void setProperty(final String key, final int i) {
		super.setProperty(key, Integer.valueOf(i));
	}

	public void addProperty(final String key, final long l) {
		super.addProperty(key, Long.valueOf(l));
	}

	public void setProperty(final String key, final long l) {
		super.setProperty(key, Long.valueOf(l));
	}

	public void addProperty(final String key, final float f) {
		super.addProperty(key, Float.valueOf(f));
	}

	public void setProperty(final String key, final float f) {
		super.setProperty(key, Float.valueOf(f));
	}

	public void addProperty(final String key, final double d) {
		super.addProperty(key, Double.valueOf(d));
	}

	public void setProperty(final String key, final double d) {
		super.setProperty(key, Double.valueOf(d));
	}

	// Same methods, but with Enum keys

	public void addProperties(final Enum<?> key, final String[] s) {
		for (final String element : s) super.addProperty(key.name().toLowerCase(), element);
	}

	public void addProperty(final Enum<?> key, final boolean b) {
		super.addProperty(key.name().toLowerCase(), Boolean.valueOf(b));
	}

	public void setProperty(final Enum<?> key, final boolean b) {
		super.setProperty(key.name().toLowerCase(), Boolean.valueOf(b));
	}

	public void addProperty(final Enum<?> key, final byte b) {
		super.addProperty(key.name().toLowerCase(), Byte.valueOf(b));
	}

	public void setProperty(final Enum<?> key, final byte b) {
		super.setProperty(key.name().toLowerCase(), Byte.valueOf(b));
	}

	public void addProperty(final Enum<?> key, final short s) {
		super.addProperty(key.name().toLowerCase(), Short.valueOf(s));
	}

	public void setProperty(final Enum<?> key, final short s) {
		super.setProperty(key.name().toLowerCase(), Short.valueOf(s));
	}

	public void addProperty(final Enum<?> key, final char c) {
		super.addProperty(key.name().toLowerCase(), Character.valueOf(c));
	}

	public void setProperty(final Enum<?> key, final char b) {
		super.setProperty(key.name().toLowerCase(), Character.valueOf(b));
	}

	public void addProperty(final Enum<?> key, final int i) {
		super.addProperty(key.name().toLowerCase(), Integer.valueOf(i));
	}

	public void setProperty(final Enum<?> key, final int i) {
		super.setProperty(key.name().toLowerCase(), Integer.valueOf(i));
	}

	public void addProperty(final Enum<?> key, final long l) {
		super.addProperty(key.name().toLowerCase(), Long.valueOf(l));
	}

	public void setProperty(final Enum<?> key, final long l) {
		super.setProperty(key.name().toLowerCase(), Long.valueOf(l));
	}

	public void addProperty(final Enum<?> key, final float f) {
		super.addProperty(key.name().toLowerCase(), Float.valueOf(f));
	}

	public void setProperty(final Enum<?> key, final float f) {
		super.setProperty(key.name().toLowerCase(), Float.valueOf(f));
	}

	public void addProperty(final Enum<?> key, final double d) {
		super.addProperty(key.name().toLowerCase(), Double.valueOf(d));
	}

	public void setProperty(final Enum<?> key, final double d) {
		super.setProperty(key.name().toLowerCase(), Double.valueOf(d));
	}

	// Polimorphic Enum version of superclass string-based methods

	public boolean containsKey(final Enum<?> key) {
		return containsKey(key.name().toLowerCase());
	}

	public Object getProperty(final Enum<?> key) {
		return getProperty(key.name().toLowerCase());
	}

	public void addProperty(final Enum<?> key, final Object arg) {
		addProperty(key.name().toLowerCase(), arg);
	}

	public BigDecimal getBigDecimal(final Enum<?> key, final BigDecimal arg) {
		return getBigDecimal(key.name().toLowerCase(), arg);
	}

	public BigDecimal getBigDecimal(final Enum<?> key) {
		return getBigDecimal(key.name().toLowerCase());
	}

	public BigInteger getBigInteger(final Enum<?> key, final BigInteger arg) {
		return getBigInteger(key.name().toLowerCase(), arg);
	}

	public BigInteger getBigInteger(final Enum<?> key) {
		return getBigInteger(key.name().toLowerCase());
	}

	public boolean getBoolean(final Enum<?> key, final boolean arg) {
		return getBoolean(key.name().toLowerCase(), arg);
	}

	public Boolean getBoolean(final Enum<?> key, final Boolean arg) {
		return getBoolean(key.name().toLowerCase(), arg);
	}

	public boolean getBoolean(final Enum<?> key) {
		return getBoolean(key.name().toLowerCase());
	}

	public byte getByte(final Enum<?> key, final byte arg) {
		return getByte(key.name().toLowerCase(), arg);
	}

	public Byte getByte(final Enum<?> key, final Byte arg) {
		return getByte(key.name().toLowerCase(), arg);
	}

	public byte getByte(final Enum<?> key) {
		return getByte(key.name().toLowerCase());
	}

	public double getDouble(final Enum<?> key, final double arg) {
		return getDouble(key.name().toLowerCase(), arg);
	}

	public Double getDouble(final Enum<?> key, final Double arg) {
		return getDouble(key.name().toLowerCase(), arg);
	}

	public double getDouble(final Enum<?> key) {
		return getDouble(key.name().toLowerCase());
	}

	public float getFloat(final Enum<?> key, final float arg) {
		return getFloat(key.name().toLowerCase(), arg);
	}

	public Float getFloat(final Enum<?> key, final Float arg) {
		return getFloat(key.name().toLowerCase(), arg);
	}

	public float getFloat(final Enum<?> key) {
		return getFloat(key.name().toLowerCase());
	}

	public int getInt(final Enum<?> key, final int arg) {
		return getInt(key.name().toLowerCase(), arg);
	}

	public int getInt(final Enum<?> key) {
		return getInt(key.name().toLowerCase());
	}

	public Integer getInteger(final Enum<?> key, final Integer arg) {
		return getInteger(key.name().toLowerCase(), arg);
	}

	public Iterator<?> getKeys(final Enum<?> key) {
		return getKeys(key.name().toLowerCase());
	}

	public List<Object> getList(final Enum<?> key, final List<Object> arg) {
		return getList(key.name().toLowerCase(), arg);
	}

	public List<?> getList(final Enum<?> key) {
		return getList(key.name().toLowerCase());
	}

	public long getLong(final Enum<?> key, final long arg) {
		return getLong(key.name().toLowerCase(), arg);
	}

	public Long getLong(final Enum<?> key, final Long arg) {
		return getLong(key.name().toLowerCase(), arg);
	}

	public long getLong(final Enum<?> key) {
		return getLong(key.name().toLowerCase());
	}

	public java.util.Properties getProperties(final Enum<?> key, final java.util.Properties arg) {
		return getProperties(key.name().toLowerCase(), arg);
	}

	public java.util.Properties getProperties(final Enum<?> key) {
		return getProperties(key.name().toLowerCase());
	}

	public short getShort(final Enum<?> key, final short arg) {
		return getShort(key.name().toLowerCase(), arg);
	}

	public Short getShort(final Enum<?> key, final Short arg) {
		return getShort(key.name().toLowerCase(), arg);
	}

	public short getShort(final Enum<?> key) {
		return getShort(key.name().toLowerCase());
	}

	public String getString(final Enum<?> key, final String arg) {
		return getString(key.name().toLowerCase(), arg);
	}

	public String getString(final Enum<?> key) {
		return getString(key.name().toLowerCase());
	}

	public String[] getStringArray(final Enum<?> key) {
		return getStringArray(key.name().toLowerCase());
	}

	public void setProperty(final Enum<?> key, final Object arg) {
		setProperty(key.name().toLowerCase(), arg);
	}

	public Configuration subset(final Enum<?> key) {
		return subset(key.name().toLowerCase());
	}

	@Override
	public String toString() {
		return ConfigurationUtils.toString(this);
	}

	@Override
	public int hashCode() {
		int h = 0;
		for(final Iterator<?> i = getKeys(); i.hasNext();) h = h * 31 + Arrays.hashCode(getStringArray((String)i.next()));
		return h;
	}

	/** Returns true if the provided object is equal to this set of properties.
	 *
	 * <p>Equality between set of properties happens when the keys are the same,
	 * and the list of strings associated with each key is the same. Note that the order
	 * in which different keys appear in a property file is irrelevant, but <em>the order
	 * between properties with the same key is significant</em>.
	 *
	 * <p>Due to the strictness of the check (e.g., no number conversion is performed)
	 * this method is mainly useful when writing tests.
	 *
	 * @return true if the argument is equal to this set of properties.
	 */

	@Override
	public boolean equals(final Object o) {
		if (! (o instanceof Properties)) return false;
		final Properties p = (Properties)o;
		for(final Iterator<?> i = getKeys(); i.hasNext();) {
			final String key = (String)i.next();
			final String[] value = p.getStringArray(key);
			if (value == null || ! Arrays.equals(getStringArray(key), value)) return false;
		}

		for(final Iterator<?> i = p.getKeys(); i.hasNext();)
			if (getStringArray((String)i.next()) == null) return false;

		return true;
	}

	private void writeObject(final ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		fileHandler = new FileHandler(this);
		fileHandler.setEncoding(StandardCharsets.UTF_8.toString());
		try {
			fileHandler.save(s);
		}
		catch (final ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		fileHandler = new FileHandler(this);
		fileHandler.setEncoding(StandardCharsets.UTF_8.toString());
		try {
			fileHandler.load(s);
		}
		catch (final ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}
