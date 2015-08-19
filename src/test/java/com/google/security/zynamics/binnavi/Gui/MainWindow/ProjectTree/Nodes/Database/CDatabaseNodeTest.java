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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database.CDatabaseNode;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
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
public final class CDatabaseNodeTest {
  private MockDatabase m_database;
  private CProject m_project;
  @SuppressWarnings("unused")
  private CAddressSpace m_addressSpace;

  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final JTree m_tree = new JTree();

  @Before
  public void setUp() throws IllegalArgumentException, SecurityException, FileReadException,
      CouldntLoadDataException, CouldntSaveDataException, LoadCancelledException {
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
  }

  @Test
  public void testChangingDescription() {
    final CDatabaseNode node = new CDatabaseNode(m_tree, new DefaultMutableTreeNode(), m_database);

    assertEquals("Mock Database", node.toString());

    m_database.getConfiguration().setDescription("Hannes");

    assertEquals("Hannes", node.toString());
  }

  @Test
  public void testClosed() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CDatabaseNode node = new CDatabaseNode(m_tree, new DefaultMutableTreeNode(), m_database);

    m_database.load();

    m_database.close();

    node.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testConstructor() {
    final CDatabaseNode node = new CDatabaseNode(m_tree, new DefaultMutableTreeNode(), m_database);

    assertEquals(m_database, node.getObject());
    assertNotNull(node.getComponent());
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CDatabaseNode node = new CDatabaseNode(m_tree, new DefaultMutableTreeNode(), m_database);

    node.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testLoaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_database.load();

    final CDatabaseNode node = new CDatabaseNode(m_tree, new DefaultMutableTreeNode(), m_database);

    node.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
  }

  @Test
  public void testUnloaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CDatabaseNode node = new CDatabaseNode(m_tree, new DefaultMutableTreeNode(), m_database);

    m_database.load();

    node.dispose();

    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")).isEmpty());
  }
}
