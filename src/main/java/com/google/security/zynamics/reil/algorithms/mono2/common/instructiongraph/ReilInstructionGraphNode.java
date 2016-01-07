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

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphNode;

public class ReilInstructionGraphNode implements IInstructionGraphNode {
  /**
   * The REIL instruction stored in the REIL instruction graph node.
   */
  ReilInstruction m_instruction;

  public ReilInstructionGraphNode(final ReilInstruction instruction) {
    m_instruction = instruction;
  }

  @Override
  public ReilInstruction getReilInstruction() {
    return m_instruction;
  }
}
