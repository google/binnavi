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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.GeneralSettingsConfigItem;

import com.jidesoft.swing.JideSplitPane;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JSplitPane;

/**
 * Contains code for updating the graph panel settings depending on the GUI state.
 */
public final class CGraphPanelSettingsSynchronizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphPanelSettingsSynchronizer() {
  }

  /**
   * Sets up the listeners that keep the window layout settings updated.
   *
   * @param graphTaggingSplitter
   * @param graphSplitter
   */
  public static void initializeSynchronizer(
      final JideSplitPane graphSplitter, final JideSplitPane graphTaggingSplitter) {
    final GeneralSettingsConfigItem.GraphWindowConfigItem settings =
        ConfigManager.instance().getGeneralSettings().getGraphWindow();

    graphSplitter.addPropertyChangeListener(
        JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            settings.setSizeBottomPanel(graphSplitter.getDividerLocation(0));
          }
        });

    graphTaggingSplitter.addPropertyChangeListener(
        JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            settings.setSizeLeftPanel(graphTaggingSplitter.getDividerLocation(0));
          }
        });

    graphTaggingSplitter.addPropertyChangeListener(
        JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            settings.setSizeRightPanel(graphTaggingSplitter.getDividerLocation(1));
          }
        });
  }
}
