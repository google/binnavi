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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Contains code for loading the nodes of a view.
 */
public final class PostgreSQLNodeLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLNodeLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads the node tags of a list of nodes.
   * 
   * @param connection The connection to the database.
   * @param nodes The nodes whose tags are loaded.
   * @param nodeTagManager Manages all node tags of the database.
   * 
   * @throws SQLException Thrown if loading the node tags failed.
   */
  private static void loadNodeTags(final CConnection connection, final List<INaviViewNode> nodes,
      final CTagManager nodeTagManager) throws SQLException {
    final HashMap<Integer, INaviViewNode> idNodeMap = new HashMap<Integer, INaviViewNode>();
    final HashMap<Integer, CTag> idTagMap = new HashMap<Integer, CTag>();

    final StringBuffer range = new StringBuffer();

    boolean isFirst = true;

    for (final INaviViewNode node : nodes) {
      range.append(isFirst ? "" : ",");
      range.append(node.getId());
      isFirst = false;

      idNodeMap.put(node.getId(), node);
    }

    if (isFirst) {
      return;
    }

    final String query =
        String.format("SELECT node_id, tag_id FROM %s WHERE node_id IN (%s)",
            CTableNames.TAGGED_NODES_TABLE, range.toString());

    final ResultSet resultSet = connection.executeQuery(query, true);

    try {
      final Set<Integer> tagIds = new HashSet<Integer>();
      while (resultSet.next()) {
        tagIds.add(resultSet.getInt("tag_id"));
      }

      final Collection<CTag> tags = CTagHelpers.findTags(nodeTagManager.getRootTag(), tagIds);
      for (final CTag tag : tags) {
        idTagMap.put(tag.getId(), tag);
      }

      resultSet.beforeFirst();
      while (resultSet.next()) {
        final INaviViewNode node = idNodeMap.get(resultSet.getInt("node_id"));
        final CTag tag = idTagMap.get(resultSet.getInt("tag_id"));
        ((CNaviViewNode) node).tagNodeSilent(tag);
      }
    } finally {
      resultSet.close();
    }
  }

  /**
   * Loads the view nodes of a view.
   * 
   * @param provider The connection to the database.
   * @param view The view whose nodes are loaded.
   * @param modules All modules that belong to the database.
   * @param nodeTagManager Tag manager responsible for tagging the nodes of the view.
   * 
   * @return The loaded nodes.
   * 
   * @throws SQLException Thrown of loading the nodes failed.
   * @throws CPartialLoadException Thrown if loading the nodes failed because a necessary module was
   *         not loaded.
   * @throws CouldntLoadDataException
   */
  public static List<INaviViewNode> loadNodes(final AbstractSQLProvider provider,
      final INaviView view, final List<INaviModule> modules, final CTagManager nodeTagManager)
      throws SQLException, CPartialLoadException, CouldntLoadDataException {
    final List<INaviViewNode> nodes = new ArrayList<INaviViewNode>();

    PostgreSQLGroupNodeLoader.load(provider, view, nodes);
    PostgreSQLFunctionNodeLoader.load(provider, view, nodes);
    PostgreSQLCodeNodeLoader.load(provider, view, nodes, modules);
    PostgreSQLTextNodeLoader.load(provider, view, nodes);

    // It is very, very important to return the nodes in the order of their IDs
    // because when a graph is Saved As, the order of the loaded nodes is compared
    // to the order of the nodes before the graph was saved.
    //
    // Furthermore this must happen before group nodes are set up.
    // TODO: sp has said this sometime in the past without any reasoning why
    // therefore this has to be checked and understood otherwise this code is just
    // burning cycles.
    Collections.sort(nodes, new Comparator<INaviViewNode>() {
      @Override
      public int compare(final INaviViewNode lhs, final INaviViewNode rhs) {
        return lhs.getId() - rhs.getId();
      }
    });

    final CConnection connection = provider.getConnection();

    PostgreSQLGroupNodeLoader.setupGroupNodes(connection, view, nodes);

    loadNodeTags(connection, nodes, nodeTagManager);

    return nodes;
  }
}
