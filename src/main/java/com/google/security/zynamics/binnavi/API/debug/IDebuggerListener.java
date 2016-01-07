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
package com.google.security.zynamics.binnavi.API.debug;

import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;

// ! Interface for listening on raw debugger messages.
/**
 * This interface can be implemented by all classes that want to work with the messages received
 * from debug clients. In most cases this is not necessary because BinNavi offers higher-level ways
 * to access this data (see the Process and Thread classes for example). There are a few cases where
 * it might nevertheless be useful to work with raw messages directly.
 */
public interface IDebuggerListener {
  /**
   * Invoked after the debugger caused an exception.
   * 
   * @param debugException The exception.
   */
  void debugException(DebugExceptionWrapper debugException);

  // ! Signals that the debugger was closed.
  /**
   * Invoked after the debugger closed down for whatever reason.
   * 
   * @param errorCode 0 to indicate success, any other value to indicate an error.
   */
  void debuggerClosed(int errorCode);

  // ! Signals a newly established connection to the debugger.
  /**
   * Invoked after the connection has been established to the debugger.
   */
  void debuggerConnected();

  // ! Signals a new attach reply.
  /**
   * Invoked after an Attach reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void debuggerAttach(DebuggerAttachReply reply);

  // ! Signals a new authentication failed reply.
  /**
   * Invoked after an Authentication Failed event was triggered.
   * 
   * @param reply The received reply.
   */
  void authenticationFailed(DebuggerAuthenticationFailedReply reply);

  // ! Signals a new breakpoint condition set reply.
  /**
   * Invoked after a Set Breakpoint Condition reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void breakpointConditionSet(DebuggerBreakpointConditionSetReply reply);

  // ! Signals a new breakpoint hit reply.
  /**
   * Invoked after a Breakpoint Hit reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void breakpointHit(DebuggerBreakpointHitReply reply);

  // ! Signals a new breakpoint set reply.
  /**
   * Invoked after a Breakpoint Set reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void breakpointSet(DebuggerBreakpointSetReply reply);

  // ! Signals a new breakpoint removed reply.
  /**
   * Invoked after a Breakpoint Removed reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void breakpointsRemoved(DebuggerBreakpointsRemovedReply reply);

  // ! Signals a new target selection canceled reply.
  /**
   * Invoked after the debug client confirmed the cancellation of the target selection.
   * 
   * @param reply The received reply.
   */
  void cancelTargetSelection(DebuggerCancelTargetSelectionReply reply);

  // ! Signals a new debugger closed unexpectedly.
  /**
   * Invoked after a Debugger Closed Unexpectedly reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void debuggerClosedUnexpectedly(DebuggerDebuggerClosedUnexpectedlyReply reply);

  // ! Signals a new debugger detached reply.
  /**
   * Invoked after a Detach reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void debuggerDetached(DebuggerDetachReply reply);

  // ! Signals a new echo breakpoint hit reply.
  /**
   * Invoked after an Echo Breakpoint Hit reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void echoBreakpointHit(DebuggerEchoBreakpointHitReply reply);

  // ! Signals a new echo breakpoint set reply.
  /**
   * Invoked after an Echo Breakpoint Set reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void echoBreakpointSet(DebuggerEchoBreakpointSetReply reply);

  // ! Signals a new echo breakpoint removed reply.
  /**
   * Invoked after an Echo Breakpoint Removed reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void echoBreakpointsRemoved(DebuggerEchoBreakpointsRemovedReply reply);

  // ! Signals a new exception occurred reply.
  /**
   * Invoked after an Exception Occurred reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void exceptionOccurred(DebuggerExceptionOccurredReply reply);

  // ! Signals a new halt reply.
  /**
   * Invoked after a Halt reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void debuggerHalt(DebuggerHaltReply reply);

  // ! Signals a new file list reply.
  /**
   * Invoked after a List Files reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void listFiles(DebuggerListFilesReply reply);

  // ! Signals a new process list reply.
  /**
   * Invoked after a List Processes reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void listProcesses(DebuggerListProcessesReply reply);

  // ! Signals a new memory map reply.
  /**
   * Invoked after a Memory Map reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void memoryMap(DebuggerMemoryMapReply reply);

  // ! Signals a new module loaded reply.
  /**
   * Invoked after a Module Loaded reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void moduleLoaded(DebuggerModuleLoadedReply reply);

  // ! Signals a new module unloaded reply.
  /**
   * Invoked after a Module Unloaded reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void moduleUnloaded(DebuggerModuleUnloadedReply reply);

  // ! Signals a new process closed reply.
  /**
   * Invoked after a Process Closed reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void processClosed(DebuggerProcessClosedReply reply);

  // ! Signals a new process start reply.
  /**
   * Invoked after the Process Start reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void processStart(DebuggerProcessStartReply reply);

  // ! Signals a new query debug event setting reply.
  /**
   * Invoked after a Query Debugger Event Settings reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void queryDebuggerEventSettings(DebuggerQueryDebuggerEventSettingsReply reply);

  // ! Signals a new read memory reply.
  /**
   * Invoked after a Read Memory reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void readMemory(DebuggerReadMemoryReply reply);

  // ! Signals a new register reply.
  /**
   * Invoked after a Registers reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void registersReply(DebuggerRegistersReply reply);

  // ! Signals a new request target reply.
  /**
   * Invoked after a Request Target reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void requestTarget(DebuggerRequestTargetReply reply);

  // ! Signals a new resume reply.
  /**
   * Invoked after a Resume reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void processResumed(DebuggerResumeReply reply);

  // ! Signals a new resume thread reply.
  /**
   * Invoked after a Resume Thread reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void threadResumed(DebuggerResumeThreadReply reply);

  // ! Signals a new search reply.
  /**
   * Invoked after a Search reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void searchReply(DebuggerSearchReply reply);

  // ! Signals a new select file reply.
  /**
   * Invoked after a Select File reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void selectFile(DebuggerSelectFileReply reply);

  // ! Signals a new select process reply.
  /**
   * Invoked after a Select Process reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void selectProcess(DebuggerSelectProcessReply reply);

  // ! Signals a new set debugger event setting reply.
  /**
   * Invoked after a Set Debugger Event Settings Reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void setDebuggerEventSettings(DebuggerSetDebuggerEventSettingsReply reply);

  // ! Signals a new set exception settings reply.
  /**
   * Invoked after a Set Exception Settings reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void setExceptionSettings(DebuggerSetExceptionSettingsReply reply);

  // ! Signals a new set register reply.
  /**
   * Invoked after a Set Register reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void setRegister(DebuggerSetRegisterReply reply);

  // ! Signals a new single step reply.
  /**
   * Invoked after a Single Step reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void singleStep(DebuggerSingleStepReply reply);

  // ! Signals a new step breakpoint hit reply.
  /**
   * Invoked after a Step Breakpoint Hit reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void stepBreakpointHit(DebuggerStepBreakpointHitReply reply);

  // ! Signals a new step breakpoint set reply.
  /**
   * Invoked after a Step Breakpoint Removed reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void stepBreakpointSet(DebuggerStepBreakpointSetReply reply);

  // ! Signals a new step breakpoint removed reply.
  /**
   * Invoked after a Step Breakpoint Set reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void stepBreakpointsRemovedReply(DebuggerStepBreakpointsRemovedReply reply);

  // ! Signals a new suspend thread reply.
  /**
   * Invoked after a Suspend Thread reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void threadSuspended(DebuggerSuspendThreadReply reply);

  // ! Signals a new target information reply.
  /**
   * Invoked after a Target Information reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void targetInformation(DebuggerTargetInformationReply reply);

  // ! Signals a new terminate reply.
  /**
   * Invoked after a Terminate reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void terminated(DebuggerTerminateReply reply);

  // ! Signals a new thread closed reply.
  /**
   * Invoked after a Thread Closed reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void threadClosed(DebuggerThreadClosedReply reply);

  // ! Signals a new thread created reply.
  /**
   * Invoked after a Thread Created reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void threadCreated(DebuggerThreadCreatedReply reply);

  // ! Signals a new validate memory reply.
  /**
   * Invoked after a Validate Memory reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void validateMemory(DebuggerValidateMemoryReply reply);

  // ! Signals a new write memory reply.
  /**
   * Invoked after a Write Memory reply was received from the debug client.
   * 
   * @param reply The received reply.
   */
  void writeMemory(DebuggerWriteMemoryReply reply);
}
