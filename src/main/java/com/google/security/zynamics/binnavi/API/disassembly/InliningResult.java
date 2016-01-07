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

// ! Stores the result of an inlining operation.
/**
 * Small helper class that gives additional information about the result of an inlining operation.
 */
public final class InliningResult {
  /**
   * Upper part of the split code node.
   */
  private final CodeNode m_firstNode;

  /**
   * Lower part of the split code node.
   */
  private final CodeNode m_secondNode;

  // / @cond INTERNAL
  /**
   * Creates a new inlining result object.
   *
   * @param firstNode Upper part of the split code node.
   * @param secondNode Lower part of the split code node.
   */
  // / @endcond
  public InliningResult(final CodeNode firstNode, final CodeNode secondNode) {
    m_firstNode = firstNode;
    m_secondNode = secondNode;
  }

  // ! Upper part of the split code node.
  /**
   * During the inline operation, the code node where the inling operation happens is typically
   * split into two parts. This function returns the upper part of the inlined code node.
   *
   *  If splitting the node was not necessary because the call instruction was the last instruction
   * of the code node, this function returns the original code node where the inlining operation
   * happened.
   *
   * @return The upper part of the inlined code node.
   */
  public CodeNode getFirstNode() {
    return m_firstNode;
  }

  // ! Lower part of the split code node.
  /**
   * During the inline operation, the code node where the inling operation happens is typically
   * split into two parts. This function returns the lower part of the inlined code node.
   *
   *  If splitting the node was not necessary because the call instruction was the last instruction
   * of the code node, this function returns null.
   *
   * @return The lower part of the inlined code node.
   */
  public CodeNode getSecondNode() {
    return m_secondNode;
  }
}
