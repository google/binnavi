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
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;

/**
 * Action that can be used to load address spaces.
 */
public final class CLoadAddressSpaceAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8781428465526314731L;

  /**
   * Tree to be updated after the address spaces were loaded.
   */
  private final JTree m_tree;

  /**
   * The address spaces to load.
   */
  private final INaviAddressSpace[] m_spaces;

  /**
   * Creates a new action object.
   * 
   * @param tree Tree to be updated after the address spaces were loaded.
   * @param spaces The address spaces to load.
   */
  public CLoadAddressSpaceAction(final JTree tree, final INaviAddressSpace[] spaces) {
    super("Load Address Space");

    m_tree = Preconditions.checkNotNull(tree, "IE01898: Tree argument can not be null");
    m_spaces =
        Preconditions.checkNotNull(spaces, "IE01899: Address spaces argument can't be null")
            .clone();

    for (final INaviAddressSpace addressSpace : spaces) {
      Preconditions.checkNotNull(addressSpace,
          "IE01900: Address spaces list contains a null-element");
    }

    putValue(ACCELERATOR_KEY, HotKeys.LOAD_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_LOAD_ADDRESS_SPACE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CAddressSpaceFunctions.loadAddressSpaces(m_tree, m_spaces);
  }
}
