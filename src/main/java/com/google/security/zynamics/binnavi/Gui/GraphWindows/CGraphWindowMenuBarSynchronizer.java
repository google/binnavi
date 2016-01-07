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

import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.IZyGraphLayoutSettingsListener;
import com.google.security.zynamics.zylib.gui.zygraph.settings.CProximitySettingsAdapter;

import javax.swing.JCheckBoxMenuItem;



/**
 * Synchronizes the menu bars of graph windows with the selected graph settings.
 */
public final class CGraphWindowMenuBarSynchronizer {
  /**
   * Settings object the menu bar is synchronized with,
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Menu used to toggle proximity browsing.
   */
  private final JCheckBoxMenuItem m_proximityBrowsingMenu;

  /**
   * Menu used to toggle automatic layouting.
   */
  private final JCheckBoxMenuItem m_autoLayoutMenu;

  /**
   * Listener used to synchronize settings and menus.
   */
  private final IZyGraphLayoutSettingsListener m_listener = new InternalSettingsListener();

  /**
   * Updates the menu bar on changes to the proximity settings.
   */
  private final InternalProximityListener m_proximityListener = new InternalProximityListener();

  /**
   * Creates a new menu bar synchronizer object.
   *
   * @param settings Settings object the menu bar is synchronized with,
   * @param proximityBrowsingMenu Menu used to toggle proximity browsing.
   * @param autoLayoutMenu Menu used to toggle automatic layouting.
   */
  public CGraphWindowMenuBarSynchronizer(final ZyGraphViewSettings settings,
      final JCheckBoxMenuItem proximityBrowsingMenu, final JCheckBoxMenuItem autoLayoutMenu) {
    m_settings = settings;
    m_proximityBrowsingMenu = proximityBrowsingMenu;
    m_autoLayoutMenu = autoLayoutMenu;

    m_settings.getLayoutSettings().addListener(m_listener);
    m_settings.getProximitySettings().addListener(m_proximityListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_settings.getLayoutSettings().removeListener(m_listener);
    m_settings.getProximitySettings().removeListener(m_proximityListener);
  }

  /**
   * Updates the menu bar on changes to the proximity settings.
   */
  private class InternalProximityListener extends CProximitySettingsAdapter {
    @Override
    public void changedProximityBrowsing(final boolean value) {
      m_proximityBrowsingMenu.setSelected(m_settings.getProximitySettings().getProximityBrowsing());
    }
  }

  /**
   * Listener used to synchronize settings and menus.
   */
  private class InternalSettingsListener implements IZyGraphLayoutSettingsListener {
    @Override
    public void changedAutomaticLayouting(final boolean value) {
      m_autoLayoutMenu.setSelected(m_settings.getLayoutSettings().getAutomaticLayouting());
    }
  }
}
