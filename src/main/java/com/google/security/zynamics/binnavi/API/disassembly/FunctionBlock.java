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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

// / Represents a single block of a function.
/**
 * Represents a function in a callgraph.
 */
public final class FunctionBlock implements IGraphNode<FunctionBlock> {
  /**
   * API function represented by this function block.
   */
  private final Function m_function;

  /**
   * Children of the block in the graph where the block resides.
   */
  private final List<FunctionBlock> m_children = new ArrayList<FunctionBlock>();

  /**
   * Parents of the block in the graph where the block resides.
   */
  private final List<FunctionBlock> m_parents = new ArrayList<FunctionBlock>();

  // / @cond INTERNAL
  /**
   * Creates a new function block object backed by an API function object.
   *
   * @param function The function object represented by the function block object.
   */
  FunctionBlock(final Function function) {
    m_function = Preconditions.checkNotNull(function, "Error: Function argument can not be null");
  }

  /**
   * Links a parent block with a child block.
   *
   * @param parent The parent block that is added to the parent list of the child.
   * @param child The child block that is added to the child list of the parent.
   */
  // / @endcond
  static void link(final FunctionBlock parent, final FunctionBlock child) {
    Preconditions.checkNotNull(parent, "Error: Parent argument can not be null");
    Preconditions.checkNotNull(child, "Error: Child argument can not be null");

    parent.m_children.add(child);
    child.m_parents.add(parent);
  }

  // ! Children of the function block.
  /**
   * Returns all child blocks of the block.
   *
   * @return A list of blocks.
   */
  @Override
  public List<FunctionBlock> getChildren() {
    return new ArrayList<FunctionBlock>(m_children);
  }

  // ! Function represented by the function block.
  /**
   * Returns the function represented by the block.
   *
   * @return The function represented by the block.
   */
  public Function getFunction() {
    return m_function;
  }

  // ! Parents of the function block.
  /**
   * Returns all parent blocks of the block.
   *
   * @return A list of blocks.
   */
  @Override
  public List<FunctionBlock> getParents() {
    return new ArrayList<FunctionBlock>(m_parents);
  }

  // ! Printable representation of the function block.
  /**
   * Returns the string representation of the function block.
   *
   * @return The string representation of the function block.
   */
  @Override
  public String toString() {
    return String.format("Function Block ['%s']", m_function.toString());
  }
}
