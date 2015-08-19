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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTreeNode;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.util.List;


public class SetupCriteriaGraphTest {

  public void setUp() throws CouldntLoadDataException, LoadCancelledException, FileReadException,
      CouldntSaveDataException {
    final ZyGraph graph = ZyGraphFactory.generateTestGraph();

    final CCriteriaFactory criteriaFactory = new CCriteriaFactory(graph, null, null);

    final List<ICriteriumCreator> criteria = criteriaFactory.getConditions();

    final CCriteriumTree m_ctree = new CCriteriumTree();
    final JCriteriumTree jtree = new JCriteriumTree(m_ctree, criteria);
    jtree.getModel().setRoot(
        new JCriteriumTreeNode(m_ctree, m_ctree.getRoot().getCriterium(), criteria));

    final CCriteriumTreeNode parent = new CCriteriumTreeNode(null);
    final CCriteriumTreeNode child = new CCriteriumTreeNode(null);
    m_ctree.appendNode(parent, child);
  }
}
