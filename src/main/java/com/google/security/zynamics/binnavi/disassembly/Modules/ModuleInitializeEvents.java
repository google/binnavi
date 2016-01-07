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
package com.google.security.zynamics.binnavi.disassembly.Modules;

/**
 * Event issued during module initialization.
 */
public enum ModuleInitializeEvents {
  /**
   * Module initialization begins.
   */
  Starting,

  /**
   * The expression tree is created.
   */
  CreatingExpressionTree,

  /**
   * The instructions are imported.
   */
  ImportingInstructions,

  /**
   * The operands are imported.
   */
  ImportingOperands,

  /**
   * The operand expressions are imported.
   */
  ImportingOperandExpressions,

  /**
   * The operand substitutions are imported.
   */
  ImportingExpressionSubstitutions,

  /**
   * The address references are imported.
   */
  ImportingAddressReferences,

  /**
   * The functions are imported.
   */
  ImportingFunctions,

  /**
   * The edges are imported.
   */
  ImportingEdges,

  /**
   * The members of base types are imported.
   */
  ImportingTypeMembers,

  /**
   * The type expression substitutions are imported.
   */
  ImportingTypeSubstitutions,

  /**
   * The base types are imported.
   */
  ImportingBaseTypes,

  /**
   * The flow graph views are created.
   */
  CreatingFlowgraphs,

  /**
   * The call graph view is created.
   */
  CreatingCallgraph,

  /**
   * Initialization is done.
   */
  Finished
}
