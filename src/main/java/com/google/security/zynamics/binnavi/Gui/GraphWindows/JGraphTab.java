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

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedHashMap;

import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Gui.CNameShortener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.ButtonTab.ButtonTabComponent;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.ButtonTab.IButtonTabListener;

/**
 * The tab component that is used to display graph panels in graph windows.
 */
public final class JGraphTab extends JTabbedPane {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6366240643759928623L;

  /**
   * Listens on clicks on the X in tabs
   */
  private final InternalTabClickListener m_listener = new InternalTabClickListener();

  /**
   * Parent window of the tab component
   */
  private final CGraphWindow m_parent;

  /**
   * 
   */
  private final LinkedHashMap<Integer, Integer> moduleIdCount =
      new LinkedHashMap<Integer, Integer>();

  /**
   * Creates a new graph tab component.
   * 
   * @param parent Parent window of the tab component.
   */
  public JGraphTab(final CGraphWindow parent) {
    super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    m_parent = Preconditions.checkNotNull(parent, "IE01638: Parent argument can not be null");
  }

  @Override
  public synchronized void addTab(final String title, final Component component) {
    Preconditions.checkNotNull(title, "IE01639: Title argument can not be null");
    Preconditions.checkNotNull(component, "IE01640: Component argument can not be null");

    super.addTab(title, component);
    setSelectedComponent(component);

    final int moduleId =
        ((IGraphPanel) component).getModel().getViewContainer().getModules().get(0)
            .getConfiguration().getId();
    if (!moduleIdCount.containsKey(moduleId)) {
      moduleIdCount.put(moduleId, 1);
    } else {
      moduleIdCount.put(moduleId, moduleIdCount.get(moduleId) + 1);
    }

    final ButtonTabComponent buttonTab = new ButtonTabComponent(this);
    buttonTab.addListener(m_listener);

    setTabComponentAt(getSelectedIndex(), buttonTab);
  }

  @Override
  public Color getBackgroundAt(final int index) {
    final IGraphPanel panel = ((IGraphPanel) this.getComponentAt(index));
    if (moduleIdCount.size() > 1) {
      return selectTabBackGroundColor(panel.getModel().getViewContainer().getModules().get(0)
          .getConfiguration().getId());
    } else {
      return super.getBackgroundAt(index);
    }
  }

  /**
   * Updates the register headers if the name of a view changed.
   */
  public void updateRegisterHeaders() {
    for (int i = 0; i < getTabCount(); i++) {
      final IGraphPanel component = (IGraphPanel) getComponentAt(i);
      setTitleAt(i, CNameShortener.shorten(component.getModel().getGraph().getRawView()));
    }
  }

  /**
   * Listener responsible for handling clicks on the X button in tabs.
   */
  private class InternalTabClickListener implements IButtonTabListener {
    @Override
    public boolean closing(final ButtonTabComponent btc) {
      final int openWindows = getTabCount();
      for (int i = 0; i < openWindows; i++) {
        if (btc == getTabComponentAt(i)) {
          final IGraphPanel panel = (IGraphPanel) getComponentAt(i);

          if (CPanelCloser.closeTab(m_parent, panel)) {
            btc.removeListener(m_listener);
            final int moduleId =
                panel.getModel().getViewContainer().getModules().get(0).getConfiguration().getId();
            if (moduleIdCount.get(moduleId) == 1) {
              moduleIdCount.remove(moduleId);
            } else {
              moduleIdCount.put(moduleId, moduleIdCount.get(moduleId) - 1);
            }
            return true;
          } else {
            return false;
          }
        }
      }

      return true;
    }
  }

  private Color selectTabBackGroundColor(final int seed) {

    final int insertionPosition =
        Iterables.indexOf(moduleIdCount.keySet(), Predicates.equalTo(seed));

    switch (insertionPosition) {
      case 0:
        return Color.getHSBColor((float) 0.55, (float) 0.2, (float) 0.8);
      case 1:
        return Color.getHSBColor((float) 0.6, (float) 0.2, (float) 0.8);
      case 2:
        return Color.getHSBColor((float) 0.65, (float) 0.2, (float) 0.8);
      case 3:
        return Color.getHSBColor((float) 0.7, (float) 0.2, (float) 0.8);
      case 4:
        return Color.getHSBColor((float) 0.75, (float) 0.2, (float) 0.8);
      case 5:
        return Color.getHSBColor((float) 0.8, (float) 0.2, (float) 0.8);
      case 6:
        return Color.getHSBColor((float) 0.85, (float) 0.2, (float) 0.8);
      case 7:
        return Color.getHSBColor((float) 0.9, (float) 0.2, (float) 0.8);
      case 8:
        return Color.getHSBColor((float) 0.95, (float) 0.2, (float) 0.8);
      case 9:
        return Color.getHSBColor(1, (float) 0.2, (float) 0.8);
      case 10:
        return Color.getHSBColor((float) 0.05, (float) 0.2, (float) 0.8);
      case 11:
        return Color.getHSBColor((float) 0.1, (float) 0.2, (float) 0.8);
      case 12:
        return Color.getHSBColor((float) 0.15, (float) 0.2, (float) 0.8);
      case 13:
        return Color.getHSBColor((float) 0.2, (float) 0.2, (float) 0.8);
      case 14:
        return Color.getHSBColor((float) 0.25, (float) 0.2, (float) 0.8);
      case 15:
        return Color.getHSBColor((float) 0.3, (float) 0.2, (float) 0.8);
      case 16:
        return Color.getHSBColor((float) 0.35, (float) 0.2, (float) 0.8);
      case 17:
        return Color.getHSBColor((float) 0.4, (float) 0.2, (float) 0.8);
      case 18:
        return Color.getHSBColor((float) 0.45, (float) 0.2, (float) 0.8);
      case 19:
        return Color.getHSBColor((float) 0.5, (float) 0.2, (float) 0.8);
      default:
        return Color.WHITE;
    }
  }
}
