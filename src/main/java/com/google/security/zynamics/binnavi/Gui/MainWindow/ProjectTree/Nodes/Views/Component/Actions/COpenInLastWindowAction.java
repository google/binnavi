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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CShowViewFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Action class used to open a number of views in the last open window.
 */
public final class COpenInLastWindowAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7167187997929184030L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * Context in which the views are opened.
   */
  private final IViewContainer m_container;

  /**
   * Views to be opened.
   */
  private final INaviView[] m_views;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param container Context in which the views are opened.
   * @param views Views to be opened.
   */
  public COpenInLastWindowAction(final Window parent, final IViewContainer container,
      final INaviView[] views) {
    super("Open in last window");

    m_parent = Preconditions.checkNotNull(parent, "IE02876: parent argument can not be null");
    m_container =
        Preconditions.checkNotNull(container, "IE02877: container argument can not be null");
    m_views = Preconditions.checkNotNull(views, "IE02878: views argument can not be null").clone();

    putValue(ACCELERATOR_KEY, HotKeys.LOAD_LAST_WINDOW_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CShowViewFunctions.showViewInLastWindow(m_parent, m_container, m_views);
  }
}
