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
package com.google.security.zynamics.binnavi.Database;

import java.util.List;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;


/**
 * Listener class for objects that want to be notified about only a few database events.
 */
public class CDatabaseListenerAdapter implements IDatabaseListener {
  @Override
  public void addedModule(final IDatabase database, final INaviModule module) {
    // Empty default implementation
  }

  @Override
  public void addedProject(final IDatabase connection, final INaviProject newProject) {
    // Empty default implementation
  }

  @Override
  public void changedAutoConnect(final IDatabase database, final boolean autoConnect) {
    // Empty default implementation
  }

  @Override
  public void changedDescription(final IDatabase database, final String description) {
    // Empty default implementation
  }

  @Override
  public void changedDriver(final IDatabase database, final String driver) {
    // Empty default implementation
  }

  @Override
  public void changedHost(final IDatabase database, final String host) {
    // Empty default implementation
  }

  @Override
  public void changedIdentity(final IDatabase database, final String identity) {
    // Empty default implementation
  }

  @Override
  public void changedName(final IDatabase database, final String name) {
    // Empty default implementation
  }

  @Override
  public void changedPassword(final IDatabase database, final String password) {
    // Empty default implementation
  }

  @Override
  public void changedRawModules(final IDatabase database, final List<INaviRawModule> oldModules,
      final List<INaviRawModule> newModules) {
    // Empty default implementation
  }

  @Override
  public void changedSavePassword(final IDatabase database, final boolean savePassword) {
    // Empty default implementation
  }

  @Override
  public void changedUser(final IDatabase database, final String user) {
    // Empty default implementation
  }

  @Override
  public void closedDatabase(final IDatabase database) {
    // Empty default implementation
  }

  @Override
  public boolean closingDatabase(final IDatabase database) {
    // Empty default implementation
    return true;
  }

  @Override
  public void deletedModule(final IDatabase database, final INaviModule module) {
    // Empty default implementation
  }

  @Override
  public void deletedProject(final IDatabase database, final INaviProject project) {
    // Empty default implementation
  }

  @Override
  public void deletedRawModule(final IDatabase database, final INaviRawModule project) {
    // Empty default implementation
  }

  @Override
  public void loadedDatabase(final IDatabase database) {
    // Empty default implementation
  }

  @Override
  public boolean loading(final LoadEvents event, final int counter) {
    // Empty default implementation
    return true;
  }

  @Override
  public void openedDatabase(final IDatabase database) {
    // Empty default implementation
  }
}
