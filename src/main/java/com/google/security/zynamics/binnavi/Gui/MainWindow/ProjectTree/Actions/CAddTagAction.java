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
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Action to create a new tag.
 */
public final class CAddTagAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8806586451554197431L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tag manager the tag is added to.
   */
  private final ITagManager m_tagManager;

  /**
   * Parent tag of the new tag.
   */
  private final ITreeNode<CTag> m_parentTag;

  /**
   * Name of the new tag.
   */
  private final String m_name;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag manager the tag is added to.
   * @param tag Parent tag of the new tag.
   * @param name Name of the new tag.
   */
  public CAddTagAction(final JFrame parent, final ITagManager tagManager,
      final ITreeNode<CTag> tag, final String name) {
    super(tag.getObject().getId() == 0 ? "Create Root Tag" : "Append Tag");

    m_parent = Preconditions.checkNotNull(parent, "IE01852: Parent argument can not be null");
    m_tagManager =
        Preconditions.checkNotNull(tagManager, "IE01853: Tag manager argument can not be null");
    m_name = Preconditions.checkNotNull(name, "IE01854: Name argument can not be null");
    m_parentTag = Preconditions.checkNotNull(tag, "IE02336: Parent Tag argument can not be null");

    putValue(MNEMONIC_KEY, tag.getObject().getId() == 0 ? (int) "HK_MENU_CREATE_TAG".charAt(0)
        : (int) "HK_MENU_APPEND_TAG".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagFunctions.addTag(m_parent, m_tagManager, m_parentTag, m_name);
  }
}
