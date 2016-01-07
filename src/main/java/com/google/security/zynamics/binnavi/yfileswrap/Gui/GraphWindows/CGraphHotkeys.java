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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CResumeHotkeyAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CShowHotkeysAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CStepBlockHotkeyAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CStepIntoHotkeyAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CStepOverHotkeyAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto.CGotoAddressField;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchField;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import y.view.Graph2DView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Can be used to register the default hotkeys of a graph view.
 */
public final class CGraphHotkeys {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphHotkeys() {
  }

  /**
   * Registers the hotkeys used for debugging.
   * 
   * @param parent Parent window used for dialogs.
   * @param graph Graph to be debugged by these hotkeys.
   * @param debuggerProvider Provides the debugger to be used for debugging.
   * @param inputMap Input map where the hotkeys are registered.
   * @param actionMap Action map where the hotkey actions are stored.
   */
  private static void registerDebuggerKeys(final JFrame parent, final ZyGraph graph,
      final IFrontEndDebuggerProvider debuggerProvider, final InputMap inputMap,
      final ActionMap actionMap) {
    final CStepIntoHotkeyAction stepIntoAction =
        new CStepIntoHotkeyAction(parent, debuggerProvider);

    final CStepOverHotkeyAction stepOverAction =
        new CStepOverHotkeyAction(parent, graph, debuggerProvider);

    final CStepBlockHotkeyAction stepBlockAction =
        new CStepBlockHotkeyAction(parent, graph, debuggerProvider);

    final CResumeHotkeyAction resumeAction = new CResumeHotkeyAction(parent, debuggerProvider);

    inputMap.put(HotKeys.DEBUGGER_SINGLE_STEP_KEY.getKeyStroke(), "SINGLE_STEP");
    actionMap.put("SINGLE_STEP", stepIntoAction);

    inputMap.put(HotKeys.DEBUGGER_STEP_OVER_KEY.getKeyStroke(), "STEP_OVER");
    actionMap.put("STEP_OVER", stepOverAction);

    inputMap.put(HotKeys.DEBUGGER_STEP_BLOCK_KEY.getKeyStroke(), "STEP_BLOCK");
    actionMap.put("STEP_BLOCK", stepBlockAction);

    inputMap.put(HotKeys.DEBUGGER_RESUME_KEY.getKeyStroke(), "RESUME");
    actionMap.put("RESUME", resumeAction);
  }

  /**
   * Registers the hot keys used for searching through graphs.
   * 
   * @param view View searched by the hotkey actions.
   * @param searchField Search field used for searching through the graph.
   * @param inputMap Input map where the hotkeys are registered.
   * @param actionMap Action map where the hotkey actions are stored.
   */
  private static void registerSearchKeys(final Graph2DView view,
      final CGraphSearchField searchField, final InputMap inputMap, final ActionMap actionMap) {

    inputMap.put(HotKeys.GRAPH_SEARCHFIELD_FOCUS_KEY.getKeyStroke(), "FOCUS_SEARCHFIELD");
    view.getInputMap().put(HotKeys.GRAPH_SEARCH_NEXT_KEY.getKeyStroke(), "NEXT");
    inputMap.put(HotKeys.GRAPH_SEARCH_NEXT_ZOOM_KEY.getKeyStroke(), "NEXT_ZOOM");
    inputMap.put(HotKeys.GRAPH_SEARCH_PREVIOUS_KEY.getKeyStroke(), "PREVIOUS");
    inputMap.put(HotKeys.GRAPH_SEARCH_PREVIOUS_ZOOM_KEY.getKeyStroke(), "PREVIOUS_ZOOM");

    view.getActionMap().put("NEXT", new AbstractAction() {
      private static final long serialVersionUID = -7289167985570632361L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        searchField.centerNextSearchHit(false, false);
      }
    });

    actionMap.put("NEXT_ZOOM", new AbstractAction() {
      private static final long serialVersionUID = -74113347341296669L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        searchField.centerNextSearchHit(false, true);
      }

    });

    actionMap.put("PREVIOUS", new AbstractAction() {
      private static final long serialVersionUID = 5698412623106859554L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        searchField.centerNextSearchHit(true, false);
      }

    });

    actionMap.put("PREVIOUS_ZOOM", new AbstractAction() {
      /**
       * Used for serialization.
       */
      private static final long serialVersionUID = -5246885767421937156L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        searchField.centerNextSearchHit(true, true);
      }

    });

    actionMap.put("FOCUS_SEARCHFIELD", new AbstractAction() {
      /**
       * Used for serialization.
       */
      private static final long serialVersionUID = 7918948798657638098L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        searchField.requestFocusInWindow();
      }
    });
  }

  /**
   * Register the default hotkeys of a graph view.
   * 
   * @param parent Parent window used for dialogs.
   * @param panel The panel where the view is shown.
   * @param debuggerProvider Provides the debugger used by some hotkeys.
   * @param searchField The search field that is shown in the graph panel.
   * @param addressField The address field that is shown in the graph panel.
   */
  public static void registerHotKeys(final JFrame parent, final CGraphPanel panel,
      final IFrontEndDebuggerProvider debuggerProvider, final CGraphSearchField searchField,
      final CGotoAddressField addressField) {
    Preconditions.checkNotNull(parent, "IE01606: Parent argument can not be null");
    Preconditions.checkNotNull(panel, "IE01607: Panel argument can not be null");
    Preconditions.checkNotNull(searchField, "IE01608: Search field argument can not be null");
    Preconditions.checkNotNull(addressField, "IE01609: Address field argument can not be null");

    final InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    final ActionMap actionMap = panel.getActionMap();

    inputMap.put(HotKeys.GRAPH_GOTO_ADDRESS_FIELD_KEY.getKeyStroke(), "GOTO_ADDRESS_FIELD");

    actionMap.put("GOTO_ADDRESS_FIELD", new AbstractAction() {
      /**
       * Used for serialization.
       */
      private static final long serialVersionUID = -8994014581850287793L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        addressField.requestFocusInWindow();
      }
    });

    inputMap.put(HotKeys.GRAPH_SHOW_HOTKEYS_ACCELERATOR_KEY.getKeyStroke(), "SHOW_HOTKEYS");
    actionMap.put("SHOW_HOTKEYS", new CShowHotkeysAction(parent));

    registerSearchKeys(panel.getModel().getGraph().getView(), searchField, inputMap, actionMap);

    registerDebuggerKeys(panel.getModel().getParent(), panel.getModel().getGraph(),
        debuggerProvider, inputMap, actionMap);
  }
}
