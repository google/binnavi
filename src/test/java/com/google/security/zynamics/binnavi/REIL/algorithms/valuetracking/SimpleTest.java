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
package com.google.security.zynamics.binnavi.REIL.algorithms.valuetracking;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
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
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.algorithms.mono.IStateVector;
import com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTracker;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

@RunWith(JUnit4.class)
public final class SimpleTest {
  private CDatabase m_database;

  @Before
  public void setUp() throws IOException, CouldntLoadDriverException, CouldntConnectException,
      IllegalStateException, CouldntLoadDataException, InvalidDatabaseException,
      CouldntInitializeDatabaseException, InvalidExporterDatabaseFormatException,
      InvalidDatabaseVersionException, LoadCancelledException {
    final String[] parts = CConfigLoader.loadPostgreSQL();

    m_database =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0],
            "test_disassembly", parts[1], parts[2], parts[3], false, false);

    m_database.connect();
    m_database.load();
  }

  @After
  public void tearDown() {
    m_database.close();
  }

  @Test
  public void simpleTracking() throws CouldntLoadDataException, InternalTranslationException,
      CPartialLoadException, LoadCancelledException {
    final INaviModule module = m_database.getContent().getModules().get(0);

    module.load();

    final INaviView view =
        module.getViewsWithAddresses(
            Lists.newArrayList(new UnrelocatedAddress(new CAddress(0x10044BB))), true).get(0);

    assertEquals(0x10044BB, module.getContent().getViewContainer().getFunction(view).getAddress()
        .toLong());

    view.load();

    final ReilTranslator<INaviInstruction> translator = new ReilTranslator<INaviInstruction>();

    final ReilFunction reilFunction = translator.translate(new StandardEnvironment(), view);

    assertEquals(0, reilFunction.getGraph().getEdges().size());

    final IStateVector<InstructionGraphNode, ValueTrackerElement> result =
        ValueTracker.track(reilFunction);

    System.out.println(result);
  }
}
