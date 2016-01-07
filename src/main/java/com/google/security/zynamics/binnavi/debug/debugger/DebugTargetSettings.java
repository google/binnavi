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
package com.google.security.zynamics.binnavi.debug.debugger;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import java.util.List;

/**
 * Interface for objects that represent debug able objects.
 */
public interface DebugTargetSettings {
  List<INaviView> getViewsWithAddresses(final List<UnrelocatedAddress> offset, final boolean all);

  /**
   * Reads a single module setting from the database.
   *
   * @param key Key of the setting to read.
   *
   * @return Value of the setting.
   *
   * @throws CouldntLoadDataException Thrown if the setting value could not be loaded.
   */
  String readSetting(String key) throws CouldntLoadDataException;

  /**
   * Writes a module setting to the database.
   *
   * @param key Key of the setting to write.
   * @param value Value of the setting to write.
   *
   * @throws CouldntSaveDataException Thrown if the setting could not be saved.
   */
  void writeSetting(String key, String value) throws CouldntSaveDataException;
}
