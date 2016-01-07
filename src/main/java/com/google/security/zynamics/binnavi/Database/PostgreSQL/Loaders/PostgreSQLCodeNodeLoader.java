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
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.NodeParser.CCodeNodeParser;
import com.google.security.zynamics.binnavi.Database.NodeParser.ParserException;
import com.google.security.zynamics.binnavi.Database.NodeParser.SqlCodeNodeProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Contains code for loading code nodes.
 */
public final class PostgreSQLCodeNodeLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLCodeNodeLoader() {
  }

  /**
   * Loads the code nodes of a view.
   * 
   * @param provider The connection to the database.
   * @param view The view whose code nodes are loaded.
   * @param nodes The loaded nodes are stored here.
   * @param modules All modules of the database.
   * 
   * @throws SQLException Thrown of loading the nodes failed.
   * @throws CPartialLoadException Thrown if loading the nodes failed because a necessary module was
   *         not loaded.
   */
  public static void load(final AbstractSQLProvider provider, final INaviView view,
      final List<INaviViewNode> nodes, final List<? extends INaviModule> modules)
      throws SQLException, CPartialLoadException {

    Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
    Preconditions.checkNotNull(view, "Error: view argument can not be null");
    Preconditions.checkNotNull(nodes, "Error: nodes argument can not be null");
    Preconditions.checkNotNull(modules, "Error: modules argument can not be null");

    final String query = " SELECT * FROM load_code_nodes(?) ";
    final PreparedStatement statement =
        provider.getConnection().getConnection()
            .prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    statement.setInt(1, view.getConfiguration().getId());
    final ResultSet resultSet = statement.executeQuery();

    try {
      final CCodeNodeParser parser =
          new CCodeNodeParser(new SqlCodeNodeProvider(resultSet), modules, provider);
      nodes.addAll(parser.parse());
    } catch (final ParserException e) {
      CUtilityFunctions.logException(e);
    } finally {
      resultSet.close();
    }
  }
}
