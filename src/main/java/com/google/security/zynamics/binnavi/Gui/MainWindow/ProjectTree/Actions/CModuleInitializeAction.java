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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CModuleInitializationFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

public class CModuleInitializeAction extends AbstractAction {
  private final JTree m_projectTree;

  private final INaviModule[] m_modules;

  public CModuleInitializeAction(final JTree projectTree, final INaviModule[] modules) {
    super(generateMenuEntryName(modules));

    m_projectTree =
        Preconditions.checkNotNull(projectTree, "IE01901: Project tree argument can not be null");
    m_modules =
        Preconditions.checkNotNull(modules, "IE01902: Module argument can't be null").clone();

    putValue(ACCELERATOR_KEY, HotKeys.INITIALIZE_MODULE_ACCELERATOR_KEY.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_INITIALIZE_MODULE".charAt(0));
  }

  private static String generateMenuEntryName(final INaviModule[] modules) {
    Preconditions.checkNotNull(modules, "IE00733: modules argument can not be null");
    if (modules.length == 1) {
      return "Initialize Module";
    } else {
      return "Initialize Modules";
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    for (final INaviModule module : m_modules) {
      CModuleInitializationFunctions.initializeModule(m_projectTree, module);
    }
  }
}
