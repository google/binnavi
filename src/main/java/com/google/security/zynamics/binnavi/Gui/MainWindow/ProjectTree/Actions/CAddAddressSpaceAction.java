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



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.INodeSelectionUpdater;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Action that can be used to add a new address space with a default name to a project.
 */
public final class CAddAddressSpaceAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1364240871843263284L;

  /**
   * Parent window used for dialogs.
   */
  private final JComponent m_parent;

  /**
   * Parent window used for dialogs.
   */
  private final INaviProject m_project;

  /**
   * Refreshes the project tree after executing the action.
   */
  private final INodeSelectionUpdater m_updater;

  /**
   * Creates a new add address space action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param project The project the address space is added to.
   * @param updater Refreshes the project tree after executing the action.
   */
  public CAddAddressSpaceAction(final JComponent parent, final INaviProject project,
      final INodeSelectionUpdater updater) {
    super("Add address space");
    m_parent = Preconditions.checkNotNull(parent, "IE01849: Parent argument can't be null");
    m_project = Preconditions.checkNotNull(project, "IE01850: Project argument can't be null");
    m_updater = Preconditions.checkNotNull(updater, "IE02335: Updater argument can not be null");

    putValue(ACCELERATOR_KEY, HotKeys.ADDRESS_SPACE_SELECTION_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_ADD_ADDRESSSPACE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CProjectFunctions.addAddressSpace(SwingUtilities.getWindowAncestor(m_parent), m_project,
        m_updater);
  }
}
