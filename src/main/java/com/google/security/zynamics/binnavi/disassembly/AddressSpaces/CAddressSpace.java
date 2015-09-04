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
package com.google.security.zynamics.binnavi.disassembly.AddressSpaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Represents an address space of a project. An address space is pretty much the same as an address
 * space in real processes.
 */
public final class CAddressSpace implements INaviAddressSpace {
  private final INaviProject m_project;

  /**
   * The SQL provider that is used to synchronize the address space object with the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listeners that are notified about changes in the address space.
   */
  private final ListenerProvider<IAddressSpaceListener> m_listeners =
      new ListenerProvider<IAddressSpaceListener>();

  /**
   * Number of modules in the address space.
   */
  private final int m_moduleCount;

  /**
   * Flag that indicates whether the address space is currently being loaded from the database.
   */
  private boolean m_isLoading = false;

  /**
   * Reports address space loading events to listeners.
   */
  private final CAddressSpaceLoaderReporter m_loadReporter = new CAddressSpaceLoaderReporter(
      m_listeners);

  /**
   * Configuration of the address space.
   */
  private final CAddressSpaceConfiguration m_configuration;

  /**
   * Content of the address space.
   */
  private CAddressSpaceContent m_content;

  /**
   * Creates a new address space object.
   * 
   * @param addressSpaceId The ID of the address space.
   * @param name The name of the address space.
   * @param description The description of the address space.
   * @param creationDate The creation date of the address space.
   * @param modificationDate The modification date of the address space.
   * @param imageBases The image bases of the modules in the address space.
   * @param debuggerTemplate The debugger template that is used to create debugger objects for this
   *        address space.
   * @param provider The SQL provider that is used to synchronize the state of the address space
   *        object with the database.
   * @param project TODO
   */
  public CAddressSpace(final int addressSpaceId, final String name, final String description,
      final Date creationDate, final Date modificationDate,
      final Map<INaviModule, IAddress> imageBases, final DebuggerTemplate debuggerTemplate,
      final SQLProvider provider, final INaviProject project) {
    Preconditions.checkArgument(addressSpaceId > 0, "IE00023: ID argument must be positive");
    Preconditions.checkNotNull(name, "IE00024: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE00025: Description argument can not be null");
    Preconditions.checkNotNull(creationDate, "IE00026: Creation date argument can not be null");
    Preconditions.checkNotNull(modificationDate,
        "IE00027: Modification date argument can not be null");
    Preconditions.checkNotNull(imageBases, "IE00028: Image bases argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE00029: Provider argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE01790: project argument can not be null");

    m_configuration =
        new CAddressSpaceConfiguration(this, provider, addressSpaceId, name, description,
            creationDate, modificationDate, debuggerTemplate);
    m_moduleCount = imageBases.size();
  }

  @Override
  public void addListener(final IAddressSpaceListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00035: Address space is not loaded");
    }

    for (final IAddressSpaceListener listener : m_listeners) {
      try {
        if (!listener.closing(this)) {
          return false;
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    // Do not actually bother to unload the modules because modules
    // are a logical part of address spaces, not a physical part.

    final CAddressSpaceContent oldContent = m_content;
    m_content = null;

    for (final IAddressSpaceListener listener : m_listeners) {
      try {
        listener.closed(this, oldContent);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return true;
  }

  @Override
  public CAddressSpaceConfiguration getConfiguration() {
    return m_configuration;
  }

  @Override
  public CAddressSpaceContent getContent() {
    Preconditions.checkNotNull(m_content, "IE00191: Address space must be loaded first");

    return m_content;
  }

  @Override
  public int getModuleCount() {
    return isLoaded() ? m_content.getModuleCount() : m_moduleCount;
  }

  @Override
  public INaviProject getProject() {
    return m_project;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00039: Object argument can not be null");

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
        if (!m_loadReporter.report(AddressSpaceLoadEvents.Starting)) {
          throw new LoadCancelledException();
        }

        if (!m_loadReporter.report(AddressSpaceLoadEvents.LoadingModules)) {
          throw new LoadCancelledException();
        }

        final List<Pair<IAddress, INaviModule>> modules = m_provider.loadModules(this);

        m_content = new CAddressSpaceContent(this, m_provider, modules);
      } catch (CouldntLoadDataException | LoadCancelledException e) {
        m_isLoading = false;

        throw e;
      } finally {
        m_loadReporter.report(AddressSpaceLoadEvents.Finished);
      }

      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.loaded(this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      m_isLoading = false;
    }
  }

  @Override
  public void removeListener(final IAddressSpaceListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public String toString() {
    return String.format("Address Space %s (%d)", m_configuration.getName(),
        m_configuration.getId());
  }
}
