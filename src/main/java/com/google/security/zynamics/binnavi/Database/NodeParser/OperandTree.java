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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple helper class that is used to build operand trees during code node parsing.
 */
public final class OperandTree {
  /**
   * ID of the operand.
   */
  private final int m_id;

  /**
   * All nodes of the operand tree.
   */
  private final List<OperandTreeNode> m_nodes = new ArrayList<OperandTreeNode>();

  /**
   * Creates a new operand tree.
   * 
   * @param operandId ID of the operand.
   */
  public OperandTree(final int operandId) {
    m_id = operandId;
  }

  /**
   * Returns the ID of the operand.
   * 
   * @return The ID of the operand.
   */
  public int getId() {
    return m_id;
  }

  /**
   * Returns the nodes of the operand tree.
   * 
   * @return The nodes of the operand tree.
   */
  public List<OperandTreeNode> getNodes() {
    // ESCA-JAVA0259: Return of collection is OK, speed is more important
    // when creating nodes.
    return m_nodes;
  }
}
