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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;



import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;

/**
 * Synchronizes the data shown in the binary data view with the underlying module module.
 */
public final class CBinaryDataSynchronizer {
  /**
   * The hex panel that is synchronized with the module.
   */
  private final JPanel m_hexPanel;

  /**
   * The module that is synchronized with the GUI.
   */
  private final INaviModule m_module;

  /**
   * Provides the data that is shown in the hex view.
   */
  private final CDataProvider m_provider;

  /**
   * Updates the GUI on relevant changes in the module.
   */
  private final IModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * Creates a new synchronizer object.
   * 
   * @param hexPanel The hex panel that is synchronized with the module.
   * @param hexView The hex view that is synchronized with the module.
   * @param module The module that is synchronized with the GUI.
   */
  public CBinaryDataSynchronizer(final JPanel hexPanel, final JHexView hexView,
      final INaviModule module) {
    m_hexPanel =
        Preconditions.checkNotNull(hexPanel, "IE01957: Hex panel argument can not be null");
    Preconditions.checkNotNull(hexView, "IE01958: Hex view argument can not be null");
    m_module = Preconditions.checkNotNull(module, "IE01959: Module argument can not be null");

    updateBorder();
    hexView.setData(m_provider = new CDataProvider(module));

    module.addListener(m_moduleListener);
  }

  /**
   * Updates the border text of the hex panel if necessary.
   */
  private void updateBorder() {
    m_hexPanel.setBorder(new TitledBorder(String.format("Binary data of '%s'", m_module
        .getConfiguration().getName())));
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_module.removeListener(m_moduleListener);
    m_provider.dispose();
  }

  /**
   * Responsible for updating the border of the hex panel whenever the name of the underlying module
   * changes.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void changedName(final INaviModule module, final String name) {
      updateBorder();
    }
  }
}
