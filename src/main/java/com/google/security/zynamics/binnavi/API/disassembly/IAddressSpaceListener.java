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

import java.util.Date;

import com.google.security.zynamics.binnavi.API.debug.Debugger;


// / Used to listen on address spaces.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link AddressSpace} objects.
 */
public interface IAddressSpaceListener {
  // ! Signals a new module in the address space.
  /**
   * Invoked after a module was added to the address space.
   *
   * @param addressSpace The address space where the module was added.
   * @param module The module that was added to the address space.
   */
  void addedModule(AddressSpace addressSpace, Module module);

  // ! Signals a new address space debugger.
  /**
   * Invoked after the debugger of the address space changed.
   *
   * @param addressSpace The address space whose debugger changed.
   * @param debugger The new debugger of the address space. This argument is null if no debugger is
   *        set.
   */
  void changedDebugger(AddressSpace addressSpace, Debugger debugger);

  // ! Signals a new address space description.
  /**
   * Invoked after the description of the address space changed.
   *
   * @param addressSpace The address space whose description changed.
   * @param description The new description of the address space.
   */
  void changedDescription(AddressSpace addressSpace, String description);

  // ! Signals a new image base for a module in the address space.
  /**
   * Invoked after the image base of module inside the address space changed.
   *
   * @param addressSpace The address space where the change happened.
   * @param module The module whose image base was changed.
   * @param address The new image base value of the module inside the address space.
   */
  void changedImageBase(AddressSpace addressSpace, Module module, Address address);

  // ! Signals a new modification date.
  /**
   * Invoked after the modification date of an address space changed.
   *
   * @param addressSpace The address space whose modification date changed.
   * @param modificationDate The new modification date of the address space.
   */
  void changedModificationDate(AddressSpace addressSpace, Date modificationDate);

  // ! Signals a new address space name.
  /**
   * Invoked after the name of the address space changed.
   *
   * @param addressSpace The address space whose name changed.
   * @param name The new name of the address space.
   */
  void changedName(AddressSpace addressSpace, String name);

  // ! Signals that the address space was closed.
  /**
   * Invoked right after an address space was closed.
   *
   * @param addressSpace The address space that was closed.
   */
  void closedAddressSpace(AddressSpace addressSpace);

  // ! Signals that the address space is about to be closed.
  /**
   * Invoked right before a address space is closed. The listening object has the opportunity to
   * veto the close process if it still needs to work with the address space.
   *
   * @param addressSpace The address space that is about to be closed.
   *
   * @return True, to indicate that the address space can be closed. False, to veto the close
   *         process.
   */
  boolean closingAddressSpace(AddressSpace addressSpace);

  // ! Signals that the address space was loaded.
  /**
   * Invoked after the address space was loaded.
   *
   * @param addressSpace The address space which was loaded.
   */
  void loadedAddressSpace(AddressSpace addressSpace);

  // ! Signals that a module was removed from the address space.
  /**
   * Invoked after a module was removed from the address space.
   *
   *  After this function was invoked, further usage of the removed module in the context of the
   * address space (for example by setting the image base of the module in the address space) lead
   * to undefined behavior.
   *
   *  It is however perfectly fine to keep using the module object in the general database context
   * because the module still exists in the database. It was simply removed from the address space,
   * not completely deleted.
   *
   * @param addressSpace The address space where the module was removed.
   * @param module The module that was removed from the address space.
   */
  void removedModule(AddressSpace addressSpace, Module module);
}
