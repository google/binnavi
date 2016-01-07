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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CModuleViewFinder;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class PostgreSQLModuleFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLModuleFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Checks the validity of a given SQL provider and a given module. If there is a problem with the
   * arguments, an exception is thrown.
   *
   * @param provider The SQL provider to check.
   * @param module The module to check.
   */
  protected static void checkArguments(final AbstractSQLProvider provider,
      final INaviModule module) {
    Preconditions.checkNotNull(provider, "IE00488: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00489: Module argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00490: Module is not part of this database");
  }

  /**
   * Sets the debugger used to debug a module.
   *
   *  The module and the debugger must be stored in the database connected to by the provider
   * argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose debugger is set.
   * @param debugger The debugger that is assigned to the module or null if a former assignment
   *        should be cleared.
   *
   * @throws CouldntSaveDataException Thrown if the debugger could not be assigned to the module.
   */
  public static void assignDebugger(final AbstractSQLProvider provider, final INaviModule module,
      final DebuggerTemplate debugger) throws CouldntSaveDataException {
    checkArguments(provider, module);

    // ATTENTION: Argument "debugger" can be null

    if ((debugger != null) && !debugger.inSameDatabase(provider)) {
      throw new IllegalArgumentException("IE00491: Debugger is not part of this database");
    }

    final CConnection connection = provider.getConnection();

    try {
      final String query = String.format("update %s set debugger_id = %s where id = %d",
          CTableNames.MODULES_TABLE, debugger == null ? "NULL" : String.valueOf(debugger.getId()),
          module.getConfiguration().getId());

      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.MODULES_TABLE,
        module.getConfiguration().getId());
  }

  /**
   * Deletes a module from the database.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the module could not be deleted.
   */
  public static void deleteModule(final AbstractSQLProvider provider, final INaviModule module)
      throws CouldntDeleteException {
    PostgreSQLModuleFunctions.checkArguments(provider, module);

    NaviLogger.info("Deleting module %s", module.getConfiguration().getName());

    final CConnection connection = provider.getConnection();

    try {
      final String moduleViewQuery = "DELETE FROM " + CTableNames.VIEWS_TABLE + " "
          + " WHERE id IN (SELECT view_id FROM " + CTableNames.MODULE_VIEWS_TABLE
          + " WHERE module_id = " + module.getConfiguration().getId() + ")";

      connection.executeUpdate(moduleViewQuery, true);

      final String nodeQuery = "DELETE FROM " + CTableNames.NODES_TABLE + " " + " WHERE id IN "
          + " (SELECT view_id FROM " + CTableNames.MODULE_VIEWS_TABLE + " WHERE module_id = "
          + module.getConfiguration().getId() + ")";

      connection.executeUpdate(nodeQuery, true);

      final String instructionsQuery = String.format(
          "DELETE FROM " + CTableNames.INSTRUCTIONS_TABLE + " WHERE module_id = %d",
          module.getConfiguration().getId());

      connection.executeUpdate(instructionsQuery, true);
      connection.executeUpdate(String.format(
          "delete FROM " + CTableNames.EXPRESSION_TREE_TABLE + "_mapping where module_id = %d",
          module.getConfiguration().getId()), true);
      connection.executeUpdate(String.format(
          "delete FROM " + CTableNames.EXPRESSION_TREE_TABLE + " where module_id = %d",
          module.getConfiguration().getId()), true);
      connection.executeUpdate(String.format(
          "delete FROM " + CTableNames.EXPRESSION_TREE_TABLE + "_ids where module_id = %d",
          module.getConfiguration().getId()), true);
      connection.executeUpdate(String.format(
          "delete FROM " + CTableNames.CODE_NODES_TABLE + " where module_id = %d",
          module.getConfiguration().getId()), true);
      connection.executeUpdate(String.format(
          "delete from " + CTableNames.MODULES_TABLE + " where id = %d",
          module.getConfiguration().getId()), true);
    } catch (final SQLException e) {
      throw new CouldntDeleteException(e);
    }
  }

  /**
   * Returns the modification date of the module.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose modification date is determined.
   *
   * @return The modification date of the module.
   *
   * @throws CouldntLoadDataException Thrown if the modification date of the module could not be
   *         determined.
   */
  public static Date getModificationDate(final AbstractSQLProvider provider,
      final INaviModule module) throws CouldntLoadDataException {
    checkArguments(provider, module);

    return PostgreSQLHelpers.getModificationDate(provider.getConnection(),
        CTableNames.MODULES_TABLE, module.getConfiguration().getId());
  }

  /**
   * Finds the views inside the module that contain instructions of a given address.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module to search through.
   * @param addresses The addresses to search for.
   * @param all True, to search for views that contain all addresses. False, for any addresses.
   *
   * @return A list of views where instructions with the given address can be found.
   *
   * @throws CouldntLoadDataException Thrown if searching through the module failed.
   */
  public static List<INaviView> getViewsWithAddresses(final AbstractSQLProvider provider,
      final INaviModule module, final List<UnrelocatedAddress> addresses, final boolean all)
      throws CouldntLoadDataException {
    checkArguments(provider, module);

    Preconditions.checkNotNull(addresses, "IE00492: Addresses argument can not be null");

    final StringBuilder queryBuilder = new StringBuilder();

    final int moduleID = module.getConfiguration().getId();

    if (addresses.size() == 0) {
      return new ArrayList<INaviView>();
    } else if (addresses.size() == 1) {
      queryBuilder.append("SELECT mvt.module_id, mvt.view_id FROM " + CTableNames.MODULE_VIEWS_TABLE
          + " AS mvt JOIN " + CTableNames.NODES_TABLE
          + " AS nt ON mvt.view_id = nt.view_id AND mvt.module_id = " + moduleID + " JOIN "
          + CTableNames.CODENODE_INSTRUCTIONS_TABLE
          + " AS cit ON nt.id = cit.node_id AND cit.module_id = " + moduleID + " JOIN "
          + CTableNames.INSTRUCTIONS_TABLE
          + " AS it ON it.address = cit.address AND it.module_id = " + moduleID
          + " WHERE it.address = " + addresses.get(0).getAddress().toLong());
    } else if (all) {
      boolean needsComma = false;

      int counter = 0;

      queryBuilder.append("select view_id from ");

      for (final UnrelocatedAddress address : addresses) {
        if (needsComma) {
          queryBuilder.append(" inner join ");
        }

        needsComma = true;

        queryBuilder.append("(SELECT mvt.module_id, mvt.view_id FROM "
            + CTableNames.MODULE_VIEWS_TABLE + " AS mvt JOIN " + CTableNames.NODES_TABLE
            + " AS nt ON mvt.view_id = nt.view_id AND mvt.module_id = " + moduleID + " JOIN "
            + CTableNames.CODENODE_INSTRUCTIONS_TABLE
            + " AS cit ON nt.id = cit.node_id AND cit.module_id = " + moduleID + " JOIN "
            + CTableNames.INSTRUCTIONS_TABLE
            + " AS it ON it.address = cit.address AND it.module_id = " + moduleID
            + " WHERE it.address = " + address.getAddress().toLong() + ") AS t" + counter);

        counter++;
      }

      queryBuilder.append(" USING (view_id)");
    } else {
      queryBuilder.append("SELECT mvt.module_id, mvt.view_id FROM " + CTableNames.MODULE_VIEWS_TABLE
          + " AS mvt JOIN " + CTableNames.NODES_TABLE
          + " AS nt ON mvt.view_id = nt.view_id AND mvt.module_id = " + moduleID + " JOIN "
          + CTableNames.CODENODE_INSTRUCTIONS_TABLE
          + " AS cit ON nt.id = cit.node_id AND cit.module_id = " + moduleID + " JOIN "
          + CTableNames.INSTRUCTIONS_TABLE
          + " AS it ON it.address = cit.address AND it.module_id = " + moduleID
          + " WHERE it.address IN (");

      boolean needsComma = false;

      for (final UnrelocatedAddress address : addresses) {
        if (needsComma) {
          queryBuilder.append(", ");
        }

        needsComma = true;

        queryBuilder.append(address.getAddress().toLong());
      }

      queryBuilder.append(") GROUP BY mvt.view_id, mvt.module_id");
    }

    return PostgreSQLHelpers.getViewsWithAddress(provider.getConnection(), queryBuilder.toString(),
        "module_id", new CModuleViewFinder(provider));
  }

  public static CModule readModule(final CConnection connection, final int moduleId,
      final INaviRawModule rawModule, final SQLProvider provider) throws CouldntLoadDataException {
    Preconditions.checkNotNull(rawModule, "IE01797: Raw module argument can not be null");
    Preconditions.checkNotNull(provider, "IE01798: Provider argument can not be null");

    final String query = "SELECT id, " + CTableNames.MODULES_TABLE + ".name, md5, sha1, "
        + " description, import_time, modification_date, image_base, file_base, stared, "
        + " initialization_state " + " FROM " + CTableNames.MODULES_TABLE + " WHERE id = "
        + moduleId + " ORDER by id";

    try {
      final ResultSet resultSet = connection.executeQuery(query, true);

      try {
        while (resultSet.next()) {
          final String name = PostgreSQLHelpers.readString(resultSet, "name");
          final String md5 = PostgreSQLHelpers.readString(resultSet, "md5");
          final String sha1 = PostgreSQLHelpers.readString(resultSet, "sha1");
          final String comment = PostgreSQLHelpers.readString(resultSet, "description");
          final Timestamp importTime = resultSet.getTimestamp("import_time");
          final Timestamp modificationDate = resultSet.getTimestamp("modification_date");
          final int functionCount = rawModule.getFunctionCount();
          final int viewCount = 0;
          final IAddress imageBase = PostgreSQLHelpers.loadAddress(resultSet, "image_base");
          final IAddress fileBase = PostgreSQLHelpers.loadAddress(resultSet, "file_base");
          final boolean isStared = resultSet.getBoolean("stared");
          final int initializationState = resultSet.getInt("initialization_state");

          return new CModule(moduleId,
              name,
              comment,
              importTime,
              modificationDate,
              md5,
              sha1,
              functionCount,
              viewCount,
              fileBase,
              imageBase,
              null,
              rawModule,
              initializationState,
              isStared,
              provider);
        }
      } finally {
        resultSet.close();
      }
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }

    throw new CouldntLoadDataException("Error: No module with the given ID exists");
  }

  /**
   * Changes the description of a module.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose description is changed.
   * @param description The new description of the module.
   *
   * @throws CouldntSaveDataException Thrown if the description of the module could not be changed.
   */
  public static void setDescription(final AbstractSQLProvider provider, final INaviModule module,
      final String description) throws CouldntSaveDataException {
    checkArguments(provider, module);

    Preconditions.checkNotNull(description, "IE00493: Description argument can not be null");

    PostgreSQLHelpers.setDescription(provider.getConnection(), module.getConfiguration().getId(),
        description, CTableNames.MODULES_TABLE);
  }

  /**
   * Changes the file base of a module.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose file base is changed.
   * @param address The new file base of the module.
   *
   * @throws CouldntSaveDataException Thrown if the file base of the module could not be changed.
   */
  public static void setFileBase(final AbstractSQLProvider provider, final INaviModule module,
      final IAddress address) throws CouldntSaveDataException {
    checkArguments(provider, module);
    Preconditions.checkNotNull(address, "IE00494: Address argument can not be null");
    final CConnection connection = provider.getConnection();

    try {
      final String query = String.format("UPDATE %s SET file_base = %s " + " WHERE id = %d",
          CTableNames.MODULES_TABLE, address.toBigInteger().toString(),
          module.getConfiguration().getId());
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.MODULES_TABLE,
        module.getConfiguration().getId());
  }

  /**
   * Changes the image base of a module.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose image base is changed.
   * @param address The new image base of the module.
   *
   * @throws CouldntSaveDataException Thrown if the image base of the module could not be changed.
   */
  public static void setImageBase(final AbstractSQLProvider provider, final INaviModule module,
      final IAddress address) throws CouldntSaveDataException {
    checkArguments(provider, module);
    Preconditions.checkNotNull(address, "IE00495: Address argument can not be null");
    final CConnection connection = provider.getConnection();

    try {
      final String query = String.format("UPDATE %s SET image_base = %s " + " WHERE id = %d",
          CTableNames.MODULES_TABLE, address.toBigInteger().toString(),
          module.getConfiguration().getId());

      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

    PostgreSQLHelpers.updateModificationDate(connection, CTableNames.MODULES_TABLE,
        module.getConfiguration().getId());
  }

  /**
   * Changes the name of a module.
   *
   * The module must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose name is changed.
   * @param name The new name of the module.
   *
   * @throws CouldntSaveDataException Thrown if changing the name of the module changed.
   */
  public static void setName(final AbstractSQLProvider provider, final INaviModule module,
      final String name) throws CouldntSaveDataException {
    checkArguments(provider, module);
    Preconditions.checkNotNull(name, "IE00496: Name argument can not be null");
    PostgreSQLHelpers.setName(provider.getConnection(), module.getConfiguration().getId(), name,
        CTableNames.MODULES_TABLE);
  }

  /**
   * Stars a module.
   *
   * @param provider Provides the connection to the database.
   * @param module The module to star.
   * @param isStared True, to star the module. False, to unstar it.
   *
   * @throws CouldntSaveDataException Thrown if the the star state of the module could not be
   *         updated.
   */
  public static void starModule(final AbstractSQLProvider provider, final INaviModule module,
      final boolean isStared) throws CouldntSaveDataException {
    final String starModuleQuery = "UPDATE " + CTableNames.MODULES_TABLE + " SET stared = "
        + isStared + " WHERE id = " + module.getConfiguration().getId();
    try {
      provider.getConnection().executeUpdate(starModuleQuery, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }
}
