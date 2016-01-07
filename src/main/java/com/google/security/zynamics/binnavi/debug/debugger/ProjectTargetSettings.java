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
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import java.util.ArrayList;
import java.util.List;

/**
 * Project target for debugging projects.
 */
public final class ProjectTargetSettings implements DebugTargetSettings {
  /**
   * The project to debug.
   */
  private final INaviProject project;

  /**
   * Creates a new project target object.
   *
   * @param project The project to debug.
   */
  public ProjectTargetSettings(final INaviProject project) {
    this.project = project;
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final List<UnrelocatedAddress> addresses,
      final boolean all) {
    try {
      return project.getViewsWithAddresses(addresses, all);
    } catch (final CouldntLoadDataException e) {
      NaviLogger.severe("Error: Could not load data. Exception: %s", e);
    }
    return new ArrayList<>();
  }

  @Override
  public String readSetting(final String key) throws CouldntLoadDataException {
    return project.readSetting(key);
  }

  @Override
  public void writeSetting(final String key, final String value) throws CouldntSaveDataException {
    project.writeSetting(key, value);
  }
}
