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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels;

import java.util.EnumMap;
import java.util.Map;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Describes the active GUI perspective for a single graph.
 */
public final class CPerspectiveModel {
  /**
   * Listeners that are notified about changes in the active perspective.
   */
  private final ListenerProvider<IPerspectiveModelListener> m_listeners =
      new ListenerProvider<IPerspectiveModelListener>();

  /**
   * Models for the individual perspectives.
   */
  private final Map<PerspectiveType, Object> m_models =
      new EnumMap<PerspectiveType, Object>(PerspectiveType.class);

  /**
   * Creates a new perspective model.
   *
   * @param model The graph model of the window the perspective model is responsible for.
   */
  public CPerspectiveModel(final IGraphModel model) {
    m_models.put(PerspectiveType.DebugPerspective, new CDebugPerspectiveModel(model));
  }

  /**
   * Adds a new listener that is notified about changes in the GUI perspective.
   *
   * @param listener The listener to add.
   */
  public void addListener(final IPerspectiveModelListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the model for a given perspective type.
   *
   * @param type The type whose model is returned.
   *
   * @return The model for the given perspective type.
   */
  public Object getModel(final PerspectiveType type) {
    Preconditions.checkNotNull(type, "IE01805: Type argument can not be null");

    return m_models.get(type);
  }

  /**
   * Removes a perspective listener.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IPerspectiveModelListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the active view.
   *
   * @param activeView The new active view.
   */
  public void setActiveView(final PerspectiveType activeView) {
    Preconditions.checkNotNull(activeView, "IE01806: Active view argument can not be null");

    for (final IPerspectiveModelListener listener : m_listeners) {
      // ESCA-JAVA0166: Notifying a listener
      try {
        listener.changedActivePerspective(activeView);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
