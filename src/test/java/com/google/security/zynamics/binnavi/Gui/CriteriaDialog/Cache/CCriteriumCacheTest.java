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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache.CCriteriumCache;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache.ICriteriumCacheListener;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.CCachedAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor.CCachedColorCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text.CCachedTextCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;


@RunWith(JUnit4.class)
public class CCriteriumCacheTest {
  ICriteriumCacheListener m_listener = new ICriteriumCacheListener() {
    @Override
    public void changedCriteria(final CCriteriumCache criteriumCache) {
    }
  };

  @Test
  public void testAdd() {
    final CCachedExpressionTree cachedTree = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree2 = new CCachedExpressionTree();

    final CCachedAndCriterium criterium = new CCachedAndCriterium();
    final CCachedExpressionTreeNode node = new CCachedExpressionTreeNode(criterium);
    CCachedExpressionTreeNode.append(cachedTree.getRoot(), node);

    final CCachedExpressionTreeNode node_a =
        new CCachedExpressionTreeNode(new CCachedColorCriterium(Color.red));
    CCachedExpressionTreeNode.append(node, node_a);

    final CCachedExpressionTreeNode node_b =
        new CCachedExpressionTreeNode(new CCachedColorCriterium(Color.blue));
    CCachedExpressionTreeNode.append(node, node_b);

    final CCachedExpressionTreeNode node2 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("foo", false, false));
    CCachedExpressionTreeNode.append(cachedTree2.getRoot(), node2);
    final CCriteriumCache criteriumCache = new CCriteriumCache();
    criteriumCache.addListener(m_listener);

    assertEquals(0, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree);

    assertEquals(1, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree2);

    assertEquals(2, criteriumCache.getTrees().size());
  }

  @Test
  public void testMoreThenEnoughTrees() {
    final CCachedExpressionTree cachedTree = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree2 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree3 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree4 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree5 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree6 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree7 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree8 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree9 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree10 = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree11 = new CCachedExpressionTree();

    final CCachedExpressionTreeNode node =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("1", false, false));
    CCachedExpressionTreeNode.append(cachedTree.getRoot(), node);

    final CCachedExpressionTreeNode node2 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("2", false, false));
    CCachedExpressionTreeNode.append(cachedTree2.getRoot(), node2);

    final CCachedExpressionTreeNode node3 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("3", false, false));
    CCachedExpressionTreeNode.append(cachedTree3.getRoot(), node3);

    final CCachedExpressionTreeNode node4 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("4", false, false));
    CCachedExpressionTreeNode.append(cachedTree4.getRoot(), node4);

    final CCachedExpressionTreeNode node5 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("5", false, false));
    CCachedExpressionTreeNode.append(cachedTree5.getRoot(), node5);

    final CCachedExpressionTreeNode node6 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("6", false, false));
    CCachedExpressionTreeNode.append(cachedTree6.getRoot(), node6);

    final CCachedExpressionTreeNode node7 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("7", false, false));
    CCachedExpressionTreeNode.append(cachedTree7.getRoot(), node7);

    final CCachedExpressionTreeNode node8 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("8", false, false));
    CCachedExpressionTreeNode.append(cachedTree8.getRoot(), node8);

    final CCachedExpressionTreeNode node9 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("9", false, false));
    CCachedExpressionTreeNode.append(cachedTree9.getRoot(), node9);

    final CCachedExpressionTreeNode node10 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("10", false, false));
    CCachedExpressionTreeNode.append(cachedTree10.getRoot(), node10);

    final CCachedExpressionTreeNode node11 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("11", false, false));
    CCachedExpressionTreeNode.append(cachedTree11.getRoot(), node11);

    final CCriteriumCache criteriumCache = new CCriteriumCache();
    criteriumCache.addListener(m_listener);

    assertEquals(0, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree);
    assertEquals(1, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree2);
    assertEquals(2, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree3);
    assertEquals(3, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree4);
    assertEquals(4, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree5);
    assertEquals(5, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree6);
    assertEquals(6, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree7);
    assertEquals(7, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree8);
    assertEquals(8, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree9);
    assertEquals(9, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree10);
    assertEquals(10, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree11);
    assertEquals(10, criteriumCache.getTrees().size());
  }

  @Test
  public void testRemoveListener() {
    final CCachedExpressionTree cachedTree = new CCachedExpressionTree();

    final CCachedExpressionTreeNode node =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("foo", false, false));
    CCachedExpressionTreeNode.append(cachedTree.getRoot(), node);

    final CCriteriumCache criteriumCache = new CCriteriumCache();
    criteriumCache.addListener(m_listener);

    assertEquals(0, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree);

    assertEquals(1, criteriumCache.getTrees().size());

    criteriumCache.removeListener(m_listener);
  }

  @Test
  public void testSameTree() {
    final CCachedExpressionTree cachedTree = new CCachedExpressionTree();
    final CCachedExpressionTree cachedTree2 = new CCachedExpressionTree();

    final CCachedExpressionTreeNode node =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("foo", false, false));
    CCachedExpressionTreeNode.append(cachedTree.getRoot(), node);

    final CCachedExpressionTreeNode node2 =
        new CCachedExpressionTreeNode(new CCachedTextCriterium("foo", false, false));
    CCachedExpressionTreeNode.append(cachedTree2.getRoot(), node2);

    final CCriteriumCache criteriumCache = new CCriteriumCache();
    criteriumCache.addListener(m_listener);

    assertEquals(0, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree);

    assertEquals(1, criteriumCache.getTrees().size());

    criteriumCache.add(cachedTree2);

    assertEquals(1, criteriumCache.getTrees().size());
  }
}
