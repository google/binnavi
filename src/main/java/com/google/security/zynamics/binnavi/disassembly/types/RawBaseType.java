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
import java.util.Objects;

/**
 * Represents raw database records which correspond to BaseType objects. This class is only used
 * when loading types from the database and shouldn't be used elsewhere.
 */
public final class RawBaseType {

  private final String name;
  private final int id;
  private final Integer pointerId;
  private final boolean isSigned;
  private final int size;
  private final BaseTypeCategory category;

  public RawBaseType(final int id, final String name, final int size, final Integer pointerId,
      final boolean isSigned, final BaseTypeCategory category) {
    this.id = id;
    this.name = Preconditions.checkNotNull(name, "Error: name can not be null.");
    this.size = size;
    this.pointerId = pointerId;
    this.isSigned = isSigned;
    this.category = category;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    final RawBaseType other = (RawBaseType) object;
    return Objects.equals(id, other.id) && Objects.equals(name, other.name)
        && Objects.equals(pointerId, other.pointerId) && Objects.equals(size, other.size)
        && Objects.equals(isSigned, other.isSigned) && Objects.equals(category, other.category);
  }

  public BaseTypeCategory getCategory() {
    return category;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getPointerId() {
    return pointerId;
  }

  public int getSize() {
    return size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.name, this.pointerId, this.size, this.isSigned);
  }

  public boolean isSigned() {
    return isSigned;
  }

  @Override
  public String toString() {
    return name;
  }
}
