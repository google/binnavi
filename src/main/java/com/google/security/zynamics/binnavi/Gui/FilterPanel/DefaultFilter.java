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
package com.google.security.zynamics.binnavi.Gui.FilterPanel;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IFilterWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IWrapperCreator;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.List;



/**
 * Default filter used for filtering tables.
 * 
 * @param <T> Elements to filter.
 * @param <Wrapper> Wraps the elements to filter.
 */
public final class DefaultFilter<T, Wrapper extends IFilterWrapper<T>> implements IFilter<T> {
  /**
   * Expression used to filter the elements.
   */
  private final IFilterExpression<Wrapper> m_expression;

  /**
   * Wraps the filtered elements.
   */
  private final IWrapperCreator<T> m_wrapper;

  /**
   * Creates a new filter object.
   * 
   * @param expression Expression used to filter the elements.
   * @param wrapper Wraps the filtered elements.
   */
  public DefaultFilter(final IFilterExpression<Wrapper> expression, final IWrapperCreator<T> wrapper) {
    m_expression = expression;
    m_wrapper = wrapper;
  }

  /**
   * Checks whether a given element passes this filter.
   * 
   * @param element The element to check.
   * 
   * @return True, if the element passes the filter. False, if it does not.
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean checkCondition(final T element) {
    return m_expression.evaluate((Wrapper) m_wrapper.wrap(element));
  }

  @Override
  public IFilledList<T> get(final List<T> modules) {
    return new FilledList<T>(CollectionHelpers.filter(modules, new ICollectionFilter<T>() {
      @Override
      public boolean qualifies(final T module) {
        return checkCondition(module);
      }
    }));
  }
}
