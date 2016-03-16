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
package com.google.security.zynamics.reil.translators.x86;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.ExpressionComparator;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collection of helper functions for translating x86 code to REIL code.
 */
public class Helpers {

  /**
   * Used to sort expressions at the same level of an n-ary operand tree
   */
  private static ExpressionComparator comparator = new ExpressionComparator();

  /**
   * Auxiliary flag constant to be used in REIL code
   */
  public static final String AUXILIARY_FLAG = "AF";

  /**
   * Carry flag constant to be used in REIL code
   */
  public static final String CARRY_FLAG = "CF";

  /**
   * Direction flag constant to be used in REIL code
   */
  public static final String DIRECTION_FLAG = "DF";

  /**
   * Interrupt flag constant to be used in REIL code
   */
  public static final String INTERRUPT_FLAG = "IF";

  /**
   * Overflow flag to be used in REIL code
   */
  public static final String OVERFLOW_FLAG = "OF";

  /**
   * Parity flag constant to be used in REIL code
   */
  public static final String PARITY_FLAG = "PF";

  /**
   * Sign flag constant to be used in REIL code
   */
  public static final String SIGN_FLAG = "SF";

  /**
   * Zero flag constant to be used in REIL code
   */
  public static final String ZERO_FLAG = "ZF";

  /**
   * Extracts a subregister (like AX, AL, AH) from a parent register (like EAX)
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset where the new REIL code can be placed
   * @param subRegister The subregister that should be extracted
   *
   * @return The result of the translation
   *
   * @throws InternalTranslationException Thrown if an internal problem occurs
   */
  private static TranslationResult extractRegister(final ITranslationEnvironment environment,
      final long offset, final String subRegister) throws InternalTranslationException {
    final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final String parentRegister = getParentRegister(subRegister);
    final OperandSize archSize = environment.getArchitectureSize();

    if (isHigher8BitRegister(subRegister)) {
      // The sub-register is not at the low end of the parent
      // register. Mask + shift is necessary here.
      final String maskResult = environment.getNextVariableString();
      final String shiftResult = environment.getNextVariableString();

      // Add the mask + shift instructions
      instructions.add(ReilHelpers.createAnd(offset, archSize, parentRegister, OperandSize.WORD,
          "65280", OperandSize.WORD, maskResult));
      instructions.add(ReilHelpers.createBsh(offset + 1, OperandSize.WORD, maskResult,
          OperandSize.WORD, "-8", OperandSize.BYTE, shiftResult));

      return new TranslationResult(shiftResult, OperandSize.BYTE, TranslationResultType.REGISTER,
          null, instructions, offset);
    } else {
      // The sub-register is already at the low end of the parent register.
      // Masking is enough.
      final OperandSize subRegisterSize = getRegisterSize(subRegister);
      final String mask = String.valueOf(TranslationHelpers.getAllBitsMask(subRegisterSize));
      final String result = environment.getNextVariableString();

      // Add the mask instruction
      instructions.add(ReilHelpers.createAnd(offset, archSize, parentRegister, subRegisterSize,
          mask, subRegisterSize, result));

      return new TranslationResult(result, subRegisterSize, TranslationResultType.REGISTER, null,
          instructions, offset);
    }
  }

  /**
   * Pushes a register onto the stack
   *
   * @param environment A valid translation environment
   * @param baseOffset The next unused REIL offset
   * @param register The name of the register to push
   * @param size The size of the register (either WORD or DWORD)
   * @param instructions A list of REIL instructions where the new REIL code is added
   */
  private static void generateRegisterPush(final ITranslationEnvironment environment,
      final long baseOffset, final String register, final OperandSize size,
      final List<ReilInstruction> instructions) {

    long offset = baseOffset;

    final String pValue;

    if (size == OperandSize.DWORD) {
      pValue = register;
    } else {
      pValue = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(offset, OperandSize.DWORD, register, size, "65535",
          size, pValue));
      offset++;
    }

    generatePush(environment, offset, pValue, size, instructions);
  }

  /**
   * Generates code that generates sign masks for values
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param value The value in question
   * @param size The size of the value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return A pair that contains the MSB of the value shifted into the LSB and the register that
   *         contains the generated sign mask
   */
  private static Pair<String, String> generateSignMask(final ITranslationEnvironment environment,
      final long offset, final String value, final OperandSize size,
      final List<ReilInstruction> instructions) {

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String shiftValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size));

    final String maskedMsb = environment.getNextVariableString();
    final String msbInLsb = environment.getNextVariableString();
    final String signMask = environment.getNextVariableString();

    // Extract the sign
    instructions.add(ReilHelpers.createAnd(offset, size, value, size, msbMask, size, maskedMsb));

    // Shift the sign into the LSB
    instructions.add(ReilHelpers.createBsh(offset + 1, size, maskedMsb, size, shiftValue, size,
        msbInLsb));

    // Calculate 0 - sign
    instructions.add(ReilHelpers.createSub(offset + 2, size, "0", size, msbInLsb, size, signMask));

    return new Pair<String, String>(msbInLsb, signMask);
  }

  /**
   * Masks a register negatively. That means the bits of the sub-register are cleared. All others
   * survive.
   *
   * @param register The sub-register in question
   * @return The described mask value
   * @throws InternalTranslationException Thrown if an invalid register was passed to the function.
   */
  private static long getNegativeMask(final String register) throws InternalTranslationException {

    if (isLower8BitRegister(register)) {
      return 0xFFFFFF00L;
    } else if (isHigher8BitRegister(register)) {
      return 0xFFFF00FFL;
    } else if (is16BitRegister(register)) {
      return 0xFFFF0000L;
    } else {
      throw new InternalTranslationException("Error: Invalid register");
    }
  }

  /**
   * Determines whether a passed register is a 16 bit register (AX, BX, ...).
   *
   * @param register The name of the register
   * @return True, if the name of the register is a 16 bit register. False, otherwise.
   */
  private static boolean is16BitRegister(final String register) {

    return register.equals("ax") || register.equals("bx") || register.equals("cx")
        || register.equals("dx") || register.equals("si") || register.equals("di")
        || register.equals("sp") || register.equals("bp") || register.equals("ip");
  }

  /**
   * Determines whether a passed register is either AL, BL, CL, or DL.
   *
   * @param register The name of the register
   * @return True, if the name of the register is either AL, BL, CL, or DL. False, otherwise.
   */
  private static boolean isLower8BitRegister(final String register) {

    return register.equals("al") || register.equals("bl") || register.equals("cl")
        || register.equals("dl");
  }

  /**
   * Checks whether a string is a memory access identifier string.
   *
   * @param value The string in question.
   * @return True, if the string is a memory access identifier string. False, otherwise.
   */
  private static boolean isMemoryAccess(final String value) {
    return value.equals("[");
  }

  /**
   * Checks whether a string identifies an x86 segment.
   *
   * @param value The string in question.
   * @return True, if the string identifies a segment. False, otherwise.
   */
  private static boolean isSegment(final String value) {
    return value.equals("cs") || value.equals("ds") || value.equals("es") || value.equals("fs")
        || value.equals("gs") || value.equals("ss");
  }

  /**
   * This is needed to identify if a particular expression is a segment register.
   *
   * @param value The string from the operand tree node
   * @return True, if the string identifies a segment expression. False, otherwise.
   */
  private static boolean isSegmentExpression(final String value) {
    return (value.endsWith(":") && isSegment(value.substring(0, value.length() - 1)));
  }

  /**
   * Iterates over the children of a node in the operand tree and generates
   * translations for them.
   * @param environment A valid translation environment.
   * @param expression The expression to translate.
   * @param size The size of the expression.
   * @param loadOperand A flag that indicates whether a LDM instruction should be added for memory
   *        access operands.
   * @param baseOffset The offset of the first instruction. This has to be a
   *        Long (and not a long) so that we have reference semantics; so that
   *        this function can update the baseOffset of calling functions.

   *

   * @return A list of translations for the individual children.
   * @throws InternalTranslationException
   */
  private static List<TranslationResult> translateChildrenOfNode(
      final ITranslationEnvironment environment,
      final IOperandTreeNode expression,
      OperandSize size,
      final boolean loadOperand,
      Long baseOffset) throws InternalTranslationException {
    // The list in which we will gather the partial translations. This will be
    // returned to the caller.
    final List<TranslationResult> partialResults = new ArrayList<>();

    // Get all child nodes of the current node and sort them. The sorting is
    // important for precedence in arithmetic expressions.
    final List<? extends IOperandTreeNode> children = expression.getChildren();
    Collections.sort(children, comparator);

    // ... and process them
    for (final IOperandTreeNode child : children) {
      // Get the code for the child expression.
      final TranslationResult nextResult =
          loadOperand(environment, baseOffset, child,
              isSegmentExpression(expression.getValue()) ? expression : null,
              size, loadOperand);
      partialResults.add(nextResult);
      baseOffset += nextResult.getInstructions().size();
    }
    return partialResults;
  }

  private static TranslationResult processLeafNode(
      final ITranslationEnvironment environment,
      final long baseOffset,
      final IOperandTreeNode expression,
      OperandSize size,
      boolean loadOperand) throws InternalTranslationException {
    // All leaves are either registers or integer literals. They are translated
    // into "STR leaf, , nextVariable" instructions. Optimizations are handled
    // during the translation of their parent nodes.
    // Get the type of the leaf.
    final String value = expression.getValue();
    final OperandType operandType = OperandType.getOperandType(value);

    TranslationResultType nodeType = null;

    switch (operandType) {
      case REGISTER:
        nodeType = TranslationResultType.REGISTER;
        break;
      case INTEGER_LITERAL:
        nodeType = TranslationResultType.LITERAL;
        break;
      default:
        throw new InternalTranslationException("Error: Leaf has invalid type");
    }

    final List<ReilInstruction> instructions = new ArrayList<>();
    final String nextVariableString = environment.getNextVariableString();
    if ((operandType == OperandType.INTEGER_LITERAL)
        || !needsExtraction(environment, value)) {
      if (loadOperand) {
        instructions.add(ReilHelpers.createStr(baseOffset, size, value, size,
                                               nextVariableString));
        return new TranslationResult(nextVariableString,
            size, nodeType, null,
            instructions, baseOffset);
      } else {
        // For single-leaf registers that have loadOperand set to false, return
        // the register directly. This will avoid the generation of superfluous
        // STR instructions which load from undefined registers.
        // Example:    mov ebx, [esp+0x14]
        // If we do not have this extra case here, we would generate REIL code
        // of the form
        //      str 0x20, --, t0
        //      str ESP, --, t1
        //      add t0, t1, t2
        //      and t2, 0xFFFFFFFF, t2
        //      ldm t2, --, t3
        //      str EBX, --, t8 <-- This is undesired
        //      str t3, --, ebx
        return new TranslationResult(value, size, nodeType, null, instructions, baseOffset);
      }
    } else {
      // Mask smaller operands
      return extractRegister(environment, baseOffset, value);
    }
  }

  /**
   * Translate a "simple" memory access operand and provide it's REIL translation.
   * A simple operand does not have any internal arithmetic, is hence either a
   * direct load from a register or a load from an immediate location.
   */
  private static TranslationResult processSimpleMemoryAccess(
      final ITranslationEnvironment environment,
      final IOperandTreeNode segmentOverride,
      OperandSize size, final boolean loadOperand,
      TranslationResult intermediateResult) {

    final TranslationResultType childType = intermediateResult.getType();

    // Do we actually need to load data from memory? This may not be needed
    // in all cases, such as "mov [eax], 0x20".
    if ((childType == TranslationResultType.LITERAL)
        || (childType == TranslationResultType.REGISTER)) {
      return processSimpleMemoryAccessLiteralOrRegisterLoad(environment, segmentOverride, size,
          loadOperand, intermediateResult);
    } else {
      return processSimpleMemoryAccessFromCompoundAddress(environment, segmentOverride, size,
          loadOperand, intermediateResult);
    }
  }

  /**
   * If the operand involves arithmetic, once this arithmetic is done, the segment
   * base register (if present) has to be added in and the result has to be truncated
   * to 32 bits. This function performs this task.
   */
  private static TranslationResult processSimpleMemoryAccessFromCompoundAddress(
      ITranslationEnvironment environment,
      IOperandTreeNode segmentOverride,
      OperandSize size,
      boolean loadOperand,
      TranslationResult intermediateResult) {

    final OperandSize archSize = environment.getArchitectureSize();
    final String truncationMask =
        String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    String childResult = intermediateResult.getRegister();

    // Add a segment register addition if a segment override prefix is present.
    if (segmentOverride != null) {
      final String pseudoRegister = getSegmentOverridePseudoRegister(segmentOverride);
      String nextVariableString = environment.getNextVariableString();
      intermediateResult.addInstruction(ReilHelpers.createAdd(0 /* reil address */,
          archSize,
          childResult,
          archSize,
          pseudoRegister,
          archSize,
          nextVariableString));
      // Now make sure that the following code operates on the result of having
      // added the segment register in.
      childResult = nextVariableString;
    }

    // Truncate the results of the address arithmetic.
    final String truncatedAddress = environment.getNextVariableString();
    intermediateResult.addInstruction(ReilHelpers.createAnd(0 /* reil address */,
        archSize,
        childResult,
        archSize,
        truncationMask,
        archSize,
        truncatedAddress));

    // Add the loading LDM instruction if this is desired.
    if (loadOperand) {
      final String loadedValue = environment.getNextVariableString();
      intermediateResult.addInstruction(
      // Set the address of the instruction to 0, it will be adjusted by
      // the TranslationResult.addInstruction.
          ReilHelpers.createLdm(0 /* reil address */, archSize, truncatedAddress, size,
              loadedValue));

      intermediateResult.updateResult(
          loadedValue, size, truncatedAddress, TranslationResultType.MEMORY_ACCESS);
    } else {
      intermediateResult.updateResult(
          truncatedAddress, size, "", TranslationResultType.MEMORY_ACCESS);
    }
    return intermediateResult;
  }

  private static String getSegmentOverridePseudoRegister(
      IOperandTreeNode segmentOverride) {
    String value = segmentOverride.getValue();
    return value.substring(0, value.length() - 1) + "base";
  }

  /**
   * Generate translation for a simple memory access or register load.
   *
   * @param environment The translation environment, keeps track of temp register usage etc.
   * @param segmentOverride Any segment overrides that were passed down from above during the
   *        translation.
   * @param size The size of the operand.
   * @param loadOperand Should a LDM be generated when "[" is seen?
   * @param intermediateResult The result of the translation so far.
   * @return The TranslationResult with the instructions for this operand.
   */
  private static TranslationResult processSimpleMemoryAccessLiteralOrRegisterLoad(
      ITranslationEnvironment environment, IOperandTreeNode segmentOverride, OperandSize size,
      boolean loadOperand, TranslationResult intermediateResult) {
    final OperandSize archSize = environment.getArchitectureSize();

    // Get the result of the previous translation.
    final String childResult = intermediateResult.getRegister();
    final String loadTarget = environment.getNextVariableString();

    // If we have a segment override, we need to add the segment override
    // into an additional temporary register before we issue the LDM
    // instruction.
    if (segmentOverride != null) {
      // TODO(thomasdullien): Consider refactoring the near-repetitions of
      // code here and elsewhere (e.g. the adding in of segment registers)
      // into separate functions.
      final String pseudoRegister = getSegmentOverridePseudoRegister(segmentOverride);
      final String nextVariableString = environment.getNextVariableString();

      intermediateResult.addInstruction(ReilHelpers.createAdd(0, /* filled later */
      archSize, childResult, archSize, pseudoRegister, archSize, nextVariableString));

      // TODO(thomasdullien): Is this inner use of loadOperand / LDM generation
      // even needed now? Or does the calling code take care of it now?
      if (loadOperand) {
        intermediateResult.addInstruction(ReilHelpers.createLdm(0, /* filled later */
            archSize,
            nextVariableString, size, loadTarget));

        // Now we need to update the internal results & types.
        intermediateResult.updateResult(loadTarget, size, nextVariableString,
            TranslationResultType.MEMORY_ACCESS);
      } else {
        intermediateResult.updateResult(nextVariableString, size, nextVariableString,
            TranslationResultType.MEMORY_ACCESS);
      }
    } else {
      // Generate the LDM instruction that loads a value from the specified address.
      if (loadOperand) {
        intermediateResult.addInstruction(
            ReilHelpers.createLdm(0, archSize, childResult, size, loadTarget));
        intermediateResult.updateResult(loadTarget, size, childResult,
            TranslationResultType.MEMORY_ACCESS);
      } else {
         intermediateResult.updateResult(intermediateResult.getRegister(),
            size, childResult, TranslationResultType.MEMORY_ACCESS);
      }
    }
    return intermediateResult;
  }

  /**
   * Generates code that loads an x86 operand.
   *
   * This is done by first recursively calling loadOperand for all children of
   * the current node, and then processing the current node in the operand tree.
   *
   * @param environment A valid translation environment.
   * @param baseOffset Offset of the first instruction to translate.
   * @param expression The operand tree node to translate next
   * @param segmentOverride If any segment override was seen in the tree so far,
   *        it should be passed down the recursion
   * @param size The size of the operand.
   * @param loadOperand TODO(thomasdullien) The purpose of this argument is unknown.
   *
   * @return The result of the translation.
   *
   * @throws InternalTranslationException Thrown if an internal problem occurs
   */
  private static TranslationResult loadOperand(final ITranslationEnvironment environment,
      final Long baseOffset,
      final IOperandTreeNode expression,
      final IOperandTreeNode segmentOverride,
      OperandSize size,
      final boolean loadOperand) throws InternalTranslationException {

    // Obtain the string that describes the current node in the operand tree.
    final String currentNodeValue = expression.getValue();

    // Obtain the number of children of the current node. This will be needed
    // in several places for different purposes, so we get it now.
    final int numberOfChildren = expression.getChildren().size();

    // If the current node is a size override (something like b4, b2 etc.),
    // reset the current operand size accordingly.
    if (TranslationHelpers.isSizeExpression(expression)) {
      size = OperandSize.sizeStringToValue(currentNodeValue);
    }

    // Now process all children of the current node and gather the results of
    // the translation of the child nodes.
    Long newBaseOffset = baseOffset.longValue();    // Make a copy of baseOffset
                                        // so we can have reference semantics. It
                                        // gets modified inside translateChildrenOfNode
                                        // below.
    final OperandSize childrenSize = isMemoryAccess(currentNodeValue)
        ? environment.getArchitectureSize() : size;
    final List<TranslationResult> partialResults =
        translateChildrenOfNode(environment,
        expression,
        childrenSize,
        loadOperand,
        newBaseOffset);

    switch (numberOfChildren) {
      case 0:
        return processLeafNode(environment, newBaseOffset, expression, size, loadOperand);
      case 1:
        // Process nodes that are somewhere inside the tree
        // and have exactly one child. These nodes can be:
        // - Size information nodes
        // - Segment information nodes
        // - Memory access nodes.
        if (OperandSize.isSizeString(currentNodeValue)) {
          // Size information nodes can be skipped (they have already made their
          // impact by resetting the "size" argument prior to the call to
          // translateChildrenOfNode. Just return the translation.
          return partialResults.get(0);
        } else if (isSegmentExpression(currentNodeValue)) {
          // If this is a segment expression, it will already have been dealt
          // with in the translation of the memory access during the call to
          // translateChildrenOfNode. Just return the translation.
          return partialResults.get(0);
        } else if (isMemoryAccess(currentNodeValue)) {
          return processSimpleMemoryAccess(environment, segmentOverride, size, loadOperand,
              partialResults.get(0));
        } else {
          throw new InternalTranslationException(
              "Error: Unknown node type with one child during address operand translation");
        }
      default:
        // Dealing with nodes that have more than one child. This means we are
        // always dealing with a compound memory dereference.
        return processInOperandArithmetic(
            partialResults,
            environment,
            newBaseOffset,
            expression);
    }
  }

  private static void addPlusOrTimesInOperandArithmetic(
      final ITranslationEnvironment environment,
      final String value,
      final String source1,
      final String source2,
      final String destination,
      final TranslationResult finalResult
      ) {
    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(archSize);
    if (value.equals("+")) {
      finalResult.addInstruction(ReilHelpers.createAdd(0, /* reil address */
          archSize,
          source1,
          archSize,
          source2,
          resultSize,
          destination));
    } else if (value.equals("*")) {
      finalResult.addInstruction(ReilHelpers.createMul(0, /* reil address */
          archSize,
          source1,
          archSize,
          source2,
          resultSize,
          destination));
    }
  }

  /**
   * Deals with complicated arithmetic within operands, such as [ebx+ecx*2+2]
   *
   * @param partialResults The list of translations for the subtrees of the operand
   * @param environment The translation environment to track temp registers etc.
   * @param baseOffset The base address of this instruction.
   * @param expression The current node in the tree to process.
   * @return The result of the translation for the operand arithmetic.
   * @throws InternalTranslationException
   */
  private static TranslationResult processInOperandArithmetic(List<
      TranslationResult> partialResults,
      ITranslationEnvironment environment,
      long baseOffset,
      IOperandTreeNode expression) throws InternalTranslationException {
    final String value = expression.getValue();
    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize nextSize = TranslationHelpers.getNextSize(archSize);

    // This result will be filled with the merged results from partialResults.
    TranslationResult finalResult = new TranslationResult("NEEDS_REPLACEMENT",
        archSize,
        TranslationResultType.REGISTER,
        "",
        new ArrayList<ReilInstruction>(),
        baseOffset);

    if (value.equals("+") || (value.equals("*"))) {
      // Join all the instructions from all partial results into a new list.
      ArrayList<ReilInstruction> allInstructions = new ArrayList<>();
      for (TranslationResult result : partialResults) {
        allInstructions.addAll(result.getInstructions());
      }
      finalResult.updateBaseAndReil(baseOffset, allInstructions);

      String source1 = partialResults.get(0).getRegister();
      String source2 = partialResults.get(1).getRegister();
      String currentTemporary = environment.getNextVariableString();

      // Join the partial results. Careful, to avoid partial unrolling of the
      // loop, the iteration behaves differently on different iterations,
      // making the code a bit tricky.
      // On the first iteration, we add result[0] and result[1] into currentTemp.
      // On the second iteration, we add result[2] and currentTemp into currentTemp
      // and so forth.
      //
      // TODO(thomasdullien): It is generally not recommended to reuse temporary
      // registers in the way that the code does it here, but this code is a
      // refactoring of legacy code that tried to preserve semantics. Find a way
      // to not reuse the temporaries here in the future.
      for (int index = 2; index <= partialResults.size(); index++) {
        addPlusOrTimesInOperandArithmetic(environment,
            value,
            source1,
            source2,
            currentTemporary,
            finalResult);
        if (index < partialResults.size()) {
          source1 = partialResults.get(index).getRegister();
          source2 = currentTemporary;
        }
      }

      // Add an AND to mask off extra bits in the address calculation.
      final String truncationMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));
      finalResult.addInstruction(ReilHelpers.createAnd(0,
          nextSize,
          currentTemporary,
          nextSize,
          truncationMask,
          archSize,
          currentTemporary));
      finalResult.updateResult(currentTemporary, archSize, "", TranslationResultType.REGISTER);
    } else if (value.equals(":")) {
      throw new InternalTranslationException(
          "Error: Don't know how to deal with 1234:ABCD1234 (segment:address) operands.");
    }

    return finalResult;
  }

  /**
   * Determines whether a register requires extraction because it is the sub-register of a bigger
   * register.
   *
   * @param environment A valid translation environment
   * @param registerName The name of the register to check.
   *
   * @return True, if the register requires extraction.
   *
   * @throws InternalTranslationException Thrown if an invalid register name was passed to the
   *         function.
   */
  private static boolean needsExtraction(final ITranslationEnvironment environment,
      final String registerName) throws InternalTranslationException {
    return (getRegisterSize(registerName) != environment.getArchitectureSize())
        && !isSegment(registerName);
  }

  /**
   * Extends the sign of a value to the next higher data size.
   *
   * Attention: Caller must mask op down to the specified size to make this work.
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param value The value to extend
   * @param size The size of the value
   * @param extendedSize The size of the extended value
   *
   * @return The result of the sign extension
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static TranslationResult extendSign(final ITranslationEnvironment environment,
      final long offset, final String value, final OperandSize size, final OperandSize extendedSize)
      throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(extendedSize, "Error: Argument extendedSize can't be null");

    // Make sure that the size of the value is less than the extended size
    Preconditions.checkArgument(size.getByteSize() < extendedSize.getByteSize(),
        "Error: Argument size must be smaller than argument extendedSize");

    final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(extendedSize));

    final String toggledMsb = environment.getNextVariableString();
    final String extendedValue = environment.getNextVariableString();
    final String truncatedValue = environment.getNextVariableString();

    instructions.add(ReilHelpers.createXor(offset, size, value, size, msbMask, size, toggledMsb));
    instructions.add(ReilHelpers.createSub(offset + 1, size, toggledMsb, size, msbMask,
        extendedSize, extendedValue));
    instructions.add(ReilHelpers.createAnd(offset + 2, extendedSize, extendedValue, extendedSize,
        truncateMask, extendedSize, truncatedValue));

    return new TranslationResult(truncatedValue, extendedSize, TranslationResultType.REGISTER,
        null, instructions, offset);
  }

  /**
   *
   * @param environment A valid translation environment
   * @param baseOffset The next unused REIL offset
   * @param value The value in question
   * @param size The size of the value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return A pair that contains the MSB of the value shifted into the LSB and the register that
   *         contains the generated absolute value
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static Pair<String, String> generateAbs(final ITranslationEnvironment environment,
      final long baseOffset, final String value, final OperandSize size,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final int oldInstructionsSize = instructions.size();

    final Pair<String, String> signMask =
        generateSignMask(environment, baseOffset, value, size, instructions);

    final long offset = (baseOffset + instructions.size()) - oldInstructionsSize;

    final String toggledSign = environment.getNextVariableString();
    final String absValue = environment.getNextVariableString();

    instructions.add(ReilHelpers.createXor(offset, size, value, size, signMask.second(), size,
        toggledSign));
    instructions.add(ReilHelpers.createSub(offset + 1, size, toggledSign, size, signMask.second(),
        size, absValue));

    return new Pair<String, String>(signMask.first(), absValue);
  }

  /**
   * Generates code for AND expressions
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param size The size of the two operands
   * @param operand1 The first operand
   * @param operand2 The second operand
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return The register that contains the result of the AND expression
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static String generateAnd(final ITranslationEnvironment environment, final long offset,
      final OperandSize size, final String operand1, final String operand2,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(operand1, "Error: Argument operand1 can't be null");
    Preconditions.checkNotNull(operand2, "Error: Argument operand2 can't be null");

    final String result = environment.getNextVariableString();

    // Do the AND operation
    instructions.add(ReilHelpers.createAnd(offset, size, operand1, size, operand2, size, result));

    // Set the flags according to the result of the AND operation
    generateBinaryOperationFlags(environment, offset + 1, result, size, instructions);

    return result;
  }

  /**
   * Sets the flags according to the result of a binary AND/OR/XOR operation.
   *
   * @param environment A valid translation environment
   * @param nextOffset The next unused REIL offset
   * @param result The result of the binary operation
   * @param resultSize The size of the result
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static void generateBinaryOperationFlags(final ITranslationEnvironment environment,
      final long nextOffset, final String result, final OperandSize resultSize,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(result, "Error: Argument result can't be null");
    Preconditions.checkNotNull(resultSize, "Error: Argument resultSize can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final long baseOffset = nextOffset - instructions.size();
    long offset = nextOffset;

    // Isolate the MSB of the result and put it into the Sign Flag
    generateSignFlagCode(environment, offset, result, resultSize, instructions);
    offset = (baseOffset + instructions.size()) - 1;

    // Update the Zero Flag
    instructions.add(ReilHelpers.createBisz(offset + 1, resultSize, result, OperandSize.BYTE,
        Helpers.ZERO_FLAG));

    // Clear CF and OF
    instructions.add(ReilHelpers.createStr(offset + 2, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.CARRY_FLAG));
    instructions.add(ReilHelpers.createStr(offset + 3, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.OVERFLOW_FLAG));
  }

  /**
   * Generates code that loads a value from the stack and puts it into the target.
   *
   * @param environment A valid translation environment
   * @param baseOffset The next unused REIL offset
   * @param size The size of the target
   * @param target The target where the stack value is stored (this parameter can be null)
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static void generateLoadFromStack(final ITranslationEnvironment environment,
      final long baseOffset, final OperandSize size, final String target,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(target, "Error: Argument target can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    long offset = baseOffset;

    final OperandSize archSize = environment.getArchitectureSize();

    if (size == archSize) {
      // Target size == archSize => We can just pop the value

      Helpers.generatePop(environment, offset, size, target, instructions);
    } else {

      // Target size != archSize => We need to mask the
      // popped value into the parent register of the target

      final String loadedReg = Helpers.generatePop(environment, offset, size, null, instructions);

      offset = offset + instructions.size();

      final String maskedReg = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(offset, size, target, size, "4294901760", size,
          maskedReg));
      instructions.add(ReilHelpers.createOr(offset + 1, size, loadedReg, size, maskedReg, size,
          target));
    }
  }

  /**
   * Generates code for a mov or cmovcc instruction
   *
   * @param environment A valid translation environment
   * @param baseOffset The next unused REIL offset
   * @param instruction The instruction to translate
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   * @throws InternalTranslationException
   */
  public static void generateMov(final ITranslationEnvironment environment, final long baseOffset,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    long reilOffset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Load source operand.
    final TranslationResult loadSource =
        Helpers.translateOperand(environment, reilOffset, operands.get(1), true);
    instructions.addAll(loadSource.getInstructions());

    // Adjust the offset of the next REIL instruction.
    reilOffset = baseOffset + instructions.size();

    // Load destination operand.
    final TranslationResult loadDest =
        Helpers.translateOperand(environment, reilOffset, operands.get(0), false);
    instructions.addAll(loadDest.getInstructions());

    // Adjust the offset of the next REIL instruction.
    reilOffset = baseOffset + instructions.size();

    // Write the loaded value back into the destination
    Helpers.writeBack(environment,
        reilOffset,
        operands.get(0),
        loadSource.getRegister(),
        loadDest.getSize(),
        loadDest.getAddress(),
        loadDest.getType(),
        instructions);
  }

  /**
   * Generates code that pops a value off the stack
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param size The size of the target
   * @param target The target where the stack value is stored (this parameter can be null)
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return The register that contains the loaded value (aka the target)
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static String generatePop(final ITranslationEnvironment environment, final long offset,
      final OperandSize size, final String target, final List<ReilInstruction> instructions)
      throws IllegalArgumentException {

    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(archSize);

    final String loadedValue = target == null ? environment.getNextVariableString() : target;

    instructions.add(ReilHelpers.createLdm(offset, archSize, "esp", size, loadedValue));

    final String tempEsp = environment.getNextVariableString();
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    // Adjust the stack
    final String stackValue = String.valueOf(size.getByteSize());
    instructions.add(ReilHelpers.createAdd(offset + 1, archSize, "esp", archSize, stackValue,
        resultSize, tempEsp));
    instructions.add(ReilHelpers.createAnd(offset + 2, resultSize, tempEsp, archSize, truncateMask,
        archSize, "esp"));

    return loadedValue;
  }

  /**
   * Generates code that pushes a value onto the stack
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param value The value in question
   * @param size The size of the value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static void generatePush(final ITranslationEnvironment environment, final long offset,
      final String value, final OperandSize size, final List<ReilInstruction> instructions)
      throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize nextSize = TranslationHelpers.getNextSize(archSize);

    final String addResult = environment.getNextVariableString();

    // Truncation mask for truncating the result of the subtraction.
    final String mask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    final String subtractionValue = String.valueOf(size.getByteSize());

    // Subtract enough space from the stack to store the value
    instructions.add(ReilHelpers.createSub(offset, archSize, "esp", archSize, subtractionValue,
        nextSize, addResult));

    // Truncate potential overflows
    instructions.add(ReilHelpers.createAnd(offset + 1, nextSize, addResult, archSize, mask,
        archSize, "esp"));

    // Store the value on the stack
    instructions.add(ReilHelpers.createStm(offset + 2, size, value, archSize, "esp"));
  }

  /**
   * Pushes all registers onto the stack (in correct order for pusha/pushaw)
   *
   * @param environment A valid translation environment
   * @param baseOffset The next unused REIL offset
   * @param size The size of the registers to push (either WORD or DWORD)
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static void generatePushAllRegisters(final ITranslationEnvironment environment,
      final long baseOffset, final OperandSize size, final List<ReilInstruction> instructions)
      throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");
    Preconditions.checkArgument((size == OperandSize.WORD) || (size == OperandSize.DWORD),
        "Error: Invalid size argument");

    long offset = baseOffset;

    final OperandSize archSize = environment.getArchitectureSize();

    final String tempEsp = environment.getNextVariableString();

    instructions.add(ReilHelpers.createStr(offset, archSize, "esp", archSize, tempEsp));

    generateRegisterPush(environment, offset + 1, "eax", size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, "ebx", size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, "ecx", size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, "edx", size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, tempEsp, size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, "ebp", size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, "esi", size, instructions);
    offset = baseOffset + instructions.size();

    generateRegisterPush(environment, offset, "edi", size, instructions);
    offset = baseOffset + instructions.size();
  }

  /**
   * Generates code to set the Sign Flag
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param value The value used to set the SF
   * @param valueSize The size of the value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static void generateSignFlagCode(final ITranslationEnvironment environment,
      final long offset, final String value, final OperandSize valueSize,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(valueSize, "Error: Argument valueSize can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final String maskValue = environment.getNextVariableString();

    final String mask = String.valueOf(TranslationHelpers.getMsbMask(valueSize));
    final String shiftValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(valueSize));

    // Isolate the MSB of the result and put it into the Sign Flag
    instructions.add(ReilHelpers.createAnd(offset, valueSize, value, valueSize, mask, valueSize,
        maskValue));
    instructions.add(ReilHelpers.createBsh(offset + 1, valueSize, maskValue, valueSize, shiftValue,
        OperandSize.BYTE, Helpers.SIGN_FLAG));
  }

  /**
   * Generates code that subtracts two values and sets the flags according to the result.
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param size The size of the two operands
   * @param operand1 The first operand
   * @param operand2 The second operand
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return The register that holds the result of the subtraction
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static String generateSub(final ITranslationEnvironment environment, final long offset,
      final OperandSize size, final String operand1, final String operand2,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(operand1, "Error: Argument operand1 can't be null");
    Preconditions.checkNotNull(operand2, "Error: Argument operand2 can't be null");

    final OperandSize resultSize = TranslationHelpers.getNextSize(size);

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String shiftMsbLsb = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size));
    final String carryMask = String.valueOf(getCarryMask(size));
    final String shiftCarryLsb = String.valueOf(-size.getBitSize());
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(size));

    final String maskedOp1 = environment.getNextVariableString();
    final String maskedOp2 = environment.getNextVariableString();
    final String subResult = environment.getNextVariableString();
    final String msbResult = environment.getNextVariableString();
    final String msbSameBefore = environment.getNextVariableString();
    final String msbHasChanged = environment.getNextVariableString();
    final String tempOf = environment.getNextVariableString();
    final String tempCf = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();

    // Isolate the MSBs of the two operands
    instructions.add(ReilHelpers.createAnd(offset, size, operand1, size, msbMask, size, maskedOp1));
    instructions.add(ReilHelpers.createAnd(offset + 1, size, operand2, size, msbMask, size,
        maskedOp2));

    // Perform the subtraction
    instructions.add(ReilHelpers.createSub(offset + 2, size, operand1, size, operand2, resultSize,
        subResult));

    // Isolate the MSB of the result and put it into the Sign Flag
    instructions.add(ReilHelpers.createAnd(offset + 3, resultSize, subResult, resultSize, msbMask,
        size, msbResult));
    instructions.add(ReilHelpers.createBsh(offset + 4, size, msbResult, size, shiftMsbLsb,
        OperandSize.BYTE, SIGN_FLAG));

    // Find out if the MSB of the two operands were different and whether the MSB of the first
    // operand changed
    instructions.add(ReilHelpers.createXor(offset + 5, size, maskedOp1, size, maskedOp2, size,
        msbSameBefore));
    instructions.add(ReilHelpers.createXor(offset + 6, size, maskedOp1, size, msbResult, size,
        msbHasChanged));
    instructions.add(ReilHelpers.createAnd(offset + 7, size, msbSameBefore, size, msbHasChanged,
        size, tempOf));

    // Write the result into the Overflow Flag
    instructions.add(ReilHelpers.createBsh(offset + 8, size, tempOf, size, shiftMsbLsb,
        OperandSize.BYTE, OVERFLOW_FLAG));

    // Update the Carry Flag
    instructions.add(ReilHelpers.createAnd(offset + 9, resultSize, subResult, resultSize,
        carryMask, resultSize, tempCf));
    instructions.add(ReilHelpers.createBsh(offset + 10, resultSize, tempCf, resultSize,
        shiftCarryLsb, OperandSize.BYTE, CARRY_FLAG));

    // Truncate the result to fit into the target
    instructions.add(ReilHelpers.createAnd(offset + 11, resultSize, subResult, resultSize,
        truncateMask, size, truncatedResult));

    // Update the Zero Flag
    instructions.add(ReilHelpers.createBisz(offset + 12, size, truncatedResult, OperandSize.BYTE,
        ZERO_FLAG));

    return truncatedResult;
  }

  /**
   * Returns a mask to mask out all bits but the carry bit.
   *
   * @param size The operand size to generate the mask for.
   * @return The carry mask.
   */
  public static long getCarryMask(final OperandSize size) {

    switch (size) {
      case BYTE:
        return 0x100;
      case WORD:
        return 0x10000;
      case DWORD:
        return 0x100000000L;
      case ADDRESS:
        break;
      case EMPTY:
        break;
      case OWORD:
        break;
      case QWORD:
        break;
      default:
        break;
    }

    throw new IllegalArgumentException("Error: Invalid argument size");
  }

  /**
   * Returns the value of the leaf of an operand expression.
   *
   * This function assumes that each child of the expression has at most 1 child.
   *
   * @param expression The expression in question
   *
   * @return The string value of the leaf expression.
   *
   * @throws IllegalArgumentException Thrown if the argument expression is null
   * @throws InternalTranslationException Thrown if any child of the expression has more than 1
   *         child.
   */
  public static String getLeafValue(final IOperandTreeNode expression)
      throws IllegalArgumentException, InternalTranslationException {

    Preconditions.checkNotNull(expression, "Error: Argument expression can't be null");

    final List<? extends IOperandTreeNode> children = expression.getChildren();

    if (children.size() == 0) {

      // We've reached leaf. Return the value now.
      return expression.getValue();

    } else if (children.size() == 1) {

      // Not yet at the leaf.
      return getLeafValue(children.get(0));

    } else {

      // The expression tree doesn't have a valid structure for this function.
      throw new InternalTranslationException("Error: Expression tree has invalid structure");

    }
  }

  /**
   * Returns the size of an operand
   *
   * @param operand The operand in question
   * @return Returns the size of an operand.
   */
  public static OperandSize getOperandSize(final IOperandTree operand) {
    Preconditions.checkNotNull(operand, "Error: Argument operand can't be null");

    // final String sizeString = "dword"; //operand.getAddress().getValue();
    final String sizeString = operand.getRootNode().getValue();

    return OperandSize.sizeStringToValue(sizeString);
  }

  /**
   * Returns the parent register of a subregister
   *
   * @param subRegister The subregister in question
   *
   * @return The parent register of the given subregister
   *
   * @throws InternalTranslationException Thrown if an invalid subregister is passed to the
   *         function.
   */
  public static String getParentRegister(final String subRegister)
      throws InternalTranslationException {

    Preconditions.checkNotNull(subRegister, "Error: Argument subRegister can't be null");

    if (subRegister.equals("al") || subRegister.equals("ah") || subRegister.equals("ax")) {
      return "eax";
    } else if (subRegister.equals("bl") || subRegister.equals("bh") || subRegister.equals("bx")) {
      return "ebx";
    } else if (subRegister.equals("cl") || subRegister.equals("ch") || subRegister.equals("cx")) {
      return "ecx";
    } else if (subRegister.equals("dl") || subRegister.equals("dh") || subRegister.equals("dx")) {
      return "edx";
    } else if (subRegister.equals("si")) {
      return "esi";
    } else if (subRegister.equals("di")) {
      return "edi";
    } else if (subRegister.equals("sp")) {
      return "esp";
    } else if (subRegister.equals("bp")) {
      return "ebp";
    } else if (subRegister.equals("ip")) {
      return "eip";
    } else {
      throw new InternalTranslationException(String.format("Error: Invalid subRegister %s",
          subRegister));
    }
  }

  /**
   * Returns the size of a register.
   *
   * @param register The name of a register
   *
   * @return The size of the passed register
   *
   * @throws InternalTranslationException Thrown if an invalid register name was passed to the
   *         function
   */
  public static OperandSize getRegisterSize(final String register)
      throws InternalTranslationException {
    if (register.equals("al") || register.equals("ah") || register.equals("bl")
        || register.equals("bh") || register.equals("cl") || register.equals("ch")
        || register.equals("dl") || register.equals("dh")) {
      return OperandSize.BYTE;
    } else if (register.equals("ax") || register.equals("bx") || register.equals("cx")
        || register.equals("dx") || register.equals("si") || register.equals("di")
        || register.equals("sp") || register.equals("bp") || register.equals("ip")
        || isSegment(register)) {
      return OperandSize.WORD;
    } else if (register.equals("eax") || register.equals("ebx") || register.equals("ecx")
        || register.equals("edx") || register.equals("esi") || register.equals("edi")
        || register.equals("esp") || register.equals("ebp")) {
      return OperandSize.DWORD;
    }

    throw new InternalTranslationException(String.format("Error: Invalid register name %s",
        register));
  }

  /**
   * Determines whether a passed register is either AH, BH, CH, or DH.
   *
   * @param register The name of the register
   *
   * @return True, if the name of the register is either AH, BH, CH, or DH. False, otherwise.
   */
  public static boolean isHigher8BitRegister(final String register) {

    return register.equals("ah") || register.equals("bh") || register.equals("ch")
        || register.equals("dh");
  }

  public static TranslationResult loadFirstDivOperand(final ITranslationEnvironment environment,
      final long offset, final OperandSize size) {

    final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final OperandSize archSize = environment.getArchitectureSize();

    if (size == OperandSize.BYTE) {

      final String dividend = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(offset, archSize, "eax", OperandSize.BYTE, "255",
          OperandSize.BYTE, dividend));

      return new TranslationResult(dividend, OperandSize.BYTE, TranslationResultType.REGISTER,
          null, instructions, offset);

    } else if (size == OperandSize.WORD) {

      final String extractedAx = environment.getNextVariableString();
      final String extractedDx = environment.getNextVariableString();
      final String shiftedDx = environment.getNextVariableString();
      final String dividend = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(offset, archSize, "eax", OperandSize.WORD, "65535",
          size, extractedAx));
      instructions.add(ReilHelpers.createAnd(offset + 1, archSize, "edx", OperandSize.WORD,
          "65535", size, extractedDx));
      instructions.add(ReilHelpers.createBsh(offset + 2, OperandSize.WORD, extractedDx,
          OperandSize.WORD, "16", OperandSize.DWORD, shiftedDx));
      instructions.add(ReilHelpers.createOr(offset + 3, OperandSize.WORD, extractedAx,
          OperandSize.DWORD, shiftedDx, OperandSize.DWORD, dividend));

      return new TranslationResult(dividend, OperandSize.DWORD, TranslationResultType.REGISTER,
          null, instructions, offset);
    } else if (size == OperandSize.DWORD) {

      final String shiftedEdx = environment.getNextVariableString();
      final String dividend = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(offset, OperandSize.DWORD, "edx", OperandSize.DWORD,
          "32", OperandSize.QWORD, shiftedEdx));
      instructions.add(ReilHelpers.createOr(offset + 1, OperandSize.DWORD, "eax",
          OperandSize.QWORD, shiftedEdx, OperandSize.QWORD, dividend));

      return new TranslationResult(dividend, OperandSize.QWORD, TranslationResultType.REGISTER,
          null, instructions, offset);
    } else {
      assert false;
      return null;
    }

  }

  /**
   * Writes a value into a subregister.
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset; new code is add here
   * @param valueSize Size of the value
   * @param value Value to write into the subregister
   * @param subRegister The target subregister
   * @param instructions List of instructions where the code is added
   *
   * @throws IllegalArgumentException Thrown if arguments passed to the function are invalid
   */
  public static void moveAndMask(final ITranslationEnvironment environment, final long offset,
      final OperandSize valueSize, final String value, final String subRegister,
      final List<ReilInstruction> instructions) throws IllegalArgumentException,
      InternalTranslationException, IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(subRegister, "Error: Argument subRegister can't be null");
    Preconditions.checkNotNull(valueSize, "Error: Argument valueSize can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final String parentRegister = getParentRegister(subRegister);

    final OperandSize registerSize = getRegisterSize(subRegister);
    final OperandSize parentRegisterSize = getRegisterSize(parentRegister);
    final OperandSize archSize = environment.getArchitectureSize();

    // The subregister is too large => Not a subregister
    if (registerSize.getByteSize() >= archSize.getByteSize()) {
      throw new InternalTranslationException("Error: Register is not a subregister");
    }

    // The value is too large => Doesn't fit into a subregister
    if (valueSize.getByteSize() >= archSize.getByteSize()) {
      throw new InternalTranslationException("Error: Value doesn't fit into a subregister");
    }

    // The value is too large => Doesn't fit into a parent register
    if (valueSize.getByteSize() >= parentRegisterSize.getByteSize()) {
      throw new InternalTranslationException("Error: Value doesn't fit into a parent register");
    }

    final String mask = String.valueOf(getNegativeMask(subRegister));

    if (isHigher8BitRegister(subRegister)) {

      // AH, BH, CH and DH must be shifted into position first.

      final String shiftedValue = environment.getNextVariableString();
      final String maskedValue = environment.getNextVariableString();

      // Shift the value into place.
      instructions.add(ReilHelpers.createBsh(offset, valueSize, value, valueSize, "8", archSize,
          shiftedValue));

      // Clear the parts of the parent register that will be overwritten
      instructions.add(ReilHelpers.createAnd(offset + 1, archSize, parentRegister, archSize, mask,
          archSize, maskedValue));

      // Write the new value into the parent register.
      instructions.add(ReilHelpers.createOr(offset + 2, archSize, shiftedValue, archSize,
          maskedValue, archSize, parentRegister));

    } else {

      // No shifting necessary for subregisters that are already at the LSB

      final String maskedValue = environment.getNextVariableString();

      // Clear the relevant parts of the parent register and fill it with the new value.
      instructions.add(ReilHelpers.createAnd(offset, archSize, parentRegister, archSize, mask,
          archSize, maskedValue));
      instructions.add(ReilHelpers.createOr(offset + 1, valueSize, value, archSize, maskedValue,
          archSize, parentRegister));
    }
  }

  /**
   * Shifts the x86 flags into a value (for pushf/pushfw)
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param size The size of the target value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return The value the flags were written to
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static String shiftFlagsIntoValue(final ITranslationEnvironment environment,
      final long offset, final OperandSize size, final List<ReilInstruction> instructions)
      throws IllegalArgumentException {

    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final String startValue = environment.getNextVariableString();
    final String afterCf = environment.getNextVariableString();
    final String shiftedPf = environment.getNextVariableString();
    final String afterPf = environment.getNextVariableString();
    final String shiftedAf = environment.getNextVariableString();
    final String afterAf = environment.getNextVariableString();
    final String shiftedZf = environment.getNextVariableString();
    final String afterZf = environment.getNextVariableString();
    final String shiftedSf = environment.getNextVariableString();
    final String afterSf = environment.getNextVariableString();
    final String shiftedOf = environment.getNextVariableString();
    final String afterOf = environment.getNextVariableString();

    instructions.add(ReilHelpers.createStr(offset, size, "2", size, startValue));

    instructions.add(ReilHelpers.createOr(offset + 1, size, startValue, OperandSize.BYTE,
        Helpers.CARRY_FLAG, size, afterCf));

    instructions.add(ReilHelpers.createBsh(offset + 2, OperandSize.BYTE, Helpers.PARITY_FLAG,
        OperandSize.BYTE, "2", size, shiftedPf));
    instructions.add(ReilHelpers
        .createOr(offset + 3, size, afterCf, size, shiftedPf, size, afterPf));

    instructions.add(ReilHelpers.createBsh(offset + 4, OperandSize.BYTE, Helpers.AUXILIARY_FLAG,
        OperandSize.BYTE, "4", size, shiftedAf));
    instructions.add(ReilHelpers
        .createOr(offset + 5, size, afterPf, size, shiftedAf, size, afterAf));

    instructions.add(ReilHelpers.createBsh(offset + 6, OperandSize.BYTE, Helpers.ZERO_FLAG,
        OperandSize.BYTE, "6", size, shiftedZf));
    instructions.add(ReilHelpers
        .createOr(offset + 7, size, afterAf, size, shiftedZf, size, afterZf));

    instructions.add(ReilHelpers.createBsh(offset + 8, OperandSize.BYTE, Helpers.SIGN_FLAG,
        OperandSize.BYTE, "7", size, shiftedSf));
    instructions.add(ReilHelpers
        .createOr(offset + 9, size, afterZf, size, shiftedSf, size, afterSf));

    instructions.add(ReilHelpers.createBsh(offset + 10, OperandSize.BYTE, Helpers.OVERFLOW_FLAG,
        OperandSize.BYTE, "11", size, shiftedOf));
    instructions.add(ReilHelpers.createOr(offset + 11, size, afterSf, size, shiftedOf, size,
        afterOf));

    return afterOf;
  }

  /**
   * Fills the x86 flags with values from a given value. This function is for use with popf/popfw
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param value The source value
   * @param size The size of the source value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static void shiftValueIntoFlags(final ITranslationEnvironment environment,
      final long offset, final String value, final OperandSize size,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final String pfInLsb = environment.getNextVariableString();
    final String afInLsb = environment.getNextVariableString();
    final String zfInLsb = environment.getNextVariableString();
    final String sfInLsb = environment.getNextVariableString();
    final String ofInLsb = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(offset, size, value, size, "1", OperandSize.BYTE,
        Helpers.CARRY_FLAG));

    instructions.add(ReilHelpers.createBsh(offset + 1, size, value, size, "-2", size, pfInLsb));
    instructions.add(ReilHelpers.createAnd(offset + 2, size, pfInLsb, size, "1", OperandSize.BYTE,
        Helpers.PARITY_FLAG));

    instructions.add(ReilHelpers.createBsh(offset + 3, size, value, size, "-4", size, afInLsb));
    instructions.add(ReilHelpers.createAnd(offset + 4, size, afInLsb, size, "1", OperandSize.BYTE,
        Helpers.AUXILIARY_FLAG));

    instructions.add(ReilHelpers.createBsh(offset + 5, size, value, size, "-6", size, zfInLsb));
    instructions.add(ReilHelpers.createAnd(offset + 6, size, zfInLsb, size, "1", OperandSize.BYTE,
        Helpers.ZERO_FLAG));

    instructions.add(ReilHelpers.createBsh(offset + 7, size, value, size, "-7", size, sfInLsb));
    instructions.add(ReilHelpers.createAnd(offset + 8, size, sfInLsb, size, "1", OperandSize.BYTE,
        Helpers.SIGN_FLAG));

    instructions.add(ReilHelpers.createBsh(offset + 9, size, value, size, "-11", size, ofInLsb));
    instructions.add(ReilHelpers.createAnd(offset + 10, size, ofInLsb, size, "1", OperandSize.BYTE,
        Helpers.OVERFLOW_FLAG));

  }

  /**
   * Translates an x86 operand to REIL code.
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset (the generated code is placed there)
   * @param operand The operand to translate
   * @param loadOperand A flag that indicates whether the operand should be loaded (if the operand
   *        is a memory access operand) or extracted (if the operand is a subregister).
   *
   * @return The result of the translation
   *
   * @throws InternalTranslationException Thrown if an internal problem occurs
   * @throws IllegalArgumentException Thrown if any argument is null
   */

  public static TranslationResult translateOperand(final ITranslationEnvironment environment,
      final long offset, final IOperandTree operand, final boolean loadOperand)
      throws InternalTranslationException, IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(operand, "Error: Argument operand can't be null");

    return loadOperand(environment,
        offset,
        operand.getRootNode(),
        null,   // We are at the root of the tree, so no segment override yet.
        OperandSize.sizeStringToValue(operand.getRootNode().getValue()),
        loadOperand);
  }

  /**
   * Writes a value back into a target.
   *
   * @param environment A valid translation environment.
   * @param offset The next unused REIL offset; the new code is written there.
   * @param targetOperand The target operand where the value is written to
   * @param sourceValue The value that's written
   * @param size
   * @param address Target address (in case the type is MEMORY_ACCESS) or null (in case it's not)
   * @param targetType Target type (Either REGISTER or MEMORY_ACCESS)
   * @param instructions The new code is added to this list of instructions
   *
   * @throws IllegalArgumentException Thrown if invalid arguments were passed to the function.
   * @throws InternalTranslationException Thrown if invalid argument combinations were passed to the
   *         function.
   */
  public static void writeBack(final ITranslationEnvironment environment, final long offset,
      final IOperandTree targetOperand, final String sourceValue, final OperandSize size,
      final String address, final TranslationResultType targetType,
      final List<ReilInstruction> instructions) throws IllegalArgumentException,
      InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(targetOperand, "Error: Argument targetOperand can't be null");
    Preconditions.checkNotNull(sourceValue, "Error: Argument sourceValue can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(targetType, "Error: Argument targetType can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    // TODO: Exact meaning of parameter size

    final OperandSize archSize = environment.getArchitectureSize();

    if (targetType == TranslationResultType.REGISTER) {

      if (address != null) {
        throw new InternalTranslationException("Error: Invalid combination of parameters");
      }

      // The target is a register. Either write the value directly or mask it into the target

      final String target = getLeafValue(targetOperand.getRootNode());

      if ((size == archSize) || isSegment(target)) {
        instructions.add(ReilHelpers.createStr(offset, size, sourceValue, size, target));
      } else {
        moveAndMask(environment, offset, size, sourceValue, target, instructions);
      }

    } else if (targetType == TranslationResultType.MEMORY_ACCESS) {

      // The target is a memory address. Store the value there.

      instructions.add(ReilHelpers.createStm(offset, size, sourceValue, archSize, address));
    } else {
      throw new InternalTranslationException("Error: Invalid target type");
    }
  }

  /**
   * Writes the parity flag according to result.
   *
   * @param environment A valid translation environment.
   * @param offset The next unused REIL offset; the new code is written there.
   * @param resultSize The size of the register or memory block containing result.
   * @param result The result of an arithmetic operation that is used to compute the parity flag.
   * @param instructions The new code is added to this list of instructions
   *
   * @throws IllegalArgumentException Thrown if invalid arguments were passed to the function.
   */
  public static void writeParityFlag(final ITranslationEnvironment environment, final long offset,
                                     final OperandSize resultSize, final String result,
                                     final List<ReilInstruction> instructions)
                                     throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(resultSize, "Error: Argument resultSize can't be null");
    Preconditions.checkNotNull(result, "Error: Argument result can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final String tempReg = environment.getNextVariableString();

    // In x86 the parity flag depends only on the 8 LSB of the corresponding result.
    // We set PARITY_FLAG = result ^ (result >> 4), so that the 4 LSB of PARITY_FLAG
    // have the same parity as the 8 LSB of result.
    instructions.add(ReilHelpers.createStr(offset, resultSize, result, resultSize, tempReg));
    instructions.add(ReilHelpers.createBsh(offset + 1, resultSize, tempReg,
        OperandSize.BYTE, "-4", resultSize, Helpers.PARITY_FLAG));
    instructions.add(ReilHelpers.createXor(offset + 2, resultSize, tempReg,
        resultSize, Helpers.PARITY_FLAG, resultSize, Helpers.PARITY_FLAG));
    // Mask off all but the 4 LSB of PARITY_FLAG.
    instructions.add(ReilHelpers.createAnd(offset + 3, resultSize, Helpers.PARITY_FLAG,
        resultSize, String.valueOf(0xFFL), OperandSize.WORD, Helpers.PARITY_FLAG));
    // For i = 0, ..., 15, the (16-i)-th rightmost bit of 0x9669 is the parity of i.
    // We set PARITY_FLAG = ((38505 << PARITY_FLAG) & (1 << 15)) >> 15.
    instructions.add(ReilHelpers.createBsh(offset + 4, OperandSize.WORD, String.valueOf(0x9669L),
        OperandSize.WORD, Helpers.PARITY_FLAG, OperandSize.WORD, Helpers.PARITY_FLAG));
    instructions.add(ReilHelpers.createAnd(offset + 5, OperandSize.WORD, Helpers.PARITY_FLAG,
        OperandSize.WORD, String.valueOf(0x8000L), OperandSize.WORD, Helpers.PARITY_FLAG));
    instructions.add(ReilHelpers.createBsh(offset + 6, OperandSize.WORD, Helpers.PARITY_FLAG,
        OperandSize.BYTE, "-15", OperandSize.BYTE, Helpers.PARITY_FLAG));
  }

  public static ArrayList<ReilInstruction> writeDivResult(
      final ITranslationEnvironment environment, final long offset, final String realDivResult,
      final String realModResult, final OperandSize size) {

    final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final OperandSize archSize = environment.getArchitectureSize();

    if (size == OperandSize.BYTE) {

      final String maskedEax = environment.getNextVariableString();
      final String partEax = environment.getNextVariableString();
      final String shiftedModResult = environment.getNextVariableString();

      // Write the div result into AL
      instructions.add(ReilHelpers.createAnd(offset, archSize, "eax", archSize, "4294901760",
          archSize, maskedEax));
      instructions.add(ReilHelpers.createOr(offset + 1, archSize, maskedEax, size, realDivResult,
          archSize, partEax));

      // Write the mod result into AH
      instructions.add(ReilHelpers.createBsh(offset + 2, size, realModResult, size, "8", archSize,
          shiftedModResult));
      instructions.add(ReilHelpers.createOr(offset + 3, archSize, partEax, archSize,
          shiftedModResult, archSize, "eax"));

      return instructions;

    } else if (size == OperandSize.WORD) {

      final String maskedEax = environment.getNextVariableString();
      final String maskedEdx = environment.getNextVariableString();

      // Write the div result into AX
      instructions.add(ReilHelpers.createAnd(offset, archSize, "eax", archSize, "4294901760",
          archSize, maskedEax));
      instructions.add(ReilHelpers.createOr(offset + 1, archSize, maskedEax, size, realDivResult,
          archSize, "eax"));

      // Write the mod result into DX
      instructions.add(ReilHelpers.createAnd(offset + 2, archSize, "edx", archSize, "4294901760",
          archSize, maskedEdx));
      instructions.add(ReilHelpers.createOr(offset + 3, archSize, maskedEdx, size, realDivResult,
          archSize, "edx"));

      return instructions;

    } else if (size == OperandSize.DWORD) {

      instructions.add(ReilHelpers.createStr(offset, size, realDivResult, size, "eax"));
      instructions.add(ReilHelpers.createStr(offset + 1, size, realModResult, size, "edx"));

      return instructions;
    } else {
      assert false;
      return null;
    }
  }

  public static ArrayList<ReilInstruction> writeMulResult(
      final ITranslationEnvironment environment, final long offset, final String result,
      final OperandSize size) {

    final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final OperandSize archSize = environment.getArchitectureSize();

    if (size == OperandSize.BYTE) {

      // Store the result in AX

      final String maskedEax = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(offset, archSize, "eax", archSize, "4294901760",
          archSize, maskedEax));
      instructions.add(ReilHelpers.createOr(offset + 1, OperandSize.WORD, result, archSize,
          maskedEax, archSize, "eax"));

      return instructions;

    } else if (size == OperandSize.WORD) {

      // Store the result in DX:AX

      final String maskResNeg = "4294901760";

      final String maskedEax = environment.getNextVariableString();
      final String maskedResult = environment.getNextVariableString();
      final String maskedEdx = environment.getNextVariableString();
      final String shiftedResult = environment.getNextVariableString();

      // Store the lower half in AX
      instructions.add(ReilHelpers.createAnd(offset, OperandSize.DWORD, "eax", OperandSize.DWORD,
          maskResNeg, OperandSize.DWORD, maskedEax));
      instructions.add(ReilHelpers.createAnd(offset + 1, OperandSize.DWORD, result,
          OperandSize.DWORD, "65535", OperandSize.DWORD, maskedResult));
      instructions.add(ReilHelpers.createOr(offset + 2, OperandSize.DWORD, maskedEax,
          OperandSize.DWORD, maskedResult, OperandSize.DWORD, "eax"));

      // Store the upper half in DX
      instructions.add(ReilHelpers.createAnd(offset + 3, OperandSize.DWORD, "edx",
          OperandSize.DWORD, maskResNeg, OperandSize.DWORD, maskedEdx));
      instructions.add(ReilHelpers.createBsh(offset + 4, OperandSize.DWORD, result,
          OperandSize.DWORD, "-16", OperandSize.DWORD, shiftedResult));
      instructions.add(ReilHelpers.createOr(offset + 5, OperandSize.DWORD, maskedEdx,
          OperandSize.DWORD, shiftedResult, OperandSize.DWORD, "edx"));

      return instructions;

    } else if (size == OperandSize.DWORD) {

      // Store the result in EDX:EAX

      instructions.add(ReilHelpers.createAnd(offset, OperandSize.QWORD, result, OperandSize.DWORD,
          "4294967295", OperandSize.DWORD, "eax"));
      instructions.add(ReilHelpers.createBsh(offset + 1, OperandSize.QWORD, result,
          OperandSize.QWORD, "-32", OperandSize.DWORD, "edx"));

      return instructions;

    } else {
      assert false;
      return instructions;
    }

  }
}
