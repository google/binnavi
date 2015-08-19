/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.IDatabaseListener;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Project;

public final class MockDatabaseListener implements IDatabaseListener {
  public String events = "";

  public boolean m_allowClosing = true;

  @Override
  public void addedModule(final Database database, final Module module) {
    events += "addedModule;";
  }

  @Override
  public void addedProject(final Database database, final Project project) {
    events += "addedProject;";
  }

  @Override
  public void changedAutoConnect(final Database database, final boolean autoConnect) {
    events += "changedAutoConnect;";
  }

  @Override
  public void changedDescription(final Database database, final String description) {
    events += "changedDescription;";
  }

  @Override
  public void changedDriver(final Database database, final String driver) {
    events += "changedDriver;";
  }

  @Override
  public void changedHost(final Database database, final String host) {
    events += "changedHost;";
  }

  @Override
  public void changedIdentity(final Database database, final String identity) {
    events += "changedIdentity;";
  }

  @Override
  public void changedName(final Database database, final String name) {
    events += "changedName;";
  }

  @Override
  public void changedPassword(final Database database, final String password) {
    events += "changedPassword;";
  }

  @Override
  public void changedSavePassword(final Database database, final boolean savePassword) {
    events += "changedSavePassword;";
  }

  @Override
  public void changedUser(final Database database, final String user) {
    events += "changedUser;";
  }

  @Override
  public void closedDatabase(final Database database) {
    events += "closedDatabase;";
  }

  @Override
  public boolean closingDatabase(final Database database) {
    events += "closingDatabase;";

    return m_allowClosing;
  }

  @Override
  public void deletedModule(final Database database, final Module module) {
    events += "deletedModule;";
  }

  @Override
  public void deletedProject(final Database database, final Project project) {
    events += "deletedProject;";
  }

  @Override
  public void loadedDatabase(final Database database) {
    events += "loadedDatabase;";
  }

  @Override
  public void openedDatabase(final Database database) {
    events += "openedDatabase;";
  }

}
