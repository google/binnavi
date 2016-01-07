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
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CNodeDeleter;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class that can be used to delete an instruction from a node.
 */
public final class CActionDeleteInstruction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2286676933315382911L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph the node belongs to.
   */
  private final ZyGraph m_graph;

  /**
   * Node from which the instruction is deleted.
   */
  private final NaviNode m_codeNode;

  /**
   * Instruction to delete from the node.
   */
  private final INaviInstruction m_instruction;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph the node belongs to.
   * @param codeNode Node from which the instruction is deleted.
   * @param instruction Instruction to delete from the node.
   */
  public CActionDeleteInstruction(final JFrame parent, final ZyGraph graph,
      final NaviNode codeNode, final INaviInstruction instruction) {
    super("Delete Instruction");

    m_parent = Preconditions.checkNotNull(parent, "IE00926: Parent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE00927: Graph argument can not be null");
    m_codeNode = Preconditions.checkNotNull(codeNode, "IE00928: Code node argument can't be null");
    m_instruction =
        Preconditions.checkNotNull(instruction, "IE00929: Instruction argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CNodeDeleter.deleteInstruction(m_parent, m_graph, m_codeNode, m_instruction);
  }
}
