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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.CProjectViewGenerator;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.ViewGenerator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Savers.PostgreSQLEdgeSaver;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Savers.PostgreSQLNodeSaver;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;

public final class PostgreSQLViewCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLViewCreator() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Inserts a new view in the database by copying an existing view.
   * 
   * @param provider The connection to the database.
   * @param containerId The ID of the container where the view is created.
   * @param view The view to be copied.
   * @param name The name of the new view.
   * @param description The description of the new view.
   * @param containerTable Name of the view container table.
   * @param viewContainerTable Name of the view container views table.
   * @param generator Generates the view.
   * @return The created view.
   * @throws CouldntSaveDataException Thrown if the view could not be created.
   */
  private static CView createView(final AbstractSQLProvider provider, final int containerId,
      final INaviView view, final String name, final String description,
      final String containerTable, final String viewContainerTable, final ViewGenerator generator)
      throws CouldntSaveDataException {
    final CConnection connection = provider.getConnection();

    try {
      PostgreSQLHelpers.beginTransaction(connection);

      final int viewId = insertView(connection, name, description);

      // Mark the view as a module view
      connection.executeUpdate("INSERT INTO " + viewContainerTable + " VALUES(" + containerId
          + ", " + viewId + ")", true);

      final List<INaviViewNode> nodes = view.getGraph().getNodes();
      final List<INaviEdge> edges = view.getGraph().getEdges();

      // Store all nodes
      PostgreSQLNodeSaver.writeNodes(provider, viewId, nodes);

      // Store all edges
      PostgreSQLEdgeSaver.writeEdges(provider, edges);

      PostgreSQLHelpers.endTransaction(connection);

      final String query =
          "SELECT creation_date, modification_date FROM " + CTableNames.VIEWS_TABLE
              + " WHERE id = " + viewId;

      final ResultSet resultSet = connection.executeQuery(query, true);

      try {
        while (resultSet.next()) {
          final Timestamp creationDate = resultSet.getTimestamp("creation_date");
          final Timestamp modificationDate = resultSet.getTimestamp("modification_date");

          PostgreSQLHelpers.updateModificationDate(connection, containerTable, containerId);

          return generator.generate(viewId, name, description, ViewType.NonNative,
              view.getGraphType(), creationDate, modificationDate, view.getNodeCount(),
              view.getEdgeCount(), new HashSet<CTag>(), new HashSet<CTag>(), false);
        }

        throw new CouldntSaveDataException("Error: Couldnt't load the created view");
      } finally {
        resultSet.close();
      }
    } catch (final SQLException exception) {
      CUtilityFunctions.logException(exception);

      try {
        PostgreSQLHelpers.rollback(connection);
      } catch (final SQLException e) {
        CUtilityFunctions.logException(e);
      }

      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Inserts a new view in the database by copying an existing view.
   * 
   * @param connection The connection to the database.
   * @param name The name of the new view.
   * @param description The description of the new view.
   * @return The id of the created view
   * @throws SQLException if the view could not be created
   */
  private static int insertView(final CConnection connection, final String name,
      final String description) throws SQLException {

    final String query =
        "INSERT INTO " + CTableNames.VIEWS_TABLE
            + "(type, name, description, creation_date, modification_date) "
            + " VALUES(?::view_type, ?, ?, NOW(), NOW()) RETURNING id";

    final PreparedStatement statement =
        connection.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY);

    try {
      statement.setString(1, "non-native");
      statement.setString(2, name);
      if (description == null) {
        statement.setNull(3, Types.VARCHAR);
      } else {
        statement.setString(3, description);
      }
      int lastId = 0;
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          if (resultSet.isFirst()) {
            lastId = resultSet.getInt(1);
            break;
          }
        }

        if (!resultSet.isFirst()) {
          throw new IllegalStateException(
              "IE02071: Unable to determine last id after SQL insertion.");
        }
      } finally {
        resultSet.close();
      }
      return lastId;
    } finally {
      statement.close();
    }
  }

  /**
   * Creates a new view by copying an existing view.
   * 
   * @param provider The connection to the database.
   * @param module The module the view is added to.
   * @param view The view to be copied.
   * @param name The name of the new view.
   * @param description The description of the new view.
   * @return The new view.
   * @throws CouldntSaveDataException Thrown if the view could not be saved.
   */
  public static CView createView(final AbstractSQLProvider provider, final INaviModule module,
      final INaviView view, final String name, final String description)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE02268: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE02269: Module argument can not be null");
    Preconditions.checkNotNull(view, "IE02270: View argument can not be null");
    Preconditions.checkNotNull(name, "IE02271: Name argument can not be null");
    Preconditions.checkState(module.inSameDatabase(provider),
        "Error: Module is not part of this database");
    Preconditions.checkState(view.inSameDatabase(provider),
        "Error: View is not part of this database");

    return createView(provider, module.getConfiguration().getId(), view, name, description, ""
        + CTableNames.MODULES_TABLE + "", "" + CTableNames.MODULE_VIEWS_TABLE + "",
        new CModuleViewGenerator(provider, module));
  }

  /**
   * Creates a new view by copying an existing view.
   * 
   * @param provider The connection to the database.
   * @param project The project the new view is added to.
   * @param view The view to be copied.
   * @param name The name of the new view.
   * @param description The description of the new view.
   * @return The created view.
   * @throws CouldntSaveDataException Thrown if the view could not be saved.
   */
  public static CView createView(final AbstractSQLProvider provider, final INaviProject project,
      final INaviView view, final String name, final String description)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(project, "Error: Project argument can not be null");
    Preconditions.checkNotNull(view, "IE02273: View argument can not be null");
    Preconditions.checkNotNull(name, "IE02274: Name argument can not be null");
    Preconditions.checkState(view.inSameDatabase(provider),
        "Error: View is not part of this database");

    return createView(provider, project.getConfiguration().getId(), view, name, description,
        CTableNames.PROJECTS_TABLE, CTableNames.PROJECT_VIEWS_TABLE, new CProjectViewGenerator(
            provider, project));
  }
}
