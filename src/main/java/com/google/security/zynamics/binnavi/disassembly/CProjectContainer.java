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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.ProjectTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.trace.ProjectTraceProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfigurationListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainerListener;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * View container that encapsulates a project.
 */
public final class CProjectContainer implements IViewContainer {
  /**
   * Database the project belongs to.
   */
  private final IDatabase m_database;

  /**
   * The encapsulated project.
   */
  private final INaviProject m_project;

  /**
   * The encapsulated address space.
   */
  private final INaviAddressSpace m_addressSpace;

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
   * Forwards project events to the view container listeners.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Forwards address space events to the view container listeners.
   */
  private final InternalAddressSpaceListener m_addressSpaceListener =
      new InternalAddressSpaceListener();

  private final InternalAddressSpaceConfigurationListener m_addressSpaceConfigurationListener =
      new InternalAddressSpaceConfigurationListener();

  /**
   * Forwards module events to the view container listeners.
   */
  private final InternalModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * Provides the debuggers for the module container.
   */
  private final DebuggerProvider m_debuggerProvider;

  /**
   * The active debuggers for each address space.
   */
  private final Map<INaviAddressSpace, IDebugger> m_activeDebuggers =
      new HashMap<INaviAddressSpace, IDebugger>();

  private final IAddressSpaceContentListener m_internalContentListener =
      new InternalAddressSpaceContentListener();

  /**
   * Creates a new project container object.
   *
   * @param database Database the project belongs to.
   * @param project The encapsulated project.
   */
  public CProjectContainer(final IDatabase database, final INaviProject project) {
    m_database = Preconditions.checkNotNull(database, "IE01785: database argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE01786: project argument can not be null");
    m_addressSpace = null;
    m_provider = new ProjectTraceProvider(m_project);

    m_debuggerProvider = new DebuggerProvider(new ProjectTargetSettings(m_project));

    if (m_project.isLoaded()) {
      updateProjectDebuggers();
    }

    m_project.addListener(m_listener);

    if (m_project.isLoaded()) {
      for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
        initializeAddressSpaceListeners(addressSpace);
      }
    }
  }

  /**
   * Creates a new project container object.
   *
   * @param database Database the project belongs to.
   * @param project The encapsulated project.
   * @param addressSpace The encapsulated address space.
   */
  public CProjectContainer(final IDatabase database, final INaviProject project,
      final INaviAddressSpace addressSpace) {
    m_database = Preconditions.checkNotNull(database, "IE01788: database argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE01789: project argument can not be null");
    m_addressSpace = addressSpace;
    m_provider = new ProjectTraceProvider(m_project);

    m_debuggerProvider = new DebuggerProvider(new ProjectTargetSettings(m_project));

    final IDebugger debugger = m_addressSpace.getConfiguration().getDebugger();

    if (debugger != null) {
      m_activeDebuggers.put(addressSpace, debugger);
      m_debuggerProvider.addDebugger(debugger);
    }

    m_project.addListener(m_listener);

    initializeAddressSpaceListeners(addressSpace);
  }

  /**
   * Sets up the listeners for a given address space.
   *
   * @param addressSpace The address space the listeners are attached to.
   */
  private void initializeAddressSpaceListeners(final INaviAddressSpace addressSpace) {
    addressSpace.addListener(m_addressSpaceListener);
    addressSpace.getConfiguration().addListener(m_addressSpaceConfigurationListener);

    final List<INaviModule> allModules = getModules();

    if (addressSpace.isLoaded()) {
      addressSpace.getContent().addListener(m_internalContentListener);

      for (final INaviModule module : addressSpace.getContent().getModules()) {
        if (CollectionHelpers.count(allModules, module) == 1) {
          module.addListener(m_moduleListener);
        }
      }
    }
  }

  /**
   * Sets up the listeners for a given module.
   *
   * @param module The module where the listeners are attached to.
   */
  private void initializeModuleListeners(final INaviModule module) {
    module.addListener(m_moduleListener);

    if (module.isLoaded()) {
      notifyModuleViews(module);
    }
  }

  /**
   * Notifies the container listener about new views.
   *
   * @param module The module where views were added.
   */
  private void notifyModuleViews(final INaviModule module) {
    for (final INaviView view : module.getContent().getViewContainer().getViews()) {
      for (final IViewContainerListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we call a listener exception
        try {
          listener.addedView(CProjectContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  /**
   * Removes all listeners from an address space.
   *
   * @param addressSpace The address space from which the listeners are removed.
   */
  private void removeAddressSpaceListeners(final INaviAddressSpace addressSpace) {
    if (addressSpace.isLoaded()) {
      for (final INaviModule module : addressSpace.getContent().getModules()) {
        module.removeListener(m_moduleListener);
      }
    }

    addressSpace.removeListener(m_addressSpaceListener);
    addressSpace.getConfiguration().removeListener(m_addressSpaceConfigurationListener);
  }

  /**
   * Removes all listeners from a module.
   *
   * @param module The module from which the listeners are removed.
   */
  private void removeModuleListeners(final INaviModule module) {
    if (module.isLoaded()) {
      for (final INaviView view : module.getContent().getViewContainer().getViews()) {
        for (final IViewContainerListener listener : m_listeners) {
          // ESCA-JAVA0166: Catch Exception because we call a listener exception
          try {
            listener.deletedView(CProjectContainer.this, view);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    module.removeListener(m_moduleListener);
  }

  /**
   * Rebuilds the active debuggers from information from the address spaces.
   */
  private void updateProjectDebuggers() {
    for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
      final IDebugger debugger = addressSpace.getConfiguration().getDebugger();

      if (debugger != null) {
        m_activeDebuggers.put(addressSpace, debugger);
        m_debuggerProvider.addDebugger(debugger);
      }
    }
  }

  @Override
  public Object getNative() {
    return m_project;
  }

  @Override
  public void addListener(final IViewContainerListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean containsModule(final INaviModule module) {
    Preconditions.checkNotNull(module, "IE02213: Module argument can not be null");

    if (m_project.isLoaded()) {
      for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
        for (final INaviModule m : addressSpace.getContent().getModules()) {
          if (module == m) {
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  public INaviView createView(final String name, final String description) {
    Preconditions.checkNotNull(name, "IE02214: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE02215: Description argument can not be null");

    return m_project.getContent().createView(name, description);
  }

  @Override
  public void deleteView(final INaviView view) throws CouldntDeleteException {
    Preconditions.checkNotNull(view, "IE02216: View argument can not be null");
    m_project.getContent().deleteView(view);
  }

  @Override
  public void dispose() {
    m_project.removeListener(m_listener);

    if (m_addressSpace != null) {
      m_addressSpace.removeListener(m_addressSpaceListener);
      m_addressSpace.getConfiguration().removeListener(m_addressSpaceConfigurationListener);
    }
  }

  @Override
  public List<INaviAddressSpace> getAddressSpaces() {
    return m_project.getContent().getAddressSpaces();
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
    Preconditions.checkNotNull(view, "IE02217: View argument can not be null");

    for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
      if (!addressSpace.isLoaded()) {
        continue;
      }

      for (final INaviModule module : addressSpace.getContent().getModules()) {
        if (!module.isLoaded()) {
          continue;
        }

        final INaviFunction function = module.getContent().getViewContainer().getFunction(view);

        if (function != null) {
          return function;
        }
      }
    }

    return null;
  }

  @Override
  public List<INaviFunction> getFunctions() {
    return new ArrayList<INaviFunction>();
  }

  @Override
  public List<INaviModule> getModules() {
    final Set<INaviModule> modules = new HashSet<INaviModule>();

    for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
      if (addressSpace.isLoaded()) {
        for (final INaviModule naviModule : addressSpace.getContent().getModules()) {
          modules.add(naviModule);
        }
      }
    }

    return new FilledList<INaviModule>(modules);
  }

  @Override
  public String getName() {
    return m_project.getConfiguration().getName();
  }

  @Override
  public List<Pair<INaviView, CTag>> getTaggedViews() {
    return CViewFilter.getTaggedViews(m_project.getContent().getViews());
  }

  @Override
  public List<INaviView> getTaggedViews(final CTag object) {
    return CViewFilter.getTaggedViews(m_project.getContent().getViews(), object);
  }

  @Override
  public ITraceListProvider getTraceProvider() {
    return m_provider;
  }

  @Override
  public List<INaviView> getUserViews() {
    return m_project.getContent().getViews();
  }

  @Override
  public INaviView getView(final INaviFunction function) {
    Preconditions.checkNotNull(function, "IE02218: Function argument can not be null");

    for (final INaviView view : getViews()) {
      if (getFunction(view) == function) {
        return view;
      }
    }

    return null;
  }

  @Override
  public int getViewCount() {
    return m_project.getContent().getViews().size();
  }

  @Override
  public List<INaviView> getViews() {
    // TODO: The isLoaded stuff in this function should not be necessary if access
    // is properly done.

    final Set<INaviView> views = new HashSet<INaviView>();

    if (m_project.isLoaded()) {
      for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
        if (addressSpace.isLoaded()) {
          for (final INaviModule module : addressSpace.getContent().getModules()) {
            if (module.isLoaded()) {
              views.addAll(module.getContent().getViewContainer().getViews());
            }
          }
        }
      }
    }

    if (m_project.isLoaded()) {
      views.addAll(m_project.getContent().getViews());
    }

    return new ArrayList<INaviView>(views);
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final List<UnrelocatedAddress> addresses,
      final boolean all) throws CouldntLoadDataException {
    Preconditions.checkNotNull(addresses, "IE02219: Addresses argument can not be null");
    return m_project.getViewsWithAddresses(addresses, all);
  }

  @Override
  public boolean isLoaded() {
    return m_project.isLoaded();
  }

  @Override
  public void removeListener(final IViewContainerListener listener) {
    Preconditions.checkNotNull(listener, "IE02220: Listener argument can not be null");

    m_listeners.removeListener(listener);
  }

  private class InternalAddressSpaceConfigurationListener extends
      CAddressSpaceConfigurationListenerAdapter {
    @Override
    public void changedDebugger(final INaviAddressSpace addressSpace,
        final DebuggerTemplate debugger) {
      final IDebugger previousDebugger = m_activeDebuggers.get(addressSpace);

      if (previousDebugger != null) {
        m_debuggerProvider.removeDebugger(previousDebugger);
        m_activeDebuggers.remove(addressSpace);
      }

      if (debugger != null) {
        m_debuggerProvider.addDebugger(addressSpace.getConfiguration().getDebugger());
        m_activeDebuggers.put(addressSpace, addressSpace.getConfiguration().getDebugger());
      }
    }

  }

  private class InternalAddressSpaceContentListener implements IAddressSpaceContentListener {
    @Override
    public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      final List<INaviModule> modules = getModules();

      if (CollectionHelpers.count(modules, module) == 1) {
        initializeModuleListeners(module);
      }
    }

    @Override
    public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
        final IAddress address) {
      // Not important
    }

    @Override
    public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      removeModuleListeners(module);
    }
  }

  /**
   * Forwards address space events to the view container listeners.
   */
  private class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
      content.removeListener(m_internalContentListener);
    }

    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      addressSpace.getContent().addListener(m_internalContentListener);

      final List<INaviModule> modules = addressSpace.getContent().getModules();
      final List<INaviModule> allModules = getModules();

      for (final INaviModule module : modules) {
        if (CollectionHelpers.count(allModules, module) == 1) {
          initializeModuleListeners(module);
        }
      }
    }
  }

  /**
   * Forwards project events to the view container listeners.
   */
  private class InternalListener extends CProjectListenerAdapter {
    @Override
    public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
      initializeAddressSpaceListeners(space);
    }

    @Override
    public void addedView(final INaviProject module, final INaviView view) {
      for (final IViewContainerListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we call a listener exception
        try {
          listener.addedView(CProjectContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedView(final INaviProject module, final INaviView view) {
      for (final IViewContainerListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we call a listener exception
        try {
          listener.deletedView(CProjectContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void loadedProject(final CProject project) {
      for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
        initializeAddressSpaceListeners(addressSpace);
      }

      updateProjectDebuggers();

      for (final IViewContainerListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we call a listener exception
        try {
          listener.loaded(CProjectContainer.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedAddressSpace(final INaviProject project,
        final INaviAddressSpace addressSpace) {
      removeAddressSpaceListeners(addressSpace);
    }
  }

  /**
   * Forwards module events to the view container listeners.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      for (final IViewContainerListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we call a listener exception
        try {
          listener.addedView(CProjectContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedView(final INaviModule module, final INaviView view) {
      // ESCA-JAVA0166: Catch Exception because we call a listener exception
      for (final IViewContainerListener listener : m_listeners) {
        try {
          listener.deletedView(CProjectContainer.this, view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void loadedModule(final INaviModule module) {
      Preconditions.checkNotNull(module, "IE02221: Module argument can not be null");
      notifyModuleViews(module);
    }
  }

}
