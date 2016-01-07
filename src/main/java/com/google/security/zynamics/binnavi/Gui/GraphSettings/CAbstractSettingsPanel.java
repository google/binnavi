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
package com.google.security.zynamics.binnavi.Gui.GraphSettings;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;


/**
 * Abstract base class for all panels to be displayed in the settings dialog.
 */
public abstract class CAbstractSettingsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8894779199818479109L;

  /**
   * Creates a new abstract settings panel object.
   *
   * @param layout The layout of the panel.
   */
  protected CAbstractSettingsPanel(final LayoutManager layout) {
    super(layout);
  }

  /**
   * Tells the panel to write its current settings into a given settings object after the user
   * clicked on the OK button of the settings dialog.
   *
   * @param settings The settings object to write to.
   *
   * @return True, to force a graph layout. False, to skip a new layout.
   */
  protected abstract boolean updateSettings(ZyGraphViewSettings settings);
}
