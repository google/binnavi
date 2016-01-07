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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.DatabaseManagerListener;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * The database manager is used to keep track of all databases configured by the user. These are the
 * databases that can be accessed from com.google.security.zynamics.binnavi.
 */
public final class CDatabaseManager implements IDatabaseManager, Iterable<IDatabase> {
  /**
   * Only valid instance of the database manager.
   */
  private static CDatabaseManager m_instance = new CDatabaseManager();

  /**
   * The list of known databases.
   */
  private final List<IDatabase> m_databases = new ArrayList<IDatabase>();

  /**
   * The listeners that are notified about changes in the database manager.
   */
  private final ListenerProvider<DatabaseManagerListener> m_listeners =
      new ListenerProvider<DatabaseManagerListener>();

  /**
   * Creates a new Database manager object.
   */
  private CDatabaseManager() {
  }

  /**
   * Returns the only valid instance of the database manager.
   * 
   * @return The only valid instance of the database manager.
   */
  public static CDatabaseManager instance() {
    return m_instance;
  }

  @Override
  public IDatabase addDatabase(final IDatabase database) {
    Preconditions.checkNotNull(database, "IE00694: Database argument can't be null");
    Preconditions.checkArgument(!m_databases.contains(database),
        "IE00695: Database object can't be added more than once");

    m_databases.add(database);

    for (final DatabaseManagerListener listener : m_listeners) {
      try {
        listener.addedDatabase(this, database);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return database;
  }

  @Override
  public void addListener(final DatabaseManagerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * This iterator can be used to iterate over all known databases.
   */
  @Override
  public Iterator<IDatabase> iterator() {
    return m_databases.iterator();
  }

  @Override
  public void moveDatabase(final IDatabase database, final int index) {
    Preconditions.checkNotNull(database, "IE00696: Database argument can't be null");
    Preconditions.checkArgument(m_databases.remove(database),
        "IE00697: The database is not known to the database manager");

    m_databases.add(index - 1, database);

    for (final DatabaseManagerListener listener : m_listeners) {
      try {
        listener.reorderedDatabases(this, database, index);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeDatabase(final IDatabase database) {
    Preconditions.checkNotNull(database, "IE00698: Database argument can't be null");
    Preconditions.checkArgument(m_databases.remove(database),
        "IE00699: The database is not known to the database manager");

    for (final DatabaseManagerListener listener : m_listeners) {
      try {
        listener.removedDatabase(this, database);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeListener(final DatabaseManagerListener listener) {
    m_listeners.removeListener(listener);
  }
}
