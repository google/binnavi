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

public final class PostgreSQLNativeViewCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLNativeViewCreator() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Creates the nodes of the native call graph of a module.
   * 
   * @param connection Connection to the database.
   * @param viewId ID of the native call graph.
   * @param moduleId ID of the BinNavi module where the nodes are created.
   * 
   * @throws SQLException Thrown if creating the nodes failed.
   */
  public static void createNativeCallgraphNodes(final CConnection connection, final int viewId,
      final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00455: connection argument can not be null");
    final String query = " { call create_native_callgraph_nodes(?,?) } ";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.setInt(1, viewId);
    call.setInt(2, moduleId);
    call.execute();
  }

  /**
   * Creates the native call graph view of a module.
   * 
   * @param connection Connection to the database.
   * @param moduleId ID of the BinNavi module where the view is created.
   * 
   * @return The ID of the created view.
   * 
   * @throws SQLException Thrown if creating the view failed.
   */
  public static int createNativeCallgraphView(final CConnection connection, final int moduleId)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE00706: connection argument can not be null");
    final String query = "{ ? = call create_native_call_graph_view(?) }";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.registerOutParameter(1, Types.INTEGER);
    call.setInt(2, moduleId);
    call.execute();
    return call.getInt(1);
  }

  /**
   * Creates the native code nodes of a module.
   * 
   * @param connection Connection to the database.
   * @param rawModuleId ID of the raw module that provides the data.
   * @param moduleId ID of the BinNavi module where the code nodes are created.
   * 
   * @throws SQLException Thrown if creating the native code nodes failed.
   */
  public static void createNativeCodeNodes(final CConnection connection, final int rawModuleId,
      final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00708: connection argument can not be null");
    final String query = " { call create_native_code_nodes(?,?) } ";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.setInt(1, rawModuleId);
    call.setInt(2, moduleId);
    call.execute();
  }

  /**
   * Creates the native flow graph edges of a module.
   * 
   * @param connection Connection to the database
   * @param rawModuleId ID of the raw module that provides the data.
   * @param moduleId ID of the BinNavi module where the edges are created.
   * 
   * @throws SQLException Thrown if creating the edges nodes failed.
   */
  public static void createNativeFlowgraphEdges(final CConnection connection,
      final int rawModuleId, final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE01634: connection argument can not be null");
    final String query = " { call create_native_flowgraph_edges(?,?) }";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.setInt(1, rawModuleId);
    call.setInt(2, moduleId);
    call.execute();
  }

  /**
   * Creates the native flow graph views of a module.
   * 
   * @param connection Connection to the database.
   * @param moduleId ID of the BinNavi module where the views are created.
   * 
   * @return The ID of the first created view.
   * 
   * @throws SQLException Thrown if creating the flow graph views failed.
   */
  public static int createNativeFlowgraphViews(final CConnection connection, final int moduleId)
      throws SQLException {
    Preconditions.checkNotNull(connection, "IE01816: connection argument can not be null");
    final String query = "{ ? = call create_native_flowgraph_views(?) }";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.registerOutParameter(1, Types.INTEGER);
    call.setInt(2, moduleId);
    call.execute();
    return call.getInt(1);
  }

  /**
   * Creates the edges of the native call graph of a module.
   * 
   * @param connection Connection to a BinNavi database.
   * @param rawModuleId ID of the raw module that provides the data.
   * @param moduleId ID of the BinNavi module where the edges are created.
   * 
   * @throws SQLException Thrown if creating the edges failed.
   */
  public static void createNativeCallgraphEdges(final CConnection connection,
      final int rawModuleId, final int moduleId) throws SQLException {
    Preconditions.checkNotNull(connection, "IE01870: connection argument can not be null");
    final String query = " { call create_native_callgraph_edges(?, ?) } ";
    final CallableStatement call = connection.getConnection().prepareCall(query);
    call.setInt(1, rawModuleId);
    call.setInt(2, moduleId);
    call.execute();
  }
}
