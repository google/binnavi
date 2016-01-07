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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree;

import java.util.List;

/**
 * Represents a single AND node in concrete filter ASTs.
 * 
 * @param <T> Type of the elements to filter.
 */
public final class CAndExpression<T> implements IFilterExpression<T> {
  /**
   * Child expressions of the AND expression.
   */
  private final List<IFilterExpression<T>> m_expressions;

  /**
   * Creates a new expression object.
   * 
   * @param expressions Child expressions of the AND expression.
   */
  public CAndExpression(final List<IFilterExpression<T>> expressions) {
    m_expressions = expressions;
  }

  @Override
  public boolean evaluate(final T module) {
    for (final IFilterExpression<T> expression : m_expressions) {
      if (!expression.evaluate(module)) {
        return false;
      }
    }

    return true;
  }
}
