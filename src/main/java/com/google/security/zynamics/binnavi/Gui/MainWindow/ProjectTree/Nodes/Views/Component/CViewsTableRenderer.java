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
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphWindowListener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeTableRenderer;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.Gui.WindowManager.IWindowManagerListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CFunctionNodeColorizer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.GraphType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Table renderer class that is used to render all tables that show views.
 * 
 * <pre>
 * TODO(timkornau): this cell renderer has state which is causing problems in 
 * certain cases where the view passed as argument to the getTableCellRendererComponent 
 * function is different to the one saved in the renderer.
 * 
 * TODO(timkornau): the update mechanism on which this renderer relied to paint the custom "progress
 * bar" into the cell was updateUI() on the table component which is not correct.
 * 
 * TODO http://today.java.net/pub/a/today/2008/08/21/complex-table-cell-rendering.html
 * </pre>
 * 
 */
public class CViewsTableRenderer implements TableCellRenderer {
  /**
   * Table rendered by this renderer.
   */
  private final IViewsTable table;

  /**
   * Container the views belong to.
   */
  private final IViewContainer container;

  /**
   * Updates the table renderer on changes in graph windows.
   */
  private final InternalGraphWindowListener listener = new InternalGraphWindowListener();

  /**
   * Updates the table renderer on changes in graph panels.
   */
  private final InternalGraphPanelListener panelListener = new InternalGraphPanelListener();

  /**
   * Font that is used to display the names of normal functions.
   */
  private static Font normalFont;

  /**
   * Font that is used to display the names of normal functions, which are currently loaded and
   * displayed in a window.
   */
  private static Font normalBoldFont;

  /**
   * Image shown if the view is stared.
   */
  private static Image starImage;

  /**
   * Creates a new renderer object.
   * 
   * @param table Table rendered by this renderer.
   * @param container Container the views belong to.
   */
  public CViewsTableRenderer(final IViewsTable table, final IViewContainer container) {
    this.container =
        Preconditions.checkNotNull(container, "IE02032: Container argument can't be null");
    this.table = Preconditions.checkNotNull(table, "IE02351: table argument can not be null");

    if (starImage == null) {
      try {
        starImage =
            new ImageIcon(CMain.class.getResource("data/star.png").toURI().toURL()).getImage();
      } catch (MalformedURLException | URISyntaxException e) {
      }
    }

    CWindowManager.instance().addListener(listener);

    for (final CGraphWindow window : CWindowManager.instance().getOpenWindows()) {
      window.addListener(panelListener);
    }
  }

  /**
   * Determines the proper background color for a function of a given type.
   * 
   * @param table Table to be rendered.
   * @param isSelected True, if the cell to be rendered is selected.
   * @param functionType The type of the function.
   * @param graphType The type of the graph.
   * 
   * @return The color for that type.
   */
  private Color getBackgroundColor(final IViewsTable table, final boolean isSelected,
      final FunctionType functionType, final GraphType graphType) {
    if (isSelected) {
      return table.self().getSelectionBackground();
    }

    if (graphType == GraphType.FLOWGRAPH && functionType == FunctionType.NORMAL)
      return Color.WHITE;
    return CFunctionNodeColorizer.getFunctionColor(functionType);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    CWindowManager.instance().removeListener(listener);

    for (final CGraphWindow window : CWindowManager.instance().getOpenWindows()) {
      window.removeListener(panelListener);
    }
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    final INaviView view = this.table.getUnsortedView(row);
    if (view == null) {
      return new JLabel("No cross references");
    }
    final INaviFunction function = container.getFunction(view);

    if ((column == 0) && (function != null)) {
      final IDebugger debugger =
          container.getDebuggerProvider().getDebugger(function.getModule());

      if (debugger != null) {
        return new CAddressLabel(table, view, debugger, function, getBackgroundColor(this.table,
            isSelected, function.getType(), view.getGraphType()), null);
      }
    }

    return new ViewLabel(value, isSelected, row, column, null);
  }

  /**
   * Updates the table renderer on changes in graph panels.
   */
  private class InternalGraphPanelListener implements IGraphWindowListener {
    @Override
    public void graphPanelClosed() {
    }

    @Override
    public void graphPanelOpened(final CGraphPanel graphPanel) {
    }
  }

  /**
   * Updates the table renderer on changes in graph windows.
   */
  private class InternalGraphWindowListener implements IWindowManagerListener {
    @Override
    public void windowClosed(final CWindowManager windowManager, final IGraphContainerWindow window) {
      window.removeListener(panelListener);
    }

    @Override
    public void windowOpened(final CWindowManager windowManager, final IGraphContainerWindow window) {
      window.addListener(panelListener);
    }
  }

  /**
   * Concrete cell renderer class for views.
   */
  public class ViewLabel extends CProjectTreeTableRenderer {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 2794817948190337490L;

    /**
     * View rendered by this label.
     */
    private final INaviView m_view;

    /**
     * Index of the column rendered by this label.
     */
    private final int m_column;

    /**
     * Value to render.
     */
    private final Object m_value;

    /**
     * Creates a new label object.
     * 
     * @param value Value to be shown in the cell.
     * @param isSelected True, if the cell is selected.
     * @param row Index of the row to render.
     * @param column Index of the cell to render.
     */
    public ViewLabel(final Object value, final boolean isSelected, final int row, final int column,
        final Font font) {
      // JLabel only sets the height of the component based on the actual text that it contains.
      super.setText("### ");
      m_column = column;
      m_value = value;

      if (font == null) {
        normalFont = new Font(getFont().getFontName(), Font.PLAIN, 12);
        normalBoldFont = new Font(getFont().getFontName(), Font.BOLD, 12);
      } else {
        normalFont = new Font(font.getName(), Font.PLAIN, font.getSize());
        normalBoldFont = new Font(font.getName(), Font.BOLD, font.getSize());
      }

      m_view = table.getUnsortedView(row);
      final GraphType graphType = m_view.getGraphType();
      FunctionType functionType = FunctionType.NORMAL;
      final INaviFunction function = container.getFunction(m_view);

      if (function != null) {
        functionType = function.getType();
      }

      setBackground(getBackgroundColor(table, isSelected, functionType, graphType));
    }

    @Override
    public void paint(final Graphics graphics) {
      graphics.setColor(getBackground());
      graphics.fillRect(0, 0, getWidth(), getHeight());

      if (m_column == table.getNameColumn()) {
        CLoadProgressPainter.paint(m_view, graphics, getWidth(), getHeight(), getBackground());

        if (m_view.isStared()) {
          graphics.drawImage(starImage, 0, 0, getHeight() - 2, getHeight() - 2, null);
        }
      }

      if (m_value == null) {
        return;
      }

      final boolean isOpen = CWindowManager.instance().isOpen(m_view);
      graphics.setColor(Color.BLACK);
      graphics.setFont(isOpen ? normalBoldFont : normalFont);

      graphics.drawString(m_value.toString(),
          (m_column == table.getNameColumn()) && m_view.isStared() ? getHeight() : 0, 12);
    }
  }
}
