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
package com.google.security.zynamics.binnavi.Gui.FunctionSelection;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JScrollPane;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Loaders.CModuleLoader;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

/**
 * Function selection dialog that can be used to let the user select an arbitrary function from all
 * modules of a database.
 */
public final class CFunctionSelectionDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1751648093815840785L;

  /**
   * Panel that is used to display the available functions.
   */
  private final CFunctionSelectionPanel m_panel;

  /**
   * Will contain the selected function when the user clicks the OK button.
   */
  private INaviFunction m_function;

  /**
   * Creates a new dialog object.
   *
   * @param owner Parent window of the dialog.
   * @param database Database the offered functions belong to.
   */
  public CFunctionSelectionDialog(final Window owner, final IDatabase database) {
    super(owner, "Please select a function", ModalityType.APPLICATION_MODAL);

    Preconditions.checkNotNull(database, "IE01572: Database argument can not be null");

    setLayout(new BorderLayout());

    new CDialogEscaper(this);

    m_panel = new CFunctionSelectionPanel(database, new InternalActionProvider());

    add(new JScrollPane(m_panel));
    add(new CPanelTwoButtons(new InternalListener(), "OK", "Cancel"), BorderLayout.SOUTH);

    setSize(500, 400);
  }


  /**
   * Return the selected function.
   *
   * @return The selected function.
   */
  public INaviFunction getSelectedFunction() {
    return m_function;
  }

  /**
   * Shields the available functions from the public API.
   */
  private class InternalActionProvider implements IActionProvider {
    @Override
    public void loadModule(final INaviModule module) {
      CModuleLoader.loadModule(CFunctionSelectionDialog.this, module);
    }
  }

  /**
   * Waits for the user to click the OK or Cancel buttons.
   */
  private class InternalListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        m_function = m_panel.getSelectedFunction();
      }

      dispose();
    }
  }
}
