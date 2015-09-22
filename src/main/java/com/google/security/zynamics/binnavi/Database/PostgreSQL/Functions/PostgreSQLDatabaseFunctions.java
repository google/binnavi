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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.DatabaseVersion;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntUpdateDatabaseException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

public final class PostgreSQLDatabaseFunctions {
  /**
   * Local cache for all tables currently in the database This is used to avoid multiple queries to
   * the database if there are > 1 modules in the database.
   */
  protected static Multimap<Pair<CConnection, String>, String> m_cache = HashMultimap.create();

  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLDatabaseFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Check arguments function to wrap a set of preconditions.
   * 
   * @param provider The provider argument to check.
   * @param debuggerManager The debugger manager argument to check.
   */
  protected static void checkArguments(final AbstractSQLProvider provider,
      final DebuggerTemplateManager debuggerManager) {
    Preconditions.checkNotNull(provider, "IE00413: Provider argument can not be null");
    Preconditions.checkNotNull(debuggerManager,
        "IE01227: Debugger final manager argument can final not be null");
    Preconditions.checkArgument(debuggerManager.inSameDatabase(provider),
        "IE00415: Debugger manager is not part of the given database");
  }

  /**
   * Checks the existence of all necessary raw module tables.
   * 
   * @param connection The connection to the database.
   * @param databaseName The name of the database.
   * @param rawModuleId The ID of the raw module whose tables are checked.
   * @return True, if all necessary tables exist.
   */
  protected static boolean checkRawModulesTables(final CConnection connection,
      final String databaseName, final int rawModuleId) {

    Preconditions.checkNotNull(connection, "IE02261: Connection argument can not be null");
    Preconditions.checkNotNull(databaseName, "IE02262: Database name argument can not be null");
    Preconditions.checkArgument(rawModuleId > 0,
        "Raw module id %s must be a positive integer", rawModuleId);

    final ImmutableSet<String> rawTableNames =
        ImmutableSet.of(String.format(CTableNames.RAW_ADDRESS_COMMENTS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_ADDRESS_REFERENCES_TABLE, rawModuleId),
            String.format(CTableNames.RAW_BASE_TYPES, rawModuleId),
            String.format(CTableNames.RAW_BASIC_BLOCK_INSTRUCTIONS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_BASIC_BLOCKS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_CALLGRAPH_TABLE, rawModuleId),
            String.format(CTableNames.RAW_CONTROL_FLOW_GRAPHS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_EXPRESSION_NODES_TABLE, rawModuleId),
            String.format(CTableNames.RAW_EXPRESSION_SUBSTITUTIONS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_EXPRESSION_TREE_NODES_TABLE, rawModuleId),
            String.format(CTableNames.RAW_EXPRESSION_TREES_TABLE, rawModuleId),
            String.format(CTableNames.RAW_EXPRESSION_TYPES_TABLE, rawModuleId),
            String.format(CTableNames.RAW_FUNCTIONS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_INSTRUCTIONS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_OPERANDS_TABLE, rawModuleId),
            String.format(CTableNames.RAW_SECTIONS, rawModuleId),
            String.format(CTableNames.RAW_EXPRESSION_TYPE_INSTANCES, rawModuleId),
            String.format(CTableNames.RAW_TYPE_INSTACES, rawModuleId),
            String.format(CTableNames.RAW_TYPES, rawModuleId));

    final Pair<CConnection, String> cacheKey = new Pair<>(connection, databaseName);

    if (PostgreSQLDatabaseFunctions.queryCache(cacheKey, rawTableNames)) {
      return true;
    } else {
      if (!PostgreSQLDatabaseFunctions.fillCache(cacheKey)) {
        return false;
      }

      return PostgreSQLDatabaseFunctions.queryCache(cacheKey, rawTableNames);
    }
  }

  /**
   * Fill the local cache of all tables which are in the database.
   * 
   * @return true if the cache could be filled.
   */
  protected static boolean fillCache(final Pair<CConnection, String> cacheKey) {
    PostgreSQLDatabaseFunctions.m_cache.clear();
    
    final String query =
      "SELECT table_name FROM information_schema.tables  WHERE table_catalog = '"
        + cacheKey.second() + "' ";
    try (ResultSet result = cacheKey.first().executeQuery(query, true)) {
      while (result.next()) {
        PostgreSQLDatabaseFunctions.m_cache.put(cacheKey, result.getString(1));
      }
    } catch (final SQLException exception) {
      return false;
    }

    return true;
  }

  /**
   * Returns the raw module with the given ID.
   * 
   * @param rawModuleId The ID to search for.
   * @param rawModules The raw modules to search through.
   * @return The raw module with the given ID.
   */
  protected static INaviRawModule findRawModule(final int rawModuleId,
      final List<INaviRawModule> rawModules) {
    Preconditions.checkArgument(rawModuleId > 0,
        "Raw module id %s must be positive integer", rawModuleId);
    Preconditions.checkNotNull(rawModules, "IE02263: raw modules argument can not be null");

    for (final INaviRawModule rawModule : rawModules) {
      if (rawModule.getId() == rawModuleId) {
        return rawModule;
      }
    }

    throw new IllegalStateException("IE00160: Could not find raw module");
  }

  /**
   * Determines the debuggers that are assigned to a project.
   * 
   * @param connection Connection to the SQL database where the information is stored.
   * @param projectId ID of the project in question.
   * @param debuggerManager Debugger manager object that belongs to the given database.
   * @return A list that contains the debugger templates assigned to the given project.
   * @throws CouldntLoadDataException Thrown if the debugger templates could not be loaded.
   */
  protected static List<DebuggerTemplate> getAssignedDebuggers(final CConnection connection,
      final int projectId, final DebuggerTemplateManager debuggerManager)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(connection, "IE02264: Connection argument can not be null");
    Preconditions.checkArgument(projectId > 0, "Project id %s must be a positive integer.",
        projectId);
    Preconditions.checkNotNull(debuggerManager,
        "IE02265: debugger manager argument can not be null");

    final List<DebuggerTemplate> debuggerIds = new ArrayList<>();

    final String query =
        String.format("SELECT debugger_id FROM %s WHERE project_id = %d",
            CTableNames.PROJECT_DEBUGGERS_TABLE, projectId);

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        debuggerIds.add(debuggerManager.findDebugger(resultSet.getInt("debugger_id")));
      }
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }

    return debuggerIds;
  }

  /**
   * Returns the number of functions in a raw module.
   * 
   * @param connection Connection to the database.
   * @param rawModuleId ID of the raw module.
   * @return The number of functions in the raw module with the given ID.
   * @throws SQLException Thrown if the number of functions could not be determined.
   */
  protected static int getRawModuleFunctionCount(final CConnection connection, final int rawModuleId)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE02266: Connection argument can not be null");
    Preconditions.checkArgument(rawModuleId > 0,
        "Raw module id %s must be a positive integer", rawModuleId);
    
    final String query = 
        "SELECT count(*) AS fcount " + " FROM ex_" + rawModuleId
            + "_functions " + " WHERE address <> 0 " + " OR type <> 3";
            
    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        return resultSet.getInt("fcount");
      }
    return 0;
    } catch (final SQLException exception) {
      throw new SQLException("Could not load function count");
    }
  }

  protected static ArrayList<Integer> getRawModuleIDs(final CConnection connection)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE02267: Connection argument can not be null");

    final ArrayList<Integer> rawModuleIDs = new ArrayList<>();
    
    try (ResultSet resultSet = connection.executeQuery("SELECT id FROM modules", true)) {
      while (resultSet.next()) {
        rawModuleIDs.add(resultSet.getInt("id"));
      }
    } catch (final Exception exception) {
      return null;
    }
    return rawModuleIDs;
  }

  /**
   * Queries the local cache of table names.
   * 
   * @param cacheKey the local cache of table names.
   * @param rawTableNames the hash set of tables names for a given raw module.
   * @return true if the cache contains all of the elements in the table names hash set.
   */
  protected static boolean queryCache(final Pair<CConnection, String> cacheKey,
      final ImmutableSet<String> rawTableNames) {
    return PostgreSQLDatabaseFunctions.m_cache.get(cacheKey).containsAll(rawTableNames);
  }

  /**
   * Determines which version the current database has. Each of the checks tries to locate the one
   * database version specific thing that was changed in a version upgrade.
   *
   * @param m_connection The connection to the database.
   * @return A {@link DatabaseVersion} for further processing.
   * @throws CouldntLoadDataException if the version could not be determined.
   * @throws SQLException if the version could not be determined.
   */
  public static DatabaseVersion getDatabaseVersion(final CConnection m_connection)
      throws CouldntLoadDataException, SQLException {

    if (PostgreSQLHelpers.hasTable(m_connection, CTableNames.SECTIONS_TABLE)) {
      return new DatabaseVersion("6.0.0");
    }
    if (PostgreSQLHelpers.hasTable(m_connection, CTableNames.USER_TABLE)) {
      return new DatabaseVersion("5.0.0");
    }
    if (PostgreSQLHelpers.hasTable(m_connection, CTableNames.RAW_MODULES_TABLE)
        && PostgreSQLHelpers.hasTable(m_connection, CTableNames.MODULES_TABLE)) {
      final ArrayList<Integer> rawModuleIds = getRawModuleIDs(m_connection);
      if (rawModuleIds.isEmpty()) {
        return new DatabaseVersion("3.0.0");
      }
      if (PostgreSQLHelpers.hasTable(m_connection, "ex_" + rawModuleIds.get(0) + "_type_structs")) {
        return new DatabaseVersion("4.0.0");
      }
      return new DatabaseVersion("3.0.0");
    }
    return new DatabaseVersion(Constants.PROJECT_VERSION);
  }

  /**
   * Loads the modules of a database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param rawModules Previously loaded raw module objects.
   * @param debuggerManager Debugger manager object that belongs to the given database.
   * 
   * @return A list of modules that contains the modules stored in the database.
   * 
   * @throws CouldntLoadDataException Thrown if the modules could not be loaded from the database.
   */
  public static List<INaviModule> loadModules(final AbstractSQLProvider provider,
      final List<INaviRawModule> rawModules, final DebuggerTemplateManager debuggerManager)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(rawModules, "IE02043: rawModules argument can not be null");
    PostgreSQLDatabaseFunctions.checkArguments(provider, debuggerManager);

    final List<CModule> modules = new ArrayList<>();
    final CConnection connection = provider.getConnection();

    if (!PostgreSQLHelpers.hasTable(connection, CTableNames.MODULES_TABLE)) {
      return new ArrayList<INaviModule>(modules);
    }

    final String query =
        "SELECT id, raw_module_id, "
            + CTableNames.MODULES_TABLE
            + ".name, "
            + " md5, sha1, description, import_time, modification_date, file_base, image_base, stared, "
            + " initialization_state, debugger_id, " + " (SELECT count(*) FROM "
            + CTableNames.FUNCTIONS_TABLE + " " + " WHERE id = " + CTableNames.FUNCTIONS_TABLE
            + ".module_id) " + " AS function_count, " + " (SELECT count(*) FROM "
            + CTableNames.MODULE_VIEWS_TABLE + " JOIN " + CTableNames.VIEWS_TABLE
            + " ON view_id = id " + " WHERE type = 'non-native' and module_id = "
            + CTableNames.MODULES_TABLE + ".id) " + " AS view_count FROM "
            + CTableNames.MODULES_TABLE + " " + " WHERE raw_module_id IS NOT NULL ORDER BY id";

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        final int moduleId = resultSet.getInt("id");
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String md5 = PostgreSQLHelpers.readString(resultSet, "md5");
        final String sha1 = PostgreSQLHelpers.readString(resultSet, "sha1");
        final String comment = PostgreSQLHelpers.readString(resultSet, "description");
        final Timestamp timestamp = resultSet.getTimestamp("import_time");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");
        int functionCount = resultSet.getInt("function_count");
        final int viewCount = resultSet.getInt("view_count");
        final IAddress imageBase = PostgreSQLHelpers.loadAddress(resultSet, "image_base");
        final IAddress fileBase = PostgreSQLHelpers.loadAddress(resultSet, "file_base");
        final int debuggerId = resultSet.getInt("debugger_id");
        final boolean isStared = resultSet.getBoolean("stared");
        final int initializationState = resultSet.getInt("initialization_state");

        final DebuggerTemplate description = debuggerManager.findDebugger(debuggerId);

        final int rawModuleId = resultSet.getInt("raw_module_id");
        final INaviRawModule rawModule =
          PostgreSQLDatabaseFunctions.findRawModule(rawModuleId, rawModules);

        if ((functionCount == 0) && (rawModule != null)) {
          functionCount = rawModule.getFunctionCount();
        }

        modules.add(new CModule(moduleId, name, comment, timestamp, modificationDate, md5, sha1,
          functionCount, viewCount, fileBase, imageBase, description, rawModule,
          initializationState, isStared, provider));
      }
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }

    return new ArrayList<INaviModule>(modules);
  }

  /**
   * Loads the projects of a database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param debuggerManager Debugger manager object that belongs to the given database.
   * 
   * @return A list of projects that contains the projects stored in the database.
   * 
   * @throws CouldntLoadDataException Thrown if the projects could not be loaded from the database.
   */
  public static List<INaviProject> loadProjects(final AbstractSQLProvider provider,
      final DebuggerTemplateManager debuggerManager) throws CouldntLoadDataException {
    PostgreSQLDatabaseFunctions.checkArguments(provider, debuggerManager);

    final CConnection connection = provider.getConnection();

    final List<INaviProject> projects = new ArrayList<>();

    if (!PostgreSQLHelpers.hasTable(connection, CTableNames.PROJECTS_TABLE)) {
      return projects;
    }
    
    String query =
          "SELECT id, name, description, creation_date, modification_date, "
              + " (SELECT count(*) FROM " + CTableNames.ADDRESS_SPACES_TABLE
              + " WHERE project_id = " + CTableNames.PROJECTS_TABLE + ".id) "
              + " AS addressspace_count FROM " + CTableNames.PROJECTS_TABLE;
              
    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        final int projectId = resultSet.getInt("id");
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String description = PostgreSQLHelpers.readString(resultSet, "description");
        final int addressSpaceCount = resultSet.getInt("addressspace_count");

        final Timestamp creationDate = resultSet.getTimestamp("creation_date");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");

        final List<DebuggerTemplate> debuggers =
            PostgreSQLDatabaseFunctions.getAssignedDebuggers(connection, projectId,
                debuggerManager);

        projects.add(new CProject(projectId, name, description == null ? "" : description,
            creationDate, modificationDate, addressSpaceCount, debuggers, provider));
      }
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }

    return new ArrayList<INaviProject>(projects);
  }

  /**
   * Loads the raw modules of a database.
   * 
   * @param provider The SQL provider that provides the connection.
   * 
   * @return A list of raw modules that contains the raw modules stored in the database.
   * 
   * @throws CouldntLoadDataException Thrown if the raw modules could not be loaded from the
   *         database.
   */
  public static final List<INaviRawModule> loadRawModules(final AbstractSQLProvider provider)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(provider, "IE00416: Provider argument can not be null");

    final CConnection connection = provider.getConnection();

    final List<INaviRawModule> modules = new ArrayList<INaviRawModule>();

    if (!PostgreSQLHelpers.hasTable(connection, CTableNames.RAW_MODULES_TABLE)) {
      return modules;
    }

    final String query = "SELECT id, name FROM " + CTableNames.RAW_MODULES_TABLE + " ORDER BY id";

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        final int rawModuleId = resultSet.getInt("id");
        final String name = PostgreSQLHelpers.readString(resultSet, "name");

        final boolean isComplete =
            PostgreSQLDatabaseFunctions.checkRawModulesTables(provider.getConnection(),
                PostgreSQLHelpers.getDatabaseName(provider.getConnection()), rawModuleId);

        final int functionCount =
            isComplete ? PostgreSQLDatabaseFunctions.getRawModuleFunctionCount(connection,
                rawModuleId) : 0;

        final CRawModule module =
            new CRawModule(rawModuleId, name, functionCount, isComplete, provider);

        modules.add(module);
      }
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }

    return modules;
  }


  /**
   * Performs an update of a postgresql database with the resource passed as argument.
   * 
   * @param connection The connection to the database.
   * @param resource The resource SQL file to perform the update with.
   * @throws CouldntUpdateDatabaseException if the update fails.
   */
  private static void databaseUpdater(final CConnection connection, final String resource)
      throws CouldntUpdateDatabaseException {

    final BufferedReader input =
        new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(resource)));

    final StringBuffer contents = new StringBuffer();
    String line = null;

    try {
      while ((line = input.readLine()) != null) {
        if (line.length() > 0 && line.charAt(0) == '#') {
          continue;
        }

        contents.append(line);
        contents.append('\n');
      }
      input.close();
    } catch (final IOException exception) {
      throw new CouldntUpdateDatabaseException(exception.toString(), 40);
    }

    try (PreparedStatement statement =
          connection.getConnection().prepareStatement(contents.toString())) {
      statement.execute();
    } catch (final SQLException exception) {
      throw new CouldntUpdateDatabaseException(exception.toString(), 41);
    }
  }


  /**
   * Updates a database to the newest version of the database. It determines the current version and
   * then updates accordingly. Supported are all versions since the postgresql move.
   * 
   * @throws CouldntUpdateDatabaseException
   */
  public static void updateDatabase(final SQLProvider provider)
      throws CouldntUpdateDatabaseException {

    try {
      if (provider.getDatabaseVersion().compareTo(new DatabaseVersion("4.0.0")) == 0) {
        databaseUpdater(provider.getConnection(), "com/google/security/zynamics/binnavi/data/"
            + "postgresql_convert_4_0_7_to_5_0_0.sql");
        databaseUpdater(provider.getConnection(), "com/google/security/zynamics/binnavi/data/"
            + "postgresql_convert_5_0_0_to_5_0_1.sql");
      } else if (provider.getDatabaseVersion().compareTo(new DatabaseVersion("5.0.0")) == 0) {
        databaseUpdater(provider.getConnection(), "com/google/security/zynamics/binnavi/data/"
            + "postgresql_convert_5_0_0_to_5_0_1.sql");
      }
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
    }
  }
}
