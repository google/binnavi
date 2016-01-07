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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations.CGraphDialogs;


/**
 * Action handler that's executed when the user wants to add a comment.
 */
public final class CActionChangeViewDescription extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3752161453514048951L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * View whose description is changed.
   */
  private final INaviView m_view;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param view View whose description is changed.
   */
  public CActionChangeViewDescription(final JFrame parent, final INaviView view) {
    super("Change View Description");
    m_parent = Preconditions.checkNotNull(parent, "IE02810: parent argument can not be null");
    m_view = Preconditions.checkNotNull(view, "IE02811: view argument can not be null");
    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_CHANGE_VIEW_DESCRIPTION_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CGraphDialogs.showViewDescriptionDialog(m_parent, m_view);
  }
}
