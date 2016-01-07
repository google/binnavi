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

import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;



/**
 * Interface to be implemented by classes that want to be notified about changes in address space
 * content.
 */
public interface IAddressSpaceContentListener {
  /**
   * Invoked after a module was added to an address space.
   * 
   * @param addressSpace The address space to which the module was added.
   * @param module The module added to the address space.
   */
  void addedModule(INaviAddressSpace addressSpace, INaviModule module);

  /**
   * Invoked after the image base of a module inside an address space changed.
   * 
   * @param addressSpace The address space the module belongs to.
   * @param module The module whose image base in the address space changed.
   * @param address The new image base of the module inside the address space.
   */
  void changedImageBase(INaviAddressSpace addressSpace, INaviModule module, IAddress address);

  /**
   * Invoked after a module was removed from an address space.
   * 
   * @param addressSpace The address space from which the module was removed.
   * @param module The module removed from the address space.
   */
  void removedModule(INaviAddressSpace addressSpace, INaviModule module);
}
