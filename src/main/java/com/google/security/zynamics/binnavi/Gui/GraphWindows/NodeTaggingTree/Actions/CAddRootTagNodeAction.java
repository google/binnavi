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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTagFunctions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Action class used to create a new root node tag.
 */
public final class CAddRootTagNodeAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8245688852710059696L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tag managed where the new tag is created.
   */
  private final ITagManager m_tagManager;

  /**
   * Parent tag of the new tag.
   */
  private final ITreeNode<CTag> m_parentTag;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag managed where the new tag is created.
   * @param parentTag Parent tag of the new tag.
   */
  public CAddRootTagNodeAction(
      final JFrame parent, final ITagManager tagManager, final ITreeNode<CTag> parentTag) {
    super("Create Root Tag");

    m_parent = Preconditions.checkNotNull(parent, "IE01780: Parent argument can not be null.");
    m_tagManager =
        Preconditions.checkNotNull(tagManager, "IE02309: Tag manager arguemnt can not be null");
    m_parentTag =
        Preconditions.checkNotNull(parentTag, "IE02310: Parent tag arguemnt can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagFunctions.appendTag(m_parent, m_tagManager, m_parentTag);
  }
}
