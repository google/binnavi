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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterComponent;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterComponentListener;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
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
 * Component to be shown next to the filter field on function tables.
 */
public final class CFunctionFilterComponent extends JPanel implements IFilterComponent<INaviView> {
  /**
   * Module whose views are filtered.
   */
  private final IViewContainer module;

  /**
   * Dialog for filtering functions.
   */
  private final CFunctionFilterDialog dialog;

  /**
   * Listens on the function filter dialog and forwards changes to the filter creator.
   */
  private final IFilterDialogListener filterDialogListener = new InternalFilterDialogListener();

  /**
   * Listeners that are notified about changes in the filter component.
   */
  private final ListenerProvider<IFilterComponentListener> m_listeners =
      new ListenerProvider<IFilterComponentListener>();

  /**
   * Creates a new component object.
   *
   * @param module The module whose functions are filtered.
   */
  public CFunctionFilterComponent(final IViewContainer module) {
    super(new BorderLayout());

    this.module = module;

    if (GraphicsEnvironment.isHeadless()) {
      dialog = null; // Workaround for tests
    } else {
      dialog =
          new CFunctionFilterDialog(module.getDatabase().getContent().getViewTagManager(), module
              .getDatabase().getContent().getNodeTagManager());
      dialog.addListener(filterDialogListener);
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
        final CFunctionTypePanel typePanel = dialog.getFunctionTypePanel();

        final INaviFunction function = module.getFunction(element);

        return ((typePanel.isShowAdjustorFunctions()
            && (function.getType() == FunctionType.ADJUSTOR_THUNK))
            || (typePanel.isShowImportedFunctions() && (function.getType() == FunctionType.IMPORT))
            || (typePanel.isShowLibraryFunctions() && (function.getType() == FunctionType.LIBRARY))
            || (typePanel.isShowNormalFunctions() && (function.getType() == FunctionType.NORMAL))
            || (typePanel.isShowThunkFunctions() && (function.getType() == FunctionType.THUNK)));
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
    if (dialog != null) {
      dialog.removeListener(filterDialogListener);
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
     * Creates a new filter action object.
     */
    public CFilterAction() {
      super("", new ImageIcon(CMain.class.getResource("data/funnel.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      int y = (getLocationOnScreen().y - dialog.getHeight()) + getHeight();

      if (y < 0) {
        y = 0;
      }

      dialog.setLocation(getLocationOnScreen().x + getWidth(), y);

      dialog.setVisible(true);
    }
  }

  /**
   * Listens on the function filter dialog and forwards changes to the filter creator.
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
