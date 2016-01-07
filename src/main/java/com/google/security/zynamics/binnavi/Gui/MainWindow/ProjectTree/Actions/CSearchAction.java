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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;



import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CViewSearcher;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action that can be used to search for views in a module or project.
 */
public final class CSearchAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6782301260180411109L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * Parent component used for dialogs.
   */
  private final JComponent m_parentComponent;

  /**
   * Container to search through.
   */
  private final IViewContainer m_container;

  /**
   * The initial address to search for. This value can be null.
   */
  private IAddress m_address;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent component used for dialogs.
   * @param container Container to search through.
   */
  public CSearchAction(final JComponent parent, final IViewContainer container) {
    super("Search View");

    checkArguments(parent);

    m_container =
        Preconditions.checkNotNull(container, "IE01149: Project argument can not be null");
    m_parent = null;
    m_parentComponent = parent;

    putValue(MNEMONIC_KEY, (int) "HK_MENU_SEARCH_VIEW".charAt(0));
  }

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param container Container to search through.
   * @param address The initial address to search for. This argument can be null.
   */
  public CSearchAction(final Window parent, final IViewContainer container, final IAddress address) {
    super("Search View");

    checkArguments(parent);

    Preconditions.checkNotNull(container, "IE01918: Container argument can not be null");

    m_parent = parent;
    m_parentComponent = null;
    m_container = container;
    m_address = address;

    putValue(MNEMONIC_KEY, (int) "HK_MENU_SEARCH_VIEW".charAt(0));
  }

  /**
   * Checks whether the arguments are null and throws an exception if they are null.
   * 
   * @param parent Parent window used for dialogs.
   */
  private static void checkArguments(final Object parent) {
    Preconditions.checkNotNull(parent, "IE01920: Parent argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Window parent =
        m_parent == null ? SwingUtilities.getWindowAncestor(m_parentComponent) : m_parent;

    CViewSearcher.searchView(parent, m_container, m_address);
  }
}
