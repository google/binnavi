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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.BaseTypeTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeMemberTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeHelpers;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeHelpers.WalkResult;
import com.google.security.zynamics.binnavi.disassembly.types.TypeChangedListener;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainerListener;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitutionChangedListener;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Tree model responsible for showing the local and global variables of a tree.
 */
public class ViewReferencesTableModel extends DefaultTreeModel {

  /**
   * Map which holds the association between {@link BaseType base types} and their unique
   * {@link BaseTypeTreeNode node} representation.
   */
  private final Map<BaseType, BaseTypeTreeNode> baseTypeToTreeNode;

  /**
   * Map which holds the association between a {@link TypeMember type member} and all of their
   * {@link TypeMemberTreeNode node} representations.
   */
  private final HashMultimap<TypeMember, TypeMemberTreeNode> typeMemberToTreeNode;

  /**
   * The listener to get informed about changes in {@link TypeSubstitution type substitutions}.
   */
  private final TypeSubstitutionChangedListener typeSubstitutionChangedListener =
      new InternalTypeSubstitutionChangedListener();

  /**
   * The listener which is used to get informed about changes in {@link TypeInstanceReference type
   * instance reference}.
   */
  private final TypeInstanceContainerListener typeInstanceContainerListener =
      new InternalTypeInstanceContainerListener();

  /**
   * Listener which is used to get information about changed in {@link BaseType base types} and
   * {@link TypeMember type members}. It is used to update the nodes representing those types in the
   * tree.
   */
  private final TypeChangedListener typeChangedListener = new InternalTypeChangedListener();

  /**
   * The {@link INaviView view} to which this table model is connected.
   */
  private final INaviView view;

  /**
   * Listens to view changed events.
   */
  private final INaviViewListener viewListener = new InternalViewListener();

  /**
   * A multiple index container to make the access to the internal used structures explicit. It
   * makes sure that the types for the arguments are correct, and limits the exposed methods.
   */
  private class ViewReferenceMultiIndex {

    private final BiMap<INaviInstruction, InstructionNode> instructionNodes;
    private final BiMap<TypeInstanceReference, InstructionNode> typeInstanceReferences;
    private final BiMap<TypeSubstitution, InstructionNode> typeSubstitutions;
    private final HashMultimap<TypeInstance, TypeInstanceReference> typeInstances;

    public ViewReferenceMultiIndex() {
      instructionNodes = HashBiMap.<INaviInstruction, InstructionNode>create();
      typeInstanceReferences = HashBiMap.<TypeInstanceReference, InstructionNode>create();
      typeSubstitutions = HashBiMap.<TypeSubstitution, InstructionNode>create();
      typeInstances = HashMultimap.<TypeInstance, TypeInstanceReference>create();
    }

    public InstructionNode getInstructionNode(final TypeInstanceReference reference) {
      return typeInstanceReferences.get(reference);
    }

    public InstructionNode getInstructionNode(final TypeSubstitution typeSubstitution) {
      return typeSubstitutions.get(typeSubstitution);
    }

    public InstructionNode putTypeReference(final TypeInstanceReference reference,
        final INaviInstruction instruction) {
      final InstructionNode node = new InstructionNode(instruction, false);
      instructionNodes.put(instruction, node);
      typeInstanceReferences.put(reference, node);
      typeInstances.put(reference.getTypeInstance(), reference);
      return node;
    }

    public InstructionNode putTypeSubstitution(final TypeSubstitution substitution,
        final INaviInstruction instruction) {
      final InstructionNode node = new InstructionNode(instruction, true);
      instructionNodes.put(instruction, node);
      typeSubstitutions.put(substitution, node);
      return node;
    }

    public void removeInstructionNode(final InstructionNode node) {
      instructionNodes.inverse().remove(node);
      if (typeInstanceReferences.containsValue(node)) {
        final TypeInstanceReference reference = typeInstanceReferences.inverse().remove(node);
        typeInstances.remove(reference.getTypeInstance(), reference);
      }
      typeSubstitutions.inverse().remove(node);
    }

    public TypeSubstitution getTypeSubstitution(final InstructionNode node) {
      return typeSubstitutions.inverse().get(node);
    }

    public TypeInstanceReference getTypeInstanceReference(final InstructionNode node) {
      return typeInstanceReferences.inverse().get(node);
    }

    public Set<TypeInstanceReference> getTypeInstanceReferences(final TypeInstance instance) {
      return typeInstances.get(instance);
    }
  }

  private final ViewReferenceMultiIndex multiIndex;

  /**
   * Creates a new {@link ViewReferencesTableModel model} for the given {@link INaviView view}.
   *
   * @param view The {@link INaviView view} to which this {@link ViewReferencesTableModel model} is
   *        associated.
   */
  public ViewReferencesTableModel(final INaviView view) {
    super(new DefaultMutableTreeNode());
    this.view = Preconditions.checkNotNull(view, "Error: view argument can not be null.");
    multiIndex = new ViewReferenceMultiIndex();
    baseTypeToTreeNode = Maps.newHashMap();
    typeMemberToTreeNode = HashMultimap.<TypeMember, TypeMemberTreeNode>create();
    collectViewReferences();
    initializeListeners();
  }

  /**
   * Iterates the elements of a graph to find all {@link TypeSubstitution type substitutions} and
   * {@link TypeInstanceReference type instance references}. TODO(timkornau): ideal would be a cache
   * lookup for all instruction in the view without the need to iterate. One possible solution here
   * would be to use the already existing instruction cache.
   */
  private void collectViewReferences() {
    for (CCodeNode node : view.getContent().getBasicBlocks()) {
      for (INaviInstruction instruction : node.getInstructions()) {
        for (COperandTree operandTree : instruction.getOperands()) {
          for (INaviOperandTreeNode operandNode : operandTree.getNodes()) {
            if (operandNode.getTypeSubstitution() != null) {
              addTypeSubstitutionToTree(operandNode, instruction);
            }
            if (operandNode.getTypeInstanceReferences() != null) {
              addTypeInstancesToTree(operandNode.getTypeInstanceReferences(), instruction);
            }
          }
        }
      }
    }
  }

  public TypeSubstitution getTypeSubstitution(final InstructionNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null.");
    return multiIndex.getTypeSubstitution(node);
  }

  public TypeInstanceReference getTypeInstanceReference(final InstructionNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null.");
    return multiIndex.getTypeInstanceReference(node);
  }

  /**
   * Adds a {@link TypeSubstitution type substitution} for the given {@link INaviInstruction
   * instruction} to the tree.
   *
   * @param operandNode The {@link INaviOperandTreeNode operand node} which holds the
   *        {@link TypeSubstitution type substitution}.
   * @param instruction The {@link INaviInstruction instruction} which holds the
   *        {@link INaviOperandTreeNode operand node}.
   */
  private void addTypeSubstitutionToTree(final INaviOperandTreeNode operandNode,
      final INaviInstruction instruction) {
    final TypeSubstitution typeSubstitution = operandNode.getTypeSubstitution();
    final BaseTypeCategory category = typeSubstitution.getBaseType().getCategory();
    if (category == BaseTypeCategory.STRUCT || category == BaseTypeCategory.ARRAY
        || category == BaseTypeCategory.UNION) {
      addCompoundTypeSubstitutionToTree(operandNode, instruction, typeSubstitution);
    } else {
      addBaseType(typeSubstitution.getBaseType());
      BaseTypeTreeNode currentNode = baseTypeToTreeNode.get(typeSubstitution.getBaseType());
      insertNodeInto(multiIndex.putTypeSubstitution(typeSubstitution, instruction), currentNode,
          currentNode.getChildCount());
    }
  }

  /**
   * Adds a {@link TypeSubstitution type substitution} for the given {@link INaviInstruction
   * instruction} to the tree. The types passed to this function need to be compund types.
   *
   * @param operandNode The {@link INaviOperandTreeNode operand node} which holds the
   *        {@link TypeSubstitution type substitution}.
   * @param instruction The {@link INaviInstruction instruction} which holds the
   *        {@link INaviOperandTreeNode operand node}.
   * @param typeSubstitution A {@link TypeSubstitution type substitution} which is a compound type.
   *        Must be of a compound {@link BaseTypeCategory base type category}.
   */
  private void addCompoundTypeSubstitutionToTree(final INaviOperandTreeNode operandNode,
      final INaviInstruction instruction, final TypeSubstitution typeSubstitution) {
    final long completeOffset = operandNode.hasAddendSibling() ? (operandNode.determineAddendValue()
        * 8 + typeSubstitution.getOffset())
        : typeSubstitution.getOffset();

    final WalkResult walkResult =
        BaseTypeHelpers.findMember(typeSubstitution.getBaseType(), completeOffset);
    if (!walkResult.isValid()) {
      return;
    }

    addBaseType(typeSubstitution.getBaseType());

    DefaultMutableTreeNode currentNode = baseTypeToTreeNode.get(typeSubstitution.getBaseType());
    for (TypeMember typeMember : walkResult.getPath()) {
      TypeMemberTreeNode nextNode = checkTypeMemberNodeExists(typeMember, currentNode);
      if (nextNode == null) {
        nextNode = new TypeMemberTreeNode(typeMember);
        typeMemberToTreeNode.put(typeMember, nextNode);
        insertNodeInto(nextNode, currentNode, currentNode.getChildCount());
      }
      currentNode = nextNode;
    }
    insertNodeInto(multiIndex.putTypeSubstitution(typeSubstitution, instruction), currentNode,
        currentNode.getChildCount());
  }

  /**
   * Adds a {@link BaseType base type} node to the tree and the internal storage if it is not
   * already present.
   *
   * @param baseType The {@link BaseType base type} to be added to the tree.
   */
  private void addBaseType(final BaseType baseType) {
    if (!baseTypeToTreeNode.containsKey(baseType)) {
      final BaseTypeTreeNode baseTypeNode = new BaseTypeTreeNode(baseType);
      baseTypeToTreeNode.put(baseType, baseTypeNode);
      insertNodeInto(baseTypeNode, (DefaultMutableTreeNode) getRoot(),
          ((DefaultMutableTreeNode) getRoot()).getChildCount());
      // TODO(timkornau): This reload is needed for the situation where the tree was initially empty
      // on view open and a new type substitution gets added. This needs to be investigated further
      // as it is unclear why this behavior is triggered at all.
      reload((DefaultMutableTreeNode) getRoot());
    }
  }

  /**
   * Check if a {@link TypeMember type member} is already a child of the given
   * {@link DefaultMutableTreeNode node}.
   *
   * @param typeMember The {@link TypeMember} to check.
   * @param currentNode The {@link DefaultMutableTreeNode} where to search.
   * @return The node to which the {@link TypeMember type member} belongs or null.
   */
  private TypeMemberTreeNode checkTypeMemberNodeExists(final TypeMember typeMember,
      final DefaultMutableTreeNode currentNode) {
    for (int i = 0; i < currentNode.getChildCount(); i++) {
      if (currentNode.getChildAt(i) instanceof TypeMemberTreeNode) {
        if (((TypeMemberTreeNode) currentNode.getChildAt(i)).getTypeMember().equals(typeMember)) {
          return (TypeMemberTreeNode) currentNode.getChildAt(i);
        }
      }
    }
    return null;
  }

  /**
   * Adds the list of {@link TypeInstanceReference type instance references} for the given
   * {@link INaviInstruction instruction} to the tree.
   *
   * @param typeInstanceReferences A List of {@link TypeInstanceReference type instance references}.
   * @param instruction The {@link INaviInstruction instruction} to which the list of references
   *        belongs.
   */
  private void addTypeInstancesToTree(final List<TypeInstanceReference> typeInstanceReferences,
      final INaviInstruction instruction) {
    for (TypeInstanceReference typeInstanceReference : typeInstanceReferences) {
      final BaseType baseType = typeInstanceReference.getTypeInstance().getBaseType();
      addBaseType(baseType);
      insertNodeInto(multiIndex.putTypeReference(typeInstanceReference, instruction),
          baseTypeToTreeNode.get(baseType), baseTypeToTreeNode.get(baseType).getChildCount());
    }
  }

  private void initializeListeners() {
    view.addListener(viewListener);
    view.getConfiguration().getModule().getTypeManager()
        .addListener(typeSubstitutionChangedListener);
    view.getConfiguration().getModule().getTypeManager().addListener(typeChangedListener);
    view
        .getConfiguration()
        .getModule()
        .getContent()
        .getTypeInstanceContainer()
        .addListener(typeInstanceContainerListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    view
        .getConfiguration()
        .getModule()
        .getContent()
        .getTypeInstanceContainer()
        .removeListener(typeInstanceContainerListener);
    view.getConfiguration().getModule().getTypeManager().removeListener(typeChangedListener);
    view.getConfiguration().getModule().getTypeManager()
        .removeListener(typeSubstitutionChangedListener);
    view.removeListener(viewListener);
  }

  // TODO(timkornau): refactor when there is a more efficient way of locating an instruction by
  // address in a view.
  private INaviInstruction findInstruction(final IAddress address) {
    for (CCodeNode codeNode : view.getBasicBlocks()) {
      for (INaviInstruction instruction : codeNode.getInstructions()) {
        if (instruction.getAddress().equals(address)) {
          return instruction;
        }
      }
    }
    return null;
  }

  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void closedView(final INaviView view,
        final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
      dispose();
    }
  }

  /**
   * Class to keep track of changes in type instances.
   */
  private class InternalTypeInstanceContainerListener implements TypeInstanceContainerListener {

    private boolean needsRefresh(final TypeInstanceReference reference) {
      return reference.getView() == view;
    }

    @Override
    public void addedTypeInstance(TypeInstance instance) {
      /* This does not matter to us as long as the reference does not change. */
    }

    @Override
    public void addedTypeInstanceReference(TypeInstanceReference reference) {
      if (needsRefresh(reference)) {
        final INaviInstruction instruction = findInstruction(reference.getAddress());
        addTypeInstancesToTree(Lists.newArrayList(reference), instruction);
      }
    }

    @Override
    public void changedTypeInstance(TypeInstance instance) {
      for (TypeInstanceReference reference : multiIndex.getTypeInstanceReferences(instance)) {
        nodeChanged(multiIndex.getInstructionNode(reference));
      }
    }

    @Override
    public void changedTypeInstanceReference(TypeInstanceReference reference) {
      if (needsRefresh(reference)) {
        nodeChanged(multiIndex.getInstructionNode(reference));
      }
    }

    @Override
    public void removedTypeInstance(TypeInstance instance) {
      /* This does not matter to us as long as the reference does not change. */
    }

    @Override
    public void removedTypeInstanceReference(TypeInstanceReference reference) {
      if (needsRefresh(reference)) {
        removeFromTree(multiIndex.getInstructionNode(reference));
      }
    }
  }

  /**
   * Given a {@link DefaultMutableTreeNode node} removes the node from the tree. Also it removes all
   * parent nodes which after the removal of the child have no other children any more.
   *
   * @param node The {@link DefaultMutableTreeNode node} which to remove from the tree.
   */
  private void removeFromTree(DefaultMutableTreeNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null.");
    Preconditions.checkArgument(!node.isRoot(), "Error: node argument can not be the root node.");
    final Set<DefaultMutableTreeNode> nodesToDelete = Sets.newHashSet();
    nodesToDelete.add(node);
    while (node.getSiblingCount() == 1 && node.getParent() != getRoot()) {
      node = (DefaultMutableTreeNode) node.getParent();
      nodesToDelete.add(node);
    }

    for (DefaultMutableTreeNode currentNode : nodesToDelete) {
      removeNodeFromParent(currentNode);
      if (currentNode instanceof InstructionNode) {
        multiIndex.removeInstructionNode((InstructionNode) currentNode);
      }
      if (currentNode instanceof BaseTypeTreeNode) {
        baseTypeToTreeNode.remove(((BaseTypeTreeNode) currentNode).getBaseType());
      }
      if (currentNode instanceof TypeMemberTreeNode) {
        typeMemberToTreeNode.remove(((TypeMemberTreeNode) currentNode).getTypeMember(),
            currentNode);
      }
    }
  }

  /**
   * Class to keep track of changes in base types.
   */
  private class InternalTypeSubstitutionChangedListener implements TypeSubstitutionChangedListener {

    private void removeSubstitutions(final Set<TypeSubstitution> substitutions) {
      // The copy here is done to decouple the result of the intersection from the
      // iteration (concurrent modification exception).
      for (TypeSubstitution typeSubstitution : ImmutableSet.<TypeSubstitution>copyOf(
          (Sets.intersection(multiIndex.typeSubstitutions.keySet(), substitutions)))) {
        removeFromTree(multiIndex.getInstructionNode(typeSubstitution));
      }
    }

    private void addSubstitutions(final Set<TypeSubstitution> substitutions) {
      for (TypeSubstitution typeSubstitution : substitutions) {
        final INaviInstruction instruction = findInstruction(typeSubstitution.getAddress());
        if (instruction != null) {
          addTypeSubstitutionToTree(typeSubstitution.getOperandTreeNode(), instruction);
        }
      }
    }

    @Override
    public void substitutionsDeleted(final Set<TypeSubstitution> deletedSubstitutions) {
      removeSubstitutions(deletedSubstitutions);
    }

    @Override
    public void substitutionsChanged(final Set<TypeSubstitution> changedSubstitutions) {
      removeSubstitutions(changedSubstitutions);
      addSubstitutions(changedSubstitutions);
    }

    @Override
    public void substitutionsAdded(final Set<TypeSubstitution> addedSubstitutions) {
      addSubstitutions(addedSubstitutions);
    }
  }

  /**
   * Class to keep track of updates done to either {@link BaseType base types} or to
   * {@link TypeMember type members}. We only care for updates here as adding / deleting / moving of
   * members and base types are actions which can not happen by themselves in this table model. They
   * are handled via the {@link InternalTypeSubstitutionChangedListener type substitution changed
   * listener}.
   */
  private class InternalTypeChangedListener implements TypeChangedListener {

    @Override
    public void memberAdded(TypeMember member) {}

    @Override
    public void memberDeleted(TypeMember member) {}

    @Override
    public void membersMoved(Set<BaseType> affectedTypes) {}

    @Override
    public void memberUpdated(TypeMember member) {
      for (TypeMemberTreeNode node : typeMemberToTreeNode.get(member)) {
        nodeChanged(node);
      }
    }

    @Override
    public void typeAdded(BaseType baseType) {}

    @Override
    public void typeDeleted(BaseType deletedType) {}

    @Override
    public void typesUpdated(Set<BaseType> baseTypes) {
      for (BaseType baseType : Sets.intersection(baseTypeToTreeNode.keySet(), baseTypes)) {
        nodeChanged(baseTypeToTreeNode.get(baseType));
      }
    }
  }
}
