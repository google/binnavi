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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.CProjectModulesModel;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;

@RunWith(JUnit4.class)
public final class CProjectModulesModelTest {
  private final MockSqlProvider m_provider = new MockSqlProvider();
  @SuppressWarnings("unused")
  private MockDatabase m_database;
  private CProject m_project;
  private CAddressSpace m_addressSpace;

  @Test
  public void testSimple() throws CouldntLoadDataException, CouldntSaveDataException,
      LoadCancelledException {
    m_database = new MockDatabase(m_provider);

    m_project =
        new CProject(1, "Mock Project", "Mock Project Description", new Date(), new Date(), 0,
            new ArrayList<DebuggerTemplate>(), m_provider);

    m_project.load();

    m_addressSpace = m_project.getContent().createAddressSpace("Address Space");

    m_addressSpace.load();

    final CModule module1 =
        new CModule(1, "Name 1", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, m_provider), null, Integer.MAX_VALUE, false, m_provider);
    m_addressSpace.getContent().addModule(module1);

    final CModule module2 =
        new CModule(2, "Name 2", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, m_provider), null, Integer.MAX_VALUE, false, m_provider);
    m_addressSpace.getContent().addModule(module2);

    final CProjectModulesModel model = new CProjectModulesModel(m_addressSpace);

    assertEquals(2, model.getRowCount());
    assertEquals("Name 1", model.getValueAt(0, 0));
    assertEquals("Name 2", model.getValueAt(1, 0));
  }
}
