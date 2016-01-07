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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsPanel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.IZyGraphDisplaySettingsListener;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphDisplaySettingsListenerAdapter;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Checkbox that can be used to toggle between file addresses and relocated addresses.
 */
public final class CRelocationCheckBox extends JCheckBox {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6503942206525294243L;

  /**
   * Graph to toggle between file addresses and relocated addresses.
   */
  private final ZyGraph m_graph;

  /**
   * Debugger used to convert between the different address modes.
   */
  private final IDebugger m_debugger;

  /**
   * Updates the graph settings on checkbox clicks.
   */
  private final ItemListener m_internalItemListener = new InternalItemListener();

  /**
   * Updates the checkbox on settings changes.
   */
  private final IZyGraphDisplaySettingsListener m_internalSettingsListener =
      new InternalSettingsListener();

  /**
   * Creates a new relocation checkbox.
   *
   * @param graph Graph to toggle between file addresses and relocated addresses.
   * @param debugger Debugger used to convert between the different address modes.
   */
  public CRelocationCheckBox(final ZyGraph graph, final IDebugger debugger) {
    super("Show Relocated Offsets");

    Preconditions.checkNotNull(graph, "IE01469: Graph argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01470: Debugger argument can not be null");

    m_graph = graph;
    m_debugger = debugger;

    addItemListener(m_internalItemListener);

    graph.getSettings().getDisplaySettings().addListener(m_internalSettingsListener);
  }

  /**
   * Updates the graph settings on checkbox clicks.
   */
  private class InternalItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      m_graph.getSettings().getDisplaySettings().setShowMemoryAddresses(m_debugger, isSelected());
    }
  }

  /**
   * Updates the checkbox on settings changes.
   */
  private class InternalSettingsListener extends ZyGraphDisplaySettingsListenerAdapter {
    @Override
    public void changedShowMemoryAddresses(final IDebugger debugger, final boolean selected) {
      setSelected(m_graph.getSettings().getDisplaySettings().getShowMemoryAddresses(m_debugger));
    }
  }
}
