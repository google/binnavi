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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters;


import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.DefaultFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.CEventGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.CNameDescriptionGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CTraceListWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IFilterWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IWrapperCreator;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;

/**
 * Creates filters that can be used to filter traces tables.
 */
public final class CTracesFilterCreator extends
    CDefaultFilterCreator<TraceList, CTraceListWrapper> {
  /**
   * Creates a new filter creator object.
   */
  @SuppressWarnings("unchecked")
  public CTracesFilterCreator() {
    super(Lists.newArrayList(new CEventGenerator(),
        new CNameDescriptionGenerator<CTraceListWrapper>()));
  }

  @Override
  protected IFilter<TraceList> createFilter(final IFilterExpression<CTraceListWrapper> convert) {
    return new DefaultFilter<TraceList, CTraceListWrapper>(convert,
        new IWrapperCreator<TraceList>() {
          @Override
          public IFilterWrapper<TraceList> wrap(final TraceList element) {
            return new CTraceListWrapper(element);
          }
        });
  }
}
