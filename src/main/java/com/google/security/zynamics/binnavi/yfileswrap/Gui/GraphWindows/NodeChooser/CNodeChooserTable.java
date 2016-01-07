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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeChooser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeChooser.CNodeChooserModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeChooser.CNodeChooserMouseListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.IGraphSearchFieldListener;
import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.INaviGraphListener;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchField;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.view.EdgeLabel;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

/**
 * Table that displays the nodes of a graph.
 */
public final class CNodeChooserTable extends JTable implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8103057846466908170L;

  /**
   * Model of the table.
   */
  private final CNodeChooserModel m_model;

  /**
   * Handles clicks on the table.
   */
  private final CNodeChooserMouseListener m_mouselistener;

  /**
   * Updates the table when new search results arrive.
   */
  private final InternalSearchFieldListener m_searchFieldListener =
      new InternalSearchFieldListener();

  /**
   * Updates the table when the view changes.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Provides the nodes for the table.
   */
  private final ZyGraph m_graph;

  /**
   * Search field whose results are displayed in the table.
   */
  private final CGraphSearchField m_searchField;

  /**
   * Creates a new node chooser table.
   * 
   * @param graph Provides the nodes for the table.
   * @param searchField Search field whose results are displayed in the table.
   */
  public CNodeChooserTable(final ZyGraph graph, final CGraphSearchField searchField) {
    m_graph = Preconditions.checkNotNull(graph, "IE01773: Graph argument can't be null.");
    m_searchField =
        Preconditions.checkNotNull(searchField, "IE01774: Search field argument can not be null");

    m_model = new CNodeChooserModel(graph);
    setModel(m_model);

    final TableRowSorter<CNodeChooserModel> tableSorter =
        new TableRowSorter<CNodeChooserModel>(m_model);
    setRowSorter(tableSorter);
    tableSorter.setComparator(CNodeChooserModel.COLUMN_IN, new IntComparator());
    tableSorter.setComparator(CNodeChooserModel.COLUMN_OUT, new IntComparator());
    tableSorter.setComparator(CNodeChooserModel.COLUMN_ADDRESS, new LexicalComparator());
    tableSorter.setComparator(CNodeChooserModel.COLUMN_COLOR, new IntComparator());

    final CNodeChooserRenderer renderer =
        new CNodeChooserRenderer(this, m_graph, m_searchField.getGraphSearcher());

    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    setCellSelectionEnabled(false);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getTableHeader().setReorderingAllowed(false);

    getColumnModel().getColumn(0).setCellRenderer(renderer);
    getColumnModel().getColumn(1).setCellRenderer(renderer);
    getColumnModel().getColumn(2).setCellRenderer(renderer);
    getColumnModel().getColumn(3).setCellRenderer(renderer);

    getColumnModel().getColumn(0).setPreferredWidth(35);
    getColumnModel().getColumn(1).setPreferredWidth(35);
    getColumnModel().getColumn(3).setPreferredWidth(50);
    getColumnModel().getColumn(0).setMaxWidth(50);
    getColumnModel().getColumn(1).setMaxWidth(50);
    getColumnModel().getColumn(3).setMaxWidth(50);

    m_searchField.addListener(m_searchFieldListener);

    m_mouselistener = new CNodeChooserMouseListener(this, graph);
    addMouseListener(m_mouselistener);
    m_graph.addListener((INaviGraphListener) m_viewListener);
    m_graph.addListener((IZyGraphSelectionListener) m_viewListener);
    initializeViewListeners(m_graph.getRawView());
  }

  /**
   * Initializes all necessary listeners.
   * 
   * @param view The view where the listeners are added.
   */
  private void initializeViewListeners(final INaviView view) {
    view.addListener(m_viewListener);
  }

  /**
   * Removes all previously added listeners.
   * 
   * @param view View from which the listeners are removed.
   */
  private void removeViewListeners(final INaviView view) {
    view.removeListener(m_viewListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    removeMouseListener(m_mouselistener);
    m_searchField.removeListener(m_searchFieldListener);
    m_graph.removeListener((INaviGraphListener) m_viewListener);
    m_graph.removeListener((IZyGraphSelectionListener) m_viewListener);
    removeViewListeners(m_graph.getRawView());
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "The node chooser is used to select or deselect individual nodes of the graph.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }

  /**
   * Converts between the index of visible rows and the index of model rows.
   * 
   * @param viewIndex The view index to convert.
   * 
   * @return The converted model index.
   */
  public int modelIndex(final int viewIndex) {
    return convertRowIndexToModel(viewIndex);
  }

  /**
   * Updates the table when new search results arrive.
   */
  private class InternalSearchFieldListener implements IGraphSearchFieldListener {
    @Override
    public void searched() {
    }
  }

  /**
   * Updates the table when the view changes.
   * 
   * TODO: Split this into multiple listener classes.
   */
  private class InternalViewListener extends CViewListenerAdapter implements
      IZyGraphSelectionListener, INaviGraphListener {

    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      m_model.addNode(node);
    }

    @Override
    public void edgeClicked(final NaviEdge node, final MouseEvent event, final double x,
        final double y) {
      // we do not care here.
    }

    @Override
    public void edgeLabelEntered(final EdgeLabel label, final MouseEvent event) {
      // we do not care here.
    }

    @Override
    public void edgeLabelExited(final EdgeLabel label) {
      // we do not care here.
    }

    @Override
    public void nodeClicked(final NaviNode node, final MouseEvent event, final double x,
        final double y) {
      // we do not care here.
    }

    @Override
    public void nodeEntered(final NaviNode node, final MouseEvent event) {
      // we do not care here.
    }

    @Override
    public void nodeHovered(final NaviNode node, final double x, final double y) {
      // we do not care here.
    }

    @Override
    public void nodeLeft(final NaviNode node) {
      // we do not care here.
    }

    @Override
    public void proximityBrowserNodeClicked(final ZyProximityNode<?> proximityNode,
        final MouseEvent event, final double x, final double y) {
      // we do not care here.
    }

    @Override
    public void addedNode(final ZyGraph graph, final NaviNode node) {
      // we do not care here.
    }

    @Override
    public void changedModel(final ZyGraph graph, final NaviNode node) {
      // we do not care here.
    }

    @Override
    public void untaggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
      // we do not care here.
    }

    @Override
    public void selectionChanged() {
      // we do not care here.
    }

    @Override
    public void changedColor(final INaviView view, final IViewNode<?> node, final Color color) {
      m_model.changedNode(node);
    }

    @Override
    public void changedSelection(final INaviView view, final IViewNode<?> node,
        final boolean selected) {
      m_model.changedNode(node);
    }

    @Override
    public void changedView(final INaviView oldView, final INaviView newView) {
      removeViewListeners(oldView);
      initializeViewListeners(newView);
    }

    @Override
    public void changedVisibility(final INaviView view, final IViewNode<?> node,
        final boolean selected) {
      m_model.changedNode(node);
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      m_model.deleteNode(node);
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      m_model.deleteNodes(nodes);
    }
  }
}
