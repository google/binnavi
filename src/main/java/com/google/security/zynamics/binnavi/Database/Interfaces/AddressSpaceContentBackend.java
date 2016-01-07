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
package com.google.security.zynamics.binnavi.Database.Interfaces;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;



public interface AddressSpaceContentBackend {
  /**
   * Adds a given module to a given address space. By default the image base of the module in the
   * address space is set to 0.
   * 
   * Note that a module can only be added to an address space once. If the module already exists in
   * the address space, the operation will fail with a {@link CouldntSaveDataException}.
   * 
   * @param addressSpace The address space where the module is added.
   * @param module The module that is added to the address space.
   * 
   * @throws IllegalArgumentException Thrown if either of the two arguments is null.
   * @throws CouldntSaveDataException Thrown if the module could not be added to the address space.
   */
  void addModule(INaviAddressSpace addressSpace, INaviModule module)
      throws CouldntSaveDataException;

  /**
   * Removes a module from an address space.
   * 
   * @param addressSpace The address space from which the module is removed.
   * @param module The module to be removed from the address space.
   * 
   * @throws CouldntDeleteException Thrown if the module could not be removed from the address
   *         space.
   * @throws CouldntSaveDataException Thrown if the modification time could not be saved.
   */
  void removeModule(INaviAddressSpace addressSpace, INaviModule module)
      throws CouldntDeleteException, CouldntSaveDataException;

  /**
   * Changes the image base of a module inside an address space.
   * 
   * @param addressSpace The address space the module belongs to.
   * @param module The module whose image base is changed.
   * @param address The new image base of the module in the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the new image base value could not be stored in the
   *         database.
   */
  void setImageBase(INaviAddressSpace addressSpace, INaviModule module, IAddress address)
      throws CouldntSaveDataException;
}
