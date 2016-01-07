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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphOpener;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Action class used to show functions in graph windows.
 */
public final class COpenFunctionAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3211341806797384109L;

  /**
   * Window in which the function view is shown.
   */
  private final CGraphWindow m_parent;

  /**
   * View container that provides the context of the view.
   */
  private final IViewContainer m_container;

  /**
   * Function to be opened.
   */
  private final INaviFunction m_function;

  /**
   * Creates a new action object.
   *
   * @param parent Window in which the function view is shown.
   * @param container View container that provides the context of the view.
   * @param function Function to be opened.
   */
  public COpenFunctionAction(
      final CGraphWindow parent, final IViewContainer container, final INaviFunction function) {
    super(String.format("Open function %s", function.getName()));

    Preconditions.checkNotNull(parent, "IE02163: Parent argument can not be null");

    Preconditions.checkNotNull(container, "IE02164: Container argument can not be null");

    m_parent = parent;
    m_container = container;
    m_function = function;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphOpener.showFunction(m_parent, m_container, m_function);
  }

}
