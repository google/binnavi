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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeChooser;

import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

/**
 * Table model of the node chooser table.
 */
public final class CNodeChooserModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1300798042465396160L;

  /**
   * Names of the columns.
   */
  private static final String[] COLUMNNAMES = {"In", "Out", "Node", "Color"};

  /**
   * Index of the column where the number of incoming edges are shown.
   */
  public static final int COLUMN_IN = 0;

  /**
   * Index of the column where the number of outgoing edges are shown.
   */
  public static final int COLUMN_OUT = 1;

  /**
   * Index of the column where the node descriptions are shown.
   */
  public static final int COLUMN_ADDRESS = 2;

  /**
   * Index of the column where the node colors are shown.
   */
  public static final int COLUMN_COLOR = 3;

  /**
   * Graph that provides the nodes to display.
   */
  private final ZyGraph m_graph;

  /**
   * Used to cache the nodes to display. This brings significant performance gains.
   */
  private List<INaviViewNode> m_nodeCache;

  /**
   * Creates a new node chooser model.
   * 
   * @param graph The graph that provides the nodes to display.
   */
  public CNodeChooserModel(final ZyGraph graph) {
    m_graph = Preconditions.checkNotNull(graph, "IE01765: Graph argument can not be null");
    m_nodeCache = CollectionHelpers.filter(getNodes(), new NodeFilter());
  }

  private String getNodeAddress(final INaviViewNode node) {
    if (node instanceof INaviFunctionNode) {
      return ((INaviFunctionNode) node).getAddress().toHexString();
    }
    if (node instanceof INaviCodeNode) {
      return ((INaviCodeNode) node).getAddress().toHexString();
    }
    return null;
  }

  /**
   * Small helper function to return the nodes of the graph.
   * 
   * @return A list of nodes.
   */
  private List<INaviViewNode> getNodes() {
    if (m_nodeCache != null) {
      return m_nodeCache;
    }
    return m_graph.getRawView().getGraph().getNodes();
  }

  @Override
  public int getColumnCount() {
    return COLUMNNAMES.length;
  }

  @Override
  public String getColumnName(final int col) {
    return COLUMNNAMES[col];
  }

  @Override
  public int getRowCount() {
    if (m_nodeCache == null) {
      m_nodeCache = CollectionHelpers.filter(getNodes(), new NodeFilter());
    }

    return m_nodeCache.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case COLUMN_ADDRESS:
        return getNodeAddress(m_nodeCache.get(rowIndex));
      case COLUMN_COLOR:
        return m_nodeCache.get(rowIndex).getColor().getRGB();
      case COLUMN_IN:
        return m_nodeCache.get(rowIndex).getIncomingEdges().size();
      case COLUMN_OUT:
        return m_nodeCache.get(rowIndex).getOutgoingEdges().size();
    }

    // Don't bother with anything here; the renderer is handling this.
    return null;
  }

  /**
   * Resets the node cache. This function is called in case something significant happens to the
   * graph (nodes are added, nodes are deleted, ...).
   */
  public void reset() {
    m_nodeCache = null;

    fireTableDataChanged();
  }

  /**
   * // NOPMD by sp on 09.08.10 11:54 Small node filter class that makes sure that only code nodes
   * and function nodes are shown in the node chooser table.
   */
  private static class NodeFilter implements ICollectionFilter<INaviViewNode> {
    @Override
    public boolean qualifies(final INaviViewNode node) {
      return (node instanceof INaviFunctionNode) || (node instanceof INaviCodeNode);
    }
  }

  /**
   * This function is invoked from external if a node is added to the model.
   * 
   * @param node The node added to the model
   */
  public void addNode(final INaviViewNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    Preconditions.checkArgument(!m_nodeCache.contains(node),
        "Error: node can not be added more then once");
    m_nodeCache.add(node);
    final int index = m_nodeCache.indexOf(node);
    fireTableRowsInserted(index, index);
  }

  /**
   * 
   * 
   * @param node
   */
  public void changedNode(final IViewNode<?> node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    final int index = m_nodeCache.indexOf(node);
    fireTableRowsUpdated(index, index);
  }

  public void deleteNode(final INaviViewNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    final int index = m_nodeCache.indexOf(node);
    Preconditions.checkArgument(index != -1, "Error: the node is not known to the model");
    m_nodeCache.remove(index);
    fireTableRowsDeleted(index, index);
  }

  public void deleteNodes(final Collection<INaviViewNode> nodes) {
    Preconditions.checkNotNull(nodes, "Error: nodes argument can not be null");
    for (final INaviViewNode node : nodes) {
      deleteNode(node);
    }
  }
}
