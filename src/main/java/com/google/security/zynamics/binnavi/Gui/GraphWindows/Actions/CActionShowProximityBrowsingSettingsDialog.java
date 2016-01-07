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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Gui.CProximitySettingsDialog;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;


/**
 * Action class for showing the proximity browsing settings dialog.
 */
public final class CActionShowProximityBrowsingSettingsDialog extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2262503500313605910L;

  /**
   * Parent window used for the dialog.
   */
  private final JFrame m_parent;

  /**
   * Settings object modified in this dialog.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for the dialog.
   * @param settings Settings object modified in this dialog.
   */
  public CActionShowProximityBrowsingSettingsDialog(
      final JFrame parent, final ZyGraphViewSettings settings) {
    super("Proximity Browsing Settings");

    m_parent = parent;
    m_settings = settings;
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CProximitySettingsDialog.showDialog(m_parent, m_settings);
  }
}
