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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a single BinNavi project. The information of a project is taken from the database.
 *
 *  Projects can come in two states. Either a project is loaded or it is not loaded. A project that
 * is not loaded does not provide all functionality. For example, you can not access the address
 * spaces of a project that is not loaded. The methods which can not produce valid results before a
 * project is loaded throw an IllegalStateException if they are called before a project is loaded.
 */
public final class CProject implements INaviProject {
  /**
   * The SQL provider that is used to load and save this project.
   */
  private final SQLProvider m_provider;

  /**
   * List of listeners that are notified about changes in the project.
   */
  private final ListenerProvider<IProjectListener> m_listeners =
      new ListenerProvider<IProjectListener>();

  /**
   * Number of address spaces in the project. This variable is only used until the project is
   * loaded.
   */
  private final int m_addressSpaceCount;

  /**
   * Contains the configuration data of the project.
   */
  private final CProjectConfiguration m_configuration;

  /**
   * Contains the loaded content of the project.
   */
  private CProjectContent m_content = null;

  /**
   * Flag that indicates whether the project is currently being loaded from the database.
   */
  private boolean m_isLoading = false;

  /**
   * Reports project loading events to listeners.
   */
  private final CProjectLoaderReporter m_loadReporter = new CProjectLoaderReporter(m_listeners);

  /**
   * Creates a new project object that represents a BinNavi project as stored in the database.
   *
   * @param projectId The ID of the project as it is found in the projects table in the database.
   * @param name The name of the project.
   * @param description The description of the project.
   * @param creationDate The creation date of the project.
   * @param modificationDate The modification date of the project.
   * @param addressSpaceCount Number of address spaces in this project.
   * @param assignedDebuggers Debuggers assigned to this project.
   * @param provider The SQL provider that is used to load and save the project.
   */
  public CProject(final int projectId,
      final String name,
      final String description,
      final Date creationDate,
      final Date modificationDate,
      final int addressSpaceCount,
      final List<DebuggerTemplate> assignedDebuggers,
      final SQLProvider provider) {
    Preconditions.checkArgument(projectId > 0, String.format(
        "IE00226: Project ID %d is invalid. Project IDs must be strictly positive", projectId));
    Preconditions.checkNotNull(name, "IE00227: Project names can't be null");
    Preconditions.checkNotNull(description, "IE00228: Project descriptions can't be null");
    Preconditions.checkNotNull(creationDate, "IE00229: Project creation dates can't be null");
    Preconditions.checkNotNull(modificationDate,
        "IE00230: Project modification dates can't be null");
    Preconditions.checkNotNull(provider, "IE00231: The SQL provider of the project can't be null");

    m_configuration = new CProjectConfiguration(this,
        m_listeners,
        provider,
        projectId,
        name,
        description,
        creationDate,
        modificationDate,
        assignedDebuggers);

    m_addressSpaceCount = addressSpaceCount;
    m_provider = provider;
  }

  @Override
  public void addListener(final IProjectListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00239: Project is not loaded");
    }

    for (final IProjectListener listener : m_listeners) {
      try {
        if (!listener.closingProject(this)) {
          return false;
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_content.close();
    m_content = null;

    for (final IProjectListener listener : m_listeners) {
      try {
        listener.closedProject(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return true;
  }

  /**
   * Returns the number of address spaces in the project.
   *
   * @return The number of address spaces in the project.
   */
  @Override
  public int getAddressSpaceCount() {
    return isLoaded() ? m_content.getAddressSpaces().size() : m_addressSpaceCount;
  }

  @Override
  public CProjectConfiguration getConfiguration() {
    return m_configuration;
  }

  @Override
  public CProjectContent getContent() {
    Preconditions.checkNotNull(m_content, "IE02198: Project is not loaded");

    return m_content;
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final List<UnrelocatedAddress> offset,
      final boolean all) throws CouldntLoadDataException {
    return m_provider.getViewsWithAddress(this, offset, all);
  }

  /**
   * Determines whether the project uses a given debugger.
   *
   * @param debugger The debugger to check for.
   *
   * @return True, if the project uses the debugger. False, otherwise.
   */
  public boolean hasDebugger(final DebuggerTemplate debugger) {
    return m_configuration.hasDebugger(debugger);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00250: Object argument can't be null");

    return object.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(m_provider);
  }

  @Override
  public boolean isLoaded() {
    return m_content != null;
  }

  @Override
  public boolean isLoading() {
    return m_isLoading;
  }

  @Override
  public void load() throws CouldntLoadDataException, LoadCancelledException {
    synchronized (m_loadReporter) {
      if (isLoaded()) {
        return;
      }

      m_isLoading = true;

      try {
        if (!m_loadReporter.report(ProjectLoadEvents.Starting)) {
          throw new LoadCancelledException();
        }

        if (!m_loadReporter.report(ProjectLoadEvents.LoadingAddressSpaces)) {
          throw new LoadCancelledException();
        }

        final List<CAddressSpace> addressSpaces = m_provider.loadAddressSpaces(this);

        for (final CAddressSpace space : addressSpaces) {
          space.load();
        }

        if (!m_loadReporter.report(ProjectLoadEvents.LoadingCallgraphViews)) {
          throw new LoadCancelledException();
        }

        final List<ICallgraphView> userCallgraphs = m_provider.loadCallgraphViews(this);

        if (!m_loadReporter.report(ProjectLoadEvents.LoadingFlowgraphViews)) {
          throw new LoadCancelledException();
        }

        final List<IFlowgraphView> userFlowgraphs = m_provider.loadFlowgraphs(this);

        if (!m_loadReporter.report(ProjectLoadEvents.LoadingMixedgraphViews)) {
          throw new LoadCancelledException();
        }

        final List<INaviView> userMixedgraphs = m_provider.loadMixedgraphs(this);

        if (!m_loadReporter.report(ProjectLoadEvents.LoadingTraces)) {
          throw new LoadCancelledException();
        }

        final List<TraceList> traces = m_provider.loadTraces(this);

        final ArrayList<INaviView> views = new ArrayList<INaviView>(userCallgraphs);
        views.addAll(userFlowgraphs);
        views.addAll(userMixedgraphs);

        m_content = new CProjectContent(this,
            m_listeners,
            m_provider,
            addressSpaces,
            views,
            new FilledList<TraceList>(traces));
      } catch (CouldntLoadDataException | LoadCancelledException e) {
        m_isLoading = false;

        throw e;
      } finally {
        m_loadReporter.report(ProjectLoadEvents.Finished);
      }

      for (final IProjectListener listener : m_listeners) {
        try {
          listener.loadedProject(this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      m_isLoading = false;
    }
  }

  @Override
  public String readSetting(final String key) throws CouldntLoadDataException {
    return m_provider.readSetting(this, key);
  }

  @Override
  public void removeListener(final IProjectListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void writeSetting(final String key, final String value) throws CouldntSaveDataException {
    m_provider.writeSetting(this, key, value);
  }
}
