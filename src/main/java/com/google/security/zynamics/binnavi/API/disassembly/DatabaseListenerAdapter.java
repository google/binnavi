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
package com.google.security.zynamics.binnavi.API.disassembly;

// / Adapter class for databases
/**
 * Adapter class that can be used by objects that want to listen on databases but only need to
 * process few events.
 */
public class DatabaseListenerAdapter implements IDatabaseListener {
  @Override
  public void addedModule(final Database database, final Module module) {
    // Adapter method
  }

  @Override
  public void addedProject(final Database database, final Project project) {
    // Adapter method
  }

  @Override
  public void changedAutoConnect(final Database database, final boolean autoConnect) {
    // Adapter method
  }

  @Override
  public void changedDescription(final Database database, final String description) {
    // Adapter method
  }

  @Override
  public void changedDriver(final Database database, final String driver) {
    // Adapter method
  }

  @Override
  public void changedHost(final Database database, final String host) {
    // Adapter method
  }

  @Override
  public void changedIdentity(final Database database, final String identity) {
    // Adapter method
  }

  @Override
  public void changedName(final Database database, final String name) {
    // Adapter method
  }

  @Override
  public void changedPassword(final Database database, final String password) {
    // Adapter method
  }

  @Override
  public void changedSavePassword(final Database database, final boolean savePassword) {
    // Adapter method
  }

  @Override
  public void changedUser(final Database database, final String user) {
    // Adapter method
  }

  @Override
  public void closedDatabase(final Database database) {
    // Adapter method
  }

  @Override
  public boolean closingDatabase(final Database database) {
    return true;
  }

  @Override
  public void deletedModule(final Database database, final Module module) {
    // Adapter method
  }

  @Override
  public void deletedProject(final Database database, final Project project) {
    // Adapter method
  }

  @Override
  public void loadedDatabase(final Database database) {
    // Adapter method
  }

  @Override
  public void openedDatabase(final Database database) {
    // Adapter method
  }
}
