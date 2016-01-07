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

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.general.ByteHelpers;

public final class PostgreSQLDataFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLDataFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Saves the data of a module to the database.
   * 
   * @param provider Provides the connection to the database.
   * @param module The module whose data is stored in the database.
   * @param data The data of the module to store in the database.
   * 
   * @throws CouldntSaveDataException Thrown if the module data could not be stored.
   */
  public static void saveData(final AbstractSQLProvider provider, final INaviModule module,
      final byte[] data) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE01267: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE01268: Module argument can not be null");
    Preconditions.checkNotNull(data, "IE01269: Data argument can not be null");

    final CConnection connection = provider.getConnection();

    try {
      connection.executeUpdate("DELETE FROM " + CTableNames.DATA_PARTS_TABLE
          + " WHERE module_id = " + module.getConfiguration().getId(), true);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }

    final String preparedStatement = "INSERT INTO " + CTableNames.DATA_PARTS_TABLE
          + "(module_id, part_id, data) VALUES(?, ?, ?)";
          
    try (PreparedStatement statement = 
      provider.getConnection().getConnection().prepareStatement(preparedStatement)) {
      statement.setInt(1, module.getConfiguration().getId());
      statement.setInt(2, 0);
      statement.setBinaryStream(3, new ByteArrayInputStream(data, 0, data.length), data.length);
      statement.execute();
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Loads the individual data chunks of module data from the database.
   * 
   * The module must be stored in the database connected to by the provider argument.
   * 
   * @param provider Provides the connection to the database.
   * @param module The module whose data chunks are loaded.
   * 
   * @return A list of chunks loaded from the database.
   * 
   * @throws SQLException Thrown if the data chunks could not be loaded.
   */
  public static List<byte[]> loadDataChunks(final AbstractSQLProvider provider, final CModule module)
      throws SQLException {
    final List<byte[]> dataList = new ArrayList<>();

    final String query =
        "SELECT data FROM " + CTableNames.DATA_PARTS_TABLE + " WHERE module_id = "
            + module.getConfiguration().getId() + " ORDER BY part_id ASC";

    try (ResultSet resultSet = provider.executeQuery(query)) {
      while (resultSet.next()) {
        dataList.add(resultSet.getBytes("data"));
      }
    }

    return dataList;
  }

  /**
   * Loads the data of a module from the database.
   * 
   * The module must be a module stored in the database.
   * 
   * @param provider Provides the connection to the database.
   * @param module The module whose data is loaded.
   * 
   * @return The module data loaded from the database.
   * 
   * @throws CouldntLoadDataException Thrown if the module data could not be loaded.
   */
  public static byte[] loadData(final AbstractSQLProvider provider, final CModule module)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(provider, "IE01265: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE01266: Module argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00532: Module is not stored in the given database");

    try {
      return ByteHelpers.combine(loadDataChunks(provider, module));
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }
}
