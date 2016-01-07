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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.disassembly.types.BaseType;

/**
 * Filters all stack frame types and function prototypes but includes the given stack frame type.
 * Includes everything else.
 */
public class LocalTypesFilter implements TypesFilter {

  private final BaseType stackFrameType;

  public LocalTypesFilter(final BaseType stackFrameType) {
    this.stackFrameType = stackFrameType;
  }

  @Override
  public boolean includeType(final BaseType baseType) {
    return baseType == stackFrameType
        || (!baseType.isStackFrame() && !baseType.isFunctionPrototype());
  }

  @Override
  public boolean includeUpdatedType(BaseType baseType) {
    return baseType == stackFrameType
        || (!baseType.isStackFrame() && !baseType.isFunctionPrototype());
  }
}
