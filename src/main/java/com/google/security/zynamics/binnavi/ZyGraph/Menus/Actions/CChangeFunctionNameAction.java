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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations.CGraphDialogs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Action class used to change the name of a view.
 */
public class CChangeFunctionNameAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2039879505043721900L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_window;

  /**
   * View whose name is changed.
   */
  private final INaviView m_view;

  /**
   * Creates a new action object.
   *
   * @param window Parent window used for dialogs.
   * @param view View whose name is changed.
   */
  public CChangeFunctionNameAction(final JFrame window, final INaviView view) {
    super(String.format("Change function name '%s'", view.getName()));

    m_window = window;
    m_view = view;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphDialogs.showViewDescriptionDialog(m_window, m_view);
  }
}
