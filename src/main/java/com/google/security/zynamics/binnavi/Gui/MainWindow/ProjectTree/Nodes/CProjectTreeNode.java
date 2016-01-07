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

import java.awt.Component;

import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTreeModel;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Base class for all nodes of the project tree.
 * 
 * @param <T> Type of the object held by the node.
 */
public abstract class CProjectTreeNode<T> extends IconNode implements IProjectTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8658942898679184241L;

  /**
   * The project tree.
   */
  private final JTree m_tree;

  /**
   * Component shown in the main window then the node is selected.
   */
  private final ILazyComponent m_component;

  /**
   * Creates the main menu and the context menu of the node.
   */
  private final CAbstractMenuBuilder m_menuBuilder;

  /**
   * Object represented by this tree node.
   */
  private T m_object;

  /**
   * Creates a new project tree node without operand backing.
   * 
   * @param tree The project tree.
   * @param component Component shown in the main window then the node is selected.
   * @param menuBuilder Creates the main menu and the context menu of the node.
   */
  protected CProjectTreeNode(final JTree tree, final ILazyComponent component,
      final CAbstractMenuBuilder menuBuilder) {
    this(tree, component, menuBuilder, null);
  }

  /**
   * Tells each child node to release its allocated resources.
   * 
   * @param tree The project tree.
   * @param component Component shown in the main window then the node is selected.
   * @param menuBuilder Creates the main menu and the context menu of the node.
   * @param object The object represented by the tree node.
   */
  protected CProjectTreeNode(final JTree tree, final ILazyComponent component,
      final CAbstractMenuBuilder menuBuilder, final T object) {
    m_tree = Preconditions.checkNotNull(tree, "Error: tree argument can not be null");
    m_component =
        Preconditions.checkNotNull(component, "Error: component argument can not be null");
    m_menuBuilder =
        Preconditions.checkNotNull(menuBuilder, "Error: menuBuilder argument can not be null");
    m_object = object;
  }

  /**
   * Invoked to tell the node to create its children.
   */
  protected abstract void createChildren();

  /**
   * Deletes all children of the node.
   */
  protected void deleteChildren() {
    for (int i = 0; i < getChildCount(); i++) {
      final IProjectTreeNode child = (IProjectTreeNode) getChildAt(i);

      child.dispose();
    }

    removeAllChildren();
  }

  /**
   * Returns the menu builder that builds the menus for this node.
   * 
   * @return The menu builder that builds the menus for this node.
   */
  protected CAbstractMenuBuilder getMenuBuilder() {
    return m_menuBuilder;
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    m_component.dispose();
    m_menuBuilder.dispose();
  }

  @Override
  public Component getComponent() {
    return m_component.getComponent();
  }

  @Override
  public final JMenuBar getMainMenu() {
    return m_menuBuilder.getMainMenu();
  }

  /**
   * Returns the object represented by the tree node.
   * 
   * @return The object represented by the tree node.
   */
  public T getObject() {
    return m_object;
  }

  @Override
  public final JPopupMenu getPopupMenu() {
    return m_menuBuilder.getPopupMenu();
  }

  /**
   * Returns the project tree.
   * 
   * @return The project tree.
   */
  public JTree getProjectTree() {
    return m_tree;
  }

  /**
   * Returns the tree model of the project tree.
   * 
   * @return The tree model of the project tree.
   */
  public CProjectTreeModel getTreeModel() {
    return (CProjectTreeModel) m_tree.getModel();
  }
}
