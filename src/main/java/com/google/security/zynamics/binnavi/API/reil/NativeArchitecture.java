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
package com.google.security.zynamics.binnavi.API.reil;

/* ! \file NativeArchitecture.java \brief Contains the NativeArchitecture enumeration * */

/**
 * Architecture identifier that is used to specify the source architecture of input code that is
 * translated to REIL code.
 */
public enum NativeArchitecture {
  /**
   * Identifies input code as 32bit x86 code
   */
  X86_32,

  /**
   * Identifies input code as 32bit PowerPC code
   */
  PPC_32,

  /**
   * Identifies input code as 32bit ARM code
   */
  ARM_32,

  /**
   * Identifies input code as REIL code
   */
  REIL;
}
