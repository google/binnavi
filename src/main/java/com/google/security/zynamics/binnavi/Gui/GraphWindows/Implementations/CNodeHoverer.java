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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import java.awt.Color;



/**
 * Class that can be used to highlight graph lines when the mouse cursor hovers above them.
 */
public final class CNodeHoverer {
  /**
   * The last line over which the mouse cursor hovered.
   */
  private ZyLineContent m_lastHoveredLine;

  /**
   * Changes the hovered line in a node.
   *
   * @param content The content of the hovered node.
   * @param lineContent The line over which the mouse hovered.
   * @param color The highlighting color.
   *
   * @return True, if the content needed updating. False, otherwise.
   */
  private static boolean setHoveredLine(
      final ZyLabelContent content, final ZyLineContent lineContent, final Color color) {
    boolean update = false;

    for (final ZyLineContent line : content) {
      if (line.equals(lineContent)) {
        update |= line.setHighlighting(1, color);
      } else {
        update |= line.clearHighlighting(1);
      }
    }

    return update;
  }

  /**
   * Clears all node hovering highlighting for a given node.
   *
   * @param node The node to clear.
   *
   * @return True, if there was highlighting to be cleared. False, otherwise.
   */
  public boolean clear(final NaviNode node) {
    final IZyNodeRealizer realizer = node.getRealizer();

    boolean update = false;

    for (final ZyLineContent line : realizer.getNodeContent()) {
      update |= line.clearHighlighting(1);
    }

    m_lastHoveredLine = null;

    return update;
  }

  /**
   * Updates the node hoverer.
   *
   * @param node Node over which the mouse hovered.
   * @param y Current y-coordinate of the mouse.
   */
  public void nodeHovered(final NaviNode node, final double y) {
    // group nodes do not have any mouse to line matchings.
    if (node.getRawNode() instanceof CGroupNode) {
      return;
    }

    final double yPos = y - node.getY();

    final IZyNodeRealizer realizer = node.getRealizer();

    final int row = node.positionToRow(yPos);

    if (row == -1) {
      if (m_lastHoveredLine != null) {
        m_lastHoveredLine.clearHighlighting(1);
        m_lastHoveredLine = null;
      }

      return;
    }

    final ZyLabelContent content = realizer.getNodeContent();

    final ZyLineContent hoveredLine = content.getLineContent(row);

    if (hoveredLine.equals(m_lastHoveredLine)) {
      return;
    }

    setHoveredLine(content, hoveredLine, realizer.isSelected() ? realizer.getFillColor()
        .darker().darker() : realizer.getFillColor().darker());

    if (m_lastHoveredLine != null) {
      m_lastHoveredLine.clearHighlighting(1);
    }

    m_lastHoveredLine = hoveredLine;
  }

}
