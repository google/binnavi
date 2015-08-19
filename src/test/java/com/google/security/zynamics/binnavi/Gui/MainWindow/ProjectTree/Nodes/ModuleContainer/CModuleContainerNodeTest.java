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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;
import java.util.Date;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@RunWith(JUnit4.class)
public final class CModuleContainerNodeTest {
  private MockDatabase m_database;

  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final JTree m_tree = new JTree();

  @Before
  public void setUp() throws IllegalArgumentException, SecurityException, FileReadException {
    ConfigManager.instance().read();

    m_database = new MockDatabase(m_provider);

    final CProjectTreeModel model = new CProjectTreeModel(m_tree);

    model.setRoot(new DefaultMutableTreeNode());

    m_tree.setModel(model);
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CModuleContainerNode node = new CModuleContainerNode(m_tree, m_database);

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
    assertTrue(((Collection<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
        m_database.getContent().getDebuggerTemplateManager(), "listeners"), "m_listeners"))
        == null);
  }

  @Test
  public void testLoaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CRawModule rawModule = new CRawModule(1, "", 0, false, m_provider);
    final CModule module1 = new CModule(123,
        "Module 1",
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
        rawModule,
        Integer.MAX_VALUE,
        false,
        m_provider);
    final CModule module2 = new CModule(123,
        "Module 2",
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
        rawModule,
        Integer.MAX_VALUE,
        false,
        m_provider);

    m_database.getContent().addModule(module1);
    m_database.getContent().addModule(module2);

    final CModuleContainerNode node = new CModuleContainerNode(m_tree, m_database);

    assertEquals("Modules (2)", node.toString());
    assertEquals(2, node.getChildCount());
    assertEquals("Module 1 (55/66)", node.getChildAt(0).toString());
    assertEquals("Module 2 (55/66)", node.getChildAt(1).toString());

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
    assertTrue(((Collection<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
        m_database.getContent().getDebuggerTemplateManager(), "listeners"), "m_listeners"))
        == null);
  }

  @Test
  public void testUnloaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CRawModule rawModule = new CRawModule(1, "", 0, false, m_provider);
    final CModule module1 = new CModule(123,
        "Module 1",
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
        rawModule,
        Integer.MAX_VALUE,
        false,
        m_provider);
    final CModule module2 = new CModule(123,
        "Module 2",
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
        rawModule,
        Integer.MAX_VALUE,
        false,
        m_provider);

    final CModuleContainerNode node = new CModuleContainerNode(m_tree, m_database);

    assertEquals("Modules (0)", node.toString());
    assertEquals(0, node.getChildCount());

    m_database.getContent().addModule(module1);
    m_database.getContent().addModule(module2);

    assertEquals("Modules (2)", node.toString());
    assertEquals(2, node.getChildCount());
    assertEquals("Module 1 (55/66)", node.getChildAt(0).toString());
    assertEquals("Module 2 (55/66)", node.getChildAt(1).toString());

    m_database.getContent().delete(module1);

    assertEquals("Modules (1)", node.toString());
    assertEquals(1, node.getChildCount());
    assertEquals("Module 2 (55/66)", node.getChildAt(0).toString());

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
    assertTrue(((Collection<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
        m_database.getContent().getDebuggerTemplateManager(), "listeners"), "m_listeners"))
        == null);
  }
}
