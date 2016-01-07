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

import java.util.Date;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;


public interface AddressSpaceConfigurationBackend {
  /**
   * Assigns a debugger to an address space. From now on that debugger template is used to debug the
   * address space.
   * 
   * If the debugger argument is null, the existing debugger is removed from the address space.
   * 
   * @param addressSpace The address space in question.
   * @param debugger The debugger that is assigned to the address space. This argument can be null.
   * 
   * @throws CouldntSaveDataException Thrown if the debugger could not be assigned to the address
   *         space.
   */
  void assignDebugger(CAddressSpace addressSpace, DebuggerTemplate debugger)
      throws CouldntSaveDataException;

  /**
   * Returns the last modification date of a given address space.
   * 
   * @param addressSpace The address space in question.
   * 
   * @return The modification date of the address space.
   * 
   * @throws CouldntLoadDataException Thrown if the address space could not be loaded.
   */
  Date getModificationDate(CAddressSpace addressSpace) throws CouldntLoadDataException;

  /**
   * Changes the description of the address space.
   * 
   * @param addressSpace The address space whose description is changed.
   * @param description The new description of the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the new description of the address space could not
   *         be stored in the database.
   */
  void setDescription(CAddressSpace addressSpace, String description)
      throws CouldntSaveDataException;

  /**
   * Changes the name of an address space.
   * 
   * @param addressSpace The address space whose name is changed.
   * @param name The new name of the address space.
   * 
   * @throws CouldntSaveDataException Thrown if the name of the address space could not be changed.
   */
  void setName(CAddressSpace addressSpace, String name) throws CouldntSaveDataException;
}
