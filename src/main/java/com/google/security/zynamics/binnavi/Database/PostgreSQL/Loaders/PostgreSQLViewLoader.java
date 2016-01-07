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

import java.sql.SQLException;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.cache.EdgeCache;
import com.google.security.zynamics.binnavi.Database.cache.NodeCache;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;

/**
 * Contains PostgreSQL queries to load views.
 */
public final class PostgreSQLViewLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLViewLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks arguments for validity.
   * 
   * @param provider The provider argument to check.
   * @param view The view argument to check.
   * @param modules The modules argument to check.
   * @param nodeTagManager The node tag manager argument to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final INaviView view,
      final List<INaviModule> modules, final CTagManager nodeTagManager) {
    Preconditions.checkNotNull(provider, "IE00619: Provider argument can not be null");
    Preconditions.checkNotNull(view, "IE00620: View argument can not be null");
    Preconditions.checkArgument(view.inSameDatabase(provider),
        "IE00621: View is not part of this database");
    Preconditions.checkNotNull(modules, "IE00622: Modules argument can not be null");

    for (final INaviModule module : modules) {
      Preconditions.checkNotNull(module, "IE00623: Modules list contains a null element");
      Preconditions.checkArgument(module.inSameDatabase(provider),
          "IE00624: Module is not part of this database");
    }

    Preconditions
        .checkNotNull(nodeTagManager, "IE00625: Node tag manager argument can not be null");
    Preconditions.checkArgument(nodeTagManager.inSameDatabase(provider),
        "IE00626: Node tag manager is not part of this database");
  }

  /**
   * Loads the graph of a view from the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view to load.
   * @param list A list of all modules that are part of the database.
   * @param nodeTagManager Node tag manager of the database.
   * 
   * @return The graph of the view.
   * 
   * @throws CouldntLoadDataException Thrown if the graph of view could not be loaded.
   * @throws CPartialLoadException Thrown if the graph could not be loaded because not all required
   *         modules are loaded.
   */
  public static MutableDirectedGraph<INaviViewNode, INaviEdge> loadView(
      final AbstractSQLProvider provider, final INaviView view, final List<INaviModule> list,
      final CTagManager nodeTagManager) throws CouldntLoadDataException, CPartialLoadException {
    checkArguments(provider, view, list, nodeTagManager);

    try {
      final List<INaviViewNode> nodes =
          PostgreSQLNodeLoader.loadNodes(provider, view, list, nodeTagManager);

      NodeCache.get(provider).addNodes(nodes);

      final List<INaviEdge> edges = PostgreSQLEdgeLoader.loadEdges(provider, view, nodes);

      EdgeCache.get(provider).addEdges(edges);

      return new MutableDirectedGraph<INaviViewNode, INaviEdge>(nodes, edges);
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
