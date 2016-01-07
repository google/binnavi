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

import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


/**
 * Convenience class that serves as the base class of all module view loaders.
 */
public class PostgreSQLModuleViewsLoader extends PostgreSQLViewsLoader {
  /**
   * Do not instantiate this class.
   */
  protected PostgreSQLModuleViewsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads view tags from the database.
   * 
   * The module and the view tag manager must be stored in the database connected to by the provider
   * argument.
   * 
   * @param connection Connection to a database.
   * @param module The module whose view tags are loaded.
   * @param viewTagManager View tag manager that contains all tags of the database.
   * 
   * @return A mapping between view IDs and the tags the views are tagged with.
   * 
   * @throws SQLException Thrown if the tags could not be loaded.
   */
  protected static final Map<Integer, Set<CTag>> loadTags(final CConnection connection,
      final INaviModule module, final CTagManager viewTagManager) throws SQLException {

    return PostgreSQLViewTagLoader.loadViewTags(connection, CTableNames.MODULE_VIEWS_TABLE,
        "module_id", module.getConfiguration().getId(), viewTagManager);
  }
}
