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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * The type for an operand expression that can be associated with a single operand node. Optionally,
 * it points to a specific position (i.e. member) in a BaseType which allows us to express
 * references to fields of compound types.
 *
 * Client code has to create instances through the type manager.
 */
public class TypeSubstitution {

  private final int position;
  private int offset;
  private BaseType baseType;
  private final IAddress address;
  private final INaviOperandTreeNode node;
  private final int expressionId;
  private List<TypeMember> memberPath;

  TypeSubstitution(final INaviOperandTreeNode node, final BaseType baseType, final int expressionId,
      final int position, final int offset, final IAddress address) {
    this(node, baseType, new ArrayList<TypeMember>(), expressionId, position, offset, address);
  }

  private static String toByteOffsetString(final int bitOffset) {
    final int byteOffset = (bitOffset < 0) ? (bitOffset - 7) / 8 : (bitOffset + 7) / 8;
    return (bitOffset < 0) ? String.format("%d", byteOffset) : String.format("+%d", byteOffset);
  }
  
  /**
   * Creates a new type substitution for the given operand tree node.
   *
   * @param node The tree node that is annotated with this type substitution.
   * @param baseType The base type that is associated with the given operand node.
   * @param memberPath A sequence of members leading to unambiguously reference a member of a type.
   *        Needed, if this type substitution references a union type, otherwise can be empty.
   * @param position The zero-based index position of the operand tree node within its instruction.
   * @param offset An additional offset relative to the base type so a type substitution can
   *        reference a specific member of a compound type.
   * @param address The address of the instruction that contains the operand tree node that is
   *        annotated with this type substitution.
   */
  TypeSubstitution(final INaviOperandTreeNode node, final BaseType baseType,
      final List<TypeMember> memberPath, final int expressionId, final int position,
      final int offset, final IAddress address) {
    this.baseType = Preconditions.checkNotNull(
        baseType, "Error: Base type argument for type substitution can not be null.");
    this.memberPath = Preconditions.checkNotNull(memberPath, "Error: Member path can not be null.");
    this.expressionId = expressionId;
    Preconditions.checkArgument(
        position >= 0, "Error: Position argument for type substitution can not be negative.");
    this.position = position;
    Preconditions.checkArgument(
        offset >= 0, "Error: Offset argument for type substitution can not be negative.");
    this.offset = offset;
    this.node = Preconditions.checkNotNull(
        node, "IE02802: Node argument for type substitution can not be null.");
    this.address = Preconditions.checkNotNull(
        address, "Error: Address argument for type sbustitution can not be null.");
  }

  private static String renderArray(final BaseType baseType, final int offset) {
    return BaseTypeHelpers.findMember(baseType, offset).getPathString();
  }

  private static String renderStruct(final BaseType baseType, final int offset) {
    final BaseTypeHelpers.WalkResult result = BaseTypeHelpers.findMember(baseType, offset);
    if (result.getMember() == null) {
      // If the offset points outside of the base type, we indicate this by showing the whole
      // offset (in bytes).
      return String.format("%s%s", baseType.getName(), toByteOffsetString(offset));
    } else {
      return result.getPathString();
    }
  }

  public List<TypeMember> getMemberPath() {
    return memberPath;
  }

  /**
   * Generates a string for the given type substitution and the additional immediate value of the
   * second operand in an expression.
   *
   *  Example: [esp+4] has a type substitution attached to esp. The operand value is 4 in this case.
   * Also, the type substitution itself might have an additional offset in order to directly address
   * members of compound base types.
   *
   * @param typeSubstitution The type substitution which should be converted into a string.
   * @param operandValue The immediate value of the second operand in the expression (in bytes).
   */
  public static String generateTypeString(
      final TypeSubstitution typeSubstitution, final long operandValue) {
    final BaseType baseType = typeSubstitution.getBaseType();
    final int totaBitOffset = (int) (operandValue) * 8 + typeSubstitution.getOffset();
    switch (baseType.getCategory()) {
      case STRUCT:
        return renderStruct(baseType, totaBitOffset);
      case UNION:
        return renderUnion(baseType, typeSubstitution.getMemberPath(), totaBitOffset);
      case ARRAY:
        return renderArray(baseType, totaBitOffset);
      case ATOMIC:
      case POINTER:
        return renderAtomic(baseType, totaBitOffset);
      default:
        return "";
    }
  }

  private static String renderAtomic(final BaseType baseType, final int totalOffset) {
    if (totalOffset == 0) {
      return baseType.getName();
    }
    // If the type substitution points outside of the atomic type, we indicate this by and
    // additional "+" and "-" together with the remaining offset, respectively.
    return String.format("%s%s", baseType.getName(), toByteOffsetString(totalOffset));
  }

  private static String renderUnion(final BaseType baseType, final List<TypeMember> memberPath,
      final int offset) {
    final StringBuilder path = new StringBuilder(baseType.getName());
    for (final TypeMember member :  memberPath) {
      path.append('.');
      path.append(member.getName());
    }
    if (offset != 0) {
      path.append(toByteOffsetString(offset));
    }
    return path.toString();
  }

  void setMemberPath(final List<TypeMember> memberPath) {
    this.memberPath = memberPath;
  }

  void setBaseType(final BaseType baseType) {
    this.baseType = baseType;
  }

  void setOffset(final int offset) {
    this.offset = offset;
  }

  public IAddress getAddress() {
    return address;
  }

  public BaseType getBaseType() {
    return baseType;
  }

  public int getExpressionId() {
    return expressionId;
  }

  public int getOffset() {
    return offset;
  }

  public INaviOperandTreeNode getOperandTreeNode() {
    return node;
  }

  public int getPosition() {
    return position;
  }
}
