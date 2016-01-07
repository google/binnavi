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
package com.google.security.zynamics.reil.translators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;


public final class BasicBlockGenerator {
  private List<ReilInstruction> currentBlock = new ArrayList<ReilInstruction>();
  private final List<ReilBlock> blocks = new ArrayList<ReilBlock>();

  private final ArrayList<Triple<ReilBlock, IAddress, EdgeType>> edgepairs =
      new ArrayList<Triple<ReilBlock, IAddress, EdgeType>>();

  public BasicBlockGenerator(final Collection<List<ReilInstruction>> instructionList,
      final Collection<IAddress> nativeJumpTargets) {
    for (final List<ReilInstruction> instructions : instructionList) {
      final HashSet<IAddress> jumpTargets = fillJumpTargets(instructions);
      jumpTargets.addAll(nativeJumpTargets);

      final ReilInstruction lastInstruction = Iterables.getLast(instructions);

      for (final ReilInstruction reilInstruction : instructions) {
        addInstruction(reilInstruction, jumpTargets, lastInstruction);
      }

      if (currentBlock.size() != 0) {
        final ReilBlock reilBlock = new ReilBlock(currentBlock);

        blocks.add(reilBlock);

        currentBlock = new ArrayList<ReilInstruction>();
      }
    }
  }

  /**
   * Takes a list of REIL instructions and tries to deduces as many jump targets as possible from
   * them.
   * 
   * @param reilInstructions A list of REIL instructions.
   * 
   * @return A list of jump target addresses that were deduced from the instructions.
   */
  private static HashSet<IAddress> fillJumpTargets(
      final Collection<ReilInstruction> reilInstructions) {
    final HashSet<IAddress> jumpTargets = new HashSet<IAddress>();

    for (final ReilInstruction reilInstruction : reilInstructions) {
      if (reilInstruction.getMnemonic().equals(ReilHelpers.OPCODE_JCC)) {
        final String jumpTarget = reilInstruction.getThirdOperand().getValue();

        if (Convert.isDecString(jumpTarget)) {
          jumpTargets.add(toReilAddress(jumpTarget));
        } else if (reilInstruction.getThirdOperand().getType() == OperandType.SUB_ADDRESS) {
          jumpTargets.add(toReilAddress(jumpTarget.split("\\.")));
        }
      }
    }

    return jumpTargets;
  }

  private static IAddress toReilAddress(final String addressString) {
    return ReilHelpers.toReilAddress(new CAddress(Long.valueOf(addressString)));
  }

  private static IAddress toReilAddress(final String[] parts) {
    return new CAddress(toReilAddress(parts[0]).toLong() + Long.valueOf(parts[1]));
  }

  private void addInstruction(final ReilInstruction reilInstruction,
      final HashSet<IAddress> jumpTargets, final ReilInstruction lastInstruction) {
    if (jumpTargets.contains(reilInstruction.getAddress()) && (currentBlock.size() != 0)) {
      final ReilBlock reilBlock = new ReilBlock(currentBlock);

      // final IAddress blockAddress = reilBlock.getAddress();

      blocks.add(reilBlock);

      // if ((reilBlock.getAddress().toLong() & 0xFFFFFFFFFFFFFF00L) ==
      // (reilInstruction.getAddress().toLong() & 0xFFFFFFFFFFFFFF00L))
      {
        edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock, reilInstruction
            .getAddress(), EdgeType.JUMP_UNCONDITIONAL));
      }

      currentBlock = new ArrayList<ReilInstruction>();
    }

    currentBlock.add(reilInstruction);

    if (reilInstruction.getMnemonic().equals(ReilHelpers.OPCODE_JCC)
        && (ReilHelpers.isDelayedBranch(reilInstruction) || (reilInstruction != lastInstruction))) {
      // Every JCC instruction finishes a block. We skip the last instruction of a block
      // because those edges already exist in the native edge set.
      //
      // Delayed branches also finish a block, at least as far as edge creation goes.

      final ReilBlock reilBlock = new ReilBlock(currentBlock);

      blocks.add(reilBlock);
      currentBlock = new ArrayList<ReilInstruction>();

      final String jumpTarget = reilInstruction.getThirdOperand().getValue();

      if (ReilHelpers.isConditionalJump(reilInstruction)) {
        // If we have a conditional jump we have to add two edges.

        edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock, null,
            EdgeType.JUMP_CONDITIONAL_FALSE));

        if (Convert.isDecString(jumpTarget)) {
          edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock,
              toReilAddress(jumpTarget), EdgeType.JUMP_CONDITIONAL_TRUE));
        } else if (reilInstruction.getThirdOperand().getType() == OperandType.SUB_ADDRESS) {
          final String[] parts = jumpTarget.split("\\.");

          edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock, toReilAddress(parts),
              EdgeType.JUMP_CONDITIONAL_TRUE));
        }
      } else if (ReilHelpers.isFunctionCall(reilInstruction)) {
        edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock, null,
            EdgeType.JUMP_UNCONDITIONAL));
      } else if (Convert.isDecString(jumpTarget)) {
        edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock,
            toReilAddress(jumpTarget), EdgeType.JUMP_UNCONDITIONAL));
      } else if (reilInstruction.getThirdOperand().getType() == OperandType.SUB_ADDRESS) {
        final String[] parts = jumpTarget.split("\\.");

        edgepairs.add(new Triple<ReilBlock, IAddress, EdgeType>(reilBlock, toReilAddress(parts),
            EdgeType.JUMP_UNCONDITIONAL));
      }
    }
  }

  public List<ReilBlock> getBlocks() {
    return new ArrayList<ReilBlock>(blocks);
  }

  public ArrayList<Triple<ReilBlock, IAddress, EdgeType>> getEdges() {
    return new ArrayList<Triple<ReilBlock, IAddress, EdgeType>>(edgepairs);
  }
}
