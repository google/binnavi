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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;



/**
 * Tree component that shows the currently active criteria tree.
 */
public final class JCriteriumTree extends JTree {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 9053115808955523376L;

  /**
   * Shows the context menu when the user right-clicks on nodes.
   */
  private final InternalMouseListener m_mouseListener = new InternalMouseListener();

  /**
   * Model of the tree.
   */
  private final JCriteriumTreeModel m_model;

  /**
   * Currently active selection path.
   */
  private TreePath m_currentCriteriumPath = null;

  /**
   * Creates a new tree object.
   * 
   * @param ctree The criterium tree that backs the visible tree.
   * @param criteria List of available criteria.
   */
  public JCriteriumTree(final CCriteriumTree ctree, final List<ICriteriumCreator> criteria) {
    m_model = new JCriteriumTreeModel(this, ctree, criteria);

    setRootVisible(true);

    setModel(m_model);

    setCellRenderer(new CTreeNodeRenderer());

    addMouseListener(m_mouseListener);
  }

  /**
   * Shows the context menu for a given mouse event.
   * 
   * @param event The mouse event that triggered the context menu.
   */
  private void showPopupMenu(final MouseEvent event) {
    final JCriteriumTreeNode selectedNode =
        (JCriteriumTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode != null) {
      final JPopupMenu menu = selectedNode.getPopupMenu();

      if (menu != null) {
        menu.show(this, event.getX(), event.getY());
      }
    }
  }

  /**
   * Frees allocated resources.
   */
  public void delete() {
    m_model.dispose();

    removeMouseListener(m_mouseListener);
  }

  /**
   * Returns the currently active selection path.
   * 
   * @return The currently active selection path.
   */
  public TreePath getCurrentCriteriumPath() {
    return m_currentCriteriumPath;
  }

  @Override
  public JCriteriumTreeModel getModel() {
    return m_model;
  }

  /**
   * Changes the currently active selection path.
   * 
   * @param path The new selection path.
   */
  public void setCurrentCriteriumPath(final TreePath path) {
    m_currentCriteriumPath = path;
  }

  /**
   * Shows the context menu when the user right-clicks on nodes.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      m_currentCriteriumPath = getPathForLocation(event.getX(), event.getY());

      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      m_currentCriteriumPath = getPathForLocation(event.getX(), event.getY());

      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
