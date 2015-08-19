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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.CCachedAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.InDegrees.CCachedIndegreeCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor.CCachedColorCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not.CCachedNotCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Or.CCachedOrCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.OutDegree.CCachedOutdegreeCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Selection.CCachedSelectionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Selection.SelectionState;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag.CCachedTagCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text.CCachedTextCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Visibillity.CCachedVisibilityCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Visibillity.VisibilityState;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

@RunWith(JUnit4.class)
public class CCriteriumExecuterTest {
  private ZyGraph m_graph;
  private CCachedExpressionTree m_tree;

  @Before
  public void setUp() throws FileReadException, CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    m_graph = ZyGraphFactory.generateTestGraph();
    m_tree = new CCachedExpressionTree();
  }

  /**
   * This test makes sure that empty criteria trees can not be evaluated.
   */
  @Test(expected = IllegalStateException.class)
  public void testEmptyCriteriumTree() {
    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  /**
   * This test makes sure that criteria trees with AND expressions with fewer than two children can
   * not be evaluated..
   */
  @Test(expected = IllegalStateException.class)
  public void testInvalidAndCriterion1() {
    final CCachedAndCriterium criterium = new CCachedAndCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidAndCriterion2() {
    final CCachedAndCriterium criterium = new CCachedAndCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidCriterionNumber() {
    final CCachedColorCriterium criterium2 = new CCachedColorCriterium(Color.BLUE);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node2);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node3);

    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidNotCriterion() {
    final CCachedNotCriterium criterium = new CCachedNotCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedTextCriterium criterium2 = new CCachedTextCriterium("nop", false, false);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(node, node2);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.BLUE);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  /**
   * This test makes sure that criteria trees with OR expressions with fewer than two children can
   * not be evaluated..
   */
  @Test(expected = IllegalStateException.class)
  public void testInvalidOrCriterion1() {
    final CCachedOrCriterium criterium = new CCachedOrCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidOrCriterion2() {
    final CCachedOrCriterium criterium = new CCachedOrCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    CCriteriumExecuter.execute(m_tree, m_graph);
  }

  /**
   * This test makes sure that AND criteria are correctly evaluated (TRUE case).
   */
  @Test
  public void testMatchingAndCriterion() {
    final CCachedAndCriterium criterium = new CCachedAndCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    final CCachedTextCriterium criterium2 = new CCachedTextCriterium("nop", false, false);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(node, node2);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(1, m_graph.getSelectedNodes().size());
  }

  /**
   * This test makes sure that NOT criteria are correctly evaluated (TRUE case).
   */
  @Test
  public void testMatchingNotCriterion() {
    final CCachedNotCriterium criterium = new CCachedNotCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(2, m_graph.getSelectedNodes().size());
  }

  /**
   * This test makes sure that OR criteria are correctly evaluated (TRUE case).
   */
  @Test
  public void testMatchingOrCriterion() {
    final CCachedOrCriterium criterium = new CCachedOrCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    final CCachedTextCriterium criterium2 = new CCachedTextCriterium("mov", false, false);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(node, node2);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(2, m_graph.getSelectedNodes().size());
  }

  @Test
  public void testSingleColorCriterion() {
    final CCachedColorCriterium criterium = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(1, m_graph.getSelectedNodes().size());
    assertTrue(m_graph.getRawView().getGraph().getNodes().get(1).isSelected());
  }

  @Test
  public void testSingleTextCriterion() {
    final CCachedTextCriterium criterium = new CCachedTextCriterium("mov", false, false);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(1, m_graph.getSelectedNodes().size());
  }

  @Test
  public void testSingleTagCriterion() {

    final Set<CTag> tags = new HashSet<CTag>();
    for (final NaviNode node : m_graph.getNodes()) {
      if (node.getRawNode().isTagged()) {
        tags.addAll(node.getRawNode().getTags());
      }
    }

    assertEquals(1, tags.size());

    final CCachedTagCriterium criterium =
        new CCachedTagCriterium(false, Iterables.getOnlyElement(tags));
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(1, m_graph.getSelectedNodes().size());
  }

  @Test
  public void testSingleInDegreeCriterion() {
    final CCachedIndegreeCriterium criterium = new CCachedIndegreeCriterium("=", 1);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(2, m_graph.getSelectedNodes().size());
  }

  @Test
  public void testSingleOutDegreeCriterion() {
    final CCachedOutdegreeCriterium criterium = new CCachedOutdegreeCriterium("=", 1);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(2, m_graph.getSelectedNodes().size());
  }

  @Test
  public void testSingleVisibilityCriterionUnvisible() {
    final CCachedVisibilityCriterium criterium =
        new CCachedVisibilityCriterium(VisibilityState.UNVISIBLE);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(1, m_graph.getSelectedNodes().size());

  }

  @Test
  public void testSingleVisibilityCriterionVisible() {
    final CCachedVisibilityCriterium criterium =
        new CCachedVisibilityCriterium(VisibilityState.VISIBLE);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(m_graph.getNodes().size() - 1, m_graph.getSelectedNodes().size());

  }

  /**
   * This test checks if a node that is selected also is selected after criterium executer has been
   * run.
   */
  @Test
  public void testSingleSelectionCriterionSelected() {

    assertEquals(0, m_graph.getSelectedNodes().size());
    m_graph.getRawView().getGraph().getNodes().get(1).setSelected(true);
    assertEquals(1, m_graph.getSelectedNodes().size());

    final CCachedSelectionCriterium criterium =
        new CCachedSelectionCriterium(SelectionState.SELECTED);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(1, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(1, m_graph.getSelectedNodes().size());
    assertTrue(m_graph.getRawView().getGraph().getNodes().get(1).isSelected());
  }

  @Test
  public void testSingleSelectionCriterionUnselected() {

    assertEquals(0, m_graph.getSelectedNodes().size());
    m_graph.getRawView().getGraph().getNodes().get(1).setSelected(true);
    assertEquals(1, m_graph.getSelectedNodes().size());

    final CCachedSelectionCriterium criterium =
        new CCachedSelectionCriterium(SelectionState.UNSELECTED);
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    assertEquals(1, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(m_graph.getNodes().size() - 1, m_graph.getSelectedNodes().size());
    assertFalse(m_graph.getRawView().getGraph().getNodes().get(1).isSelected());
  }

  /**
   * This test makes sure that AND criteria are correctly evaluated (FALSE case).
   */
  @Test
  public void testValidAndCriterion() {
    final CCachedAndCriterium criterium = new CCachedAndCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.RED);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    final CCachedColorCriterium criterium2 = new CCachedColorCriterium(Color.BLUE);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(node, node2);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(0, m_graph.getSelectedNodes().size());
  }

  /**
   * This test makes sure that NOT criteria are correctly evaluated (FALSE case).
   */
  @Test
  public void testValidNotCriterion() {
    final CCachedNotCriterium criterium = new CCachedNotCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedTextCriterium criterium2 = new CCachedTextCriterium("nop", false, false);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(node, node2);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(2, m_graph.getSelectedNodes().size());
  }

  /**
   * This test makes sure that OR criteria are correctly evaluated (FALSE case).
   */
  @Test
  public void testValidOrCriterion() {
    final CCachedOrCriterium criterium = new CCachedOrCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(m_tree.getRoot(), node);

    final CCachedColorCriterium criterium3 = new CCachedColorCriterium(Color.GREEN);
    final CCachedExpressionTreeNode node3 = new CCachedExpressionTreeNode(criterium3);
    CCachedExpressionTreeNode.append(node, node3);

    final CCachedColorCriterium criterium2 = new CCachedColorCriterium(Color.BLUE);
    final CCachedExpressionTreeNode node2 = new CCachedExpressionTreeNode(criterium2);
    CCachedExpressionTreeNode.append(node, node2);

    assertEquals(0, m_graph.getSelectedNodes().size());

    CCriteriumExecuter.execute(m_tree, m_graph);

    assertEquals(0, m_graph.getSelectedNodes().size());
  }
}
