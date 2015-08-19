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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.CModuleNode;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@RunWith(JUnit4.class)
public final class CModuleNodeTest {
  private MockDatabase m_database;
  private CModule m_module;

  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final JTree m_tree = new JTree();

  @Before
  public void setUp() throws IllegalArgumentException, SecurityException, FileReadException {
    ConfigManager.instance().read();

    m_database = new MockDatabase(m_provider);

    final CRawModule rawModule = new CRawModule(1, "", 0, false, m_provider);
    m_module =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, m_provider), rawModule, Integer.MAX_VALUE, false, m_provider);

    final CProjectTreeModel model = new CProjectTreeModel(m_tree);

    model.setRoot(new DefaultMutableTreeNode());

    m_tree.setModel(model);
  }

  @Test
  public void testChangingName() throws CouldntSaveDataException {
    final CModuleNode node =
        new CModuleNode(m_tree, new DefaultMutableTreeNode(), m_database, m_module,
            new CModuleContainer(m_database, m_module));

    assertEquals("Name (55/66)", node.toString());

    m_module.getConfiguration().setName("Hannes");

    assertEquals("Hannes (55/66)", node.toString());
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CModuleContainer container = new CModuleContainer(m_database, m_module);
    final CModuleNode node =
        new CModuleNode(m_tree, new DefaultMutableTreeNode(), m_database, m_module, container);

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_module, "m_listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testLoaded() throws CouldntLoadDataException, CouldntDeleteException,
      LoadCancelledException, IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    m_module.load();

    final CModuleNode node =
        new CModuleNode(m_tree, new DefaultMutableTreeNode(), m_database, m_module,
            new CModuleContainer(m_database, m_module));

    assertEquals("Name (1/0)", node.toString());

    final CView view = m_module.getContent().getViewContainer().createView("Foo", "Bar");

    assertEquals("Name (1/1)", node.toString());

    m_module.getContent().getViewContainer().deleteView(view);

    assertEquals("Name (1/0)", node.toString());

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_module, "m_listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testUnloaded() throws CouldntLoadDataException, CouldntDeleteException,
      IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException,
      LoadCancelledException {
    final CModuleNode node =
        new CModuleNode(m_tree, new DefaultMutableTreeNode(), m_database, m_module,
            new CModuleContainer(m_database, m_module));

    assertEquals("Name (55/66)", node.toString());

    m_module.load();

    assertEquals("Name (1/0)", node.toString());

    final CView view = m_module.getContent().getViewContainer().createView("Foo", "Bar");

    assertEquals("Name (1/1)", node.toString());

    m_module.getContent().getViewContainer().deleteView(view);

    assertEquals("Name (1/0)", node.toString());

    node.dispose();

    final LinkedHashSet<?> viewListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(
            ReflectionHelpers.getField(view, "m_listeners"), "m_listeners");

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_module, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(viewListeners.isEmpty());
  }

  @Test
  public void testUnloadedClosed() throws CouldntLoadDataException, CouldntDeleteException,
      LoadCancelledException, IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    final CModuleNode node =
        new CModuleNode(m_tree, new DefaultMutableTreeNode(), m_database, m_module,
            new CModuleContainer(m_database, m_module));

    assertEquals("Name (55/66)", node.toString());

    m_module.load();

    assertEquals("Name (1/0)", node.toString());

    final CView view = m_module.getContent().getViewContainer().createView("Foo", "Bar");

    assertEquals("Name (1/1)", node.toString());

    m_module.getContent().getViewContainer().deleteView(view);

    assertEquals("Name (1/0)", node.toString());

    m_module.close();

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_module, "m_listeners"), "m_listeners")).isEmpty());
  }
}
