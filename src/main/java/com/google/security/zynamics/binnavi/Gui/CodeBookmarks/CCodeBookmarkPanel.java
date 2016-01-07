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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Actions.CAddBookmarkAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.CCodeNodeExtensionAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.IGraphPanelExtension;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.BorderLayout;

import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel class that is used to code bookmark information.
 */
public final class CCodeBookmarkPanel extends CAbstractResultsPanel
    implements IGraphPanelExtension {

  /**
   * Bookmark manager used to manage all bookmarks shown in this panel.
   */
  private final CCodeBookmarkManager m_bookmarkManager;

  /**
   * The code bookmark table that is shown in the panel.
   */
  private final CCodeBookmarkTable m_bookmarkTable;

  /**
   * Creates a new bookmark panel.
   */
  public CCodeBookmarkPanel() {
    super(new BorderLayout());

    m_bookmarkManager = new CCodeBookmarkManager();
    m_bookmarkTable = new CCodeBookmarkTable(m_bookmarkManager);

    add(new JScrollPane(m_bookmarkTable), BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    m_bookmarkTable.dispose();
  }

  @Override
  public String getTitle() {
    return "Code Bookmarks";
  }

  @Override
  public void visit(final CGraphModel model, final IGraphPanelExtender extender) {
    extender.registerCodeNodeExtension(new CodeNodeExtension());

    extender.addTab("Code Bookmarks", this);

    m_bookmarkTable.getSelectionModel().addListSelectionListener(
        new InternalSelectionListener(m_bookmarkTable, model.getGraph()));
  }

  /**
   * This class is responsible for extending the context menu of code bookmarks. It adds a new menu
   * to the instruction menu that is shown when the user right-clicks on an instruction.
   */
  private class CodeNodeExtension extends CCodeNodeExtensionAdapter {
    @Override
    public void extendInstruction(
        final JMenu menu, final INaviCodeNode node, final INaviInstruction instruction) {
      if (!m_bookmarkManager.hasBookmark(instruction.getModule(), instruction.getAddress())) {
        menu.add(CActionProxy.proxy(new CAddBookmarkAction(
            m_bookmarkManager, instruction.getModule(), instruction.getAddress())));
      }
    }
  }

  /**
   * Responsible for zooming to code bookmarks when they are selected in the table that lists all
   * bookmarks.
   */
  private class InternalSelectionListener implements ListSelectionListener {
    /**
     * Graph to be zoomed.
     */
    private final ZyGraph m_graph;

    /**
     * Table where the code bookmarks are shown.
     */
    private final JTable m_table;

    /**
     * Creates a new listener object.
     *
     * @param table Table where the code bookmarks are shown.
     * @param graph Graph to be zoomed.
     */
    private InternalSelectionListener(final JTable table, final ZyGraph graph) {
      m_table = table;
      m_graph = graph;
    }

    @Override
    public void valueChanged(final ListSelectionEvent event) {
      if (!event.getValueIsAdjusting() && (m_table.getSelectedRow() != -1)) {
        ZyZoomHelpers.zoomToAddress(m_graph,
            m_bookmarkManager.get(m_table.getSelectedRow()).getAddress(),
            m_bookmarkManager.get(m_table.getSelectedRow()).getModule(), true);
      }
    }
  }
}
