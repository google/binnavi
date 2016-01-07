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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNodeListener;
import com.google.security.zynamics.binnavi.disassembly.OperandDisplayStyle;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.IReplacement;
import com.google.security.zynamics.zylib.general.ListenerProvider;

// / Represents a single operand expression of an operand.
/**
 * An operand expression is a single, atomic part of an operand tree. Examples for operand
 * expressions are registers, literal values, arithmetic operator like + and *, or size prefixes.
 */
public final class OperandExpression implements ApiObject<INaviOperandTreeNode> {
  /**
   * Wrapped internal operand tree node object.
   */
  private final INaviOperandTreeNode m_node;

  /**
   * Child nodes of the operand expression.
   */
  private final List<OperandExpression> m_children = new ArrayList<OperandExpression>();

  /**
   * Parent node of the operand expression.
   */
  private OperandExpression m_parent = null;

  /**
   * Outgoing references of the operand expression.
   */
  private final List<Reference> m_references = new ArrayList<Reference>();

  /**
   * Keeps the API operand expression object synchronized with the internal operand expression
   * object.
   */
  private final INaviOperandTreeNodeListener m_internalListener =
      new InternalOperandTreeNodeListener();

  /**
   * Listeners that are notified about changes in the operand expression.
   */
  private final ListenerProvider<IOperandExpressionListener> m_listeners =
      new ListenerProvider<IOperandExpressionListener>();

  // / @cond INTERNAL
  /**
   * Creates a new operand expression object.
   *
   * @param node The wrapped internal operand tree node object.
   */
  // / @endcond
  public OperandExpression(final INaviOperandTreeNode node) {
    m_node = Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    for (final IReference reference : m_node.getReferences()) {
      m_references.add(new Reference(reference));
    }

    m_node.addListener(m_internalListener);
  }

  // ! Creates a new operand expression.
  /**
   * Creates a new operand expression.
   *
   * @param module The module the operand expression belongs to.
   * @param value The value of the operand expression.
   * @param type The type of the operand expression.
   *
   * @return The created operand expression.
   */
  public static OperandExpression create(
      final Module module, final String value, final ExpressionType type) {
    return new OperandExpression(module.getNative().createOperandExpression(value, type.getNative()));
  }

  // / @cond INTERNAL
  /**
   * Links two operand expressions.
   *
   * @param parent The parent node to link.
   * @param child The child node to link.
   */
  // / @endcond
  public static void link(final OperandExpression parent, final OperandExpression child) {
    Preconditions.checkNotNull(parent, "Error: Parent argument can't be null");
    Preconditions.checkNotNull(child, "Error: Child argument can't be null");

    parent.m_children.add(child);
    child.m_parent = parent;
  }

  @Override
  public INaviOperandTreeNode getNative() {
    return m_node;
  }

  // ! Adds an operand expression listener.
  /**
   * Adds an object that is notified about changes in the operand expression.
   *
   * @param listener The listener object that is notified about changes in the operand expression.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the operand
   *         expression.
   */
  public void addListener(final IOperandExpressionListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Adds a new reference to the operand expression.
  /**
   * This method can be used to add new code or data references to operand expressions.
   *
   * @param address The target address of the new reference.
   * @param type The type of the new reference.
   *
   * @return The created reference.
   *
   * @throws CouldntSaveDataException Thrown if the new reference could not be saved.
   */
  public Reference addReference(final Address address, final ReferenceType type)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    Preconditions.checkNotNull(type, "Error: Type argument can not be null");

    try {
      final CReference reference = new CReference(new CAddress(address.toLong()), type.getNative());

      m_node.addReference(reference);

      return ObjectFinders.getObject(reference, m_references);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Deletes a reference from the operand expression
  /**
   * Permanently deletes a code or data reference from this operand expression.
   *
   * @param reference The reference to delete.
   *
   * @throws CouldntDeleteException Thrown if the reference could not be deleted.
   */
  public void deleteReference(final Reference reference) throws CouldntDeleteException {
    Preconditions.checkNotNull(reference, "Error: Reference argument can not be null");

    try {
      m_node.deleteReference(reference.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException e) {
      throw new CouldntDeleteException(e);
    }
  }

  // ! Child nodes of the operand.
  /**
   * Returns the child expressions of the operand expression.
   *
   * @return The child expressions of the operand expression.
   */
  public List<OperandExpression> getChildren() {
    return new ArrayList<OperandExpression>(m_children);
  }

  // ! Parent node of the operand.
  /**
   * Returns the parent expression of the operand expression. If this expression is null, the
   * operand expression is a root node in its operand tree.
   *
   * @return The parent expression of the operand expression or null if the operand expression is a
   *         root node.
   */
  public OperandExpression getParent() {
    return m_parent;
  }

  // ! References associated with the expression.
  /**
   * Returns the reference objects associated with this operand expression.
   *
   * @return The reference objects associated with the operand expression.
   */
  public List<Reference> getReferences() {
    return new ArrayList<Reference>(m_references);
  }

  // ! Replacement value of the expression.
  /**
   * Returns the replacement value of the operand expression. This is the value that is shown in
   * views instead of the original value of the expression. If an operand expression has no
   * replacement, null is returned.
   *
   * @return The replacement of the operand expression or null.
   */
  public String getReplacement() {
    final IReplacement replacement = m_node.getReplacement();

    return replacement == null ? null : replacement.toString();
  }

  // ! Type of the expression.
  /**
   * Returns the type of the operand expression.
   *
   * @return The type of the operand expression.
   */
  public ExpressionType getType() {
    return ExpressionType.convert(m_node.getType());
  }

  // ! Value of the expression.
  /**
   * Returns the string value of the operand expression.
   *
   * @return The string value of the operand expression.
   */
  public String getValue() {
    return m_node.getValue();
  }

  // ! Removes a listener from the operand expression.
  /**
   * Removes a listener object from the operand expression.
   *
   * @param listener The listener object to remove from the operand expression.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the operand
   *         expression.
   */
  public void removeListener(final IOperandExpressionListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the expression.
  /**
   * Returns the string representation of the operand expression.
   *
   * @return The string representation of the operand expression.
   */
  @Override
  public String toString() {
    return getValue();
  }

  /**
   * Keeps the API operand expression object synchronized with the internal operand expression
   * object.
   */
  private class InternalOperandTreeNodeListener implements INaviOperandTreeNodeListener {
    @Override
    public void addedReference(
        final INaviOperandTreeNode operandTreeNode, final IReference reference) {
      final Reference newReference = new Reference(reference);

      m_references.add(newReference);

      for (final IOperandExpressionListener listener : m_listeners) {
        try {
          listener.addedReference(OperandExpression.this, newReference);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDisplayStyle(
        final COperandTreeNode operandTreeNode, final OperandDisplayStyle style) {
      // Don't expose this to the API
    }

    @Override
    public void changedValue(final INaviOperandTreeNode operandTreeNode) {
      for (final IOperandExpressionListener listener : m_listeners) {
        try {
          listener.changed(OperandExpression.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedReference(
        final INaviOperandTreeNode operandTreeNode, final IReference reference) {
      final Reference removedReference = ObjectFinders.getObject(reference, m_references);

      m_references.remove(removedReference);

      for (final IOperandExpressionListener listener : m_listeners) {
        try {
          listener.removedReference(OperandExpression.this, removedReference);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
