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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboBox;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.CModuleOverviewPanel;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CStandardEditPanel;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTree;

@RunWith(JUnit4.class)
public final class CModuleOverviewPanelTest {
  private JButton m_saveButton;
  private CStandardEditPanel m_editPanel;
  private CModuleOverviewPanel m_component;
  private MockDatabase m_database;
  private CProject m_project;

  private CDebuggerComboBox m_debuggerCombo;
  private final MockSqlProvider m_provider = new MockSqlProvider();
  private INaviModule m_module;
  private IViewContainer m_viewContainer;
  private JTextField m_fileBaseAddr;
  private JTextField m_imageBaseAddr;

  private void sleep() {
    try {
      Thread.sleep(100);
    } catch (final InterruptedException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  @Before
  public void setUp() throws CouldntLoadDataException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException, FileReadException, LoadCancelledException {
    ConfigManager.instance().read();

    final JTree tree = new JTree();

    m_database = new MockDatabase(m_provider);

    m_project =
        new CProject(1, "Mock Project", "Mock Project Description", new Date(), new Date(), 0,
            new ArrayList<DebuggerTemplate>(), m_provider);

    m_project.load();

    final DebuggerTemplate template =
        new DebuggerTemplate(1, "Mock Debugger", "localhaus", 88, m_provider);

    m_module =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), template, null, Integer.MAX_VALUE, false,
            m_provider);

    m_database.getContent().getDebuggerTemplateManager().addDebugger(template);

    m_viewContainer = new CModuleContainer(m_database, m_module);

    m_component = new CModuleOverviewPanel(tree, m_database, null, m_module, m_viewContainer);

    m_saveButton = (JButton) ReflectionHelpers.getField(m_component, "m_saveButton");
    m_editPanel = (CStandardEditPanel) ReflectionHelpers.getField(m_component, "m_stdEditPanel");
    m_fileBaseAddr =
        (JTextField) ReflectionHelpers.getField(
            ReflectionHelpers.getField(m_component, "m_debuggerPanel"), "m_fileBaseAddr");
    m_imageBaseAddr =
        (JTextField) ReflectionHelpers.getField(
            ReflectionHelpers.getField(m_component, "m_debuggerPanel"), "m_imageBaseAddr");
    m_debuggerCombo =
        (CDebuggerComboBox) ReflectionHelpers.getField(
            ReflectionHelpers.getField(m_component, "m_debuggerPanel"), "m_debuggerCombo");
  }

  @Test
  public void testChangingDebugger() throws CouldntSaveDataException, SecurityException,
      NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    final DebuggerTemplate template1 = new DebuggerTemplate(1, "A", "", 0, m_provider);
    final DebuggerTemplate template2 = new DebuggerTemplate(1, "B", "", 0, m_provider);

    m_database.getContent().getDebuggerTemplateManager().addDebugger(template1);
    m_database.getContent().getDebuggerTemplateManager().addDebugger(template2);

    m_project.getConfiguration().addDebugger(template1);
    m_project.getConfiguration().addDebugger(template2);

    sleep();

    assertFalse(m_saveButton.isEnabled());

    m_debuggerCombo.setSelectedIndex(3);

    sleep();

    assertTrue(m_saveButton.isEnabled());

    m_module.getConfiguration().setDebuggerTemplate(template2);

    sleep();

    assertFalse(m_saveButton.isEnabled());

    m_module.getConfiguration().setDebuggerTemplate(template1);

    sleep();

    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testChangingDescription() throws CouldntSaveDataException, SecurityException,
      NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    assertFalse(m_saveButton.isEnabled());

    m_editPanel.setDescription("Hannes");

    assertTrue(m_saveButton.isEnabled());

    m_module.getConfiguration().setDescription("Hannes");

    sleep();

    assertFalse(m_saveButton.isEnabled());

    m_module.getConfiguration().setDescription("Hannes2");

    sleep();

    assertEquals("Hannes2", m_editPanel.getDescription());
    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testChangingFilebase() throws CouldntSaveDataException, SecurityException,
      NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException {
    assertFalse(m_saveButton.isEnabled());

    m_fileBaseAddr.setText("1");

    Thread.sleep(100);

    assertTrue(m_saveButton.isEnabled());

    m_module.getConfiguration().setFileBase(new CAddress(1));

    Thread.sleep(100);

    assertFalse(m_saveButton.isEnabled());

    m_module.getConfiguration().setFileBase(new CAddress(2));

    Thread.sleep(100);

    assertEquals("00000002", m_fileBaseAddr.getText());
    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testChangingImagebase() throws CouldntSaveDataException, SecurityException,
      NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException {
    assertFalse(m_saveButton.isEnabled());

    m_imageBaseAddr.setText("1");

    Thread.sleep(100);

    assertTrue(m_saveButton.isEnabled());

    m_module.getConfiguration().setImageBase(new CAddress(1));

    Thread.sleep(100);

    assertFalse(m_saveButton.isEnabled());

    m_module.getConfiguration().setImageBase(new CAddress(2));

    Thread.sleep(100);

    assertEquals("00000002", m_imageBaseAddr.getText());
    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testChangingName() throws CouldntSaveDataException, SecurityException,
      NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    assertFalse(m_saveButton.isEnabled());

    m_editPanel.setNameString("Hannes");

    sleep();

    assertTrue(m_saveButton.isEnabled());

    m_module.getConfiguration().setName("Hannes");

    sleep();

    assertFalse(m_saveButton.isEnabled());

    m_module.getConfiguration().setName("Hannes2");

    sleep();

    assertEquals("Hannes2", m_editPanel.getNameString());
    assertFalse(m_saveButton.isEnabled());

    testListenersRemoved();
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_component.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")) == null);
  }

  @Test
  public void testSaveButton() throws InterruptedException, IllegalArgumentException,
      SecurityException, IllegalAccessException, NoSuchFieldException {
    m_editPanel.setNameString("New Name");
    m_editPanel.setDescription("New Description");

    assertTrue(m_saveButton.isEnabled());

    m_saveButton.getAction().actionPerformed(null);

    Thread.sleep(500);

    assertFalse(m_saveButton.isEnabled());

    assertEquals("New Name", m_module.getConfiguration().getName());
    assertEquals("New Description", m_module.getConfiguration().getDescription());

    testListenersRemoved();
  }
}
