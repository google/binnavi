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
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Convenience class that serves as the base class of all project view loaders.
 */
public class PostgreSQLProjectViewsLoader extends PostgreSQLViewsLoader {
  /**
   * Do not instantiate this class.
   */
  protected PostgreSQLProjectViewsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks the validity of a given SQL provider and a given project. If there is a problem with the
   * arguments, an exception is thrown.
   * 
   * @param provider The SQL provider to check.
   * @param project The project to check.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   */
  protected static final void checkArguments(final SQLProvider provider,
      final INaviProject project, final CTagManager viewTagManager) {
    checkArguments(provider, viewTagManager);
    Preconditions.checkNotNull(project, "IE00644: Module argument can't be null");
    Preconditions.checkArgument(project.inSameDatabase(provider),
        "IE00528: Project is not part of this database");
  }

  /**
   * Loads view tags from the database.
   * 
   * @param connection Connection to a PostgreSQL database.
   * @param project The project whose view tags are loaded.
   * @param viewTagManager View tag manager that contains all tags of the database.
   * 
   * @return A pair of view IDs and the tags the views are tagged with.
   * 
   * @throws SQLException Thrown if the tags could not be loaded.
   */
  protected static final Map<Integer, Set<CTag>> loadTags(final CConnection connection,
      final INaviProject project, final CTagManager viewTagManager) throws SQLException {
    return PostgreSQLViewTagLoader.loadViewTags(connection, CTableNames.PROJECT_VIEWS_TABLE,
        "project_id", project.getConfiguration().getId(), viewTagManager);
  }
}
