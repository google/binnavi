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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSaver;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Action used for cloning a view.
 */
public final class CActionClone extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5056920635632555488L;

  /**
   * Window where the new view is shown.
   */
  private final CGraphWindow m_parent;

  /**
   * View to be cloned.
   */
  private final INaviView m_view;

  /**
   * Container where the new view is stored.
   */
  private final IViewContainer m_container;

  /**
   * Creates a new action object.
   *
   * @param parent Window where the new view is shown.
   * @param view View to be cloned.
   * @param container Container where the new view is stored.
   */
  public CActionClone(
      final CGraphWindow parent, final INaviView view, final IViewContainer container) {
    super("Clone View");

    m_parent = Preconditions.checkNotNull(parent, "IE01641: Parent can't be null");
    m_view = Preconditions.checkNotNull(view, "IE01642: View argument can not be null");
    m_container =
        Preconditions.checkNotNull(container, "IE01643: Container argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    CGraphSaver.clone(m_parent, m_view, m_container);
  }
}
