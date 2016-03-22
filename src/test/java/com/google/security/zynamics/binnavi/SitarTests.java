/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi;

import com.google.security.zynamics.binnavi.Log.NaviLogger;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;


@RunWith(Suite.class)
@SuiteClasses({com.google.security.zynamics.binnavi.models.Bookmarks.memory.AllTests.class,
  com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLConvertTests.class,
  com.google.security.zynamics.binnavi.disassembly.ExpensiveTests.class,
  com.google.security.zynamics.binnavi.ZyGraph.ExpensiveTests.class,
  com.google.security.zynamics.binnavi.REIL.ExpensiveTests.class,
  com.google.security.zynamics.binnavi.Database.PostgreSQLAllTests.class})
public final class SitarTests {
  static {
    // Need to disable assertions for this package, otherwise tests fail.
    // TODO(cblichmann): Fix the overuse of "assert false" in the codebase.
    SitarTests.class.getClassLoader()
        .setPackageAssertionStatus("com.google.security.zynamics.binnavi", false);
  }

  @BeforeClass
  public static void initializeDatabase() {
    NaviLogger.setLevel(Level.INFO);
    try {
      final IntegrationTestSetup setup = new IntegrationTestSetup();
      setup.createIntegrationTestDatabase();
    } catch (final SQLException | IOException e) {
      e.printStackTrace();
    } 
  }
}

