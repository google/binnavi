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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.CDebuggerNode;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@RunWith(JUnit4.class)
public final class CDebuggerNodeTest {
  private MockDatabase m_database;

  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final JTree m_tree = new JTree();

  private DebuggerTemplate m_template;

  @Before
  public void setUp() throws IllegalArgumentException, SecurityException, FileReadException {
    ConfigManager.instance().read();

    m_database = new MockDatabase(m_provider);

    m_template = new DebuggerTemplate(1, "My Debugger", "Bar", 123, m_provider);

    final CProjectTreeModel model = new CProjectTreeModel(m_tree);

    model.setRoot(new DefaultMutableTreeNode());

    m_tree.setModel(model);
  }

  @Test
  public void testChangingDescription() throws CouldntSaveDataException {
    final CDebuggerNode node =
        new CDebuggerNode(m_tree, new DefaultMutableTreeNode(), m_database, m_template);

    assertEquals("My Debugger", node.toString());

    m_template.setName("Hannes");

    assertEquals("Hannes", node.toString());
  }

  @Test
  public void testClosed() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CDebuggerNode node =
        new CDebuggerNode(m_tree, new DefaultMutableTreeNode(), m_database, m_template);

    m_database.load();

    m_database.close();

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
  }

  @Test
  public void testConstructor() {
    final CDebuggerNode node =
        new CDebuggerNode(m_tree, new DefaultMutableTreeNode(), m_database, m_template);

    assertEquals(m_template, node.getObject());
    assertNotNull(node.getComponent());
  }

  @Test
  public void testListenersRemoved() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CDebuggerNode node =
        new CDebuggerNode(m_tree, new DefaultMutableTreeNode(), m_database, m_template);

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
  }

  @Test
  public void testLoaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_database.load();

    final CDebuggerNode node =
        new CDebuggerNode(m_tree, new DefaultMutableTreeNode(), m_database, m_template);

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
  }

  @Test
  public void testUnloaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CDebuggerNode node =
        new CDebuggerNode(m_tree, new DefaultMutableTreeNode(), m_database, m_template);

    m_database.load();

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
  }
}
