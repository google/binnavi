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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.AddressSpaceContentBackend;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.ProjectTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.TcpDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Contains the loaded content of an address space.
 */
public final class CAddressSpaceContent {
  /**
   * Address space the content belongs to.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Provides the database backend.
   */
  private final AddressSpaceContentBackend m_provider;

  /**
   * Listeners that are notified about changes in the address space.
   */
  private final ListenerProvider<IAddressSpaceContentListener> m_listeners =
      new ListenerProvider<IAddressSpaceContentListener>();

  /**
   * The modules that can be found in the address space.
   */
  private final List<INaviModule> m_modules = new ArrayList<INaviModule>();

  /**
   * The image bases of the individual modules in the address space.
   */
  private final Map<INaviModule, IAddress> m_imageBases = new HashMap<INaviModule, IAddress>();

  /**
   * Creates a new content object.
   * 
   * @param addressSpace Address space the content belongs to.
   * @param provider Provides the database backend.
   * @param modules The modules that can be found in the address space.
   */
  public CAddressSpaceContent(final INaviAddressSpace addressSpace,
      final AddressSpaceContentBackend provider, final List<Pair<IAddress, INaviModule>> modules) {
    m_addressSpace =
        Preconditions.checkNotNull(addressSpace, "IE01791: Address space argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE01792: Provider argument can not be null");
    Preconditions.checkNotNull(modules, "IE01793: Modules argument can not be null");

    for (final Pair<IAddress, INaviModule> pair : modules) {
      final IAddress address = pair.first();
      final INaviModule module = pair.second();

      m_modules.add(module);
      m_imageBases.put(module, address);

      final IDebugger debugger = addressSpace.getConfiguration().getDebugger();

      if (debugger != null) {
        debugger.setAddressTranslator(module, module.getConfiguration().getFileBase(),
            getImageBase(module));
      }
    }

    final IDebugger debugger = addressSpace.getConfiguration().getDebugger();

    if ((debugger == null) || !debugger.isConnected()) {
      final DebuggerTemplate template = addressSpace.getConfiguration().getDebuggerTemplate();

      if (template == null) {
        addressSpace.getConfiguration().setDebugger(null);
      } else {
        final TcpDebugger newDebuggerdebugger =
            new TcpDebugger(template, new ProjectTargetSettings(m_addressSpace.getProject()));

        for (final INaviModule module : m_modules) {
          newDebuggerdebugger.setAddressTranslator(module, module.getConfiguration().getFileBase(),
              getImageBase(module));
        }

        addressSpace.getConfiguration().setDebugger(newDebuggerdebugger);
      }
    }
  }

  /**
   * Adds a listener that is notified about changes in the content.
   * 
   * @param listener The listener to add.
   */
  public void addListener(final IAddressSpaceContentListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Adds a module to the address space.
   * 
   * Note that the address space must be loaded before you can add modules to it. Furthermore note
   * that each module can only be added to an address space once.
   * 
   * @param module The module to add to the address space.
   * 
   * @throws CouldntSaveDataException
   */
  public void addModule(final INaviModule module) throws CouldntSaveDataException {
    Preconditions.checkNotNull(module, "IE00030: Module argument can not be null");
    Preconditions.checkArgument(!m_modules.contains(module),
        "IE00032: Module can not be added more than once");
    Preconditions.checkArgument(module.inSameDatabase(m_addressSpace),
        "IE00033: Module and address space are not in the same database");

    m_provider.addModule(m_addressSpace, module);

    m_modules.add(module);

    for (final IAddressSpaceContentListener listener : m_listeners) {
      try {
        listener.addedModule(m_addressSpace, module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    setImageBase(module, module.getConfiguration().getImageBase());
  }

  /**
   * Determines the image base of a module in the address space.
   * 
   * @param module The module in question.
   * 
   * @return The image base of the module in the address space.
   */
  public IAddress getImageBase(final INaviModule module) {
    Preconditions.checkNotNull(module, "IE00034: Module argument can not be null");
    Preconditions.checkArgument(m_modules.contains(module),
        "IE00037: Module is not part of the address space");

    // ATTENTION: Returning the file base here is not an error, do not replace with ImageBase.
    return m_imageBases.containsKey(module) ? m_imageBases.get(module) : module.getConfiguration()
        .getFileBase();
  }

  /**
   * Returns the number of modules in the address space.
   * 
   * @return The number of modules in the address space.
   */
  public int getModuleCount() {
    return m_modules.size();
  }

  /**
   * Returns the modules that are part of the address space.
   * 
   * @return The modules that are part of the address space.
   */
  public List<INaviModule> getModules() {
    return new ArrayList<INaviModule>(m_modules);
  }

  /**
   * Removes a previously added listener.
   * 
   * @param listener The previously added listener.
   */
  public void removeListener(final IAddressSpaceContentListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Removes a module from the address space.
   * 
   * @param module The module to remove.
   * 
   * @throws CouldntDeleteException Thrown if the module could not be removed from the address
   *         space.
   * @throws CouldntSaveDataException Thrown if the modification time could not be saved.
   */
  public void removeModule(final INaviModule module) throws CouldntDeleteException,
      CouldntSaveDataException {
    Preconditions.checkNotNull(module, "IE00041: Module argument can not be null");
    Preconditions.checkArgument(m_modules.contains(module),
        "IE00043: Module does not belong to this address space");

    m_provider.removeModule(m_addressSpace, module);

    m_modules.remove(module);

    for (final IAddressSpaceContentListener listener : m_listeners) {
      try {
        listener.removedModule(m_addressSpace, module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_addressSpace.getConfiguration().updateModificationDate();
  }

  /**
   * Sets the image base of a module inside the address space.
   * 
   * @param module The module in question.
   * @param imageBase The new image base of the module.
   * 
   * @throws CouldntSaveDataException Thrown if the new image base could not be stored to the
   *         database.
   */
  public void setImageBase(final INaviModule module, final IAddress imageBase)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(module, "IE00045: Module argument can not be null");
    Preconditions.checkNotNull(imageBase, "IE00046: Address argument can not be null");
    Preconditions.checkArgument(m_modules.contains(module),
        "IE00048: Module is not part of the address space");

    if (imageBase.equals(m_imageBases.get(module))) {
      return;
    }

    m_provider.setImageBase(m_addressSpace, module, imageBase);

    m_imageBases.put(module, imageBase);

    final IDebugger debugger = m_addressSpace.getConfiguration().getDebugger();

    if (debugger != null) {
      debugger.setAddressTranslator(module, module.getConfiguration().getFileBase(),
          getImageBase(module));
    }

    for (final IAddressSpaceContentListener listener : m_listeners) {
      try {
        listener.changedImageBase(m_addressSpace, module, imageBase);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_addressSpace.getConfiguration().updateModificationDate();
  }
}
