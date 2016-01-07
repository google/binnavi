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

import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.List;


/**
 * Interface to be implemented by all objects that want to filter tables.
 * 
 * @param <T> Elements to filter.
 */
public interface IFilter<T> {
  /**
   * Checks whether an element matches the filter.
   * 
   * @param element The element to check.
   * 
   * @return True, if the element matches the filter. False, otherwise.
   */
  boolean checkCondition(final T element);

  /**
   * Filters a list of elements.
   * 
   * @param elements All input elements.
   * 
   * @return The elements that pass the filter.
   */
  IFilledList<T> get(List<T> elements);
}
