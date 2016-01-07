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

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;


/**
 * Interface to be implemented by classes that want to be notified about changes in address space
 * configuration.
 */
public interface IAddressSpaceConfigurationListener {
  /**
   * Invoked after the debugger of an address space changed.
   * 
   * @param addressSpace The address space whose debugger changed.
   * @param debugger The new debugger of the address space.
   */
  void changedDebugger(INaviAddressSpace addressSpace, DebuggerTemplate debugger);

  /**
   * Invoked after the description of an address space changed.
   * 
   * @param addressSpace The address space whose description changed.
   * @param description The new description of the address space.
   */
  void changedDescription(INaviAddressSpace addressSpace, String description);

  /**
   * Invoked after the modification date of an address space changed.
   * 
   * @param addressSpace The address space whose modification date changed.
   * @param modificationDate The new modification date of the address space.
   */
  void changedModificationDate(CAddressSpace addressSpace, Date modificationDate);

  /**
   * Invoked after the name of an address space changed.
   * 
   * @param addressSpace The address space whose name changed.
   * @param name The new name of the address space.
   */
  void changedName(INaviAddressSpace addressSpace, String name);

}
