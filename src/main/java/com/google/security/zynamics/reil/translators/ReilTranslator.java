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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilProgram;
import com.google.security.zynamics.reil.translators.arm.TranslatorARM;
import com.google.security.zynamics.reil.translators.mips.TranslatorMIPS;
import com.google.security.zynamics.reil.translators.ppc.TranslatorPPC;
import com.google.security.zynamics.reil.translators.reil.TranslatorREIL;
import com.google.security.zynamics.reil.translators.x86.TranslatorX86;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IBlockContainer;
import com.google.security.zynamics.zylib.disassembly.ICodeContainer;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.common.ICollectionMapper;


/**
 * Translates disassembled programs to REIL code.
 */
public class ReilTranslator<InstructionType extends IInstruction> {
  private final Map<String, ITranslator<InstructionType>> m_translators =
      new HashMap<String, ITranslator<InstructionType>>();

  public ReilTranslator() {
    m_translators.put("X86-32", new TranslatorX86<InstructionType>());
    m_translators.put("ARM-32", new TranslatorARM<InstructionType>());
    m_translators.put("POWERPC-32", new TranslatorPPC<InstructionType>());
    m_translators.put("REIL", new TranslatorREIL<InstructionType>());
    m_translators.put("MIPS-32", new TranslatorMIPS<InstructionType>());
  }

  /**
   * Returns the addresses of all basic blocks of a function. Note that the addresses are already
   * converted to REIL addresses.
   * 
   * @param function The input function.
   * 
   * @return A list of all basic block addresses of the function.
   */
  private static Collection<IAddress> getBlockAddresses(final IBlockContainer<?> function) {
    return CollectionHelpers.map(function.getBasicBlocks(),
        new ICollectionMapper<ICodeContainer<?>, IAddress>() {
          private boolean isDelayedBranch(final ReilInstruction instruction) {
            return instruction.getMnemonic().equals(ReilHelpers.OPCODE_JCC)
                && ReilHelpers.isDelayedBranch(instruction);
          }

          @Override
          public IAddress map(final ICodeContainer<?> block) {
            final IInstruction lastInstruction = Iterables.getFirst(block.getInstructions(), null); // getLastInstruction(block);

            final ReilTranslator<IInstruction> translator = new ReilTranslator<IInstruction>();

            try {
              final ReilGraph reilGraph =
                  translator.translate(new StandardEnvironment(), lastInstruction);

              final ReilBlock lastNode = reilGraph.getNodes().get(reilGraph.getNodes().size() - 1);

              final ReilInstruction lastReilInstruction =
                  Iterables.getLast(lastNode.getInstructions());

              if (isDelayedBranch(lastReilInstruction)) // If branch-delay
              {
                return ReilHelpers.toReilAddress(Iterables.get(block.getInstructions(), 1)
                    .getAddress());
              } else {
                return ReilHelpers.toReilAddress(block.getAddress());
              }
            } catch (final InternalTranslationException e) {
              return ReilHelpers.toReilAddress(block.getAddress());
            }
          }
        });
  }

  /**
   * Returns the first instruction from a code container.
   * 
   * @param container The code container.
   * 
   * @return The first instruction from the code container.
   */
  private static IInstruction getFirstInstruction(final ICodeContainer<?> container) {
    return Iterables.getFirst(container.getInstructions(), null);
  }

  /**
   * Returns the first instruction from a list of REIL instructions.
   * 
   * @param list The list of REIL instructions.
   * 
   * @return The first instruction from the list.
   */
  private static ReilInstruction getFirstInstruction(final List<ReilInstruction> list) {
    return list.get(0);
  }

  /**
   * Returns the last instruction from a code container.
   * 
   * @param container The code container.
   * 
   * @return The last instruction from the code container.
   */
  private static IInstruction getLastInstruction(final ICodeContainer<?> container) {
    return Iterables.getLast(container.getInstructions());
  }

  /**
   * Returns the last instruction from a list of REIL instructions.
   * 
   * @param list The list of REIL instructions.
   * 
   * @return The last instruction from the list.
   */
  private static ReilInstruction getLastInstruction(final List<ReilInstruction> list) {
    return list.get(list.size() - 1);
  }

  /**
   * Returns the REIL block from a list of REIL blocks that starts with a given instruction.
   * 
   * @param instruction The REIL instruction to search for.
   * @param blocks The blocks to search through.
   * 
   * @return The block that starts with the given REIL instruction.
   */
  private static ReilBlock getNode(final ReilInstruction instruction, final List<ReilBlock> blocks) {
    for (final ReilBlock block : blocks) {
      for (final ReilInstruction reilInstruction : block) {
        if (instruction == reilInstruction) {
          return block;
        }
      }
      // if (getFirstInstruction(block) == instruction)
      // {
      // return block;
      // }
    }

    throw new IllegalStateException(String.format(
        "Error: Unknown block (Instruction '%s' not found)", instruction));
  }

  /**
   * This function is responsible for modifying the generated graph at all likely delayed branches.
   * 
   * @param nodes
   * @param edges
   * @param delayedTrueBranches
   */
  private static void handleDelayedTrueBranches(final List<ReilBlock> nodes,
      final List<ReilEdge> edges, final List<List<ReilInstruction>> delayedTrueBranches) {
    for (final List<ReilInstruction> lastReil : delayedTrueBranches) {
      // In this post-processing step we consider all the delayed branches where the delayed
      // instruction is only executed if the branch is taken.
      //
      // We solve the problem by removing the branch-delayed instruction from the original
      // block. Then we take the removed instructions to form a new block. The true branch
      // of the original block is connected with the new block. The false branch of the
      // original block remains unchanged.

      final ReilInstruction lastReilInstruction = lastReil.get(lastReil.size() - 1);

      for (final ReilBlock node : nodes) {
        final Iterable<ReilInstruction> nodeInstructions = node.getInstructions();

        if (Iterables.getLast(nodeInstructions) == lastReilInstruction) {
          // The situation we are having here is that have located the
          // node that contains the delayed instruction. In fact this node
          // contains only the code of the delayed instruction. It has one
          // incoming edge and two outgoing edges.
          //
          // We now have to rewrite the edges to make sure the jump has
          // the two outgoing edges.

          final ReilEdge incomingEdge = node.getIncomingEdges().get(0);

          final ReilBlock parentNode = incomingEdge.getSource();

          edges.remove(incomingEdge);

          boolean first = true;

          for (final ReilEdge outgoingEdge : node.getOutgoingEdges()) {
            if (first) {
              first = false;

              final ReilEdge newEdge =
                  new ReilEdge(parentNode, node, EdgeType.JUMP_CONDITIONAL_TRUE);

              ReilBlock.link(parentNode, node, newEdge);

              edges.add(newEdge);

              final ReilEdge newEdge2 =
                  new ReilEdge(parentNode, outgoingEdge.getTarget(),
                      EdgeType.JUMP_CONDITIONAL_FALSE);

              ReilBlock.link(parentNode, outgoingEdge.getTarget(), newEdge2);

              edges.add(newEdge2);
            } else {
              final ReilEdge newEdge2 =
                  new ReilEdge(node, outgoingEdge.getTarget(), outgoingEdge.getType());

              ReilBlock.link(node, outgoingEdge.getTarget(), newEdge2);

              edges.add(newEdge2);
            }

            edges.remove(outgoingEdge);

            ReilBlock.unlink(outgoingEdge.getSource(), outgoingEdge.getTarget(), outgoingEdge);
          }
        }
      }
    }
  }

  /**
   * Tests whether there is any node in a list of nodes that were generated from the same
   * instruction as a given node and have an outgoing edge to a given instruction.
   * 
   * Basically, what this function checks is whether there is an edge from an original native
   * instruction to a given REIL instruction.
   * 
   * @param nodes The list of nodes to check.
   * @param node The node that provides the core address of all nodes that are checked.
   * @param instruction The instruction to that is the edge target.
   * 
   * @return True, if any node exists with an edge to the given instruction.
   */
  private static boolean hasEdge(final List<ReilBlock> nodes, final ReilBlock node,
      final ReilInstruction instruction) {
    for (final ReilBlock block : nodes) {
      if (shareOriginalInstruction(node, block) && hasEdge(block, instruction)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether a REIL node has an outgoing edge to a given REIL instruction.
   * 
   * @param node The node whose outgoing edges are checked.
   * @param instruction The instruction to search for.
   * 
   * @return True, if the node has an outgoing edge to the instructions. False, otherwise.
   */
  private static boolean hasEdge(final ReilBlock node, final ReilInstruction instruction) {
    return CollectionHelpers.any(node.getOutgoingEdges(), new ICollectionFilter<ReilEdge>() {
      @Override
      public boolean qualifies(final ReilEdge outgoingEdge) {
        return getFirstInstruction(outgoingEdge.getTarget()) == instruction;
      }
    });
  }

  /**
   * Inserts a single missing edge that could not be deduced automatically from the REIL
   * instructions and their operands.
   * 
   * @param nodes List of translated REIL nodes.
   * @param edges List of translated REIL edges. This list is extended by the function.
   * @param nativeEdge The native to check for and add if necessary.
   * @param sourceReilInstruction Source REIL instruction of the edge.
   * @param targetReilInstruction Target REIL instruction of the edge.
   */
  private static void insertNativeEdge(final List<ReilBlock> nodes, final List<ReilEdge> edges,
      final ICodeEdge<?> nativeEdge, final ReilInstruction sourceReilInstruction,
      final ReilInstruction targetReilInstruction) {
    // This part here is very hackish. We want to find out whether there is already an edge that
    // connects the original source and target nodes passed to the function.
    // This is trickier than expected because it is not guaranteed that the final REIL instruction
    // that is generated for the original source contains the jump. For this reason we need to check
    // all the REIL blocks generated from the original input block (and at this point it is not
    // clear that this behaviour is what we want).
    // Furthermore it is not guaranteed that the final instruction of a translated jump instruction
    // is the JCC instruction. In some cases the translated instruction series end with other REIL
    // instructions (mostly nop). In these cases, there will only be one outgoing edge and the type
    // of this outgoing edge must explicitly set to Unconditional. At this point it is not clear if
    // this is the desired behaviour.
    //
    // When changing this code please check back with
    //
    // - notepad.exe, NPCommand, the jumps at the beginning of the entry basic block
    // - coredll.dll, mbstowcs_std__3Unothrow_t_1_B, the outgoing edges of the nop-nodes

    for (final ReilBlock node : nodes) {
      if ((sourceReilInstruction == getLastInstruction(node))
          && !hasEdge(nodes, node, targetReilInstruction)) {
        final EdgeType edgeType =
            ReilHelpers.isJump(sourceReilInstruction) ? nativeEdge.getType()
                : EdgeType.JUMP_UNCONDITIONAL;

        final ReilBlock targetNode = getNode(targetReilInstruction, nodes);
        final ReilEdge newEdge = new ReilEdge(node, targetNode, edgeType);
        ReilBlock.link(node, targetNode, newEdge);

        edges.add(newEdge);
      }
    }
  }

  /**
   * Inserts missing edges that could not be deduced automatically from the REIL instructions and
   * their operands.
   * 
   * @param nativeEdges List of native edges of the input graph.
   * @param nodes List of translated REIL nodes.
   * @param edges List of translated REIL edges. This list is extended by the function.
   * @param firstMap Maps between native instructions and their first REIL instructions.
   * @param lastMap Maps between native instructions and their last REIL instructions.
   */
  private static void insertNativeEdges(final List<? extends ICodeEdge<?>> nativeEdges,
      final List<ReilBlock> nodes, final List<ReilEdge> edges,
      final Map<IInstruction, ReilInstruction> firstMap,
      final Map<IInstruction, ReilInstruction> lastMap) {
    for (final ICodeEdge<?> nativeEdge : nativeEdges) {
      final Object source = nativeEdge.getSource();
      final Object target = nativeEdge.getTarget();

      if ((source instanceof ICodeContainer) && (target instanceof ICodeContainer)) {
        final ICodeContainer<?> sourceCodeNode = (ICodeContainer<?>) source;
        final ICodeContainer<?> targetCodeNode = (ICodeContainer<?>) target;

        final IInstruction sourceInstruction = getLastInstruction(sourceCodeNode);
        final IInstruction targetInstruction = getFirstInstruction(targetCodeNode);

        final ReilInstruction sourceReilInstruction = lastMap.get(sourceInstruction);
        final ReilInstruction targetReilInstruction = firstMap.get(targetInstruction);

        insertNativeEdge(nodes, edges, nativeEdge, sourceReilInstruction, targetReilInstruction);
      }
    }
  }

  private static boolean isInlineSource(final ICodeContainer<?> container) {
    return (container.getOutgoingEdges().size() == 1)
        && (container.getOutgoingEdges().get(0).getType() == EdgeType.ENTER_INLINED_FUNCTION);
  }

  private static boolean shareOriginalInstruction(final ReilBlock node, final ReilBlock block) {
    return (getLastInstruction(block).getAddress().toLong() & ~0xFF) == (getLastInstruction(node)
        .getAddress().toLong() & ~0xFF);
  }

  private List<ReilInstruction> getReilInstructions(final IInstruction instruction,
      final ArrayList<ReilInstruction> instructions) {
    final List<ReilInstruction> result = new ArrayList<ReilInstruction>();

    for (final ReilInstruction reilInstruction : instructions) {
      if (ReilHelpers.toNativeAddress(reilInstruction.getAddress())
          .equals(instruction.getAddress())) {
        result.add(reilInstruction);
      }
    }

    return result;
  }

  /**
   * Translates a disassembled function to REIL code.
   * 
   * @param environment The translation environment for the translation process
   * @param function The disassembled function
   * 
   * @return The function translated to REIL code
   * 
   * @throws InternalTranslationException Thrown if an internal error occurs
   */
  public ReilFunction translate(final ITranslationEnvironment environment,
      final IBlockContainer<InstructionType> function) throws InternalTranslationException {
    return translate(environment, function, new ArrayList<ITranslationExtension<InstructionType>>());
  }

  /**
   * Translates a disassembled function to REIL code.
   * 
   * @param environment The translation environment for the translation process
   * @param function The disassembled function
   * 
   * @return The function translated to REIL code
   * 
   * @throws InternalTranslationException Thrown if an internal error occurs
   */
  public ReilFunction translate(final ITranslationEnvironment environment,
      final IBlockContainer<InstructionType> function,
      final List<ITranslationExtension<InstructionType>> extensions)
      throws InternalTranslationException {
    final LinkedHashMap<ICodeContainer<InstructionType>, List<ReilInstruction>> instructionMap =
        new LinkedHashMap<ICodeContainer<InstructionType>, List<ReilInstruction>>();

    final Map<IInstruction, ReilInstruction> firstMap =
        new HashMap<IInstruction, ReilInstruction>();
    final Map<IInstruction, ReilInstruction> lastMap = new HashMap<IInstruction, ReilInstruction>();
    final List<List<ReilInstruction>> delayedTrueBranches = new ArrayList<List<ReilInstruction>>();
    for (final ICodeContainer<InstructionType> block : function.getBasicBlocks()) {
      final Iterable<InstructionType> blockInstructions = block.getInstructions();
      final IInstruction lastBlockInstruction = Iterables.getLast(blockInstructions);
      final boolean endsWithInlining = isInlineSource(block);
      final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();
      instructionMap.put(block, instructions);

      for (final InstructionType instruction : blockInstructions) {
        environment.nextInstruction();

        final ITranslator<InstructionType> translator =
            m_translators.get(instruction.getArchitecture().toUpperCase());

        if (translator == null) {
          throw new InternalTranslationException(
              "Could not translate instruction from unknown architecture "
                  + instruction.getArchitecture());
        }

        try {
          final List<ReilInstruction> result =
              translator.translate(environment, instruction, extensions);
          instructions.addAll(result);

          if (endsWithInlining && (instruction == lastBlockInstruction)) {
            // We skip the last JCC instruction of blocks that were split by inlining. In 99%
            // of all cases this should be the inlined call; unless the user removed the
            // call from the block.

            final ReilInstruction lastInstruction = instructions.get(instructions.size() - 1);

            if (lastInstruction.getMnemonic().equals(ReilHelpers.OPCODE_JCC)
                && lastInstruction.getMetaData().containsKey("isCall")) {
              instructions.remove(instructions.size() - 1);
              result.remove(result.size() - 1);
            }
          }

          firstMap.put(instruction, getFirstInstruction(result));
          lastMap.put(instruction, getLastInstruction(result));
        } catch (final InternalTranslationException exception) {
          exception.setInstruction(instruction);

          throw exception;
        }
      }

      // In this step we have to consider delayed branches of the form
      //
      // BRANCH CONDITION, SOMEWHERE
      // EXECUTE ALWAYS
      //
      // We basically re-order the instructions to
      //
      // EVALUATE CONDITION -> TEMP
      // EXECUTE ALWAYS
      // BRANCH TEMP, SOMEWHERE

      final IInstruction secondLastInstruction =
          Iterables.size(block.getInstructions()) > 2 ? Iterables.get(block.getInstructions(),
              Iterables.size(block.getInstructions()) - 2, null) : null;

      if (secondLastInstruction != null) {
        final List<ReilInstruction> secondLastReil =
            getReilInstructions(secondLastInstruction, instructions);

        if (ReilHelpers.isDelayedBranch(secondLastReil.get(secondLastReil.size() - 1))) {
          final IInstruction lastInstruction = getLastInstruction(block);
          final List<ReilInstruction> lastReil = getReilInstructions(lastInstruction, instructions);
          if (secondLastReil.get(secondLastReil.size() - 1).getMnemonic()
              .equals(ReilHelpers.OPCODE_JCC)) {
            instructions.removeAll(lastReil);
            instructions.addAll(instructions.size() - 1, lastReil);
          }
        } else if (ReilHelpers.isDelayedTrueBranch(secondLastReil.get(secondLastReil.size() - 1))) {
          final IInstruction lastInstruction = getLastInstruction(block);
          final List<ReilInstruction> lastReil = getReilInstructions(lastInstruction, instructions);
          delayedTrueBranches.add(lastReil);
        }
      }
    }

    // In this step we determine all jump targets of the input graph.
    // We need them later because not all original jump targets can be
    // found in the translated REIL graph. The reason for this is that
    // source instructions of edges in the input graph do not necessarily
    // have a reference to the address of the edge target. This happens
    // for example when removing the first instruction from a code node.
    // The edge still goes to the code node, but the jump instruction now
    // refers to the removed instruction.
    final Collection<IAddress> nativeJumpTargets = getBlockAddresses(function);

    final Pair<List<ReilBlock>, List<ReilEdge>> pair =
        ReilGraphGenerator.createGraphElements(instructionMap.values(), nativeJumpTargets);

    final List<ReilBlock> nodes = pair.first();
    final List<ReilEdge> edges = pair.second();

    // In a post-processing step all edges which could not be determined
    // from the REIL instructions alone are inserted into the graph.
    insertNativeEdges(function.getBasicBlockEdges(), nodes, edges, firstMap, lastMap);

    handleDelayedTrueBranches(nodes, edges, delayedTrueBranches);

    return new ReilFunction("REIL - " + function.getName(), new ReilGraph(nodes, edges));
  }

  public ReilGraph translate(final ITranslationEnvironment environment,
      final ICodeContainer<InstructionType> block) throws InternalTranslationException {
    return translate(environment, block, new ArrayList<ITranslationExtension<InstructionType>>());
  }

  public ReilGraph translate(final ITranslationEnvironment environment,
      final ICodeContainer<InstructionType> block,
      final List<ITranslationExtension<InstructionType>> extensions)
      throws InternalTranslationException {
    final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    for (final InstructionType instruction : block.getInstructions()) {
      environment.nextInstruction();

      final ITranslator<InstructionType> translator =
          m_translators.get(instruction.getArchitecture().toUpperCase());

      if (translator == null) {
        throw new InternalTranslationException(
            "Could not translate instruction from unknown architecture "
                + instruction.getArchitecture());
      }

      try {
        instructions.addAll(translator.translate(environment, instruction, extensions));
      } catch (final InternalTranslationException exception) {
        exception.setInstruction(instruction);

        throw exception;
      }
    }

    final LinkedHashMap<ICodeContainer<InstructionType>, List<ReilInstruction>> instructionMap =
        new LinkedHashMap<ICodeContainer<InstructionType>, List<ReilInstruction>>();

    instructionMap.put(block, instructions);

    return ReilGraphGenerator.createGraph(instructionMap.values(), new ArrayList<IAddress>());
  }

  public ReilGraph translate(final ITranslationEnvironment environment,
      final InstructionType instruction) throws InternalTranslationException {
    return translate(environment, instruction,
        new ArrayList<ITranslationExtension<InstructionType>>());
  }

  public ReilGraph translate(final ITranslationEnvironment environment,
      final InstructionType instruction,
      final List<ITranslationExtension<InstructionType>> extensions)
      throws InternalTranslationException {
    final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    environment.nextInstruction();

    final ITranslator<InstructionType> translator =
        m_translators.get(instruction.getArchitecture().toUpperCase());

    if (translator == null) {
      throw new InternalTranslationException(
          "Could not translate instruction from unknown architecture "
              + instruction.getArchitecture());
    }

    try {
      instructions.addAll(translator.translate(environment, instruction, extensions));
    } catch (final InternalTranslationException exception) {
      exception.setInstruction(instruction);

      throw exception;
    }

    final LinkedHashMap<ICodeContainer<InstructionType>, List<ReilInstruction>> instructionMap =
        new LinkedHashMap<ICodeContainer<InstructionType>, List<ReilInstruction>>();

    final InstructionContainer<InstructionType> container =
        new InstructionContainer<InstructionType>(instruction);

    instructionMap.put(container, instructions);

    return ReilGraphGenerator.createGraph(instructionMap.values(), new ArrayList<IAddress>());
  }

  /**
   * Translates a disassembled program to REIL code.
   * 
   * @param environment The translation environment for the translation process
   * @param functions The functions of the program to be translated.
   * 
   * @return The program translated to REIL code
   * 
   * @throws InternalTranslationException Thrown if an internal error occurs
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public ReilProgram<InstructionType> translate(final ITranslationEnvironment environment,
      final List<? extends IBlockContainer<InstructionType>> functions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");

    final ReilProgram<InstructionType> program = new ReilProgram<InstructionType>();

    for (final IBlockContainer<InstructionType> function : functions) {
      program.addFunction(function, translate(environment, function));
    }

    return program;
  }

  private static class InstructionContainer<InstructionType extends IInstruction> implements
      ICodeContainer<InstructionType> {
    private final InstructionType m_instruction;

    private InstructionContainer(final InstructionType instruction) {
      m_instruction = instruction;
    }

    @Override
    public IAddress getAddress() {
      return m_instruction.getAddress();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<InstructionType> getInstructions() {
      return Lists.newArrayList(m_instruction);
    }

    @Override
    public InstructionType getLastInstruction() {
      return m_instruction;
    }

    @Override
    public List<? extends ICodeEdge<?>> getOutgoingEdges() {
      return new ArrayList<ICodeEdge<?>>();
    }

    @Override
    public boolean hasInstruction(final InstructionType instruction) {
      return m_instruction == instruction;
    }
  }
}
