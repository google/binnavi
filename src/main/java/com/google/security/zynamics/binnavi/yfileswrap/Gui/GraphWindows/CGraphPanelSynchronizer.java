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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CNodeHoverer;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu.CGraphWindowMenuBar;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CProximityFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.CEdgeMenu;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.CProximityNodeMenu;
import com.google.security.zynamics.binnavi.ZyGraph.Updaters.CNodeUpdaterInitializer;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviGraphListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

/**
 * Synchronizes events in graph and view objects with the displayed graph.
 */
public final class CGraphPanelSynchronizer {
  /**
   * Model of the displayed graph.
   */
  private final CGraphModel m_model;

  /**
   * Graph-specific menu bar that is shown when the graph is active.
   */
  private final CGraphWindowMenuBar m_menuBar;

  /**
   * Handles clicks on nodes.
   */
  private final CNodeClickHandler m_clickHandler;

  /**
   * Updates the displayed graph on relevant changes in the underlying view object.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Updates the displayed graph on relevant changes in the graph.
   */
  private final InternalGraphListener m_graphListener = new InternalGraphListener();

  /**
   * Flag that indicates whether the graph panel has requested the view to be closed or not.
   */
  private boolean closing = false;

  /**
   * Extension objects that add menu items to the context menu of code nodes.
   */
  private final List<ICodeNodeExtension> m_codeNodeExtensions = new ArrayList<ICodeNodeExtension>();

  /**
   * Creates a new synchronizer object.
   * 
   * @param model Model of the displayed graph.
   * @param menuBar Graph-specific menu bar that is shown when the graph is active.
   */
  public CGraphPanelSynchronizer(final CGraphModel model, final CGraphWindowMenuBar menuBar) {
    m_model = Preconditions.checkNotNull(model, "IE01618: Model argument can not be null");
    m_menuBar = Preconditions.checkNotNull(menuBar, "IE01620: Menu bar argument can not be null");

    m_clickHandler = new CNodeClickHandler(model);

    m_model.getGraph().addListener(m_graphListener);
    m_model.getGraph().getRawView().addListener(m_viewListener);
  }


  /**
   * Frees allocated resources.
   */
  public void dispose() {
    closing = true;

    m_model.getGraph().getRawView().removeListener(m_viewListener);
    m_model.getGraph().removeListener(m_graphListener);
  }

  /**
   * Adds a new extension object that adds entries to the context menu of code nodes.
   * 
   * @param extension The extension object to add.
   */
  public void registerCodeNodeContextMenuExtension(final ICodeNodeExtension extension) {
    Preconditions.checkNotNull(extension, "IE01621: Extension argument can not be null");

    m_codeNodeExtensions.add(extension);
  }

  /**
   * Updates the displayed graph on relevant changes in the graph.
   */
  private class InternalGraphListener extends NaviGraphListenerAdapter {
    /**
     * Highlights lines on mouse-over.
     */
    private final CNodeHoverer m_nodeHoverer = new CNodeHoverer();

    /**
     * Handles proximity-browsing related updating events.
     */
    private final CProximityFunctions m_proximityFunctions = new CProximityFunctions();

    @Override
    public void addedNode(final ZyGraph graph, final NaviNode node) {
      CNodeUpdaterInitializer.addUpdaters(m_model, node);
    }

    @Override
    public void changedModel(final ZyGraph graph, final NaviNode node) {
      CNodeUpdaterInitializer.addUpdaters(m_model, node);
    }

    @Override
    public void changedView(final INaviView oldView, final INaviView newView) {
      oldView.removeListener(m_viewListener);

      newView.addListener(m_viewListener);

      m_menuBar.updateGui();
    }

    @Override
    public void edgeClicked(final NaviEdge edge, final MouseEvent event, final double x,
        final double y) {
      if (event.getButton() == MouseEvent.BUTTON3) {
        final JPopupMenu menu = new CEdgeMenu(m_model.getParent(), m_model.getGraph(), edge);

        menu.show(m_model.getGraph().getView(), event.getX(), event.getY());
      }
    }

    @Override
    public void nodeClicked(final NaviNode node, final MouseEvent event, final double x,
        final double y) {
      m_clickHandler.nodeClicked(node, event, x, y, m_codeNodeExtensions);
    }

    @Override
    public void nodeHovered(final NaviNode node, final double x, final double y) {
      m_nodeHoverer.nodeHovered(node, y);

      m_model.getGraph().updateViews();
    }

    @Override
    public void nodeLeft(final NaviNode node) {
      if (m_nodeHoverer.clear(node)) {
        m_model.getGraph().updateViews();
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void proximityBrowserNodeClicked(final ZyProximityNode<?> proximityNode,
        final MouseEvent event, final double x, final double y) {
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (!event.isShiftDown() && !event.isControlDown()) {
          m_proximityFunctions.showHiddenNodes(m_model.getParent(), m_model.getGraph(),
              (ZyProximityNode<INaviViewNode>) proximityNode);
        } else if (event.isShiftDown()) {
          m_proximityFunctions.unhideAndSelect(m_model.getGraph(),
              (ZyProximityNode<INaviViewNode>) proximityNode);
        } else if (event.isControlDown()) {
          m_proximityFunctions.unhideAndSelectOnly(m_model.getGraph(),
              (ZyProximityNode<INaviViewNode>) proximityNode);
        }
      } else if (event.getButton() == MouseEvent.BUTTON3) {
        final JPopupMenu menu =
            new CProximityNodeMenu(m_model.getParent(), m_model.getGraph(),
                (ZyProximityNode<INaviViewNode>) proximityNode);

        menu.show(m_model.getGraph().getView(), event.getX(), event.getY());
      }
    }
  }

  /**
   * Updates the displayed graph on relevant changes in the underlying view object.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public boolean closingView(final INaviView view) {
      return view != m_model.getGraph().getRawView() || closing;
    }
  }
}
