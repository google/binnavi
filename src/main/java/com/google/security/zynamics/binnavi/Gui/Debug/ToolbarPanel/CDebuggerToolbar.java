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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CAttachAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CDetachAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CHaltAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CResumeAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CStartTraceAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CStepBlockAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CStepEndAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CStepIntoAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CStepOverAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CStopTraceAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions.CTerminateAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * This is the toolbar that is displayed in each debugger panel. The user can send commands like
 * Attach/Detach/Single Step/... to this toolbar.
 */
public final class CDebuggerToolbar extends JToolBar {
  /**
   * Debugger that is controlled from the toolbar.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * Action that is used to detach from the target process
   */
  private final Action m_detachAction;

  /**
   * Action that is used to connect to the debug client
   */
  private final Action m_startAction;

  /**
   * Action that is used to terminate the debug client
   */
  private final Action m_terminateAction;

  /**
   * Action that is used to single step
   */
  private final Action m_stepIntoAction;

  /**
   * Action that is used to step over a sub-function
   */
  private final Action m_stepOverAction;

  /**
   * Action that is used to start the tracer
   */
  private final Action m_startTraceAction;

  /**
   * Action that is used to stop the trace
   */
  private final Action m_stopTraceAction;

  /**
   * Action that is used to step to the next basic block
   */
  private final Action m_stepBlockAction;

  /**
   * Action that is used to step to the end of a function.
   */
  private final Action m_stepEndAction;

  /**
   * Action that is used to resume the target process
   */
  private final Action m_resumeAction;

  /**
   * Button used to detach from the target process
   */
  private final JButton m_detachButton;

  /**
   * Button used to terminate the target process
   */
  private final JButton m_terminateButton;

  /**
   * Action used to halt the target debugger
   */
  private final Action m_haltAction;

  /**
   * Button used to halt the target debugger
   */
  private final JButton m_haltButton;

  /**
   * Creates a new debugger toolbar object that can be used by the user to interact with the
   * debugger.
   *
   * @param debugger Debugger that is controlled from the toolbar.
   */
  public CDebuggerToolbar(final CDebugPerspectiveModel debugger) {
    m_debugger = debugger;
    setFloatable(false);
    final IGraphModel model = debugger.getGraphModel();
    final CGraphWindow parent = model.getParent();
    final ZyGraph graph = model.getGraph();
    // Initialize the action objects for the debug commands
    m_startAction = new CAttachAction(parent, debugger);
    m_detachAction = new CDetachAction(parent, debugger);
    m_terminateAction = new CTerminateAction(parent, debugger);
    m_stepIntoAction = new CStepIntoAction(parent, debugger);
    m_stepOverAction = new CStepOverAction(parent, debugger, graph);
    m_stepBlockAction = new CStepBlockAction(parent, debugger, graph);
    m_stepEndAction = new CStepEndAction(parent, debugger, graph);
    m_resumeAction = new CResumeAction(parent, debugger);
    m_haltAction = new CHaltAction(parent, debugger);
    m_startTraceAction = new CStartTraceAction(parent, debugger, graph);
    m_stopTraceAction = new CStopTraceAction(parent, debugger);
    createAndAddIconToToolbar(m_startAction, "data/startdebugger_up.jpg",
        "data/startdebugger_hover.jpg", "data/startdebugger_down.jpg");
    m_detachButton = createAndAddIconToToolbar(m_detachAction, "data/detachdebugger_up.jpg",
        "data/detachdebugger_hover.jpg", "data/detachdebugger_down.jpg");
    m_terminateButton = createAndAddIconToToolbar(m_terminateAction,
        "data/terminatedebugger_up.jpg", "data/terminatedebugger_hover.jpg",
        "data/terminatedebugger_down.jpg");
    final JButton stepButton = createAndAddIconToToolbar(m_stepIntoAction, "data/stepnext_up.jpg",
        "data/stepnext_hover.jpg", "data/stepnext_down.jpg");
    addHotkey(stepButton, HotKeys.DEBUGGER_SINGLE_STEP_KEY.getKeyStroke(), m_stepIntoAction,
        HotKeys.DEBUGGER_SINGLE_STEP_KEY.getDescription());
    final JButton stepOverButton = createAndAddIconToToolbar(m_stepOverAction,
        "data/stepover_up.jpg", "data/stepover_hover.jpg", "data/stepover_down.jpg");
    addHotkey(stepOverButton, HotKeys.DEBUGGER_STEP_OVER_KEY.getKeyStroke(), m_stepOverAction,
        HotKeys.DEBUGGER_STEP_OVER_KEY.getDescription());
    final JButton stepBlockButton = createAndAddIconToToolbar(m_stepBlockAction,
        "data/stepnextnode_up.jpg", "data/stepnextnode_hover.jpg", "data/stepnextnode_down.jpg");
    addHotkey(stepBlockButton, HotKeys.DEBUGGER_STEP_BLOCK_KEY.getKeyStroke(), m_stepBlockAction,
        HotKeys.DEBUGGER_STEP_BLOCK_KEY.getDescription());
    createAndAddIconToToolbar(m_stepEndAction, "data/step_to_end_up.png",
        "data/step_to_end_hover.png", "data/step_to_end_down.png");
    final JButton resumeButton = createAndAddIconToToolbar(m_resumeAction,
        "data/resumedebugger_up.jpg", "data/resumedebugger_hover.jpg",
        "data/resumedebugger_down.jpg");
    addHotkey(resumeButton, HotKeys.DEBUGGER_RESUME_KEY.getKeyStroke(), m_resumeAction,
        HotKeys.DEBUGGER_RESUME_KEY.getDescription());
    m_haltButton = createAndAddIconToToolbar(m_haltAction, "data/suspenddebugger_up.jpg",
        "data/suspenddebugger_hover.jpg", "data/suspenddebugger_down.jpg");
    createAndAddIconToToolbar(m_startTraceAction, "data/record_up.jpg", "data/record_hover.jpg",
        "data/record_down.jpg");
    createAndAddIconToToolbar(m_stopTraceAction, "data/recordstop_up.jpg",
        "data/recordstop_hover.jpg", "data/recordstop_down.jpg");

  }

  /**
   * Assigns a hotkey to an action.
   *
   * @param button The button the action belongs to.
   * @param keyStroke The hotkey.
   * @param action The action to be triggered on the hotkey.
   * @param name The name of the hotkey.
   */
  private static void addHotkey(final JButton button, final KeyStroke keyStroke,
      final Action action, final String name) {
    final InputMap windowImap = button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    windowImap.put(keyStroke, name);
    button.getActionMap().put(name, action);
  }

  /**
   * Small helper function for adding buttons to the toolbar.
   *
   * @param action Action associated with the new button.
   * @param defaultIconPath Path to the default icon for the button.
   * @param rolloverIconPath Path to the roll-over icon for the button.
   * @param pressedIconPath Path to the pressed icon for the button.
   *
   * @return The created button.
   */
  private JButton createAndAddIconToToolbar(final Action action, final String defaultIconPath,
      final String rolloverIconPath, final String pressedIconPath) {
    final JButton button = add(CActionProxy.proxy(action));
    button.setBorder(new EmptyBorder(0, 0, 0, 0));
    button.setIcon(new ImageIcon(CMain.class.getResource(defaultIconPath)));
    button.setRolloverIcon(new ImageIcon(CMain.class.getResource(rolloverIconPath)));
    button.setPressedIcon(new ImageIcon(CMain.class.getResource(pressedIconPath)));
    return button;
  }

  /**
   * Updates the toolbar depending on the debugger options of the target debugger.
   *
   * @param options Debugger options the toolbar layout depends on.
   */
  public void updateFromDebuggerOptions(final DebuggerOptions options) {
    Preconditions.checkNotNull(options, "IE01521: Options argument can not be null");
    m_detachButton.setEnabled(options.canDetach());
    m_terminateButton.setEnabled(options.canTerminate());
    m_haltButton.setEnabled(options.canHalt());
    updateGui();
  }

  /**
   * Updates the GUI according to the currently available information about the target process.
   */
  public void updateGui() {
    // Note: There is a reason for why we have to update the toolbar
    // from the outside. The toolbar can not update itself because
    // the toolbar state depends on the state of the TID box in the
    // debug panel.
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        debugger == null ? null : debugger.getProcessManager().getActiveThread();
    final boolean connected = (debugger != null) && debugger.isConnected();
    final boolean suspended = connected && (activeThread != null);
    m_startAction.setEnabled(!connected);
    final boolean haltBeforeCommunicating = (debugger != null) && connected
        && (debugger.getProcessManager().getTargetInformation() != null) && debugger
            .getProcessManager().getTargetInformation().getDebuggerOptions()
            .mustHaltBeforeCommunicating();
    m_detachAction.setEnabled(connected && (!haltBeforeCommunicating || suspended));
    m_terminateAction.setEnabled(connected);
    m_stepIntoAction.setEnabled(connected && suspended);
    m_stepIntoAction.setEnabled(connected && suspended);
    m_stepOverAction.setEnabled(connected && suspended);
    m_stepBlockAction.setEnabled(connected && suspended);
    m_stepEndAction.setEnabled(connected && suspended);
    m_resumeAction.setEnabled(connected && suspended);
    m_haltAction.setEnabled(connected && !suspended);
    final boolean tracing =
        (debugger != null) && m_debugger.getTraceLogger(debugger).hasEchoBreakpoints();
    m_startTraceAction.setEnabled(connected && (!haltBeforeCommunicating || suspended));
    m_stopTraceAction.setEnabled(connected && tracing && (!haltBeforeCommunicating || suspended));
  }
}
