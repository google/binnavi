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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceCombinationFunctions;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;


/**
 * Action class for combining trace lists with the set-difference operator.
 */
public final class CActionDifferenceTraces extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8908506695025334568L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Creates the new combined trace.
   */
  private final ITraceListProvider m_traceProvider;

  /**
   * First operand of the set difference operation.
   */
  private final TraceList m_trace1;

  /**
   * Second operand of the set difference operation.
   */
  private final TraceList m_trace2;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param traceProvider Creates the new combined trace.
   * @param trace1 First operand of the set difference operation.
   * @param trace2 Second operand of the set difference operation.
   */
  public CActionDifferenceTraces(final JFrame parent, final ITraceListProvider traceProvider,
      final TraceList trace1, final TraceList trace2) {
    super(String.format("%s - %s", trace1.getName(), trace2.getName()));

    m_parent = parent;
    m_traceProvider = traceProvider;
    m_trace1 = trace1;
    m_trace2 = trace2;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTraceCombinationFunctions.differenceTraces(m_parent, m_traceProvider, m_trace1, m_trace2);
  }
}
