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

import java.sql.SQLException;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;

/**
 * This class provides PostgreSQL queries for working with raw modules.
 */
public final class PostgreSQLRawModuleFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLRawModuleFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Deletes a raw module from the database. The raw module must be stored in the database connected
   * to by the provider argument.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param module The raw module to delete.
   * @throws CouldntDeleteException Thrown if the raw module could not be deleted.
   */
  public static void deleteRawModule(final AbstractSQLProvider provider, final INaviRawModule module)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(provider, "IE00529: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00530: Raw module can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00531: Raw module is not part of this database");

    final CConnection connection = provider.getConnection();

    NaviLogger.info("Deleting raw module %s", module.getName());

    final int moduleId = module.getId();

    try {
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_ADDRESS_COMMENTS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_ADDRESS_REFERENCES_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_BASIC_BLOCK_INSTRUCTIONS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_BASIC_BLOCKS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_CALLGRAPH_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_CONTROL_FLOW_GRAPHS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_EXPRESSION_NODES_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_EXPRESSION_SUBSTITUTIONS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_EXPRESSION_TREE_NODES_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_EXPRESSION_TREES_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_FUNCTIONS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_INSTRUCTIONS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_OPERANDS_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection, String.format(CTableNames.RAW_SECTIONS, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_TYPE_INSTACES, moduleId));
      PostgreSQLHelpers.deleteTable(connection, String.format(CTableNames.RAW_TYPES, moduleId));
      PostgreSQLHelpers
          .deleteTable(connection, String.format(CTableNames.RAW_BASE_TYPES, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_EXPRESSION_TYPES_TABLE, moduleId));
      PostgreSQLHelpers.deleteTable(connection,
          String.format(CTableNames.RAW_EXPRESSION_TYPE_INSTANCES, moduleId));
      PostgreSQLHelpers.deleteById(connection, CTableNames.RAW_MODULES_TABLE, moduleId);
    } catch (final SQLException e) {
      // Log this silently because failure at this point is not very
      // important

      CUtilityFunctions.logException(e);
    }
  }
}
