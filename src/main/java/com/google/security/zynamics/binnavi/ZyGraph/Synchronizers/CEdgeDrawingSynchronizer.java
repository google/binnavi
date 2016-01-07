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
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CEdgeDrawingFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphEdgeSettingsListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.EdgeHidingMode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.settings.CProximitySettingsAdapter;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettingsListener;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * This synchronizer makes sure to update the visibility state of graph edges whenever something
 * happens that influences the visibility state of graph edges.
 */
public final class CEdgeDrawingSynchronizer {
  /**
   * Graph that is synchronized.
   */
  private final ZyGraph m_graph;

  /**
   * Current edge visibility state.
   */
  private boolean m_areEdgesVisible;

  /**
   * Synchronizes the edge visibility state with the graph settings.
   */
  private final InternalSettingsListener m_settingsListener = new InternalSettingsListener();

  /**
   * Updates the edge drawing state on changes in proximity browsing.
   */
  private final IProximitySettingsListener m_proximityListener = new InternalProximityListener();

  /**
   * Creates a new edge visibility synchronizer.
   *
   * @param graph Graph that is synchronized.
   */
  public CEdgeDrawingSynchronizer(final ZyGraph graph) {
    m_graph = Preconditions.checkNotNull(graph, "IE00980: Graph argument can not be null");

    m_areEdgesVisible = CEdgeDrawingFunctions.calcDrawSloppyEdges(m_graph);

    m_graph.getSettings().getEdgeSettings().addListener(m_settingsListener);
    m_graph.getSettings().getProximitySettings().addListener(m_proximityListener);
  }

  /**
   * Sets the drawing mode of all edges in the graph.
   *
   * @param draw True, to set all edges to visible. False, otherwise.
   */
  private void setEdgeDrawing(final boolean draw) {
    m_graph.iterateEdges(new IEdgeCallback<NaviEdge>() {
      @Override
      public IterationMode nextEdge(final NaviEdge edge) {
        edge.setDrawSloppyEdges(draw);

        return IterationMode.CONTINUE;
      }
    });

    m_graph.updateViews();
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_graph.getSettings().getEdgeSettings().removeListener(m_settingsListener);
    m_graph.getSettings().getProximitySettings().removeListener(m_proximityListener);
  }

  /**
   * Updates the visibility state of all edges in the graph.
   */
  public void updateEdgeDrawingState() {
    if (m_graph.getSettings().getEdgeSettings().getEdgeHidingMode()
        == EdgeHidingMode.HIDE_ON_THRESHOLD) {
      final boolean drawEdges = CEdgeDrawingFunctions.calcDrawSloppyEdges(m_graph);

      if (m_areEdgesVisible != drawEdges) {
        setEdgeDrawing(drawEdges);

        m_areEdgesVisible = drawEdges;
      }
    }
  }

  /**
   * Updates the edge drawing state on changes in proximity browsing.
   */
  private class InternalProximityListener extends CProximitySettingsAdapter {
    @Override
    public void changedProximityBrowsing(final boolean value) {
      updateEdgeDrawingState();
    }

    @Override
    public void changedProximityBrowsingPreview(final boolean value) {
      updateEdgeDrawingState();
    }
  }

  /**
   * Synchronizes the edge visibility state with the graph settings.
   */
  private class InternalSettingsListener extends ZyGraphEdgeSettingsListenerAdapter {
    @Override
    public void changedEdgeHidingMode(final EdgeHidingMode value) {
      setEdgeDrawing(CEdgeDrawingFunctions.calcDrawSloppyEdges(m_graph));
    }

    @Override
    public void changedEdgeHidingThreshold(final int threshold) {
      setEdgeDrawing(CEdgeDrawingFunctions.calcDrawSloppyEdges(m_graph));
    }
  }
}
