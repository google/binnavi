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

import java.util.Iterator;
import java.util.List;


import com.google.security.zynamics.binnavi.Database.Interfaces.DatabaseManagerListener;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;

public final class MockDatabaseManager implements IDatabaseManager {
  private final List<IDatabase> m_databases = new FilledList<IDatabase>();

  private final ListenerProvider<DatabaseManagerListener> m_listeners =
      new ListenerProvider<DatabaseManagerListener>();

  @Override
  public IDatabase addDatabase(final IDatabase database) {
    m_databases.add(database);

    for (final DatabaseManagerListener listener : m_listeners) {
      listener.addedDatabase(this, database);
    }

    return database;
  }

  @Override
  public void addListener(final DatabaseManagerListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public Iterator<IDatabase> iterator() {
    return m_databases.iterator();
  }

  @Override
  public void moveDatabase(final IDatabase database, final int index) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeDatabase(final IDatabase database) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final DatabaseManagerListener listener) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

}
