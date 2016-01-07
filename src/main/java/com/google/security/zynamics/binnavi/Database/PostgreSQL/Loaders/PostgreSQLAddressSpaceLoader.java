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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Contains SQL queries to load address spaces.
 */
public final class PostgreSQLAddressSpaceLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLAddressSpaceLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks the validity of a given SQL provider and a given project. If there is a problem with the
   * arguments, an exception is thrown.
   * 
   * @param provider The SQL provider to check.
   * @param project The project to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final INaviProject project) {
    Preconditions.checkNotNull(provider, "IE00400: Provider argument can not be null");
    Preconditions.checkNotNull(project, "IE00401: Project argument can not be null");
    Preconditions.checkArgument(project.inSameDatabase(provider),
        "IE00402: Project is not part of this database");
  }

  /**
   * Searches for a module with the given id.
   * 
   * @param list Modules to search through.
   * @param moduleId Module ID to search for.
   * 
   * @return The module with the given ID.
   * 
   * @throws MaybeNullException Thrown if the module could not be found.
   */
  private static INaviModule findModule(final List<INaviModule> list, final int moduleId)
      throws MaybeNullException {
    for (final INaviModule module : list) {
      if (module.getConfiguration().getId() == moduleId) {
        return module;
      }
    }

    throw new MaybeNullException();
  }

  /**
   * Loads the image bases of the given modules within the given address space.
   * 
   * The address space ID and the modules in the module list must all reference items that are
   * stored in the database connected to by connection argument.
   * 
   * @param connection Connection to the database.
   * @param addressSpaceId ID of the address space.
   * @param list Modules whose image bases are loaded.
   * 
   * @return A mapping of modules -> image bases for all modules in the address space.
   * 
   * @throws CouldntLoadDataException Thrown if the image bases could not be loaded.
   */
  private static Map<INaviModule, IAddress> loadImageBases(final CConnection connection,
      final int addressSpaceId, final List<INaviModule> list) throws CouldntLoadDataException {
    final HashMap<INaviModule, IAddress> imageBases = new HashMap<INaviModule, IAddress>();

    final String query =
        "SELECT module_id, image_base FROM " + CTableNames.SPACE_MODULES_TABLE
            + " WHERE address_space_id = " + addressSpaceId;

    try {
      final ResultSet resultSet = connection.executeQuery(query, true);

      try {
        while (resultSet.next()) {
          try {
            final INaviModule module = findModule(list, resultSet.getInt("module_id"));

            imageBases.put(module, PostgreSQLHelpers.loadAddress(resultSet, "image_base"));
          } catch (final MaybeNullException exception) {
            // I can not think of a scenario where this can happen.
            CUtilityFunctions.logException(exception);
          }
        }
      } finally {
        resultSet.close();
      }

      return imageBases;
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Loads the address spaces of a project.
   * 
   * The project, the debugger manager, and all modules in the module list must be stored in the
   * database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param project The parent project of the address spaces to load.
   * @param debuggerManager Debugger manager of the database.
   * @param list A list of all modules that belong to the database.
   * 
   * @return A list that contains the address spaces of the project.
   * 
   * @throws CouldntLoadDataException Thrown if the address spaces could not be loaded.
   */
  public static List<CAddressSpace> loadAddressSpaces(final AbstractSQLProvider provider,
      final INaviProject project, final DebuggerTemplateManager debuggerManager,
      final List<INaviModule> list) throws CouldntLoadDataException {
    checkArguments(provider, project);

    Preconditions.checkNotNull(debuggerManager,
        "IE01543: Debugger provider argument can not be null");
    Preconditions.checkNotNull(list, "IE01545: Modules argument can not be null");
    NaviLogger.info("Loading address spaces of project %s", project.getConfiguration().getName());

    final CConnection connection = provider.getConnection();

    final List<CAddressSpace> addressSpaces = new ArrayList<CAddressSpace>();

    final String query =
        "SELECT id, name, description, creation_date, modification_date, debugger_id " + " FROM "
            + CTableNames.ADDRESS_SPACES_TABLE + " WHERE project_id = "
            + project.getConfiguration().getId();

    try {
      final ResultSet resultSet = connection.executeQuery(query, true);

      try {
        while (resultSet.next()) {
          final int addressSpaceId = resultSet.getInt("id");

          final Map<INaviModule, IAddress> imageBases =
              loadImageBases(connection, addressSpaceId, list);

          final String name = PostgreSQLHelpers.readString(resultSet, "name");
          final String description = PostgreSQLHelpers.readString(resultSet, "description");
          final Timestamp creationDate = resultSet.getTimestamp("creation_date");
          final Timestamp modificationDate = resultSet.getTimestamp("modification_date");
          final DebuggerTemplate debuggerDescription =
              debuggerManager.findDebugger(resultSet.getInt("debugger_id"));

          addressSpaces.add(new CAddressSpace(addressSpaceId, name, description, creationDate,
              modificationDate, imageBases, debuggerDescription, provider, project));
        }

        return addressSpaces;
      } finally {
        resultSet.close();
      }
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }

}
