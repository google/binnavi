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
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeChooser.CNodeChooserModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Model.GraphSearcher;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.HtmlGenerator;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer class for the node chooser table.
 */
public final class CNodeChooserRenderer extends DefaultTableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7732373934467520210L;

  /**
   * Font used in the node chooser table.
   */
  private static final Font DEFAULT_CELL_FONT =
      new Font(GuiHelper.getDefaultFont(), Font.PLAIN, 12);

  /**
   * Background color for nodes that are not currently displaying search results.
   */
  private static final Color DEFAULT_BACKGROUND = new Color(252, 252, 252);

  /**
   * Background color for nodes that are currently displaying search results.
   */
  private static final Color SEARCH_HIT_BACKGROUND = new Color(252, 252, 200);

  /**
   * Text color used to print the row text of selected nodes.
   */
  private static final Color SELECTED_FOREGROUND = new Color(160, 0, 0);

  /**
   * Text color used to print the row text of unselected nodes.
   */
  private static final Color UNSELECTED_FOREGROUND = new Color(0, 0, 0);

  /**
   * Text color used to print the row text of invisible nodes.
   */
  private static final Color INVISIBLE_FOREGROUND = new Color(128, 128, 128);

  /**
   * Default monospace font used for tooltips.
   */
  private static final String MONOSPACE_FONT = GuiHelper.getMonospaceFont();

  /**
   * Table that is rendered by this renderer.
   */
  private final CNodeChooserTable m_table;

  /**
   * Graph that provides the nodes shown in the table.
   */
  private final ZyGraph m_graph;

  /**
   * Searcher field that provides search results to be rendered in the table.
   */
  private final GraphSearcher m_searcher;

  /**
   * Creates a new renderer object.
   *
   * @param nodeChooserTable Table that is rendered by this renderer.
   * @param graph Graph that provides the nodes shown in the table.
   * @param searcher Searcher field that provides search results to be rendered in the table.
   */
  public CNodeChooserRenderer(final CNodeChooserTable nodeChooserTable, final ZyGraph graph,
      final GraphSearcher searcher) {
    m_table = Preconditions.checkNotNull(nodeChooserTable, "IE01770: Table can't be null.");
    m_graph = Preconditions.checkNotNull(graph, "IE01771: Graph can't be null.");
    m_searcher = Preconditions.checkNotNull(searcher, "IE01772: Graph searcher can't be null.");

    setOpaque(true);
  }

  /**
   * Builds the tooltip text of a table row from the content of a node.
   *
   * @param content The content of a node.
   *
   * @return The tooltip text for the node.
   */
  private static String buildToolTip(final ZyLabelContent content) {
    return HtmlGenerator.getHtml(content, MONOSPACE_FONT, true);
  }

  /**
   * Returns the text to be shown in the node chooser table for a given node.
   *
   * @param node The node to display.
   *
   * @return The display text of the node.
   */
  private static String getNodeText(final INaviViewNode node) {
    if (node instanceof INaviFunctionNode) {
      return ((INaviFunctionNode) node).getFunction().getName();
    } else if (node instanceof INaviCodeNode) {
      return Iterables.getFirst(((INaviCodeNode) node).getInstructions(), null).getAddress()
          .toHexString();
    } else if (node instanceof INaviGroupNode) {
      return "GroupNode"; // ((INaviGroupNode) node).getComment(); //TODO fix this.
    } else if (node instanceof INaviTextNode) {
      return "Comment node";
    } else {
      throw new IllegalStateException("IE01152: Invalid node in node chooser");
    }
  }

  /**
   * Updates the background color of a row in case the corresponding node is currently displaying
   * search results.
   *
   * @param node The node that corresponds to the table row.
   */
  private void calculateBackgroundColor(final INaviViewNode node) {
    final List<SearchResult> results = m_searcher.getResults();

    for (final SearchResult result : results) {
      if ((result.getObject() instanceof NaviNode)
          && ((NaviNode) result.getObject()).getRawNode().equals(node)) {
        setBackground(SEARCH_HIT_BACKGROUND);
        return;
      }
    }
  }

  /**
   * Small helper function to return the node of a given node index.
   *
   * @param index The node index.
   *
   * @return The node at the given index.
   */
  private INaviViewNode getNode(final int index) {
    return m_graph.getRawView().getGraph().getNodes().get(index);
  }

  // ESCA-JAVA0138: Not our function
  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    setFont(DEFAULT_CELL_FONT);

    final INaviViewNode node = getNode(m_table.modelIndex(row));

    Color textColor = UNSELECTED_FOREGROUND;

    if (node.isSelected()) {
      textColor = SELECTED_FOREGROUND;
    } else if (!node.isVisible()) {
      textColor = INVISIBLE_FOREGROUND;
    }

    setForeground(textColor);

    if (column == CNodeChooserModel.COLUMN_IN) {
      setText(String.valueOf(node.getIncomingEdges().size()));
    } else if (column == CNodeChooserModel.COLUMN_OUT) {
      setText(String.valueOf(node.getOutgoingEdges().size()));
    } else if (column == CNodeChooserModel.COLUMN_ADDRESS) {
      setText(getNodeText(node));
    } else {
      setText("");
    }

    setBackground(DEFAULT_BACKGROUND);

    if (column == CNodeChooserModel.COLUMN_COLOR) {
      setBackground(node.getColor());
    } else {
      calculateBackgroundColor(node);
    }

    setToolTipText(buildToolTip(m_graph.getNode(node).getNodeContent()));

    return this;
  }
}
