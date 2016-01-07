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

import org.antlr.runtime.RecognitionException;

/**
 * Factory class for table filters.
 * 
 * @param <T> Type of the elements shown in the table.
 */
public interface IFilterFactory<T> {
  /**
   * Creates a new filter object from an input string.
   * 
   * @param text The input string.
   * 
   * @return The created filter object.
   * 
   * @throws RecognitionException Thrown if the filter object could not be created.
   */
  IFilter<T> createFilter(String text) throws RecognitionException;

  void dispose();

  /**
   * Returns the optional filter component that is shown next to the filter text field above
   * filtered tables.
   * 
   * @return The filter component or null.
   */
  IFilterComponent<T> getFilterComponent();
}
