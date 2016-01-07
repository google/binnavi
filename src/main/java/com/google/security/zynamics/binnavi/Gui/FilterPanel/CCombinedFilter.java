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

import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.List;


/**
 * Filter class that combines a list of filters into a single filter whose sub-filters must all be
 * true for an item to be selected.
 *
 * @param <T> The type of the elements to filter.
 */
public final class CCombinedFilter<T> implements IFilter<T> {
  /**
   * The sub-filters.
   */
  private final List<IFilter<T>> m_filters = new FilledList<IFilter<T>>();

  /**
   * Creates a new filter object.
   *
   * @param filters The sub-filters.
   */
  @SafeVarargs
  public CCombinedFilter(final IFilter<T>... filters) {
    for (final IFilter<T> filter : filters) {
      m_filters.add(filter);
    }
  }

  /**
   * Checks whether a given element passes this filter.
   *
   * @param element The element to check.
   *
   * @return True, if the element passes the filter. False, if it does not.
   */
  @Override
  public boolean checkCondition(final T element) {
    for (final IFilter<T> filter : m_filters) {
      if (!filter.checkCondition(element)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public IFilledList<T> get(final List<T> elements) {
    return new FilledList<T>(CollectionHelpers.filter(elements, new ICollectionFilter<T>() {
      @Override
      public boolean qualifies(final T element) {
        return checkCondition(element);
      }
    }));
  }
}
