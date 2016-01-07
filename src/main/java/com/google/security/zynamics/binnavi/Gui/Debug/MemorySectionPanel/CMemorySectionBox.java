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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel;

import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import javax.swing.JComboBox;

/**
 * Combobox that displays memory sections.
 */
public final class CMemorySectionBox extends JComboBox<CMemorySectionWrapper> {
  /**
   * Creates a new memory section box.
   */
  public CMemorySectionBox() {
    setFont(GuiHelper.MONOSPACED_FONT);
  }

  @Override
  public CMemorySectionWrapper getSelectedItem() {
    return ((CMemorySectionWrapper) super.getSelectedItem());
  }

  @Override
  public void setSelectedItem(final Object section) {
    if (section == null) {
      return;
    }
    for (int i = 0; i < getItemCount(); i++) {
      final CMemorySectionWrapper wrapper = getItemAt(i);

      if (wrapper == section || wrapper.getObject() == section) {
        super.setSelectedItem(wrapper);

        new SwingInvoker() {
          @Override
          protected void operation() {
            updateUI();
          }
        }.invokeLater();
      }
    }
  }
}
