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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

import java.util.HashMap;
import java.util.Map;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyGraphBuilder;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Class that keeps track of what ZyGraph builder objects are currently building graphs.
 */
public final class ZyGraphBuilderManager {
  /**
   * The only valid instance of this class.
   */
  private static final ZyGraphBuilderManager m_instance = new ZyGraphBuilderManager();

  /**
   * Currently active builders.
   */
  private final Map<INaviView, ZyGraphBuilder> m_builders =
      new HashMap<INaviView, ZyGraphBuilder>();

  /**
   * Listeners that are notified about changes in the managed builders.
   */
  private final ListenerProvider<IGraphBuilderManagerListener> m_listeners =
      new ListenerProvider<IGraphBuilderManagerListener>();

  /**
   * Private constructor => Singleton.
   */
  private ZyGraphBuilderManager() {
  }

  /**
   * Returns the only valid instance of this class.
   *
   * @return The only valid instance of this class.
   */
  public static ZyGraphBuilderManager instance() {
    return m_instance;
  }

  /**
   * Adds a listener object that is notified about changes in the managed builders.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IGraphBuilderManagerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the builder associated with the given view.
   *
   * @param view The view whose builder is returned.
   *
   * @return The builder for the view.
   *
   * @throws MaybeNullException Thrown if the view is not currently built.
   */
  public ZyGraphBuilder getBuilder(final INaviView view) throws MaybeNullException {
    final ZyGraphBuilder builder = m_builders.get(view);

    if (builder == null) {
      throw new MaybeNullException();
    }

    return builder;
  }

  /**
   * Removes a builder from the manager.
   *
   * @param view The view whose builder is removed.
   */
  public void removeBuilder(final INaviView view) {
    final ZyGraphBuilder builder = m_builders.get(view);

    Preconditions.checkNotNull(builder, "IE00704: View was not managed");

    m_builders.remove(view);

    for (final IGraphBuilderManagerListener listener : m_listeners) {
      try {
        listener.removedBuilder(view, builder);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a previously listening listener.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IGraphBuilderManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Sets the builder for a view.
   *
   * @param view The view to be built.
   * @param builder The builder of the view.
   */
  public void setBuilder(final INaviView view, final ZyGraphBuilder builder) {
    m_builders.put(view, builder);

    for (final IGraphBuilderManagerListener listener : m_listeners) {
      try {
        listener.addedBuilder(view, builder);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
