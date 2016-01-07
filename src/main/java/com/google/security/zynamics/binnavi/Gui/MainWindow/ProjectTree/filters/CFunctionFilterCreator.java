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
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterComponent;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.CBlockGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.CEdgeGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.CInstructionGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.CNameDescriptionGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CViewWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IFilterWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IWrapperCreator;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions.CFunctionFilterComponent;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Creates filters that can be used to filter view tables.
 */
public final class CFunctionFilterCreator extends CDefaultFilterCreator<INaviView, CViewWrapper> {
  /**
   * Component to be shown next to the filter text field.
   */
  private final CFunctionFilterComponent m_component;

  /**
   * Creates a new filter creator object.
   * 
   * @param module Module whose views are filtered.
   */
  @SuppressWarnings("unchecked")
  public CFunctionFilterCreator(final IViewContainer module) {
    super(Lists.newArrayList(new CInstructionGenerator(module), new CBlockGenerator(),
        new CEdgeGenerator(), new CNameDescriptionGenerator<CViewWrapper>(module)));

    m_component = new CFunctionFilterComponent(module);
  }

  @Override
  protected IFilter<INaviView> createFilter(final IFilterExpression<CViewWrapper> convert) {
    return new DefaultFilter<INaviView, CViewWrapper>(convert, new IWrapperCreator<INaviView>() {
      @Override
      public IFilterWrapper<INaviView> wrap(final INaviView element) {
        return new CViewWrapper(element);
      }
    });
  }

  @Override
  public void dispose() {
    m_component.dispose();
  }

  @Override
  public IFilterComponent<INaviView> getFilterComponent() {
    return m_component;
  }
}
