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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ViewManager;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class provides PostgreSQL queries for loading the functions of a module.
 */
public final class PostgreSQLFunctionsLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLFunctionsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks arguments for validity.
   *
   * @param provider The provider argument to validate.
   * @param module The module argument to validate.
   */
  private static void checkArguments(final SQLProvider provider, final INaviModule module) {
    Preconditions.checkNotNull(provider, "IE00466: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00467: Module can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00468: Module is not part of this database");
  }

  /**
   * This function parses a {@link ResultSet result set} from a database query and generates a
   * {@link List} of {@link INaviFunction} from it.
   *
   * @param resultSet The {@link ResultSet} in which the result from the database query is stored.
   * @param provider The {@link SQLProvider} to access the database with.
   * @param module The {@link INaviModule} to which the {@link INaviFunction function(s)} are
   *        associated.
   *
   * @return A {@link List} of {@link INaviFunction functions} parsed from the {@link ResultSet
   *         resultSet}.
   *
   * @throws SQLException if the {@link ResultSet} could not be parsed.
   * @throws CouldntLoadDataException if the associated comments could not be loaded from the
   *         database.
   */
  private static List<INaviFunction> parseFunctionInformation(final ResultSet resultSet,
      final SQLProvider provider, final INaviModule module) throws SQLException,
      CouldntLoadDataException {

    final List<INaviFunction> functions = Lists.newArrayList();
    final Map<Integer, INaviFunction> commentIdToFunction = new HashMap<Integer, INaviFunction>();

    try {
      while (resultSet.next()) {
        final IAddress address = PostgreSQLHelpers.loadAddress(resultSet, "address");
        final String name = resultSet.getString("name");
        final String originalName = resultSet.getString("original_name");

        Integer globalCommentId = resultSet.getInt("global_comment");
        if (resultSet.wasNull()) {
          globalCommentId = null;
        }

        final String description = resultSet.getString("description");
        final FunctionType type = FunctionType.valueOf(resultSet.getString("type").toUpperCase());

        final String parentModuleName = resultSet.getString("parent_module_name");
        final int parentModuleId = resultSet.getInt("parent_module_id");

        final IAddress parentModuleFunction = resultSet.wasNull() ? null
            : PostgreSQLHelpers.loadAddress(resultSet, "parent_module_function");

        final Integer nodeCount = resultSet.getInt("bbcount");
        final Integer edgeCount = resultSet.getInt("edgeCount");
        final Integer indegree = resultSet.getInt("incount");
        final Integer outdegree = resultSet.getInt("outcount");
        Integer stackFrameId = resultSet.getInt("stack_frame");
        if (resultSet.wasNull()) {
          stackFrameId = null;
        }
        Integer prototypeId = resultSet.getInt("prototype");
        if (resultSet.wasNull()) {
          prototypeId = null;
        }
        final INaviView view = ViewManager.get(provider).getView(resultSet.getInt("view_id"));
        final BaseType stackFrame =
            stackFrameId == null ? null : module.getTypeManager().getBaseType(stackFrameId);
        if (stackFrameId != null) {
          module.getTypeManager().setStackFrame(stackFrame);
        }
        final BaseType prototype =
            prototypeId == null ? null : module.getTypeManager().getBaseType(prototypeId);

        final CFunction function = new CFunction(module,
            view,
            address,
            name,
            originalName,
            description,
            indegree,
            outdegree,
            nodeCount,
            edgeCount,
            type,
            parentModuleName,
            parentModuleId,
            parentModuleFunction,
            stackFrame,
            prototype,
            provider);

        if (globalCommentId != null) {
          commentIdToFunction.put(globalCommentId, function);
        }

        functions.add(function);
      }

    } finally {
      resultSet.close();
    }

    if (!commentIdToFunction.isEmpty()) {
      final HashMap<Integer, ArrayList<IComment>> commentIdToComments = PostgreSQLCommentFunctions
          .loadMultipleCommentsById(provider, commentIdToFunction.keySet());
      for (final Entry<Integer, ArrayList<IComment>> commentIdToComment :
          commentIdToComments.entrySet()) {
        CommentManager.get(provider).initializeGlobalFunctionComment(
            commentIdToFunction.get(commentIdToComment.getKey()), commentIdToComment.getValue());
      }
    }

    return functions;
  }

  /**
   * This function loads a {@link INaviFunction} from the database given the {@link INaviModule}
   * where it is associated to and the {@link IAddress} of the function.
   *
   * @param provider The {@link SQLProvider} to access the database with.
   * @param module The {@link INaviModule} in which the function is located.
   * @param address The {@link IAddress} of the function to load.
   *
   * @return A {@link INaviFunction} if found.
   *
   * @throws CouldntLoadDataException if the function information could not be loaded from the
   *         database.
   */
  public static INaviFunction loadFunction(final SQLProvider provider, final INaviModule module,
      final IAddress address) throws CouldntLoadDataException {
    checkArguments(provider, module);
    final String query = " SELECT * FROM load_function_information(?,?) ";

    try {
      final PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query);
      statement.setInt(1, module.getConfiguration().getId());
      statement.setObject(2, address.toBigInteger(), Types.BIGINT);
      final ResultSet resultSet = statement.executeQuery();
      return Iterables.getFirst(parseFunctionInformation(resultSet, provider, module), null);
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Loads the functions of a module.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module whose functions are loaded.
   * @param views The views that potentially back the function.
   *
   * @return A list of functions that belong to the module.
   *
   * @throws CouldntLoadDataException Thrown if the functions of the module could not be loaded.
   */
  public static List<INaviFunction> loadFunctions(final SQLProvider provider,
      final INaviModule module, final List<IFlowgraphView> views) throws CouldntLoadDataException {
    checkArguments(provider, module);
    final String query = " SELECT * FROM load_function_information(?) ";

    try {
      final PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query);
      statement.setInt(1, module.getConfiguration().getId());
      final ResultSet resultSet = statement.executeQuery();
      return parseFunctionInformation(resultSet, provider, module);
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }
}
