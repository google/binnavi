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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CViewWrapper;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Filters views according to the instruction they contain.
 */
public final class CInstructionFilterExpression implements IFilterExpression<CViewWrapper> {
  /**
   * Views that match the entered instruction.
   */
  private final List<INaviView> m_views;

  /**
   * Creates a new filter expression object.
   * 
   * @param views Views that match the entered instruction.
   */
  public CInstructionFilterExpression(final List<INaviView> views) {
    m_views = views;
  }

  @Override
  public boolean evaluate(final CViewWrapper viewWrapper) {
    return m_views.contains(viewWrapper.unwrap());
  }
}
