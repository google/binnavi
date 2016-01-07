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
package com.google.security.zynamics.binnavi.disassembly.AddressSpaces;

import java.util.Date;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.AddressSpaceConfigurationBackend;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.ProjectTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.TcpDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Contains the configuration options of an address space.
 */
public final class CAddressSpaceConfiguration {
  /**
   * Listeners that are notified about changes in the address space.
   */
  private final ListenerProvider<IAddressSpaceConfigurationListener> m_listeners =
      new ListenerProvider<IAddressSpaceConfigurationListener>();

  /**
   * The address space the configuration belongs to.
   */
  private final CAddressSpace m_addressSpace;

  /**
   * Provides database access for the address space configuration object.
   */
  private final AddressSpaceConfigurationBackend m_provider;

  /**
   * The ID of the address space.
   */
  private final int m_id;

  /**
   * The name of the address space.
   */
  private String m_name;

  /**
   * The description of the address space.
   */
  private String m_description;

  /**
   * The creation date of the address space.
   */
  private final Date m_creationDate;

  /**
   * The modification date of the address space.
   */
  private Date m_modificationDate;

  /**
   * The debugger template that is used to create a concrete debugger object for the address space
   * when necessary.
   */
  private DebuggerTemplate m_debuggerTemplate = null;

  /**
   * The debugger object associated with the address space.
   */
  private IDebugger m_debugger = null;

  /**
   * Creates a new address space configuration object.
   * 
   * @param addressSpace The address space the configuration belongs to.
   * @param provider Provides database access for the address space configuration object.
   * @param addressSpaceId The ID of the address space.
   * @param name The name of the address space.
   * @param description The description of the address space.
   * @param creationDate The creation date of the address space.
   * @param modificationDate The modification date of the address space.
   * @param debuggerTemplate The debugger template that is used to create a concrete debugger object
   *        for the address space when necessary.
   */
  public CAddressSpaceConfiguration(final CAddressSpace addressSpace,
      final AddressSpaceConfigurationBackend provider, final int addressSpaceId,
      final String name, final String description, final Date creationDate,
      final Date modificationDate, final DebuggerTemplate debuggerTemplate) {
    m_addressSpace = addressSpace;
    m_provider = provider;
    m_id = addressSpaceId;
    m_name = name;
    m_description = description;
    m_creationDate = new Date(creationDate.getTime());
    m_modificationDate = new Date(modificationDate.getTime());
    m_debuggerTemplate = debuggerTemplate;
  }

  /**
   * Takes a debugger template and updates the debugger object of the address space accordingly if
   * possible.
   * 
   * @param template The debugger template that provides the debugger information.
   */
  private void updateDebugger(final DebuggerTemplate template) {
    // The requirements that enable the construction of a new debugger object are
    // as follows:
    //
    // 1. There must be a debugger template. Having a null-template is possible too.
    // 2. The address space must be loaded, otherwise we do not know the file mappings
    // for the modules inside the address space.
    // 3. No debugger exists yet or the existing debugger is not active. We can
    // not replace a debugger that's currently in use.

    if (m_addressSpace.isLoaded() && ((m_debugger == null) || !m_debugger.isConnected())) {
      if (template == null) {
        m_debugger = null;
      } else {

        m_debugger =
            new TcpDebugger(template, new ProjectTargetSettings(m_addressSpace.getProject()));

        for (final INaviModule module : m_addressSpace.getContent().getModules()) {
          m_debugger.setAddressTranslator(module, module.getConfiguration().getFileBase(),
              m_addressSpace.getContent().getImageBase(module));
        }
      }
    }
  }

  /**
   * Adds a listener object that is notified about changes in the configuration.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final IAddressSpaceConfigurationListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the creation date of the address space.
   * 
   * @return The creation date of the address space.
   */
  public Date getCreationDate() {
    return new Date(m_creationDate.getTime());
  }

  /**
   * Returns the debugger object that can be used to debug the address space. There is not
   * necessarily a debugger configured for an address space, so this object can be null.
   * 
   * @return The debugger object of the address space.
   */
  public IDebugger getDebugger() {
    return m_debugger;
  }

  /**
   * Returns the assigned debugger template.
   * 
   * @return The assigned debugger template.
   */
  public DebuggerTemplate getDebuggerTemplate() {
    return m_debuggerTemplate;
  }

  /**
   * Returns the description of the address space.
   * 
   * @return The description of the address space
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Returns the ID of the address space
   * 
   * @return The ID of the address space
   */
  public int getId() {
    return m_id;
  }

  /**
   * Returns the modification date of the address space
   * 
   * @return The modification date of the address space
   */
  public Date getModificationDate() {
    return new Date(m_modificationDate.getTime());
  }

  /**
   * Returns the name of the address space
   * 
   * @return The name of the address space
   */
  public String getName() {
    return m_name;
  }

  /**
   * Removes a previously added listener.
   * 
   * @param listener The previously added listener.
   */
  public void removeListener(final IAddressSpaceConfigurationListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Sets the debugger.
   * 
   * @param debugger The new debugger.
   */
  public void setDebugger(final TcpDebugger debugger) {
    m_debugger = debugger;
  }

  /**
   * Changes the debugger template.
   * 
   * @param template The new debugger template.
   * 
   * @throws CouldntSaveDataException Thrown if the debugger template could not be changed.
   */
  public void setDebuggerTemplate(final DebuggerTemplate template) throws CouldntSaveDataException {
    m_provider.assignDebugger(m_addressSpace, template);

    m_debuggerTemplate = template;

    m_addressSpace.getConfiguration().updateDebugger(m_debuggerTemplate);

    for (final IAddressSpaceConfigurationListener listener : m_listeners) {
      try {
        listener.changedDebugger(m_addressSpace, template);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Sets the description of the address space.
   * 
   * @param description The new description of the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the new description of the address space could not
   *         be saved to the database.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00044: Description argument can not be null");

    if (m_description.equals(description)) {
      return;
    }

    m_provider.setDescription(m_addressSpace, description);

    m_description = description;

    for (final IAddressSpaceConfigurationListener listener : m_listeners) {
      try {
        listener.changedDescription(m_addressSpace, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Sets the name of the address space.
   * 
   * @param name The new name of the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the new name could not be written to the database.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00049: Name argument can not be null");

    if (m_name.equals(name)) {
      return;
    }

    m_provider.setName(m_addressSpace, name);

    m_name = name;

    for (final IAddressSpaceConfigurationListener listener : m_listeners) {
      try {
        listener.changedName(m_addressSpace, name);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateModificationDate();
  }

  /**
   * Updates the modification date of the address space.
   */
  public void updateModificationDate() {
    try {
      m_modificationDate = m_provider.getModificationDate(m_addressSpace);

      for (final IAddressSpaceConfigurationListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we call a listener function
        try {
          listener.changedModificationDate(m_addressSpace, m_modificationDate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);
    }
  }
}
