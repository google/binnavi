/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.Map;


import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.Modules.CFunctionContainer;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;
import com.google.security.zynamics.zylib.types.lists.FilledList;

/**
 * Simple helper function to modify the internal state of CFunctionContainer via reflection.
 */
public class CFunctionContainerHelper {
  private CFunctionContainerHelper() {
  }

  @SuppressWarnings("unchecked")
  public static void addFunction(final CFunctionContainer container, final INaviFunction function)
      throws IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    final FilledList<INaviFunction> functions =
        (FilledList<INaviFunction>) ReflectionHelpers.getField(CFunctionContainer.class, container,
            "m_functions");
    final Map<IAddress, INaviFunction> functionMap =
        (Map<IAddress, INaviFunction>) ReflectionHelpers.getField(CFunctionContainer.class,
            container, "m_functionMap");
    functions.add(function);
    functionMap.put(function.getAddress(), function);
  }
}
