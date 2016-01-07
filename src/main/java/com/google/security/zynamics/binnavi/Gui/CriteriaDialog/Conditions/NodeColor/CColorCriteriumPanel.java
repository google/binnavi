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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;
import com.google.security.zynamics.zylib.gui.ColorPanel.IColorPanelListener;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;



/**
 * Panel used to edit a select by color criterium.
 */
public final class CColorCriteriumPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8472785670692105722L;

  /**
   * The criterium that is edited in this panel.
   */
  private final CColorCriterium m_colorCriterium;

  /**
   * Color panel where the user can pick a color.
   */
  private final ColorPanel m_selectedColorPanel = new ColorPanel(null, false);

  /**
   * Color panels for all colors of a graph.
   */
  private final List<ColorPanel> m_colorPanels = new ArrayList<ColorPanel>();

  /**
   * Updates the GUI on user input.
   */
  private final InternalColorPanelListener m_colorPanelListener = new InternalColorPanelListener();

  /**
   * Creates a new criterium panel.
   *
   * @param colorCriterium The criterium that is edited in this panel.
   * @param graph The graph on which Select by Criteria is executed.
   */
  public CColorCriteriumPanel(final CColorCriterium colorCriterium, final ZyGraph graph) {
    super(new BorderLayout());

    m_colorCriterium = colorCriterium;

    m_selectedColorPanel.addListener(m_colorPanelListener);
    m_selectedColorPanel.addMouseListener(m_colorPanelListener);

    initPanel(graph);
  }

  /**
   * Returns all node colors of a graph sorted by how often they appear.
   *
   * @param graph The graph whose node colors are determined.
   *
   * @return The list of node colors.
   */
  private List<Color> getColors(final ZyGraph graph) {
    final HashMap<Color, Integer> colors = new HashMap<Color, Integer>();

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode item) {
        final Color color = item.getRawNode().getColor();

        if (!colors.containsKey(color)) {
          colors.put(color, 0);
        }

        colors.put(color, colors.get(color) + 1);

        return IterationMode.CONTINUE;
      }
    });

    final ArrayList<Entry<Color, Integer>> colorList =
        new ArrayList<Map.Entry<Color, Integer>>(colors.entrySet());

    Collections.sort(colorList, new Comparator<Map.Entry<Color, Integer>>() {
      @Override
      public int compare(final Entry<Color, Integer> lhs, final Entry<Color, Integer> rhs) {
        return lhs.getValue() - rhs.getValue();
      }
    });

    return project(colorList);
  }

  /**
   * Creates the GUI of the panel.
   *
   * @param graph The graph whose node colors are determined.
   */
  private void initPanel(final ZyGraph graph) {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Color Condition"));

    final JPanel selectedColorPanel = new JPanel(new BorderLayout());
    selectedColorPanel.setBorder(new EmptyBorder(0, 5, 3, 5));
    selectedColorPanel.add(m_selectedColorPanel);

    final List<Color> colors = getColors(graph);

    final JPanel colorGrid = new JPanel(new GridLayout(1 + colors.size() / 4, 4));
    colorGrid.setBorder(new TitledBorder(""));

    for (final Color color : colors) {
      final JPanel outerColorPanel = new JPanel(new BorderLayout());
      outerColorPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

      final ColorPanel colorPanel = new ColorPanel(color, false);
      outerColorPanel.add(colorPanel, BorderLayout.CENTER);

      m_colorPanels.add(colorPanel);

      colorPanel.addListener(m_colorPanelListener);
      colorPanel.addMouseListener(m_colorPanelListener);

      colorGrid.add(outerColorPanel, BorderLayout.NORTH);
    }

    m_selectedColorPanel.setColor(colors.isEmpty() ? new Color(255, 255, 255) : colors.get(0));

    mainPanel.add(selectedColorPanel, BorderLayout.NORTH);

    final JPanel gridContainer = new JPanel(new BorderLayout());
    gridContainer.add(colorGrid, BorderLayout.NORTH);
    gridContainer.setBorder(new EmptyBorder(3, 5, 0, 5));

    mainPanel.add(gridContainer, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);

  }

  /**
   * Returns the colors from a color/integer map entry.
   *
   * @param colorList The list of map entries.
   *
   * @return The colors of the map entries.
   */
  private List<Color> project(final List<Entry<Color, Integer>> colorList) {
    final List<Color> colors = new ArrayList<Color>();

    for (final Entry<Color, Integer> entry : colorList) {
      colors.add(entry.getKey());
    }

    return colors;
  }

  /**
   * Frees allocated resources.
   */
  public void delete() {
    m_selectedColorPanel.removeListener(m_colorPanelListener);
    m_selectedColorPanel.removeMouseListener(m_colorPanelListener);

    for (final ColorPanel cp : m_colorPanels) {
      cp.removeListener(m_colorPanelListener);
      cp.removeMouseListener(m_colorPanelListener);
    }
  }

  /**
   * Returns the color selected by the user.
   *
   * @return The color selected by the user.
   */
  public Color getColor() {
    return m_selectedColorPanel.getColor();
  }

  /**
   * Updates the GUI on user input.
   */
  private class InternalColorPanelListener extends MouseAdapter implements IColorPanelListener {
    @Override
    public void changedColor(final ColorPanel panel) {
      m_colorCriterium.notifyListeners();
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final ColorPanel panel = (ColorPanel) event.getSource();

        final Color color = panel.getColor();

        if (color != null) {
          m_selectedColorPanel.setColor(color);
        }
      }
    }
  }
}
