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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphOpener;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;


/**
 * Action class used to open the original function of a code node. This action can be used to
 * quickly get back to the original function when working with a code node in another context.
 */
public final class COpenOriginalFunction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3054835777427924106L;

  /**
   * Parent window used for dialogs.
   */
  private final CGraphWindow m_parent;

  /**
   * Container that contains the function to open.
   */
  private final IViewContainer m_container;

  /**
   * The function to open.
   */
  private final INaviFunction m_function;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param container Container that contains the function to open.
   * @param function The function to open.
   */
  public COpenOriginalFunction(
      final CGraphWindow parent, final IViewContainer container, final INaviFunction function) {
    super(String.format("Open original function '%s'", function.getName()));

    m_parent = parent;
    m_container = container;
    m_function = function;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphOpener.showFunction(m_parent, m_container, m_function);
  }
}
