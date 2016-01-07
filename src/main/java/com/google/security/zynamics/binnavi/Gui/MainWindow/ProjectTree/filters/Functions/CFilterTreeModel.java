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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Tree model class for tag filter trees.
 */
public final class CFilterTreeModel implements TreeModel {
  /**
   * Root tag of the tag hierarchy.
   */
  private final ITreeNode<CTag> rootTag;
  private final EventListenerList listeners = new EventListenerList();

  /**
   * Maps between tags and the nodes in the tag filter tree.
   */
  private final Map<ITreeNode<CTag>, CTagFilterNode> tree = new HashMap<>();

  /**
   * Creates a new model object.
   *
   * @param rootTag Root tag of the tag hierarchy.
   */
  public CFilterTreeModel(final ITreeNode<CTag> rootTag) {
    this.rootTag = rootTag;
    createTree(rootTag);
  }

  /**
   * Fills the tree map.
   *
   * @param tag The next tag to process.
   */
  private void createTree(final ITreeNode<CTag> tag) {
    tree.put(tag, new CTagFilterNode(tag));
    for (final ITreeNode<CTag> child : tag.getChildren()) {
      createTree(child);
    }
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    final CTagFilterNode p = (CTagFilterNode) parent;
    return tree.get(p.getTag().getChildren().get(index));
  }

  @Override
  public int getChildCount(final Object parent) {
    final CTagFilterNode p = (CTagFilterNode) parent;
    return p.getTag().getChildren().size();
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    final CTagFilterNode p = (CTagFilterNode) parent;
    final CTagFilterNode c = (CTagFilterNode) child;
    return p.getTag().getChildren().indexOf(c.getTag());
  }

  @Override
  public Object getRoot() {
    return tree.get(rootTag);
  }

  @Override
  public boolean isLeaf(final Object node) {
    final CTagFilterNode n = (CTagFilterNode) node;
    return n.getTag().getChildren().isEmpty();
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object object) {}

  @Override
  public void addTreeModelListener(final TreeModelListener newListener) {
    listeners.add(TreeModelListener.class, newListener);
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener removeListener) {
    listeners.remove(TreeModelListener.class, removeListener);
  }
}
