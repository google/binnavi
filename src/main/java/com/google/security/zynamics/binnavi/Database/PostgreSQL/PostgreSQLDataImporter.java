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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class provides the SQL queries that are necessary to convert data from the exporter tables
 * into BinNavi data.
 */
public final class PostgreSQLDataImporter {


  /**
   * Do not instantiate this class.
   */
  private PostgreSQLDataImporter() {
    // You are not supposed to instantiate this class
  }

  /**
   * Determines the architecture string of a raw module.
   * 
   * The raw module ID must refer to a raw module that is stored in the database connected to by the
   * connection argument.
   * 
   * @param connection The connection to the database.
   * @param rawModuleId The ID of the raw module whose architecture is determined.
   * 
   * @return The architecture string of the given module.
   * 
   * @throws SQLException Thrown if the architecture string could not be read.
   */
  private static String getArchitecture(final CConnection connection, final int rawModuleId)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE00207: provider argument can not be null");

    final String query = "SELECT architecture FROM modules WHERE id = " + rawModuleId;
    try (ResultSet resultSet =
        connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        return PostgreSQLHelpers.readString(resultSet, "architecture");
      }

      throw new SQLException("Error: Could not determine architecture of new module");
    }
  }

  /**
   * Connects expression trees with their expression tree nodes.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module the.
   * @throws SQLException Thrown if the connection failed.
   */
  public static void connectExpressionTrees(final CConnection connection, final int moduleId,
      final int rawModuleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00208: provider argument can not be null");

    final String query =
        "INSERT INTO " + CTableNames.EXPRESSION_TREE_MAPPING_TABLE + " (SELECT " + moduleId
        + ", expression_tree_id, expression_node_id " + " FROM ex_" + rawModuleId
        + "_expression_tree_nodes)";

    connection.executeUpdate(query, true);
  }

  /**
   * Imports the address references table.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importAddressReferences(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00266: connection argument can not be null");
    Preconditions.checkArgument(rawModuleId >= 0,
        "Error: raw module id can only be a positive number.");
    Preconditions.checkArgument(moduleId >= 0, "Error: module if can only be a positive number");

    final String query =
        "INSERT INTO " + CTableNames.ADDRESS_REFERENCES_TABLE
        + " (module_id, address, position, expression_id, type, target) " + " SELECT "
        + moduleId + ", address, position, expression_node_id, "
        + " (ENUM_RANGE(NULL::address_reference_type))[type + 1], destination  " + " FROM ex_"
        + rawModuleId
        + "_address_references  WHERE position IS NOT NULL AND expression_node_id IS NOT NULL;";

    connection.executeUpdate(query, true);
  }

  /**
   * Imports the base types.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importBaseTypes(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    final String query =
        "INSERT INTO " + CTableNames.BASE_TYPES_TABLE + " SELECT " + moduleId
        + ", id, name, size, pointer, signed " + "FROM "
        + String.format(CTableNames.RAW_BASE_TYPES, rawModuleId);
    connection.executeUpdate(query, true);

    final String updateSequence =
        String.format("SELECT setval('bn_base_types_id_seq', " +
                      "COALESCE((SELECT MAX(id) + 1 FROM %s), 1), false) from %s",
            CTableNames.BASE_TYPES_TABLE, CTableNames.BASE_TYPES_TABLE);
    connection.executeQuery(updateSequence, true);
  }

  /**
   * Imports the expression substitutions table.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importExpressionSubstitutions(final CConnection connection,
      final int rawModuleId, final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00433: connection argument can not be null");

    final String query =
        "INSERT INTO " + CTableNames.EXPRESSION_SUBSTITUTIONS_TABLE
        + " (module_id, address, position, expression_id, replacement) " + " SELECT "
        + moduleId + " , address, position, expression_node_id, replacement " + " FROM ex_"
        + rawModuleId + "_expression_substitutions";
    connection.executeUpdate(query, true);
  }

  /**
   * Imports the expression tree table.
   * 
   * @param connection Connection to the SQL database.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importExpressionTree(final CConnection connection, final int moduleId,
      final int rawModuleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00434: connection argument can not be null");

    final String query =
        "INSERT INTO " + CTableNames.EXPRESSION_TREE_TABLE
        + "(module_id, id, type, symbol, immediate, position, parent_id)" + " SELECT "
        + moduleId + ", id, type, symbol, immediate, position, parent_id " + " FROM ex_"
        + rawModuleId + "_expression_nodes";
    connection.executeUpdate(query, true);
  }

  /**
   * Imports the expressions table tree.
   * 
   * @param connection Connection to the SQL database.
   * @param moduleId ID of the raw module.
   * 
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importExpressionTrees(final CConnection connection, final int moduleId,
      final int rawModuleId) throws SQLException {
    final String query =
        "INSERT INTO " + CTableNames.EXPRESSION_TREE_IDS_TABLE + " SELECT " + moduleId + ", id "
            + " FROM ex_" + rawModuleId + "_expression_trees";
    connection.executeUpdate(query, true);
  }

  /**
   * Imports the expression type substitutions.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importExpressionTypes(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    final String query =
        "INSERT INTO " + CTableNames.EXPRESSION_TYPES_TABLE + " SELECT " + moduleId
        + ", address, \"position\", \"offset\", expression_id, type" + " FROM "
        + String.format(CTableNames.RAW_EXPRESSION_TYPES_TABLE, rawModuleId);
    connection.executeUpdate(query, true);
  }


  /**
   * Imports the functions table.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * 
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importFunctions(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00435: connection argument can not be null");
    Preconditions.checkArgument(rawModuleId >= 0,
        "Error: raw module id can only be a positive number.");
    Preconditions.checkArgument(moduleId >= 0, "Error: module if can only be a positive number");

    final String query =
        "INSERT INTO " + CTableNames.FUNCTIONS_TABLE
        + " (module_id, address, name, original_name, type, description, "
        + "parent_module_name, parent_module_id, parent_module_function, comment_id) "
        + " SELECT " + moduleId
        + ", address, demangled_name, name, (ENUM_RANGE(NULL::function_type))[type + 1], "
        + "'', module_name, null, null, null " + " FROM ex_" + rawModuleId + "_functions";
    connection.executeUpdate(query, true);
  }

  /**
   * Imports the instructions table.
   * 
   * @param provider The instance that provides access to a database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * 
   * @throws SQLException Thrown if the data could not be imported.
   * @throws CouldntLoadDataException if the user manager could not be loaded.
   */
  public static void importInstructions(final SQLProvider provider, final int rawModuleId,
      final int moduleId) throws SQLException, CouldntLoadDataException {
    final String architecture = getArchitecture(provider.getConnection(), rawModuleId);

    final int userId = CUserManager.get(provider).getCurrentActiveUser().getUserId();

    final String query =
        "WITH comments_to_id(id, address, comment) AS " + " ( "
            + "   SELECT nextval('bn_comments_id_seq'::regclass), address, comment "
            + "   FROM ex_"
            + rawModuleId
            + "_address_comments "
            + " ), comments_table AS ( "
            + "   INSERT INTO "
            + CTableNames.COMMENTS_TABLE
            + " (id, parent_id, user_id, comment_text) "
            + "   SELECT id, null, "
            + userId
            + ", comment "
            + "   FROM comments_to_id "
            + " ) "
            + "   INSERT INTO "
            + CTableNames.INSTRUCTIONS_TABLE
            + " (module_id, address, mnemonic, data, native, architecture, comment_id) "
            + "   SELECT "
            + moduleId
            + ", isn.address, mnemonic, data, true, '"
            + architecture
            + "', com.id "
            + "   FROM ex_"
            + rawModuleId
            + "_instructions AS isn "
            + "   LEFT JOIN comments_to_id AS com ON com.address = isn.address; ";

    provider.getConnection().executeUpdate(query, true);
  }

  /**
   * Imports the operands table.
   * 
   * @param connection Connection to the SQL database.
   * @param rawModuleId ID of the raw module from which to import the data.
   * @param moduleId ID of the BinNavi module where the data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importOperands(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    final String query =
        "INSERT INTO " + CTableNames.OPERANDS_TABLE + " SELECT " + moduleId
        + ", address, expression_tree_id, position " + " FROM ex_" + rawModuleId + "_operands";
    connection.executeUpdate(query, true);
  }

  /**
   * Imports the type members.
   * 
   * @param connection The connection to the SQL database.
   * @param rawModuleId The Id of the raw module from which to import the data.
   * @param moduleId The Id of the BinNavi module where data is imported to.
   * @throws SQLException Thrown if the data could not be imported.
   */
  public static void importTypes(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    final String query =
        "INSERT INTO " + CTableNames.TYPE_MEMBERS_TABLE + " SELECT " + moduleId
        + ", id, name, base_type, parent_id, position, argument, number_of_elements" + " FROM "
        + String.format(CTableNames.RAW_TYPES, rawModuleId);
    connection.executeUpdate(query, true);

    final String updateSequence = String.format(
        "SELECT setval('bn_types_id_seq', " +
        "COALESCE((SELECT MAX(id) + 1 FROM %s), 1), false) from %s",
        CTableNames.TYPE_MEMBERS_TABLE, CTableNames.TYPE_MEMBERS_TABLE);
    connection.executeQuery(updateSequence, true);
  }
}
