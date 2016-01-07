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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainerListener;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single operand expression in the operand tree of a single operand of an instruction.
 */
public final class COperandTreeNode implements INaviOperandTreeNode {
  private int id;

  private COperandTreeNode parent;

  private final List<COperandTreeNode> children = new ArrayList<COperandTreeNode>();
  private String expressionValue;
  private ExpressionType expressionType;
  private final INaviReplacement replacement;
  /** List of outgoing code/data references. */
  private final List<IReference> references;
  private final SQLProvider provider;
  private INaviOperandTree operandTree;
  private final ListenerProvider<INaviOperandTreeNodeListener> listeners =
      new ListenerProvider<INaviOperandTreeNodeListener>();
  private final INaviReplacementListener listener = new InternalReplacementListener();
  private OperandDisplayStyle displayStyle = OperandDisplayStyle.UNSIGNED_HEXADECIMAL;
  private TypeSubstitution substitution;
  /* We initialize the reference list lazily in order to save memory. */
  private List<TypeInstanceReference> instanceReferences = null;

  private final TypeInstanceContainerListener typeInstanceContainerListener =
      new InternalTypeInstanceContainerListener();

  /**
   * Creates a new operand tree node object.
   *
   * @param nodeId ID of the operand tree node.
   * @param type The type of the expression. This value must be one of the expression types defined
   *        in IOperandTree.
   * @param value The value of the operand expression.
   * @param replacement The replacement string of the operand expression.
   * @param references List of outgoing references of that operand tree node.
   * @param provider Synchronizes the operand tree node with the database.
   */
  private COperandTreeNode(final int nodeId, final ExpressionType type, final String value,
      final INaviReplacement replacement, final List<IReference> references,
      final SQLProvider provider) {
    expressionType = Preconditions.checkNotNull(type, "IE00182: Type argument can not be null");
    expressionValue = Preconditions.checkNotNull(value, "IE00183: Value argument can not be null");
    this.references = new ArrayList<IReference>(
        Preconditions.checkNotNull(references, "IE00184: References argument can not be null"));
    this.provider =
        Preconditions.checkNotNull(provider, "IE00185: Provider argument can not be null");
    id = nodeId;
    this.replacement = replacement;
    if (replacement != null) {
      replacement.addListener(listener);
      displayStyle = OperandDisplayStyle.OFFSET;
    }
  }

  /**
   * Creates a new operand tree node object.
   *
   * @param nodeId ID of the operand tree node.
   * @param type The type of the expression. This value must be one of the expression types defined
   *        in IOperandTree.
   * @param value The value of the operand expression.
   * @param replacement The replacement string of the operand expression.
   * @param references List of outgoing references of that operand tree node.
   * @param provider Synchronizes the operand tree node with the database.
   */
  public COperandTreeNode(final int nodeId, final int type, final String value,
      final INaviReplacement replacement, final List<IReference> references,
      final SQLProvider provider, final TypeManager typeManager,
      final TypeInstanceContainer instanceContainer) {
    this.provider =
        Preconditions.checkNotNull(provider, "IE02212: Provider argument can not be null");
    Preconditions.checkNotNull(value, "IE00214: Value can not be null");
    this.references = new ArrayList<IReference>(
        Preconditions.checkNotNull(references, "IE02211: References argument can not be null"));
    id = nodeId;
    for (final IReference reference : references) {
      Preconditions.checkNotNull(reference, "IE00215: Invalid reference in reference list");
    }
    Preconditions.checkNotNull(typeManager, "Type manager can not be null.");
    Preconditions.checkNotNull(instanceContainer, "Type instance container can not be null");

    initValue(type, value);

    if (expressionValue == null) {
      throw new IllegalArgumentException(
          String.format("IE00216: Unknown operand value '%s'", value));
    }

    this.replacement = replacement;
    if (replacement != null) {
      replacement.addListener(listener);
      displayStyle = OperandDisplayStyle.OFFSET;
    }
    instanceContainer.addListener(typeInstanceContainerListener);
  }

  /**
   * Determines the sibling node of the given node that is part of a two component sum as described
   * in {@link COperandTreeNode#determineAddendValue}.
   *
   * @param node The node for which to determine the addend sibling.
   * @return The addend sibling or null if no such node exists.
   */
  private static INaviOperandTreeNode determineAddendSibling(final INaviOperandTreeNode node) {
    final INaviOperandTreeNode parent = node.getParent();
    if (parent == null || parent.getChildren().size() != 2 || !parent.getValue().equals("+")) {
      return null;
    }
    final INaviOperandTreeNode firstChild = parent.getChildren().get(0);
    final INaviOperandTreeNode secondChild = parent.getChildren().get(1);
    // One node must be an immediate integer in the pair to be a valid addend.
    if (firstChild.getType().equals(ExpressionType.IMMEDIATE_INTEGER)
        ^ secondChild.getType().equals(ExpressionType.IMMEDIATE_INTEGER)) {
      return firstChild == node ? secondChild : firstChild;
    }
    return null;
  }

  /**
   * Links two operand tree nodes.
   *
   * @param parent The parent node.
   * @param child The child node that is added to the parent.
   */
  public static void link(final COperandTreeNode parent, final COperandTreeNode child) {
    Preconditions.checkNotNull(child, "IE00218: Child argument can not be null");
    Preconditions.checkNotNull(parent, "IE00217: Parent argument can not be null");
    parent.children.add(child);
    child.parent = parent;
  }

  /**
   * Takes the type of an operand expression and its value and creates the real string that is
   * displayed for that operand expression.
   *
   * @param type The type of the operand expression.
   * @param value The value of the operand expression.
   */
  private void initValue(final int type, final String value) {
    switch (type) {
      case IOperandTree.NODE_TYPE_SYMBOL_ID:
        expressionType = ExpressionType.SYMBOL;
        expressionValue = value;
        break;
      case IOperandTree.NODE_TYPE_IMMEDIATE_INT_ID:
        expressionType = ExpressionType.IMMEDIATE_INTEGER;
        expressionValue = value;
        break;
      case IOperandTree.NODE_TYPE_IMMEDIATE_FLOAT_ID:
        expressionType = ExpressionType.IMMEDIATE_FLOAT;
        expressionValue = value;
        break;
      case IOperandTree.NODE_TYPE_OPERATOR_ID:
        if (value.equals("{")) {
          expressionType = ExpressionType.EXPRESSION_LIST;
          expressionValue = value;
        } else {
          expressionType = ExpressionType.OPERATOR;
          expressionValue = value;
        }
        break;
      case IOperandTree.NODE_TYPE_REGISTER_ID:
        expressionType = ExpressionType.REGISTER;
        expressionValue = value;
        break;
      case IOperandTree.NODE_TYPE_SIZE_PREFIX_ID:
        if (value.equals("b1")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "byte";
        } else if (value.equals("b2")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "word";
        } else if (value.equals("b4") || value.equals("dword")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "dword";
        } else if (value.equals("b6")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "fword";
        } else if (value.equals("b8")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "qword";
        } else if (value.equals("b10")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "double";
        } else if (value.equals("b16")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "oword";
        } else if (value.equals("b_var")) {
          expressionType = ExpressionType.SIZE_PREFIX;
          expressionValue = "b_var";
        }
        break;
      case IOperandTree.NODE_TYPE_DEREFERENCE_ID:
        expressionType = ExpressionType.MEMDEREF;
        expressionValue = value;
        break;
      default:
        throw new IllegalStateException(
            String.format("IE00219: Unknown node type (%d : %s)", type, value));
    }
  }

  protected void setOperand(final INaviOperandTree operandTree) {
    Preconditions.checkArgument(
        this.operandTree == null, "IE00220: Operand tree was already initialized");
    this.operandTree = operandTree;
  }

  @Override
  public void addInstanceReference(final TypeInstanceReference reference) {
    if (instanceReferences == null) {
      instanceReferences = new ArrayList<TypeInstanceReference>();
    }
    instanceReferences.add(reference);
  }

  @Override
  public void addListener(final INaviOperandTreeNodeListener listener) {
    listeners.addListener(listener);
  }

  @Override
  public void addReference(final IReference reference) throws CouldntSaveDataException {
    Preconditions.checkNotNull(reference, "IE00221: Reference argument can not be null");
    Preconditions.checkArgument(
        !references.contains(reference), "IE00222: Reference can not be added twice");
    provider.addReference(this, reference.getTarget(), reference.getType());
    references.add(reference);

    for (final INaviOperandTreeNodeListener listener : listeners) {
      try {
        listener.addedReference(this, reference);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  // TODO(jannewger): this method doesn't seem to be correct: the list of references isn't cloned
  // so the clone as well as the original COperandTreeNode refer to the same list of references.
  // Also, it is a mutable field which means that changes in the original instance are reflected to
  // the clone, and vice versa. The same holds true for TypeSubstitutions: we should discuss whether
  // this is desired behavior. It clearly breaks the strict "clone contract".
  public COperandTreeNode cloneNode() {
    final COperandTreeNode clonedNode = new COperandTreeNode(id, expressionType, expressionValue,
        replacement == null ? null : replacement.cloneReplacement(), references, provider);

    for (final COperandTreeNode child : children) {
      COperandTreeNode.link(clonedNode, child.cloneNode());
    }

    return clonedNode;
  }

  @Override
  public void close() {
    if (replacement != null) {
      replacement.close();
      replacement.removeListener(listener);
    }
    if (substitution != null) {
      operandTree.getInstruction()
          .getModule().getTypeManager().removeTypeSubstitutionInstance(substitution);
    }
    if (instanceReferences != null && !instanceReferences.isEmpty()) {
      final TypeInstanceContainer container =
          operandTree.getInstruction().getModule().getContent().getTypeInstanceContainer();
      for (final TypeInstanceReference reference : instanceReferences) {
        container.deactivateTypeInstanceReference(reference);
      }
    }
  }

  @Override
  public void deleteReference(final IReference reference) throws CouldntDeleteException {
    Preconditions.checkNotNull(reference, "IE00223: Reference argument can not be null");
    Preconditions.checkArgument(
        references.contains(reference), "IE00224: No such reference at this node");
    provider.deleteReference(this, reference.getTarget(), reference.getType());

    references.remove(reference);

    for (final INaviOperandTreeNodeListener listener : listeners) {
      try {
        listener.removedReference(this, reference);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public long determineAddendValue() {
    final INaviOperandTreeNode sibling = Preconditions.checkNotNull(
        determineAddendSibling(this), "Error: operand expression is not a two component sum.");
    // TODO(jannewger): there should be a sane way to determine the architecture of an instruction
    // (e.g. via an enum).
    if (sibling.getOperand().getInstruction().getArchitecture().equalsIgnoreCase("x86-64")) {
      return new BigInteger(sibling.getValue()).longValue();
    }
    // If the default assumption of a 32-bit architecture doesn't hold we'll get an exception here.
    // However, since the architecture might be unknown anyway we have no better way than to fail
    // here.
    return (int) Long.parseLong(sibling.getValue());
  }

  @Override
  public List<INaviOperandTreeNode> getChildren() {
    return new ArrayList<INaviOperandTreeNode>(children);
  }

  @Override
  public OperandDisplayStyle getDisplayStyle() {
    return displayStyle;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public IAddress getInstructionAddress() {
    return getOperand().getInstruction().getAddress();
  }

  @Override
  public INaviOperandTree getOperand() {
    return operandTree;
  }

  @Override
  public int getOperandPosition() {
    return getOperand().getInstruction().getOperandPosition(getOperand());
  }

  @Override
  public INaviOperandTreeNode getParent() {
    return parent;
  }

  @Override
  public List<IReference> getReferences() {
    return references;
  }

  @Override
  public INaviReplacement getReplacement() {
    return replacement;
  }

  @Override
  public ExpressionType getType() {
    return expressionType;
  }

  @Override
  public List<TypeInstanceReference> getTypeInstanceReferences() {
    if (instanceReferences == null) {
      return new ArrayList<TypeInstanceReference>();
    } else {
      return instanceReferences;
    }
  }

  @Override
  public TypeSubstitution getTypeSubstitution() {
    return substitution;
  }

  /**
   * Returns the value of the operand expression. Note that the value just represents the raw data
   * held by this node. The actual string representation can be different if replacements or types
   * are used.
   *
   * @see com.google.security.zynamics.binnavi.disassembly.COperandTreeNode#toString()
   * @return The value of the operand expression.
   */
  @Override
  public String getValue() {
    return expressionValue;
  }

  @Override
  public boolean hasAddendSibling() {
    return determineAddendSibling(this) != null;
  }

  @Override
  public void removeListener(final INaviOperandTreeNodeListener listener) {
    listeners.removeListener(listener);
  }

  public void setDisplayStyle(final OperandDisplayStyle style) {
    Preconditions.checkNotNull(style, "IE00463: Style argument can not be null");
    if (displayStyle.equals(style)) {
      return;
    }
    displayStyle = style;

    for (final INaviOperandTreeNodeListener listener : listeners) {
      try {
        listener.changedDisplayStyle(this, style);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setId(final int nodeId) {
    id = nodeId;
  }

  @Override
  public void setTypeSubstitution(final TypeSubstitution substitution) {
    this.substitution = substitution;
  }

  @Override
  public String toString() {
    return (replacement == null) || (expressionType == ExpressionType.SIZE_PREFIX) ? (expressionValue
        .equals("dword") ? "" : expressionValue)
        : replacement.toString();
  }

  // TODO(timkornau): We are currently not sure if function replacements are already handled
  // correctly therefore this code is still here. After the mechanism how function replacements are
  // updated has been validated to not rely on this method we are able to delete it.
  private class InternalReplacementListener implements INaviReplacementListener {
    @Override
    public void changed(final INaviReplacement replacement) {
      for (final INaviOperandTreeNodeListener listener : listeners) {
        try {
          listener.changedValue(COperandTreeNode.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  private void notifyListeners() {
    for (final INaviOperandTreeNodeListener listener : listeners) {
      listener.changedValue(COperandTreeNode.this);
    }
  }

  /**
   * Internal listener class for type instance events.
   */
  public class InternalTypeInstanceContainerListener implements TypeInstanceContainerListener {


    private void notifyOperandChanged(final TypeInstanceReference reference) {
      if (instanceReferences != null && instanceReferences.contains(reference)) {
        notifyListeners();
      }
    }

    @Override
    public void addedTypeInstance(final TypeInstance instance) {}

    @Override
    public void addedTypeInstanceReference(final TypeInstanceReference reference) {
      notifyOperandChanged(reference);
    }

    @Override
    public void changedTypeInstance(final TypeInstance instance) {
      if (instanceReferences == null) {
        return;
      }
      for (final TypeInstanceReference reference : instanceReferences) {
        if (instance == reference.getTypeInstance()) {
          notifyListeners();
          return;
        }
      }
    }

    @Override
    public void changedTypeInstanceReference(final TypeInstanceReference reference) {
      notifyOperandChanged(reference);
    }

    @Override
    public void removedTypeInstance(final TypeInstance instance) {}

    @Override
    public void removedTypeInstanceReference(final TypeInstanceReference reference) {
      notifyOperandChanged(reference);
    }
  }
}
