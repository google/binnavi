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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * The tree component that displays all types and members in the type editor.
 */
public class TypesTree extends JTree {

  public TypesTree() {
    setRootVisible(false);
    setDragEnabled(true);
    setDropMode(DropMode.INSERT); // Dropped objects are inserted behind the drop location.
    setToggleClickCount(1); // Child nodes expand with a single click.
    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    setSelectionModel(selectionModel);
    final IconNodeRenderer renderer = new IconNodeRenderer();
    renderer.setFont(GuiHelper.MONOSPACED_FONT);
    setCellRenderer(renderer);
  }

  private static TypesTree CreateDragAndDropTypesTree(final TypeManager typeManager) {
    final TypesTree typesTree = new TypesTree();
    typesTree.setTransferHandler(new MemberNodeTransferHandler(typesTree, typeManager));
    return typesTree;
  }

  private static void unfoldFirstNode(final TypesTree typesTree) {
    final DefaultMutableTreeNode root = (DefaultMutableTreeNode) typesTree.getModel().getRoot();
    if (root != null && root.getChildCount() > 0 && root.getFirstChild().getChildCount() > 0) {
      typesTree.setSelectionPath(
          new TreePath(typesTree.getModel().getPathToRoot(root.getFirstChild().getChildAt(0))));
    }
  }

  /**
   * Creates a types tree control, sets the model that includes all existing types and installs
   * drag'n'drop support.
   *
   * @param typeManager The type manager that holds all existing types.
   * @return A new types tree instance.
   */
  public static TypesTree createDefaultDndTypesTree(final TypeManager typeManager) {
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    final TypesTree typesTree = CreateDragAndDropTypesTree(typeManager);
    typesTree.setModel(TypesTreeModel.createDefaultModel(typeManager));
    return typesTree;
  }

  /**
   * Creates a types tree model, sets the model to only include the stack frame of the corresponding
   * function and install drag'n'drop support.
   *
   * @param function The function for which to display the stack frame.
   * @param typeManager The type manager that holds all existing types.
   * @return A new types tree instance.
   */
  public static TypesTree createStackFrameDndTypesTree(final INaviFunction function,
      final TypeManager typeManager) {
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    final TypesTree typesTree = CreateDragAndDropTypesTree(typeManager);
    final TypesTreeModel model =
        function.getStackFrame() == null ? TypesTreeModel.createEmptyTypeModel()
            : TypesTreeModel.createSingleTypeModel(typeManager, function.getStackFrame());
    typesTree.setModel(model);
    unfoldFirstNode(typesTree);
    return typesTree;
  }

  /**
   * Creates a types tree model, sets the model to only include the prototype of the corresponding
   * function and install drag'n'drop support.
   *
   * @param function The {@link INaviFunction function} for which to display the prototype.
   * @param typeManager The {@link TypeManager type manager} that holds the current type system.
   * @return A new types tree instance.
   */
  public static TypesTree createPrototypeDndTypesTree(INaviFunction function,
      TypeManager typeManager) {
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    final TypesTree typesTree = CreateDragAndDropTypesTree(typeManager);
    final TypesTreeModel model =
        function.getPrototype() == null ? TypesTreeModel.createEmptyTypeModel()
            : TypesTreeModel.createSingleTypeModel(typeManager, function.getPrototype());
    typesTree.setModel(model);
    unfoldFirstNode(typesTree);
    return typesTree;
  }

  /**
   * Represents a selection in the types tree: a root base type plus a possibly empty path of member
   * types.
   */
  public class TypeSelectionPath {
    private final BaseType rootType;
    private final List<TypeMember> members;
    private final boolean containsUnion;

    /**
     * Takes ownership of members.
     *
     * @param rootType The base type where the path starts. Can be null if no node is selected.
     * @param members The ordered list of members leading from the root type to the referenced
     *        member.
     */
    public TypeSelectionPath(BaseType rootType, List<TypeMember> members) {
      this.rootType = rootType;
      this.members = members;
      containsUnion = rootType == null ? false : containsUnion(rootType, members);
    }

    public BaseType getRootType() {
      return rootType;
    }

    public List<TypeMember> getMembers() {
      return members;
    }

    public boolean hasSelection() {
      return rootType != null;
    }

    public boolean containsUnion() {
      return containsUnion;
    }

    private boolean containsUnion(BaseType rootType, List<TypeMember> members) {
      if (rootType.getCategory() == BaseTypeCategory.UNION) {
        return true;
      }
      for (final TypeMember member : members) {
        if (member.getBaseType().getCategory() == BaseTypeCategory.UNION) {
          return true;
        }
      }
      return false;
    }

    /**
     * Returns the total offset of the selected member in bits.
     */
    public int determineTotalMemberOffset() {
      if (containsBaseTypeOnly()) {
        return 0;
      }
      int offset = 0;
      for (final TypeMember member : members) {
        offset += member.getBitOffset().get();
      }
      return offset;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder(rootType.getName());
      for (TypeMember member : members) {
        builder.append('.');
        builder.append(member.getName());
      }
      return builder.toString();
    }

    /**
     * Returns whether the selection only contains a base type but no members.
     */
    public boolean containsBaseTypeOnly() {
      return members.isEmpty();
    }
  }

  /**
   * Returns a {@link TypeSelectionPath} instance that describes the path to the selected member or
   * base type. Returns null if no selection exists.
   */
  public TypeSelectionPath determineTypePath() {
    final TreePath path = getSelectionModel().getSelectionPath();
    if (path == null || path.getPathCount() == 0) {
      return new TypeSelectionPath(null, new ArrayList<TypeMember>());
    }
    // The first node is the invisible root node, the second a base type node. Afterwards, a list
    // of member nodes.
    final Object[] nodes = path.getPath();
    final BaseType rootType = ((BaseTypeTreeNode) nodes[1]).getBaseType();
    final List<TypeMember> memberPath = Lists.newArrayList();
    for (int i = 2; i < path.getPathCount(); ++i) {
      memberPath.add(((TypeMemberTreeNode) nodes[i]).getTypeMember());
    }
    return new TypeSelectionPath(rootType, memberPath);
  }

  @Override
  public TypesTreeModel getModel() {
    return (TypesTreeModel) super.getModel();
  }
}
