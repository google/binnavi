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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers;

import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;

/**
 * Wraps an address space object for filtering.
 */
public final class CAddressSpaceWrapper implements IFilterWrapper<INaviAddressSpace>, INamedElement {
  /**
   * The wrapped address space.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Creates a new wrapper object.
   * 
   * @param addressSpace The address space to wrap.
   */
  public CAddressSpaceWrapper(final INaviAddressSpace addressSpace) {
    m_addressSpace = addressSpace;
  }

  @Override
  public String getDescription() {
    return m_addressSpace.getConfiguration().getDescription();
  }

  @Override
  public String getName() {
    return m_addressSpace.getConfiguration().getName();
  }

  @Override
  public INaviAddressSpace unwrap() {
    return m_addressSpace;
  }
}
