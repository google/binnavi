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

/**
 * Action class used for editing name and description of node tags.
 */
public final class CEditTagAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2794171930752365528L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The tag to delete.
   */
  private final CTag m_tag;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param tag The tag to delete.
   */
  public CEditTagAction(final JFrame parent, final CTag tag) {
    super("Edit Tag Description");

    m_parent = Preconditions.checkNotNull(parent, "IE01217: Parent argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE01782: Tag can't be null.");
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    CTagFunctions.editTag(m_parent, m_tag);
  }
}
