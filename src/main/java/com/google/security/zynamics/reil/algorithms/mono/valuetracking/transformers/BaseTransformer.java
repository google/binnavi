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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers;

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.BitwiseAnd;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IElementGenerator;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Literal;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Register;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Symbol;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Undefined;
import com.google.security.zynamics.zylib.disassembly.CAddress;


/**
 * Base transformer that should be the base of all concrete transformer classes.
 */
public class BaseTransformer {
  private static IValueElement getOutputValue(final ReilOperand firstOperand,
      final IValueElement previousState1, final ReilOperand secondOperand,
      final IValueElement previousState2, final IElementGenerator generator) {
    if ((previousState1 == null) && (previousState2 == null)) {
      // MNEM R1, R2, R3 -> (R3 => R1 MNEM R2)

      return generator.generate(getAtomicType(firstOperand), getAtomicType(secondOperand));
    } else if ((previousState1 instanceof Undefined) || (previousState2 instanceof Undefined)) {
      return new Undefined();
    } else if ((previousState1 != null) && (previousState2 == null)) {
      // MNEM R1, R2, R3 -> (R3 => STATE(R1) MNEM R2)

      return generator.generate(previousState1, getAtomicType(secondOperand));
    } else if ((previousState1 == null) && (previousState2 != null)) {
      // MNEM R1, R2, R3 -> (R3 => R1 MNEM STATE(R2))

      return generator.generate(getAtomicType(firstOperand), previousState2);
    } else {
      // MNEM R1, R2, R3 -> (R3 => STATE(R1) MNEM STATE(R2))

      return generator.generate(previousState1, previousState2);
    }
  }

  /**
   * Determines whether the two input operands of an instruction are a literal (first operand) and a
   * register (second operand).
   * 
   * @param instruction The instruction whose input operands are checked.
   * 
   * @return True, if the first operand is a literal and the second operand is a register. False,
   *         otherwise.
   */
  private static boolean inputOperandsAreLiteralRegister(final ReilInstruction instruction) {
    final OperandType firstOperandType = instruction.getFirstOperand().getType();
    final OperandType secondOperandType = instruction.getSecondOperand().getType();

    return (firstOperandType == OperandType.INTEGER_LITERAL)
        && (secondOperandType == OperandType.REGISTER);
  }

  /**
   * Determines whether the two input operands of an instruction are both literals.
   * 
   * @param instruction The instruction whose input operands are checked.
   * 
   * @return True, if both input operands of the instruction are literals.
   */
  private static boolean inputOperandsAreLiterals(final ReilInstruction instruction) {
    final OperandType firstOperandType = instruction.getFirstOperand().getType();
    final OperandType secondOperandType = instruction.getSecondOperand().getType();

    return (firstOperandType == OperandType.INTEGER_LITERAL)
        && (secondOperandType == OperandType.INTEGER_LITERAL);
  }

  /**
   * Determines whether the two input operands of an instruction are both registers.
   * 
   * @param instruction The instruction whose input operands are checked.
   * 
   * @return True, if both input operands of the instruction are registers.
   */
  private static boolean inputOperandsAreRegisters(final ReilInstruction instruction) {
    final OperandType firstOperandType = instruction.getFirstOperand().getType();
    final OperandType secondOperandType = instruction.getSecondOperand().getType();

    return (firstOperandType == OperandType.REGISTER)
        && (secondOperandType == OperandType.REGISTER);
  }

  protected static IValueElement getAtomicType(final ReilOperand operand) {
    return operand.getType() == OperandType.INTEGER_LITERAL ? new Literal(new BigInteger(
        operand.getValue())) : new Symbol(new CAddress(0), operand.getValue());
  }

  protected static IValueElement getOperandValue(final ReilOperand operand,
      final ValueTrackerElement state) {
    return operand.getType() == OperandType.INTEGER_LITERAL ? new Literal(new BigInteger(
        operand.getValue())) : getState(state, operand);
  }

  protected static IValueElement getState(final ValueTrackerElement state,
      final ReilOperand inputOperand) {
    return state.getState(inputOperand.getValue());
  }

  /**
   * Determines whether the two input operands of an instruction are a register (first operand) and
   * a literal (second operand).
   * 
   * @param instruction The instruction whose input operands are checked.
   * 
   * @return True, if the first operand is a register and the second operand is a literal. False,
   *         otherwise.
   */
  protected static boolean inputOperandsAreRegisterLiteral(final ReilInstruction instruction) {
    final OperandType firstOperandType = instruction.getFirstOperand().getType();
    final OperandType secondOperandType = instruction.getSecondOperand().getType();

    return (firstOperandType == OperandType.REGISTER)
        && (secondOperandType == OperandType.INTEGER_LITERAL);
  }

  /**
   * Transforms an incoming lattice state to one that considers the state change brought by a given
   * instruction.
   * 
   * @param instruction The instruction that changes the given state.
   * @param incomingState The state to be transformed.
   * @param generator Combines the input operand values of the instruction.
   * 
   * @return The transformed state.
   */
  protected static ValueTrackerElement transform(final ReilInstruction instruction,
      final ValueTrackerElement incomingState, final IElementGenerator generator) {
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    Preconditions.checkNotNull(incomingState, "Error: incomingState argument can not be null");

    if (inputOperandsAreLiterals(instruction)) {
      return transformLiterals(instruction, incomingState, generator);
    } else if (inputOperandsAreRegisters(instruction)) {
      return transformRegisters(instruction, incomingState, generator);
    } else if (inputOperandsAreRegisterLiteral(instruction)
        || inputOperandsAreLiteralRegister(instruction)) {
      return transformRegisterLiteral(instruction, incomingState, generator);
    }

    throw new IllegalStateException("Not yet implemented");
  }

  /**
   * Transforms MNEM L1, L2, R1 to (R1 => CALC(L1, L2))
   * 
   * @param instruction The instruction in question.
   * @param incomingState The incoming state from the parents of the instruction.
   * @param generator Combines the input operand values of the instruction.
   * 
   * @return The new state of the graph node that represents the instruction.
   */
  protected static ValueTrackerElement transformLiterals(final ReilInstruction instruction,
      final ValueTrackerElement incomingState, final IElementGenerator generator) {
    // The easiest case is handling the combination of two integer literals.
    // The result of this combination is obviously another literal.

    final ReilOperand firstOperand = instruction.getFirstOperand();
    final ReilOperand secondOperand = instruction.getSecondOperand();
    final String thirdOperand = instruction.getThirdOperand().getValue();

    // Now we can update the state of the registers after this instruction is executed.
    // The register in the output operand is updated while the values of all other
    // registers are taken from the incoming state.

    final Register resultAloc = new Register(thirdOperand);
    final IValueElement alocValue =
        generator.generate(getAtomicType(firstOperand), getAtomicType(secondOperand));

    assert alocValue instanceof Literal : "The combination of two literals was not a literal";

    return incomingState.update(instruction, resultAloc, alocValue);
  }

  /**
   * Transforms MNEM R1, L1, R2 to (R2 => COMBINE(R1, L1))
   * 
   * @param instruction The instruction in question.
   * @param incomingState The incoming state from the parents of the instruction.
   * @param generator Combines the input operand values of the instruction.
   * 
   * @return The new state of the graph node that represents the instruction.
   */
  protected static ValueTrackerElement transformRegisterLiteral(final ReilInstruction instruction,
      final ValueTrackerElement incomingState, final IElementGenerator generator) {
    // Combine a register to a literal. This means we have to look up the state of the input
    // register
    // in the incoming state.

    final boolean registerFirst = inputOperandsAreRegisterLiteral(instruction);

    final ReilOperand registerOperand =
        registerFirst ? instruction.getFirstOperand() : instruction.getSecondOperand();
    final ReilOperand literalOperand =
        registerFirst ? instruction.getSecondOperand() : instruction.getFirstOperand();
    final ReilOperand outputOperand = instruction.getThirdOperand();

    final IValueElement previousState = incomingState.getState(registerOperand.getValue());

    final IValueElement result =
        getOutputValue(registerOperand, previousState, literalOperand, null, generator);

    if (previousState instanceof BitwiseAnd) {
      // MNEM R1, L1, R2 | R1 := X & Y -> (R2 => (X & Y) MNEM L1)

      return AndSimplifier.simplifyAnd(instruction, registerOperand, literalOperand,
          (BitwiseAnd) previousState, incomingState, generator);
    } else {
      return incomingState.update(instruction, new Register(outputOperand.getValue()), result);
    }
  }

  /**
   * Transforms MNEM R1, R2, R3 to (R3 => COMBINE(R1, R2))
   * 
   * @param instruction The instruction in question.
   * @param incomingState The incoming state from the parents of the instruction.
   * @param generator Combines the input operand values of the instruction.
   * 
   * @return The new state of the graph node that represents the instruction.
   */
  protected static ValueTrackerElement transformRegisters(final ReilInstruction instruction,
      final ValueTrackerElement incomingState, final IElementGenerator generator) {
    final ReilOperand firstOperand = instruction.getFirstOperand();
    final ReilOperand secondOperand = instruction.getSecondOperand();
    final ReilOperand thirdOperand = instruction.getThirdOperand();

    final IValueElement previousState1 = incomingState.getState(firstOperand.getValue());
    final IValueElement previousState2 = incomingState.getState(secondOperand.getValue());

    final IValueElement outputValue =
        getOutputValue(firstOperand, previousState1, secondOperand, previousState2, generator);

    return incomingState.update(instruction, new Register(thirdOperand.getValue()), outputValue);
  }
}
