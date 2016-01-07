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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.google.security.zynamics.binnavi.CMain;


/**
 * Button used to refresh the displayed target process memory and the target process memory map.
 */
public final class CMemoryRefreshButton extends JButton {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8066425580359336720L;

  /**
   * Creates a new memory refresh button.
   *
   *  Please note that by default there is no action associated with this button. The actions are
   * set by the button synchronizer instead depending on the current state of the debug GUI
   * perspective.
   */
  public CMemoryRefreshButton() {
    setIcon(new ImageIcon(CMain.class.getResource("data/memoryupdate_up.jpg")));
    setPreferredSize(new Dimension(32, 32));
  }
}
