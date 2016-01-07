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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CUserViewsTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Help.CViewFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CViewFilterCreator;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainerListener;
import com.google.security.zynamics.zylib.disassembly.GraphType;

import java.util.List;

import javax.swing.JTree;

/**
 * Component that is shown on the right side of the main window when a views container node is
 * selected.
 */
public final class CUserViewsComponent extends CTablePanel<INaviView> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -781341538571122763L;

  /**
   * Container that provides the views to be shown in the table.
   */
  private final IViewContainer container;

  /**
   * Updates the GUI on relevant changes to the view container.
   */
  private final InternalContainerListener m_containerListener = new InternalContainerListener();

  /**
   * Updates the GUI on relevant changes to the views.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Creates a new component object.
   *
   * @param projectTree Project tree that is updated on certain events.
   * @param container Container that provides the views to be shown in the table.
   */
  public CUserViewsComponent(final JTree projectTree, final IViewContainer container) {
    super(new CUserViewsTable(projectTree, container, container),
        new CViewFilterCreator(container), new CViewFilterHelp());

    Preconditions.checkNotNull(projectTree, "IE02013: Project tree argument can not be null");
    Preconditions.checkNotNull(container, "IE02014: Original container argument can not be null");

    this.container = container;

    container.addListener(m_containerListener);

    if (container.isLoaded()) {
      for (final INaviView view : container.getViews()) {
        view.addListener(m_viewListener);
      }
    }

    updateBorderText(getBorderText());
  }

  /**
   * Creates the border text that gives information about the number of views.
   *
   * @return The created border text.
   */
  private String getBorderText() {
    return String.format("%d Views", container.getViewCount());
  }

  @Override
  protected void disposeInternal() {
    container.removeListener(m_containerListener);

    if (container.isLoaded()) {
      for (final INaviView view : container.getViews()) {
        view.removeListener(m_viewListener);
      }
    }
  }

  /**
   * Updates the GUI on relevant changes to the view container.
   */
  private class InternalContainerListener implements IViewContainerListener {
    @Override
    public void addedView(final IViewContainer container, final INaviView view) {
      updateBorderText(getBorderText());

      view.addListener(m_viewListener);
    }

    @Override
    public void closedContainer(final IViewContainer moduleContainer, final List<INaviView> views) {
      if (container.isLoaded()) {
        for (final INaviView view : container.getViews()) {
          view.removeListener(m_viewListener);
        }
      }
    }

    @Override
    public void deletedView(final IViewContainer moduleContainer, final INaviView view) {
      updateBorderText(getBorderText());

      view.removeListener(m_viewListener);
    }

    @Override
    public void loaded(final IViewContainer container) {
      for (final INaviView view : container.getViews()) {
        view.addListener(m_viewListener);
      }

      updateBorderText(getBorderText());
    }
  }

  /**
   * Updates the GUI on relevant changes to the views.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void changedGraphType(final INaviView view, final GraphType type, final GraphType oldType) {
      updateBorderText(getBorderText());
    }
  }
}
