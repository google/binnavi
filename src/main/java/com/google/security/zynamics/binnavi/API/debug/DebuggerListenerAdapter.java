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

// / Adapter class for debugger events.
/**
 * Adapter class that can be used by objects that want to listen on debuggers but only need to
 * process few events.
 */
public class DebuggerListenerAdapter implements IDebuggerListener {
  @Override
  public void authenticationFailed(final DebuggerAuthenticationFailedReply reply) {
    // Empty default implementation
  }

  @Override
  public void breakpointConditionSet(final DebuggerBreakpointConditionSetReply reply) {
    // Empty default implementation
  }

  @Override
  public void breakpointHit(final DebuggerBreakpointHitReply reply) {
    // Empty default implementation
  }

  @Override
  public void breakpointSet(final DebuggerBreakpointSetReply reply) {
    // Empty default implementation
  }

  @Override
  public void breakpointsRemoved(final DebuggerBreakpointsRemovedReply reply) {
    // Empty default implementation
  }

  @Override
  public void cancelTargetSelection(final DebuggerCancelTargetSelectionReply reply) {
    // Empty default implementation
  }

  @Override
  public void debugException(final DebugExceptionWrapper debugException) {
    // Empty default implementation
  }

  @Override
  public void debuggerAttach(final DebuggerAttachReply reply) {
    // Empty default implementation
  }

  @Override
  public void debuggerClosed(final int errorCode) {
    // Empty default implementation
  }

  @Override
  public void debuggerClosedUnexpectedly(final DebuggerDebuggerClosedUnexpectedlyReply reply) {
    // Empty default implementation
  }

  @Override
  public void debuggerConnected() {
    // Empty default implementation
  }

  @Override
  public void debuggerHalt(final DebuggerHaltReply reply) {
    // Empty default implementation
  }

  @Override
  public void echoBreakpointsRemoved(final DebuggerEchoBreakpointsRemovedReply reply) {
    // Empty default implementation
  }

  @Override
  public void exceptionOccurred(final DebuggerExceptionOccurredReply reply) {
    // Empty default implementation
  }

  @Override
  public void listFiles(final DebuggerListFilesReply reply) {
    // Empty default implementation
  }

  @Override
  public void listProcesses(final DebuggerListProcessesReply reply) {
    // Empty default implementation
  }

  @Override
  public void memoryMap(final DebuggerMemoryMapReply reply) {
    // Empty default implementation
  }

  @Override
  public void moduleLoaded(final DebuggerModuleLoadedReply reply) {
    // Empty default implementation
  }

  @Override
  public void moduleUnloaded(final DebuggerModuleUnloadedReply reply) {
    // Empty default implementation
  }

  @Override
  public void processClosed(final DebuggerProcessClosedReply reply) {
    // Empty default implementation
  }

  @Override
  public void processStart(final DebuggerProcessStartReply reply) {
    // Empty default implementation
  }

  @Override
  public void queryDebuggerEventSettings(final DebuggerQueryDebuggerEventSettingsReply reply) {
    // Empty default implementation
  }

  @Override
  public void readMemory(final DebuggerReadMemoryReply reply) {
    // Empty default implementation
  }

  @Override
  public void debuggerDetached(final DebuggerDetachReply reply) {
    // Empty default implementation
  }

  @Override
  public void echoBreakpointHit(final DebuggerEchoBreakpointHitReply reply) {
    // Empty default implementation
  }

  @Override
  public void echoBreakpointSet(final DebuggerEchoBreakpointSetReply reply) {
    // Empty default implementation
  }

  @Override
  public void registersReply(final DebuggerRegistersReply reply) {
    // Empty default implementation
  }

  @Override
  public void requestTarget(final DebuggerRequestTargetReply reply) {
    // Empty default implementation
  }

  @Override
  public void processResumed(final DebuggerResumeReply reply) {
    // Empty default implementation
  }

  @Override
  public void threadResumed(final DebuggerResumeThreadReply reply) {
    // Empty default implementation
  }

  @Override
  public void searchReply(final DebuggerSearchReply reply) {
    // Empty default implementation
  }

  @Override
  public void selectFile(final DebuggerSelectFileReply reply) {
    // Empty default implementation
  }

  @Override
  public void selectProcess(final DebuggerSelectProcessReply reply) {
    // Empty default implementation
  }

  @Override
  public void setDebuggerEventSettings(final DebuggerSetDebuggerEventSettingsReply reply) {
    // Empty default implementation
  }

  @Override
  public void setExceptionSettings(final DebuggerSetExceptionSettingsReply reply) {
    // Empty default implementation
  }

  @Override
  public void setRegister(final DebuggerSetRegisterReply reply) {
    // Empty default implementation
  }

  @Override
  public void singleStep(final DebuggerSingleStepReply reply) {
    // Empty default implementation
  }

  @Override
  public void stepBreakpointHit(final DebuggerStepBreakpointHitReply reply) {
    // Empty default implementation
  }

  @Override
  public void stepBreakpointSet(final DebuggerStepBreakpointSetReply reply) {
    // Empty default implementation
  }

  @Override
  public void stepBreakpointsRemovedReply(final DebuggerStepBreakpointsRemovedReply reply) {
    // Empty default implementation
  }

  @Override
  public void threadSuspended(final DebuggerSuspendThreadReply reply) {
    // Empty default implementation
  }

  @Override
  public void targetInformation(final DebuggerTargetInformationReply reply) {
    // Empty default implementation
  }

  @Override
  public void terminated(final DebuggerTerminateReply reply) {
    // Empty default implementation
  }

  @Override
  public void threadClosed(final DebuggerThreadClosedReply reply) {
    // Empty default implementation
  }

  @Override
  public void threadCreated(final DebuggerThreadCreatedReply reply) {
    // Empty default implementation
  }

  @Override
  public void validateMemory(final DebuggerValidateMemoryReply reply) {
    // Empty default implementation
  }

  @Override
  public void writeMemory(final DebuggerWriteMemoryReply reply) {
    // Empty default implementation
  }
}
