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

import com.google.security.zynamics.binnavi.Database.CDatabaseConfiguration;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntUpdateDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for all database objects.
 */
public interface IDatabase extends IDatabaseObject {
  /**
   * Adds a listener object that is notified about changes in the database.
   * 
   * @param listener The listener object.
   */
  void addListener(IDatabaseListener listener);

  /**
   * Closes the connection to the database.
   * 
   * @return True, if the database was closed. False, if the database was vetoed.
   * 
   * @throws IllegalStateException Thrown if the database is not connected.
   */
  boolean close();

  /**
   * Connects to the database.
   * 
   * This function is guaranteed to be thread-safe. If the connection to the database can not be
   * established, the state of the database object remains unchanged.
   * 
   * @throws IllegalStateException Thrown if the database is already connected.
   * @throws CouldntLoadDriverException Thrown if the specified database driver could not be loaded.
   * @throws CouldntConnectException Thrown if the connection to the database could not be
   *         established.
   * @throws InvalidDatabaseException Thrown if the database is in an invalid state.
   * @throws CouldntInitializeDatabaseException Thrown if the database could not be initialized.
   * @throws InvalidExporterDatabaseFormatException Thrown if the exporter tables are in an unknown
   *         state.
   * @throws LoadCancelledException Thrown if the user cancelled loading manually.
   * @throws InvalidDatabaseVersionException
   */
  void connect() throws CouldntLoadDriverException, CouldntConnectException,
      InvalidDatabaseException, CouldntInitializeDatabaseException,
      InvalidExporterDatabaseFormatException, LoadCancelledException,
      InvalidDatabaseVersionException;

  /**
   * Executes a query in the database.
   * 
   * @param query The query to execute.
   * 
   * @return The result of the query.
   * 
   * @throws SQLException Thrown if the query could not be executed.
   */
  ResultSet executeQuery(String query) throws SQLException;

  /**
   * Returns the database configuration object.
   * 
   * @return The database configuration object.
   */
  CDatabaseConfiguration getConfiguration();

  /**
   * Returns the database content.
   * 
   * @return The database content.
   */
  IDatabaseContent getContent();

  /**
   * Determines whether a connection to the database exists.
   * 
   * @return True, if a connection to the database exists. False, otherwise.
   */
  boolean isConnected();

  /**
   * Returns whether the database is currently connecting.
   * 
   * @return True, if the database is currently connecting. False, otherwise.
   */
  boolean isConnecting();

  /**
   * Determines whether the information from the database was loaded.
   * 
   * @return True, if the database was loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Returns whether the database is currently loading.
   * 
   * @return True, if the database is currently loading. False, otherwise.
   */
  boolean isLoading();

  /**
   * Loads the information about projects, modules, and raw modules from the database.
   * 
   * Note that the database must be connected. Otherwise an exception is thrown.
   * 
   * This function is guaranteed to be exception-safe. Should an exception occur, the state of the
   * database object is not modified.
   * 
   * @throws IllegalStateException Thrown if the database is not connected.
   * @throws CouldntLoadDataException Thrown if the data could not be loaded from the database.
   * @throws InvalidDatabaseVersionException Thrown if the BinNavi tables inside the database have
   *         an invalid version.
   * @throws LoadCancelledException Thrown if the user manually cancelled loading.
   */
  void load() throws CouldntLoadDataException, InvalidDatabaseVersionException,
      LoadCancelledException;

  /**
   * Removes a listening object from the list of objects that are notified about events in this
   * database.
   * 
   * @param listener The listening object that is removed.
   */
  void removeListener(IDatabaseListener listener);

  /**
   * Updates the database to the latest version.
   * 
   * @throws CouldntUpdateDatabaseException Thrown if the database could not be updated.
   */
  void update() throws CouldntUpdateDatabaseException;
}
