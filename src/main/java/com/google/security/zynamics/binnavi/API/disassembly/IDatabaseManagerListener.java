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
package com.google.security.zynamics.binnavi.API.disassembly;

// / Used to listen on database managers.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link DatabaseManager} objects.
 */
public interface IDatabaseManagerListener {
  // ! Signals a new database configuration.
  /**
   * Invoked after a new database configuration was added to a database manager.
   *
   * @param manager The database manager where the new database configuration was added.
   * @param database The new database configuration that was added to the database manager.
   */
  void addedDatabase(DatabaseManager manager, Database database);

  // ! Signals the deletion of a database configuration.
  /**
   * Invoked after a database configuration was removed from the database manager.
   *
   * @param databaseManager The database manager the database was removed from.
   * @param removedDatabase The database configuration that was removed from the database manager.
   */
  void removedDatabase(DatabaseManager databaseManager, Database removedDatabase);
}
