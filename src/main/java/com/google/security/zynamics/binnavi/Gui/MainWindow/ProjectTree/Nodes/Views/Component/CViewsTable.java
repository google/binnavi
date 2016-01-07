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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CShowViewFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CViewDragHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeViewsTableModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions.COpenInLastWindowAction;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.GraphBuilderEvents;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.IGraphBuilderListener;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.IGraphBuilderManagerListener;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyGraphBuilderManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyGraphBuilder;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * Table used to display views.
 */
public abstract class CViewsTable extends CAbstractTreeTable<INaviView> implements IViewsTable {
  /**
   * Model of the table.
   */
  private final CAbstractTreeViewsTableModel viewsTableModel;

  /**
   * Container the views belong to.
   */
  private final IViewContainer viewContainer;

  /**
   * Updates the table model on changes to the build state of views.
   */
  private final IGraphBuilderListener graphBuilderListener = new IGraphBuilderListener() {
    @Override
    public boolean building(final GraphBuilderEvents event, final int counter) {
      return true;
    }
  };

  /**
   * Synchronizes the table model with changes to the builder manager.
   */
  private final IGraphBuilderManagerListener builderManagerListener =
      new IGraphBuilderManagerListener() {
        @Override
        public void addedBuilder(final INaviView view, final ZyGraphBuilder builder) {
          builder.addListener(graphBuilderListener);
        }

        @Override
        public void removedBuilder(final INaviView view, final ZyGraphBuilder builder) {
          builder.removeListener(graphBuilderListener);
        }
      };

  /**
   * Creates a new table object.
   *
   * @param projectTree Project tree of the main window.
   * @param model Table model of this table.
   * @param container Container the views belong to.
   * @param helpInfo Provides context-sensitive information for the table.
   */
  public CViewsTable(final JTree projectTree, final CAbstractTreeViewsTableModel model,
      final IViewContainer container, final IHelpInformation helpInfo) {
    super(projectTree, model, helpInfo);

    viewContainer =
        Preconditions.checkNotNull(container, "IE02031: Container argument can not be null");
    viewsTableModel = Preconditions.checkNotNull(model, "IE02350: model argument can not be null");

    if (!GraphicsEnvironment.isHeadless()) {
      setDragEnabled(true);
    }

    setTransferHandler(new CViewDragHandler(this));

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.LOAD_NEW_WINDOW_HK.getKeyStroke(), "ShowNewKeyStroke");
    getActionMap().put("ShowNewKeyStroke", CActionProxy.proxy(new ShowNewWindowAction()));

    windowImap.put(HotKeys.LOAD_LAST_WINDOW_HK.getKeyStroke(), "ShowLastKeyStroke");
    getActionMap().put("ShowLastKeyStroke", CActionProxy.proxy(new ShowLastWindowAction()));

    ZyGraphBuilderManager.instance().addListener(builderManagerListener);
  }

  /**
   * Returns the selected views of the table. If the view of the passed row index is already
   * selected, then the selected views are returned. Otherwise, the given row is selected and the
   * view of that row is returned.
   *
   * @param sortedRow Row to be selected.
   *
   * @return Selected views.
   */
  private INaviView[] getSelectedViews(final int sortedRow) {
    final int[] sortSelectedRows = getSortSelectedRows();
    if (Ints.contains(sortSelectedRows, sortedRow)) {
      return getViews(sortSelectedRows);
    } else {
      final int viewRow = convertRowIndexToView(sortedRow);
      setRowSelectionInterval(viewRow, viewRow);
      return getViews(new int[]{sortedRow});
    }
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    return new CViewPopupMenu(getParentWindow(), this, viewContainer, getSelectedViews(row), x, y);
  }

  @Override
  protected void handleDoubleClick(final int row) {
    final Action action = CActionProxy.proxy(new COpenInLastWindowAction(getParentWindow(),
        viewContainer, new INaviView[] {viewsTableModel.getViews().get(row)}));

    action.actionPerformed(null);
  }

  @Override
  public void dispose() {
    super.dispose();
    ((CViewsTableRenderer) getDefaultRenderer(Object.class)).dispose();
    ZyGraphBuilderManager.instance().removeListener(builderManagerListener);
  }

  @Override
  public int getNameColumn() {
    return 0;
  }

  @Override
  public CAbstractTreeViewsTableModel getTreeTableModel() {
    return viewsTableModel;
  }

  @Override
  public INaviView getUnsortedView(final int row) {
    return getTreeTableModel().getViews().get(convertRowIndexToModel(row));
  }

  /**
   * Returns the views that correspond to row indices.
   *
   * @param rows The indices whose views are returned.
   *
   * @return The views identified by the row indices.
   */
  public INaviView[] getViews(final int[] rows) {
    final INaviView[] views = new INaviView[rows.length];

    for (int i = 0; i < views.length; i++) {
      views[i] = viewsTableModel.getViews().get(rows[i]);
    }

    return views;
  }

  /**
   * Action class used for showing a view in the last window.
   */
  private class ShowLastWindowAction extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      CShowViewFunctions.showViewInLastWindow(getParentWindow(), viewContainer,
          getViews(getSortSelectedRows()));
    }
  }

  /**
   * Action class used for showing a view in a new window.
   */
  private class ShowNewWindowAction extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      CShowViewFunctions.showViewInNewWindow(getParentWindow(), viewContainer,
          getViews(getSortSelectedRows()));
    }
  }
}
