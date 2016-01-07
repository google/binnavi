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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component;

import com.google.security.zynamics.zylib.disassembly.FunctionType;

/**
 * Pair of function types and names that is used to sort function tables in a special way.
 */
public final class CFunctionNameTypePair {
  /**
   * Name of a function.
   */
  private final String m_name;

  /**
   * Type of a function.
   */
  private final FunctionType m_functionType;

  /**
   * Creates a new pair of function name and function type.
   * 
   * @param name Name of a function.
   * @param functionType Type of a function.
   */
  public CFunctionNameTypePair(final String name, final FunctionType functionType) {
    m_name = name;
    m_functionType = functionType;
  }

  /**
   * Returns the function type.
   * 
   * @return The function type.
   */
  public FunctionType getFunctionType() {
    return m_functionType;
  }

  /**
   * Returns the function name.
   * 
   * @return The function name.
   */
  public String getName() {
    return m_name;
  }

  @Override
  public String toString() {
    return m_name;
  }
}
