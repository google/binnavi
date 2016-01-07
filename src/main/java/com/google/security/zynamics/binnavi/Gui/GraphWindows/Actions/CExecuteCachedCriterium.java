/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CCriteriumExecuter;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTree;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class that can be used to execute previously cached Select by Criteria expression trees.
 */
public final class CExecuteCachedCriterium extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -656538362058526164L;

  /**
   * The graph on which the expression operates.
   */
  private final ZyGraph m_graph;

  /**
   * The expression tree that specifies the selection criterium.
   */
  private final CCachedExpressionTree m_tree;

  /**
   * Creates a new action object.
   *
   * @param graph The graph on which the expression operates.
   * @param tree The expression tree that specifies the selection criterium.
   */
  public CExecuteCachedCriterium(final ZyGraph graph, final CCachedExpressionTree tree) {
    super(tree.getFormulaString());

    m_graph = graph;
    m_tree = tree;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CCriteriumExecuter.execute(m_tree, m_graph);
  }
}
