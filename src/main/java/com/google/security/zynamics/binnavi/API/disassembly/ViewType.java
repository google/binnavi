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

/* ! \file ViewType.java \brief Contains the ViewType enumeration * */

/**
 * Describes the type of views.
 */
public enum ViewType {
  /***
   * Identifies views as native views. These views are created during the initial import process
   * when a module is first converted from a raw module into a BinNavi module. Native views can not
   * be modified.
   */
  Native,

  /**
   * Identifies views as non-native views. These views are user-created views that can be modified.
   */
  NonNative;

  // / @cond INTERNAL
  /**
   * Converts an internal view type to an API view type.
   *
   * @param type The view type to convert.
   *
   * @return The converted view type.
   */
  public static ViewType convert(final com.google.security.zynamics.zylib.disassembly.ViewType type) {
    switch (type) {
      case Native:
        return Native;
      case NonNative:
        return NonNative;
      default:
        throw new IllegalArgumentException("Error: Unknown view type");
    }
  }

  /**
   * Converts an API view type to an internal view type.
   *
   * @return The internal view type.
   */
  // / @endcond
  public final com.google.security.zynamics.zylib.disassembly.ViewType getNative() {
    switch (this) {
      case Native:
        return com.google.security.zynamics.zylib.disassembly.ViewType.Native;
      case NonNative:
        return com.google.security.zynamics.zylib.disassembly.ViewType.NonNative;
      default:
        throw new IllegalArgumentException("Error: Unknown view type");
    }
  }
}
