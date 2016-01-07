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

import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Wraps a view object for filtering.
 */
public final class CViewWrapper implements IFilterWrapper<INaviView>, INamedElement {
  /**
   * The wrapped view object.
   */
  private final INaviView m_view;

  /**
   * Creates a new wrapper object.
   * 
   * @param module The wrapped view object.
   */
  public CViewWrapper(final INaviView module) {
    m_view = module;
  }

  @Override
  public String getDescription() {
    return m_view.getConfiguration().getDescription();
  }

  @Override
  public String getName() {
    return m_view.getName();
  }

  @Override
  public INaviView unwrap() {
    return m_view;
  }
}
