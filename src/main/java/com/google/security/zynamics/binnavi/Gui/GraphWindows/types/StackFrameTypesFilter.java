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
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;

/**
 * Only includes a single stack frame type and all transitively reachable types when these are
 * updated in the type system.
 */
class StackFrameTypesFilter implements TypesFilter {

  private final BaseType stackFrameType;
  private final TypeManager typeManager;

  public StackFrameTypesFilter(final BaseType stackFrameType, TypeManager typeManager) {
    this.stackFrameType = stackFrameType;
    this.typeManager = typeManager;
  }

  @Override
  public boolean includeType(final BaseType baseType) {
    return baseType == stackFrameType;
  }

  @Override
  public boolean includeUpdatedType(BaseType baseType) {
    return baseType == stackFrameType || typeManager.isContainedIn(stackFrameType, baseType);
  }
}
