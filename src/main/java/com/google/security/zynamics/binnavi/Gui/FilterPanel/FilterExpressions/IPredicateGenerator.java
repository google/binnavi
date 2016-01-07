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

/**
 * Interface for classes that want to create filter expressions from filter strings.
 * 
 * @param <T> Type of the element to be filtered.
 */
public interface IPredicateGenerator<T> {
  /**
   * Checks whether the generator can parse a given filter string.
   * 
   * @param text The filter string to parse.
   * 
   * @return True, if the generator can parse the filter string. False, otherwise.
   */
  boolean canParse(String text);

  /**
   * Creates a filter expression from a filter string.
   * 
   * @param text The filter string to parse.
   * 
   * @return The generated filter expression.
   */
  IFilterExpression<T> createExpression(String text);
}
