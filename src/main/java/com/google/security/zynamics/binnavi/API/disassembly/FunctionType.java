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

/* ! \file FunctionType.java \brief Contains the FunctionType enumeration * */

/**
 * Describes the type of functions.
 */
public enum FunctionType {
  /**
   * Normal function
   */
  Normal,

  /**
   * Function that was identified to be part of some standard library
   */
  Library,

  /**
   * Function that is dynamically imported from an external module
   */
  Import,

  /**
   * Function that merely jumps to another function
   */
  Thunk,

  /**
   * Adjustor thunk function
   */
  AdjustorThunk,

  /**
   * An incomplete or invalid function.
   */
  Invalid,

  /**
   * Function whose type is not known
   */
  Unknown;

  // / @cond INTERNAL
  /**
   * Converts an internal function type to an API function type.
   *
   * @param type The function type to convert.
   *
   * @return The converted function type.
   */
  public static FunctionType convert(final com.google.security.zynamics.zylib.disassembly.FunctionType type) {
    switch (type) {
      case NORMAL:
        return Normal;
      case LIBRARY:
        return Library;
      case IMPORT:
        return Import;
      case THUNK:
        return Thunk;
      case ADJUSTOR_THUNK:
        return AdjustorThunk;
      case INVALID:
        return Invalid;
      case UNKNOWN:
        return Unknown;
      default:
        throw new IllegalArgumentException("Error: Unknown function type");
    }
  }

  /**
   * Converts an API function type to an internal function type.
   *
   * @return The internal function type.
   */
  // / @endcond
  public com.google.security.zynamics.zylib.disassembly.FunctionType getNative() {
    switch (this) {
      case Normal:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.NORMAL;
      case Library:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.LIBRARY;
      case Import:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.IMPORT;
      case Thunk:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.THUNK;
      case AdjustorThunk:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.ADJUSTOR_THUNK;
      case Invalid:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.INVALID;
      case Unknown:
        return com.google.security.zynamics.zylib.disassembly.FunctionType.UNKNOWN;
      default:
        throw new IllegalArgumentException("Error: Unknown function type");
    }
  }
}
