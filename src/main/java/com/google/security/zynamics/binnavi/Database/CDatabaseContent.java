/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Database;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseContent;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

/**
 * Contains the content of a loaded database.
 */
public final class CDatabaseContent implements IDatabaseContent {
  /**
   * Synchronizes changes with the underlying database.
   */
  private final SQLProvider m_provider;

  /**
   * Database whose content is stored in the object.
   */
  private final IDatabase m_database;

  /**
   * Listeners to be notified about changes in the database.
   */
  private final ListenerProvider<IDatabaseListener> m_listeners;

  /**
   * The projects that can be found in the database. If the database is not loaded, this list is
   * null.
   */
  private final List<INaviProject> m_projects;

  /**
   * The modules that can be found in the database. If the database is not loaded, this list is
   * null.
   */
  private final List<INaviModule> m_modules;

  /**
   * The raw modules that can be found in the database. If the database is not loaded, this list is
   * null.
   */
  private final List<INaviRawModule> m_rawModules;

  /**
   * The debugger template manager of the database.
   */
  private final DebuggerTemplateManager m_debuggerDescriptionManager;

  /**
   * Manages the view tags stored in the database.
   */
  private final CTagManager m_viewTagManager;

  /**
   * Manages the node tags stored in the database.
   */
  private final CTagManager m_nodeTagManager;

  /**
   * Creates a new content object.
   * 
   * @param provider Synchronizes changes with the underlying database.
   * @param database Database whose content is stored in the object.
   * @param listeners Listeners to be notified about changes in the database.
   * @param projects Projects in the database.
   * @param modules Modules in the database.
   * @param rawModules Raw modules in the database.
   * @param viewTagManager View tag manager of the database.
   * @param nodeTagManager Node tag manager of the database.
   * @param debuggerDescriptionManager Debugger description manager of the database.
   */
  public CDatabaseContent(final SQLProvider provider, final IDatabase database,
      final ListenerProvider<IDatabaseListener> listeners, final List<INaviProject> projects,
      final List<INaviModule> modules, final List<INaviRawModule> rawModules,
      final CTagManager viewTagManager, final CTagManager nodeTagManager,
      final DebuggerTemplateManager debuggerDescriptionManager) {
    m_viewTagManager =
        Preconditions.checkNotNull(viewTagManager, "IE00051: View tag manager can not be null");
    m_provider =
        Preconditions.checkNotNull(provider, "IE00053: Database content provider can not be null");
    m_database = Preconditions.checkNotNull(database, "IE00390: Database can not be null");
    m_listeners =
        Preconditions.checkNotNull(listeners, "IE00391: Database listeners can not be null");
    m_projects = Preconditions.checkNotNull(projects, "IE00410: Projects can not be null");
    m_modules = Preconditions.checkNotNull(modules, "IE00412: Modules can not be null");
    m_rawModules = Preconditions.checkNotNull(rawModules, "IE00420: Raw modules can not be null");
    m_nodeTagManager =
        Preconditions.checkNotNull(nodeTagManager, "IE00441: Node tag manager can not be null");
    m_debuggerDescriptionManager =
        Preconditions.checkNotNull(debuggerDescriptionManager,
            "IE00442: Debugger descriptor manager can not be null");
  }

  /**
   * Determines whether a module backed by a given raw module already exists.
   * 
   * @param modules Modules to search through.
   * @param rawModule Modules to search for.
   * 
   * @return True, if a module backed by the raw module exists. False, otherwise.
   */
  private static boolean hasModule(final List<INaviModule> modules, final INaviRawModule rawModule) {
    // TODO (timkornau): Equality compare does not really work

    return CollectionHelpers.any(modules, new ICollectionFilter<INaviModule>() {
      @Override
      public boolean qualifies(final INaviModule item) {
        return item.getConfiguration().getRawModule().getId() == rawModule.getId();
      }
    });
  }

  /**
   * Creates a new module for a raw module.
   * 
   * @param rawModule The raw module that backs the module.
   * 
   * @return The created module.
   * 
   * @throws CouldntLoadDataException Thrown if the module data could not be loaded.
   * @throws CouldntSaveDataException Thrown if the module could not be created.
   */
  private CModule createModule(final INaviRawModule rawModule) throws CouldntLoadDataException,
      CouldntSaveDataException {
    final CModule newModule = m_provider.createModule(rawModule);

    m_modules.add(newModule);

    return newModule;
  }

  /**
   * After deleting a module, this function removes the module from all address spaces.
   * 
   * @param module The module that was deleted and needs to be removed.
   */
  private void removeDeletedModuleFromNamespaces(final INaviModule module) {
    for (final INaviProject project : m_projects) {
      if (!project.isLoaded()) {
        continue;
      }

      for (final INaviAddressSpace addressSpace : project.getContent().getAddressSpaces()) {
        if (addressSpace.isLoaded() && addressSpace.getContent().getModules().contains(module)) {
          try {
            addressSpace.getContent().removeModule(module);
          } catch (CouldntDeleteException | CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }

  /**
   * Creates BinNavi modules for a list of raw modules.
   * 
   * @param modules List of existing modules.
   * @param rawModules List of existing raw modules.
   * 
   * @return The created modules.
   */
  List<INaviModule> initializeRawModules(final List<INaviModule> modules,
      final List<INaviRawModule> rawModules) {
    final List<INaviModule> newModules = new ArrayList<INaviModule>();

    for (final INaviRawModule rawModule : rawModules) {
      if (!hasModule(modules, rawModule)) {
        try {
          newModules.add(createModule(rawModule));
        } catch (CouldntLoadDataException | CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    return newModules;
  }

  /**
   * Adds a new project to the database.
   * 
   * This function is guaranteed to be thread-safe. If the new project could not be saved to the
   * database, the state of the database object remains unchanged.
   * 
   * @param name The name of the new project.
   * 
   * @return The added project.
   * 
   * @throws IllegalArgumentException Thrown if the name of the new project is null.
   * @throws CouldntSaveDataException Thrown if the new project could not be saved to the database.
   */
  @Override
  public INaviProject addProject(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00661: Project name can not be null");
    Preconditions.checkArgument(m_database.isConnected(),
        "IE00662: Database must be connected before a project can be added");
    Preconditions.checkArgument(m_database.isLoaded(),
        "IE00663: Database must be loaded before a project can be added");

    final CProject newProject = m_provider.createProject(name);

    m_projects.add(newProject);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.addedProject(m_database, newProject);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return newProject;
  }

  @Override
  public void delete(final INaviModule module) throws CouldntDeleteException {
    Preconditions.checkNotNull(module, "IE00670: Module can not be null");
    Preconditions.checkArgument(m_database.isConnected(),
        "IE00671: Database must be connected before you can delete modules");
    Preconditions.checkArgument(m_database.isLoaded(),
        "IE00672: Database must be loaded before you can delete modules");
    Preconditions.checkArgument(m_modules.contains(module),
        "IE00673: Module does not belong to this database");

    m_provider.deleteModule(module);
    m_modules.remove(module);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.deletedModule(m_database, module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    removeDeletedModuleFromNamespaces(module);
  }

  @Override
  public void delete(final INaviProject project) throws CouldntDeleteException {
    Preconditions.checkNotNull(project, "IE00674: Project can not be null");
    Preconditions.checkArgument(m_database.isConnected(),
        "IE00675: Database must be connected before you can delete projects");
    Preconditions.checkArgument(m_database.isLoaded(),
        "IE00676: Database must be loaded before you can delete projects");
    Preconditions.checkArgument(m_projects.contains(project),
        "IE00677: Project does not belong to the database");

    m_provider.deleteProject(project);
    m_projects.remove(project);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.deletedProject(m_database, project);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void delete(final INaviRawModule rawModule) throws CouldntDeleteException {
    Preconditions.checkNotNull(rawModule, "IE00006: Raw module can not be null");
    Preconditions.checkArgument(m_database.isConnected(),
        "IE00031: Database must be connected before you can delete raw modules");
    Preconditions.checkArgument(m_database.isLoaded(),
        "IE00036: Database must be loaded before you can delete raw modules");
    Preconditions.checkArgument(m_rawModules.contains(rawModule),
        "IE00038: Raw module does not belong to this database");

    m_provider.deleteRawModule(rawModule);
    m_rawModules.remove(rawModule);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.deletedRawModule(m_database, rawModule);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public DebuggerTemplateManager getDebuggerTemplateManager() {
    return m_debuggerDescriptionManager;
  }

  @Override
  public INaviModule getModule(final int moduleId) {
    for (final INaviModule module : m_modules) {
      if (module.getConfiguration().getId() == moduleId) {
        return module;
      }
    }

    return null;
  }

  @Override
  public List<INaviModule> getModules() {
    return new ArrayList<INaviModule>(m_modules);
  }

  @Override
  public CTagManager getNodeTagManager() {
    return m_nodeTagManager;
  }

  @Override
  public List<INaviProject> getProjects() throws IllegalStateException {
    return new ArrayList<INaviProject>(m_projects);
  }

  @Override
  public List<INaviRawModule> getRawModules() {
    return new ArrayList<INaviRawModule>(m_rawModules);
  }

  @Override
  public CTagManager getViewTagManager() {
    return m_viewTagManager;
  }

  @Override
  public void refreshRawModules() throws CouldntLoadDataException {
    Preconditions.checkArgument(m_database.isConnected(), "IE00687: Not connected to the database");
    Preconditions.checkArgument(m_database.isLoaded(),
        "IE00688: Raw modules were not loaded previously");

    final List<INaviRawModule> oldModules = m_rawModules;

    final List<INaviRawModule> refreshedModules = m_provider.loadRawModules();

    m_rawModules.clear();
    m_rawModules.addAll(refreshedModules);

    final List<INaviModule> newModules = initializeRawModules(m_modules, refreshedModules);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedRawModules(m_database, oldModules, refreshedModules);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    for (final INaviModule naviModule : newModules) {
      for (final IDatabaseListener listener : m_listeners) {
        try {
          listener.addedModule(m_database, naviModule);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

  }
}
