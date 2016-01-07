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

import com.google.common.collect.ImmutableMap;

/**
 * Specifies the category of a given BaseType instance. The categories carry the same semantics as
 * their corresponding types in a C type system. The string values correspond to the enum
 * representation in the database.
 */
public enum BaseTypeCategory {
  /**
   * An atomic, i.e. built-in non-compound.
   */
  ATOMIC("atomic"),
  /**
   * An array type, i.e. a composition of the same type with a fixed number of elements.
   */
  ARRAY("array"),
  /**
   * A pointer type.
   */
  POINTER("pointer"),
  /**
   * A structure, i.e. user defined compound type.
   */
  STRUCT("struct"),
  /**
   * A union type in the sense of a C type system union.
   */
  UNION("union"),
  /**
   * A function prototype.
   */
  FUNCTION_PROTOTYPE("function_pointer");

  private final String value;
  private static final ImmutableMap<String, BaseTypeCategory> lookup = createLookup();

  private BaseTypeCategory(final String value) {
    this.value = value;
  }

  private static ImmutableMap<String, BaseTypeCategory> createLookup() {
    final ImmutableMap.Builder<String, BaseTypeCategory> builder = ImmutableMap.builder();
    for (final BaseTypeCategory category : BaseTypeCategory.values()) {
      builder.put(category.value, category);
    }
    return builder.build();
  }

  /**
   * Returns the enum value corresponding to the given string or null if no such value exists.
   *
   * @param value The string value to translate.
   * @return The corresponding enum value or null.
   */
  public static BaseTypeCategory fromString(final String value) {
    return lookup.get(value);
  }

  public static boolean isOffsetCategory(final BaseTypeCategory category) {
    return category == BaseTypeCategory.STRUCT || category == BaseTypeCategory.UNION;
  }

  @Override
  public String toString() {
    return value;
  }
}
