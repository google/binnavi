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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceCombinationFunctions;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;


/**
 * Action class for combining trace lists with the set-union operator.
 */
public final class CActionCombineTraces extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6732921925572053363L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Creates the new combined trace.
   */
  private final ITraceListProvider m_traceProvider;

  /**
   * The traces to combine.
   */
  private final List<TraceList> m_traces;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param traceProvider Creates the new combined trace.
   * @param traces The traces to combine.
   */
  public CActionCombineTraces(
      final JFrame parent, final ITraceListProvider traceProvider, final List<TraceList> traces) {
    super("Union");

    m_parent = parent;
    m_traceProvider = traceProvider;
    m_traces = new ArrayList<TraceList>(traces);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTraceCombinationFunctions.unionizeTraces(m_parent, m_traceProvider, m_traces);
  }
}
