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
 * Database manager objects are used to keep track of known databases that can be used with com.google.security.zynamics.binnavi.
 */
public interface IDatabaseManager extends Iterable<IDatabase> {
  /**
   * Adds a database to the list of known databases.
   * 
   * @param database The database to add to the list of known databases.
   * 
   * @return The added database.
   * 
   * @throws IllegalArgumentException Thrown if the value null is passed to the function.
   * @throws IllegalStateException Thrown if the database was already added to the database manager
   *         earlier.
   */
  IDatabase addDatabase(final IDatabase database);

  /**
   * Adds a new database manager listener to the list of listeners that are notified about changes
   * in the database manager.
   * 
   * @param listener The new listener.
   */
  void addListener(final DatabaseManagerListener listener);

  /**
   * Moves a known database to a new index. This can be used to reorder the known databases.
   * 
   * @param database The database to move.
   * @param index The new index of the database.
   * 
   * @throws IllegalArgumentException Thrown if the database argument is invalid or the database is
   *         not known to the database manager.
   * @throws IndexOutOfBoundsException Thrown if the new index is invalid.
   */
  void moveDatabase(final IDatabase database, final int index);

  /**
   * Removes a database from the list of databases known to the database manager.
   * 
   * @param database The database to remove from the database manager.
   * 
   * @throws IllegalArgumentException Thrown if the given database is null or it is not known to the
   *         database manager.
   */
  void removeDatabase(final IDatabase database);

  /**
   * Removes a database manager listener from this database manager. The removed listener does not
   * receive further notifications about events that occur in this database manager.
   * 
   * @param listener The listener to remove.
   */
  void removeListener(final DatabaseManagerListener listener);
}
