/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.LoadEvents;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Importers.CBinExportImporter;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;

// ESCA-JAVA0136:
// / Represents a single database configuration.
/**
 * Database objects provide configurations for individual databases and - once loaded - access to
 * the projects, modules, and other objects stored in that database.
 * 
 * To use database objects it is important to understand the lifecycle of database objects. After
 * the creation of a database object you have to connect to the database first. Once the connection
 * is established it is possible to load the data from the database. If loading was successful you
 * have full access to the projects, modules, and other objects stored in the database.
 */
public final class Database implements ApiObject<IDatabase> {

  /**
   * Wrapped internal database object.
   */
  private final IDatabase m_database;

  /**
   * Modules stored in the database.
   */
  private final List<Module> m_modules = new ArrayList<Module>();

  /**
   * Projects stored in the database.
   */
  private final List<Project> m_projects = new ArrayList<Project>();

  /**
   * View tag manager of the database.
   */
  private TagManager m_viewTagManager;

  /**
   * Node tag manager of the database.
   */
  private TagManager m_nodeTagManager;

  /**
   * Debugger template manager of the database.
   */
  private DebuggerTemplateManager m_debuggerTemplateManager;

  /**
   * Listeners that are notified about changes in the database.
   */
  private final ListenerProvider<IDatabaseListener> m_listeners =
      new ListenerProvider<IDatabaseListener>();

  /**
   * Keeps the API database object synchronized with the internal database object.
   */
  private final InternalDatabaseListener m_internalListener = new InternalDatabaseListener();

  // / @cond INTERNAL
  /**
   * Creates a new API database object.
   * 
   * @param database The wrapped internal database object.
   */
  // / @endcond
  public Database(final IDatabase database) {
    Preconditions.checkNotNull(database, "Error: Database argument can't be null");

    m_database = database;

    if (database.isLoaded()) {
      convertData();
    }

    database.addListener(m_internalListener);
  }

  /**
   * Converts the native objects stored in the database to API objects.
   */
  private void convertData() {
    m_viewTagManager = new TagManager(m_database.getContent().getViewTagManager());
    m_nodeTagManager = new TagManager(m_database.getContent().getNodeTagManager());

    m_modules.clear();
    m_projects.clear();

    for (final INaviModule module : m_database.getContent().getModules()) {
      m_modules.add(new Module(this, module, m_nodeTagManager, m_viewTagManager));
    }

    for (final INaviProject project : m_database.getContent().getProjects()) {
      m_projects.add(new Project(this, project, m_nodeTagManager, m_viewTagManager));
    }

    m_debuggerTemplateManager =
        new DebuggerTemplateManager(m_database.getContent().getDebuggerTemplateManager());
  }

  /**
   * Frees all allocated resources of the child objects.
   */
  private void disposedLoadedObjects() {
    if (m_modules != null) {
      for (final Module module : m_modules) {
        module.dispose();
      }

      for (final Project project : m_projects) {
        project.dispose();
      }

      if (m_debuggerTemplateManager != null) {
        m_debuggerTemplateManager.dispose();
      }

      m_viewTagManager = null;
      m_nodeTagManager = null;
      m_modules.clear();
      m_projects.clear();
      m_debuggerTemplateManager = null;
    }
  }

  @Override
  public IDatabase getNative() {
    return m_database;
  }

  // ! Adds a database listener.
  /**
   * Adds an object that is notified about changes in the database.
   * 
   * @param listener The listener object that is notified about changes in the database.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         database.
   */
  public void addListener(final IDatabaseListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Closes the database.
  /**
   * Closes the database.
   */
  public void close() {
    m_database.close();
  }

  // ! Connects to the database.
  /**
   * Opens a connection to the database.
   * 
   * @throws CouldntLoadDriverException Thrown if the driver to be used to connect to the database
   *         could not be loaded.
   * @throws CouldntConnectException Thrown if no connection to the database could be established.
   * @throws InvalidDatabaseException Thrown if the database is in an inconsistent state and can not
   *         be used.
   * @throws CouldntInitializeDatabaseException Thrown if the database could not be initialized for
   *         use with com.google.security.zynamics.binnavi.
   * @throws InvalidDatabaseFormatException Thrown if the database is not a BinNavi 3.0 database.
   */
  public void connect() throws CouldntLoadDriverException, CouldntConnectException,
      InvalidDatabaseException, CouldntInitializeDatabaseException, InvalidDatabaseFormatException {
    try {
      m_database.connect();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException e) {
      throw new CouldntLoadDriverException(e);
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException | LoadCancelledException e) {
      throw new CouldntConnectException(e);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException e) {
      throw new InvalidDatabaseException(e);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException e) {
      throw new CouldntInitializeDatabaseException(e);
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException 
        | com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException e) {
      throw new InvalidDatabaseFormatException(e);
    } 
  }

  // ! Creates a new project.
  /**
   * Creates a new project in the database. The created project is immediately stored in the
   * database. Further save operations are not necessary.
   * 
   * @param name The name of the new project
   * 
   * @return The newly created project.
   * 
   * @throws IllegalArgumentException Thrown if the name argument is null.
   * @throws IllegalStateException Thrown if there is no connection to the database or the database
   *         is not loaded.
   * @throws CouldntSaveDataException Thrown if the project could not be created.
   */
  public Project createProject(final String name) throws CouldntSaveDataException {
    try {
      final INaviProject newProject = m_database.getContent().addProject(name);

      return ObjectFinders.getObject(newProject, m_projects);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Deletes a module from the database.
  /**
   * Deletes a module from the database.
   * 
   * @param module The module to delete from the database.
   * 
   * @throws CouldntDeleteException Thrown if the module could not be deleted from the database.
   */
  public void deleteModule(final Module module) throws CouldntDeleteException {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    try {
      m_database.getContent().delete(module.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  // ! Deletes a project from the database.
  /**
   * Deletes a project from the database.
   * 
   * @param project The project to delete from the database.
   * 
   * @throws CouldntDeleteException Thrown if the project could not be deleted from the database.
   */
  public void deleteProject(final Project project) throws CouldntDeleteException {
    Preconditions.checkNotNull(project, "Error: Project argument can not be null");

    try {
      m_database.getContent().delete(project.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  // / @cond INTERNAL
  /**
   * Frees all allocated resources.
   */
  // / @endcond
  public void dispose() {
    m_database.removeListener(m_internalListener);

    disposedLoadedObjects();
  }

  // ! Query the database with a raw SQL query.
  /**
   * Executes a raw SQL query on the database and returns the result set of the query.
   * 
   * @param statement The SQL query to issue.
   * 
   * @return The results of the query. Plugins using this method have to close this result set
   *         themselves when they are done.
   * 
   * @throws SQLException Thrown if the query failed for whatever reason.
   */
  public ResultSet executeQuery(final PreparedStatement statement) throws SQLException {
    Preconditions.checkNotNull(statement, "Statement argument can not be null");

    return statement.executeQuery();
  }

  // ! Query the database with a raw SQL query.
  /**
   * Executes a raw SQL query on the database and returns the result set of the query.
   * 
   * @param query The query to issue.
   * 
   * @return The results of the query. Plugins using this method have to close this result set
   *         themselves when they are done.
   * 
   * @throws SQLException Thrown if the query failed for whatever reason.
   */
  public ResultSet executeQuery(final String query) throws SQLException {
    Preconditions.checkNotNull(query, "Query argument can not be null");

    return m_database.executeQuery(query);
  }

  // ! Debugger template manager of the database.
  /**
   * Returns the debugger template manager of this database. This is the manager that keeps track of
   * all pre-defined debugger templates.
   * 
   * @return The debugger template manager of this database.
   */
  public DebuggerTemplateManager getDebuggerTemplateManager() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: Database must be loaded first");
    }

    return m_debuggerTemplateManager;
  }

  // ! Description of the database.
  /**
   * Returns the description of the database. The description of the database is the text string
   * that is shown in the database node in the project tree.
   * 
   * @return The description of the database.
   */
  public String getDescription() {
    return m_database.getConfiguration().getDescription();
  }

  // ! Driver used to connect to the database.
  /**
   * Returns the name of the driver that is used to connect to this database.
   * 
   * @return The driver of the database.
   */
  public String getDriver() {
    return m_database.getConfiguration().getDriver();
  }

  // ! Host of the database.
  /**
   * Returns the host of the database.
   * 
   * @return The host of the database.
   */
  public String getHost() {
    return m_database.getConfiguration().getHost();
  }

  // ! Modules in the database.
  /**
   * Returns a list of modules that can be found in the database.
   * 
   * @return A list of modules.
   * @throws IllegalStateException Thrown if the database was not loaded before.
   */
  public List<Module> getModules() {
    if (!isLoaded()) {
      try {
        load();
      } catch (CouldntLoadDataException | InvalidDatabaseVersionException e) {
        return new ArrayList<Module>();
      } 
    }

    return new ArrayList<Module>(m_modules);
  }

  // ! Name of the database.
  /**
   * Returns the name of the database as it is known to the database server.
   * 
   * @return The name of the database.
   */
  public String getName() {
    return m_database.getConfiguration().getName();
  }

  // ! Node tag manager of the database.
  /**
   * Returns the node tag manager that manages all tags contained in the database.
   * 
   * @return The node tag manager of the database.
   */
  public TagManager getNodeTagManager() {
    if (!isLoaded()) {
      throw new IllegalArgumentException("Error: The database is not loaded yet");
    }

    return m_nodeTagManager;
  }

  // ! Password used to connect to the database.
  /**
   * Returns the password string that is used to connect to the database.
   * 
   * @return The password string.
   */
  public String getPassword() {
    return m_database.getConfiguration().getPassword();
  }

  // ! Projects in the database.
  /**
   * Returns a list of projects that can be found in the database.
   * 
   * @return A list of projects.
   * 
   * @throws IllegalStateException Thrown if the database was not loaded before.
   */
  public List<Project> getProjects() {
    if (!isLoaded()) {
      try {
        load();
      } catch (CouldntLoadDataException | InvalidDatabaseVersionException e) {
        return new ArrayList<Project>();
      } 
    }

    return new ArrayList<Project>(m_projects);
  }

  // ! URL of the database.
  /**
   * Returns the URL that describes the location of the database.
   * 
   * @return The URL of the database.
   */
  public String getUrl() {
    return m_database.getConfiguration().getUrl();
  }

  // ! User used to connect to the database.
  /**
   * Returns the user that is used to connect to the database.
   * 
   * @return The user string.
   */
  public String getUser() {
    return m_database.getConfiguration().getUser();
  }

  // ! View tag manager of the database.
  /**
   * Returns the view tag manager that manages all tags contained in the database.
   * 
   * @return The view tag manager of the database.
   */
  public TagManager getViewTagManager() {
    if (!isLoaded()) {
      throw new IllegalArgumentException("Error: The database is not loaded yet");
    }

    return m_viewTagManager;
  }

  // ! Imports an IDB file to the database.
  /**
   * Imports an IDB file.
   * 
   * @param idbfile The location of the IDB file to import.
   * 
   * @throws ImportFailedException Thrown if the IDB file could not be imported.
   */
  public void importIDB(final String idbfile) throws ImportFailedException {
    try {
      new CBinExportImporter().importIdbFile(
          ConfigManager.instance().getGeneralSettings().getIdaDirectory(), idbfile, m_database);
    } catch (final com.google.security.zynamics.binnavi.Importers.ImportFailedException e) {
      throw new ImportFailedException(e);
    }
  }

  // ! Checks whether connections to this database are established automatically.
  /**
   * Returns a flag that indicates whether a connection to the database is established automatically
   * when BinNavi is started.
   * 
   * @return True, if a connection to the database is established automatically. False, otherwise.
   */
  public boolean isAutoConnect() {
    return m_database.getConfiguration().isAutoConnect();
  }

  // ! Checks the connection state of the database.
  /**
   * Returns a flag that indicates whether a connection to the database is currently open.
   * 
   * @return True, if a connection to the database is open. False, otherwise.
   */
  public boolean isConnected() {
    return m_database.isConnected();
  }

  // ! Checks whether data from this database was already loaded.
  /**
   * Returns a flag that indicates whether the data has been loaded from the database.
   * 
   * @return True, if the database has been loaded. False, otherwise.
   */
  public boolean isLoaded() {
    return m_database.isLoaded();
  }

  // ! Checks whether the password to this database is stored in the config file.
  /**
   * Returns a flag that indicates whether the password of this database configuration is stored in
   * the configuration file when BinNavi exits.
   * 
   * @return True, if the password is saved to configuration file. False, otherwise.
   */
  public boolean isSavePassword() {
    return m_database.getConfiguration().isSavePassword();
  }

  // ! Loads data from the database.
  /**
   * Loads the data from the database.
   * 
   * Note that a connection to the database must be open before this function can succeed.
   * 
   * @throws IllegalStateException Thrown if no connection to the database is open.
   * @throws CouldntLoadDataException Thrown if the data could not be loaded from the database.
   * @throws InvalidDatabaseVersionException Thrown if the BinNavi tables in the database are not
   *         compatible with the used version of com.google.security.zynamics.binnavi.
   */
  public void load() throws CouldntLoadDataException, InvalidDatabaseVersionException {
    try {
      m_database.load();
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException | LoadCancelledException e) {
      throw new CouldntLoadDataException(e);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException e) {
      throw new InvalidDatabaseVersionException(e);
    } 
  }

  // ! Refreshes the modules list.
  /**
   * Refreshes the list of modules from the database. This is useful to call if you suspect that the
   * modules stored in the database changed.
   * 
   * @throws CouldntLoadDataException Thrown if the modules could not reloaded.
   */
  public void refresh() throws CouldntLoadDataException {
    try {
      m_database.getContent().refreshRawModules();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  // ! Removes a database listener.
  /**
   * Removes a listener object from the database.
   * 
   * @param listener The listener object to remove from the database.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the database.
   */
  public void removeListener(final IDatabaseListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the auto-connect flag of the database.
  /**
   * Changes the auto-connect flag of the database that is responsible for loading the content of
   * databases automatically when BinNavi is started.
   * 
   * @param autoConnect True, to automatically load this database when BinNavi is started. False,
   *        otherwise.
   */
  public void setAutoConnect(final boolean autoConnect) {
    m_database.getConfiguration().setAutoConnect(autoConnect);
  }

  // ! Changes the description of the database.
  /**
   * Changes the description of the database. The description string of a database is the string
   * that is displayed in the database node of the project tree.
   * 
   * @param description The new description of the database.
   * 
   * @throws IllegalArgumentException Thrown if the description argument is null.
   */
  public void setDescription(final String description) {
    m_database.getConfiguration().setDescription(description);
  }

  // ! Changes the driver string of the database.
  /**
   * Changes the driver string of the database.
   * 
   * @param driver The new driver string of the database.
   * 
   * @throws IllegalArgumentException Thrown if the driver argument is null.
   */
  public void setDriver(final String driver) {
    m_database.getConfiguration().setDriver(driver);
  }

  // ! Changes the host of the database.
  /**
   * Changes the host string of the database.
   * 
   * @param host The new host string of the database.
   * 
   * @throws IllegalArgumentException Thrown if the host argument is null.
   */
  public void setHost(final String host) {
    m_database.getConfiguration().setHost(host);
  }

  // ! Changes the name string of the database.
  /**
   * Changes the name string of the database.
   * 
   * @param name The new name string of the database.
   * 
   * @throws IllegalArgumentException Thrown if the name argument is null.
   */
  public void setName(final String name) {
    m_database.getConfiguration().setName(name);
  }

  // ! Changes password used to connect to the database.
  /**
   * Changes the password that is used to connect to the database.
   * 
   * @param password The new password.
   * 
   * @throws IllegalArgumentException Thrown if the password argument is null.
   */
  public void setPassword(final String password) {
    m_database.getConfiguration().setPassword(password);
  }

  // ! Changes the save-password flag of the database.
  /**
   * Changes the save-password flag of the database that is responsible for storing the password to
   * this database in the BinNavi configuration file.
   * 
   * @param savePassword True, to store the password. False, otherwise.
   */
  public void setSavePassword(final boolean savePassword) {
    m_database.getConfiguration().setSavePassword(savePassword);
  }

  // ! Changes the database user.
  /**
   * Changes the user that is used to connect to the database.
   * 
   * @param user The new user string.
   * 
   * @throws IllegalArgumentException Thrown if the user argument is null.
   */
  public void setUser(final String user) {
    m_database.getConfiguration().setUser(user);
  }

  // ! Printable representation of the database.
  /**
   * Returns the string representation of the database.
   * 
   * @return The string representation of the database.
   */
  @Override
  public String toString() {
    if (isLoaded()) {
      return String.format("Database '%s' [%d projects, %d modules]", getDescription(),
          getProjects().size(), getModules().size());
    } else {
      return String.format("Database '%s' [Unloaded]", getDescription());
    }
  }

  /**
   * Keeps the API database object synchronized with the internal database object.
   */
  private class InternalDatabaseListener implements
      com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener {

    @Override
    public void addedModule(final IDatabase database, final INaviModule module) {
      final Module newModule =
          new Module(Database.this, module, m_nodeTagManager, m_viewTagManager);

      m_modules.add(newModule);

      for (final IDatabaseListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedModule(Database.this, newModule);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedProject(final IDatabase connection, final INaviProject project) {
      final Project newProject =
          new Project(Database.this, project, m_nodeTagManager, m_viewTagManager);

      m_projects.add(newProject);

      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.addedProject(Database.this, newProject);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedAutoConnect(final IDatabase database, final boolean autoConnect) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedAutoConnect(Database.this, autoConnect);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDescription(final IDatabase database, final String description) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedDescription(Database.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDriver(final IDatabase database, final String driver) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedDriver(Database.this, driver);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedHost(final IDatabase database, final String host) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedHost(Database.this, host);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedIdentity(final IDatabase database, final String identity) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedIdentity(Database.this, identity);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final IDatabase database, final String name) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedName(Database.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedPassword(final IDatabase database, final String password) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedPassword(Database.this, password);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedRawModules(final IDatabase database, final List<INaviRawModule> oldModules,
        final List<INaviRawModule> newModules) {
      // Don't pass this to the API
    }

    @Override
    public void changedSavePassword(final IDatabase database, final boolean savePassword) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedSavePassword(Database.this, savePassword);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedUser(final IDatabase database, final String user) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.changedUser(Database.this, user);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void closedDatabase(final IDatabase connection) {
      disposedLoadedObjects();

      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.closedDatabase(Database.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean closingDatabase(final IDatabase database) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          if (!listener.closingDatabase(Database.this)) {
            return false;
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      return true;
    }

    @Override
    public void deletedModule(final IDatabase database, final INaviModule module) {
      final Module deletedModule = ObjectFinders.getObject(module, m_modules);

      deletedModule.dispose();

      m_modules.remove(deletedModule);

      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.deletedModule(Database.this, deletedModule);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedProject(final IDatabase database, final INaviProject project) {
      final Project deletedProject = ObjectFinders.getObject(project, m_projects);

      deletedProject.dispose();

      m_projects.remove(deletedProject);

      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.deletedProject(Database.this, deletedProject);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedRawModule(final IDatabase database, final INaviRawModule module) {
      // Don't pass this to the API
    }

    @Override
    public void loadedDatabase(final IDatabase database) {
      convertData();

      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.loadedDatabase(Database.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean loading(final LoadEvents event, final int counter) {
      return true;
    }

    @Override
    public void openedDatabase(final IDatabase connection) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.openedDatabase(Database.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
