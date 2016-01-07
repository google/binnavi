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
package com.google.security.zynamics.binnavi.Database;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseLoadProgressReporter;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.zylib.general.Pair;

import java.sql.SQLException;



/**
 * Creates connections to BinNavi databases.
 */
public final class CDatabaseConnection {
  /**
   * You are not supposed to instantiate this class.
   */
  private CDatabaseConnection() {
  }

  /**
   * Reports progress to the user.
   * 
   * @param reporter Notifies the user about events during database loading.
   * @param event The event to report to the user.
   * 
   * @throws LoadCancelledException Thrown if the user canceled loading manually.
   */
  private static void reportProgress(final IDatabaseLoadProgressReporter<LoadEvents> reporter,
      final LoadEvents event) throws LoadCancelledException {
    if (!reporter.report(event)) {
      throw new LoadCancelledException();
    }
  }

  public static Pair<CConnection, SQLProvider> connect(
      final CDatabaseConfiguration m_databaseConfiguration,
      final IDatabaseLoadProgressReporter<LoadEvents> reporter) throws CouldntLoadDriverException,
      CouldntConnectException, CouldntInitializeDatabaseException, InvalidDatabaseException,
      InvalidExporterDatabaseFormatException, LoadCancelledException {
    try {
      reportProgress(reporter, LoadEvents.CONNECTING_TO_DATABASE);

      final CConnection connection = new CConnection(m_databaseConfiguration);
      final AbstractSQLProvider sql = new PostgreSQLProvider(connection);

      reportProgress(reporter, LoadEvents.CHECKING_EXPORTER_TABLE_FORMAT);

      if (!sql.isExporterDatabaseFormatValid()) {
        throw new InvalidExporterDatabaseFormatException(
            "E00202: Database exporter format is not compatible with "
                + Constants.PROJECT_NAME_VERSION);
      }

      reportProgress(reporter, LoadEvents.CHECKING_INITIALIZATION_STATUS);

      final DatabaseVersion databaseVersion = sql.getDatabaseVersion();
      final DatabaseVersion currentVersion = new DatabaseVersion(Constants.PROJECT_VERSION);

      if ((databaseVersion.compareTo(currentVersion) == 0) && !sql.isInitialized()) {
        reportProgress(reporter, LoadEvents.INITIALIZING_DATABASE_TABLES);

        sql.initializeDatabase();
      }

      if ((databaseVersion.compareTo(currentVersion) == 0) && !sql.isInitialized()) {
        throw new CouldntInitializeDatabaseException("E00052: Database could not be initialized.");
      }

      return new Pair<CConnection, SQLProvider>(connection, sql);
    } catch (final CouldntLoadDataException e) {
      throw new CouldntConnectException(e, 0, "");
    } catch (final SQLException e) {
      throw new CouldntConnectException(e, e.getErrorCode(), e.getSQLState());
    }
  }
}
