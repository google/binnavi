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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.List;

/**
 * Large functional test. This test verifies that all known good conditions about the test
 * executable hold.
 */
@RunWith(JUnit4.class)
public class PostgreSQLVerifyCalcTest {
  private CDatabase m_database;

  private static void verify(final INaviModule module) throws CouldntLoadDataException,
      CPartialLoadException, LoadCancelledException {
    verifyViews(module);
  }

  private static void verifyViews(final INaviModule module) throws CouldntLoadDataException,
      CPartialLoadException, LoadCancelledException {
    final List<INaviView> views = module.getContent().getViewContainer().getViews();

    final INaviView callgraph = views.get(0);

    assertEquals(null, module.getContent().getViewContainer().getFunction(callgraph));
    assertEquals(ViewType.Native, callgraph.getType());
    assertEquals(GraphType.CALLGRAPH, callgraph.getGraphType());

    callgraph.load();

    callgraph.close();

    for (int i = 1; i < views.size(); i++) {
      final INaviView view = views.get(i);
      final INaviFunction function = module.getContent().getViewContainer().getFunction(view);

      assertEquals(view.getName(), function.getName());
      assertEquals(ViewType.Native, view.getType());
      assertEquals(GraphType.FLOWGRAPH, view.getGraphType());

      assertEquals(view.getNodeCount(), function.getBasicBlockCount());
      assertEquals(view.getEdgeCount(), function.getEdgeCount());

      view.load();
      function.load();

      assertEquals(view.getNodeCount(), function.getBasicBlockCount());
      assertEquals(view.getEdgeCount(), function.getEdgeCount());

      view.close();
      function.close();
    }
  }

  @Before
  public void setUp() throws IOException, CouldntLoadDriverException, CouldntConnectException,
      IllegalStateException, CouldntLoadDataException, InvalidDatabaseException,
      CouldntInitializeDatabaseException, InvalidExporterDatabaseFormatException,
      InvalidDatabaseVersionException, LoadCancelledException {
    final String[] parts = CConfigLoader.loadPostgreSQL();

    m_database =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0], "test_import",
            parts[1], parts[2], parts[3], false, false);

    m_database.connect();
    m_database.load();
  }

  @After
  public void tearDown() {
    m_database.close();
  }

  @Test
  public void verify() throws CouldntLoadDataException, IllegalStateException,
      CPartialLoadException, LoadCancelledException {
    final INaviModule module = m_database.getContent().getModules().get(1);

    module.load();

    verify(module);
  }
}
