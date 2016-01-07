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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

public final class PostgreSQLTagManagerFunctions {
  /**
   * You are not supposed to instantiate the class.
   */
  private PostgreSQLTagManagerFunctions() {
    // You are not supposed to instantiate the class.
  }

  /**
   * Loads a tag manager from the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param type Type of the tags managed by the tag manager.
   * 
   * @return The tag manager that is loaded from the database.
   * 
   * @throws CouldntLoadDataException Thrown if the tag manager could not be loaded.
   */
  public static CTagManager loadTagManager(final AbstractSQLProvider provider, final TagType type)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(type, "IE00567: Tag type argument can't be null");

    final CConnection connection = provider.getConnection();

    // should always have a tags table
    if (!PostgreSQLHelpers.hasTable(connection, CTableNames.TAGS_TABLE)) {
      final CTag rootTag = new CTag(0, "Root Node", "", type, provider);

      return new CTagManager(new Tree<CTag>(new TreeNode<CTag>(rootTag)), type, provider);
    }

    final String query =
        String.format("select id, parent_id, name, description from %s where type = '%s'",
            CTableNames.TAGS_TABLE, PostgreSQLTagFunctions.tagToString(type));

    try {
      final ResultSet resultSet = connection.executeQuery(query, true);

      try {
        final HashMap<Integer, Pair<TreeNode<CTag>, Integer>> treeNodes =
            new HashMap<Integer, Pair<TreeNode<CTag>, Integer>>();

        final CTag rootTag = new CTag(0, "Root Node", "", type, provider);

        final TreeNode<CTag> rootTreeNode = new TreeNode<CTag>(rootTag);

        treeNodes.put(0, new Pair<TreeNode<CTag>, Integer>(rootTreeNode, -1));

        while (resultSet.next()) {
          final int tagId = resultSet.getInt("id");
          final int parentId = resultSet.getInt("parent_id");
          final TreeNode<CTag> treeNode =
              new TreeNode<CTag>(new CTag(tagId, PostgreSQLHelpers.readString(resultSet, "name"),
                  PostgreSQLHelpers.readString(resultSet, "description"), type, provider));
          final Pair<TreeNode<CTag>, Integer> pair =
              new Pair<TreeNode<CTag>, Integer>(treeNode, parentId);

          treeNodes.put(tagId, pair);
        }

        for (final Entry<Integer, Pair<TreeNode<CTag>, Integer>> e : treeNodes.entrySet()) {
          if (e.getKey() == 0) {
            continue;
          }

          final TreeNode<CTag> child = e.getValue().first();
          final TreeNode<CTag> parent = treeNodes.get(e.getValue().second()).first();
          child.setParent(parent);
          parent.addChild(child);
        }

        return new CTagManager(new Tree<CTag>(rootTreeNode), type, provider);
      } finally {
        resultSet.close();
      }

    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }

  }

}
