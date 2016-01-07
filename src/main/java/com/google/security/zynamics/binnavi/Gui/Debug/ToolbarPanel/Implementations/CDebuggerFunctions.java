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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CDebugEventNotifier;
import com.google.security.zynamics.binnavi.Gui.Debug.OptionsDialog.COptionsDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.storage.DebuggerEventSettingsStorage;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerExceptionHandlingAction;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

/**
 * Contains the implementations for the debugger functions that are available from the debugger GUI.
 */
public final class CDebuggerFunctions {
  /**
   * Checks arguments for validity.
   *
   * @param parent Parent argument to check.
   * @param debugger Debugger argument to check.
   */
  private static void checkArguments(final JFrame parent, final IDebugger debugger) {
    Preconditions.checkNotNull(parent, "IE01552: Parent argument can not be null");
    Preconditions.checkNotNull(debugger, "IE01553: Debugger argument can not be null");
  }

  /**
   * Checks arguments for validity.
   *
   * @param parent Parent argument to check.
   * @param debugger Debugger argument to check.
   * @param graph Graph object to check.
   */
  private static void checkArguments(final JFrame parent, final IDebugger debugger,
      final ZyGraph graph) {
    checkArguments(parent, debugger);
    Preconditions.checkNotNull(graph, "IE01555: Graph argument can not be null");
  }

  /**
   * Convert a list of exceptions to a map for fast lookup.
   *
   * @param exceptions The list of exceptions to be converted.
   * @return The map of exceptions.
   */
  private static Map<Long, DebuggerException> getExceptionsMap(
      final Collection<DebuggerException> exceptions) {
    final Map<Long, DebuggerException> result = new HashMap<Long, DebuggerException>();
    for (final DebuggerException exception : exceptions) {
      result.put(exception.getExceptionCode(), exception);
    }
    return result;
  }

  /**
   * Read the debugger event settings from the database storage.
   *
   * @param parent The parent dialog to show an error message.
   * @param debugger The currently active debugger.
   * @param target The currently active debug target.
   *
   * @return The CDebuggerEventSettings which has been deserialized from the database.
   */
  private static DebuggerEventSettings readDebuggerEventSettings(final JFrame parent,
      final IDebugger debugger, final DebugTargetSettings target) {
    final DebuggerEventSettingsStorage eventSettingsStorage =
        new DebuggerEventSettingsStorage(debugger, target);
    try {
      return eventSettingsStorage.deserialize();
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
      final String innerMessage = "Could not send debugger event settings command";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the debugger event settings command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The state of the debugged process remains unchanged."});
      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
    }
    return null;
  }

  /**
   * Sends the list of exceptions which need special handling by the debug client.
   *
   * @param parent The parent window used for dialogs.
   * @param debugger The debugger that receives the list of exceptions
   * @param exceptions The list of exceptions to be sent
   */
  private static void sendExceptionSettings(final JFrame parent, final IDebugger debugger,
      final Collection<DebuggerException> exceptions) {
    Preconditions.checkNotNull(exceptions, "IE00679: Exceptions argument can not be null");
    try {
      debugger.setExceptionSettings(exceptions);
    } catch (final DebugExceptionWrapper e) {
      CUtilityFunctions.logException(e);
      final String innerMessage = "Could not send exceptions settings command";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the exception settings command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The state of the debugged process remains unchanged."});
      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Write the Debugger Event Settings to the per-module storage.
   *
   * @param debugger The currently active debugger.
   * @param debugTarget The currently active debug target.
   * @param eventSettings The event settings to be stored in the database.
   *
   * @throws CouldntSaveDataException Thrown if the data could not be saved.
   */
  private static void writeDebuggerEventSettings(final IDebugger debugger,
      final DebugTargetSettings debugTarget, final DebuggerEventSettings eventSettings)
      throws CouldntSaveDataException {
    final DebuggerEventSettingsStorage eventSettingsStorage =
        new DebuggerEventSettingsStorage(debugger, debugTarget);
    eventSettingsStorage.serialize(eventSettings);
  }

  /**
   * Write the settings contained in CDebuggerOptions to the per-module storage.
   *
   * @param target The debug target to store the settings in the database.
   * @param debuggerId The Id of the currently active debugger.
   *
   * @throws CouldntSaveDataException
   */
  private static void writeDebuggerExceptionSettings(final DebuggerOptions options,
      final DebugTargetSettings target, final int debuggerId) throws CouldntSaveDataException {
    target.writeSetting("show_debugger_options", "1");
    for (final DebuggerException pe : options.getExceptions()) {
      target.writeSetting(DebuggerException.getSettingKey(pe, debuggerId),
          Integer.toString(pe.getExceptionAction().getValue()));
    }
  }

  /**
   * Attaches to a target process.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that attaches to the target process.
   * @param notifier Notifier that tells the user about problems.
   */
  public static void attach(final JFrame parent, final IDebugger debugger,
      final CDebugEventNotifier notifier) {
    try {
      notifier.start();
      debugger.connect();
    } catch (final DebugExceptionWrapper exception) {
      notifier.stop();
      CUtilityFunctions.logException(exception);
      final String innerMessage = "E00082: " + "Could not attach to the debug client";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not attach to the debug client.", new String[] {
              "There was a problem with the connection to the debug client."}, new String[] {
              "The debugger process was not started yet. You can try to debug "
              + "the client again once you resolved the connection problems."});
      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
    }
  }

  /**
   * Detaches from the target process.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that detaches from the target process.
   */
  public static void detach(final JFrame parent, final IDebugger debugger) {
    checkArguments(parent, debugger);
    if (!debugger.isConnected()) {
      return;
    }
    try {
      debugger.detach();
    } catch (final DebugExceptionWrapper exception) {
      CUtilityFunctions.logException(exception);
      final String innerMessage = "E00073: " + "Could not send detach command";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the detach command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The debugger remains attached to the debug target."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
    }
  }

  /**
   * Halts the target process.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that is used to halt the target process.
   */
  public static void halt(final JFrame parent, final IDebugger debugger) {
    checkArguments(parent, debugger);
    if (!debugger.isConnected()) {
      return;
    }
    try {
      debugger.halt();
    } catch (final DebugExceptionWrapper e) {
      CUtilityFunctions.logException(e);
      final String innerMessage = "E00083: " + "Could not send halt command";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the halt command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The state of the debugged process remains unchanged."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Merge the exception settings from the database with the ones received from the debugger.
   *
   * @param target The debug target allowing us to access the per-module storage mechanism.
   * @param exceptions The list of exceptions received from the debugger.
   *
   * @return The collection of exceptions which have to be used during this session.
   *
   * @throws CouldntLoadDataException
   */
  public static Collection<DebuggerException> mergeExceptionsSettings(
      final DebugTargetSettings target, final Collection<DebuggerException> exceptions,
      final int debuggerId) throws CouldntLoadDataException {
    final Map<Long, DebuggerException> exceptionsMap = getExceptionsMap(exceptions);
    for (final DebuggerException dbgException : exceptions) {
      final String setting =
          target.readSetting(DebuggerException.getSettingKey(dbgException, debuggerId));
      if (setting != null) {
        final DebuggerExceptionHandlingAction handlingAction =
            DebuggerExceptionHandlingAction.convertToHandlingAction(Integer.valueOf(setting));
        final DebuggerException newException = new DebuggerException(
            dbgException.getExceptionName(), dbgException.getExceptionCode(), handlingAction);
        exceptionsMap.put(dbgException.getExceptionCode(), newException);
      }
    }
    return exceptionsMap.values();
  }

  /**
   * Resumes the target process.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that is used to resume the thread.
   */
  public static void resume(final JFrame parent, final IDebugger debugger) {
    checkArguments(parent, debugger);
    if (!debugger.isConnected()) {
      return;
    }
    try {
      debugger.getProcessManager().setActiveThread(null);
      debugger.resume();
    } catch (final DebugExceptionWrapper e) {
      CUtilityFunctions.logException(e);
      final String innerMessage = "E00029: " + "Could not send resume command to the debug client";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the resume command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The state of the debugged process remains unchanged."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  public static void sendDebuggerEventSettings(final JFrame parent, final IDebugger debugger,
      final DebugTargetSettings debugTarget) {
    try {
      final DebuggerEventSettingsStorage eventSettingsStorage =
          new DebuggerEventSettingsStorage(debugger, debugTarget);
      debugger.setDebuggerEventSettings(eventSettingsStorage.deserialize());
    } catch (final DebugExceptionWrapper exception) {
      CUtilityFunctions.logException(exception);
      final String message = "Debugger event settings could not be sent to the debugger.";
      final String description = CUtilityFunctions.createDescription(
          String.format("BinNavi could not send the debugger event settings to the debug client."),
          new String[] {},
          new String[] {"The default debugger event settings will be used during this session."});
      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
      final String message = "Debugger event settings could not be retrieved from the database.";
      final String description = CUtilityFunctions.createDescription(
          String.format("BinNavi could not send the debugger event settings to the debug client."),
          new String[] {},
          new String[] {"The default debugger event settings will be used during this session."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }

  /**
   * Shows the debugger options dialog.
   *
   * @param parent Parent window of the dialog.
   * @param target Provides the debugger target.
   * @param debugger The debugger whose options are shown in the dialog.
   */
  public static void showDebuggerOptionsDialogAlways(final JFrame parent,
      final DebugTargetSettings target, final IDebugger debugger) {
    Preconditions.checkNotNull(parent, "IE01559: Parent argument can not be null");
    Preconditions.checkNotNull(target, "IE01560: Target argument can not be null");
    Preconditions.checkNotNull(debugger, "IE01561: Debugger argument can not be null");
    final TargetInformation targetInformation = debugger.getProcessManager().getTargetInformation();
    if (targetInformation == null) {
      CMessageBox.showInformation(parent,
          "Debugger information can not be shown before the debugger is active.");
      return;
    }
    DebuggerOptions options = targetInformation.getDebuggerOptions();
    if (options == null) {
      CMessageBox.showInformation(parent,
          "Debugger information can not be shown before the debugger is active.");
      return;
    }
    if (!debugger.isConnected()) {
      CMessageBox.showInformation(parent,
          "Debugger information can not be shown since the debugger is not currently connected.");
      return;
    }
    try {
      options.setExceptions(new ArrayList<DebuggerException>(
          mergeExceptionsSettings(target, options.getExceptions(), debugger.getId())));
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
      final String message = "Exception settings could not be loaded.";
      final String description = CUtilityFunctions.createDescription(
          String.format("BinNavi could not load the exception settings from the database."),
          new String[] {"Communication error while contacting the database"}, new String[] {
              "BinNavi is unable to show the debugger options dialog until the problem is resolved."
              + " The default debugger options will be used during this session."});
      NaviErrorDialog.show(parent, message, description, exception);
    }
    final DebuggerEventSettings eventSettings = readDebuggerEventSettings(parent, debugger, target);
    final COptionsDialog dlg = new COptionsDialog(parent, options, eventSettings);
    dlg.setVisible(true);
    options = dlg.getDebuggerOptions();
    try {
      writeDebuggerExceptionSettings(options, target, debugger.getId());
      writeDebuggerEventSettings(debugger, target, dlg.getDebuggerEventSettings());
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);
      CUtilityFunctions.logException(exception);
      final String message = "Exception settings could not be written.";
      final String description = CUtilityFunctions.createDescription(
          String.format("BinNavi could not write the debugger options to the database."),
          new String[] {"Communication error while contacting the database"}, new String[] {
              "BinNavi is unable to store the debugger options in the database.",
              "Nevertheless, the debugger options will be sent to the debugger."});
      NaviErrorDialog.show(parent, message, description, exception);
    }
    sendExceptionSettings(parent, debugger, options.getExceptions());
    sendDebuggerEventSettings(parent, debugger, target);
  }

  /**
   * Lets the debugger step to the next block.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that steps to the next block.
   * @param graph The graph where the step operation happens.
   */
  public static void stepBlock(final JFrame parent, final IDebugger debugger, final ZyGraph graph) {
    checkArguments(parent, debugger, graph);
    if (!debugger.isConnected()) {
      return;
    }
    final TargetProcessThread activeThread = debugger.getProcessManager().getActiveThread();
    if (activeThread == null) {
      return;
    }
    final RelocatedAddress currentAddress = activeThread.getCurrentAddress();
    if (currentAddress == null) {
      CMessageBox.showError(parent, "Could not step because the selected thread is not suspended");
      return;
    }
    final UnrelocatedAddress oldAddress = debugger.memoryToFile(currentAddress);
    final Set<BreakpointAddress> relocatedBlockAddresses =
        CStepBlockHelper.getNextBlocks(graph, oldAddress);
    if (relocatedBlockAddresses.isEmpty()) {
      CMessageBox.showError(parent, "Couldn't step to the next block");
      return;
    } else {
      debugger.getProcessManager().setActiveThread(null);
      final Set<BreakpointAddress> setBreakpoints = new HashSet<BreakpointAddress>();
      debugger.getBreakpointManager().addBreakpoints(BreakpointType.STEP, relocatedBlockAddresses);
      setBreakpoints.addAll(relocatedBlockAddresses);
      try {
        debugger.resume();
      } catch (final DebugExceptionWrapper e) {
        // TODO: Step breakpoints should be removed at this point
        debugger.getProcessManager().setActiveThread(activeThread);
        CUtilityFunctions.logException(e);
        final String innerMessage =
            "E00045: " + "Could not send step block command to the debug client";
        final String innerDescription = CUtilityFunctions.createDescription(
            "BinNavi could not send the step block command to the debug client.",
            new String[] {"There was a problem with the connection to the debug client."},
            new String[] {"The state of the debugged process remains unchanged."});
        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Lets the debugger step to the end of the function.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that steps to the end of the function.
   * @param graph The graph where the step operation happens.
   */
  public static void stepEnd(final JFrame parent, final IDebugger debugger, final ZyGraph graph) {
    checkArguments(parent, debugger, graph);
    if (!debugger.isConnected()) {
      return;
    }
    final TargetProcessThread activeThread = debugger.getProcessManager().getActiveThread();
    if (activeThread == null) {
      return;
    }
    final RelocatedAddress currentAddress = activeThread.getCurrentAddress();
    if (currentAddress == null) {
      CMessageBox.showError(parent, "Could not step because the selected thread is not suspended");
      return;
    }
    final Set<BreakpointAddress> relocatedBlockAddresses = CStepEndHelper.getEndAddresses(graph);
    if (relocatedBlockAddresses.isEmpty()) {
      CMessageBox.showError(parent, "Couldn't step to the end of the function");
      return;
    } else {
      debugger.getProcessManager().setActiveThread(null);
      debugger.getBreakpointManager().addBreakpoints(BreakpointType.STEP, relocatedBlockAddresses);
      try {
        debugger.resume();
      } catch (final DebugExceptionWrapper e) {
        debugger.getBreakpointManager().removeBreakpoints(BreakpointType.STEP,
            relocatedBlockAddresses);
        debugger.getProcessManager().setActiveThread(activeThread);
        CUtilityFunctions.logException(e);
        final String innerMessage =
            "E00086: " + "Could not send step end command to the debug client";
        final String innerDescription = CUtilityFunctions.createDescription(
            "BinNavi could not send the step end command to the debug client.",
            new String[] {"There was a problem with the connection to the debug client."},
            new String[] {"The state of the debugged process remains unchanged."});
        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Lets the debugger execute a single step.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that executes the single step.
   */
  public static void stepInto(final JFrame parent, final IDebugger debugger) {
    checkArguments(parent, debugger);
    if (!debugger.isConnected()) {
      return;
    }
    final TargetProcessThread activeThread = debugger.getProcessManager().getActiveThread();
    try {
      debugger.getProcessManager().setActiveThread(null);
      debugger.singleStep();
    } catch (final DebugExceptionWrapper e) {
      CUtilityFunctions.logException(e);
      debugger.getProcessManager().setActiveThread(activeThread);
      final String innerMessage =
          "E00192: " + "Could not send single step command to the debug client";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the single step command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The state of the debugged process remains unchanged."});
      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Lets the debugger step over the next instruction.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that steps over the next instruction.
   * @param graph The graph where the step operation happens.
   */
  public static void stepOver(final JFrame parent, final IDebugger debugger, final ZyGraph graph) {
    checkArguments(parent, debugger, graph);
    if (!debugger.isConnected()) {
      return;
    }
    final TargetProcessThread activeThread = debugger.getProcessManager().getActiveThread();
    if (activeThread == null) {
      return;
    }
    final RelocatedAddress currentAddress = activeThread.getCurrentAddress();
    if (currentAddress == null) {
      CMessageBox.showError(parent, "Could not step because the selected thread is not suspended");
      return;
    }
    final UnrelocatedAddress oldAddress = debugger.memoryToFile(currentAddress);
    final Set<BreakpointAddress> relocatedAddresses =
        CStepOverHelper.getNextInstructions(graph, oldAddress);
    if (relocatedAddresses.isEmpty()) {
      CMessageBox.showError(parent, "Couldn't step over the current instruction");
      return;
    } else {
      debugger.getProcessManager().setActiveThread(null);
      debugger.getBreakpointManager().addBreakpoints(BreakpointType.STEP, relocatedAddresses);
      try {
        debugger.resume();
      } catch (final DebugExceptionWrapper e) {
        // TODO: Step breakpoints should be removed at this point
        debugger.getProcessManager().setActiveThread(activeThread);
        CUtilityFunctions.logException(e);
        final String innerMessage =
            "E00087: " + "Could not send step over command to the debug client";
        final String innerDescription = CUtilityFunctions.createDescription(
            "BinNavi could not send the step over command to the debug client.",
            new String[] {"There was a problem with the connection to the debug client."},
            new String[] {"The state of the debugged process remains unchanged."});
        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Shuts down the target process and the debug client.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that terminates the target process.
   */
  public static void terminate(final JFrame parent, final IDebugger debugger) {
    checkArguments(parent, debugger);
    try {
      if (debugger.isConnected()) {
        debugger.terminate();
      }
    } catch (final DebugExceptionWrapper e) {
      CUtilityFunctions.logException(e);
      final String innerMessage =
          "E00193: " + "Could not send terminate command to the debug client";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not send the terminate command to the debug client.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The state of the debugged process remains unchanged."});
      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }
}
