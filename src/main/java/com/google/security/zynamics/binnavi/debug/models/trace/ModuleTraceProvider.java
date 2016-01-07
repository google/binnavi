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
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.CTraceContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.ITraceContainerListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates the traces that are bound to a single module.
 */
public final class ModuleTraceProvider extends AbstractTraceProvider {
  /**
   * Module that provides the traces.
   */
  private final INaviModule module;

  /**
   * Listeners that are notified about changes in the traces.
   */
  private final List<ListenerWrapper> listenerWrapper = new ArrayList<>();

  /**
   * Creates a new module trace provider object.
   *
   * @param module The module that is encapsulated.
   */
  public ModuleTraceProvider(final INaviModule module) {
    this.module = Preconditions.checkNotNull(module, "IE00772: Module argument can not be null");
  }

  @Override
  public void addListener(final ITraceManagerListener listener) {
    final ListenerWrapper newListener = new ListenerWrapper(listener);
    listenerWrapper.add(newListener);
    module.addListener(newListener);
    if (module.isLoaded()) {
      module.getContent().getTraceContainer().addListener(newListener);
    }
  }

  @Override
  public TraceList createTrace(final String name, final String description)
      throws CouldntSaveDataException {
    return module.getContent().getTraceContainer().createTrace(name, description);
  }

  @Override
  public TraceList getList(final int index) {
    return module.getContent().getTraceContainer().getTraces().get(index);
  }

  @Override
  public int getNumberOfTraceLists() {
    return module.getContent().getTraceContainer().getTraceCount();
  }

  @Override
  public List<TraceList> getTraces() {
    return module.getContent().getTraceContainer().getTraces();
  }

  @Override
  public boolean isLoaded() {
    return module.isLoaded();
  }

  @Override
  public Iterator<TraceList> iterator() {
    return module.getContent().getTraceContainer().getTraces().iterator();
  }

  @Override
  public void removeList(final TraceList trace) throws CouldntDeleteException {
    module.getContent().getTraceContainer().deleteTrace(trace);
  }

  @Override
  public void removeListener(final ITraceManagerListener listener) {
    for (final ListenerWrapper wrapper : new ArrayList<ListenerWrapper>(listenerWrapper)) {
      if (wrapper.getListener() == listener) {
        module.removeListener(wrapper);
        listenerWrapper.remove(wrapper);
        if (module.isLoaded()) {
          module.getContent().getTraceContainer().removeListener(wrapper);
        }
      }
    }
  }

  /**
   * Wraps between trace manager listeners and module listeners.
   */
  private static class ListenerWrapper extends CModuleListenerAdapter implements
      ITraceContainerListener {
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
          Preconditions.checkNotNull(listener, "IE00773: Listener argument can not be null");
    }

    @Override
    public void addedTrace(final CTraceContainer container, final TraceList trace) {
      listener.addedTrace(trace);
    }

    @Override
    public void deletedTrace(final CTraceContainer container, final TraceList trace) {
      listener.removedTrace(trace);
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
    public void loadedModule(final INaviModule module) {
      listener.loaded();
      module.getContent().getTraceContainer().addListener(this);
    }
  }
}
