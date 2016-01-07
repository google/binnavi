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
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.trace.ModuleTraceProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainerListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * View container that encapsulates a module.
 */
public final class CModuleContainer implements IViewContainer {
  /**
   * Database the module belongs to.
   */
  private final IDatabase m_database;

  /**
   * The encapsulated module.
   */
  private final INaviModule m_module;

  /**
   * Trace provider of the container.
   */
  private final ITraceListProvider m_provider;

  /**
   * Listeners that are notified about changes in the view container.
   */
  private final ListenerProvider<IViewContainerListener> m_listeners =
      new ListenerProvider<IViewContainerListener>();

  /**
   * Forwards module events to the view container listeners.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Provides the debuggers for the module container.
   */
  private final DebuggerProvider m_debuggerProvider;

  /**
   * The active debugger.
   */
  private IDebugger m_activeDebugger;

  /**
   * Creates a new container object.
   *
   * @param database Database the module belongs to.
   * @param module The encapsulated module.
   */
  public CModuleContainer(final IDatabase database, final INaviModule module) {
    m_database = Preconditions.checkNotNull(database, "IE02397: database argument can not be null");
    m_module = Preconditions.checkNotNull(module, "IE02398: module argument can not be null");
    m_debuggerProvider = new DebuggerProvider(new ModuleTargetSettings(m_module));

    final IDebugger debugger = m_module.getConfiguration().getDebugger();

    if (debugger != null) {
      m_activeDebugger = debugger;
      m_debuggerProvider.addDebugger(debugger);
    }

    m_module.addListener(m_listener);

    m_provider = new ModuleTraceProvider(m_module);
  }

  @Override
  public Object getNative() {
    return m_module;
  }

  @Override
  public void addListener(final IViewContainerListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean containsModule(final INaviModule module) {
    return module.equals(m_module);
  }

  @Override
  public INaviView createView(final String name, final String description) {
    return m_module.getContent().getViewContainer().createView(name, description);
  }

  @Override
  public void deleteView(final INaviView view) throws CouldntDeleteException {
    m_module.getContent().getViewContainer().deleteView(view);
  }

  @Override
  public void dispose() {
    m_module.removeListener(m_listener);
  }

  @Override
  public List<INaviAddressSpace> getAddressSpaces() {
    return null;
  }

  @Override
  public IDatabase getDatabase() {
    return m_database;
  }

  @Override
  public DebuggerProvider getDebuggerProvider() {
    return m_debuggerProvider;
  }

  @Override
  public INaviFunction getFunction(final INaviView view) {
    return m_module.getContent().getViewContainer().getFunction(view);
  }

  @Override
  public List<INaviFunction> getFunctions() {
    return m_module.getContent().getFunctionContainer().getFunctions();
  }

  @Override
  public List<INaviModule> getModules() {
    return Lists.newArrayList(m_module);
  }

  @Override
  public String getName() {
    return m_module.getConfiguration().getName();
  }

  @Override
  public List<Pair<INaviView, CTag>> getTaggedViews() {
    return CViewFilter.getTaggedViews(m_module.getContent().getViewContainer().getViews());
  }

  @Override
  public List<INaviView> getTaggedViews(final CTag object) {
    return CViewFilter.getTaggedViews(m_module.getContent().getViewContainer().getViews(), object);
  }

  @Override
  public ITraceListProvider getTraceProvider() {
    return m_provider;
  }

  @Override
  public List<INaviView> getUserViews() {
    return m_module.getContent().getViewContainer().getUserViews();
  }

  @Override
  public INaviView getView(final INaviFunction function) {
    for (final INaviView view : m_module.getContent().getViewContainer().getViews()) {
      if (m_module.getContent().getViewContainer().getFunction(view) == function) {
        return view;
      }
    }

    return null;
  }

  @Override
  public int getViewCount() {
    return m_module.getCustomViewCount();
  }

  @Override
  public List<INaviView> getViews() {
    return m_module.getContent().getViewContainer().getViews();
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final List<UnrelocatedAddress> address,
      final boolean all) throws CouldntLoadDataException {
    return m_module.getViewsWithAddresses(address, all);
  }

  @Override
  public boolean isLoaded() {
    return m_module.isLoaded();
  }

  @Override
  public void removeListener(final IViewContainerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Forwards module events to the view container listeners.
   */
  private class InternalListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      for (final IViewContainerListener listener : m_listeners) {
        try {
          listener.addedView(CModuleContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDebugger(final INaviModule module, final IDebugger debugger) {
      if (m_activeDebugger != null) {
        m_debuggerProvider.removeDebugger(m_activeDebugger);
      }

      if (debugger != null) {
        m_activeDebugger = debugger;
        m_debuggerProvider.addDebugger(debugger);
      }
    }

    @Override
    public void closedModule(final CModule module, final ICallgraphView callgraphView,
        final List<IFlowgraphView> flowgraphs) {
      final List<INaviView> views = new ArrayList<INaviView>(flowgraphs);
      views.add(callgraphView);

      for (final IViewContainerListener listener : m_listeners) {
        try {
          listener.closedContainer(CModuleContainer.this, views);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedView(final INaviModule module, final INaviView view) {
      for (final IViewContainerListener listener : m_listeners) {
        try {
          listener.deletedView(CModuleContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void loadedModule(final INaviModule module) {
      for (final IViewContainerListener listener : m_listeners) {
        try {
          listener.loaded(CModuleContainer.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
