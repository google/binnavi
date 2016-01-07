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
package com.google.security.zynamics.binnavi.disassembly.algorithms;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import java.awt.Color;

/**
 * Helper class for finding the right color of a new function node.
 */
public final class CFunctionNodeColorizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CFunctionNodeColorizer() {
  }

  /**
   * Returns the proper color for function nodes of a given type.
   * 
   * @param type The type of the function.
   * 
   * @return The color for function nodes of the given type.
   */
  public static Color getFunctionColor(final FunctionType type) {
    Preconditions.checkNotNull(type, "IE02200: Type argument cannot be null");

    switch (type) {
      case ADJUSTOR_THUNK:
        return ConfigManager.instance().getColorSettings().getAdjustorThunkFunctionColor();
      case IMPORT:
        return ConfigManager.instance().getColorSettings().getImportedFunctionColor();
      case LIBRARY:
        return ConfigManager.instance().getColorSettings().getLibraryFunctionColor();
      case NORMAL:
        return ConfigManager.instance().getColorSettings().getNormalFunctionColor();
      case THUNK:
        return ConfigManager.instance().getColorSettings().getThunkFunctionColor();
      case UNKNOWN:
        return ConfigManager.instance().getColorSettings().getUnknownFunctionColor();
      default:
        throw new IllegalStateException(String.format("IE00908: Unknown function type %s", type));
    }
  }
}
