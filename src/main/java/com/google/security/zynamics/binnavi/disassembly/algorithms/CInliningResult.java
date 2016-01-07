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

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;

/**
 * Helper class that provides the results of an inlining operation.
 */
public final class CInliningResult {
  /**
   * Entry node of the inlined function.
   */
  private final INaviCodeNode m_firstNode;

  /**
   * Exit node of the inlined function.
   */
  private final INaviCodeNode m_returnNode;

  /**
   * Creates a new inlining result object.
   * 
   * @param firstNode Entry node of the inlined function.
   * @param returnNode Exit node of the inlined function.
   */
  public CInliningResult(final INaviCodeNode firstNode, final INaviCodeNode returnNode) {
    m_firstNode = firstNode;
    m_returnNode = returnNode;
  }

  /**
   * Returns the entry node of the inlined function.
   * 
   * @return The entry node of the inlined function.
   */
  public INaviCodeNode getFirstNode() {
    return m_firstNode;
  }

  /**
   * Returns the exit node of the inlined function.
   * 
   * @return The exit node of the inlined function.
   */
  public INaviCodeNode getSecondNode() {
    return m_returnNode;
  }
}
