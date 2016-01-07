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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel;

import java.math.BigInteger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Actions.CCopyRegisterValueAction;
import com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Actions.CGotoOffsetAction;
import com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Actions.CZoomToAddressAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchAction;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.gui.JRegisterView.IMenuProvider;

/**
 * Provides the context menu of register views.
 */
public final class CRegisterMenuProvider implements IMenuProvider {
  /**
   * Debug perspective that provides the active debugger.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Provides the register data.
   */
  private final CRegisterProvider m_dataProvider;

  /**
   * Creates a new register view menu provider.
   *
   * @param debugPerspectiveModel Debug perspective that provides the active debugger.
   * @param dataProvider Provides the register data.
   */
  public CRegisterMenuProvider(
      final CDebugPerspectiveModel debugPerspectiveModel, final CRegisterProvider dataProvider) {
    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01219: Debug perspective model argument can not be null");
    Preconditions.checkNotNull(dataProvider, "IE01474: Data provider argument can not be null");

    m_debugPerspectiveModel = debugPerspectiveModel;
    m_dataProvider = dataProvider;
  }

  /**
   * Checks whether a view contains the given address.
   *
   * @param rawView The view to check.
   * @param address The address to search for.
   *
   * @return True, if the view contains the address. False, otherwise.
   */
  private boolean containsAddress(final INaviView rawView, final long address) {
    for (final INaviViewNode node : rawView.getGraph()) {
      if (node instanceof INaviFunctionNode) {
        final INaviFunctionNode fnode = (INaviFunctionNode) node;

        if (fnode.getAddress().toLong() == address) {
          return true;
        }
      } else if (node instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) node;

        for (final INaviInstruction instruction : cnode.getInstructions()) {
          if (instruction.getAddress().toLong() == address) {
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  public JPopupMenu getRegisterMenu(final int registerNumber) {
    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger == null) {
      return null;
    }

    final JPopupMenu menu = new JPopupMenu();

    final BigInteger value = m_dataProvider.getRegisterInformation(registerNumber).getValue();

    menu.add(CActionProxy.proxy(new CCopyRegisterValueAction(value.toString(16).toUpperCase())));

    final MemorySection section = ProcessHelpers.getSectionWith(
        debugger.getProcessManager().getMemoryMap(), new CAddress(value));

    final JMenuItem gotoAddress = menu.add(CActionProxy.proxy(
        new CGotoOffsetAction(m_debugPerspectiveModel, new CAddress(value))));
    gotoAddress.setEnabled(section != null);

    if (containsAddress(
        m_debugPerspectiveModel.getGraphModel().getGraph().getRawView(), value.longValue())) {
      menu.add(CActionProxy.proxy(new CZoomToAddressAction(
          m_debugPerspectiveModel.getGraphModel().getGraph(), new CAddress(value),
          debugger.getModule(new RelocatedAddress(new CAddress(value))))));
    } else {
      final IViewContainer container = m_debugPerspectiveModel.getGraphModel().getViewContainer();

      menu.add(CActionProxy.proxy(new CSearchAction(
          m_debugPerspectiveModel.getGraphModel().getParent(), container, new CAddress(value))));
    }

    return menu;
  }
}
