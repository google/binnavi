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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.CCriteriaFactory;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.CAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor.CColorCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTreeModel;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTreeNode;
import com.google.security.zynamics.binnavi.Plugins.PluginRegistry;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.swing.JTree;


@RunWith(JUnit4.class)
public class JCriteriumTreeModelTest {
  private ZyGraph m_graph;
  @SuppressWarnings("unused")
  private CCachedExpressionTree m_tree;

  @Before
  public void setUp() throws FileReadException, CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    m_graph = ZyGraphFactory.generateTestGraph();
    m_tree = new CCachedExpressionTree();

    try {
      PluginInterface.instance("", new MockDatabaseManager(), new PluginRegistry());
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testAppendNode() {
    final JTree tree = new JTree();

    final CCriteriumTree cCriteriumTree = new CCriteriumTree();

    final CCriteriaFactory cCriteriaFactory = new CCriteriaFactory(m_graph, null, null);

    final JCriteriumTreeModel jCriterumTreeModel =
        new JCriteriumTreeModel(tree, cCriteriumTree, cCriteriaFactory.getConditions());

    final CCriteriumTreeNode child = new CCriteriumTreeNode(new CColorCriterium(m_graph));
    cCriteriumTree.appendNode(cCriteriumTree.getRoot(), child);

    assertEquals(1, ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildCount());
    assertEquals(child.getCriterium(),
        ((JCriteriumTreeNode) ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildAt(0))
            .getCriterium());
  }

  @Test
  public void testEmpty() {
    final JTree tree = new JTree();

    final CCriteriumTree cCriteriumTree = new CCriteriumTree();

    final CCriteriaFactory cCriteriaFactory = new CCriteriaFactory(m_graph, null, null);

    @SuppressWarnings("unused")
    final JCriteriumTreeModel jCriterumTreeModel =
        new JCriteriumTreeModel(tree, cCriteriumTree, cCriteriaFactory.getConditions());
  }

  @Test
  public void testInsertNode() {
    final JTree tree = new JTree();

    final CCriteriumTree cCriteriumTree = new CCriteriumTree();

    final CCriteriaFactory cCriteriaFactory = new CCriteriaFactory(m_graph, null, null);

    final JCriteriumTreeModel jCriterumTreeModel =
        new JCriteriumTreeModel(tree, cCriteriumTree, cCriteriaFactory.getConditions());

    final CCriteriumTreeNode child = new CCriteriumTreeNode(new CColorCriterium(m_graph));
    cCriteriumTree.appendNode(cCriteriumTree.getRoot(), child);

    final CCriteriumTreeNode child2 = new CCriteriumTreeNode(new CAndCriterium());
    cCriteriumTree.insertNode(cCriteriumTree.getRoot(), child2);

    assertEquals(1, ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildCount());
    assertEquals(child2.getCriterium(),
        ((JCriteriumTreeNode) ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildAt(0))
            .getCriterium());
    assertEquals(child.getCriterium(),
        ((JCriteriumTreeNode) ((JCriteriumTreeNode) ((JCriteriumTreeNode) jCriterumTreeModel
            .getRoot()).getChildAt(0)).getChildAt(0)).getCriterium());
  }

  @Test
  public void testRemoveAll() {
    final JTree tree = new JTree();

    final CCriteriumTree cCriteriumTree = new CCriteriumTree();

    final CCriteriaFactory cCriteriaFactory = new CCriteriaFactory(m_graph, null, null);

    final JCriteriumTreeModel jCriterumTreeModel =
        new JCriteriumTreeModel(tree, cCriteriumTree, cCriteriaFactory.getConditions());

    final CCriteriumTreeNode child = new CCriteriumTreeNode(new CColorCriterium(m_graph));
    cCriteriumTree.appendNode(cCriteriumTree.getRoot(), child);

    final CCriteriumTreeNode child2 = new CCriteriumTreeNode(new CAndCriterium());
    cCriteriumTree.insertNode(cCriteriumTree.getRoot(), child2);

    final CCriteriumTreeNode child3 = new CCriteriumTreeNode(new CColorCriterium(m_graph));
    cCriteriumTree.appendNode(child2, child3);

    cCriteriumTree.clear();
    assertEquals(0, ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildCount());

  }

  @Test
  public void testRemoveNode() {
    final JTree tree = new JTree();

    final CCriteriumTree cCriteriumTree = new CCriteriumTree();

    final CCriteriaFactory cCriteriaFactory = new CCriteriaFactory(m_graph, null, null);

    final JCriteriumTreeModel jCriterumTreeModel =
        new JCriteriumTreeModel(tree, cCriteriumTree, cCriteriaFactory.getConditions());

    final CCriteriumTreeNode child = new CCriteriumTreeNode(new CColorCriterium(m_graph));
    cCriteriumTree.appendNode(cCriteriumTree.getRoot(), child);

    final CCriteriumTreeNode child2 = new CCriteriumTreeNode(new CAndCriterium());
    cCriteriumTree.insertNode(cCriteriumTree.getRoot(), child2);

    final CCriteriumTreeNode child3 = new CCriteriumTreeNode(new CColorCriterium(m_graph));
    cCriteriumTree.appendNode(child2, child3);

    cCriteriumTree.remove(child);
    assertEquals(1, ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildCount());
    assertEquals(1,
        ((JCriteriumTreeNode) ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildAt(0))
            .getChildCount());
    assertEquals(child2.getCriterium(),
        ((JCriteriumTreeNode) ((JCriteriumTreeNode) jCriterumTreeModel.getRoot()).getChildAt(0))
            .getCriterium());
  }
}
