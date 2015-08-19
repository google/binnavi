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
package com.google.security.zynamics.binnavi.Database.MockClasses;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.CDatabaseConfiguration;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.sql.ResultSet;

public final class MockDatabase implements IDatabase {
  private final ListenerProvider<IDatabaseListener> listeners =
      new ListenerProvider<IDatabaseListener>();

  private boolean isLoaded = true;

  private final MockDatabaseContent content;

  private final CDatabaseConfiguration m_descriptionX = new CDatabaseConfiguration(this,
      listeners,
      "Mock Database",
      "postgres",
      "",
      "",
      "",
      "",
      CommonTestObjects.TEST_USER_1.getUserName(),
      true,
      true);

  /**
   * The SQL provider that is used to communicate with the database. If the database is not
   * connected, this object is null.
   */
  private final SQLProvider provider;

  public MockDatabase() {
    this(new MockSqlProvider());
  }

  public MockDatabase(final MockSqlProvider mockSqlProvider) {
    provider = mockSqlProvider;
    content =
        new MockDatabaseContent(this, new DebuggerTemplateManager(mockSqlProvider), listeners);
  }

  @Override
  public void addListener(final IDatabaseListener listener) {
    listeners.addListener(listener);
  }

  public void addProject(final CProject project) {
    content.addProject(project);
  }

  @Override
  public boolean close() {
    for (final IDatabaseListener listener : listeners) {
      try {
        if (!listener.closingDatabase(this)) {
          return false;
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    for (final IDatabaseListener listener : listeners) {
      try {
        listener.closedDatabase(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    isLoaded = false;

    return true;
  }

  @Override
  public void connect() {
    for (final IDatabaseListener listener : listeners) {
      try {
        listener.openedDatabase(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public ResultSet executeQuery(final String query) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public CDatabaseConfiguration getConfiguration() {
    return m_descriptionX;
  }

  @Override
  public MockDatabaseContent getContent() {
    return content;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return true;
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return this.provider == provider;
  }

  @Override
  public boolean isConnected() {
    return isLoaded;
  }

  @Override
  public boolean isConnecting() {
    return false;
  }

  @Override
  public boolean isLoaded() {
    return isLoaded;
  }

  @Override
  public boolean isLoading() {
    return false;
  }

  @Override
  public void load() {
    for (final IDatabaseListener listener : listeners) {
      try {
        listener.loadedDatabase(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeListener(final IDatabaseListener listener) {
    listeners.removeListener(listener);
  }

  @Override
  public void update() {}
}
