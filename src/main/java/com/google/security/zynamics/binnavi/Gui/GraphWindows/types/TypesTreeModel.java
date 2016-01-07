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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.TypeChangedListener;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * The model of the types editor component. The model initializes itself by adding all nodes from
 * the given type manager according to the corresponding filter.
 *
 * This model is shared across JTrees in different views of a single module.
 */
public class TypesTreeModel extends DefaultTreeModel {

  /**
   * Holds a base type tree node and an index corresponding to the position where this tree node was
   * inserted as a child.
   */
  private class ChildNodeDescriptor {
    private final int index;
    private final BaseTypeTreeNode node;

    // Indicates an invalid index and a non-existing tree node.
    public ChildNodeDescriptor() {
      index = -1;
      node = null;
    }

    public ChildNodeDescriptor(final BaseTypeTreeNode node, final int index) {
      this.node = node;
      this.index = index;
    }

    public BaseTypeTreeNode getNode() {
      return node;
    }

    public int getIndex() {
      return index;
    }
  }

  private static int compareBaseTypeStrings(final String lhs, final String rhs) {
    return lhs.toLowerCase().compareTo(rhs.toLowerCase());
  }

  // Note that a single member can have multiple associated nodes in the tree if the member is
  // (implicitly) nested within other compound types.
  private final HashMultimap<TypeMember, TypeMemberTreeNode> memberNodes = HashMultimap.create();
  // Maps compound base types to the corresponding member nodes that are nested inside top level
  // compound base types. This map is required to find all parent struct nodes when adding a
  // member to a struct.
  private final HashMultimap<BaseType, TypeMemberTreeNode> nestedStructNodes =
      HashMultimap.create();
  private final InternalTypeListener listener = new InternalTypeListener();

  // Controls which base types should be filtered and not included in the tree model.
  private final TypesFilter filter;

  // Creates an instance without any types.
  private TypesTreeModel() {
    super(null);
    filter = new DefaultTypesFilter();
  }

  public TypesTreeModel(final TypeManager typeManager, final TypesFilter filter) {
    // Since we need multiple root nodes for all base types, we have to create them as child nodes
    // of an invisible root node.
    super(new DefaultMutableTreeNode("invisible_root"));
    this.filter = filter;
    createNodes(typeManager);
    typeManager.addListener(listener);
  }

  /**
   * Creates a new instance of the types tree model that contains all existing types besides the
   * stack frame types in the corresponding type manager.
   */
  public static TypesTreeModel createDefaultModel(final TypeManager typeManager) {
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    return new TypesTreeModel(typeManager, new DefaultTypesFilter());
  }

  /**
   * Creates a new instance of the types tree model that doesn't contain any nodes.
   */
  public static TypesTreeModel createEmptyTypeModel() {
    return new TypesTreeModel();
  }

  /**
   * Creates a new instance of the types tree model that only includes a single base type.
   */
  public static TypesTreeModel createSingleTypeModel(final TypeManager typeManager,
      final BaseType includedType) {
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    Preconditions.checkNotNull(includedType, "Error: includedType argument can not be null");
    return new TypesTreeModel(typeManager, new StackFrameTypesFilter(includedType, typeManager));
  }

  private TreeNode createNodes(final TypeManager typeManager) {
    for (final BaseType baseType : typeManager.getTypes()) {
      if (filter.includeType(baseType)) {
        insertBaseType(baseType);
      }
    }
    return root;
  }

  // Recursively creates all tree nodes for the given base type instance.
  private void createTypeNodes(final DefaultMutableTreeNode currentNode, final BaseType baseType) {
    if (baseType.getCategory() == BaseTypeCategory.ARRAY) {
      // Array types are special in the sense that the nested member that represents the array
      // elements should not have corresponding nodes in the tree model. Thus, we simply return here
      // without creating that member node.
      return;
    } else {
      for (final TypeMember member : baseType) {
        switch (member.getBaseType().getCategory()) {
          case ARRAY:
          case ATOMIC:
          case POINTER:
            final TypeMemberTreeNode memberNode = new TypeMemberTreeNode(member);
            currentNode.add(memberNode);
            memberNodes.put(member, memberNode);
            break;
          case FUNCTION_PROTOTYPE:
            break;
          case STRUCT:
          case UNION:
            // This member has a base type that itself has multiple members: we need to go deeper!
            final TypeMemberTreeNode nestedNode = new TypeMemberTreeNode(member);
            memberNodes.put(member, nestedNode);
            nestedStructNodes.put(member.getBaseType(), nestedNode);
            currentNode.add(nestedNode);
            createTypeNodes(nestedNode, member.getBaseType());
            break;
          default:
            NaviLogger.warning("Unknown type category: %d", member.getBaseType().getCategory());
            break;
        }
      }
    }
  }

  private ChildNodeDescriptor insertBaseType(final BaseType baseType) {
    return insertType((DefaultMutableTreeNode) root, baseType,
        findInsertIndex(baseType, (DefaultMutableTreeNode) root));
  }

  /**
   * Removes a list of given nodes from the model and updates node maps.
   */
  private void removeNodes(final List<DefaultMutableTreeNode> nodes) {
    for (final DefaultMutableTreeNode node : nodes) {
      if (node instanceof TypeMemberTreeNode) {
        final TypeMember member = ((TypeMemberTreeNode) node).getTypeMember();
        nestedStructNodes.get(member.getBaseType()).removeAll(memberNodes.get(member));
        memberNodes.remove(member, node);
      }
      node.removeFromParent();
    }
  }

  private ChildNodeDescriptor deleteBaseType(final BaseType baseType) {
    final ChildNodeDescriptor descriptor = getBaseTypeTreeNode(baseType);
    removeNodes(Lists.<DefaultMutableTreeNode>newArrayList(descriptor.getNode()));
    return descriptor;
  }

  // Returns the child index of the node corresponding to the given base type, otherwise returns
  // (-insertion_index). Whereas insertion_index is defined as the point at which the node would
  // be inserted according to the sort order.
  // Since Swing doesn't offer a sortable tree model, we had to roll our own.
  private static int findTreeNodeIndex(final BaseType baseType, final DefaultMutableTreeNode root) {
    int lower = 0;
    if (root.getChildCount() == 0) {
      return 0;
    }
    int upper = root.getChildCount() - 1;
    final String baseTypeString = BaseTypeTreeNode.renderBaseType(baseType);
    while (upper >= lower) {
      final int middle = (upper + lower) >>> 1;
      final int comparison =
          compareBaseTypeStrings(baseTypeString, root.getChildAt(middle).toString());
      if (comparison < 0) {
        upper = middle - 1;
      } else if (comparison > 0) {
        lower = middle + 1;
      } else {
        return middle;
      }
    }
    return -lower;
  }

  private static int findInsertIndex(final BaseType baseType, final DefaultMutableTreeNode root) {
    final int index = findTreeNodeIndex(baseType, root);
    return index < 0 ? -index : index;
  }

  private ChildNodeDescriptor getBaseTypeTreeNode(final BaseType baseType) {
    final int index = findTreeNodeIndex(baseType, (DefaultMutableTreeNode) root);
    if (index < 0 || index >= root.getChildCount()) {
      return new ChildNodeDescriptor();
    }
    final BaseTypeTreeNode node =
        (BaseTypeTreeNode) ((DefaultMutableTreeNode) root).getChildAt(index);
    return new ChildNodeDescriptor(node, index);
  }

  /**
   * Creates a new base type tree node below the given parent node and recursively creates all
   * member nodes.
   */
  private ChildNodeDescriptor insertType(final DefaultMutableTreeNode parentNode,
      final BaseType baseType, final int index) {
    final BaseTypeTreeNode newNode = new BaseTypeTreeNode(baseType);
    parentNode.insert(newNode, index);
    createTypeNodes(newNode, baseType);
    return new ChildNodeDescriptor(newNode, index);
  }

  /**
   * React on changes in the type manager and modify the tree model accordingly.
   *
   *  Note that we need to add/delete/update the tree nodes in this listener since another client
   * could add types to the type manager (e.g. a script) but those types should be displayed in the
   * type editor as well.
   */
  private class InternalTypeListener implements TypeChangedListener {

    private void addMemberNodes(final TypeMember member) {
      final int index = determineInsertIndex(member);
      // Add member to all nested struct nodes.
      final BaseType parentType = member.getParentType();
      final Set<TreeNode> parentsToUpdate = new HashSet<TreeNode>();
      if (nestedStructNodes.containsKey(parentType)) {
        for (final TypeMemberTreeNode node : nestedStructNodes.get(parentType)) {
          insertMemberAt(member, node, index);
          parentsToUpdate.add(node);
        }
      }
      // Add member to the top level base type node (if existing).
      // If this is a listener for a filtered type editor model (e.g. a stack frame tree) it is
      // possible that we only have nested base type nodes, but no corresponding top level nodes
      // (besides the stackframe itself).
      final ChildNodeDescriptor descriptor = getBaseTypeTreeNode(parentType);
      if (descriptor.getNode() != null) {
        insertMemberAt(member, descriptor.getNode(), index);
        parentsToUpdate.add(descriptor.getNode());
      }
      for (TreeNode parent : parentsToUpdate) {
        nodeStructureChanged(parent);
      }
    }

    private List<DefaultMutableTreeNode> collectSubtreeNodes(final DefaultMutableTreeNode node) {
      final List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
      for (final Enumeration<?> e = node.breadthFirstEnumeration(); e.hasMoreElements();) {
        nodes.add((DefaultMutableTreeNode) e.nextElement());
      }
      return nodes;
    }

    private void deleteMemberNodes(final TypeMember member) {
      nestedStructNodes.get(member.getBaseType()).removeAll(memberNodes.get(member));
      final Set<TreeNode> parentsToUpdate = new HashSet<TreeNode>();
      for (final TypeMemberTreeNode node : memberNodes.get(member)) {
        final MutableTreeNode parent = (MutableTreeNode) node.getParent();
        parentsToUpdate.add(parent);
        parent.remove(node);
      }
      for (final TreeNode parent : parentsToUpdate) {
        nodeStructureChanged(parent);
      }
      memberNodes.removeAll(member);
    }

    private int determineInsertIndex(final TypeMember member) {
      if (!member.getBitOffset().isPresent()) {
        return 0;
      }
      int index = 0;
      for (final TypeMember currentMember : member.getParentType()) {
        if (currentMember.getBitOffset().get() < member.getBitOffset().get()) {
          index++;
        }
      }
      return index;
    }

    /**
     * Inserts member as a child of parentNode at the given index without raising any events
     * regarding the updates model!
     */
    private void insertMemberAt(final TypeMember member, final DefaultMutableTreeNode parentNode,
        final int index) {
      final TypeMemberTreeNode memberNode = new TypeMemberTreeNode(member);
      parentNode.insert(memberNode, index);
      createTypeNodes(memberNode, member.getBaseType());
      nestedStructNodes.put(member.getBaseType(), memberNode);
      memberNodes.put(member, memberNode);
    }

    @Override
    public void memberAdded(final TypeMember member) {
      if (!filter.includeUpdatedType(member.getParentType())) {
        return;
      }
      addMemberNodes(member);
    }

    @Override
    public void memberDeleted(final TypeMember member) {
      if (!filter.includeUpdatedType(member.getParentType())) {
        return;
      }
      deleteMemberNodes(member);
    }

    /**
     * Modify the tree model according to the changes in the type system due to the member movement.
     * We need to find all corresponding (nested) nodes, delete and re-create them.
     */
    @Override
    public void membersMoved(final Set<BaseType> affectedTypes) {
      // Note: we need to queue modifications to the model in the dispatcher thread, otherwise we
      // interfere with the not yet completed drag and drop operation.
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          // Perform the actual member movement.
          for (final BaseType baseType : affectedTypes) {
            if (!filter.includeType(baseType)) {
              continue;
            }
            // 1) Determine nodes that need to be re-created due to the member move and remove them.
            final List<DefaultMutableTreeNode> oldNodes =
                collectSubtreeNodes(getBaseTypeTreeNode(baseType).getNode());
            removeNodes(oldNodes);
            // 2) Re-create the base type node and all child nodes.
            nodeStructureChanged(insertBaseType(baseType).getNode());
          }
        }
      });
    }

    @Override
    public void memberUpdated(final TypeMember member) {
      final BaseType parentType = member.getParentType();
      if (!filter.includeUpdatedType(parentType)) {
        return;
      }
      deleteMemberNodes(member);
      addMemberNodes(member);
    }

    @Override
    public void typeAdded(final BaseType baseType) {
      if (filter.includeType(baseType)) {
        final ChildNodeDescriptor insertResult = insertBaseType(baseType);
        nodesWereInserted(root, new int[] {insertResult.getIndex()});
      }
    }

    @Override
    public void typeDeleted(final BaseType deletedType) {
      if (filter.includeType(deletedType)) {
        final ChildNodeDescriptor descriptor = deleteBaseType(deletedType);
        nodesWereRemoved(root, new int[] {descriptor.getIndex()},
            new BaseTypeTreeNode[] {descriptor.getNode()});
      }
    }

    @Override
    public void typesUpdated(final Set<BaseType> baseTypes) {
      for (final BaseType baseType : baseTypes) {
        if (filter.includeType(baseType)) {
          // Since we cannot change the index of an already existing node, we have to delete and
          // re-insert it.
          deleteBaseType(baseType);
          ChildNodeDescriptor descriptor = insertBaseType(baseType);
          nodeStructureChanged(descriptor.node);
        }
      }
    }
  }
}
