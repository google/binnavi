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

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.INamedElement;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Takes a filter string and uses it to filter named elements.
 * 
 * @param <T> Type of the named elements to filter.
 */
public final class CNameDescriptionGenerator<T extends INamedElement> implements
    IPredicateGenerator<T> {
  /**
   * The module to filter.
   */
  private final IViewContainer m_module;

  /**
   * Creates a new generator object.
   */
  public CNameDescriptionGenerator() {
    this(null);
  }

  /**
   * Creates a new generator object.
   * 
   * @param module The module to filter.
   */
  public CNameDescriptionGenerator(final IViewContainer module) {
    m_module = module;
  }

  @Override
  public boolean canParse(final String text) {
    return true;
  }

  @Override
  public IFilterExpression<T> createExpression(final String text) {
    return new CNameDescriptionFilterExpression<T>(text, m_module);
  }
}
