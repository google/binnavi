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
 * Action class used to delete node tags.
 */
public final class CDeleteTagAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6650512354129676379L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tag manager from which the tag is deleted.
   */
  private final ITagManager m_tagManager;

  /**
   * The tag to delete.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag manager from which the tag is deleted.
   * @param tag The tag to delete.
   */
  public CDeleteTagAction(
      final JFrame parent, final ITagManager tagManager, final ITreeNode<CTag> tag) {
    super("Delete Tag");

    m_parent = Preconditions.checkNotNull(parent, "IE01217: Parent argument can not be null");
    m_tagManager =
        Preconditions.checkNotNull(tagManager, "IE01218: Tag manager argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE01782: Tag can't be null.");
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    CTagFunctions.deleteTag(m_parent, m_tagManager, m_tag);
  }
}
