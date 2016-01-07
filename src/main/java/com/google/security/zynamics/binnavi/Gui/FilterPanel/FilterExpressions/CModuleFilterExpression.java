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

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.CNumericFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CAddressSpaceWrapper;

/**
 * Filters address spaces according to the number of their modules.
 */
public final class CModuleFilterExpression extends CNumericFilterExpression<CAddressSpaceWrapper> {
  /**
   * Creates a new filter expression object.
   * 
   * @param relation Relation used to filter.
   * @param value Value to compare the module count of the address spaces to.
   */
  public CModuleFilterExpression(final FilterRelation relation, final long value) {
    super(relation, value);
  }

  @Override
  public boolean evaluate(final CAddressSpaceWrapper module) {
    return evaluate(module.unwrap().getModuleCount());
  }
}
