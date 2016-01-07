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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Handles drag and drop operations within the types tree.
 *
 * @author jannewger (Jan Newger)
 *
 */
class MemberNodeTransferHandler extends TransferHandler {

  private final TypesTree typesTree;
  private List<TypeMemberTreeNode> selectedNodes;
  private final TypeManager typeManager;

  public MemberNodeTransferHandler(final TypesTree typesTree, final TypeManager typeManager) {
    this.typesTree = typesTree;
    this.typeManager = typeManager;
  }

  // Determines the list of currently selected nodes ordered by the offset of their corresponding
  // type members.
  private static List<TypeMemberTreeNode> getSelectedNodesSorted(final JTree tree) {
    final List<TypeMemberTreeNode> nodes = Lists.newArrayList();
    for (final TreePath path : tree.getSelectionPaths()) {
      final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
      if (node instanceof TypeMemberTreeNode) {
        nodes.add((TypeMemberTreeNode) node);
      } else {
        return Lists.newArrayList();
      }
    }
    Collections.sort(nodes, new Comparator<TypeMemberTreeNode>() {
      @Override
      public int compare(final TypeMemberTreeNode node0, final TypeMemberTreeNode node1) {
        return node0.getTypeMember().compareTo(node1.getTypeMember());
      }
    });

    return nodes;
  }

  // Determines the offset in bits by which the dragged members were moved.
  private int determineDestinationOffset(
      final JTree.DropLocation location, final BaseTypeTreeNode parentNode) {
    if (location.getChildIndex() == 0) {
      // The nodes were dropped before the first member.
      return 0;
    } else if (location.getPath().getLastPathComponent() == typesTree.getModel().getRoot()) {
      // The nodes were dropped behind the last member node of parentNode.
      return parentNode.getBaseType().getLastMember().getBitOffset().get();
    }
    // The nodes were dropped somewhere in between.
    final TypeMember destinationMember =
        ((TypeMemberTreeNode) parentNode.getChildAt(location.getChildIndex() - 1)).getTypeMember();
    return destinationMember.getBitOffset().get();
  }

  private BaseTypeTreeNode determineParentNode(final JTree.DropLocation location) {
    final Object node = location.getPath().getLastPathComponent();
    if (node instanceof BaseTypeTreeNode) {
      return (BaseTypeTreeNode) node;
    } else if (node == typesTree.getModel().getRoot()) {
      // The user dropped the member behind the last member of the base type.
      if (location.getChildIndex() > 0) {
        return (BaseTypeTreeNode) typesTree.getModel()
            .getChild(typesTree.getModel().getRoot(), location.getChildIndex() - 1);
      }
    }
    return null;
  }

  private boolean isDropForbidden(final TransferSupport support) {
    // Note: we need to make sure that this is actually a drop operation, otherwise
    // getDropLocation will throw an IllegalStateException.
    if (!support.isDrop() || !(support.getComponent() instanceof TypesTree)) {
      return true;
    }
    final JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
    // final TreeNode destinationNode = (TreeNode) location.getPath().getLastPathComponent();
    final BaseTypeTreeNode destinationNode = determineParentNode(location);
    if (destinationNode != null && location.getChildIndex() != -1) {
      final BaseType parentType = destinationNode.getBaseType();
      // We do not (yet) allow inter-base type member drag and drop operations.
      for (final TypeMemberTreeNode memberNode : selectedNodes) {
        if (memberNode.getTypeMember().getParentType() != parentType) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  @Override
  protected Transferable createTransferable(final JComponent component) {
    final TypesTree tree = (TypesTree) component;
    final Object node = tree.getSelectionPath().getLastPathComponent();
    if (node instanceof BaseTypeTreeNode) {
      return new TransferableBaseType(((BaseTypeTreeNode) node).getBaseType());
    } else if (node instanceof TypeMemberTreeNode) {
      final List<TypeMemberTreeNode> nodes = getSelectedNodesSorted((TypesTree) component);
      if (!nodes.isEmpty()) {
        selectedNodes = nodes;
        return new TransferableMemberNodes(nodes);
      }
    }
    return null;
  }

  @Override
  public boolean canImport(final TransferSupport support) {
    if (!support.isDataFlavorSupported(TypeDataFlavor.TYPE_MEMBER_FLAVOR)
        || isDropForbidden(support)) {
      return false;
    }
    return true;
  }

  @Override
  public int getSourceActions(final JComponent component) {
    return MOVE;
  }

  /**
   * Remove and re-insert dragged nodes at the correct position into JTree if this is a valid drop
   * operation.
   */
  @Override
  public boolean importData(final TransferSupport support) {
    if (!support.isDrop() || !canImport(support)) {
      return false;
    }

    try {
      @SuppressWarnings("unchecked")
      final List<TypeMemberTreeNode> draggedNodes = (List<
          TypeMemberTreeNode>) support.getTransferable()
          .getTransferData(TypeDataFlavor.TYPE_MEMBER_FLAVOR);
      final JTree.DropLocation location = typesTree.getDropLocation();
      final TypesTree tree = (TypesTree) support.getComponent();
      final TypesTreeModel model = tree.getModel();

      // Before updating the model we need to determine the parts of the tree that need to be
      // updated after the changes were made.
      final int[] selectedRows = new int[tree.getSelectionCount()];
      int row = 0;
      final BaseTypeTreeNode parentNode = determineParentNode(location);
      for (final TypeMemberTreeNode node : getSelectedNodesSorted(tree)) {
        selectedRows[row++] = model.getIndexOfChild(parentNode, node);
      }
      final List<TypeMember> draggedMembers = Lists.newArrayList();
      for (final TypeMemberTreeNode node : draggedNodes) {
        draggedMembers.add(node.getTypeMember());
      }

      final int delta = determineDestinationOffset(location, parentNode)
          - draggedMembers.get(0).getBitOffset().get();
      typeManager.moveMembers(parentNode.getBaseType(), draggedMembers, delta);
    } catch (IOException | UnsupportedFlavorException | CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);
      return false;
    }
    return true;
  }
}
