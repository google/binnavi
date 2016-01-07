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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;

/**
 * This class provides PostgreSQL queries for working with settings.
 */
public final class PostgreSQLSettingsFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLSettingsFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Reads a view container setting from the database.
   * 
   * @param connection The connection to the database.
   * @param containerId The ID of the view container.
   * @param key The name of the setting to read.
   * @param column The name of the column that identifies the view container ID.
   * @param table The name of the table that identifies the view container table.
   * 
   * @return The loaded setting.
   * 
   * @throws CouldntLoadDataException Thrown if the setting could not be loaded.
   */
  private static String readSetting(final CConnection connection, final int containerId,
      final String key, final String column, final String table) throws CouldntLoadDataException {
    try {
      final PreparedStatement statement =
          connection.getConnection().prepareStatement(
              "select value from " + table + " where name = ? and " + column + " = ?");

      try {
        statement.setString(1, key);
        statement.setInt(2, containerId);

        final ResultSet resultSet = statement.executeQuery();

        try {
          while (resultSet.next()) {
            return PostgreSQLHelpers.readString(resultSet, "value");
          }
        } finally {
          resultSet.close();
        }

        return null;
      } finally {
        statement.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Writes a view container setting to the database.
   * 
   * @param connection The connection to the database.
   * @param containerId The ID of the view container.
   * @param key The name of the setting to write.
   * @param value The value of the setting to write.
   * @param table The name of the table that identifies the view container table.
   * 
   * @throws CouldntSaveDataException Thrown if the setting could not be written to the database.
   */
  private static void writeSetting(final CConnection connection, final int containerId,
      final String key, final String value, final String table) throws CouldntSaveDataException {
    String id_column = "";

    if (table.equalsIgnoreCase(CTableNames.MODULE_SETTINGS_TABLE)) {
      id_column = "module_id";
    } else {
      id_column = "project_id";
    }

    final String deleteQuery =
        "DELETE FROM " + table + " WHERE " + id_column + " = " + containerId + " AND \"name\" = \'"
            + key + "\'";
    final String insertQuery =
        "INSERT INTO " + table + " VALUES(\'" + containerId + "\',\'" + key + "\'," + value + ")";

    try {
      PostgreSQLHelpers.beginTransaction(connection);
      connection.executeUpdate(deleteQuery, true);
      connection.executeUpdate(insertQuery, true);
      PostgreSQLHelpers.endTransaction(connection);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException("E00058: Could not update setting on " + table);
    }
  }

  /**
   * Reads a module setting from the database.
   * 
   * @param provider The connection to the database.
   * @param module The module whose setting is read.
   * @param key The name of the setting to read.
   * 
   * @return The loaded setting.
   * 
   * @throws CouldntLoadDataException Thrown if the setting could not be read.
   */
  public static String readSetting(final AbstractSQLProvider provider, final CModule module,
      final String key) throws CouldntLoadDataException {
    Preconditions.checkNotNull(module, "IE00534: Module argument can not be null");
    Preconditions.checkNotNull(key, "IE00535: Key argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00536: Module is not part of this database");
    return readSetting(provider.getConnection(), module.getConfiguration().getId(), key,
        "module_id", CTableNames.MODULE_SETTINGS_TABLE);
  }

  /**
   * Reads a project setting from the database.
   * 
   * @param provider The connection to the database.
   * @param project The project whose setting is read.
   * @param key The name of the setting to read.
   * 
   * @return The loaded setting.
   * 
   * @throws CouldntLoadDataException Thrown if the setting could not be read.
   */
  public static String readSetting(final AbstractSQLProvider provider, final INaviProject project,
      final String key) throws CouldntLoadDataException {
    Preconditions.checkNotNull(project, "IE00537: Project argument can not be null");
    Preconditions.checkNotNull(key, "IE00538: Key argument can not be null");
    Preconditions.checkArgument(project.inSameDatabase(provider),
        "IE00539: Project is not part of this database");
    return readSetting(provider.getConnection(), project.getConfiguration().getId(), key,
        "project_id", CTableNames.PROJECT_SETTINGS_TABLE);
  }

  /**
   * Writes a module setting to the database.
   * 
   * @param provider The connection to the database.
   * @param module The module whose setting is written.
   * @param key Name of the setting to write.
   * @param value Value of the setting to write.
   * 
   * @throws CouldntSaveDataException Thrown if the setting could not be written.
   */
  public static void writeSetting(final AbstractSQLProvider provider, final CModule module,
      final String key, final String value) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE01999: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00540: Module argument can not be null");
    Preconditions.checkNotNull(key, "IE00541: Key argument can not be null");
    Preconditions.checkNotNull(value, "IE02011: Value argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00542: Module is not part of this database");
    writeSetting(provider.getConnection(), module.getConfiguration().getId(), key, value,
        CTableNames.MODULE_SETTINGS_TABLE);
  }

  /**
   * Writes a project setting to the database.
   * 
   * @param provider The connection to the database.
   * @param project The project whose setting is written.
   * @param key Name of the setting to write.
   * @param value Value of the setting to write.
   * 
   * @throws CouldntSaveDataException Thrown if the setting could not be written.
   */
  public static void writeSetting(final AbstractSQLProvider provider, final INaviProject project,
      final String key, final String value) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE02050: Provider argument can not be null");
    Preconditions.checkNotNull(project, "IE00543: Project argument can not be null");
    Preconditions.checkNotNull(key, "IE00544: Key argument can not be null");
    Preconditions.checkNotNull(value, "IE02082: Value argument can not be null");
    Preconditions.checkArgument(project.inSameDatabase(provider),
        "IE00545: Project is not part of this database");
    writeSetting(provider.getConnection(), project.getConfiguration().getId(), key, value,
        CTableNames.PROJECT_SETTINGS_TABLE);
  }
}
