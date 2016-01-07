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

/**
 * Interface for classes that want to create wrapper objects for filtering.
 * 
 * @param <T> Type of the wrapped objects.
 */
public interface IWrapperCreator<T> {
  /**
   * Creates a new wrapper object that wraps a given object.
   * 
   * @param element The object to wrap.
   * 
   * @return The wrapper for the given object.
   */
  IFilterWrapper<T> wrap(T element);
}
