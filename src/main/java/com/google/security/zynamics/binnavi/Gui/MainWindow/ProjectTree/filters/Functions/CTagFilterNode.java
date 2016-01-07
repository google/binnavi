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

import javax.swing.tree.DefaultMutableTreeNode;



/**
 * Tree node for the tag filter panels.
 */
public final class CTagFilterNode extends DefaultMutableTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4940295831566036107L;

  /**
   * The tag represented by the node.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Creates a new node object.
   * 
   * @param tag The tag represented by the node.
   */
  public CTagFilterNode(final ITreeNode<CTag> tag) {
    m_tag = tag;
  }

  /**
   * Returns the tag represented by the node.
   * 
   * @return The tag represented by the node.
   */
  public ITreeNode<CTag> getTag() {
    return m_tag;
  }

  @Override
  public String toString() {
    return m_tag.getObject().getName();
  }
}
