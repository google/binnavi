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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.disassembly.types.BaseType;

/**
 * Defines a filter function that is used by the tree model to display a subset of all base types
 * provided by the type manager.
 */
public interface TypesFilter {
  /**
   * Returns whether the given base type should be added as a top level type to the tree model,
   * i.e. if a corresponding base type tree node needs to be created.
   *
   * @param baseType The base type for which to test if it belongs to the set of included base
   *        types.
   * @return True iff the given base type should be included into the set of displayed base types.
   */
  boolean includeType(BaseType baseType);

  /**
   * Returns whether changes to the given type affect any of the base types that are included by
   * means of the includeType method. This method is used to ensure that transitively dependent type
   * nodes can be determined.
   *
   * @param baseType The base type for which to test if it affected included base types.
   * @return True iff any of the included base types depends on baseType.
   */
  boolean includeUpdatedType(BaseType baseType);
}