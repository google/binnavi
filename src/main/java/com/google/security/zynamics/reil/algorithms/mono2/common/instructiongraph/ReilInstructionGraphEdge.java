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
package com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph;

import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphEdge;

public class ReilInstructionGraphEdge implements IInstructionGraphEdge {
  /**
   * Boolean to determine if the REIL instruction graph edge is an unconditional edge.
   */
  private final boolean isTrue;

  /**
   * Boolean to determine if the REIL instruction graph edge is an native instruction exit edge.
   */
  private final boolean isExit;

  /**
   * Constructor for an instruction graph edge.
   * 
   * @param isTrue Boolean to determine if the edge is an unconditional edge.
   * @param isExit Boolean to determine if the edge is an native instruction exit edge.
   */
  public ReilInstructionGraphEdge(final boolean isTrue, final boolean isExit) {
    this.isTrue = isTrue;
    this.isExit = isExit;
  }

  @Override
  public boolean isInstructionExit() {
    return isExit;
  }

  @Override
  public boolean isTrue() {
    return isTrue;
  }
}
