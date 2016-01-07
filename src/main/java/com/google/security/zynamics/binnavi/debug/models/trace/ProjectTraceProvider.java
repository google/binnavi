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
package com.google.security.zynamics.binnavi.debug.models.trace;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceManagerListener;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates the traces that are bound to a single project.
 */
public final class ProjectTraceProvider extends AbstractTraceProvider {
  /**
   * Project that provides the traces.
   */
  private final INaviProject project;

  /**
   * Listeners that are notified about changes in the traces.
   */
  private final List<ListenerWrapper> listenerWrappers = new ArrayList<>();

  /**
   * Creates a new project trace provider object.
   *
   * @param project The project that is encapsulated.
   */
  public ProjectTraceProvider(final INaviProject project) {
    this.project = project;
  }

  @Override
  public void addListener(final ITraceManagerListener listener) {
    final ListenerWrapper newListener = new ListenerWrapper(listener);
    listenerWrappers.add(newListener);
    project.addListener(newListener);
  }

  @Override
  public TraceList createTrace(final String name, final String description)
      throws CouldntSaveDataException {
    return project.getContent().createTrace(name, description);
  }

  @Override
  public TraceList getList(final int index) {
    return project.getContent().getTraces().get(index);
  }

  @Override
  public int getNumberOfTraceLists() {
    return project.getContent().getTraceCount();
  }

  @Override
  public List<TraceList> getTraces() {
    return project.getContent().getTraces();
  }

  @Override
  public boolean isLoaded() {
    return project.isLoaded();
  }

  @Override
  public Iterator<TraceList> iterator() {
    return project.getContent().getTraces().iterator();
  }

  @Override
  public void removeList(final TraceList trace) throws CouldntDeleteException {
    project.getContent().removeTrace(trace);
  }

  @Override
  public void removeListener(final ITraceManagerListener listener) {
    for (final ListenerWrapper wrapper : new ArrayList<ListenerWrapper>(listenerWrappers)) {
      if (wrapper.getListener() == listener) {
        project.removeListener(wrapper);
        listenerWrappers.remove(wrapper);
      }
    }
  }

  /**
   * Wraps between trace manager listeners and project listeners.
   */
  private static class ListenerWrapper extends CProjectListenerAdapter {
    /**
     * The listener object to wrap.
     */
    private final ITraceManagerListener listener;

    /**
     * Creates a new listener wrapper.
     *
     * @param listener The listener object to wrap.
     */
    public ListenerWrapper(final ITraceManagerListener listener) {
      this.listener =
          Preconditions.checkNotNull(listener, "IE00774: Listener argument can't be null");
    }

    @Override
    public void addedTrace(final INaviProject project, final TraceList trace) {
      listener.addedTrace(trace);
    }

    /**
     * Returns the wrapped listener.
     *
     * @return The wrapped listener.
     */
    public ITraceManagerListener getListener() {
      return listener;
    }

    @Override
    public void loadedProject(final CProject project) {
      listener.loaded();
    }

    @Override
    public void removedTrace(final INaviProject project, final TraceList trace) {
      listener.removedTrace(trace);
    }
  }
}
