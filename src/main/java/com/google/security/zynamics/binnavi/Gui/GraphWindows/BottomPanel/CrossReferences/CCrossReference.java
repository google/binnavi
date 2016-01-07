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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import com.google.security.zynamics.binnavi.disassembly.INaviFunction;

/**
 * Represents a single cross reference.
 */
public final class CCrossReference {
  /**
   * Calling function.
   */
  private final INaviFunction m_sourceFunction;

  /**
   * Called function.
   */
  private final INaviFunction m_targetFunction;

  /**
   * Creates a new cross reference object.
   *
   * @param sourceFunction Calling function.
   * @param targetFunction Called function.
   */
  public CCrossReference(final INaviFunction sourceFunction, final INaviFunction targetFunction) {
    m_sourceFunction = sourceFunction;
    m_targetFunction = targetFunction;
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof CCrossReference)) {
      return false;
    }

    final CCrossReference rhsRef = (CCrossReference) rhs;

    return m_sourceFunction == rhsRef.m_sourceFunction
        && m_targetFunction == rhsRef.m_targetFunction;
  }

  /**
   * Returns the called function.
   *
   * @return The called function.
   */
  public INaviFunction getCalledFunction() {
    return m_targetFunction;
  }

  /**
   * Returns the calling function.
   *
   * @return The calling function.
   */
  public INaviFunction getCallingFunction() {
    return m_sourceFunction;
  }

  @Override
  public int hashCode() {
    return m_sourceFunction.hashCode() * m_targetFunction.hashCode();
  }
}
