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



import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CTagFunctions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;



/**
 * Action that can be used to insert new tags into a new tag manager.
 */
public final class CInsertTagAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8649696035201341391L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tag manager where the tag is inserted.
   */
  private final ITagManager m_tagManager;

  /**
   * Parent tag of the new tag.
   */
  private final TreeNode<CTag> m_parentTag;

  /**
   * Name of the new tag.
   */
  private final String m_name;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag manager where the tag is inserted.
   * @param tag Parent tag of the new tag.
   * @param name Name of the new tag.
   */
  public CInsertTagAction(final JFrame parent, final ITagManager tagManager,
      final TreeNode<CTag> tag, final String name) {
    super("Insert Tag");

    m_parent = parent;
    m_tagManager = tagManager;
    m_parentTag = tag;
    m_name = name;

    putValue(MNEMONIC_KEY, (int) "HK_MENU_INSERT_TAG".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagFunctions.insertTag(m_parent, m_tagManager, m_parentTag, m_name);
  }
}
