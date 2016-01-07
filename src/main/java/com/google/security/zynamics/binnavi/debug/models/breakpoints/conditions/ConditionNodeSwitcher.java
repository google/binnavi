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
package com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions;

/**
 * Class used to switch between condition tree nodes according to their concrete type.
 */
public class ConditionNodeSwitcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private ConditionNodeSwitcher() {}

  /**
   * Switches a condition tree node.
   *
   * @param <T> Return type of the function.
   *
   * @param node The node to switch.
   * @param switcher The switcher callback object.
   *
   * @return The return value of the switched method.
   */
  public static <T> T process(final ConditionNode node, final NodeSwitcher<T> switcher) {
    if (node instanceof ExpressionNode) {
      return switcher.process((ExpressionNode) node);
    } else if (node instanceof FormulaNode) {
      return switcher.process((FormulaNode) node);
    } else if (node instanceof IdentifierNode) {
      return switcher.process((IdentifierNode) node);
    } else if (node instanceof MemoryNode) {
      return switcher.process((MemoryNode) node);
    } else if (node instanceof NumberNode) {
      return switcher.process((NumberNode) node);
    } else if (node instanceof RelationNode) {
      return switcher.process((RelationNode) node);
    } else if (node instanceof SubNode) {
      return switcher.process((SubNode) node);
    } else {
      throw new IllegalStateException("IE00355: Unknown node type");
    }
  }

  /**
   * Callback interface for condition node switcher objects.
   *
   * @param <T> The return values of all methods.
   */
  public interface NodeSwitcher<T> {
    /**
     * Processes an expression node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(ExpressionNode node);

    /**
     * Processes a formula node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(FormulaNode node);

    /**
     * Processes an identifier node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(IdentifierNode node);

    /**
     * Processes a memory node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(MemoryNode node);

    /**
     * Processes a number node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(NumberNode node);

    /**
     * Processes a relation node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(RelationNode node);

    /**
     * Processes a sub node.
     *
     * @param node The node to process.
     *
     * @return Can return anything.
     */
    T process(SubNode node);
  }
}
