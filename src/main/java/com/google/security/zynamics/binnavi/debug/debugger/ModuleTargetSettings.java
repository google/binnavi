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
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import java.util.ArrayList;
import java.util.List;

/**
 * Module target for debugging modules.
 */
public final class ModuleTargetSettings implements DebugTargetSettings {
  /**
   * The module to debug.
   */
  private final INaviModule module;

  /**
   * Creates a new module target object.
   *
   * @param module The module to debug.
   */
  public ModuleTargetSettings(final INaviModule module) {
    this.module = module;
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final List<UnrelocatedAddress> addresses,
      final boolean all) {
    try {
      return module.getViewsWithAddresses(addresses, all);
    } catch (final CouldntLoadDataException e) {
      NaviLogger.severe("Error: Could not load data. Exception: %s", e);
    }
    return new ArrayList<>();
  }

  @Override
  public String readSetting(final String key) throws CouldntLoadDataException {
    return module.readSetting(key);
  }

  @Override
  public void writeSetting(final String key, final String value) throws CouldntSaveDataException {
    module.writeSetting(key, value);
  }
}
