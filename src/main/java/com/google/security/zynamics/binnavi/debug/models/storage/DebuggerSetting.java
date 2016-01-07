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
package com.google.security.zynamics.binnavi.debug.models.storage;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;

/**
 * Provides a mechanism to persistently store data determined by the derived class to the database.
 * The storage is debugger and module specific, respectively.
 *
 * @param <T> The type of class to be serialized.
 */
public abstract class DebuggerSetting<T> {
  private final IDebugger debugger;
  private final DebugTargetSettings debugTargetSettings;

  public DebuggerSetting(final IDebugger debugger, final DebugTargetSettings debugTarget) {
    this.debugger = debugger;
    debugTargetSettings = debugTarget;
  }

  /**
   * Generate a key which is unique among different debugger and modules, respectively.
   *
   * @param keySuffix Key suffix which needs to be supplied by the derived class.
   *
   * @return The unique key string.
   */
  private String getUniqueKey(final String keySuffix) {
    return "dbg_" + debugger.getId() + "_" + keySuffix;
  }

  /**
   * Read a string and return its boolean representation.
   *
   * @param key The key which is used to lookup the value.
   *
   * @return The boolean representation of the corresponding value.
   *
   * @throws CouldntLoadDataException
   */
  protected boolean readBoolSetting(final String key) throws CouldntLoadDataException {
    return Boolean.parseBoolean(readSetting(key));
  }

  /**
   * Lookup a string value based on the supplied key.
   *
   * @param key The key which is used to lookup the value.
   *
   * @return The corresponding string value.
   *
   * @throws CouldntLoadDataException
   */
  protected String readSetting(final String key) throws CouldntLoadDataException {
    return debugTargetSettings.readSetting(getUniqueKey(key));
  }

  protected void writeBoolSetting(final String key, final boolean value)
      throws CouldntSaveDataException {
    writeSetting(key, Boolean.toString(value));
  }

  protected void writeSetting(final String key, final String value)
      throws CouldntSaveDataException {
    debugTargetSettings.writeSetting(getUniqueKey(key), value);
  }

  public abstract T deserialize() throws CouldntLoadDataException;

  public abstract void serialize(final T instance) throws CouldntSaveDataException;
}
