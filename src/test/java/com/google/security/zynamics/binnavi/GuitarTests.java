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

import com.google.common.flags.Flags;
import com.google.common.logging.LogConfig;
import com.google.net.borg_bridge.ProxyLocator;
import com.google.net.borg_bridge.VirtualSocketImplFactory;
import com.google.security.zynamics.binnavi.Log.NaviLogger;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Suite class for tests run with the Guitar test suite. These tests are all integration tests which
 * need a database.
 */
@RunWith(Suite.class)
@SuiteClasses({com.google.security.zynamics.binnavi.models.Bookmarks.memory.AllTests.class,
    com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLConvertTests.class,
    com.google.security.zynamics.binnavi.disassembly.ExpensiveTests.class,
    com.google.security.zynamics.binnavi.ZyGraph.ExpensiveTests.class,
    com.google.security.zynamics.binnavi.REIL.ExpensiveTests.class,
    com.google.security.zynamics.binnavi.Database.PostgreSQLAllTests.class})
public final class GuitarTests {
  static {
    // Need to disable assertions for this package, otherwise tests fail.
    // TODO(cblichmann): Fix the overuse of "assert false" in the codebase.
    GuitarTests.class.getClassLoader()
        .setPackageAssertionStatus("com.google.security.zynamics.binnavi", false);
  }

  @BeforeClass
  public static void initializeDatabase() {
    // Periscope function overloading must happen before flags
    // processing. This finishes up the setup of overloading all
    // socket classes.
    try {
      VirtualSocketImplFactory.initializePeriscopeOpenJdk();
    } catch (final IOException ex) {
      return;
    }
    final String[] args =
        {"--periscope_gslb_service=periscope2corp-proxy-locator", "--periscope_prod2corp=true"};
    Flags.parse(args);
    // Periscope proxies need to be found by this binary. This
    // is done via GSLB-balanced RPC's. The configuration for GSLB requires
    // Flags to be set. This is why we must fire up the locator here.
    ProxyLocator.getProxyLocator().initialize();
    LogConfig.specifyFromProgram("FINE", null);
    NaviLogger.setLevel(Level.INFO);
    try {
      final IntegrationTestSetup setup = new IntegrationTestSetup();
      setup.createIntegrationTestDatabase();
    } catch (final SQLException e) {
      e.printStackTrace();
    } 
  }
}
