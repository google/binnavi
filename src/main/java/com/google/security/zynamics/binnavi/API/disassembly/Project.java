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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.ProjectLoadEvents;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// / Represents a single project.
/**
 * Project objects can be used to create collections of individual modules to create an environment
 * for cross-module reverse engineering.
 */
public final class Project implements ApiObject<INaviProject>, ViewContainer {

  /**
   * Database where the project is stored.
   */
  private final Database m_database;

  /**
   * Wrapped internal project object.
   */
  private final INaviProject m_project;

  /**
   * Node tag manager of the database.
   */
  private final TagManager m_nodeTagManager;

  /**
   * View tag manager of the database.
   */
  private final TagManager m_viewTagManager;

  /**
   * Address spaces of the project.
   */
  private IFilledList<AddressSpace> m_addressSpaces;

  /**
   * Views of the project.
   */
  private IFilledList<View> m_views;

  /**
   * Traces recorded for the object.
   */
  private IFilledList<Trace> m_traces;

  /**
   * Debugger templates of the project.
   */
  private IFilledList<DebuggerTemplate> m_debuggerTemplates;

  /**
   * Keeps the API project object synchronized with the internal project object.
   */
  private final InternalListener m_internalListener = new InternalListener();

  /**
   * Listeners that are notified about changes in the project.
   */
  private final ListenerProvider<IProjectListener> m_listeners = new ListenerProvider<>();

  // / @cond INTERNAL
  /**
   * Creates a new API project object.
   *
   * @param database Database where the project is stored.
   * @param project Wrapped internal project object.
   * @param nodeTagManager Node tag manager of the database.
   * @param viewTagManager View tag manager of the database.
   */
  // / @endcond
  public Project(final Database database, final INaviProject project,
      final TagManager nodeTagManager, final TagManager viewTagManager) {
    m_database = Preconditions.checkNotNull(database, "Error: Database argument can't be null");
    m_project = Preconditions.checkNotNull(project, "Error: Project argument can't be null");
    m_nodeTagManager = Preconditions.checkNotNull(nodeTagManager,
        "Error: Node tag manager argument can't be null");
    m_viewTagManager = Preconditions.checkNotNull(viewTagManager,
        "Error: View  tag manager argument can't be null");

    if (project.isLoaded()) {
      convertData();
    }

    project.addListener(m_internalListener);
  }

  /**
   * Converts the internal project data to API project data.
   */
  private void convertData() {
    m_debuggerTemplates = new FilledList<DebuggerTemplate>();

    final List<DebuggerTemplate> apiTemplates =
        m_database.getDebuggerTemplateManager().getDebuggerTemplates();

    for (final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate
        debuggerTemplate : m_project.getConfiguration().getDebuggers()) {
      m_debuggerTemplates.add(ObjectFinders.getObject(debuggerTemplate, apiTemplates));
    }

    m_traces = new FilledList<Trace>();

    for (final TraceList trace : m_project.getContent().getTraces()) {
      m_traces.add(new Trace(trace));
    }

    m_addressSpaces = new FilledList<AddressSpace>();

    for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
      m_addressSpaces.add(new AddressSpace(m_database, this, addressSpace));
    }

    m_views = new FilledList<View>();

    for (final INaviView view : m_project.getContent().getViews()) {
      m_views.add(new View(this, view, m_nodeTagManager, m_viewTagManager));
    }
  }

  @Override
  public INaviProject getNative() {
    return m_project;
  }

  // ! Adds a debugger template to the project.
  /**
   * Adds a debugger template that describes a debugger that is available in the address spaces of
   * the project.
   *
   * @param debuggerTemplate The debugger template to add.
   *
   * @throws CouldntSaveDataException Thrown if the debugger template could not be written to the
   *         database.
   */
  public void addDebuggerTemplate(final DebuggerTemplate debuggerTemplate)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(debuggerTemplate,
        "Error: Debugger template argument can not be null");

    try {
      m_project.getConfiguration().addDebugger(debuggerTemplate.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Adds a project listener.
  /**
   * Adds an object that is notified about changes in the project.
   *
   * @param listener The listener object that is notified about changes in the project.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         project.
   */
  public void addListener(final IProjectListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Closes the project.
  /**
   * Closes the project. Listeners listening on this project have the option to veto the close
   * operation in the closingProject method.
   *
   * @return True, if the project was closed. False, if the close operation was vetoed.
   */
  public boolean close() {
    return m_project.close();
  }

  // ! Creates a new address space.
  /**
   * Creates a new address space in the project.
   *
   * @param name The name of the new address space.
   *
   * @return The created address space.
   *
   * @throws CouldntSaveDataException Thrown if the address space could not be created.
   * @throws CouldntLoadDataException
   */
  public AddressSpace createAddressSpace(final String name) throws CouldntSaveDataException,
      CouldntLoadDataException {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The address space has not yet been loaded");
    }

    try {
      final CAddressSpace addressSpace = m_project.getContent().createAddressSpace(name);

      return ObjectFinders.getObject(addressSpace, m_addressSpaces);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Creates a new project view.
  /**
   * Creates a new empty project view.
   *
   * @param name The name of the view.
   * @param description The description of the view.
   *
   * @return The created view.
   */
  @Override
  public View createView(final String name, final String description) {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The project has not yet been loaded");
    }

    final INaviView newView = m_project.getContent().createView(name, description);

    return ObjectFinders.getObject(newView, m_views);
  }

  // ! Creates a copy of a view.
  /**
   * Creates a new view by copying an existing view.
   *
   * @param view The view to copy.
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The created view.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be created.
   */
  public View createView(final View view, final String name, final String description)
      throws CouldntSaveDataException {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The project has not yet been loaded");
    }

    Preconditions.checkNotNull(view, "Error: View argument can't be null");

    final INaviView newView = m_project.getContent().createView(view.getNative(), name, description);

    return ObjectFinders.getObject(newView, m_views);
  }

  // ! Deletes an address space.
  /**
   * Permanently deletes an address space from the project.
   *
   * @param addressSpace The address space to delete.
   *
   * @return True, if the address space was deleted. False, if some part of BinNavi vetoed the
   *         deletion operation.
   *
   * @throws CouldntDeleteException Thrown if the address space could not be deleted from the
   *         database.
   */
  public boolean deleteAddressSpace(final AddressSpace addressSpace) throws CouldntDeleteException {
    Preconditions.checkNotNull(addressSpace, "Error: Address space argument can not be null");

    try {
      return m_project.getContent().removeAddressSpace(addressSpace.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException e) {
      throw new CouldntDeleteException(e);
    }
  }

  // ! Deletes a view.
  /**
   * Permanently deletes a view from the project.
   *
   * @param view The view to delete.
   *
   * @return True, if the view was deleted. False, if some part of BinNavi vetoed the deletion
   *         operation.
   *
   * @throws CouldntDeleteException Thrown if the view could not be deleted from the database.
   */
  public boolean deleteView(final View view) throws CouldntDeleteException {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    try {
      return m_project.getContent().deleteView(view.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException e) {
      throw new CouldntDeleteException(e);
    }
  }

  // / @cond INTERNAL
  /**
   * Frees allocated resources.
   */
  // / @endcond
  public void dispose() {
    m_project.removeListener(m_internalListener);
  }

  // ! Address spaces of the project.
  /**
   * Returns a list of address spaces that are present in the project.
   *
   * @return A list of address spaces.
   *
   * @throws IllegalStateException Thrown if the project was not loaded before.
   */
  public List<AddressSpace> getAddressSpaces() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The project has not yet been loaded");
    }

    return new ArrayList<AddressSpace>(m_addressSpaces);
  }

  // ! Creation date of the project.
  /**
   * Returns the creation date of the project. This is the date when the project was first written
   * to the database.
   *
   * @return The creation date of the project.
   */
  public Date getCreationDate() {
    return m_project.getConfiguration().getCreationDate();
  }

  // ! Database the project belongs to.
  /**
   * Returns the database the project belongs to.
   *
   * @return The database the project belongs to.
   */
  @Override
  public Database getDatabase() {
    return m_database;
  }

  // ! Debugger templates associated with the project.
  /**
   * Returns the debugger templates that describe the debuggers that are available for debugging the
   * address spaces of the project.
   *
   * @return A list of debugger templates.
   */
  public List<DebuggerTemplate> getDebuggerTemplates() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The project is not loaded");
    }

    return new ArrayList<DebuggerTemplate>(m_debuggerTemplates);
  }

  // ! Project description.
  /**
   * Returns the description string of the project.
   *
   * @return The description string of the project.
   */
  public String getDescription() {
    return m_project.getConfiguration().getDescription();
  }

  @Override
  public Function getFunction(final INaviFunction function) {
    Preconditions.checkNotNull(function, "Error: Function argument can not be null");

    if (!isLoaded()) {
      throw new IllegalStateException("Error: The project has not yet been loaded");
    }

    for (final AddressSpace addressSpace : m_addressSpaces) {
      if (addressSpace.isLoaded()) {
        for (final Module module : addressSpace.getModules()) {
          if (module.isLoaded()) {
            final Function mfunction = module.getFunction(function);

            if ((mfunction != null) && (mfunction.getNative() == function)) {
              return mfunction;
            }
          }
        }
      }
    }

    return null;
  }

  // ! Functions that belong to the project.
  /**
   * Returns all functions that belong to the project.
   *
   * @return The functions that belong to the (loaded) modules in the project.
   */
  @Override
  public List<Function> getFunctions() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: Project is not loaded");
    }

    final List<Function> list = new ArrayList<>();

    for (final AddressSpace addressSpace : m_addressSpaces) {
      if (addressSpace.isLoaded()) {
        for (final Module module : addressSpace.getModules()) {
          if (module.isLoaded()) {
            list.addAll(module.getFunctions());
          }
        }
      }
    }

    return new ArrayList<Function>(list);
  }

  // ! Modification date of the project.
  /**
   * Returns the modification date of the project. This is the date when the project was last
   * modified.
   *
   * @return The modification date of the project.
   */
  public Date getModificationDate() {
    return m_project.getConfiguration().getModificationDate();
  }

  // ! Name of the project.
  /**
   * Returns the name of the project.
   *
   * @return The name of the project.
   */
  public String getName() {
    return m_project.getConfiguration().getName();
  }

  // ! Recorded debug traces of the project.
  /**
   * Returns all debug traces recorded for this project.
   *
   * @return A list of debug traces.
   */
  public List<Trace> getTraces() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: The project is not loaded");
    }

    return new ArrayList<Trace>(m_traces);
  }

  // ! Views of the project.
  /**
   * Returns all project views of the project.
   *
   * @return A list of project views.
   */
  public List<View> getViews() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: Project must be loaded first");
    }

    return new ArrayList<View>(m_views);
  }

  // ! Checks if the project is loaded.
  /**
   * Returns a flag that indicates whether the project data has been loaded from the database.
   *
   * @return True, if the project has been loaded. False, otherwise.
   */
  public boolean isLoaded() {
    return m_project.isLoaded();
  }

  // ! Loads the project.
  /**
   * Loads the project data from the database.
   *
   * @throws IllegalStateException Thrown if the project is already loaded.
   * @throws CouldntLoadDataException Thrown if the project data could not be loaded from the
   *         database.
   */
  public void load() throws CouldntLoadDataException {
    if (isLoaded()) {
      return;
    }

    try {
      m_project.load();
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException | LoadCancelledException e) {
      throw new CouldntLoadDataException(e);
    } 
  }

  // ! Removes a debugger template from the debugger.
  /**
   * Removes a debugger template that describes a debugger that was previously available for
   * debugging the address spaces of the project.
   *
   * @param debuggerTemplate The debugger template to remove from the project.
   *
   * @throws CouldntDeleteException Thrown if the debugger could not be removed from the project.
   */
  public void removeDebuggerTemplate(final DebuggerTemplate debuggerTemplate)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(debuggerTemplate,
        "Error: Debugger template argument can not be null");

    try {
      m_project.getConfiguration().removeDebugger(debuggerTemplate.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntDeleteException(e);
    }
  }

  // ! Removes a project listener.
  /**
   * Removes a listener object from the project.
   *
   * @param listener The listener object to remove from the project.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the project.
   */
  public void removeListener(final IProjectListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the project description.
  /**
   * Changes the description of the project.
   *
   * @param description The new description of the project.
   *
   * @throws IllegalArgumentException Thrown if the description argument is null.
   * @throws CouldntSaveDataException Thrown if the description could not be changed.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    try {
      m_project.getConfiguration().setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the project name.
  /**
   * Changes the name of the project.
   *
   * @param name The new name of the project.
   *
   * @throws IllegalArgumentException Thrown if the name argument is null.
   * @throws CouldntSaveDataException Thrown if the name could not be changed.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      m_project.getConfiguration().setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Printable representation of the project.
  /**
   * Returns the string representation of the project.
   *
   * @return The string representation of the project.
   */
  @Override
  public String toString() {
    final StringBuilder spacesString = new StringBuilder();

    if (isLoaded()) {
      boolean addComma = false;

      for (final AddressSpace addressSpace : getAddressSpaces()) {
        if (addComma) {
          spacesString.append(", ");
        }

        addComma = true;

        spacesString.append("'");
        spacesString.append(addressSpace.getName());
        spacesString.append("'");
      }
    } else {
      spacesString.append(
          String.format("unloaded, %d address spaces", m_project.getAddressSpaceCount()));
    }

    return String.format("Project '%s' [%s]", getName(), spacesString);
  }

  /**
   * Keeps the API project object synchronized with the internal project object.
   */
  private class InternalListener implements
      com.google.security.zynamics.binnavi.disassembly.IProjectListener {

    @Override
    public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
      final AddressSpace newSpace = new AddressSpace(m_database, Project.this, space);

      m_addressSpaces.add(newSpace);

      for (final IProjectListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedAddressSpace(Project.this, newSpace);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedDebugger(final INaviProject project,
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      final DebuggerTemplate newTemplate = ObjectFinders.getObject(debugger,
          m_database.getDebuggerTemplateManager().getDebuggerTemplates());

      m_debuggerTemplates.add(newTemplate);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.addedDebuggerTemplate(Project.this, newTemplate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedTrace(final INaviProject project, final TraceList trace) {
      final Trace newTrace = new Trace(trace);

      m_traces.add(newTrace);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.addedTrace(Project.this, newTrace);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedView(final INaviProject project, final INaviView view) {
      final View newView = new View(Project.this, view, m_nodeTagManager, m_viewTagManager);

      m_views.add(newView);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.addedView(Project.this, newView);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDescription(final INaviProject project, final String description) {
      for (final IProjectListener listener : m_listeners) {
        try {
          listener.changedDescription(Project.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedModificationDate(final INaviProject project, final Date date) {
      for (final IProjectListener listener : m_listeners) {
        try {
          listener.changedModificationDate(Project.this, date);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final INaviProject project, final String name) {
      for (final IProjectListener listener : m_listeners) {
        try {
          listener.changedName(Project.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void closedProject(final CProject project) {
      m_addressSpaces = null;
      m_views = null;
      m_traces = null;
      m_debuggerTemplates = null;

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.closedProject(Project.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean closingProject(final CProject project) {
      for (final IProjectListener listener : m_listeners) {
        try {
          if (!listener.closingProject(Project.this)) {
            return false;
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      return true;
    }

    @Override
    public void deletedView(final INaviProject project, final INaviView view) {
      final View deletedView = ObjectFinders.getObject(view, m_views);

      m_views.remove(deletedView);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.deletedView(Project.this, deletedView);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean deletingTrace(final INaviProject project, final TraceList trace) {
      return true;
    }

    @Override
    public void loadedProject(final CProject project) {
      convertData();

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.loadedProject(Project.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean loading(final ProjectLoadEvents event, final int counter) {
      return true;
    }

    @Override
    public void removedAddressSpace(final INaviProject project,
        final INaviAddressSpace addressSpace) {
      final AddressSpace deletedAddressSpace =
          ObjectFinders.getObject(addressSpace, m_addressSpaces);

      m_addressSpaces.remove(deletedAddressSpace);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.deletedAddressSpace(Project.this, deletedAddressSpace);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedDebugger(final INaviProject project,
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      final DebuggerTemplate removedTemplate = ObjectFinders.getObject(debugger,
          m_database.getDebuggerTemplateManager().getDebuggerTemplates());

      m_debuggerTemplates.remove(removedTemplate);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.removedDebuggerTemplate(Project.this, removedTemplate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedTrace(final INaviProject project, final TraceList trace) {
      final Trace deletedTrace = ObjectFinders.getObject(trace, m_traces);

      m_traces.remove(deletedTrace);

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.deletedTrace(Project.this, deletedTrace);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
