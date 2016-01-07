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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;

// / Keeps track of all known databases.
/**
 * The database manager keeps track of the database configurations that are known to
 * com.google.security.zynamics.binnavi. It can be used to access the individual database
 * configurations, to create new database configurations, or to delete existing instances.
 */
public final class DatabaseManager implements Iterable<Database>, ApiObject<IDatabaseManager> {
  /**
   * Wrapped internal database manager object.
   */
  private final IDatabaseManager m_manager;

  /**
   * Managed databases.
   */
  private final List<Database> m_databases = new ArrayList<Database>();

  /**
   * Listeners that are notified about changes in the database manager.
   */
  private final ListenerProvider<IDatabaseManagerListener> m_listeners =
      new ListenerProvider<IDatabaseManagerListener>();

  /**
   * Keeps the API database manager object synchronized with the internal database manager object.
   */
  private final InternalDatabaseListener m_internalListener = new InternalDatabaseListener();

  // / @cond INTERNAL
  /**
   * Creates a new API database manager object.
   * 
   * @param manager The internal database manager object to wrap.
   */
  // / @endcond
  public DatabaseManager(final IDatabaseManager manager) {
    Preconditions.checkNotNull(manager, "Error: Manager argument can't be null");

    m_manager = manager;

    for (final IDatabase database : m_manager) {
      m_databases.add(new Database(database));
    }

    manager.addListener(m_internalListener);
  }

  @Override
  public IDatabaseManager getNative() {
    return m_manager;
  }

  // ESCA-JAVA0138: Takes more than 5 arguments to define the whole database in one step.
  // ! Adds a new database.
  /**
   * Adds a new database configuration to the database manager.
   * 
   * @param description The description of the new database configuration. This is the text that is
   *        displayed in the project tree.
   * @param driver The driver that is used to connect to the database.
   * @param host Host address of the database server.
   * @param name The name of the database on the database server.
   * @param user The user that is used to connect to the database.
   * @param password The password that is used to connect to the database.
   * @param identity The identity under which the current user operates.
   * @param savePassword Flag that indicates whether the password should be saved in the
   *        configuration file.
   * @param autoConnect Flag that indicates whether this a connection to this database is
   *        established automatically when BinNavi starts.
   * @return The created database.
   * 
   * @throws IllegalArgumentException Thrown if any of the passed arguments are null.
   */
  public Database addDatabase(final String description, final String driver, final String host,
      final String name, final String user, final String password, final String identity,
      final boolean savePassword, final boolean autoConnect) {
    Preconditions.checkNotNull(description, "Error: description argument can not be null");
    Preconditions.checkNotNull(driver, "Error: driver argument can not be null");
    Preconditions.checkNotNull(host, "Error: host argument can not be null");
    Preconditions.checkNotNull(name, "Error: name argument can not be null");

    final IDatabase newDatabase =
        m_manager.addDatabase(new CDatabase(description, driver, host, name, user, password,
            identity, savePassword, autoConnect));

    return ObjectFinders.getObject(newDatabase, m_databases);
  }

  // ! Adds a database manager listener.
  /**
   * Adds a listener that is notified about changes in the database manager.
   * 
   * @param listener The listener object that is notified about changes in the database manager.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         database manager.
   */
  public void addListener(final IDatabaseManagerListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Databases managed by this manager.
  /**
   * Returns a list of all database configurations known to the database manager.
   * 
   * Please note that the order of the databases inside the manager is not deterministic and may
   * change at any time. If you reuse calculated indices into the returned list you will get
   * undefined behavior.
   * 
   * @return A list of database configurations.
   */
  public List<Database> getDatabases() {
    return new ArrayList<Database>(m_databases);
  }

  // ! Iterates over all managed databases.
  /**
   * Returns an iterator that can be used to iterate over the databases managed by this database
   * manager.
   * 
   * @return An iterator to iterate over the managed database.
   */
  @Override
  public Iterator<Database> iterator() {
    return m_databases.iterator();
  }

  // ! Removes a database from the manager.
  /**
   * Removes a database from the manager. If a database is removed from the database manager it is
   * no longer available in com.google.security.zynamics.binnavi.
   * 
   * @param database The database to remove.
   */
  public void removeDatabase(final Database database) {
    Preconditions.checkNotNull(database, "Error: Database argument can not be null");

    m_manager.removeDatabase(database.getNative());
  }

  // ! Removes a database listener.
  /**
   * Removes a listener object from the database manager.
   * 
   * @param listener The listener object to remove from the database manager.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the database
   *         manager.
   */
  public void removeListener(final IDatabaseManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the database manager.
  /**
   * Returns a string representation of the database manager.
   * 
   * @return A string representation of the database manager.
   */
  @Override
  public String toString() {
    final StringBuffer databaseString = new StringBuffer();

    boolean addComma = false;

    for (final Database database : getDatabases()) {
      if (addComma) {
        databaseString.append(", ");
      }

      addComma = true;

      databaseString.append("'" + database.getDescription() + "'");
    }

    return String.format("Database Manager [%s]", databaseString);
  }

  /**
   * Keeps the API database manager object synchronized with the internal database manager object.
   */
  private class InternalDatabaseListener implements
      com.google.security.zynamics.binnavi.Database.Interfaces.DatabaseManagerListener {
    @Override
    public void addedDatabase(final IDatabaseManager databaseManager, final IDatabase database) {
      final Database newDatabase = new Database(database);

      m_databases.add(newDatabase);

      for (final IDatabaseManagerListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedDatabase(DatabaseManager.this, newDatabase);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedDatabase(final IDatabaseManager databaseManager, final IDatabase database) {
      final Database removedDatabase = ObjectFinders.getObject(database, m_databases);

      removedDatabase.dispose();

      m_databases.remove(removedDatabase);

      for (final IDatabaseManagerListener listener : m_listeners) {
        try {
          listener.removedDatabase(DatabaseManager.this, removedDatabase);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void reorderedDatabases(final IDatabaseManager databaseManager,
        final IDatabase database, final int index) {
      // Irrelevant for the user.
    }
  }
}
