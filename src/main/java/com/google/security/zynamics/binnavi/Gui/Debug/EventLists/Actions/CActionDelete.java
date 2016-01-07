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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;

/**
 * Action class that is used to delete event lists.
 */
public final class CActionDelete extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1617294268068549549L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Trace list provider that manages the traces to be deleted.
   */
  private final ITraceListProvider m_traceListProvider;

  /**
   * List of indices of traces that are deleted by the action.
   */
  private final int[] m_traces;

  /**
   * Creates a new action that can be used to delete event lists.
   *
   * @param parent Parent window used for dialogs.
   * @param traceListProvider Trace list provider that manages the traces to be deleted.
   * @param traces List of indices of traces that are deleted by the action.
   */
  public CActionDelete(
      final JFrame parent, final ITraceListProvider traceListProvider, final int[] traces) {
    super("Delete");

    m_parent = Preconditions.checkNotNull(parent, "IE01376: Parent argument can't be null");
    m_traceListProvider = Preconditions.checkNotNull(
        traceListProvider, "IE01377: Trace list provider argument can't be null");
    m_traces = Preconditions.checkNotNull(traces, "IE01378: Traces argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTraceFunctions.deleteTrace(m_parent, m_traceListProvider, m_traces);
  }
}
