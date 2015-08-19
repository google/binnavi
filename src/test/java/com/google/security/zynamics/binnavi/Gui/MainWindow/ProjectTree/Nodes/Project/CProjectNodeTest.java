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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.CProjectNode;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@RunWith(JUnit4.class)
public final class CProjectNodeTest {
  private MockDatabase m_database;
  private CProject m_project;

  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final JTree m_tree = new JTree();

  @Before
  public void setUp() throws IllegalArgumentException, SecurityException, FileReadException {
    ConfigManager.instance().read();

    m_database = new MockDatabase(m_provider);

    m_project =
        new CProject(123, "Name", "Comment", new Date(), new Date(), 0,
            new ArrayList<DebuggerTemplate>(), m_provider);

    final CProjectTreeModel model = new CProjectTreeModel(m_tree);

    model.setRoot(new DefaultMutableTreeNode());

    m_tree.setModel(model);
  }

  @Test
  public void testChangingName() throws CouldntSaveDataException {
    final CProjectNode node =
        new CProjectNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            new CProjectContainer(m_database, m_project));

    assertEquals("Name (0)", node.toString());

    m_project.getConfiguration().setName("Hannes");

    assertEquals("Hannes (0)", node.toString());
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CProjectNode node =
        new CProjectNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            new CProjectContainer(m_database, m_project));

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testLoaded() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, LoadCancelledException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_project.load();

    final CProjectNode node =
        new CProjectNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            new CProjectContainer(m_database, m_project));

    assertEquals("Name (1)", node.toString());

    final CAddressSpace addressSpace = m_project.getContent().createAddressSpace("Fark");

    assertEquals("Name (2)", node.toString());

    m_project.getContent().removeAddressSpace(addressSpace);

    assertEquals("Name (1)", node.toString());

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testUnloaded() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException, LoadCancelledException {
    final CProjectNode node =
        new CProjectNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            new CProjectContainer(m_database, m_project));

    assertEquals("Name (0)", node.toString());

    m_project.load();

    assertEquals("Name (1)", node.toString());

    final CAddressSpace addressSpace = m_project.getContent().createAddressSpace("Fark");

    assertEquals("Name (2)", node.toString());

    m_project.getContent().removeAddressSpace(addressSpace);

    assertEquals("Name (1)", node.toString());

    node.dispose();

    final LinkedHashSet<?> viewListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(
            ReflectionHelpers.getField(addressSpace, "m_listeners"), "m_listeners");

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
    assertTrue(viewListeners.isEmpty());
  }

  @Test
  public void testUnloadedClosed() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, LoadCancelledException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CProjectNode node =
        new CProjectNode(m_tree, new DefaultMutableTreeNode(), m_database, m_project,
            new CProjectContainer(m_database, m_project));

    assertEquals("Name (0)", node.toString());

    m_project.load();

    assertEquals("Name (1)", node.toString());

    final CAddressSpace addressSpace = m_project.getContent().createAddressSpace("Fark");

    assertEquals("Name (2)", node.toString());

    m_project.getContent().removeAddressSpace(addressSpace);

    assertEquals("Name (1)", node.toString());

    m_project.close();

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_project, "m_listeners"), "m_listeners")).isEmpty());
  }
}
