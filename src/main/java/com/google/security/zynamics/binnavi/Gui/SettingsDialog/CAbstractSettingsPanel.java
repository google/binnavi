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
package com.google.security.zynamics.binnavi.Gui.SettingsDialog;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Abstract base class for all panels that are shown in the settings dialog.
 */
public abstract class CAbstractSettingsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4906261606347522097L;

  /**
   * Creates a new settings panel object.
   * 
   * @param layout The layout of the settings panel object.
   */
  protected CAbstractSettingsPanel(final LayoutManager layout) {
    super(layout);
  }

  /**
   * This method is invoked whenever the settings described in the panel should be saved.
   * 
   * @return True, if some options were changed that require a restart. False, if no restart if
   *         necessary.
   */
  protected abstract boolean save();
}
