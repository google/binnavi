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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;


public final class PostgreSQLViewTagLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLViewTagLoader() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Loads view tags from the database.
   * 
   * @param connection Connection to a SQL database.
   * @param tableName Table name from where the tags are loaded.
   * @param containerColumn Column that identifies the tag container.
   * @param containerId ID of the tag container.
   * @param viewTagManager View tag manager that contains all tags of the database.
   * 
   * @return A pair of view IDs and the tags the views are tagged with.
   * 
   * @throws SQLException Thrown if the tags could not be loaded.
   */
  public static Map<Integer, Set<CTag>> loadViewTags(final CConnection connection,
      final String tableName, final String containerColumn, final int containerId,
      final CTagManager viewTagManager) throws SQLException {
    final Map<Integer, Set<CTag>> setTag = new HashMap<Integer, Set<CTag>>();

    final String query =
        "SELECT " + CTableNames.TAGGED_VIEWS_TABLE + ".view_id, tag_id" + " FROM "
            + CTableNames.TAGGED_VIEWS_TABLE + " JOIN " + tableName + " ON " + tableName
            + ".view_id = " + CTableNames.TAGGED_VIEWS_TABLE + ".view_id" + " WHERE "
            + containerColumn + " = " + containerId + " ORDER BY view_id";

    final ResultSet resultSet = connection.executeQuery(query, true);

    try {
      int currentView = 0;
      Set<CTag> currentTags = new HashSet<CTag>();

      while (resultSet.next()) {
        final int view = resultSet.getInt("view_id");
        final int tagId = resultSet.getInt("tag_id");

        if (currentView == 0) {
          currentView = view;
        }

        if (currentView != view) {
          setTag.put(currentView, currentTags);

          currentTags = new HashSet<CTag>();
          currentView = view;
        }

        currentTags.add(CTagHelpers.findTag(viewTagManager.getRootTag(), tagId));
      }

      if (!currentTags.isEmpty()) {
        setTag.put(currentView, currentTags);
      }
    } finally {
      resultSet.close();
    }

    return setTag;
  }
}
