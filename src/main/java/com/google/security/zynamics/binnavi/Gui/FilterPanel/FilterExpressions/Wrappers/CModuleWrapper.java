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

import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Wraps a module object for filtering.
 */
public final class CModuleWrapper implements IFilterWrapper<INaviModule>, INamedElement {
  /**
   * The wrapped module object.
   */
  private final INaviModule m_module;

  /**
   * Creates a new wrapper object.
   * 
   * @param module The wrapped module object.
   */
  public CModuleWrapper(final INaviModule module) {
    m_module = module;
  }

  @Override
  public String getDescription() {
    return m_module.getConfiguration().getDescription();
  }

  @Override
  public String getName() {
    return m_module.getConfiguration().getName();
  }

  @Override
  public INaviModule unwrap() {
    return m_module;
  }
}
