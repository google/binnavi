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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Action that can be used to remove modules from address spaces.
 */
public final class CRemoveModuleAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2720370876742985692L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Address spaces the modules are removed from.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * The modules to be removed.
   */
  private final INaviModule[] m_modules;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param addressSpace Address spaces the modules are removed from.
   * @param modules The modules to be removed.
   */
  public CRemoveModuleAction(final JFrame parent, final INaviAddressSpace addressSpace,
      final INaviModule[] modules) {
    super("Remove Module");

    m_parent = Preconditions.checkNotNull(parent, "IE01911: Parent argument can't be null");
    m_addressSpace =
        Preconditions.checkNotNull(addressSpace, "IE01912: Address space argument can't be null");
    m_modules = Preconditions.checkNotNull(modules, "IE01913: Modules argument can't be null");

    for (final INaviModule module : modules) {
      Preconditions.checkNotNull(module, "IE01914: Modules list contains a null-element");
    }

    putValue(ACCELERATOR_KEY, HotKeys.DELETE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_REMOVE_MODULE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CAddressSpaceFunctions.removeModules(m_parent, m_addressSpace, m_modules);
  }
}
