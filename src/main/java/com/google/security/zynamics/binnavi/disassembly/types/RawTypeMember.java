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
import java.util.Objects;

/**
 * Represents raw database records which correspond to TypeMember objects. This class is only used
 * when loading members from the database and shouldn't be used elsewhere.
 */
public final class RawTypeMember {
  private final int id;
  private final String name;
  private final int baseTypeId;
  private final Optional<Integer> numberOfElements;
  private final Integer parentId;
  private final Optional<Integer> offset;
  private final Optional<Integer> argument;

  public RawTypeMember(final int id, final String name, final int baseTypeId,
      final Integer parentId, final Integer offset, final Integer argument,
      final Integer numberOfElements) {
    this.id = id;
    this.name = Preconditions.checkNotNull(name, "Error: name can not be null");
    this.baseTypeId = baseTypeId;
    this.parentId = parentId;
    Preconditions.checkArgument(offset == null || offset >= 0, "Error: offset must be positive.");
    this.offset = Optional.fromNullable(offset);
    this.argument = Optional.fromNullable(argument);
    Preconditions.checkArgument(numberOfElements == null || numberOfElements >= 0,
        "Error: number of elements must be null or greater than zero.");
    Preconditions.checkArgument(
        !Objects.equals(numberOfElements, offset) ^ this.argument.isPresent(),
        "Error: Either this is a struct member or a array member, or a prototype argument.");
    this.numberOfElements = Optional.fromNullable(numberOfElements);
  }

  public Optional<Integer> getArgumentIndex() {
    return argument;
  }

  public int getBaseTypeId() {
    return baseTypeId;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Optional<Integer> getNumberOfElements() {
    return numberOfElements;
  }

  public Optional<Integer> getOffset() {
    return offset;
  }

  public Integer getParentId() {
    return parentId;
  }

  @Override
  public String toString() {
    return String.format(
        "name=%s, id=%d, offset=%d, numberOfElements=%d, parentId=%d, baseTypeId=%d, argument=%d",
        name, id, offset, numberOfElements, parentId, baseTypeId, argument);
  }
}
