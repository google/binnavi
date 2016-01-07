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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Keeps track of the results of register tracking operations.
 */
public final class CTrackingResultContainer {
  /**
   * The graph on which the register tracking operations happen.
   */
  private final ZyGraph m_graph;

  /**
   * The current tracking results.
   */
  private CTrackingResult m_result = null;

  /**
   * Listeners that are notified about changes in the tracking result.
   */
  private final ListenerProvider<ITrackingResultsListener> m_listeners =
      new ListenerProvider<ITrackingResultsListener>();

  /**
   * The view container the graph belongs to.
   */
  private final IViewContainer m_viewContainer;

  /**
   * Creates a new results container object.
   * 
   * @param viewContainer The view container the graph belongs to.
   * @param graph The graph on which the register tracking operations happen.
   */
  public CTrackingResultContainer(final IViewContainer viewContainer, final ZyGraph graph) {
    m_viewContainer =
        Preconditions.checkNotNull(viewContainer,
            "IE01683: View container argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01684: Graph argument can not be null");
  }

  /**
   * Adds a new listener object that is notified about changes in the register tracking result.
   * 
   * @param listener The register object to track.
   */
  public void addListener(final ITrackingResultsListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the graph on which the register tracking operations happen.
   * 
   * @return The graph on which the register tracking operations happen.
   */
  public ZyGraph getGraph() {
    return m_graph;
  }

  /**
   * Returns the current register tracking result.
   * 
   * @return The current register tracking result.
   */
  public CTrackingResult getResult() {
    return m_result;
  }

  /**
   * Returns the view container the graph belongs to.
   * 
   * @return The view container the graph belongs to.
   */
  public IViewContainer getViewContainer() {
    return m_viewContainer;
  }

  /**
   * Removes a listener object from the list of objects that are notified about changes in the
   * tracking result.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final ITrackingResultsListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Updates the current tracking result.
   * 
   * @param result The new tracking result.
   */
  public void setResult(final CTrackingResult result) {
    m_result = Preconditions.checkNotNull(result, "IE01685: Result argument can not be null");

    for (final ITrackingResultsListener listener : m_listeners) {
      listener.updatedResult(this, result);
    }
  }
}
