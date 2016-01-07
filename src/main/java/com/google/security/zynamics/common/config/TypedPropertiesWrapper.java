/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.common.config;

import java.awt.Color;
import java.util.Properties;

/**
 * Class that wraps a Properties object to provide useful getter and setter methods for the
 * supported default types.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class TypedPropertiesWrapper {

  private final Properties properties;
  private String keyPrefix;

  public TypedPropertiesWrapper() {
    this(new Properties());
  }

  public TypedPropertiesWrapper(final Properties properties) {
    this.properties = properties;
    keyPrefix = "";
  }

  /** Gets the wrapped properties object. Useful for calling methods like loadFromXML(), etc. */
  public Properties getProperties() {
    return properties;
  }

  public void setKeyPrefix(final String prefix) {
    keyPrefix = prefix;
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public boolean getBoolean(final String key, final boolean defaultValue) {
    return Boolean.valueOf(
        properties.getProperty(keyPrefix + key, String.valueOf(defaultValue)));
  }

  public void setBoolean(final String key, final boolean value) {
    properties.setProperty(keyPrefix + key, String.valueOf(value));
  }

  public Color getColor(final String key, final Color defaultValue) {
    try {
      return Color.decode(properties.getProperty(
          keyPrefix + key, String.valueOf(defaultValue.getRGB())));
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  public void setColor(final String key, final Color value) {
    properties.setProperty(keyPrefix + key, String.valueOf(value.getRGB()));
  }

  public int getInteger(final String key, final int defaultValue) {
    try {
      return Integer.valueOf(properties.getProperty(
          keyPrefix + key, String.valueOf(defaultValue)));
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  public void setInteger(final String key, final int value) {
    properties.setProperty(keyPrefix + key, String.valueOf(value));
  }

  public long getLong(final String key, final long defaultValue) {
    try {
      return Long.valueOf(properties.getProperty(
          keyPrefix + key, String.valueOf(defaultValue)));
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  public void setLong(final String key, final long value) {
    properties.setProperty(keyPrefix + key, String.valueOf(value));
  }

  public String getString(final String key, final String defaultValue) {
    return properties.getProperty(keyPrefix + key, defaultValue);
  }

  public void setString(final String key, final String value) {
    properties.setProperty(keyPrefix + key, value);
  }
}