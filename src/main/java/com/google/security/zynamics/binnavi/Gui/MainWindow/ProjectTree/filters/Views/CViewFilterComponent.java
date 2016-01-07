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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Views;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterComponent;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterComponentListener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions.IFilterDialogListener;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;



/**
 * Component to be shown next to the filter field on views tables.
 */
public final class CViewFilterComponent extends JPanel implements IFilterComponent<INaviView> {
  /**
   * Dialog for filtering views.
   */
  private final CViewFilterDialog m_dialog;

  /**
   * Listens on the view filter dialog and forwards changes to the filter creator.
   */
  private final IFilterDialogListener m_filterDialogListener = new InternalFilterDialogListener();

  /**
   * Listeners that are notified about changes in the filter component.
   */
  private final ListenerProvider<IFilterComponentListener> m_listeners =
      new ListenerProvider<IFilterComponentListener>();

  /**
   * Creates a new component object.
   *
   * @param module Provides the tags to filter.
   */
  public CViewFilterComponent(final IViewContainer module) {
    super(new BorderLayout());

    if (GraphicsEnvironment.isHeadless()) {
      m_dialog = null;
    } else {
      m_dialog =
          new CViewFilterDialog(module.getDatabase().getContent().getViewTagManager(), module
              .getDatabase().getContent().getNodeTagManager());

      m_dialog.addListener(m_filterDialogListener);
    }

    add(new JButton(new CFilterAction()));
  }

  @Override
  public void addListener(final IFilterComponentListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public IFilter<INaviView> createFilter() {
    return new IFilter<INaviView>() {
      @Override
      public boolean checkCondition(final INaviView element) {
        final CViewTypePanel typePanel = m_dialog.getViewTypePanel();

        return ((typePanel.isShowCallgraphViews()
            && (element.getGraphType() == GraphType.CALLGRAPH)) || (typePanel.isShowMixedViews()
            && (element.getGraphType() == GraphType.MIXED_GRAPH)) || (
            typePanel.isShowFlowgraphViews() && (element.getGraphType() == GraphType.FLOWGRAPH)));
      }

      @Override
      public IFilledList<INaviView> get(final List<INaviView> elements) {
        return new FilledList<INaviView>(CollectionHelpers.filter(elements,
            new ICollectionFilter<INaviView>() {
              @Override
              public boolean qualifies(final INaviView item) {
                return checkCondition(item);
              }
            }));
      }
    };
  }

  public void dispose() {
    if (m_dialog != null) {
      m_dialog.removeListener(m_filterDialogListener);
    }
  }

  @Override
  public Component getComponent() {
    return this;
  }

  @Override
  public void removeListener(final IFilterComponentListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Action class for handling clicks on the Filter button.
   */
  private class CFilterAction extends AbstractAction {
    /**
     * Creates a new filter action object. TODO(thomasdullien): There seems to be duplication with
     * code in CFunctionFilterComponent, refactor to get rid of the duplication.
     */
    public CFilterAction() {
      super("", new ImageIcon(CMain.class.getResource("data/funnel.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      int y = (getLocationOnScreen().y - m_dialog.getHeight()) + getHeight();

      if (y < 0) {
        y = 0;
      }

      m_dialog.setLocation(getLocationOnScreen().x + getWidth(), y);

      m_dialog.setVisible(true);
    }
  }

  /**
   * Listens on the view filter dialog and forwards changes to the filter creator.
   */
  private class InternalFilterDialogListener implements IFilterDialogListener {
    @Override
    public void updated() {
      for (final IFilterComponentListener listener : m_listeners) {
        listener.updated();
      }
    }
  }
}
