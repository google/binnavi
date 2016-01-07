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

import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CNodeFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Action class used for splitting code nodes.
 */
public class CActionSplitAfter extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4757485005546985298L;

  /**
   * View the node belongs to.
   */
  private final INaviView m_view;

  /**
   * Node to split.
   */
  private final INaviCodeNode m_node;

  /**
   * Instruction after which the node is split.
   */
  private final INaviInstruction m_instruction;

  /**
   * Creates a new action object.
   *
   * @param view View the node belongs to.
   * @param node Node to split.
   * @param instruction Instruction after which the node is split.
   */
  public CActionSplitAfter(
      final INaviView view, final INaviCodeNode node, final INaviInstruction instruction) {
    super("Split node after instruction");

    m_view = view;
    m_node = node;
    m_instruction = instruction;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CNodeFunctions.splitAfter(m_view, m_node, m_instruction);
  }
}
