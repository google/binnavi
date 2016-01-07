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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Represents a base type in BinNavis type system. This base type can refer to a compound type, e.g.
 * structure or array, but can also mean an atomic type such as an int in the C programming
 * language.
 *
 *  Important: all setters of this class have package level visibility so this class is immutable to
 * client code outside the types package. This is important since all changes to the type system
 * must be made through the type manager since we need a central entity that dispatches
 * notifications about changes in the type system.
 *
 *  Note that the type system allows for incomplete compound types (in contrast to the C type
 * system), i.e. there can be gaps in a structure where no members are present. When creating types
 * during the process of RE this allows the user to incrementally build a structure even if not all
 * members have been discovered yet. As a consequence, a compound type may be larger than the sum of
 * its members, however it can never shrink below the offset + size required by the last member,
 * unless the corresponding member(s) is deleted first.
 *
 *  The size of a type is handled depending on the category of that type: if a base type is an
 * atomic or pointer type, it has an explicit size (also in the database). If on the other hand it
 * is a compound type (structure, union, array), the size is calculated online and not stored in the
 * database. This makes it easier to keep type sizes in sync and doesn't put an unnecessary burden
 * on database updates.
 *
 */
public class BaseType implements Iterable<TypeMember> {

  /**
   * The name of the {@link BaseType base type}.
   */
  private String name;

  /**
   * A flag indicating if the {@link BaseType base type} is signed.
   */
  private boolean signed;

  /**
   * pointsTo is filled with a {@link BaseType base type} when this instance is a pointer and points
   * to the {@link BaseType base type} saved in pointsTo.
   */
  private BaseType pointsTo;

  /**
   * {@link BaseType base type} which points to this {@link BaseType base type}.
   */
  private BaseType pointedToBy;

  /**
   * The size of this {@link BaseType base type} in bits.
   */
  private int bitSize;

  /**
   * The id of this {@link BaseType base type} which is retrieved from the database.
   */
  private final int id;

  /**
   * A flag that indicates if this {@link BaseType base type} is a stack frame.
   */
  private boolean isStackFrame;

  /**
   * The set of members contained in this base type sorted by member offsets.
   */
  private final TreeSet<TypeMember> members = new TreeSet<TypeMember>();

  /**
   * The category of this base type following the nomenclature of the C type system.
   */
  private final BaseTypeCategory category;

  /**
   * Create a new base type. The constructor is package-private on purpose, so client code cannot
   * instantiate base types without going through the type manager.
   *
   * @param id The type id in the base_types table.
   * @param name The name of the type.
   * @param bitSize The size of the type in bits.
   * @param signed Specifies whether this type is signed.
   * @param category The category of the base type following the nomenclature of the C type system.
   */
  BaseType(final int id, final String name, final int bitSize, final boolean signed,
      final BaseTypeCategory category) {
    Preconditions.checkArgument(id > 0, "Error: id must be greater than zero.");
    this.name = Preconditions.checkNotNull(name, "IE02757: Name of base type can not be null.");
    Preconditions.checkArgument(bitSize >= 0,
        "Error: Size of base must be above or equal to zero.");
    this.signed = signed;
    this.bitSize = bitSize;
    this.id = id;
    this.category = category;
  }

  private static int computerPointerLevel(final BaseType startType) {
    int pointerLevel = 0;
    for (BaseType baseType = startType.pointsTo(); baseType != null;
        baseType = baseType.pointsTo()) {
      ++pointerLevel;
    }
    return pointerLevel;
  }

  private static int determineOccupiedSize(final TreeSet<TypeMember> members) {
    final TypeMember lastMember = members.last();
    return lastMember.getBitOffset().get() - members.first().getBitOffset().get()
        + lastMember.getBitSize();
  }

  // Returns all members with their offset in [startOffset, endOffset).
  private static List<TypeMember> getMembers(final int startOffset, final int endOffset,
      final TreeSet<TypeMember> members) {
    final ArrayList<TypeMember> result = Lists.newArrayList();
    for (final TypeMember member : members) {
      if (member.getBitOffset().get() >= startOffset && member.getBitOffset().get() < endOffset) {
        result.add(member);
      }
    }
    return result;
  }

  /**
   * Appends a base type to the pointer hierarchy just below parent, e.g. if type is an int then
   * pointer must be an int*. The "bottom" element in this hierarchy is the value type, followed by
   * the pointer types.
   *
   * @param type The type to point to.
   * @param pointer The pointer to point to the type.
   */
  static void appendToPointerHierarchy(final BaseType type, final BaseType pointer) {
    Preconditions.checkNotNull(type, "Error: type argument can not be null.");
    Preconditions.checkNotNull(pointer, "Error: pointer argument can not be null.");
    Preconditions.checkArgument(type != pointer,
        "Error: Can not establish pointer relation between identical types.");
    Preconditions.checkArgument(pointer.pointedToBy != type,
        "Error: Can not establish circular pointer relation.");
    Preconditions.checkArgument(type.pointsTo != pointer,
        "Error: Can not establish circular pointer relation.");
    pointer.pointsTo = type;
    type.pointedToBy = pointer;
  }

  /**
   * Creates the type name for the value type of the given base type with the specified pointer
   * level, e.g. returns "int **" for ("int *", 2).
   *
   * @param baseType The base type for which to create a string type representation with the given
   *        pointer level.
   * @param pointerLevel The absolute pointer level to use when generating the string.
   * @return The string representation of the respective pointer type.
   */
  public static String getPointerTypeName(final BaseType baseType, final int pointerLevel) {
    Preconditions.checkNotNull(baseType, "Error: base type argument can not be null.");
    Preconditions.checkArgument(pointerLevel > 0,
        "Error: pointer level must be greater than zero.");
    return String.format("%s %s", BaseType.getValueTypeName(baseType),
        Strings.repeat("*", pointerLevel));
  }

  /**
   * Determines the value type for a given pointer type, e.g. returns "int" for "int**". If a
   * non-pointer type is given, the name of that type is returned.
   *
   * @param baseType The base type for which to determine the corresponding value type.
   * @return The corresponding value type.
   */
  public static BaseType getValueType(BaseType baseType) {
    Preconditions.checkNotNull(baseType, "Error: base type argument can not be null.");
    while (baseType.pointsTo != null) {
      baseType = baseType.pointsTo;
    }
    return baseType;
  }

  /**
   * Determines the value type name for a given pointer type, e.g. returns "int" for "int**". If a
   * non-pointer type is given, the name of that type is returned.
   *
   * @param baseType The base type for which to determine the corresponding value type name.
   * @return The name of the corresponding value type.
   */
  public static String getValueTypeName(final BaseType baseType) {
    Preconditions.checkNotNull(baseType, "Error: base type argument can not be null.");
    final BaseType valueType = getValueType(baseType);
    return valueType == null ? baseType.getName() : valueType.getName();
  }

  /**
   * Normalizes an array name from the exported name for example array_one[123] to array_one.
   *
   * @param arrayType The {@link BaseType array type} to normalize the name for.
   * @return The normalized name for the array.
   */
  public static String normalizeArrayName(final BaseType arrayType) {
    Preconditions.checkNotNull(arrayType, "Error: array type argument can not be null.");
    Preconditions.checkArgument(arrayType.getCategory() == BaseTypeCategory.ARRAY,
        "Error: array type argument must be of category array.");
    return arrayType.getName().split("\\[")[0];
  }

  private boolean areMembersConsecutive(final TreeSet<TypeMember> members) {
    TypeMember lastMember = null;
    for (final TypeMember member : members) {
      if (lastMember != null && member.getBitOffset().get() - lastMember.getBitOffset().get()
          != lastMember.getBitSize()) {
        return false;
      }
      lastMember = member;
    }
    return true;
  }

  /**
   * Adds a member to this base type.
   *
   * @param member The member to add.
   */
  void addMember(final TypeMember member) {
    Preconditions.checkNotNull(member, "Error: member argument can not be null.");
    Preconditions.checkArgument(member.getParentType() == this,
        "Error: the member's parent type is not this type.");
    members.add(member);
  }

  /**
   * Deletes a member from the list of members. This function removes the member without moving
   * subsequent members and doesn't update the size of this type.
   *
   * @param member The member that should be deleted.
   */
  void deleteMember(final TypeMember member) {
    members.remove(Preconditions.checkNotNull(member, "Error: member argument can not be null."));
  }

  /**
   * Adjusts the offsets of the given members delta (in bits). Other members of the containing
   * structure are moved. Relative distances between members are preserved. Only blocks of
   * consecutive members can be moved, otherwise an illegal argument exception is thrown.
   *
   * @param moveMembers The set of members to move.
   * @param moveDelta The delta in bits by which to move the members.
   * @return Returns the list of implicitly moved members and the corresponding delta.
   */
  MemberMoveResult moveMembers(final TreeSet<TypeMember> moveMembers, final int moveDelta) {
    Preconditions.checkArgument(moveMembers.first().getBitOffset().get() + moveDelta >= 0,
        "Cannot move members to negative offset.");
    Preconditions.checkArgument(moveMembers.last().getBitOffset().get() + moveDelta
        <= members.last().getBitOffset().get() + members.last().getBitSize(),
        "Cannot move members behind last member.");
    Preconditions.checkArgument(areMembersConsecutive(moveMembers),
        "Cannot move members that are not consecutive to each other.");

    final TypeMember firstMember = moveMembers.first();
    final TypeMember lastMember = moveMembers.last();
    final boolean moveTowardsBeginning = moveDelta < 0;
    final int startOffset = moveTowardsBeginning ? firstMember.getBitOffset().get() + moveDelta
        : members.higher(lastMember).getBitOffset().get();
    final int endOffset = moveTowardsBeginning ? firstMember.getBitOffset().get()
        : members.higher(lastMember).getBitOffset().get() + moveDelta;
    final List<TypeMember> implicitlyMoved = getMembers(startOffset, endOffset, members);
    final int implicitMoveDelta = moveTowardsBeginning ? determineOccupiedSize(moveMembers)
        : -determineOccupiedSize(moveMembers);

    // Note that we have to remove the members before changing their offsets since the natural order
    // would be in an inconsistent state otherwise, breaking our internal members TreeSet.
    members.removeAll(moveMembers);
    members.removeAll(implicitlyMoved);
    for (final TypeMember member : implicitlyMoved) {
      member.setOffset(Optional.<Integer>of(member.getBitOffset().get() + implicitMoveDelta));
    }
    for (final TypeMember member : moveMembers) {
      member.setOffset(Optional.<Integer>of(member.getBitOffset().get() + moveDelta));
    }
    members.addAll(moveMembers);
    members.addAll(implicitlyMoved);
    return new MemberMoveResult(implicitlyMoved, implicitMoveDelta);
  }

  /**
   * Marks the base type as being a stack frame.
   *
   * @param isStackFrame True iff this type is a stack frame.
   */
  void setIsStackFrame(final boolean isStackFrame) {
    this.isStackFrame = isStackFrame;
  }

  /**
   * Specifies the new name of this type.
   *
   * @param name The new name of this type.
   */
  void setName(final String name) {
    this.name = Preconditions.checkNotNull(name, "Error: name argument can not be null.");
  }

  /**
   * Specifies whether this type is able to represent signed numbers.
   *
   * @param signed True iff this type can represent signed numbers.
   */
  void setSigned(final boolean signed) {
    this.signed = signed;
  }

  /**
   * Sets the new size in bits for this base type. Note that the size of a compound type can not be
   * explicitly set, see class comment.
   *
   * @param bitSize The new size of this type in bits.
   */
  void setSize(final int bitSize) {
    Preconditions.checkArgument(bitSize >= 0, "Error: type size must be positive.");
    Preconditions.checkArgument(
        category == BaseTypeCategory.ATOMIC || category == BaseTypeCategory.POINTER,
        "Error: can not set size of non-atomic or non-pointer type.");
    this.bitSize = bitSize;
  }

  private int getArraySize() {
    final TypeMember lastMember = members.last();
    return lastMember.getNumberOfElements().get() * lastMember.getBitSize();
  }

  private int getStructSize() {
    if (members.isEmpty()) {
      return 0;
    }
    // Members are sorted by offset and the by size, so this also works for unions.
    // See documentation in TypeMember.compareTo().
    final TypeMember lastMember = members.last();
    return lastMember.getBitOffset().get() + lastMember.getBitSize();
  }

  private int getUnionSize() {
    int maxSize = 0;
    for (TypeMember member : members) {
      if (maxSize < member.getBitSize()) {
        maxSize = member.getBitSize();
      }
    }
    return maxSize;
  }

  /**
   * Returns the size of this type in bits.
   *
   * @return The size of this type in bits.
   */
  public int getBitSize() {
    switch (category) {
      case ATOMIC:
      case FUNCTION_PROTOTYPE:
      case POINTER:
        return bitSize;
      case ARRAY:
        return getArraySize();
      case STRUCT:
        return getStructSize();
      case UNION:
        return getUnionSize();
      default:
        throw new IllegalStateException("Error: can not infer size of type.");
    }
  }

  /**
   * Returns the size of this type in bytes, rounded up to the next byte boundary if necessary.
   *
   * @return The size of this type in bytes.
   */
  public int getByteSize() {
    return (getBitSize() + 7) / 8;
  }

  public BaseTypeCategory getCategory() {
    return category;
  }

  /**
   * Returns whether this type is a function prototype.
   *
   * @return True iff this is a function prototype.
   */
  public boolean isFunctionPrototype() {
    return category == BaseTypeCategory.FUNCTION_PROTOTYPE;
  }

  /**
   * Returns the (unique) type id which connects this instance to the corresponding database record.
   *
   * @return The corresponding database type id.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the member with the largest offset or null if this type has no members.
   *
   * @return The last member of this type or null.
   */
  public TypeMember getLastMember() {
    return members.isEmpty() ? null : members.last();
  }

  /**
   * Returns the number of members this type has.
   *
   * @return The number of members.
   */
  public int getMemberCount() {
    return members.size();
  }

  /**
   * Returns the name of this base type.
   *
   * @return The name of this type.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the position in the hierarchy of pointer types, i.e. the number of '*' in a
   * corresponding C type.
   *
   * @return The pointer level in the hierarchy of pointer types.
   */
  public int getPointerLevel() {
    return computerPointerLevel(this);
  }

  /**
   * Determines all members that have an offset that is larger than or equal to offset, or that
   * overlap with offset. An empty set is returned if no such members exist.
   *
   * @param offset The offset given in bits.
   * @return The set of subsequent members or an empty set.
   */
  public ImmutableList<TypeMember> getSubsequentMembersInclusive(final int offset) {
    Preconditions.checkArgument(BaseTypeCategory.isOffsetCategory(category),
        "Error: Base type category does not have subsequent members.");
    Preconditions.checkArgument(offset >= 0, "Error: offset can not be negative.");
    if (members.isEmpty()) {
      return ImmutableList.of();
    }
    final int searchOffset =
        (offset > members.last().getBitOffset().get() && offset < getBitSize()) ? members.last()
            .getBitOffset().get()
            : offset;
    return ImmutableList.<TypeMember>copyOf(
        members.tailSet(TypeMember.createSearchProxy(searchOffset), true));
  }

  /**
   * Returns an immutable list of members that have a strictly larger offset than the given member.
   * The members are sorted by offset in ascending order.
   *
   * @param member The member to determine the subsequent members for.
   * @return The list of subsequent members.
   */
  public ImmutableList<TypeMember> getSubsequentMembers(final TypeMember member) {
    Preconditions.checkArgument(BaseTypeCategory.isOffsetCategory(category),
        "Error: Base type category does not have subsequent members.");
    return ImmutableList.<TypeMember>copyOf(members.tailSet(member, false));
  }

  /**
   * Returns true iff this type has any members.
   *
   * @return True iff this type has any members.
   */
  public boolean hasMembers() {
    return members.size() > 0;
  }

  /**
   * Specifies whether this base type is signed.
   *
   * @return True if this base type is signed.
   */
  public boolean isSigned() {
    return signed;
  }

  /**
   * Returns true if this {@link BaseType base type} is used in a {@link INaviFunction function} as
   * a stack frame.
   *
   * @return true if {@link INaviFunction function} stack frame.
   */
  public boolean isStackFrame() {
    return isStackFrame;
  }

  @Override
  public Iterator<TypeMember> iterator() {
    // TODO(jannewger): this iterator is potentially dangerous since it leaks internal state which
    // might change while client code is using the iterator (e.g. a member is added due to a DB
    // notification), causing a concurrent modification exception.
    return members.iterator();
  }

  /**
   * Returns the next pointer type in the hierarchy of pointer types, i.e. the type returned has one
   * more level of indirection.
   *
   * @return The next pointer type.
   */
  public BaseType pointedToBy() {
    return pointedToBy;
  }

  /**
   * Returns the type whose level of pointer indirection is one less than the current type, e.g. if
   * the current type is int* we return int.
   *
   * @return A reference to the next base type in the hierarchy of pointer types. Null if this type
   *         is already a value base type.
   */
  public BaseType pointsTo() {
    return pointsTo;
  }

  /**
   * Note: this method shouldn't be used for displaying types in binnavi. The purpose of this
   * function is to make debugging more convenient.
   */
  @Override
  public String toString() {
    return String.format("%s, bitSize=%d bits, %d members, id=%d", name, bitSize, members.size(),
        id);
  }
}
