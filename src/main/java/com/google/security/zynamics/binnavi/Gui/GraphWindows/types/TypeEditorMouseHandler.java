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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.AddTypeAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.AppendMemberAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.EditMemberAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.EditTypeAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.InsertMemberAction;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Handles mouse input and displays the appropriate popup menus if the user clicks on the types
 * editor tree component.
 *
 * @author jannewger (Jan Newger)
 */
public class TypeEditorMouseHandler extends MouseAdapter {

  private final JTree tree;
  private final TypeEditor typeEditor;
  private final TypeManager typeManager;
  private final JFrame owner;

  /**
   * Creates a new mouse adapter instance for the given tree component.
   *
   * @param owner The owner window that can be used to display additional GUI elements.
   * @param tree The tree component for which to show context menus.
   * @param typeManager The type manager that holds the current type system.
   * @param typeEditor The type editor that is used to determine currently selected base or member
   *        types.
   */
  public TypeEditorMouseHandler(final JFrame owner, final JTree tree, final TypeManager typeManager,
      final TypeEditor typeEditor) {
    this.owner = owner;
    this.tree = tree;
    this.typeManager = typeManager;
    this.typeEditor = typeEditor;
  }

  private JPopupMenu createNodeClickedMenu(final TreeNode clickedNode) {
    final JPopupMenu popupMenu = new JPopupMenu();
    if (clickedNode instanceof TypeMemberTreeNode) {
      final TypeMember selectedMember = ((TypeMemberTreeNode) clickedNode).getTypeMember();
      final AbstractAction editMemberAction = new EditMemberAction(
          owner, typeManager, selectedMember);
      final AbstractAction insertAction =
          new InsertMemberAction(owner, typeManager, selectedMember);
      if (tree.getSelectionCount() > 1) {
        editMemberAction.setEnabled(false);
        insertAction.setEnabled(false);
      }
      if (selectedMember.getParentType() != null
          && selectedMember.getParentType().getCategory() == BaseTypeCategory.STRUCT) {
        popupMenu.add(new AppendMemberAction(owner, typeManager, selectedMember.getParentType()));
        popupMenu.add(insertAction);
      }
      popupMenu.add(editMemberAction);
      popupMenu.add(new DeleteMemberAction(owner, typeManager, typeEditor));
    } else if (clickedNode instanceof BaseTypeTreeNode) {
      final BaseType selectedType = ((BaseTypeTreeNode) clickedNode).getBaseType();
      final AbstractAction editAction = new EditTypeAction(owner, typeManager, selectedType);
      final AbstractAction appendAction = new AppendMemberAction(owner, typeManager, selectedType);
      if (tree.getSelectionCount() > 1) {
        editAction.setEnabled(false);
        appendAction.setEnabled(false);
      } else if (selectedType.getCategory() != BaseTypeCategory.STRUCT) {
        appendAction.setEnabled(false);
      }
      popupMenu.add(editAction);
      popupMenu.add(appendAction);
      popupMenu.add(new DeleteTypeAction(owner, typeManager, typeEditor));
    }
    return popupMenu;
  }

  private void showMenu(final MouseEvent e) {
    final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    JPopupMenu popupMenu;
    if (path != null) {
      popupMenu = createNodeClickedMenu((TreeNode) path.getLastPathComponent());
    } else {
      popupMenu = new JPopupMenu();
      popupMenu.add(new AddTypeAction(owner, typeManager));
    }
    popupMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      showMenu(e);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      showMenu(e);
    }
  }
}