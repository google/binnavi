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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IFilterWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.INamedElement;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;

/**
 * Wraps memory modules for filtering.
 */
public final class CMemoryModuleWrapper implements IFilterWrapper<MemoryModule>, INamedElement {
  /**
   * The wrapped module.
   */
  private final MemoryModule m_module;

  /**
   * Creates a new wrapper object.
   *
   * @param module The module to wrap.
   */
  public CMemoryModuleWrapper(final MemoryModule module) {
    m_module = module;
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getName() {
    return m_module.getName();
  }

  @Override
  public MemoryModule unwrap() {
    return m_module;
  }
}
