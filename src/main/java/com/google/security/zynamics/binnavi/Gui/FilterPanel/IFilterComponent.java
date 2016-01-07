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

import java.awt.Component;

/**
 * Interface to be implemented by components that should serve as filter components next to filter
 * text fields above filtered tables.
 * 
 * @param <T> Type of the elements to filter.
 */
public interface IFilterComponent<T> {
  /**
   * Adds a listener object to the filter component.
   * 
   * @param listener The listener to add.
   */
  void addListener(IFilterComponentListener listener);

  /**
   * Returns a filter created from the current settings of the filter component.
   * 
   * @return The created filter.
   */
  IFilter<T> createFilter();

  /**
   * Returns the component to add next to the text field.
   * 
   * @return The component to add next to the text field.
   */
  Component getComponent();

  /**
   * Removes a previously added component filter listener object.
   * 
   * @param listener The listener object to remove.
   */
  void removeListener(IFilterComponentListener listener);
}
