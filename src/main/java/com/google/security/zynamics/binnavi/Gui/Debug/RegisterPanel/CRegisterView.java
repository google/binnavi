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

import java.awt.BorderLayout;
import java.math.BigInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Implementations.CRegisterFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.zylib.gui.JRegisterView.JRegisterView;

/**
 * Encapsulates the register view with its register values provider.
 */
public final class CRegisterView extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1272866008900112960L;

  /**
   * Parent window of the register view.
   */
  private final JFrame m_parent;

  /**
   * Provides information about the active debug perspective.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * The register viewer component is used to display the current values of the CPU registers in the
   * target process
   */
  private final JRegisterView m_registerView;

  /**
   * Data provider that contains the displayed register values.
   */
  private final CRegisterProvider m_dataProvider = new CRegisterProvider();

  /**
   * Synchronizes the registers shown in the register view with the underlying data selected in the
   * GUI.
   */
  private final CRegisterViewSynchronizer m_synchronizer;

  /**
   * Keeps track of register modifications by the user.
   */
  private final InternalDataEnteredListener m_enteredDataListener =
      new InternalDataEnteredListener();

  /**
   * Creates a new register view.
   *
   * @param parent Parent window of the register view.
   * @param debugPerspectiveModel Provides information about the active debug perspective.
   */
  public CRegisterView(final JFrame parent, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01477: Parent argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01478: Debug perspective model argument can not be null");

    m_parent = parent;
    m_debugPerspectiveModel = debugPerspectiveModel;

    setBorder(new TitledBorder("Register Values"));

    // Tell the data provider what connection it can use
    // to reload memory from the target process.
    m_dataProvider.addListener(m_enteredDataListener);

    m_registerView = new JRegisterView(m_dataProvider);
    m_registerView.setVisible(true);
    m_registerView.setMenuProvider(
        new CRegisterMenuProvider(debugPerspectiveModel, m_dataProvider));

    final JScrollPane regScroller = new JScrollPane(m_registerView);

    regScroller.getViewport().setBackground(m_registerView.getBackground());
    regScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    add(regScroller);

    m_synchronizer =
        new CRegisterViewSynchronizer(m_registerView, m_dataProvider, debugPerspectiveModel);
  }

  /**
   * Cleans up allocated resources.
   */
  public void dispose() {
    m_synchronizer.dispose();
    m_registerView.dispose();
  }

  /**
   * Updates register values in the target process when the user changes them manually.
   */
  private class InternalDataEnteredListener implements IDataEnteredListener {
    @Override
    public void registerChanged(
        final int index, final BigInteger oldValue, final BigInteger newValue) {
      final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
      final TargetProcessThread activeThread =
          debugger == null ? null : debugger.getProcessManager().getActiveThread();

      if (debugger != null && activeThread != null) {
        CRegisterFunctions.changeRegister(m_parent,
            debugger,
            m_registerView,
            activeThread.getThreadId(),
            index,
            newValue);
      }
    }
  }
}
