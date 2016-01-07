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

/**
 * Listener interface for objects that want to be notified about changes in address spaces.
 */
public interface IAddressSpaceListener {
  /**
   * Invoked after an address space was closed.
   * 
   * @param addressSpace The address space that was closed.
   * @param oldContent The unloaded content.
   */
  void closed(INaviAddressSpace addressSpace, CAddressSpaceContent oldContent);

  /**
   * Invoked right before an address space is closed.
   * 
   * @param addressSpace The address space to be closed.
   * 
   * @return True, to allow the address space to be closed. False, to veto closing the address
   *         space.
   */
  boolean closing(INaviAddressSpace addressSpace);

  /**
   * Invoked after an address space was loaded.
   * 
   * @param addressSpace The address space that was loaded.
   */
  void loaded(INaviAddressSpace addressSpace);

  /**
   * Invoked after a new load event happened.
   * 
   * @param event The event.
   * @param counter The number of the event.
   * 
   * @return True, to continue loading. False, to cancel it.
   */
  boolean loading(AddressSpaceLoadEvents event, int counter);
}
