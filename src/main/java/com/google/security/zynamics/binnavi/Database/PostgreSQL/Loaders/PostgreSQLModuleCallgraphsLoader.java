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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * This class provides queries that can be used to load information about module call graph views.
 */
public final class PostgreSQLModuleCallgraphsLoader extends PostgreSQLViewsLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLModuleCallgraphsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads the call graph views of a module. Depending on the view type argument, this function can
   * load the native call graph view or non-native call graph views.
   * 
   * The arguments module, viewTagManager, and nodeTagManager must belong to the database connected
   * to by the provider argument.
   * 
   * @param provider The connection to the database.
   * @param module The module whose call graph views are loaded.
   * @param viewTagManager The tag manager responsible for tagging the views.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * @param viewType The type of the views to load.
   * 
   * @return The loaded views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  private static IFilledList<ICallgraphView> loadModuleCallgraphs(
      final AbstractSQLProvider provider, final CModule module, final CTagManager viewTagManager,
      final ITagManager nodeTagManager, final ViewType viewType) throws CouldntLoadDataException {
    PostgreSQLViewsLoader.checkArguments(provider, module, viewTagManager);

    final String query = "SELECT * FROM load_module_call_graphs(?, ?)";
    try {
      final CConnection connection = provider.getConnection();
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);

      statement.setInt(1, module.getConfiguration().getId());
      statement.setObject(2, viewType == ViewType.Native ? "native" : "non-native", Types.OTHER);

      final ResultSet resultSet = statement.executeQuery();

      final Map<Integer, Set<CTag>> tags =
          PostgreSQLModuleViewsLoader.loadTags(connection, module, viewTagManager);

      return new FilledList<ICallgraphView>(processQueryResults(resultSet, module, tags,
          nodeTagManager, provider, new ArrayList<CView>(), viewType, GraphType.CALLGRAPH));
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Loads the non-native call graph views of a module.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param module The module from where the views are loaded.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return A list of non-native call graph views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static IFilledList<ICallgraphView> loadCallgraphViews(final AbstractSQLProvider provider,
      final CModule module, final CTagManager viewTagManager, final CTagManager nodeTagManager)
      throws CouldntLoadDataException {
    return loadModuleCallgraphs(provider, module, viewTagManager, nodeTagManager,
        ViewType.NonNative);
  }

  /**
   * Loads the native call graph view of a module.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param module The module from where the views are loaded.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return The native call graph view.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static ICallgraphView loadNativeCallgraph(final AbstractSQLProvider provider,
      final CModule module, final CTagManager viewTagManager, final CTagManager nodeTagManager)
      throws CouldntLoadDataException {
    final List<ICallgraphView> views =
        loadModuleCallgraphs(provider, module, viewTagManager, nodeTagManager, ViewType.Native);

    if (views.size() != 1) {
      throw new CouldntLoadDataException("Error: Malformed project");
    }

    return views.get(0);
  }
}
