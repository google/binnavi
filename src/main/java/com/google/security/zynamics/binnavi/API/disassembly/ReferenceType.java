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
package com.google.security.zynamics.binnavi.API.disassembly;

/* ! \file ReferenceType.java \brief Contains the ReferenceType enumeration * */

// / Describes the types of references.
/**
 * Describes the type of references.
 */
public enum ReferenceType {
  CONDITIONAL_TRUE,
  CONDITIONAL_FALSE,
  UNCONDITIONAL,
  SWITCH,
  CALL_DIRECT,
  CALL_INDIRECT,
  CALL_VIRTUAL,
  DATA,
  DATA_STRING;

  // / @cond INTERNAL
  /**
   * Converts an internal reference type to an API reference type.
   *
   * @param type The reference type to convert.
   *
   * @return The converted reference type.
   */
  public static ReferenceType convert(
      final com.google.security.zynamics.zylib.disassembly.ReferenceType type) {
    if (type.compareTo(
        com.google.security.zynamics.zylib.disassembly.ReferenceType.CONDITIONAL_TRUE) == 0) {
      return CONDITIONAL_TRUE;
    }
    if (type.compareTo(
        com.google.security.zynamics.zylib.disassembly.ReferenceType.CONDITIONAL_FALSE) == 0) {
      return CONDITIONAL_FALSE;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.UNCONDITIONAL)
        == 0) {
      return UNCONDITIONAL;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.SWITCH) == 0) {
      return SWITCH;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.CALL_DIRECT)
        == 0) {
      return CALL_DIRECT;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.CALL_INDIRECT)
        == 0) {
      return CALL_INDIRECT;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.CALL_VIRTUAL)
        == 0) {
      return CALL_VIRTUAL;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.DATA) == 0) {
      return DATA;
    }
    if (type.compareTo(com.google.security.zynamics.zylib.disassembly.ReferenceType.DATA_STRING)
        == 0) {
      return DATA_STRING;
    } else {
      throw new IllegalStateException(
          "Error reference type conversion failed due to a wrong type argument");
    }
  }

  /**
   * Indicates whether a reference is a code reference.
   *
   * @param referenceType The reference type.
   *
   * @return true if the reference is a code reference false otherwise.
   */
  public static boolean isCodeReference(final ReferenceType referenceType) {
    return com.google.security.zynamics.zylib.disassembly.ReferenceType.isCodeReference(
        referenceType.getNative());
  }

  /**
   * Indicates whether a reference is a data reference.
   *
   * @param referenceType The reference type.
   *
   * @return true if the reference is a data type reference false otherwise.
   */
  public static boolean isDataReference(final ReferenceType referenceType) {
    return com.google.security.zynamics.zylib.disassembly.ReferenceType.isDataReference(
        referenceType.getNative());
  }

  /**
   * Converts an API reference type to an internal reference type.
   *
   * @return The internal reference type.
   */
  // / @endcond
  public com.google.security.zynamics.zylib.disassembly.ReferenceType getNative() {
    if (this.compareTo(CONDITIONAL_FALSE) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.CONDITIONAL_FALSE;
    }
    if (this.compareTo(CONDITIONAL_TRUE) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.CONDITIONAL_TRUE;
    }
    if (this.compareTo(UNCONDITIONAL) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.UNCONDITIONAL;
    }
    if (this.compareTo(SWITCH) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.SWITCH;
    }
    if (this.compareTo(CALL_DIRECT) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.CALL_DIRECT;
    }
    if (this.compareTo(CALL_INDIRECT) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.CALL_INDIRECT;
    }
    if (this.compareTo(CALL_VIRTUAL) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.CALL_VIRTUAL;
    }
    if (this.compareTo(DATA) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.DATA;
    }
    if (this.compareTo(DATA_STRING) == 0) {
      return com.google.security.zynamics.zylib.disassembly.ReferenceType.DATA_STRING;
    } else {
      throw new IllegalArgumentException(
          "Error reference type conversion failed due to a wrong type argument");
    }
  }
}
