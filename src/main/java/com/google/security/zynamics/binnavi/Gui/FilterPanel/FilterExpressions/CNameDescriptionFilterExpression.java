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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CViewWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.INamedElement;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Filters named elements according to their name and description.
 * 
 * @param <T> Type of the named element.
 */
public final class CNameDescriptionFilterExpression<T extends INamedElement> implements
    IFilterExpression<T> {
  /**
   * Input string to search for.
   */
  private final String m_text;

  /**
   * Module to filter. This element can be null.
   */
  private final IViewContainer m_module;

  /**
   * Creates a new filter expression.
   * 
   * @param text Input string to search for.
   * @param module Module to filter. This argument can be null.
   */
  public CNameDescriptionFilterExpression(final String text, final IViewContainer module) {
    m_text = Preconditions.checkNotNull(text, "IE02808: text argument can not be null");
    m_module = module;
  }

  /**
   * Filters the address of a given view.
   * 
   * @param view The view to filter.
   * 
   * @return True, if the view address passes the filter. False, otherwise.
   */
  private boolean checkAddress(final INaviView view) {
    if (m_module == null) {
      return false;
    }

    final INaviFunction function = m_module.getFunction(view);

    if (function == null) {
      return false;
    }

    return function.getAddress().toString().toLowerCase().contains(m_text.toLowerCase());
  }

  @Override
  public boolean evaluate(final T element) {
    if (element.getDescription() != null) {
      return element.getName().toLowerCase().contains(m_text.toLowerCase())
          || element.getDescription().toLowerCase().contains(m_text.toLowerCase())
          || ((element instanceof CViewWrapper) && checkAddress(((CViewWrapper) element).unwrap()));
    } else {
      return element.getName().toLowerCase().contains(m_text.toLowerCase())
          || ((element instanceof CViewWrapper) && checkAddress(((CViewWrapper) element).unwrap()));
    }
  }
}
