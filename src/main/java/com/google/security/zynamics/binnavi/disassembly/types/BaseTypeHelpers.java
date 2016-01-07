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
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * Includes helper functions to derive additional information about base types that is not trivially
 * available via methods.
 */
public final class BaseTypeHelpers {

  private static String buildArrayName(final BaseType arrayType, final long l) {
    final StringBuilder builder = new StringBuilder(BaseType.normalizeArrayName(arrayType));
    builder.append('[');
    builder.append(l);
    builder.append(']');
    return builder.toString();
  }

  private static String determineIndexedMemberString(final TypeMember member, final long offset) {
    return String.format("%s[%d]", member.getName(),
        offset / getArrayElementType(member.getBaseType()).getBitSize());
  }

  private static WalkResult findArrayMember(final BaseType arrayType, final long offset) {
    if (offset >= 0 && offset < arrayType.getBitSize()) {
      return new WalkResult(arrayType.getLastMember(),
          Lists.newArrayList(arrayType.getLastMember()),
          buildArrayName(arrayType, offset / getArrayElementType(arrayType).getBitSize()));
    }
    final long byteOffset = (offset < 0) ? (offset - 7) / 8 : (offset + 7) / 8;
    final String arrayString = (offset < 0) ? String.format("%s%d", buildArrayName(arrayType, 0
        /* element */), byteOffset) : String.format("%s+%d", arrayType.getName(),
            byteOffset - arrayType.getByteSize());
    return new WalkResult(
        arrayType.getLastMember(), Lists.newArrayList(arrayType.getLastMember()), arrayString);
  }

  private static WalkResult findStructMember(final BaseType structBaseType, final long offset) {
    final StringBuilder pathString = new StringBuilder(structBaseType.getName());
    final List<TypeMember> memberList = Lists.newArrayList();
    int memberOffset = 0;
    int currentOffset = 0;
    int lastMemberOffset = 0;
    Iterator<TypeMember> it = structBaseType.iterator();

    while (it.hasNext()) {
      final TypeMember member = it.next();
      memberOffset = member.getBitOffset().isPresent() ? member.getBitOffset().get() : 0;
      currentOffset += memberOffset - lastMemberOffset;
      lastMemberOffset = memberOffset;

      // Is member a candidate for the offset?
      if (currentOffset <= offset) {
        switch (member.getBaseType().getCategory()) {
          case ATOMIC:
          case POINTER:
            if (currentOffset == offset) {
              pathString.append('.');
              pathString.append(member.getName());
              memberList.add(member);
              return new WalkResult(member, memberList, pathString.toString());
            }
            break;
          case ARRAY:
            // TODO(jannewger): handle out-of-bounds array access.
            if (offset < currentOffset + member.getBaseType().getBitSize()
                && offset % getArrayElementType(member.getBaseType()).getBitSize() == 0) {
              pathString.append('.');
              pathString.append(determineIndexedMemberString(member, offset - currentOffset));
              memberList.add(member);
              return new WalkResult(member, memberList, pathString.toString());
            }
            break;
          case STRUCT:
          case UNION:
            if (offset < currentOffset + member.getBaseType().getBitSize()) {
              pathString.append('.');
              pathString.append(member.getName());
              memberList.add(member);
              it = member.getBaseType().iterator();
              lastMemberOffset = 0;
            }
            break;
          default:
            return new WalkResult();
        }
      }
    }
    return new WalkResult();
  }

  /**
   * Finds a member of a {@link BaseType compound type} at the given offset.
   *
   * @param baseType The {@link BaseType compound type} where to lookup the member at the given
   *        offset.
   * @param offset The offset in bits at which to look for the member.
   * @return A {@link WalkResult} which contains the {@link TypeMember member} of the compound type.
   *         If there is no member at the given offset a {@link WalkResult result} without a
   *         {@link TypeMember member} is returned.
   */
  public static WalkResult findMember(final BaseType baseType, final long offset) {
    Preconditions.checkNotNull(baseType, "IE02760: Base type can not be null.");
    switch (baseType.getCategory()) {
      case POINTER:
      case ATOMIC:
        return new WalkResult();
      case ARRAY:
        return findArrayMember(baseType, offset);
      case STRUCT:
      case UNION:
        return findStructMember(baseType, offset);
      default:
        throw new IllegalStateException(
            "Error: BaseTypeCategory " + baseType.getCategory() + " not supported.");
    }
  }

  /**
   * Gets the byte size of the {@link TypeMember array elements} {@link BaseType base type},
   *
   * @param arrayType The array where to determine the element byte size.
   * @return The {@link TypeMember array elements} {@link BaseType base types} byte size.
   */
  public static int getArrayElementByteSize(final BaseType arrayType) {
    Preconditions.checkNotNull(arrayType, "Error: array type argument can not be null.");
    Preconditions.checkArgument(arrayType.getCategory() == BaseTypeCategory.ARRAY,
        "Error: arrayType argument not of type ARRAY.");
    return getArrayElementType(arrayType).getByteSize();
  }

  /**
   * Determines the type of the elements in an {@link BaseType array type}.
   *
   * @param arrayType The {@link BaseType array type} to determine the elements type in.
   * @return The {@link BaseType type} of the elements.
   */
  private static BaseType getArrayElementType(final BaseType arrayType) {
    return arrayType.iterator().next().getBaseType();
  }

  /**
   * Determines whether the given offset points to an existing member in the given type or points
   * outside of the type.
   *
   * @param baseType The type to check the offset for.
   * @param offset The offset to be checked (in bits).
   * @return True iff the offset is that of an existing member in the base type.
   */
  public static boolean isValidOffset(final BaseType baseType, final int offset) {
    Preconditions.checkNotNull(baseType, "Error: base type argument can not be null.");
    return (offset < 0) ? false : findMember(baseType, offset).isValid();
  }

  /**
   * Represents the results of finding a member starting from a given base type and offset.
   */
  public static final class WalkResult {
    private final TypeMember member;
    private final List<TypeMember> path;
    private final String pathString;

    public WalkResult() {
      this.member = null;
      this.path = null;
      this.pathString = "";
    }

    public WalkResult(
        final TypeMember member, final List<TypeMember> path, final String pathString) {
      this.member = member;
      this.path = path;
      this.pathString = pathString;
    }

    public boolean isValid() {
      return member != null && path != null;
    }

    public TypeMember getMember() {
      return member;
    }

    public String getPathString() {
      return pathString;
    }

    public List<TypeMember> getPath() {
      return path;
    }
  }
}
