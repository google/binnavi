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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions;



import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CViewFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Action class for renaming a function back to its original name.
 */
public class CRenameBackAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8396702921696088859L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * The view to rename.
   */
  private final INaviView m_view;

  /**
   * The original name of the view.
   */
  private final String m_originalName;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param view The view to rename.
   * @param originalName The original name of the view.
   */
  public CRenameBackAction(final Window parent, final INaviView view, final String originalName) {
    super(String.format("Rename back to '%s'", originalName));

    m_parent = parent;
    m_view = view;
    m_originalName = originalName;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CViewFunctions.renameBack(m_parent, m_view, m_originalName);
  }
}
