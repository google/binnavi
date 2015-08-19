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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.CAddressSpaceNode;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyGraphBuilderManager;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@RunWith(JUnit4.class)
public final class CAddressSpaceNodeTest {
  private MockDatabase m_database;
  private CProject m_project;
  private CAddressSpace m_addressSpace;

  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final JTree m_tree = new JTree();
  private IViewContainer m_container;

  @Before
  public void setUp() throws IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException, FileReadException, CouldntLoadDataException, CouldntSaveDataException,
      LoadCancelledException {
    ConfigManager.instance().read();

    m_database = new MockDatabase(m_provider);

    m_project =
        new CProject(1, "Mock Project", "Mock Project Description", new Date(), new Date(), 0,
            new ArrayList<DebuggerTemplate>(), m_provider);

    m_project.load();

    m_addressSpace = m_project.getContent().createAddressSpace("Address Space");

    final CProjectTreeModel model = new CProjectTreeModel(m_tree);

    model.setRoot(new DefaultMutableTreeNode());

    m_tree.setModel(model);

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_addressSpace, "m_listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(ZyGraphBuilderManager.instance(), "m_listeners"), "m_listeners"))
        == null);

    m_container = new CProjectContainer(m_database, m_project, m_addressSpace);
  }

  @Test
  public void testChangingName() throws CouldntSaveDataException {
    final CAddressSpaceNode node =
        new CAddressSpaceNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            m_addressSpace, m_container);

    assertEquals("Address Space (?)", node.toString());

    m_addressSpace.getConfiguration().setName("Hannes");

    assertEquals("Hannes (?)", node.toString());
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CAddressSpaceNode node =
        new CAddressSpaceNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            m_addressSpace, m_container);

    node.dispose();

    m_container.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_addressSpace, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(ZyGraphBuilderManager.instance(), "m_listeners"), "m_listeners")) == null);
  }

  @Test
  public void testLoaded() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, LoadCancelledException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_addressSpace.load();

    final CAddressSpaceNode node =
        new CAddressSpaceNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            m_addressSpace, m_container);

    assertEquals("Address Space (0)", node.toString());

    final MockModule unloadedModule = new MockModule(m_provider, false);

    m_addressSpace.getContent().addModule(new MockModule(m_provider, true));
    m_addressSpace.getContent().addModule(unloadedModule);

    assertEquals("Address Space (2)", node.toString());
    assertEquals(2, node.getChildCount());

    m_addressSpace.getContent().removeModule(unloadedModule);

    assertEquals("Address Space (1)", node.toString());
    assertEquals(1, node.getChildCount());

    node.dispose();

    m_container.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_addressSpace, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(ZyGraphBuilderManager.instance(), "m_listeners"), "m_listeners")) == null);
  }

  @Test
  public void testUnloaded() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, LoadCancelledException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CAddressSpaceNode node =
        new CAddressSpaceNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            m_addressSpace, m_container);

    assertEquals("Address Space (?)", node.toString());

    m_addressSpace.load();

    assertEquals("Address Space (0)", node.toString());

    final MockModule unloadedModule = new MockModule(m_provider, false);

    m_addressSpace.getContent().addModule(new MockModule(m_provider, true));
    m_addressSpace.getContent().addModule(unloadedModule);

    assertEquals("Address Space (2)", node.toString());
    assertEquals(2, node.getChildCount());

    m_addressSpace.getContent().removeModule(unloadedModule);

    assertEquals("Address Space (1)", node.toString());
    assertEquals(1, node.getChildCount());

    node.dispose();

    m_container.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_addressSpace, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(ZyGraphBuilderManager.instance(), "m_listeners"), "m_listeners")) == null);
  }

  @Test
  public void testUnloadedClosed() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, LoadCancelledException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CAddressSpaceNode node =
        new CAddressSpaceNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            m_addressSpace, m_container);

    assertEquals("Address Space (?)", node.toString());

    m_addressSpace.load();

    assertEquals("Address Space (0)", node.toString());

    final MockModule unloadedModule = new MockModule(m_provider, false);

    m_addressSpace.getContent().addModule(new MockModule(m_provider, true));
    m_addressSpace.getContent().addModule(unloadedModule);

    assertEquals("Address Space (2)", node.toString());
    assertEquals(2, node.getChildCount());

    m_addressSpace.getContent().removeModule(unloadedModule);

    assertEquals("Address Space (1)", node.toString());
    assertEquals(1, node.getChildCount());

    m_addressSpace.close();

    node.dispose();

    m_container.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_addressSpace, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(ZyGraphBuilderManager.instance(), "m_listeners"), "m_listeners")) == null);
  }
}
