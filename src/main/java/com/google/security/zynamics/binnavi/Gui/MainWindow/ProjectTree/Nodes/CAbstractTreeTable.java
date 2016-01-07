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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.CTableSearcherHelper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTable;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Base class for all tables to be displayed on the right side of the main window.
 *
 * @param <T> Type of the elements shown in the table.
 */
public abstract class CAbstractTreeTable<T> extends CFilteredTable<T> {
  /**
   * The project tree of the main window.
   */
  private final JTree tree;

  /**
   * The raw table model of the table. Sorting is done in the super class.
   */
  private final CAbstractTreeTableModel<T> treeTableModel;

  /**
   * Mouse listener that handles clicks on the table.
   */
  private final InternalMouseListener mouseListener = new InternalMouseListener();

  /**
   * Creates a new abstract tree table object.
   *
   * @param projectTree The project tree shown in the main window.
   * @param model The raw model that is responsible for the table layout.
   * @param helpInfo Provides context-sensitive information for the table.
   */
  public CAbstractTreeTable(final JTree projectTree, final CAbstractTreeTableModel<T> model,
      final IHelpInformation helpInfo) {
    super(model, helpInfo);

    treeTableModel = Preconditions.checkNotNull(model, "IE01939: Model argument can't be null");
    tree =
        Preconditions.checkNotNull(projectTree, "IE02343: Project tree argument can not be null");

    addMouseListener(mouseListener);

    setDefaultRenderer(String.class, new CProjectTreeTableRenderer());

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.SEARCH_HK.getKeyStroke(), "SEARCH");
    getActionMap().put("SEARCH", CActionProxy.proxy(new SearchAction()));

    windowImap.put(HotKeys.DELETE_HK.getKeyStroke(), "DELETE");
    getActionMap().put("DELETE", CActionProxy.proxy(new DeleteAction()));

    updateUI();
  }

  /**
   * Creates a popup menu depending on where the user clicked and shows that context menu in the
   * table.
   *
   * @param event The mouse event that was created when the user clicked.
   */
  private void displayPopupMenu(final MouseEvent event) {
    final int selectedIndex = getSelectionIndex(event);

    if (selectedIndex != -1) {
      final JPopupMenu popupMenu = getPopupMenu(event.getX(), event.getY(), selectedIndex);

      if (popupMenu != null) {
        popupMenu.show(this, event.getX(), event.getY());
      }
    }
  }

  /**
   * Uses information from a mouse event to determine what row was clicked.
   *
   * @param event The mouse event.
   *
   * @return The index of the row that was clicked or -1 if the row could not be determined.
   */
  private int getSelectionIndex(final MouseEvent event) {
    return convertRowIndexToModel(rowAtPoint(event.getPoint()));
  }

  /**
   * Deletes the selected rows.
   */
  protected void deleteRows() {
  }

  /**
   * Returns the parent window of the tree table.
   *
   * @return The parent window of the tree table.
   */
  protected JFrame getParentWindow() {
    return (JFrame) SwingUtilities.getWindowAncestor(tree);
  }

  /**
   * Creates a table-specific popup menu.
   *
   * @param x The x coordinate where the user clicked.
   * @param y The y coordinate where the user clicked.
   * @param selectedIndex The index of the row where the user clicked.
   *
   * @return The popup menu to be shown or null if no popup menu should be shown.
   */
  protected abstract JPopupMenu getPopupMenu(int x, int y, int selectedIndex);

  /**
   * Returns the project tree of the main window.
   *
   * @return The project tree of the main window.
   */
  protected JTree getProjectTree() {
    return tree;
  }

  /**
   * Returns the normalized indices of the selected rows.
   *
   * @return The normalized indices of the selected rows.
   */
  protected int[] getSortSelectedRows() {
    final int[] rows = getSelectedRows();

    for (int i = 0; i < rows.length; i++) {
      rows[i] = convertRowIndexToModel(rows[i]);
    }
    return rows;
  }

  /**
   * Handles double-clicks on table rows.
   *
   * @param row The index of the row that was clicked.
   */
  protected abstract void handleDoubleClick(int row);

  @Override
  protected boolean processKeyBinding(final KeyStroke keyStroke, final KeyEvent event,
      final int condition, final boolean pressed) {
    // turn off edit but still can cause actions
    if (event.getKeyCode() == KeyEvent.VK_DELETE) {
      putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
      final boolean retvalue = super.processKeyBinding(keyStroke, event, condition, pressed);
      putClientProperty("JTable.autoStartsEdit", Boolean.TRUE);

      return retvalue;
    }

    return super.processKeyBinding(keyStroke, event, condition, pressed);
  }

  /**
   * Clean-up function.
   */
  @Override
  public void dispose() {
    removeMouseListener(mouseListener);
    treeTableModel.delete();
  }

  @Override
  public CAbstractTreeTableModel<T> getTreeTableModel() {
    return treeTableModel;
  }

  /**
   * Action class that handles the deletion of rows.
   */
  private class DeleteAction extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      deleteRows();
    }
  }

  /**
   * Listens on mouse events and handles right-clicks and double-clicks.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
        handleDoubleClick(getSelectionIndex(event));
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }
  }

  /**
   * Action class that handles table searching.
   */
  private class SearchAction extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      CTableSearcherHelper.search(SwingUtilities.getWindowAncestor(CAbstractTreeTable.this),
          CAbstractTreeTable.this);
    }
  }
}
