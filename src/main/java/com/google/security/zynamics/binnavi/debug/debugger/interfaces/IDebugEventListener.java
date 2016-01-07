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
package com.google.security.zynamics.binnavi.debug.debugger.interfaces;

import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AttachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AuthenticationFailedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointConditionSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.CancelTargetSelectionReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerClosedUnexpectedlyReply;
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
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;

/**
 * Interface that must be implemented by objects that want to be notified about high level debug
 * events.
 */
public interface IDebugEventListener {
  /**
   * Invoked after the debugger caused an exception.
   *
   * @param debugException The exception.
   */
  void debugException(DebugExceptionWrapper debugException);

  /**
   * Invoked after the debugger was closed.
   *
   * @param errorCode Error code that indicates why the debugger was closed.
   */
  void debuggerClosed(int errorCode);

  /**
   * Invoked after an Attach reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(AttachReply reply);

  /**
   * Invoked after an Authentication Failed event was triggered.
   *
   * @param reply The received reply.
   */
  void receivedReply(AuthenticationFailedReply reply);

  /**
   * Invoked after a Set Breakpoint Condition reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(BreakpointConditionSetReply reply);

  /**
   * Invoked after a Breakpoint Hit reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(BreakpointHitReply reply);

  /**
   * Invoked after a Breakpoint Set reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(BreakpointSetReply reply);

  /**
   * Invoked after a Breakpoint Removed reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(BreakpointsRemovedReply reply);

  /**
   * Invoked after the debug client confirmed the cancellation of the target selection.
   *
   * @param reply The received reply.
   */
  void receivedReply(CancelTargetSelectionReply reply);

  /**
   * Invoked after a Debugger Closed Unexpectedly reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(DebuggerClosedUnexpectedlyReply reply);

  /**
   * Invoked after a Detach reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(DetachReply reply);

  /**
   * Invoked after an Echo Breakpoint Hit reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(EchoBreakpointHitReply reply);

  /**
   * Invoked after an Echo Breakpoint Set reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(EchoBreakpointSetReply reply);

  /**
   * Invoked after an Echo Breakpoint Removed reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(EchoBreakpointsRemovedReply reply);

  /**
   * Invoked after an Exception Occurred reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ExceptionOccurredReply reply);

  /**
   * Invoked after a Halt reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(HaltReply reply);

  /**
   * Invoked after a List Files reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ListFilesReply reply);

  /**
   * Invoked after a List Processes reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ListProcessesReply reply);

  /**
   * Invoked after a Memory Map reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(MemoryMapReply reply);

  /**
   * Invoked after a Module Loaded reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ModuleLoadedReply reply);

  /**
   * Invoked after a Module Unloaded reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ModuleUnloadedReply reply);

  /**
   * Invoked after a Process Closed reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ProcessClosedReply reply);

  /**
   * Invoked after the Process Start reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ProcessStartReply reply);

  /**
   * Invoked after a Query Debugger Event Settings reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(QueryDebuggerEventSettingsReply reply);

  /**
   * Invoked after a Read Memory reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ReadMemoryReply reply);

  /**
   * Invoked after a Registers reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(RegistersReply reply);

  /**
   * Invoked after a Request Target reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(RequestTargetReply reply);

  /**
   * Invoked after a Resume reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ResumeReply reply);

  /**
   * Invoked after a Resume Thread reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ResumeThreadReply reply);

  /**
   * Invoked after a Search reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SearchReply reply);

  /**
   * Invoked after a Select File reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SelectFileReply reply);

  /**
   * Invoked after a Select Process reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SelectProcessReply reply);

  /**
   * Invoked after a Set Debugger Event Settings Reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SetDebuggerEventSettingsReply reply);

  /**
   * Invoked after a Set Exception Settings reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SetExceptionSettingsReply reply);

  /**
   * Invoked after a Set Register reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SetRegisterReply reply);

  /**
   * Invoked after a Single Step reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SingleStepReply reply);

  /**
   * Invoked after a Step Breakpoint Hit reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(StepBreakpointHitReply reply);

  /**
   * Invoked after a Step Breakpoint Removed reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(StepBreakpointSetReply reply);

  /**
   * Invoked after a Step Breakpoint Set reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(StepBreakpointsRemovedReply reply);

  /**
   * Invoked after a Suspend Thread reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(SuspendThreadReply reply);

  /**
   * Invoked after a Target Information reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(TargetInformationReply reply);

  /**
   * Invoked after a Terminate reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(TerminateReply reply);

  /**
   * Invoked after a Thread Closed reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ThreadClosedReply reply);

  /**
   * Invoked after a Thread Created reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ThreadCreatedReply reply);

  /**
   * Invoked after a Validate Memory reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(ValidateMemoryReply reply);

  /**
   * Invoked after a Write Memory reply was received from the debug client.
   *
   * @param reply The received reply.
   */
  void receivedReply(WriteMemoryReply reply);
}
