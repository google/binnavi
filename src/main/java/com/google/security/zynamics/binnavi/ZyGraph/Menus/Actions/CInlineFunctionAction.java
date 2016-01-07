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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphInliner;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;

/**
 * Action class used to inline a function into a code node.
 */
public final class CInlineFunctionAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 12562710513373420L;

  /**
   * Model of the graph where the function is inlined.
   */
  private final CGraphModel m_model;

  /**
   * Code node where the inlining happens.
   */
  private final INaviCodeNode m_node;

  /**
   * Instruction where the inlining operation happens.
   */
  private final INaviInstruction m_instruction;

  /**
   * Function that is inlined.
   */
  private final INaviFunction m_function;

  /**
   * Creates a new action object.
   * 
   * @param model Model of the graph where the function is inlined.
   * @param node Code node where the inlining happens.
   * @param instruction Instruction where the inlining operation happens.
   * @param function Function that is inlined.
   */
  public CInlineFunctionAction(final CGraphModel model, final INaviCodeNode node,
      final INaviInstruction instruction, final INaviFunction function) {
    super(String.format("Inline function %s",
        Preconditions.checkNotNull(function, "IE02879: function argument can not be null").getName()));

    m_model = Preconditions.checkNotNull(model, "IE02160: Model argument can not be null");
    m_node = Preconditions.checkNotNull(node, "IE02161: Node argument can not be null");
    m_instruction =
        Preconditions.checkNotNull(instruction, "IE02162: Instruction argument can not be null");
    m_function = function;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphInliner.inlineFunction(m_model.getParent(), m_model.getViewContainer(),
        m_model.getGraph(), m_node, m_instruction, m_function);
  }
}
