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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.CCallgraphEdge;
import com.google.security.zynamics.binnavi.disassembly.CCallgraphNode;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphEdge;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Provides PostgreSQL queries for loading call graphs.
 */
public final class PostgreSQLCallgraphLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLCallgraphLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks arguments for validity and throws an exception if something is wrong.
   * 
   * @param provider Provider argument to check.
   * @param module Module argument to check.
   * @param functions Functions argument to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final CModule module,
      final List<INaviFunction> functions) {
    Preconditions.checkNotNull(provider, "IE00404: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00405: Module argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(module),
        "IE00406: Module is not part of this database");
    Preconditions.checkNotNull(functions, "IE00407: Functions argument can not be null");

    for (final INaviFunction function : functions) {
      Preconditions.checkNotNull(function, "IE00408: Function list contains a null-element");
      Preconditions.checkArgument(function.inSameDatabase(module),
          "IE00409: Function list contains an element that is not part of this database");
    }
  }

  /**
   * Returns a mapping between function addresses and function objects from a list of functions.
   * 
   * @param functions The functions to process.
   * 
   * @return The address -> function mapping for all functions in the list.
   */
  private static Map<IAddress, INaviFunction> getFunctionMap(final Iterable<INaviFunction> functions) {
    final HashMap<IAddress, INaviFunction> functionMap = new HashMap<IAddress, INaviFunction>();

    for (final INaviFunction function : functions) {
      functionMap.put(function.getAddress(), function);
    }

    return functionMap;
  }

  /**
   * Loads the edges of the call graph.
   * 
   * The call graph ID and all call graph nodes must refer to objects stored in the database
   * connected to by the connection argument.
   * 
   * @param connection Connection to the database.
   * @param callgraphId ID of the call graph view to load.
   * @param nodeMap Map between call graph node IDs and call graph view objects.
   * 
   * @return The edged of the loaded call graph.
   * 
   * @throws SQLException Thrown if loading the call graph edges failed.
   */
  private static List<ICallgraphEdge> loadEdges(final CConnection connection,
      final int callgraphId, final Map<Integer, CCallgraphNode> nodeMap) throws SQLException {
    final List<ICallgraphEdge> edges = new ArrayList<ICallgraphEdge>();

    final String edgeQuery =
        "SELECT source_node_id, target_node_id" + " FROM " + CTableNames.NODES_TABLE + " JOIN "
            + CTableNames.EDGES_TABLE + " ON " + CTableNames.NODES_TABLE + ".id = "
            + CTableNames.EDGES_TABLE + ".source_node_id" + " WHERE view_id = " + callgraphId;

    final ResultSet edgeResult = connection.executeQuery(edgeQuery, true);

    try {
      while (edgeResult.next()) {
        final CCallgraphNode source = nodeMap.get(edgeResult.getInt("source_node_id"));
        final CCallgraphNode target = nodeMap.get(edgeResult.getInt("target_node_id"));

        CCallgraphNode.link(source, target);

        edges.add(new CCallgraphEdge(source, target));
      }
    } finally {
      edgeResult.close();
    }

    return edges;
  }

  /**
   * Loads the nodes of a call graph.
   * 
   * @param connection Connection to the database.
   * @param callgraphId ID of the call graph view to load.
   * @param functions List of functions in the module whose call graph is loaded.
   * 
   * @return <Call graph nodes, Call graph node IDs => Call graph nodes>
   * 
   * @throws SQLException Thrown if loading the nodes failed.
   */
  private static Pair<List<ICallgraphNode>, Map<Integer, CCallgraphNode>> loadNodes(
      final CConnection connection, final int callgraphId, final Collection<INaviFunction> functions)
      throws SQLException {
    // TODO: Simplify the return value of this method.

    // For performance reasons, we need a quick way to look up functions by their address.
    final Map<IAddress, INaviFunction> functionMap = getFunctionMap(functions);

    final List<ICallgraphNode> nodes = new ArrayList<ICallgraphNode>();

    final String nodeQuery =
        "SELECT nodes.id, function FROM " + CTableNames.NODES_TABLE + " AS nodes JOIN "
            + CTableNames.FUNCTION_NODES_TABLE
            + " AS function_nodes ON nodes.id = function_nodes.node_id WHERE nodes.view_id = "
            + callgraphId;

    final ResultSet nodeResult = connection.executeQuery(nodeQuery, true);

    final HashMap<Integer, CCallgraphNode> nodeMap = new HashMap<Integer, CCallgraphNode>();

    try {
      while (nodeResult.next()) {
        final int nodeId = nodeResult.getInt("id");
        final IAddress functionAddress = PostgreSQLHelpers.loadAddress(nodeResult, "function");
        final INaviFunction function = functionMap.get(functionAddress);

        final CCallgraphNode cgnode = new CCallgraphNode(function);

        nodeMap.put(nodeId, cgnode);

        nodes.add(cgnode);
      }
    } finally {
      nodeResult.close();
    }

    return new Pair<List<ICallgraphNode>, Map<Integer, CCallgraphNode>>(nodes, nodeMap);
  }

  /**
   * Loads the call graph of a module.
   * 
   * The module, the call graph ID, and all functions of the function list must refer to objects
   * stored in the database connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose call graph is loaded.
   * @param callgraphId The view ID of the call graph to load.
   * @param functions A list that contains all functions of the module.
   * 
   * @return The loaded call graph.
   * 
   * @throws CouldntLoadDataException Thrown if the call graph could not be loaded.
   */
  public static CCallgraph loadCallgraph(final AbstractSQLProvider provider, final CModule module,
      final int callgraphId, final List<INaviFunction> functions) throws CouldntLoadDataException {
    checkArguments(provider, module, functions);

    final CConnection connection = provider.getConnection();

    try {
      final Pair<List<ICallgraphNode>, Map<Integer, CCallgraphNode>> nodeResult =
          loadNodes(connection, callgraphId, functions);

      final List<ICallgraphEdge> edges = loadEdges(connection, callgraphId, nodeResult.second());

      return new CCallgraph(nodeResult.first(), edges);
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
