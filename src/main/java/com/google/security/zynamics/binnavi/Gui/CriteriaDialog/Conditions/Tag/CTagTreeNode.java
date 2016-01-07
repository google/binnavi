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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.Icon;
import javax.swing.ImageIcon;



/**
 * Nodes shown in the tag trees.
 */
public class CTagTreeNode extends IconNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7643218675064344763L;

  /**
   * Icon used for non-root tags.
   */
  private static final ImageIcon ICON_GREEN_TAG =
      new ImageIcon(CMain.class.getResource("data/nodetaggingtreeicons/green_tag.png"));

  /**
   * Icon used for root tags.
   */
  private static final ImageIcon ICON_GREEN_ROOTTAG =
      new ImageIcon(CMain.class.getResource("data/nodetaggingtreeicons/green_roottag.png"));

  /**
   * Tag represented by this node.
   */
  private final ITreeNode<CTag> m_tagNode;

  /**
   * Creates a new node object.
   *
   * @param tagNode Tag represented by this node.
   */
  public CTagTreeNode(final ITreeNode<CTag> tagNode) {
    m_tagNode = tagNode;

    createChildren();
  }

  /**
   * Creates the child nodes of the node.
   */
  private void createChildren() {
    for (final ITreeNode<CTag> child : m_tagNode.getChildren()) {
      add(new CTagTreeNode(child));
    }
  }

  @Override
  public Icon getIcon() {
    if (getLevel() == 1) {
      return ICON_GREEN_ROOTTAG;
    }

    return ICON_GREEN_TAG;
  }

  /**
   * Returns the tag represented by this node.
   *
   * @return The tag represented by this node.
   */
  public CTag getTag() {
    return m_tagNode.getObject();
  }

  @Override
  public String toString() {
    return m_tagNode.getObject().getName();
  }
}
