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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CConnection;

public final class PostgreSQLModuleCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLModuleCreator() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Fills the table that connects the views of a module with the functions of a module.
   * 
   * @param connection Connection to the database.
   * @param moduleId ID of the BinNavi module that contains the views and functions.
   * @param firstViewId ID of the first BinNavi view to connect.
   * 
   * @throws SQLException Thrown if connecting the views to the functions failed.
   */
  public static void connectViewsFunctions(final CConnection connection, final int moduleId,
      final int firstViewId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00453: connection argument can not be null");
    final String query = " { call connect_views_to_functions(?,?) } ";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.setInt(1, moduleId);
    call.setInt(2, firstViewId);
    call.execute();
  }

  /**
   * Creates a new BinNavi module.
   * 
   * @param connection Connection to the database.
   * @param rawModuleId ID of the raw module that provides the data.
   * 
   * @return ID of the created module.
   * 
   * @throws SQLException Thrown if the module could not be created.
   */
  public static int createNewModule(final CConnection connection, final int rawModuleId)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE01857: Connection argument can not be null");
    final String query = " { ? = call create_module(?) } ";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.registerOutParameter(1, Types.INTEGER);
    call.setInt(2, rawModuleId);
    call.execute();
    return call.getInt(1);
  }

  /**
   * Fills the table that connects the instructions of a module with the code nodes of a module.
   * 
   * @param connection Connection to the database.
   * @param moduleId ID of the BinNavi module that contains the code nodes.
   * @param rawModuleId The ID of the raw module that provides the input data.
   * 
   * @throws SQLException Thrown if connecting the instructions to the code nodes failed.
   */
  public static void connectInstructionsToCodeNodes(final CConnection connection,
      final int rawModuleId, final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00454: connection argument can not be null");
    final String query = " { call connect_instructions_to_code_nodes(?,?) } ";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.setInt(1, rawModuleId);
    call.setInt(2, moduleId);
    call.execute();
  }
}
