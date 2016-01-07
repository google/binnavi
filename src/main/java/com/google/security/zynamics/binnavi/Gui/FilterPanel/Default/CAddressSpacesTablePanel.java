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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.Default;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterFieldListener;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilteredTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CAddressSpaceFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CAddressSpaceFilterCreator;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.zylib.gui.textfields.TextHelpers;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;



/**
 * Component that is displayed on the right side of the main window whenever an address spaces node
 * was selected.
 */
public final class CAddressSpacesTablePanel extends CTablePanel<INaviAddressSpace> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1095550741905926918L;

  /**
   * Shows a context menu when the filter field is clicked.
   */
  private final IFilterFieldListener m_fieldListener = new InternalFieldListener();

  /**
   * Creates a new panel object.
   * 
   * @param table The table to show on the panel.
   */
  public CAddressSpacesTablePanel(final IFilteredTable<INaviAddressSpace> table) {
    super(table, new CAddressSpaceFilterCreator(), new CAddressSpaceFilterHelp());

    addListener(m_fieldListener);
  }

  /**
   * Context menu of the filter field.
   */
  private static class CFieldMenu extends JPopupMenu {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -2569909227954331131L;

    /**
     * Creates a new menu object.
     * 
     * @param filterField The filter field the context menu belongs to.
     */
    public CFieldMenu(final JTextField filterField) {
      add(new CFilterModulesAction(filterField));
    }
  }

  /**
   * Action that adds text to filter by module count to the filter field.
   */
  private static final class CFilterModulesAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 8698938796121096841L;

    /**
     * The filter field where the text is inserted.
     */
    private final JTextField m_filterField;

    /**
     * Creates a new action object.
     * 
     * @param filterField The filter field where the text is inserted.
     */
    public CFilterModulesAction(final JTextField filterField) {
      super("Filter by module count");

      m_filterField = filterField;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      TextHelpers.insert(m_filterField, "modules==0");
    }
  }

  /**
   * Shows a context menu when the filter field is clicked.
   */
  private class InternalFieldListener implements IFilterFieldListener {
    /**
     * Shows a context menu depending on the mouse event.
     * 
     * @param event The mouse event the context menu depends on.
     */
    private void showPopupMenu(final MouseEvent event) {
      final JPopupMenu menu = new CFieldMenu(getFilterField());

      menu.show(event.getComponent(), event.getX(), event.getY());
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
