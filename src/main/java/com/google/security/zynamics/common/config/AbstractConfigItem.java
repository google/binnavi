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

/**
 * A simple class that can load and store itself in a {@link TypedPropertiesWrapper}.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public abstract class AbstractConfigItem {

  /** Loads configuration data from the specified properties object. */
  public abstract void load(final TypedPropertiesWrapper properties);

  public void loadWithKeyPrefix(final TypedPropertiesWrapper properties, final String keyPrefix) {
    final String savedPrefix = properties.getKeyPrefix();
    properties.setKeyPrefix(keyPrefix);
    load(properties);
    properties.setKeyPrefix(savedPrefix);
  }

  /** Saves configuration data to the specified properties object. */
  public abstract void store(final TypedPropertiesWrapper properties);

  public void storeWithKeyPrefix(final TypedPropertiesWrapper properties, final String keyPrefix) {
    final String savedPrefix = properties.getKeyPrefix();
    properties.setKeyPrefix(keyPrefix);
    store(properties);
    properties.setKeyPrefix(savedPrefix);
  }
}
