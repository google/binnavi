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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CReilConverter;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Action class for showing a REIL code view.
 */
public final class CActionShowReil extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2478803857892102988L;

  /**
   * Window in which the REIL graph is shown.
   */
  private final CGraphWindow m_parent;

  /**
   * Context in which the REIL view is generated.
   */
  private final IViewContainer m_container;

  /**
   * Module where the view is created.
   */
  private final INaviModule m_module;

  /**
   * View to be converted to REIL code.
   */
  private final INaviView m_view;

  /**
   * Creates a new action object.
   *
   * @param parent Window in which the REIL graph is shown.
   * @param container Context in which the REIL view is generated.
   * @param module Module where the view is created.
   * @param view View to be converted to REIL code.
   */
  public CActionShowReil(final CGraphWindow parent, final IViewContainer container,
      final INaviModule module, final INaviView view) {
    super("Show REIL Code");

    m_parent = Preconditions.checkNotNull(parent, "IE01650: Parent can't be null");
    m_container =
        Preconditions.checkNotNull(container, "IE01652: Container argument can not be null");
    m_module = Preconditions.checkNotNull(module, "IE02299: Module argument can not be null");
    m_view = Preconditions.checkNotNull(view, "IE02300: View argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CReilConverter.showReilGraph(m_parent, m_container, m_module, m_view);
  }
}
