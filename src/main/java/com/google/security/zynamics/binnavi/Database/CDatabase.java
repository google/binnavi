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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntUpdateDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseContent;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseLoadProgressReporter;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.NotificationChannel;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.PostgreSQLNotificationProvider;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The database class is used to store all information about one database that can be used in
 * com.google.security.zynamics.binnavi.
 */
public final class CDatabase implements IDatabase, IDatabaseObject {
  /**
   * Contains the database connection data.
   */
  private final CDatabaseConfiguration description;

  /**
   * Contains the content of the database. This value is null if the database is not loaded.
   */
  private CDatabaseContent content = null;

  /**
   * The list of listeners that are notified about changes in the database.
   */
  private final ListenerProvider<IDatabaseListener> listeners =
      new ListenerProvider<IDatabaseListener>();

  /**
   * The SQL provider that is used to communicate with the database. If the database is not
   * connected, this object is null.
   */
  private SQLProvider provider;

  /**
   * Synchronizes the debugger template manager with other components of the database.
   */
  private final InternalDebuggerTemplateListener internalDebuggerTemplateListener =
      new InternalDebuggerTemplateListener();

  /**
   * Reports database loading events to listeners.
   */
  private final IDatabaseLoadProgressReporter<LoadEvents> loadReporter =
      new DefaultDatabaseLoadProgressReporter<LoadEvents>() {
        @Override
        protected boolean report(final LoadEvents event, final int counter) {
          boolean cont = true;

          for (final IDatabaseListener listener : listeners) {
            cont &= listener.loading(event, counter);
          }

          return cont;
        }
      };

  /**
   * Flag that indicates whether the database is in the process of connecting.
   */
  private boolean isConnecting = false;

  /**
   * Flag that indicates whether the database is in the process of loading.
   */
  private boolean isLoading = false;

  /**
   * Creates a new database object. The database is not connected automatically. To connect to this
   * database please use the method connect. Even after connecting, the content of the database is
   * not loaded automatically. Please use the method load to load the content of the database.
   *
   * @param description The description of the database.
   * @param driver The driver that is used to connect to the database.
   * @param host Host location of the database.
   * @param name Name of the database.
   * @param user The user name that is used to connect to the database.
   * @param password The password that is used to connect to the database.
   * @param identity The identity under which the current user interacts with the database.
   * @param savePassword True, if the password of this database should be stored in the
   *        configuration file.
   * @param autoConnect True, if BinNavi should connect to this database automatically.
   * @throws IllegalArgumentException Thrown if any of the arguments are null or invalid.
   */
  public CDatabase(final String description,
      final String driver,
      final String host,
      final String name,
      final String user,
      final String password,
      final String identity,
      final boolean savePassword,
      final boolean autoConnect) {
    Preconditions.checkNotNull(description, "IE00656: Database description can not be null");
    Preconditions.checkNotNull(driver, "IE00657: Database driver can not be null");
    Preconditions.checkNotNull(host, "IE01148: Database host can not be null");
    Preconditions.checkNotNull(name, "IE00658: Database name can not be null");
    Preconditions.checkNotNull(user, "IE00659: Database user can not be null");
    Preconditions.checkNotNull(password, "IE00660: Database password can not be null");
    Preconditions.checkNotNull(identity, "IE00064: identity argument can not be null");

    this.description = new CDatabaseConfiguration(this,
        listeners,
        description,
        driver,
        host,
        name,
        user,
        password,
        identity,
        autoConnect,
        savePassword);
  }

  /**
   * Loads the tag manager responsible for node tagging.
   *
   * @return The loaded tag manager.
   */
  private CTagManager loadNodeTagManager() {
    try {
      return provider.loadTagManager(TagType.NODE_TAG);
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);

      final CTag root = new CTag(0, "Root Node Tag", "", TagType.NODE_TAG, provider);
      final Tree<CTag> tree = new Tree<CTag>(new TreeNode<CTag>(root));

      return new CTagManager(tree, TagType.NODE_TAG, provider);
    }
  }

  /**
   * Loads the user manager and initializes it.
   *
   * @return The loaded user manager.
   *
   * @throws CouldntLoadDataException if the manager could not be initialized.
   */
  private CUserManager loadUserManager() throws CouldntLoadDataException {
    final CUserManager userManager = CUserManager.get(provider);
    final String userName = getConfiguration().getIdentity();
    if (userManager.containsUserName(userName)) {
      userManager.setCurrentActiveUser(userManager.getUserByUserName(userName));
    } else {
      try {
        userManager.setCurrentActiveUser(userManager.addUser(userName));
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);
        throw new CouldntLoadDataException(e);
      }
    }

    return userManager;
  }

  /**
   * Loads the tag manager responsible for view tagging.
   *
   * @return The loaded view tag manager.
   */
  private CTagManager loadViewTagManager() {
    try {
      return provider.loadTagManager(TagType.VIEW_TAG);
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);

      final CTag root = new CTag(0, "Root View Tag", "", TagType.VIEW_TAG, provider);
      final Tree<CTag> tree = new Tree<CTag>(new TreeNode<CTag>(root));

      return new CTagManager(tree, TagType.VIEW_TAG, provider);
    }
  }

  @Override
  public void addListener(final IDatabaseListener listener) {
    listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    Preconditions.checkArgument(
        isConnected(), "IE00664: Can not disconnect from the database because it is not connected");

    if (PostgreSQLNotificationProvider.contains(provider)) {
      PostgreSQLNotificationProvider.get(provider).unInitialize();
    }

    for (final IDatabaseListener listener : listeners) {
      try {
        if (!listener.closingDatabase(this)) {
          return false;
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    if (isLoaded()) {
      for (final INaviProject project : content.getProjects()) {
        if (project.isLoaded() && !project.close()) {
          return false;
        }
      }
    }

    provider.close();
    provider = null;

    for (final IDatabaseListener listener : listeners) {
      try {
        listener.closedDatabase(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    content = null;
    return true;
  }

  @Override
  public void connect()
      throws CouldntLoadDriverException,
      CouldntConnectException,
      InvalidDatabaseException,
      CouldntInitializeDatabaseException,
      InvalidExporterDatabaseFormatException,
      LoadCancelledException {

    loadReporter.start();
    isConnecting = true;

    try {
      final Pair<CConnection, SQLProvider> connectionData =
          CDatabaseConnection.connect(description, loadReporter);

      provider = connectionData.second();
    } catch (final CouldntLoadDriverException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } catch (final CouldntConnectException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } catch (final CouldntInitializeDatabaseException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } catch (final InvalidDatabaseException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } catch (final InvalidExporterDatabaseFormatException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } catch (final LoadCancelledException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } finally {
      isConnecting = false;
    }

    for (final IDatabaseListener listener : listeners) {
      try {
        listener.openedDatabase(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    try {
      final PostgreSQLNotificationProvider notificationProvider =
          PostgreSQLNotificationProvider.initialize(provider, description);
      notificationProvider.listen(NotificationChannel.all());
      notificationProvider.startPolling();
    } catch (final SQLException exception) {
      NaviLogger.severe(
          "Error: Could not establish a channel for receiving notifications from the database %s",
          exception);
    }
  }

  @Override
  public ResultSet executeQuery(final String query) throws SQLException {
    Preconditions.checkNotNull(query, "IE00729: Query argument can not be null");

    return provider.executeQuery(query);
  }

  @Override
  public CDatabaseConfiguration getConfiguration() {
    return description;
  }

  @Override
  public IDatabaseContent getContent() {
    return content;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return this.provider == provider;
  }

  @Override
  public boolean isConnected() {
    return provider != null;
  }

  @Override
  public boolean isConnecting() {
    return isConnecting;
  }

  @Override
  public boolean isLoaded() {
    return content != null;
  }

  @Override
  public boolean isLoading() {
    return isLoading;
  }

  @Override
  public void load()
      throws CouldntLoadDataException, InvalidDatabaseVersionException, LoadCancelledException {
    Preconditions.checkArgument(isConnected(), "IE00686: Not connected to the database");

    try {
      if (!loadReporter.report(LoadEvents.LOADING_DATABASE)) {
        throw new LoadCancelledException();
      }

      if (!loadReporter.report(LoadEvents.DETERMINING_DATABASE_VERSION)) {
        throw new LoadCancelledException();
      }

      final DatabaseVersion databaseVersion = provider.getDatabaseVersion();
      final DatabaseVersion currentVersion = new DatabaseVersion(Constants.PROJECT_VERSION);

      if ((databaseVersion.compareTo(currentVersion) != 0)
          && currentVersion.needsUpgrading(databaseVersion)) {
        throw new InvalidDatabaseVersionException(databaseVersion);
      }

      if (!loadReporter.report(LoadEvents.LOADING_USERS)) {
        throw new LoadCancelledException();
      }
      loadUserManager();

      if (!loadReporter.report(LoadEvents.LOADING_VIEW_TAGS)) {
        throw new LoadCancelledException();
      }
      final CTagManager viewTagManager = loadViewTagManager();

      if (!loadReporter.report(LoadEvents.LOADING_NODE_TAGS)) {
        throw new LoadCancelledException();
      }
      final CTagManager nodeTagManager = loadNodeTagManager();

      if (!loadReporter.report(LoadEvents.LOADING_DEBUGGERS)) {
        throw new LoadCancelledException();
      }
      final DebuggerTemplateManager debuggerDescriptionManager = provider.loadDebuggers();

      if (!loadReporter.report(LoadEvents.LOADING_PROJECTS)) {
        throw new LoadCancelledException();
      }
      final List<INaviProject> projects = provider.loadProjects();

      if (!loadReporter.report(LoadEvents.LOADING_RAW_MODULES)) {
        throw new LoadCancelledException();
      }
      final List<INaviRawModule> rawModules = provider.loadRawModules();

      if (!loadReporter.report(LoadEvents.LOADING_MODULES)) {
        throw new LoadCancelledException();
      }
      final List<INaviModule> modules = provider.loadModules();

      debuggerDescriptionManager.addListener(internalDebuggerTemplateListener);

      content = new CDatabaseContent(provider,
          this,
          listeners,
          projects,
          modules,
          rawModules,
          viewTagManager,
          nodeTagManager,
          debuggerDescriptionManager);
    } catch (final CouldntLoadDataException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      close();

      throw exception;
    } catch (final InvalidDatabaseVersionException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      throw exception;
    } catch (final LoadCancelledException exception) {
      loadReporter.report(LoadEvents.LOADING_FINISHED);
      close();
      throw exception;
    } finally {
      isLoading = false;
    }

    content.initializeRawModules(content.getModules(), content.getRawModules());

    for (final IDatabaseListener listener : listeners) {
      try {
        listener.loadedDatabase(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    loadReporter.report(LoadEvents.LOADING_FINISHED);
  }

  @Override
  public void removeListener(final IDatabaseListener listener) {
    listeners.removeListener(listener);
  }

  @Override
  public void update() throws CouldntUpdateDatabaseException {
    provider.updateDatabase();
  }

  /**
   * Synchronizes the debugger template manager with other components of the database.
   */
  private class InternalDebuggerTemplateListener implements IDebuggerTemplateManagerListener {
    @Override
    public void addedDebugger(
        final DebuggerTemplateManager manager, final DebuggerTemplate debugger) {}

    @Override
    public void removedDebugger(
        final DebuggerTemplateManager manager, final DebuggerTemplate debugger) {
      if (!isLoaded()) {
        return;
      }

      for (final INaviProject project : content.getProjects()) {
        try {
          if (project.getConfiguration().hasDebugger(debugger)) {
            project.getConfiguration().removeDebugger(debugger);
          }
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);
        }
      }

      for (final INaviModule module : content.getModules()) {
        try {
          if (module.getConfiguration().getDebuggerTemplate() == debugger) {
            module.getConfiguration().setDebuggerTemplate(null);
          }
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);
        }
      }
    }
  }
}
