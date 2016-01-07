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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations;

import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.helpers.RelocationChecker;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.math.BigInteger;
import java.util.List;



/**
 * Class that can be used to evaluate operand expressions.
 */
public final class CExpressionEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CExpressionEvaluator() {}

  /**
   * Evaluates a literal node.
   *
   * @param node The literal node to evaluate.
   * @param debugger The active debugger.
   * @param module The module the node belongs to.
   *
   * @return The evaluated value.
   */
  private static BigInteger evaluateInteger(final INaviOperandTreeNode node,
      final IDebugger debugger, final INaviModule module) {
    final int longValue = (int) (long) Long.valueOf(node.getValue());

    if (RelocationChecker.needsRelocation(node, module)) {
      return debugger.fileToMemory(module, new UnrelocatedAddress(new CAddress(longValue)))
          .getAddress().toBigInteger();
    } else {
      return BigInteger.valueOf(longValue);
    }
  }

  /**
   * Evaluates an operator node.
   *
   * @param node The operator node to evaluate.
   * @param registers Register values to use during expression evaluation.
   * @param debugger The active debugger.
   * @param module The module the node belongs to.
   *
   * @return The evaluated value.
   *
   * @throws CExpressionEvaluationException Thrown if the expression could not be evaluated.
   */
  private static BigInteger evaluateOperator(final INaviOperandTreeNode node,
      final ImmutableList<RegisterValue> registers, final IDebugger debugger,
      final INaviModule module) throws CExpressionEvaluationException {
    final List<INaviOperandTreeNode> children = node.getChildren();

    BigInteger initial = evaluateExpression(children.get(0), registers, debugger, module);

    for (int i = 1; i < children.size(); i++) {
      if (node.getValue().equals("+")) {
        initial = initial.add(evaluateExpression(children.get(i), registers, debugger, module));
      } else if (node.getValue().equals("-")) {
        initial =
            initial.subtract(evaluateExpression(children.get(i), registers, debugger, module));
      } else if (node.getValue().equals("*")) {
        initial =
            initial.multiply(evaluateExpression(children.get(i), registers, debugger, module));
      } else {
        throw new CExpressionEvaluationException(
            String.format("Unknown operand '%s'", node.getValue()));
      }
    }

    return initial;
  }

  /**
   * Evaluates a register node.
   *
   * @param node The register node to evaluate.
   * @param registers Register values to use during expression evaluation.
   *
   * @return The evaluated value.
   *
   * @throws CExpressionEvaluationException Thrown if the expression could not be evaluated.
   */
  private static BigInteger evaluateRegister(final INaviOperandTreeNode node,
      final ImmutableList<RegisterValue> registers) throws CExpressionEvaluationException {
    for (final RegisterValue registerValue : registers) {
      if (registerValue.getName().equalsIgnoreCase(node.getValue())) {
        return registerValue.getValue();
      }
    }

    throw new CExpressionEvaluationException(
        String.format("Value of register %s could not be determined", node.getValue()));
  }

  /**
   * Evaluates operand expressions.
   *
   * @param node Root node of the expression to evaluate.
   * @param registers Register values to use during expression evaluation.
   * @param debugger The active debugger.
   * @param module The module the node belongs to.
   *
   * @return The evaluated value.
   *
   * @throws CExpressionEvaluationException Thrown if the expression could not be evaluated.
   */
  public static BigInteger evaluateExpression(final INaviOperandTreeNode node,
      final ImmutableList<RegisterValue> registers, final IDebugger debugger,
      final INaviModule module) throws CExpressionEvaluationException {
    switch (node.getType()) {
      case IMMEDIATE_INTEGER:
        return evaluateInteger(node, debugger, module);
      case REGISTER:
        return evaluateRegister(node, registers);
      case OPERATOR:
        return evaluateOperator(node, registers, debugger, module);
      default:
        throw new IllegalStateException("IE00610: Unknown operand type");
    }
  }
}
