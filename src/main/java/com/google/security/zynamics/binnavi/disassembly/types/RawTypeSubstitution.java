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
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Represents raw database records which correspond to TypeSubstitution objects. This class is only
 * used when loading type substitutions from the database and shouldn't be used elsewhere.
 */
public final class RawTypeSubstitution {
  private final IAddress address;
  private final int position;
  private final int expressionId;
  private final int baseTypeId;
  private final Integer[] path;
  private final Integer offset;

  /**
   * Creates a new raw type substitution.
   *
   * @param expressionId The expression id of the corresponding operand tree node.
   * @param address The address of the instruction that is annotated with a type substitution.
   * @param position The zero-based index position of the operand within its instruction.
   * @param offset An additional offset (in bits) relative to the beginning of the base type.
   * @param baseTypeId The base type id referring to the actual base type of the substitution.
   */
  public RawTypeSubstitution(final IAddress address, final int position, final int expressionId,
      final int baseTypeId, final Integer[] path, final Integer offset) {
    this.address = Preconditions.checkNotNull(address, "Error: address can not be null.");
    Preconditions.checkArgument(position >= 0, "Error: position must be positive.");
    this.position = position;
    this.expressionId = expressionId;
    this.baseTypeId = baseTypeId;
    this.path = Preconditions.checkNotNull(path, "Error: path argument can not be null.");
    this.offset = offset;
  }

  public IAddress getAddress() {
    return address;
  }

  public int getBaseTypeId() {
    return baseTypeId;
  }

  public int getExpressionId() {
    return expressionId;
  }

  public Integer getOffset() {
    return offset;
  }

  public Integer[] getPath() {
    return path;
  }

  public int getPosition() {
    return position;
  }
}
