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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@RunWith(JUnit4.class)
public final class CDebuggerContainerNodeTest {
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
    final CDebuggerContainerNode node = new CDebuggerContainerNode(m_tree, m_database);

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
        m_database.getContent().getDebuggerTemplateManager(), "listeners"), "m_listeners"))
        .isEmpty());
  }

  @Test
  public void testLoaded() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final DebuggerTemplate debugger1 = new DebuggerTemplate(1, "Debugger 1", "", 0, m_provider);
    final DebuggerTemplate debugger2 = new DebuggerTemplate(2, "Debugger 2", "", 0, m_provider);

    m_database.getContent().getDebuggerTemplateManager().addDebugger(debugger1);
    m_database.getContent().getDebuggerTemplateManager().addDebugger(debugger2);

    final CDebuggerContainerNode node = new CDebuggerContainerNode(m_tree, m_database);

    assertEquals("Debuggers (2)", node.toString());
    assertEquals(2, node.getChildCount());
    assertEquals("Debugger 1", node.getChildAt(0).toString());
    assertEquals("Debugger 2", node.getChildAt(1).toString());

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
        m_database.getContent().getDebuggerTemplateManager(), "listeners"), "m_listeners"))
        .isEmpty());
  }

  @Test
  public void testUnloaded()
      throws CouldntDeleteException,
      IllegalArgumentException,
      SecurityException,
      IllegalAccessException,
      NoSuchFieldException {
    final DebuggerTemplate debugger1 = new DebuggerTemplate(1, "Debugger 1", "", 0, m_provider);
    final DebuggerTemplate debugger2 = new DebuggerTemplate(2, "Debugger 2", "", 0, m_provider);

    final CDebuggerContainerNode node = new CDebuggerContainerNode(m_tree, m_database);

    assertEquals("Debuggers (0)", node.toString());
    assertEquals(0, node.getChildCount());

    m_database.getContent().getDebuggerTemplateManager().addDebugger(debugger1);
    m_database.getContent().getDebuggerTemplateManager().addDebugger(debugger2);

    assertEquals("Debuggers (2)", node.toString());
    assertEquals(2, node.getChildCount());
    assertEquals("Debugger 1", node.getChildAt(0).toString());
    assertEquals("Debugger 2", node.getChildAt(1).toString());

    m_database.getContent().getDebuggerTemplateManager().removeDebugger(debugger1);

    assertEquals("Debuggers (1)", node.toString());
    assertEquals(1, node.getChildCount());
    assertEquals("Debugger 2", node.getChildAt(0).toString());

    node.dispose();

    assertTrue(((Collection<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(m_database, "listeners"), "m_listeners")) == null);
    assertTrue(((LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
        m_database.getContent().getDebuggerTemplateManager(), "listeners"), "m_listeners"))
        .isEmpty());
  }
}
