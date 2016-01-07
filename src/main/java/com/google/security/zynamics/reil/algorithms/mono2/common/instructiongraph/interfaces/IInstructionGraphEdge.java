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
package com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces;

public interface IInstructionGraphEdge {

  /**
   * Specifies whether this edge marks a transition between two REIL instructions which have been
   * translated from different architecture specific instructions.
   * 
   * @return True if this edge is exiting a REIL block belonging to an architecture specific
   *         instruction.
   */
  public boolean isInstructionExit();

  // TODO: These methods might be interesting in the future, so we keep them here as a reminder.
  // public boolean isBasicblockExit();
  // public boolean isFunctionExit();

  /**
   * Specifies whether this edge is an true edge.
   * 
   * @return True if edge is the true edge of a conditional.
   */
  public boolean isTrue();
}
