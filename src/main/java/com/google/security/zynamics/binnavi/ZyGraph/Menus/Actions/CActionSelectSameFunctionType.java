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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSelecter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

/**
 * Action class used to select all function nodes of a graph which have functions of a given type.
 */
public final class CActionSelectSameFunctionType extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6289781166754889730L;

  /**
   * Graph in which the selection event happens.
   */
  private final ZyGraph m_graph;

  /**
	 */
  private final FunctionType m_functionType;

  /**
   * Creates a new action object.
   *
   * @param graph Graph in which the selection event happens.
   * @param functionType Type of the functions to select.
   */
  public CActionSelectSameFunctionType(final ZyGraph graph, final FunctionType functionType) {
    super(String.format("Select functions of type %s", functionType));

    m_graph = Preconditions.checkNotNull(graph, "IE01285: Graph argument can't be null");
    m_functionType =
        Preconditions.checkNotNull(functionType, "IE01286: Function type can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphSelecter.selectNodesWithFunctionType(m_graph, m_functionType);
  }
}
