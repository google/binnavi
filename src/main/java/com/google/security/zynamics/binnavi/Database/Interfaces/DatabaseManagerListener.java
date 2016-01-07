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
package com.google.security.zynamics.binnavi.Database.Interfaces;


/**
 * This interface can be implemented by objects that want to be notified about changes in database
 * managers.
 */
public interface DatabaseManagerListener {
  /**
   * This method is called when a new database was added to the database manager.
   * 
   * @param databaseManager The database manager the database was added to.
   * @param database The database that was added.
   */
  void addedDatabase(IDatabaseManager databaseManager, IDatabase database);

  /**
   * This method is called when a known database was removed from the database manager.
   * 
   * @param databaseManager The database manager where the database was removed.
   * @param database The database that was removed.
   */
  void removedDatabase(IDatabaseManager databaseManager, IDatabase database);

  /**
   * This method is called when the position of a known database changed inside the database
   * manager.
   * 
   * @param databaseManager The database manager where the position change happened.
   * @param database The database whose position changed.
   * @param index The new position of the database in the database manager.
   */
  void reorderedDatabases(IDatabaseManager databaseManager, IDatabase database, int index);
}
