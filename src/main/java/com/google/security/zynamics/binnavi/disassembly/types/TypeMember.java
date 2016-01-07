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

/**
 * Represents a member variable of a (compound) type.
 *
 *  Client code has to create instances of this class through the type manager. Also, no class
 * outside this package is able to modify the state of a TypeMember. Client code is supposed to
 * perform any change through the type manager.
 *
 *  Note that TypeMember has a natural order (by offset, and id) that is inconsistent with equals,
 * i.e. (x.compareTo(y) == 0) == x.equals(y) is not true. The reason is that we would need to
 * implement equals for all fields of TypeMember which is something we don't want to/can't do for
 * BaseType fields since BaseType equality testing would be very expensive.
 */
public final class TypeMember implements Comparable<TypeMember> {

  /**
   * The name of the type member. Can not be null.
   */
  private String name;

  /**
   * The base type of the type member. Can not be null.
   */
  private BaseType baseType;

  /**
   * The parent type of the type member. Can not be null.
   */
  private final BaseType parentType;

  /**
   * The offset of the type member. Can be absent if the type member does not have an offset.
   */
  private Optional<Integer> offset;

  /**
   * The number of elements for the type member. Used only for array compound types. Otherwise
   * absent.
   */
  private Optional<Integer> numberOfElements;

  /**
   * The argument index for the type member. Used only for function prototypes. Otherwise absent.
   */
  private Optional<Integer> argumentIndex;

  /**
   * The id of the type member. Unique per type system.
   */
  private final int id;

  /**
   * Creates a new member instance.
   *
   * @param id The is of the database record corresponding to this instance.
   * @param parentType The base type where this member is contained in.
   * @param baseType The base type of this member.
   * @param name The name of this member.
   * @param offset The offset of this member relative to the beginning of the containing base type
   *        (in bits). If the parameter is null, this indicates that the containing base type
   *        represents an array type.
   * @param numberOfElements The number of elements this member has.
   * @param argumentIndex The argument index if this member is contained in a function prototype.
   */
  private TypeMember(final int id,
      final BaseType parentType,
      final BaseType baseType,
      final String name,
      final Optional<Integer> offset,
      final Optional<Integer> numberOfElements,
      final Optional<Integer> argumentIndex) {
    this.id = id;
    this.name = Preconditions.checkNotNull(name, "IE02622: Member name can not be null.");
    this.baseType =
        Preconditions.checkNotNull(baseType, "IE02623: Base type of member can not be null.");
    this.parentType =
        Preconditions.checkNotNull(parentType, "IE02624: The parent type can not be null.");
    Preconditions.checkArgument(parentType != baseType,
        "Error: Cannot create recursive member declaration.");
    this.numberOfElements =
        Preconditions.checkNotNull(numberOfElements, "Error: number of elements can not be null.");
    Preconditions.checkArgument(!numberOfElements.isPresent() || numberOfElements.get() >= 0,
        "Error: Number of elements for member must either be greater zero or absent.");
    this.offset = Preconditions.checkNotNull(offset, "Error: offset argument can not be null.");
    this.argumentIndex =
        Preconditions.checkNotNull(argumentIndex, "Error: argument can not be null.");
    Preconditions.checkArgument(!argumentIndex.isPresent() || argumentIndex.get() >= 0,
        "Error: Argument index member must either be greater zero or absent.");
    Preconditions.checkArgument(
        (numberOfElements.isPresent() != offset.isPresent()) ^ argumentIndex.isPresent(),
        "Error: Either this is a struct member or an array member, or a prototype argument");
    Preconditions.checkArgument(!offset.isPresent() || offset.get() >= 0,
        "Error: Member offset must either be greater zero or absent.");
  }

  /**
   * Factory method to create a structure type member.
   *
   * @param id The is of the database record corresponding to this instance.
   * @param parentType The base type where this member is contained in.
   * @param baseType The base type of this member.
   * @param name The name of this member.
   * @param structureOffset The offset of the member in the structure.
   * @return The created {@link TypeMember member}.
   */
  static TypeMember createStructureMember(final int id, final BaseType parentType,
      final BaseType baseType, final String name, final int structureOffset) {
    return new TypeMember(id,
        parentType,
        baseType,
        name,
        Optional.of(structureOffset),
        Optional.<Integer>absent(),
        Optional.<Integer>absent());
  }

  /**
   * Factory method to create an union type member.
   *
   * @param id The is of the database record corresponding to this instance.
   * @param parentType The base type where this member is contained in.
   * @param baseType The base type of this member.
   * @param name The name of this member.
   * @return The created {@link TypeMember member}.
   */
  static TypeMember createUnionMember(final int id, final BaseType parentType,
      final BaseType baseType, final String name) {
    return new TypeMember(id,
        parentType,
        baseType,
        name,
        Optional.of(0),
        Optional.<Integer>absent(),
        Optional.<Integer>absent());
  }

  /**
   * Factory method to create a function prototype type member.
   *
   * @param id The is of the database record corresponding to this instance.
   * @param parentType The base type where this member is contained in.
   * @param baseType The base type of this member.
   * @param name The name of this member.
   * @param argumentIndex The index of the member in the function prototype.
   * @return The created {@link TypeMember member}.
   */
  static TypeMember createFunctionPrototypeMember(final int id, final BaseType parentType,
      final BaseType baseType, final String name, final int argumentIndex) {
    return new TypeMember(id,
        parentType,
        baseType,
        name,
        Optional.<Integer>absent(),
        Optional.<Integer>absent(),
        Optional.of(argumentIndex));
  }

  /**
   * Factory method to create an array type member.
   *
   * @param id The is of the database record corresponding to this instance.
   * @param parentType The base type where this member is contained in.
   * @param baseType The base type of this member.
   * @param name The name of this member.
   * @param numberOfElements The number of the elements in the member.
   * @return The created {@link TypeMember member}.
   */
  static TypeMember createArrayMember(final int id, final BaseType parentType,
      final BaseType baseType, final String name, final int numberOfElements) {
    return new TypeMember(id,
        parentType,
        baseType,
        name,
        Optional.<Integer>absent(),
        Optional.of(numberOfElements),
        Optional.<Integer>absent());
  }

  /**
   * Creates a new instance that can be used as a search proxy in sorted containers to be compared
   * against regular TypeMember instances.
   */
  static TypeMember createSearchProxy(final int offset) {
    return new TypeMember(offset);
  }

  /**
   * Creates a new member instance that is not backed by the database and has only default field
   * values apart from offset.
   */
  private TypeMember(final int offset) {
    // Represents a non-existing database id.
    id = -1;
    name = "";
    parentType = null;
    this.offset = Optional.<Integer>of(offset);
    numberOfElements = Optional.<Integer>absent();
    argumentIndex = Optional.<Integer>absent();
  }

  private int compareOptionalInteger(final Optional<Integer> lhs, final Optional<Integer> rhs) {
    // Optional values can be absent if both sides of the comparison are they are equal.
    if (!lhs.isPresent() && !rhs.isPresent()) {
      return 0;
    } else if (!lhs.isPresent()) {
      return -1;
    } else if (!rhs.isPresent()) {
      return 1;
    } else {
      return lhs.get().compareTo(rhs.get());
    }
  }

  void setBaseType(final BaseType baseType) {
    this.baseType = baseType;
  }

  void setName(final String name) {
    this.name = name;
  }

  void setNumberOfElements(final Optional<Integer> numberOfElements) {
    Preconditions.checkArgument(this.numberOfElements.isPresent() == numberOfElements.isPresent(),
        "Error: can not change a non array type to an array type.");
    this.numberOfElements = numberOfElements;
  }

  /**
   * Sets the offset of this member. Note that offset can be null if the parent type represents an
   * array.
   *
   * @param offset The new offset of this member.
   */
  void setOffset(final Optional<Integer> offset) {
    Preconditions.checkArgument(this.offset.isPresent() == offset.isPresent(),
        "Error: can not change an array type to a non array type and vice versa.");
    // Changes to offset require synchronization with our parent type. See comment of compareTo
    // method.
    parentType.deleteMember(this);
    this.offset = offset;
    parentType.addMember(this);
  }

  void setArgumentIndex(final Optional<Integer> argumentIndex) {
    Preconditions.checkArgument(this.argumentIndex.isPresent() == argumentIndex.isPresent(),
        "Error: can not change a function prototype type to a non function prototype type.");
    this.argumentIndex = argumentIndex;
  }

  @Override
  public int compareTo(final TypeMember rhs) {
    // Note that the containing base type needs to be informed about any changes to a TypeMember
    // instance that affects the sorting order since base types keep a sorted container of member
    // instances that must be updated accordingly.
    final int offsetComparison = compareOptionalInteger(getBitOffset(), rhs.getBitOffset());
    if (offsetComparison != 0) {
      return offsetComparison;
    }
    final int numberOfElementsComparison =
        compareOptionalInteger(getNumberOfElements(), rhs.getNumberOfElements());
    if (numberOfElementsComparison != 0) {
      return numberOfElementsComparison;
    }
    final int argumentIndexComparison =
        compareOptionalInteger(getArgumentIndex(), rhs.getArgumentIndex());
    if (argumentIndexComparison != 0) {
      return argumentIndexComparison;
    }
    return getId() - rhs.getId();
  }

  /**
   * Returns the base type of the member variable which is represented by this instance.
   *
   * @return The base type of this member.
   */
  public BaseType getBaseType() {
    return baseType;
  }

  /**
   * Returns the size of this member in bits.
   *
   * @return The size of this member in bits.
   */
  public int getBitSize() {
    return baseType.getBitSize();
  }

  /**
   * Returns the corresponding database id that is associated with this instance.
   *
   * @return The corresponding database id of this instance.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the name of the member variable which is represented by this instance.
   *
   * @return The declared name of this member.
   */
  public String getName() {
    return name;
  }

  /**
   * If the member variable represented by this instance is an array, this method returns the number
   * of elements in that array.
   *
   * @return The number of elements this member has.
   */
  public Optional<Integer> getNumberOfElements() {
    return numberOfElements;
  }

  /**
   * Returns the offset of this member relative to the beginning of the containing structure (in
   * bits).
   *
   * @return The offset of this member in the containing base type. Note that offset can be null if
   *         the parent type is an array.
   */
  public Optional<Integer> getBitOffset() {
    return offset;
  }

  /**
   * If the member variable represented by this instance is a member of a function prototype, this
   * method returns the argument index of the member within the function prototype.
   *
   * @return The argument index of the type member.
   */
  public Optional<Integer> getArgumentIndex() {
    return argumentIndex;
  }

  /**
   * Returns the offset of this member in bytes.
   *
   * @return The byte offset of this member.
   */
  public Optional<Integer> getByteOffset() {
    return offset.isPresent() ? Optional.of((offset.get() + 7) / 8) : offset;
  }

  /**
   * Returns the enclosing base type which contains this member variable.
   *
   * @return The enclosing base type of this member.
   */
  public BaseType getParentType() {
    return parentType;
  }

  public boolean isOffsetType() {
    return getBitOffset().isPresent();
  }

  public boolean isIndexType() {
    return getArgumentIndex().isPresent();
  }

  // Note: this method shouldn't be used for displaying members in BinNavi.
  // The purpose of this function is to make debugging more convenient.
  @Override
  public String toString() {
    return String.format("%s %s, parent=%s, offset=%d", baseType.getName(), name,
        parentType.getName(), offset.orNull());
  }
}
