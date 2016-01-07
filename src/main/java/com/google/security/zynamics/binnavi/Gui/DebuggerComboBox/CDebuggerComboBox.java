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
package com.google.security.zynamics.binnavi.Gui.DebuggerComboBox;

import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveCombobox;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;

import javax.swing.ComboBoxModel;

/**
 * This combo box class can be used to display a list of debuggers.
 */
public final class CDebuggerComboBox extends CSaveCombobox<CDebuggerTemplateWrapper> {
  /**
   * Creates a new debugger combobox.
   *
   * @param model The list model used to fill the combobox.
   */
  public CDebuggerComboBox(final ComboBoxModel<CDebuggerTemplateWrapper> model) {
    super(model);

    if (model != null && model.getSize() != 0) {
      setSelectedIndex(0);
    }
  }

  /**
   * Returns the selected debugger.
   *
   * @return The selected debugger.
   */
  public DebuggerTemplate getSelectedDebugger() {
    return ((CDebuggerTemplateWrapper) getSelectedItem()).getObject();
  }

  /**
   * Sets the selected debugger.
   *
   * @param debugger The debugger to select.
   */
  public void setSelectedDebugger(final DebuggerTemplate debugger) {
    for (int index = 0; index < getItemCount(); index++) {
      final DebuggerTemplate currentDebugger = getItemAt(index).getObject();
      if (currentDebugger != null && currentDebugger.equals(debugger)) {
        setSelectedIndex(index);
        break;
      }
    }
  }
}
