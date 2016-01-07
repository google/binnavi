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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CConnection;

/**
 * Contains SQL queries to assign the initial border color to entry nodes and exit nodes of native
 * views.
 * 
 * The functions from this class are used to initialize the border colors of special code nodes when
 * a module is first initialized.
 */
public final class PostgreSQLBorderColorizer {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLBorderColorizer() {
    // You are not supposed to instantiate this class
  }

  /**
   * Sets the border color of all entry nodes to green, the border color of all exit nodes to red,
   * and the border color of all nodes that are both entry and exist nodes to blue.
   * 
   * The module ID given must refer to a module that is stored in the database connected to by the
   * connection argument.
   * 
   * @param connection Connection to a SQL database.
   * @param moduleId ID of the module the nodes belong to.
   * 
   * @throws SQLException if the color of the nodes border could not be updated.
   */
  public static void setInitialBorderColors(final CConnection connection, final int moduleId)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE00403: Connection argument can not be null");
    final String query = " { call colorize_module_nodes(?) } ";
    final CallableStatement colorizeNodes = connection.getConnection().prepareCall(query);
    colorizeNodes.setInt(1, moduleId);
    colorizeNodes.execute();
  }
}
