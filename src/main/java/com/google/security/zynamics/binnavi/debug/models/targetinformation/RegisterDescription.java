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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

import com.google.common.base.Preconditions;

/**
 * This class is used to describe a CPU register. This information is necessary for displaying and
 * updating registers.
 */
public final class RegisterDescription {
  /**
   * Name of the described register.
   */
  private final String name;

  /**
   * Size of the register in bytes.
   */
  private final int byteSize;

  /**
   * Flag that determines whether the register is editable or not.
   */
  private final boolean isEditable;

  /**
   * Creates a new register description object.
   *
   * @param name Name of the described register.
   * @param size Size of the register in bytes.
   * @param editable Flag that determines whether the register is editable or not.
   */
  public RegisterDescription(final String name, final int size, final boolean editable) {
    this.name = Preconditions.checkNotNull(name, "IE01031: Name argument can not be null");
    if (!isValidSize(size)) {
      throw new IllegalArgumentException(String.format("IE01032: Invalid register size %d", size));
    }
    byteSize = size;
    isEditable = editable;
  }

  /**
   * Determines whether an integer value is a valid register size.
   *
   * @param size The size value to check.
   *
   * @return True, if the value is a valid register size value. False, otherwise.
   */
  private static boolean isValidSize(final int size) {
    return size == 0 || size == 1 || size == 2 || size == 4 || size == 8;
  }

  /**
   * Returns the name of the described register.
   *
   * @return The name of the described register.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the size in bytes of the described register.
   *
   * @return The size in bytes of the described register.
   */
  public int getSize() {
    return byteSize;
  }

  /**
   * Returns a flag that says whether the register is editable or not.
   *
   * @return True, if the register is editable. False, otherwise.
   */
  public boolean isEditable() {
    return isEditable;
  }
}
