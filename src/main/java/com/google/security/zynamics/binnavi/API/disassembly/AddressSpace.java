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
import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.AddressSpaceLoadEvents;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceConfigurationListener;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// / Represents a single address space.
/**
 * Address spaces represent a single snapshot of memory. This snapshot can contain multiple modules
 * that can interact with each other.
 */
public final class AddressSpace implements ApiObject<INaviAddressSpace> {

  /**
   * Database where the address space is stored.
   */
  private final Database m_database;

  /**
   * The project the address space belongs to.
   */
  private final Project m_project;

  /**
   * The wrapped internal address space.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * The modules of the address space.
   */
  private List<Module> m_modules;

  /**
   * Debugger used to debug the address space.
   */
  private Debugger m_debugger;

  /**
   * Listeners that are notified about changes in the address space.
   */
  private final ListenerProvider<IAddressSpaceListener> m_listeners =
      new ListenerProvider<IAddressSpaceListener>();

  /**
   * Keeps the API address space object synchronized with the internal address space object.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Keeps the API address space object synchronized with the internal address space configuration
   * object.
   */
  private final InternalConfigurationListener m_configurationListener =
      new InternalConfigurationListener();

  /**
   * Keeps the API address space object synchronized with the internal address space content object.
   */
  private final IAddressSpaceContentListener m_internalContentListener =
      new InternalContentListener();

  // / @cond INTERNAL
  /**
   * Creates a new API address space object.
   *
   * @param database Database where the address space is stored.
   * @param project The project the address space belongs to.
   * @param addressSpace The wrapped internal address space.
   */
  // / @endcond
  public AddressSpace(final Database database, final Project project,
      final INaviAddressSpace addressSpace) {
    Preconditions.checkNotNull(database, "Error: Database argument can't be null");
    Preconditions.checkNotNull(addressSpace, "Error: Address space argument can't be null");

    m_database = database;
    m_project = project;
    m_addressSpace = addressSpace;
    m_debugger = m_addressSpace.getConfiguration().getDebugger() == null ? null
        : new Debugger(m_addressSpace.getConfiguration().getDebugger());

    if (addressSpace.isLoaded()) {
      convertData();
    }

    addressSpace.addListener(m_listener);
    addressSpace.getConfiguration().addListener(m_configurationListener);
  }

  /**
   * Looks up a native module and adds it to the list of modules in the address space.
   *
   * @param module The native module to look up.
   *
   * @return The found API module. This value can be null.
   */
  private Module addModule(final INaviModule module) {
    Module apiModule = null;
    apiModule = ObjectFinders.getObject(module, m_database.getModules());

    if (apiModule == null) {
      NaviLogger.severe("Error: Could not determine API module for native module '%s'",
          module.getConfiguration().getName());
    } else {
      m_modules.add(apiModule);
    }

    return apiModule;
  }

  /**
   * Converts internal module objects to API module objects.
   */
  private void convertData() {
    m_addressSpace.getContent().addListener(m_internalContentListener);

    m_modules = new ArrayList<Module>();

    for (final INaviModule module : m_addressSpace.getContent().getModules()) {
      addModule(module);
    }
  }

  @Override
  public INaviAddressSpace getNative() {
    return m_addressSpace;
  }

  // ! Adds an address space listener.
  /**
   * Adds an object that is notified about changes in the address space.
   *
   * @param listener The listener object that is notified about changes in the address space.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the address
   *         space.
   */
  public void addListener(final IAddressSpaceListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Adds a module to the address space.
  /**
   * Adds a module to the address space.
   *
   * Please note that each module can only be added once to an address space.
   *
   * @param module The module to add to the address space.
   *
   * @throws IllegalArgumentException Thrown if the module argument is null or the module is not in
   *         the same database as the address space.
   * @throws IllegalStateException Thrown if the address space is not loaded or the module is
   *         already part of the address space.
   * @throws CouldntSaveDataException Thrown if the module could not be added to the address space.
   */
  public void addModule(final Module module) throws CouldntSaveDataException {
    try {
      m_addressSpace.getContent().addModule(module.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Closes the address space.
  /**
   * Closes the address space.
   *
   *  It is possible that the address space stays open because other plugins can veto the close
   * operation if they require the address space to remain open.
   *
   * @return True, if the address space was closed. False, otherwise.
   */
  public boolean close() {
    return m_addressSpace.close();
  }

  // ! Creation date of the address space.
  /**
   * Returns the creation date of the address space. This is the date when the address space was
   * first written to the database.
   *
   * @return The creation date of the module.
   */
  public Date getCreationDate() {
    return m_addressSpace.getConfiguration().getCreationDate();
  }

  // ! Debugger of the address space.
  /**
   * Returns the debugger that is used to debug the address space.
   *
   * @return The debugger that is used to debug the address space.
   */
  public Debugger getDebugger() {
    return m_debugger;
  }

  // ! Debugger template of the address space.
  /**
   * Returns the debugger template that was used to create the current address space debugger.
   *
   * @return The debugger template of the address space debugger.
   */
  public DebuggerTemplate getDebuggerTemplate() {
    final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate internalTemplate =
        m_addressSpace.getConfiguration().getDebuggerTemplate();

    return internalTemplate == null ? null
        : ObjectFinders.getObject(internalTemplate,
            m_database.getDebuggerTemplateManager().getDebuggerTemplates());
  }

  // ! Description of the address space.
  /**
   * Returns the description of the address space.
   *
   * @return The description of the address space.
   */
  public String getDescription() {
    return m_addressSpace.getConfiguration().getDescription();
  }

  // ! Returns the image base of a module inside an address space.
  /**
   * Returns the image base of a module inside an address space.
   *
   * @param module The module whose image base is returned.
   *
   * @return The image base of the module inside the address space.
   */
  public Address getImagebase(final Module module) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    return new Address(m_addressSpace.getContent().getImageBase(module.getNative()).toBigInteger());
  }

  // ! Modification date of the address space.
  /**
   * Returns the modification date of the address space. This is the date when the address space was
   * last written to the database.
   *
   * @return The modification date of the address space.
   */
  public Date getModificationDate() {
    return m_addressSpace.getConfiguration().getModificationDate();
  }

  // ! List of modules in the address space.
  /**
   * Returns the list of modules that are part of the address space.
   *
   * @return A list of modules.
   */
  public List<Module> getModules() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: Address space is not loaded");
    }

    return new ArrayList<Module>(m_modules);
  }

  // ! Name of the address space.
  /**
   * Returns the name of the address space.
   *
   * @return The name of the address space.
   */
  public String getName() {
    return m_addressSpace.getConfiguration().getName();
  }

  // ! The project the address space belongs to.
  /**
   * Returns the project the address space belongs to.
   *
   * @return The project the address space belongs to.
   */
  public Project getProject() {
    return m_project;
  }

  // ! Checks if the address space is loaded.
  /**
   * Returns a flag that indicates whether the address space is loaded.
   *
   * @return True, if the address space is loaded. False, otherwise.
   */
  public boolean isLoaded() {
    return m_addressSpace.isLoaded();
  }

  // ! Loads the address space.
  /**
   * Loads the address space. If the address space is already loaded nothing happens.
   *
   * @throws CouldntLoadDataException Thrown if the address space could not be loaded.
   */
  public void load() throws CouldntLoadDataException {
    if (isLoaded()) {
      return;
    }

    try {
      m_addressSpace.load();
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException | LoadCancelledException e) {
      throw new CouldntLoadDataException(e);
    } 
  }

  // ! Removes an address space listener.
  /**
   * Removes a listener object from the address space.
   *
   * @param listener The listener object to remove from the address space.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the address
   *         space.
   */
  public void removeListener(final IAddressSpaceListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Removes a module from the address space.
   *
   * @param module The module to be removed from the address space.
   *
   * @throws CouldntDeleteException Thrown if the module could not be deleted.
   * @throws CouldntSaveDataException Thrown if the modification time could not be saved.
   */
  public void removeModule(final Module module) throws CouldntDeleteException,
      CouldntSaveDataException {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    try {
      m_addressSpace.getContent().removeModule(module.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException exception) {
      throw new CouldntDeleteException(exception);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  // ! Changes the debugger template of the address space.
  /**
   * Changes the debugger template of the address space.
   *
   * @param template The new debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the new debugger template could not be saved to the
   *         database.
   */
  public void setDebuggerTemplate(final DebuggerTemplate template) throws CouldntSaveDataException {
    try {
      m_addressSpace.getConfiguration().setDebuggerTemplate(template == null ? null : template.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the description of the address space.
  /**
   * Changes the description of the address space.
   *
   * @param description The new description of the address space.
   *
   * @throws IllegalArgumentException Thrown if the description argument is null.
   * @throws CouldntSaveDataException Thrown if the description of the address space could not be
   *         changed.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "Error: Description argument can not be null");

    try {
      m_addressSpace.getConfiguration().setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the image base of a module in an address space.
  /**
   * Changes the image base of a module in the context of the address space.
   *
   * @param module The module whose image base is changed.
   * @param address The new image base of the module.
   *
   * @throws CouldntSaveDataException Thrown if the new image base could not be set.
   */
  public void setImageBase(final Module module, final Address address)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    try {
      m_addressSpace.getContent().setImageBase(module.getNative(), new CAddress(address.toLong()));
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the name of the address space.
  /**
   * Changes the name of the address space.
   *
   * @param name The new name of the address space.
   *
   * @throws IllegalArgumentException Thrown if the name argument is null.
   * @throws CouldntSaveDataException Thrown if the name of the address space could not be changed.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      m_addressSpace.getConfiguration().setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Printable representation of the address space.
  /**
   * Converts the address space to a printable string.
   *
   * @return The printable representation of the address space.
   */
  @Override
  public String toString() {
    final StringBuilder moduleString = new StringBuilder();

    if (isLoaded()) {
      boolean addComma = false;

      for (final Module module : getModules()) {
        if (addComma) {
          moduleString.append(", ");
        }

        addComma = true;

        moduleString.append(module.getName());
      }
    } else {
      moduleString.append(String.format("unloaded, %d modules", m_addressSpace.getModuleCount()));
    }

    return String.format("Address space %s [%s]", getName(), moduleString);
  }

  /**
   * Keeps the API address space object synchronized with the internal address space configuration
   * object.
   */
  private class InternalConfigurationListener implements IAddressSpaceConfigurationListener {
    @Override
    public void changedDebugger(final INaviAddressSpace addressSpace,
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      m_debugger = addressSpace.getConfiguration().getDebugger() == null ? null
          : new Debugger(addressSpace.getConfiguration().getDebugger());

      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.changedDebugger(AddressSpace.this, m_debugger);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDescription(final INaviAddressSpace addressSpace, final String description) {
      for (final IAddressSpaceListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedDescription(AddressSpace.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedModificationDate(final CAddressSpace addressSpace,
        final Date modificationDate) {
      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.changedModificationDate(AddressSpace.this, modificationDate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final INaviAddressSpace addressSpace, final String name) {
      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.changedName(AddressSpace.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  /**
   * Keeps the API address space object synchronized with the internal address space content object.
   */
  private class InternalContentListener implements IAddressSpaceContentListener {
    @Override
    public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      final Module newModule = addModule(module);

      for (final IAddressSpaceListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedModule(AddressSpace.this, newModule);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
        final IAddress address) {
      final Address newAddress = new Address(address.toBigInteger());

      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.changedImageBase(AddressSpace.this, ObjectFinders.getObject(module, m_modules),
              newAddress);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      final Module deletedModule = ObjectFinders.getObject(module, m_modules);

      m_modules.remove(deletedModule);

      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.removedModule(AddressSpace.this, deletedModule);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  /**
   * Keeps the API address space object synchronized with the internal address space object.
   */
  private class InternalListener implements
      com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceListener {
    @Override
    public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
      content.removeListener(m_internalContentListener);

      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.closedAddressSpace(AddressSpace.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean closing(final INaviAddressSpace addressSpace) {
      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          if (!listener.closingAddressSpace(AddressSpace.this)) {
            return false;
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      return true;
    }

    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      convertData();

      for (final IAddressSpaceListener listener : m_listeners) {
        try {
          listener.loadedAddressSpace(AddressSpace.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean loading(final AddressSpaceLoadEvents event, final int counter) {
      return true;
    }
  }
}
