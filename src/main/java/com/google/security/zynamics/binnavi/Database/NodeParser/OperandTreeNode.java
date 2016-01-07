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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.binnavi.disassembly.INaviReplacement;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple helper class that is used to build operand trees during instruction parsing. This helper
 * class is used to temporarily store relevant operand tree node data that was read from the
 * database. In a later step the information stored in this class is then converted into proper
 * operand tree node objects which are then used in com.google.security.zynamics.binnavi.
 */
public final class OperandTreeNode {
  /**
   * The ID of the node.
   */
  private final int m_id;

  /**
   * The numerical representation of the node type.
   */
  private final int m_type;

  /**
   * The value of the node.
   */
  private final String m_value;

  /**
   * The parent ID of the node. This value can be null if the node is a root node.
   */
  private final Integer m_parentId;

  /**
   * Replacement string of the node. This value can be null if no replacement is given.
   */
  private final INaviReplacement m_replacement;

  /**
   * List of references from the operand to memory locations.
   */
  private final List<CReference> m_reference;

  private final RawTypeSubstitution substitution;

  /**
   * The id of the {@link TypeInstance type instance} this {@link OperandTreeNode node} is
   * associated to if any.
   */
  private final Integer typeInstanceId;

  private final int position;

  private final IAddress address;

  /**
   * Creates a new operand tree node object.
   *
   * @param operandId The ID of the node.
   * @param type The numerical representation of the node type.
   * @param value The value of the node.
   * @param parentId The parent ID of the node. This value can be null if the node is a root node.
   * @param replacement Replacement string of the node. This value can be null if no replacement is
   *        given.
   * @param references List of references from the operand to memory locations.
   * @param substitution The type substitution for this operand tree node or null.
   * @param address The address of the operand tree node.
   * @param operandPosition The position of the operand tree node.
   * @param instanceId The type instance id for this operand tree node.
   */
  public OperandTreeNode(final int operandId,
      final int type,
      final String value,
      final Integer parentId,
      final INaviReplacement replacement,
      final List<CReference> references,
      final RawTypeSubstitution substitution,
      final Integer instanceId,
      final int operandPosition,
      final IAddress address) {
    m_value = Preconditions.checkNotNull(value, "IE01298: Value argument can not be null");
    m_id = operandId;
    m_type = type;
    m_parentId = parentId;
    m_replacement = replacement;
    m_reference = references;
    this.substitution = substitution;
    this.typeInstanceId = instanceId;
    this.position = operandPosition;
    this.address = address;
  }

  public IAddress getAddress() {
    return address;
  }

  public int getPosition() {
    return position;
  }

  /**
   * Returns the ID of the node.
   *
   * @return The ID of the node.
   */
  public int getId() {
    return m_id;
  }

  /**
   * Returns the id of the {@link TypeInstance instance} which is associated to this
   * {@link OperandTreeNode node}
   *
   * @return The id of the {@link TypeInstance}.
   */
  public Integer getTypeInstanceId() {
    return typeInstanceId;
  }

  /**
   * Returns the parent ID of the node.
   *
   * @return The parent ID of the node or null if the node is a root node.
   */
  public Integer getParentId() {
    return m_parentId;
  }

  /**
   * Returns the references from the node to memory addresses.
   *
   * @return The outgoing references of this node.
   */
  public List<IReference> getReferences() {
    return new ArrayList<IReference>(m_reference);
  }

  /**
   * Returns the replacement string of the node.
   *
   * @return The replacement string of the node or null.
   */
  public INaviReplacement getReplacement() {
    return m_replacement;
  }

  /**
   * Returns the numerical value of the operand type.
   *
   * @return The numerical value of the operand type.
   */
  public int getType() {
    return m_type;
  }

  /**
   * Returns the type substitution associated with this operand tree node or null if there is no
   * substitution.
   *
   * @return The type substitution of this operand tree node.
   */
  public RawTypeSubstitution getTypeSubstitution() {
    return substitution;
  }

  /**
   * Returns the value of the node.
   *
   * @return The value of the node.
   */
  public String getValue() {
    return m_value;
  }

  @Override
  public String toString() {
    return m_value;
  }
}
