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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CProjectViewGenerator;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * Describes the loadable content of a project.
 */
public final class CProjectContent {
  /**
   * The project the content belongs to.
   */
  private final INaviProject m_project;

  /**
   * Listeners that are notified about changes in the content.
   */
  private final ListenerProvider<IProjectListener> m_listeners;

  /**
   * Synchronizes the project content with the database.
   */
  private final SQLProvider m_provider;

  /**
   * The list of address spaces in the project.
   */
  private final List<CAddressSpace> m_addressSpaces;

  /**
   * List of project views that belong to the project.
   */
  private final List<INaviView> m_views;

  /**
   * Traces recorded for this project.
   */
  private final IFilledList<TraceList> m_traces;

  /**
   * Creates a new content object.
   * 
   * @param project The project the content belongs to.
   * @param listeners Listeners that are notified about changes in the content.
   * @param provider Synchronizes the project content with the database.
   * @param addressSpaces Address spaces of the project.
   * @param views List of project views that belong to the project.
   * @param traces Traces recorded for this project.
   */
  public CProjectContent(final INaviProject project,
      final ListenerProvider<IProjectListener> listeners, final SQLProvider provider,
      final List<CAddressSpace> addressSpaces, final List<INaviView> views,
      final IFilledList<TraceList> traces) {
    m_project = Preconditions.checkNotNull(project, "IE02222: Project argument can not be null");
    m_listeners =
        Preconditions.checkNotNull(listeners, "IE02223: Listeners argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE02224: Provider argument can not be null");
    m_addressSpaces =
        Preconditions
            .checkNotNull(addressSpaces, "IE02225: AddressSpaces argument can not be null");
    m_views = Preconditions.checkNotNull(views, "IE02226: Views argument can not be null");
    m_traces = Preconditions.checkNotNull(traces, "IE02227: Traces argument can not be null");
  }

  /**
   * Adds a view to the project.
   * 
   * @param view The view to add to the project.
   */
  public void addView(final INaviView view) {
    Preconditions.checkNotNull(view, "IE00232: View argument can't be null");
    Preconditions.checkArgument(view.getType() == ViewType.NonNative,
        "IE00233: Only non-native views can be added to projects");
    Preconditions.checkArgument(!m_views.contains(view),
        "IE00235: View can not be added to the project more than once");
    Preconditions.checkArgument(view.inSameDatabase(m_provider),
        "IE00236: View and project are not in the same database");

    m_views.add(view);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.addedView(m_project, view);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * TODO: Improve this.
   * 
   * @return Improve this.
   */
  private boolean isLoaded() {
    return true;
  }

  /**
   * Closes the project.
   * 
   * @return True, if the project was closed. False, otherwise.
   */
  public boolean close() {
    for (final CAddressSpace addressSpace : m_addressSpaces) {
      if (addressSpace.isLoaded() && !addressSpace.close()) {
        return false;
      }
    }

    for (final INaviView view : m_views) {
      if (view.isLoaded() && !view.close()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Creates a new address space with the given name in the project. The new address space is
   * immediately saved to the database.
   * 
   * This function is guaranteed to be exception-safe. If an exception is thrown while saving the
   * address space to the database, the project object remains unchanged.
   * 
   * @param name The name of the new address space.
   * 
   * @return The new address space that was created in the project.
   * 
   * @throws CouldntSaveDataException Thrown if the address space couldn't be saved to the database.
   */
  public CAddressSpace createAddressSpace(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00240: The value null is illegal for address space names");

    final CAddressSpace space = m_provider.createAddressSpace(m_project, name);

    m_addressSpaces.add(space);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.addedAddressSpace(m_project, space);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_project.getConfiguration().updateModificationDate();

    return space;
  }

  /**
   * Creates a new trace with the given name in the project. The new trace is immediately saved to
   * the database.
   * 
   * This function is guaranteed to be exception-safe. If an exception is thrown while saving the
   * trace to the database, the project object remains unchanged.
   * 
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   * 
   * @return The new trace that was created in the project.
   * 
   * @throws CouldntSaveDataException Thrown if the trace could not be saved to the database.
   */
  public TraceList createTrace(final String name, final String description)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00242: Name argument can't be null");

    Preconditions.checkNotNull(description, "IE00246: Description argument can't be null");

    final TraceList trace = m_provider.createTrace(m_project, name, description);

    m_traces.add(trace);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.addedTrace(m_project, trace);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_project.getConfiguration().updateModificationDate();

    return trace;
  }

  /**
   * Creates a new view with the given name and description in the project by copying an existing
   * view. The new view is not stored in the database until INaviView::save is called.
   * 
   * @param view The view that is copied.
   * @param name The name of the new view.
   * @param description The description of the new view.
   * 
   * @return The new view that was created in the project.
   */
  public INaviView createView(final INaviView view, final String name, final String description) {
    Preconditions.checkNotNull(view, "IE02228: view argument can not be null");
    Preconditions.checkArgument(view.inSameDatabase(m_provider),
        "IE02229: View and project Content are not in the same database");
    Preconditions.checkNotNull(name, "IE02230: name argument can not be null");
    Preconditions.checkNotNull(description, "IE02231: description argument can not be null");

    final CView newView =
        CView.createUnsavedProjectView(m_project, view, name, description, m_provider);

    addView(newView);
    return newView;
  }

  /**
   * Creates a new empty view with the given name and description in the project. The new view is
   * not stored in the database until INaviView::save is called.
   * 
   * @param name The name of the new view.
   * @param description The description of the new view.
   * 
   * @return The new view that was created in the project.
   */
  public INaviView createView(final String name, final String description) {
    final Date date = new Date();
    final CProjectViewGenerator generator = new CProjectViewGenerator(m_provider, m_project);
    final CView view =
        generator.generate(-1, name, description, ViewType.NonNative, GraphType.MIXED_GRAPH, date,
            date, 0, 0, new HashSet<CTag>(), new HashSet<CTag>(), false);

    try {
      view.load();
    } catch (CouldntLoadDataException | CPartialLoadException | LoadCancelledException e) {
      // This can not happen; new views with ID -1 do not access the database
      // when they are loaded.

      CUtilityFunctions.logException(e);
    }

    addView(view);
    return view;
  }

  /**
   * Deletes a view from the project.
   * 
   * @param view The view to delete.
   * 
   * @return True, if the view was deleted. False, if the delete operation was vetoed.
   * 
   * @throws CouldntDeleteException Thrown if the view could not be deleted.
   */
  public boolean deleteView(final INaviView view) throws CouldntDeleteException {
    Preconditions.checkNotNull(view, "IE00243: View argument can't be null");
    Preconditions.checkArgument(m_views.contains(view), "IE00244: View is not part of the module");

    m_provider.deleteView(view);
    m_views.remove(view);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.deletedView(m_project, view);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_project.getConfiguration().updateModificationDate();

    return true;
  }

  /**
   * Returns a list of all address spaces that can be found in the project.
   * 
   * Note that this function is only available if the project was loaded earlier.
   * 
   * @return A list that contains all address spaces of the project.
   */
  public List<INaviAddressSpace> getAddressSpaces() {
    Preconditions.checkState(isLoaded(),
        "IE00245: You have to load a project before accessing its address spaces");
    return new ArrayList<INaviAddressSpace>(m_addressSpaces);
  }

  /**
   * Returns the number of traces recorded for the project.
   * 
   * @return The number of traces recorded for the project.
   */
  public int getTraceCount() {
    Preconditions.checkState(isLoaded(), "IE00247: Project must be loaded first");
    return m_traces.size();
  }

  /**
   * Returns the traces recorded for the project.
   * 
   * @return The traces recorded for the project
   */
  public List<TraceList> getTraces() {
    Preconditions.checkState(isLoaded(), "IE00248: Project must be loaded first");
    return new ArrayList<TraceList>(m_traces);
  }

  /**
   * Returns all views of the project.
   * 
   * @return All views of the project.
   */
  public List<INaviView> getViews() {
    Preconditions.checkState(isLoaded(), "IE00249: Project must be loaded first");
    return new ArrayList<INaviView>(m_views);
  }

  /**
   * Removes an address space from the project.
   * 
   * Note that if the project is not loaded, the address space is removed
   * 
   * @param addressSpace The address space to remove from the project.
   * 
   * @return True, if the address space was removed. False, if the removal operation was vetoed.
   * 
   * @throws CouldntDeleteException Thrown if the address space could not be removed from the
   *         project.
   */
  public boolean removeAddressSpace(final INaviAddressSpace addressSpace)
      throws CouldntDeleteException {
    Preconditions
        .checkNotNull(addressSpace, "IE00251: The value null is not a valid address space");
    Preconditions.checkState(m_project.isLoaded(),
        "IE00252: You can only delete address spaces from loaded projects");
    Preconditions.checkArgument(m_addressSpaces.contains(addressSpace),
        "IE00253: Address space does not belong to the project");
    Preconditions.checkState(!addressSpace.isLoaded(),
        "IE00868: You can only delete unloaded address spaces");

    m_provider.deleteAddressSpace(addressSpace);

    m_addressSpaces.remove(addressSpace);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.removedAddressSpace(m_project, addressSpace);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_project.getConfiguration().updateModificationDate();

    return true;
  }

  /**
   * Removes a trace from the project.
   * 
   * @param trace The trace to remove.
   * 
   * @throws CouldntDeleteException Thrown if the trace could not be removed from the database.
   */
  public void removeTrace(final TraceList trace) throws CouldntDeleteException {
    Preconditions.checkNotNull(trace, "IE02232: Trace argument can not be null");

    if (!trace.inSameDatabase(m_provider)) {
      throw new IllegalStateException("IE02233: Trace and project are not in the same database");
    }

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.deletingTrace(m_project, trace);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_provider.deleteTrace(trace);

    m_traces.remove(trace);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.removedTrace(m_project, trace);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_project.getConfiguration().updateModificationDate();
  }

  public boolean deleteViewInternal(final INaviView view) {
    Preconditions.checkNotNull(view, "IE00243: View argument can't be null");
    Preconditions.checkArgument(m_views.contains(view), "IE00244: View is not part of the module");

    m_views.remove(view);

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.deletedView(m_project, view);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_project.getConfiguration().updateModificationDate();

    return true;
  }
}
