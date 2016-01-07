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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CModuleFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Action that can be used to load modules.
 */
public final class CLoadModuleAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5178343179269965998L;

  /**
   * Tree to be updated after the modules were loaded.
   */
  private final JTree m_projectTree;

  /**
   * Modules to be loaded.
   */
  private final INaviModule[] m_modules;

  /**
   * Creates a new action object.
   * 
   * @param projectTree Tree to be updated after the modules were loaded.
   * @param modules Modules to be loaded.
   */
  public CLoadModuleAction(final JTree projectTree, final INaviModule[] modules) {
    super(generateActionString(modules));

    m_projectTree =
        Preconditions.checkNotNull(projectTree, "IE01901: Project tree argument can not be null");
    m_modules =
        Preconditions.checkNotNull(modules, "IE01902: Modules argument can't be null").clone();

    for (final INaviModule module : modules) {
      Preconditions.checkNotNull(module, "IE01903: Modules list contains a null-element");
    }

    putValue(ACCELERATOR_KEY, HotKeys.LOAD_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_LOAD_MODULE".charAt(0));

  }

  public static String generateActionString(final INaviModule module) {
    if (module.isInitialized()) {
      return "Load Module";
    } else {
      return "Initialize & Load Module";
    }
  }

  private static String generateActionString(final INaviModule[] modules) {
    if (modules.length == 1) {
      final INaviModule module = modules[0];
      return generateActionString(module);
    } else {
      boolean allLoad = true;
      boolean allInitialize = true;

      for (final INaviModule module : modules) {
        if (module.isInitialized()) {
          allInitialize = false;
        } else {
          allLoad = false;
        }
      }
      if (allLoad) {
        return "Load Modules";
      } else if (allInitialize) {
        return "Initialize & Load Modules";
      } else {
        return "Load / Initialize & Load Modules";
      }

    }
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CModuleFunctions.loadModules(m_projectTree, m_modules);
  }
}
