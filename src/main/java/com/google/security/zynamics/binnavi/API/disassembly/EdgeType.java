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

/* ! \file EdgeType.java \brief Contains the EdgeType enumeration * */

// / Used to determine the type of an edge.
/**
 * Each edge of a graph must have a type.
 */
public enum EdgeType {
  /**
   * The True branch of a conditional jump.
   */
  JumpConditionalTrue,

  /**
   * The False branch of a conditional jump.
   */
  JumpConditionalFalse,

  /**
   * Unconditional jump.
   */
  JumpUnconditional,

  /**
   * Edge inside a Switch statement.
   */
  JumpSwitch,

  /**
   * True branch of a conditional jump inside a loop.
   */
  JumpConditionalTrueLoop,

  /**
   * False branch of a conditional jump inside a loop.
   */
  JumpConditionalFalseLoop,

  /**
   * Unconditional jump inside a loop.
   */
  JumpUnconditionalLoop,

  /**
   * Edge that enters an inlined function.
   */
  EnterInlinedFunction,

  /**
   * Edge that leaves an inlined function.
   */
  LeaveInlinedFunction,

  /**
   * Edge that connects a comment node with another node.
   */
  TextNode;

  // / @cond INTERNAL
  /**
   * Converts an internal edge type to an API edge type.
   *
   * @param type The edge type to convert.
   *
   * @return The converted edge type.
   */
  public static EdgeType convert(
      final com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType type) {
    switch (type) {
      case ENTER_INLINED_FUNCTION:
        return EdgeType.EnterInlinedFunction;
    // case INTER_ADDRESSSPACE_EDGE: return EdgeType.InterAddressSpace;
    // case INTER_MODULE: return EdgeType.InterModule;
      case JUMP_CONDITIONAL_FALSE:
        return EdgeType.JumpConditionalFalse;
      case JUMP_CONDITIONAL_FALSE_LOOP:
        return EdgeType.JumpConditionalFalseLoop;
      case JUMP_CONDITIONAL_TRUE:
        return EdgeType.JumpConditionalTrue;
      case JUMP_CONDITIONAL_TRUE_LOOP:
        return EdgeType.JumpConditionalTrueLoop;
      case JUMP_SWITCH:
        return EdgeType.JumpSwitch;
      case JUMP_UNCONDITIONAL:
        return EdgeType.JumpUnconditional;
      case JUMP_UNCONDITIONAL_LOOP:
        return EdgeType.JumpUnconditionalLoop;
      case LEAVE_INLINED_FUNCTION:
        return EdgeType.LeaveInlinedFunction;
      case TEXTNODE_EDGE:
        return EdgeType.TextNode;
      default:
        throw new IllegalStateException("Error: Unknown edge type");
    }
  }

  /**
   * Converts an API edge type to an internal edge type.
   *
   * @return The internal edge type.
   */
  public com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType getNative() {
    switch (this) {
      case JumpConditionalTrue:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_CONDITIONAL_TRUE;
      case JumpConditionalFalse:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_CONDITIONAL_FALSE;
      case JumpUnconditional:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_UNCONDITIONAL;
      case JumpSwitch:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_SWITCH;
      case JumpConditionalTrueLoop:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_CONDITIONAL_TRUE_LOOP;
      case JumpConditionalFalseLoop:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_CONDITIONAL_FALSE_LOOP;
      case JumpUnconditionalLoop:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_UNCONDITIONAL_LOOP;
      case EnterInlinedFunction:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.ENTER_INLINED_FUNCTION;
      case LeaveInlinedFunction:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.LEAVE_INLINED_FUNCTION;
    // case InterModule: return
    // com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.INTER_MODULE;
    // case InterAddressSpace: return
    // com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.INTER_ADDRESSSPACE_EDGE;
      case TextNode:
        return com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.TEXTNODE_EDGE;
      default:
        throw new IllegalStateException("Error: Unknown edge type");
    }
  }
}
