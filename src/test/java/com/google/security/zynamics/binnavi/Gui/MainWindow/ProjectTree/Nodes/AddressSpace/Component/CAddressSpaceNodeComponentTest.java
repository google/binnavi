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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboBox;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CStandardEditPanel;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;

import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;

@RunWith(JUnit4.class)
public final class CAddressSpaceNodeComponentTest {
  private JButton m_saveButton;
  private CStandardEditPanel m_editPanel;
  private CAddressSpaceNodeComponent m_component;
  private MockDatabase m_database;
  private CProject m_project;
  private CAddressSpace m_addressSpace;

  private TitledBorder m_titledBorder;
  private CDebuggerComboBox m_debuggerCombo;
  private final MockSqlProvider m_provider = new MockSqlProvider();

  private void sleep() {
    try {
      Thread.sleep(100);
    } catch (final InterruptedException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  @Before
  public void setUp()
      throws CouldntLoadDataException,
      CouldntSaveDataException,
      IllegalArgumentException,
      SecurityException,
      IllegalAccessException,
      NoSuchFieldException,
      LoadCancelledException {
    final JTree tree = new JTree();

    m_database = new MockDatabase(m_provider);

    m_project = new CProject(1,
        "Mock Project",
        "Mock Project Description",
        new Date(),
        new Date(),
        0,
        new ArrayList<DebuggerTemplate>(),
        m_provider);

    m_project.load();

    m_addressSpace = m_project.getContent().createAddressSpace("Address Space");

    m_component = new CAddressSpaceNodeComponent(tree, m_database, m_project, m_addressSpace);

    m_saveButton = (JButton) ReflectionHelpers.getField(m_component, "m_saveButton");
    m_editPanel = (CStandardEditPanel) ReflectionHelpers.getField(m_component, "m_stdEditPanel");
    m_titledBorder = (TitledBorder) ReflectionHelpers.getField(m_component, "m_titledBorder");
    m_debuggerCombo =
        (CDebuggerComboBox) ReflectionHelpers.getField(m_component, "m_debuggerCombo");

  }

  @Test
  public void testChangingDebugger()
      throws CouldntSaveDataException,
      SecurityException,
      NoSuchFieldException,
      IllegalArgumentException,
      IllegalAccessException {
    final DebuggerTemplate template1 = new DebuggerTemplate(1, "", "", 0, m_provider);
    final DebuggerTemplate template2 = new DebuggerTemplate(1, "", "", 0, m_provider);

    m_database.getContent().getDebuggerTemplateManager().addDebugger(template1);
    m_database.getContent().getDebuggerTemplateManager().addDebugger(template2);

    m_project.getConfiguration().addDebugger(template1);
    m_project.getConfiguration().addDebugger(template2);

    assertFalse(m_saveButton.isEnabled());

    m_debuggerCombo.setSelectedIndex(2);

    assertTrue(m_saveButton.isEnabled());

    m_addressSpace.getConfiguration().setDebuggerTemplate(template2);

    assertFalse(m_saveButton.isEnabled());

    m_addressSpace.getConfiguration().setDebuggerTemplate(template1);

    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testChangingDescription()
      throws CouldntSaveDataException,
      SecurityException,
      NoSuchFieldException,
      IllegalArgumentException,
      IllegalAccessException {
    assertFalse(m_saveButton.isEnabled());

    m_editPanel.setDescription("Hannes");

    assertTrue(m_saveButton.isEnabled());

    m_addressSpace.getConfiguration().setDescription("Hannes");

    sleep();

    assertFalse(m_saveButton.isEnabled());

    m_addressSpace.getConfiguration().setDescription("Hannes2");

    sleep();

    assertEquals("Hannes2", m_editPanel.getDescription());
    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testChangingModules()
      throws CouldntSaveDataException,
      CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      IllegalArgumentException,
      SecurityException,
      IllegalAccessException,
      NoSuchFieldException {
    m_addressSpace.load();

    final CModule module = new CModule(123,
        "Name",
        "Comment",
        new Date(),
        new Date(),
        "12345678123456781234567812345678",
        "1234567812345678123456781234567812345678",
        55,
        66,
        new CAddress(0x555),
        new CAddress(0x666),
        new DebuggerTemplate(1, "Mock Debugger", "localhaus", 88, m_provider),
        null,
        Integer.MAX_VALUE,
        false,
        m_provider);

    final String previousBorderTest = m_titledBorder.getTitle();

    assertSame(previousBorderTest, m_titledBorder.getTitle());

    m_addressSpace.getContent().addModule(module);

    assertNotSame(previousBorderTest, m_titledBorder.getTitle());

    m_addressSpace.getContent().removeModule(module);

    assertEquals(previousBorderTest, m_titledBorder.getTitle());

    testListenersRemoved();
  }

  @Test
  public void testChangingName()
      throws CouldntSaveDataException,
      SecurityException,
      NoSuchFieldException,
      IllegalArgumentException,
      IllegalAccessException {
    assertFalse(m_saveButton.isEnabled());

    m_editPanel.setNameString("Hannes");

    assertTrue(m_saveButton.isEnabled());

    m_addressSpace.getConfiguration().setName("Hannes");

    sleep();

    assertFalse(m_saveButton.isEnabled());

    m_addressSpace.getConfiguration().setName("Hannes2");

    sleep();

    assertEquals("Hannes2", m_editPanel.getNameString());
    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_component.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_addressSpace, "m_listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testSaveButton()
      throws InterruptedException,
      IllegalArgumentException,
      SecurityException,
      IllegalAccessException,
      NoSuchFieldException {
    m_editPanel.setNameString("New Name");
    m_editPanel.setDescription("New Description");

    assertTrue(m_saveButton.isEnabled());

    m_saveButton.getAction().actionPerformed(null);

    Thread.sleep(500);

    assertFalse(m_saveButton.isEnabled());

    assertEquals("New Name", m_addressSpace.getConfiguration().getName());
    assertEquals("New Description", m_addressSpace.getConfiguration().getDescription());

    testListenersRemoved();
  }
}
