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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CTagFunctions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

/**
 * Action class that can be used to delete an entire subtree of tags.
 */
public final class CDeleteTagSubtreeAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2817976106266766090L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tag manager from which the subtree is deleted.
   */
  private final ITagManager m_tagManager;

  /**
   * Root node of the subtree to be deleted.
   */
  private final TreeNode<CTag> m_tag;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag manager from which the subtree is deleted.
   * @param tag Root node of the subtree to be deleted.
   */
  public CDeleteTagSubtreeAction(final JFrame parent, final ITagManager tagManager,
      final TreeNode<CTag> tag) {
    super("Delete Subtree");

    m_parent = Preconditions.checkNotNull(parent, "IE01893: Parent argument can not be null");
    m_tagManager =
        Preconditions.checkNotNull(tagManager, "IE01894: Tag manager argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE01895: Tag argument can not be null");

    putValue(MNEMONIC_KEY, (int) "HK_MENU_DELETE_TAG_SUBTREE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagFunctions.deleteTagSubtree(m_parent, m_tagManager, m_tag);
  }
}
