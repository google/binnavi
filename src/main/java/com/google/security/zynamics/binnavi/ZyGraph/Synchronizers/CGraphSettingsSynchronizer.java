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
package com.google.security.zynamics.binnavi.ZyGraph.Synchronizers;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphDisplaySettingsListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Implementations.CLayoutFunctions;

/**
 * Synchronizes a given graph with its settings.
 */
public final class CGraphSettingsSynchronizer {
  /**
   * The graph to synchronize with its settings.
   */
  private final ZyGraph m_graph;

  /**
   * Updates the graph on relevant settings changes.
   */
  private final InternalSettingsListener m_settingsListener = new InternalSettingsListener();

  /**
   * Creates a new synchronizer object.
   *
   * @param graph The graph to synchronize with its settings.
   */
  public CGraphSettingsSynchronizer(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE02189: Graph argument can not be null");

    m_graph = graph;

    m_graph.getSettings().getDisplaySettings().addListener(m_settingsListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_graph.getSettings().getDisplaySettings().removeListener(m_settingsListener);
  }

  /**
   * Updates the graph on relevant settings changes.
   */
  private class InternalSettingsListener extends ZyGraphDisplaySettingsListenerAdapter {
    @Override
    public void changedGradientBackground(final boolean value) {
      CLayoutFunctions.updateBackground(m_graph);
    }
  }
}
