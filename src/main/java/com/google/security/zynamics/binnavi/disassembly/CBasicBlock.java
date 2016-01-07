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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.NodeParser.IInstructionContainer;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Represents a basic block in flow graphs.
 */
public final class CBasicBlock implements INaviBasicBlock, IInstructionContainer {
  /**
   * The ID of the basic block.
   */
  private final int m_id;

  /**
   * The global comment of the basic block.
   */
  private final String m_globalComment;

  /**
   * List of instructions of the basic block.
   */
  private final List<INaviInstruction> m_instructions;

  /**
   * Creates a new basic block object.
   * 
   * @param blockId The ID of the basic block.
   * @param globalComment The global comment of the basic block.
   * @param instructions The
   */
  public CBasicBlock(final int blockId, final String globalComment,
      final List<INaviInstruction> instructions) {
    Preconditions.checkArgument(blockId >= 0, "IE00050: Basic Block IDs can not be negative");
    m_globalComment =
        Preconditions.checkNotNull(globalComment, "IE00052: Global comment can not be null");
    m_instructions =
        Preconditions.checkNotNull(instructions, "IE01783: Instructions argument can not be null");

    m_id = blockId;
  }

  @Override
  public IAddress getAddress() {
    return m_instructions.get(0).getAddress();
  }

  @Override
  public String getGlobalComment() {
    return m_globalComment;
  }

  @Override
  @Deprecated
  // Returns wrong results
  public int getId() {
    return m_id;
  }

  @Override
  public List<INaviInstruction> getInstructions() {
    return Collections.unmodifiableList(m_instructions);
  }

  @Override
  public Iterator<INaviInstruction> iterator() {
    return m_instructions.iterator();
  }
}
