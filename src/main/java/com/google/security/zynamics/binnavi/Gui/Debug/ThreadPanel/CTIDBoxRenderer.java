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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadPanel;

import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Renderer class that is used to render thread IDs in the thread ID combobox.
 */
public final class CTIDBoxRenderer extends JLabel implements ListCellRenderer<TargetProcessThread> {
  @Override
  public Component getListCellRendererComponent(JList<? extends TargetProcessThread> list, TargetProcessThread thread,
      int index, boolean isSelected, boolean cellHasFocus) {
    if (thread != null) {
      setText(String.format("%d (0x%X)", thread.getThreadId(), thread.getThreadId()));
    }
    return this;
  }
}
