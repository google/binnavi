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
package com.google.security.zynamics.binnavi.Gui.Debug.Notifier;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

/**
 * Contains code for showing information about wrongly relocated modules to the user.
 */
public final class CRelocationNotifier {
  /**
   * You are not supposed to instantiate this class.
   */
  private CRelocationNotifier() {}

  /**
   * Finds wrongly relocated modules by comparing a snapshot of the modules in an address space
   * being reported by the debug client with those configured in
   * com.google.security.zynamics.binnavi.
   *
   * @param debugger The active debugger.
   * @param viewContainer The view container that is being debugged.
   * @param memoryModules The modules whose base addresses are checked.
   *
   * @return A list of wrongly relocated modules.
   */
  private static List<Pair<INaviModule, MemoryModule>> collectWronglyPlacedModules(
      final IDebugger debugger, final IViewContainer viewContainer,
      final List<MemoryModule> memoryModules) {
    final List<Pair<INaviModule, MemoryModule>> wronglyPlacedModules =
        new ArrayList<Pair<INaviModule, MemoryModule>>();

    final List<INaviModule> modules = viewContainer.getModules();

    for (final INaviModule module : modules) {
      for (final MemoryModule memoryModule : memoryModules) {
        if (module.getConfiguration().getName().equalsIgnoreCase(memoryModule.getName())) {
          final RelocatedAddress assumedAddress = debugger.fileToMemory(module,
              new UnrelocatedAddress(module.getConfiguration().getFileBase()));
          final IAddress memoryAddress = memoryModule.getBaseAddress().getAddress();

          if (!assumedAddress.getAddress().equals(memoryAddress)) {
            wronglyPlacedModules.add(new Pair<INaviModule, MemoryModule>(module, memoryModule));
          }
        }
      }
    }

    return wronglyPlacedModules;
  }

  /**
   * Tests the correctness of the base addresses of memory modules and displays corrected base
   * addresses to the user.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The active debugger.
   * @param viewContainer The view container that is being debugged.
   * @param memoryModules The modules whose base addresses are checked.
   */
  public static void checkBaseAddresses(final JFrame parent, final IDebugger debugger,
      final IViewContainer viewContainer, final List<MemoryModule> memoryModules) {
    final List<Pair<INaviModule, MemoryModule>> wronglyPlacedModules =
        collectWronglyPlacedModules(debugger, viewContainer, memoryModules);

    if (!wronglyPlacedModules.isEmpty()) {
      for (final Pair<INaviModule, MemoryModule> pair : wronglyPlacedModules) {
        final INaviModule module = pair.first();
        final MemoryModule memoryModule = pair.second();

        final List<INaviAddressSpace> addressSpaces = viewContainer.getAddressSpaces();

        if (addressSpaces == null) {
          try {
            module.getConfiguration().setImageBase(memoryModule.getBaseAddress().getAddress());
          } catch (final CouldntSaveDataException e) {
            CUtilityFunctions.logException(e);
          }
        } else {
          for (final INaviAddressSpace addressSpace : addressSpaces) {
            if (addressSpace.getContent().getModules().contains(module)) {
              try {
                addressSpace.getContent().setImageBase(module,
                    memoryModule.getBaseAddress().getAddress());
              } catch (final CouldntSaveDataException e) {
                CUtilityFunctions.logException(e);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Search list of module for navi module corresponding to the given memory module and relocate the
   * navi module if needed.
   *
   * @param debugger The currently active debugger.
   * @param memoryModule The memory module to be checked against the navi modules.
   */
  public static void relocateModule(final IDebugger debugger, final MemoryModule memoryModule) {
    for (final INaviModule module : debugger.getModules()) {
      if (module.getConfiguration().getName().equalsIgnoreCase(memoryModule.getName())) {
        final RelocatedAddress assumedAddress = debugger.fileToMemory(module,
            new UnrelocatedAddress(module.getConfiguration().getFileBase()));

        if (!assumedAddress.getAddress().equals(memoryModule.getBaseAddress().getAddress())) {
          try {
            module.getConfiguration().setImageBase(memoryModule.getBaseAddress().getAddress());
          } catch (final CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }
}
