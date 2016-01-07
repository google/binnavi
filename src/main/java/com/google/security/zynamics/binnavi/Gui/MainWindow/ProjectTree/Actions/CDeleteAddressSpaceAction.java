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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Action that can be used to delete address spaces from projects.
 */
public final class CDeleteAddressSpaceAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6752720753654671256L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Project the address space belongs to.
   */
  private final INaviProject m_project;

  /**
   * Address space to be deleted.
   */
  private final INaviAddressSpace[] m_addressSpace;

  /**
   * Updates the project tree after the action was executed.
   */
  private final ITreeUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param project Project from which the address spaces are deleted.
   * @param spaces Address spaces to be deleted.
   * @param updater Updates the project tree after the action was executed.
   */
  public CDeleteAddressSpaceAction(final JFrame parent, final INaviProject project,
      final INaviAddressSpace[] spaces, final ITreeUpdater updater) {
    super("Delete Address Space");

    m_parent = Preconditions.checkNotNull(parent, "IE01867: Parent argument can't be null");
    m_project = Preconditions.checkNotNull(project, "IE01868: Project argument can't be null");
    m_addressSpace =
        Preconditions.checkNotNull(spaces, "IE01869: Addres spaces argument can't be null").clone();
    m_updater = Preconditions.checkNotNull(updater, "IE02338: Updater argument can not be null");

    putValue(ACCELERATOR_KEY, HotKeys.DELETE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_DELETE_ADDRESS_SPACE".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CProjectFunctions.removeAddressSpace(m_parent, m_project, m_addressSpace, m_updater);
  }
}
