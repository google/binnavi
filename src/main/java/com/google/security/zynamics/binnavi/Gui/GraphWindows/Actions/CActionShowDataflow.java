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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Action class for creating data flow graphs.
 */
public final class CActionShowDataflow extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3300675538631018022L;

  /**
   * Window where the new view is shown.
   */
  private final CGraphWindow m_parent;

  /**
   * Container where the new view is created.
   */
  private final IViewContainer m_container;

  /**
   * The view whose data flow graph is created.
   */
  private final INaviView m_view;

  /**
   * Creates a new action object.
   *
   * @param parent Window where the new view is shown.
   * @param container Container where the new view is created.
   * @param view The view whose data flow graph is created.
   */
  public CActionShowDataflow(
      final CGraphWindow parent, final IViewContainer container, final INaviView view) {
    super("Show Dataflow graph");

    m_parent = Preconditions.checkNotNull(parent, "IE01647: Parent can not be null");
    m_container =
        Preconditions.checkNotNull(container, "IE01649: Container argument can not be null");
    m_view = Preconditions.checkNotNull(view, "IE02298: View argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphFunctions.showDataflowGraph(m_parent, m_container, m_view);
  }
}
