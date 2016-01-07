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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.CProjectViewGenerator;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ViewType;

// ESCA-JAVA0014:
/**
 * Base class for all view loaders. Provides a few convenience functions that are required for
 * loading views from projects and modules.
 */
public class PostgreSQLViewsLoader {
  /**
   * Validates arguments.
   * 
   * @param provider Provider argument to validate.
   * @param module Module argument to validate.
   * @param flowgraphs Flowgraphs argument to validate.
   * @param functions Functions argument to validate.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final CModule module,
      final List<IFlowgraphView> flowgraphs, final List<INaviFunction> functions) {

    Preconditions.checkNotNull(provider, "IE00630: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00631: Module argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00632: Module is not part of this database");
    Preconditions.checkNotNull(flowgraphs, "IE00633: Flowgraphs argument can not be null");

    for (final IFlowgraphView view : flowgraphs) {
      Preconditions.checkNotNull(view, "IE00634: View list contains a null-element");
      Preconditions.checkArgument(view.inSameDatabase(provider),
          "IE00635: View is not part of this database");
    }

    Preconditions.checkNotNull(functions, "IE00636: Functions argument can not be null");

    for (final INaviFunction function : functions) {
      Preconditions.checkNotNull(function, "IE00637: Function list contains a null-element");
      Preconditions.checkArgument(function.inSameDatabase(provider),
          "IE00638: Function is not part of this database");
    }
  }

  /**
   * Loads the node tags of the views of a module.
   * 
   * @param connection Provides the connection to the database.
   * @param module The module whose node-tagged views are determined.
   * @param nodeTagManager Provides the available node tags.
   * 
   * @return Maps view ID -> node tags used in the view.
   * 
   * @throws SQLException Thrown if the data could not be loaded.
   */
  private static Map<Integer, Set<CTag>> getNodeTags(final CConnection connection,
      final INaviModule module, final ITagManager nodeTagManager) throws SQLException {
    final Map<Integer, Set<CTag>> tagMap = new HashMap<Integer, Set<CTag>>();

    final String query = "SELECT * FROM load_module_node_tags(?)";
    final PreparedStatement statement = connection.getConnection().prepareStatement(query);
    statement.setInt(1, module.getConfiguration().getId());
    final ResultSet resultSet = statement.executeQuery();

    try {
      while (resultSet.next()) {
        final int viewId = resultSet.getInt(1);
        final int tagId = resultSet.getInt(2);

        if (!tagMap.containsKey(viewId)) {
          tagMap.put(viewId, new HashSet<CTag>());
        }

        final CTag tag = CTagHelpers.findTag(nodeTagManager.getRootTag(), tagId);

        if (tag != null) {
          tagMap.get(viewId).add(tag);
        }
      }

    } finally {
      resultSet.close();
    }

    return tagMap;
  }

  /**
   * Loads the node tags of the views of a project.
   * 
   * @param connection Provides the connection to the database.
   * @param project The project whose node-tagged views are determined.
   * @param nodeTagManager Provides the available node tags.
   * 
   * @return Maps view ID -> node tags used in the view.
   * 
   * @throws SQLException Thrown if the data could not be loaded.
   */
  private static Map<Integer, Set<CTag>> getNodeTags(final CConnection connection,
      final INaviProject project, final ITagManager nodeTagManager) throws SQLException {
    final Map<Integer, Set<CTag>> tagMap = new HashMap<Integer, Set<CTag>>();

    final String query = " SELECT * FROM load_project_node_tags(?) ";
    final PreparedStatement statement = connection.getConnection().prepareStatement(query);
    statement.setInt(1, project.getConfiguration().getId());
    final ResultSet resultSet = statement.executeQuery();

    try {
      while (resultSet.next()) {
        final int viewId = resultSet.getInt("view_id");
        final int tagId = resultSet.getInt("tag_id");

        if (!tagMap.containsKey(viewId)) {
          tagMap.put(viewId, new HashSet<CTag>());
        }

        final CTag tag = CTagHelpers.findTag(nodeTagManager.getRootTag(), tagId);

        if (tag != null) {
          tagMap.get(viewId).add(tag);
        }
      }
    } finally {
      resultSet.close();
    }

    return tagMap;
  }

  /**
   * Checks the validity of a given SQL provider and a given module. If there is a problem with the
   * arguments, an exception is thrown.
   * 
   * @param provider The SQL provider to check.
   * @param module The module to check.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   */
  protected static final void checkArguments(final SQLProvider provider, final CModule module,
      final CTagManager viewTagManager) {
    checkArguments(provider, viewTagManager);
    Preconditions.checkNotNull(module, "IE00497: Module argument can't be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00498: Module is not part of this database");
  }

  /**
   * Validates arguments.
   * 
   * @param provider Provider argument to validate.
   * @param viewTagManager View tag manager argument to validate.
   */
  protected static final void checkArguments(final SQLProvider provider,
      final CTagManager viewTagManager) {
    Preconditions.checkNotNull(provider, "IE00639: Provider argument can not be null");
    Preconditions.checkNotNull(viewTagManager, "IE00640: Tag manager argument can not be null");
    Preconditions.checkArgument(viewTagManager.inSameDatabase(provider),
        "IE00641: Tag manager is not part of this database");
  }

  /**
   * Processes the results of a view loading query.
   * 
   * @param resultSet Contains the results of the SQL query.
   * @param module The module the views were loaded for.
   * @param tags Map that contains the tags the views are tagged with.
   * @param nodeTagManager Provides the node tags.
   * @param provider The connection to the database.
   * @param views The loaded views are stored in this list.
   * @param viewType View type of the loaded views.
   * @param graphType Graph type of the loaded views.
   * 
   * @return The loaded views.
   * 
   * @throws SQLException Thrown if the views could not be loaded.
   */
  protected static final List<CView> processQueryResults(final ResultSet resultSet,
      final INaviModule module, final Map<Integer, Set<CTag>> tags,
      final ITagManager nodeTagManager, final SQLProvider provider, final List<CView> views,
      final ViewType viewType, final GraphType graphType) throws SQLException {
    final Map<Integer, Set<CTag>> nodeTagMap =
        getNodeTags(provider.getConnection(), module, nodeTagManager);

    try {
      while (resultSet.next()) {
        final int viewId = resultSet.getInt("view_id");
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String description = PostgreSQLHelpers.readString(resultSet, "description");
        final Timestamp creationDate = resultSet.getTimestamp("creation_date");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");
        final boolean starState = resultSet.getBoolean("stared");
        final int nodeCount = resultSet.getInt("bbcount");
        final int edgeCount = resultSet.getInt("edgecount");

        final Set<CTag> viewTags =
            tags.containsKey(viewId) ? tags.get(viewId) : new HashSet<CTag>();
        final Set<CTag> nodeTags =
            nodeTagMap.containsKey(viewId) ? nodeTagMap.get(viewId) : new HashSet<CTag>();

        final CModuleViewGenerator generator = new CModuleViewGenerator(provider, module);
        views.add(generator.generate(viewId, name, description, viewType, graphType, creationDate,
            modificationDate, nodeCount, edgeCount, viewTags, nodeTags, starState));
      }

      return views;
    } finally {
      resultSet.close();
    }
  }

  /**
   * Processes the results of a view loading query.
   * 
   * @param resultSet Contains the results of the SQL query.
   * @param project The project the views were loaded for.
   * @param tags Map that contains the tags the views are tagged with.
   * @param nodeTagManager Provides the node tags.
   * @param provider The connection to the database.
   * @param views The loaded views are stored in this list.
   * @param viewType View type of the loaded views.
   * @param graphType Graph type of the loaded views.
   * 
   * @return The loaded views.
   * 
   * @throws SQLException Thrown if the views could not be loaded.
   */
  protected static final List<CView> processQueryResults(final ResultSet resultSet,
      final INaviProject project, final Map<Integer, Set<CTag>> tags,
      final ITagManager nodeTagManager, final SQLProvider provider, final List<CView> views,
      final ViewType viewType, final GraphType graphType) throws SQLException {
    final Map<Integer, Set<CTag>> nodeTagMap =
        getNodeTags(provider.getConnection(), project, nodeTagManager);

    try {
      while (resultSet.next()) {
        final int viewId = resultSet.getInt("view_id");
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String description = PostgreSQLHelpers.readString(resultSet, "description");
        final Timestamp creationDate = resultSet.getTimestamp("creation_date");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");
        final boolean starState = resultSet.getBoolean("stared");
        final int nodeCount = resultSet.getInt("bbcount");
        final int edgeCount = resultSet.getInt("edgecount");

        final Set<CTag> viewTags =
            tags.containsKey(viewId) ? tags.get(viewId) : new HashSet<CTag>();
        final Set<CTag> nodeTags =
            nodeTagMap.containsKey(viewId) ? nodeTagMap.get(viewId) : new HashSet<CTag>();

        final CProjectViewGenerator generator = new CProjectViewGenerator(provider, project);
        views.add(generator.generate(viewId, name, description, viewType, graphType, creationDate,
            modificationDate, nodeCount, edgeCount, viewTags, nodeTags, starState));
      }

      return views;
    } finally {
      resultSet.close();
    }
  }

  /**
   * Loads the view -> function mapping from the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param flowgraphs List of all native Flow graph views of a module.
   * @param functions List of all functions of a module.
   * @param module The module from which to load the mapping.
   * 
   * @return A view -> function mapping and a function -> view mapping.
   * 
   * @throws CouldntLoadDataException Thrown if the mapping could not be loaded.
   */
  public static final ImmutableBiMap<INaviView, INaviFunction> loadViewFunctionMapping(
      final AbstractSQLProvider provider, final List<IFlowgraphView> flowgraphs,
      final List<INaviFunction> functions, final CModule module) throws CouldntLoadDataException {
    checkArguments(provider, module, flowgraphs, functions);

    final HashMap<Integer, INaviView> viewmap = new HashMap<Integer, INaviView>();

    for (final IFlowgraphView view : flowgraphs) {
      viewmap.put(view.getConfiguration().getId(), view);
    }

    final HashMap<IAddress, INaviFunction> functionMap = new HashMap<IAddress, INaviFunction>();

    for (final INaviFunction function : functions) {
      functionMap.put(function.getAddress(), function);
    }

    final CConnection connection = provider.getConnection();

    final String query =
        "SELECT view_id, function FROM " + CTableNames.FUNCTION_VIEWS_TABLE + " WHERE module_id = "
            + module.getConfiguration().getId();

    final HashMap<INaviView, INaviFunction> viewFunctionMap =
        new HashMap<INaviView, INaviFunction>();

    try {
      final ResultSet resultSet = connection.executeQuery(query, true);
      try {
        while (resultSet.next()) {
          final INaviView view = viewmap.get(resultSet.getInt("view_id"));
          final INaviFunction function =
              functionMap.get(PostgreSQLHelpers.loadAddress(resultSet, "function"));

          if ((view != null) && (function != null)) {
            viewFunctionMap.put(view, function);
          }
        }
      } finally {
        resultSet.close();
      }

      return new ImmutableBiMap.Builder<INaviView, INaviFunction>().putAll(viewFunctionMap).build();

    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }
}
