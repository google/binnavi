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
package com.google.security.zynamics.binnavi.debug.debugger.synchronizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.DebugEventListener;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AttachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AuthenticationFailedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointConditionSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.CancelTargetSelectionReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerClosedUnexpectedlyReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ExceptionOccurredReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.HaltReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListFilesReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListProcessesReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleLoadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleUnloadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.QueryDebuggerEventSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RegistersReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RequestTargetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeThreadReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SearchReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SelectFileReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SelectProcessReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetDebuggerEventSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetExceptionSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetRegisterReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SingleStepReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SuspendThreadReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TerminateReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadCreatedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ValidateMemoryReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.WriteMemoryReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * The debugger synchronizer takes the raw events that come from the debugger and converts them into
 * the appropriate high-level events in the process manager or breakpoint manager.
 */
public final class DebuggerSynchronizer implements DebugEventListener {
  /**
   * Listeners that are notified about high level debug events.
   */
  private final ListenerProvider<IDebugEventListener> listeners =
      new ListenerProvider<>();

  /**
   * Synchronizes Attach replies from the debug client with the simulated target process.
   */
  private final AttachSynchronizer attachSynchronizer;

  /**
   * Synchronizes Breakpoint Hit replies from the debug client with the simulated target process.
   */
  private final BreakpointHitSynchronizer breakpointHitSynchronizer;

  /**
   * Synchronizes Breakpoint Removed replies from the debug client with the simulated target
   * process.
   */
  private final BreakpointRemovedSynchronizer breakpointRemovedSynchronizer;

  /**
   * Synchronizes Breakpoint Set replies from the debug client with the simulated target process.
   */
  private final BreakpointSetSynchronizer breakpointSetSynchronizer;

  /**
   * Synchronizes Detach replies from the debug client with the simulated target process.
   */
  private final DetachSynchronizer detachSynchronizer;

  /**
   * Synchronizes Echo Breakpoint Hit replies from the debug client with the simulated target
   * process.
   */
  private final EchoBreakpointHitSynchronizer echoBreakpointHitSynchronizer;

  /**
   * Synchronizes Echo Breakpoint Removed replies from the debug client with the simulated target
   * process.
   */
  private final EchoBreakpointRemovedSynchronizer echoBreakpointRemovedSynchronizer;

  /**
   * Synchronizes Echo Breakpoint Set replies from the debug client with the simulated target
   * process.
   */
  private final EchoBreakpointSetSynchronizer echoBreakpointSetSynchronizer;

  /**
   * Synchronizes Connection Closed replies from the debug client with the simulated target process.
   */
  private final ErrorConnectionClosedSynchronizer errorConnectionClosedSynchronizer;

  /**
   * Synchronizes Exception Occurred replies from the debug client with the simulated target
   * process.
   */
  private final ExceptionOccurredSynchronizer exceptionOccurredSynchronizer;

  /**
   * Synchronizes Select File replies from the debug client with the simulated target process.
   */
  private final SelectFileSynchronizer fileSelectedSynchronizer;

  /**
   * Synchronizes Halt replies from the debug client with the simulated target process.
   */
  private final HaltSynchronizer haltSynchronizer;

  /**
   * Synchronizes Target Information replies from the debug client with the simulated target
   * process.
   */
  private final TargetInformationSynchronizer targetInformationSynchronizer;

  /**
   * Synchronizes List Files replies from the debug client with the simulated target process.
   */
  private final ListFilesSynchronizer listFilesSynchronizer;

  /**
   * Synchronizes List Processes replies from the debug client with the simulated target process.
   */
  private final ListProcessesSynchronizer listProcessesSynchronizer;

  /**
   * Synchronizes Memory Map replies from the debug client with the simulated target process.
   */
  private final MemoryMapSynchronizer memoryMapSynchronizer;

  /**
   * Synchronizes Module Loaded replies from the debug client with the simulated target process.
   */
  private final ModuleLoadedSynchronizer moduleLoadedSynchronizer;

  /**
   * Synchronizes Module Unloaded replies from the debug client with the simulated target process.
   */
  private final ModuleUnloadedSynchronizer moduleUnloadedSynchronizer;

  /**
   * Synchronizes Process Closed replies from the debug client with the simulated target process.
   */
  private final ProcessClosedSynchronizer processClosedSynchronizer;

  /**
   * Synchronizes Process Selected replies from the debug client with the simulated target process.
   */
  private final ProcessSelectedSynchronizer processSelectedSynchronizer;

  /**
   * Synchronizes Read Memory replies from the debug client with the simulated target process.
   */
  private final ReadMemorySynchronizer readMemorySynchronizer;

  /**
   * Synchronizes Read Register replies from the debug client with the simulated target process.
   */
  private final ReadRegistersSynchronizer readRegistersSynchronizer;

  /**
   * Synchronizes Request Target replies from the debug client with the simulated target process.
   */
  private final RequestTargetSynchronizer requestTargetSynchronizer;

  /**
   * Synchronizes Resume replies from the debug client with the simulated target process.
   */
  private final ResumeSynchronizer resumeSynchronizer;

  /**
   * Synchronizes Search replies from the debug client with the simulated target process.
   */
  private final SearchSynchronizer searchSynchronizer;

  /**
   * Synchronizes Set Register replies from the debug client with the simulated target process.
   */
  private final SetRegisterSynchronizer setRegisterSynchronizer;

  /**
   * Synchronizes Single Step replies from the debug client with the simulated target process.
   */
  private final SingleStepSynchronizer singleStepSynchronizer;

  /**
   * Synchronizes Step Breakpoint Hit replies from the debug client with the simulated target
   * process.
   */
  private final StepBreakpointHitSynchronizer stepBreakpointHitSynchronizer;

  /**
   * Synchronizes Step Breakpoint Removed replies from the debug client with the simulated target
   * process.
   */
  private final StepBreakpointRemovedSynchronizer stepBreakpointRemovedSynchronizer;

  /**
   * Synchronizes Step Breakpoint Set replies from the debug client with the simulated target
   * process.
   */
  private final StepBreakpointSetSynchronizer stepBreakpointSetSynchronizer;

  /**
   * Synchronizes Terminate replies from the debug client with the simulated target process.
   */
  private final TerminateSynchronizer terminateSynchronizer;

  /**
   * Synchronizes Thread Closed replies from the debug client with the simulated target process.
   */
  private final ThreadClosedSynchronizer threadClosedSynchronizer;

  /**
   * Synchronizes Thread Created replies from the debug client with the simulated target process.
   */
  private final ThreadCreatedSynchronizer threadCreatedSynchronizer;

  /**
   * Synchronizes Validate Memory replies from the debug client with the simulated target process.
   */
  private final ValidateMemorySynchronizer validateMemorySynchronizer;

  /**
   * Synchronizes Resume Thread replies from the debug client with the simulated target process.
   */
  private final ResumeThreadSynchronizer resumeThreadSynchronizer;

  /**
   * Synchronizes Suspend Thread replies from the debug client with the simulated target process.
   */
  private final SuspendThreadSynchronizer suspendThreadSynchronizer;

  /**
   * Synchronizes Set Breakpoint Condition replies from the debug client with the simulated target
   * process.
   */
  private final BreakpointConditionSetSynchronizer breakpointConditionSetSynchronizer;

  /**
   * Synchronizes Write Memory replies from the debug client with the simulated target process.
   */
  private final WriteMemorySynchronizer writeMemorySynchronizer;

  /**
   * Synchronizes Authentication Failed events.
   */
  private final AuthenticationFailedSynchronizer authenticationFailedSynchronizer;

  /**
   * Synchronizes Thread Activated events.
   */
  private final QueryDebuggerEventSettingsSynchronizer queryDebuggerEventSettingsSynchronizer;

  /**
   * Synchronizes Set Exception Settings events.
   */
  private final SetExceptionSettingsSynchronizer setExceptionSettingsSynchronizer;

  /**
   * Synchronizes Process Start events.
   */
  private final ProcessStartSynchronizer processStartSynchronizer;

  /**
   * Synchronizes Set Debugger Event Settings events.
   */
  private final SetDebuggerEventSettingsSynchronizer setDebuggerEventSettingsSynchronizer;

  /**
   * Synchronizes target cancellation events.
   */
  private final CancelTargetSelectionSynchronizer cancelTargetSelectionSynchronizer;

  /**
   * Creates a new debugger synchronizer object.
   *
   * @param debugger The debugger object that provides the low level events and the high level data
   *        structures like the process manager.
   */
  public DebuggerSynchronizer(final IDebugger debugger) {
    Preconditions.checkNotNull(debugger, "IE00789: Debugger argument can not be null");

    attachSynchronizer = new AttachSynchronizer(debugger, listeners);
    breakpointHitSynchronizer = new BreakpointHitSynchronizer(debugger, listeners);
    breakpointRemovedSynchronizer =
        new BreakpointRemovedSynchronizer(debugger, listeners);
    breakpointSetSynchronizer = new BreakpointSetSynchronizer(debugger, listeners);
    detachSynchronizer = new DetachSynchronizer(debugger, listeners);
    echoBreakpointHitSynchronizer =
        new EchoBreakpointHitSynchronizer(debugger, listeners);
    echoBreakpointRemovedSynchronizer =
        new EchoBreakpointRemovedSynchronizer(debugger, listeners);
    echoBreakpointSetSynchronizer =
        new EchoBreakpointSetSynchronizer(debugger, listeners);
    errorConnectionClosedSynchronizer =
        new ErrorConnectionClosedSynchronizer(debugger, listeners);
    authenticationFailedSynchronizer =
        new AuthenticationFailedSynchronizer(debugger, listeners);
    exceptionOccurredSynchronizer =
        new ExceptionOccurredSynchronizer(debugger, listeners);
    fileSelectedSynchronizer = new SelectFileSynchronizer(debugger, listeners);
    haltSynchronizer = new HaltSynchronizer(debugger, listeners);
    targetInformationSynchronizer =
        new TargetInformationSynchronizer(debugger, listeners);
    listFilesSynchronizer = new ListFilesSynchronizer(debugger, listeners);
    listProcessesSynchronizer = new ListProcessesSynchronizer(debugger, listeners);
    memoryMapSynchronizer = new MemoryMapSynchronizer(debugger, listeners);
    moduleLoadedSynchronizer = new ModuleLoadedSynchronizer(debugger, listeners);
    moduleUnloadedSynchronizer = new ModuleUnloadedSynchronizer(debugger, listeners);
    processClosedSynchronizer = new ProcessClosedSynchronizer(debugger, listeners);
    processSelectedSynchronizer = new ProcessSelectedSynchronizer(debugger, listeners);
    readMemorySynchronizer = new ReadMemorySynchronizer(debugger, listeners);
    readRegistersSynchronizer = new ReadRegistersSynchronizer(debugger, listeners);
    requestTargetSynchronizer = new RequestTargetSynchronizer(debugger, listeners);
    resumeSynchronizer = new ResumeSynchronizer(debugger, listeners);
    searchSynchronizer = new SearchSynchronizer(debugger, listeners);
    setRegisterSynchronizer = new SetRegisterSynchronizer(debugger, listeners);
    singleStepSynchronizer = new SingleStepSynchronizer(debugger, listeners);
    stepBreakpointHitSynchronizer =
        new StepBreakpointHitSynchronizer(debugger, listeners);
    stepBreakpointRemovedSynchronizer =
        new StepBreakpointRemovedSynchronizer(debugger, listeners);
    stepBreakpointSetSynchronizer =
        new StepBreakpointSetSynchronizer(debugger, listeners);
    terminateSynchronizer = new TerminateSynchronizer(debugger, listeners);
    threadClosedSynchronizer = new ThreadClosedSynchronizer(debugger, listeners);
    threadCreatedSynchronizer = new ThreadCreatedSynchronizer(debugger, listeners);
    validateMemorySynchronizer = new ValidateMemorySynchronizer(debugger, listeners);
    resumeThreadSynchronizer = new ResumeThreadSynchronizer(debugger, listeners);
    suspendThreadSynchronizer = new SuspendThreadSynchronizer(debugger, listeners);
    breakpointConditionSetSynchronizer =
        new BreakpointConditionSetSynchronizer(debugger, listeners);
    writeMemorySynchronizer = new WriteMemorySynchronizer(debugger, listeners);
    queryDebuggerEventSettingsSynchronizer =
        new QueryDebuggerEventSettingsSynchronizer(debugger, listeners);
    setExceptionSettingsSynchronizer =
        new SetExceptionSettingsSynchronizer(debugger, listeners);
    processStartSynchronizer = new ProcessStartSynchronizer(debugger, listeners);
    setDebuggerEventSettingsSynchronizer =
        new SetDebuggerEventSettingsSynchronizer(debugger, listeners);
    cancelTargetSelectionSynchronizer =
        new CancelTargetSelectionSynchronizer(debugger, listeners);
  }

  /**
   * Adds a listener object that is notified about debug events.
   *
   * @param listener The listener object.
   */
  public void addListener(final IDebugEventListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Can be overwritten to handle Attach replies.
   *
   * @param reply The reply to handle.
   */
  @Override
  public void receivedEvent(final DebuggerReply reply) {
    Preconditions.checkNotNull(reply, "IE01211: Debug event can't be null");
    if (reply instanceof AttachReply) {
      attachSynchronizer.handle((AttachReply) reply);
    } else if (reply instanceof BreakpointHitReply) {
      breakpointHitSynchronizer.handle((BreakpointHitReply) reply);
    } else if (reply instanceof BreakpointsRemovedReply) {
      breakpointRemovedSynchronizer.handle((BreakpointsRemovedReply) reply);
    } else if (reply instanceof BreakpointSetReply) {
      breakpointSetSynchronizer.handle((BreakpointSetReply) reply);
    } else if (reply instanceof CancelTargetSelectionReply) {
      cancelTargetSelectionSynchronizer.handle((CancelTargetSelectionReply) reply);
    } else if (reply instanceof AuthenticationFailedReply) {
      authenticationFailedSynchronizer.handle((AuthenticationFailedReply) reply);
    } else if (reply instanceof DebuggerClosedUnexpectedlyReply) {
      errorConnectionClosedSynchronizer.handle((DebuggerClosedUnexpectedlyReply) reply);
    } else if (reply instanceof DetachReply) {
      detachSynchronizer.handle((DetachReply) reply);
    } else if (reply instanceof EchoBreakpointHitReply) {
      echoBreakpointHitSynchronizer.handle((EchoBreakpointHitReply) reply);
    } else if (reply instanceof EchoBreakpointsRemovedReply) {
      echoBreakpointRemovedSynchronizer.handle((EchoBreakpointsRemovedReply) reply);
    } else if (reply instanceof EchoBreakpointSetReply) {
      echoBreakpointSetSynchronizer.handle((EchoBreakpointSetReply) reply);
    } else if (reply instanceof ExceptionOccurredReply) {
      exceptionOccurredSynchronizer.handle((ExceptionOccurredReply) reply);
    } else if (reply instanceof HaltReply) {
      haltSynchronizer.handle((HaltReply) reply);
    } else if (reply instanceof ListFilesReply) {
      listFilesSynchronizer.handle((ListFilesReply) reply);
    } else if (reply instanceof ListProcessesReply) {
      listProcessesSynchronizer.handle((ListProcessesReply) reply);
    } else if (reply instanceof MemoryMapReply) {
      memoryMapSynchronizer.handle((MemoryMapReply) reply);
    } else if (reply instanceof ModuleLoadedReply) {
      moduleLoadedSynchronizer.handle((ModuleLoadedReply) reply);
    } else if (reply instanceof ModuleUnloadedReply) {
      moduleUnloadedSynchronizer.handle((ModuleUnloadedReply) reply);
    } else if (reply instanceof ProcessClosedReply) {
      processClosedSynchronizer.handle((ProcessClosedReply) reply);
    } else if (reply instanceof ReadMemoryReply) {
      readMemorySynchronizer.handle((ReadMemoryReply) reply);
    } else if (reply instanceof RegistersReply) {
      readRegistersSynchronizer.handle((RegistersReply) reply);
    } else if (reply instanceof RequestTargetReply) {
      requestTargetSynchronizer.handle((RequestTargetReply) reply);
    } else if (reply instanceof ResumeReply) {
      resumeSynchronizer.handle((ResumeReply) reply);
    } else if (reply instanceof SearchReply) {
      searchSynchronizer.handle((SearchReply) reply);
    } else if (reply instanceof SelectFileReply) {
      fileSelectedSynchronizer.handle((SelectFileReply) reply);
    } else if (reply instanceof SelectProcessReply) {
      processSelectedSynchronizer.handle((SelectProcessReply) reply);
    } else if (reply instanceof SetRegisterReply) {
      setRegisterSynchronizer.handle((SetRegisterReply) reply);
    } else if (reply instanceof SingleStepReply) {
      singleStepSynchronizer.handle((SingleStepReply) reply);
    } else if (reply instanceof StepBreakpointHitReply) {
      stepBreakpointHitSynchronizer.handle((StepBreakpointHitReply) reply);
    } else if (reply instanceof StepBreakpointsRemovedReply) {
      stepBreakpointRemovedSynchronizer.handle((StepBreakpointsRemovedReply) reply);
    } else if (reply instanceof StepBreakpointSetReply) {
      stepBreakpointSetSynchronizer.handle((StepBreakpointSetReply) reply);
    } else if (reply instanceof TargetInformationReply) {
      targetInformationSynchronizer.handle((TargetInformationReply) reply);
    } else if (reply instanceof TerminateReply) {
      terminateSynchronizer.handle((TerminateReply) reply);
    } else if (reply instanceof ThreadClosedReply) {
      threadClosedSynchronizer.handle((ThreadClosedReply) reply);
    } else if (reply instanceof ThreadCreatedReply) {
      threadCreatedSynchronizer.handle((ThreadCreatedReply) reply);
    } else if (reply instanceof ValidateMemoryReply) {
      validateMemorySynchronizer.handle((ValidateMemoryReply) reply);
    } else if (reply instanceof ResumeThreadReply) {
      resumeThreadSynchronizer.handle((ResumeThreadReply) reply);
    } else if (reply instanceof SuspendThreadReply) {
      suspendThreadSynchronizer.handle((SuspendThreadReply) reply);
    } else if (reply instanceof BreakpointConditionSetReply) {
      breakpointConditionSetSynchronizer.handle((BreakpointConditionSetReply) reply);
    } else if (reply instanceof WriteMemoryReply) {
      writeMemorySynchronizer.handle((WriteMemoryReply) reply);
    } else if (reply instanceof QueryDebuggerEventSettingsReply) {
      queryDebuggerEventSettingsSynchronizer.handle((QueryDebuggerEventSettingsReply) reply);
    } else if (reply instanceof SetExceptionSettingsReply) {
      setExceptionSettingsSynchronizer.handle((SetExceptionSettingsReply) reply);
    } else if (reply instanceof ProcessStartReply) {
      processStartSynchronizer.handle((ProcessStartReply) reply);
    } else if (reply instanceof SetDebuggerEventSettingsReply) {
      setDebuggerEventSettingsSynchronizer.handle((SetDebuggerEventSettingsReply) reply);
    } else {
      throw new IllegalStateException("IE01105: Unknown reply");
    }
  }

  /**
   * Removes a listener object from the debugger synchronizer.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IDebugEventListener listener) {
    listeners.removeListener(listener);
  }
}
