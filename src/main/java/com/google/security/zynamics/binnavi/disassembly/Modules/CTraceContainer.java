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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all trace-related functions of a module.
 */
public final class CTraceContainer {
  /**
   * Modules whose traces are provided by the container.
   */
  private final INaviModule m_module;

  /**
   * List of traces that were recorded for this module.
   */
  private final IFilledList<TraceList> m_traces;

  /**
   * Listeners that are notified about changes in traces.
   */
  private final ListenerProvider<ITraceContainerListener> m_listeners =
      new ListenerProvider<ITraceContainerListener>();

  /**
   * Synchronizes changes in the traces with the database.
   */
  private final SQLProvider m_provider;

  /**
   * Creates a new trace container object.
   *
   * @param module Modules whose traces are provided by the container.
   * @param traces List of traces that were recorded for this module.
   * @param provider Synchronizes changes in the traces with the database.
   */
  public CTraceContainer(final INaviModule module, final List<TraceList> traces,
      final SQLProvider provider) {
    m_module = Preconditions.checkNotNull(module, "IE01694: module argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE01695: provider argument can not be null");
    m_traces = new FilledList<TraceList>(traces);
  }

  /**
   * Adds a listener that is notified about changes in the trace container.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final ITraceContainerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Creates a new debug trace for the module.
   *
   * @param name The name of the new debug trace.
   * @param description The description of the new debug trace.
   *
   * @return The new debug trace object.
   *
   * @throws CouldntSaveDataException Thrown if the new debug trace could not be saved to the
   *         database.
   */
  public TraceList createTrace(final String name, final String description)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00157: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE00158: Description argument can not be null");

    final TraceList trace = m_provider.createTrace(m_module, name, description);

    m_traces.add(trace);

    for (final ITraceContainerListener listener : m_listeners) {
      try {
        listener.addedTrace(this, trace);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_module.getConfiguration().updateModificationDate();

    return trace;
  }

  /**
   * Deletes a trace from the module.
   *
   * @param trace The trace to delete.
   *
   * @throws CouldntDeleteException Thrown of the trace could not be deleted.
   */
  public void deleteTrace(final TraceList trace) throws CouldntDeleteException {
    Preconditions.checkNotNull(trace, "IE00167: Trace argument can not be null");
    Preconditions.checkArgument(m_traces.contains(trace),
        "IE00168: Trace is not part of this module");

    m_provider.deleteTrace(trace);
    m_traces.remove(trace);

    for (final ITraceContainerListener listener : m_listeners) {
      try {
        listener.deletedTrace(this, trace);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Returns the number of debug traces that were recorded for this module.
   *
   * @return The number of debug traces in the module.
   */
  public int getTraceCount() {
    return m_traces.size();
  }

  /**
   * Returns the debug traces that were recorded for this module.
   *
   * @return The debug traces that were recorded for this module.
   */
  public List<TraceList> getTraces() {
    return new ArrayList<TraceList>(m_traces);
  }

  /**
   * Removes a listener that was previously notified about changes in the trace container.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final ITraceContainerListener listener) {
    m_listeners.removeListener(listener);
  }
}
