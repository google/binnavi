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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfiguration;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceListener;

/**
 * Interface for address spaces.
 */
public interface INaviAddressSpace extends IDatabaseObject {
  /**
   * Adds a listener object that is notified about changes in the address space.
   * 
   * @param listener The listener object.
   */
  void addListener(IAddressSpaceListener listener);

  /**
   * Closes the address space.
   * 
   * @return True, if the address space was closed. False, if the operation was vetoed.
   */
  boolean close();

  CAddressSpaceConfiguration getConfiguration();

  CAddressSpaceContent getContent();

  /**
   * Returns the number of modules in the address space.
   * 
   * @return The number of modules in the address space.
   */
  int getModuleCount();

  INaviProject getProject();

  /**
   * Returns whether the address space is loaded or not.
   * 
   * @return True, if the address space is loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Returns whether the address space is currently being loaded from the database.
   * 
   * @return True, if is currently being loaded. False, otherwise.
   */
  boolean isLoading();

  /**
   * Loads the address space and all modules inside the address space.
   * 
   * @throws CouldntLoadDataException
   * @throws LoadCancelledException
   */
  void load() throws CouldntLoadDataException, LoadCancelledException;

  /**
   * Removes a listener object from the address space.
   * 
   * @param listener The listener object.
   */
  void removeListener(IAddressSpaceListener listener);
}
