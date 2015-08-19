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

import com.google.common.collect.Iterables;
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
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.IBlockNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLVerifyNotepadTest {
  private CDatabase m_database;

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
  public void verifyFunctions() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = m_database.getContent().getModules().get(0);

    module.load();

    assertEquals("RegQueryValueExW",
        module.getContent().getFunctionContainer().getFunctions().get(0).getName());
    assertEquals("GlobalFree", module.getContent().getFunctionContainer().getFunctions().get(50)
        .getName());
    assertEquals("DefWindowProcW",
        module.getContent().getFunctionContainer().getFunctions().get(100).getName());
    assertEquals("sub_10075F4", module.getContent().getFunctionContainer().getFunctions().get(285)
        .getName());

    assertEquals(BigInteger.valueOf(16781312), module.getContent().getFunctionContainer()
        .getFunctions().get(0).getAddress().toBigInteger());
    assertEquals(BigInteger.valueOf(16781524), module.getContent().getFunctionContainer()
        .getFunctions().get(50).getAddress().toBigInteger());
    assertEquals(BigInteger.valueOf(16781732), module.getContent().getFunctionContainer()
        .getFunctions().get(100).getAddress().toBigInteger());
    assertEquals(BigInteger.valueOf(16807412), module.getContent().getFunctionContainer()
        .getFunctions().get(285).getAddress().toBigInteger());

    module.close();
  }

  @Test
  public void verifyInstructions() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = m_database.getContent().getModules().get(0);

    module.load();

    final INaviFunction function =
        module.getContent().getFunctionContainer()
            .getFunction(new CAddress(BigInteger.valueOf(0x0100195D)));

    function.load();

    final List<IBlockNode> blocks = function.getBasicBlocks();

    final IBlockNode block = blocks.get(0);

    assertEquals(BigInteger.valueOf(0x0100195D), block.getAddress().toBigInteger());

    final Iterable<INaviInstruction> instructions = block.getInstructions();

    Iterables.get(instructions, 0).toString();

    assertEquals("0100195D mov edi, edi", Iterables.get(instructions, 0).toString());
    assertEquals("0100195F push ebp", Iterables.get(instructions, 1).toString());
    assertEquals("01001960 mov ebp, esp", Iterables.get(instructions, 2).toString());
    assertEquals("01001962 push ecx", Iterables.get(instructions, 3).toString());
    assertEquals("01001963 push 2", Iterables.get(instructions, 4).toString());
    assertEquals("01001965 lea eax, ss: [ebp + LCData]", Iterables.get(instructions, 5).toString());
    assertEquals("01001968 push eax", Iterables.get(instructions, 6).toString());
    assertEquals("01001969 push 13", Iterables.get(instructions, 7).toString());
    assertEquals("0100196B push 1024", Iterables.get(instructions, 8).toString());
    assertEquals("01001970 mov ds: [16819428], sub_1005F63", Iterables.get(instructions, 9)
        .toString());
    assertEquals("0100197A mov ds: [16819436], 12", Iterables.get(instructions, 10).toString());
    assertEquals("01001984 call ds: [GetLocaleInfoW]", Iterables.get(instructions, 11).toString());
    assertEquals("0100198A cmp word ss: [ebp + LCData], word 49", Iterables.get(instructions, 12)
        .toString());
    assertEquals("0100198F jnz loc_10019B1", Iterables.get(instructions, 13).toString());

    module.close();
  }

  @Test
  public void verifyReferences() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = m_database.getContent().getModules().get(0);

    module.load();

    final INaviFunction function =
        module.getContent().getFunctionContainer()
            .getFunction(new CAddress(BigInteger.valueOf(0x01006F10)));

    function.load();

    final List<IBlockNode> blocks = function.getBasicBlocks();

    final IBlockNode block = blocks.get(12);

    assertEquals(BigInteger.valueOf(0x100702B), block.getAddress().toBigInteger());

    final Iterable<INaviInstruction> instructions = block.getInstructions();

    assertEquals(1, Iterables.get(instructions, 10).getOperands().get(0).getRootNode()
        .getChildren().get(0).getReferences().size());

    module.close();
  }

  @Test
  public void verifyViews() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException, IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    final SQLProvider provider = (SQLProvider) ReflectionHelpers.getField(m_database, "provider");
    final INaviModule module = m_database.getContent().getModules().get(0);

    module.load();

    final List<INaviView> views = module.getContent().getViewContainer().getViews();

    final INaviView callgraph = views.get(0);

    assertEquals(null, module.getContent().getViewContainer().getFunction(callgraph));
    assertEquals(ViewType.Native, callgraph.getType());
    assertEquals(GraphType.CALLGRAPH, callgraph.getGraphType());

    assertEquals(287, callgraph.getNodeCount());
    assertEquals(848, callgraph.getEdgeCount());

    callgraph.load();

    assertEquals(287, callgraph.getNodeCount());
    assertEquals(848, callgraph.getEdgeCount());

    callgraph.close();

    final LinkedHashSet<?> cgListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(
            ReflectionHelpers.getField(CommentManager.get(provider), "listeners"), "m_listeners");
    assertEquals(module.getContent().getFunctionContainer().getFunctions().size() + 1,
        cgListeners.size()); // the +1 here is the listener in the type instance container.

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

      if (function.getAddress().toBigInteger().equals(BigInteger.valueOf(0x1001929))) {
        assertEquals(view.getNodeCount(), 5);
        assertEquals(view.getEdgeCount(), 6);
      } else if (function.getAddress().toBigInteger().equals(BigInteger.valueOf(16790571))) {
        assertEquals(view.getNodeCount(), 92);
        assertEquals(view.getEdgeCount(), 144);
      } else if (i == 1) {
        assertEquals(view.getNodeCount(), 0);
        assertEquals(view.getEdgeCount(), 0);
      }

      view.close();
      function.close();
    }

    module.close();
  }
}
