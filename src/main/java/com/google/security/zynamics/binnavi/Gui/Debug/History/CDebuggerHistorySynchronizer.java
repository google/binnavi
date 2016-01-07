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
package com.google.security.zynamics.binnavi.Gui.Debug.History;

import javax.swing.JTextPane;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;


/**
 * Synchronizes the history log shown in the debugger history panel with the events arriving from
 * the debug client.
 */
public class CDebuggerHistorySynchronizer {
  /**
   * Builds string output for the debug history.
   */
  private final CHistoryStringBuilder m_historyStringBuilder = new CHistoryStringBuilder();

  /**
   * Provides the active debugger.
   */
  private final CDebugPerspectiveModel m_model;

  /**
   * Text area where debug messages are shown.
   */
  private final JTextPane m_area;

  /**
   * Reacts to changes in the active debugger.
   */
  private final IDebugPerspectiveModelListener m_perspectiveListener =
      new CDebugPerspectiveModelListenerAdapter() {
        @Override
        public void changedActiveDebugger(
            final IDebugger oldDebugger, final IDebugger newDebugger) {
          if (m_area.isEnabled()) {
            synchronizeDebugger(newDebugger);
          }
        }
      };

  /**
   * Listens on changes in the string builder.
   */
  private final IHistoryStringBuilderListener m_builderListener =
      new InternalStringBuilderListener();

  /**
   * Creates a new synchronizer object.
   *
   * @param model Provides the active debugger.
   * @param area Text area where debug messages are shown.
   */
  public CDebuggerHistorySynchronizer(final CDebugPerspectiveModel model, final JTextPane area) {
    m_model = model;
    m_area = area;

    m_area.setEnabled(false);

    model.addListener(m_perspectiveListener);
    m_historyStringBuilder.addListener(m_builderListener);
  }

  /**
   * Keeps the listeners attached to the right debugger.
   *
   * @param newDebugger The now active debugger.
   */
  private void synchronizeDebugger(final IDebugger newDebugger) {
    m_historyStringBuilder.setDebugger(newDebugger);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_model.removeListener(m_perspectiveListener);
    m_historyStringBuilder.removeListener(m_builderListener);

    synchronizeDebugger(null);
  }

  /**
   * Enables or disabled event logging.
   *
   * @param enabled True, to enable event logging. False, to disable it.
   */
  public void setEnabled(final boolean enabled) {
    if (enabled) {
      synchronizeDebugger(m_model.getCurrentSelectedDebugger());
    } else {
      synchronizeDebugger(null);
    }

    m_area.setEnabled(enabled);
  }

  /**
   * Listens on changes in the string builder.
   */
  private class InternalStringBuilderListener implements IHistoryStringBuilderListener {
    @Override
    public void changedText(final String text) {
      m_area.setText(text);

      m_area.setSelectionStart(m_area.getText().length());
    }
  }
}
