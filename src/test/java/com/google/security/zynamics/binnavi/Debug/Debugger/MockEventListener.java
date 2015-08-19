/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Debug.Debugger;


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
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;

public final class MockEventListener implements IDebugEventListener {
  public int exception;
  public int detached;
  public String events = "";

  @Override
  public void debugException(final DebugExceptionWrapper debugException) {
    ++exception;
    events += String.format("DEBUGGER_EXCEPTION;");
  }

  @Override
  public void debuggerClosed(final int errorCode) {
    events += "DEBUGGER_CLOSED/" + errorCode + ";";
  }

  @Override
  public void receivedReply(final AttachReply reply) {
    if (reply.success()) {
    } else {
      events += "ERROR_ATTACH/" + reply.getErrorCode() + ";";
    }
  }

  @Override
  public void receivedReply(final AuthenticationFailedReply reply) {
    events += "AUTHENTICATION_FAILED/";
  }

  @Override
  public void receivedReply(final BreakpointConditionSetReply reply) {
    events += "CONDITION_SET/";
  }

  @Override
  public void receivedReply(final BreakpointHitReply reply) {
  }

  @Override
  public void receivedReply(final BreakpointSetReply reply) {
    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      if (resultPair.second() != 0) {
        events +=
            "ERROR_SET_BREAKPOINTS/" + resultPair.first().getAddress().toHexString() + "/"
                + resultPair.second() + ";";
      }
    }
  }

  @Override
  public void receivedReply(final BreakpointsRemovedReply reply) {
    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      if (resultPair.second() == 0) {
      } else {
        events +=
            "ERROR_REMOVE_BREAKPOINTS/" + resultPair.first().getAddress().toHexString() + "/"
                + resultPair.second() + ";";
      }
    }
  }

  @Override
  public void receivedReply(final CancelTargetSelectionReply reply) {
  }

  @Override
  public void receivedReply(final DebuggerClosedUnexpectedlyReply reply) {
  }

  @Override
  public void receivedReply(final DetachReply reply) {
    if (reply.success()) {
    } else {
      events += "ERROR_DETACH/" + reply.getErrorCode() + ";";
    }
  }

  @Override
  public void receivedReply(final EchoBreakpointHitReply reply) {
  }

  @Override
  public void receivedReply(final EchoBreakpointSetReply reply) {
    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      if (resultPair.second() != 0) {
        events +=
            "ERROR_SET_ECHO_BREAKPOINT/" + resultPair.first().getAddress().toHexString() + "/"
                + resultPair.second() + ";";
      }
    }
  }

  @Override
  public void receivedReply(final EchoBreakpointsRemovedReply reply) {
  }

  @Override
  public void receivedReply(final ExceptionOccurredReply reply) {
    if (reply.success()) {
      events += "EXCEPTION_OCCURRED/" + reply.getExceptionCode() + ";";
    } else {
    }
  }

  @Override
  public void receivedReply(final HaltReply reply) {
  }

  @Override
  public void receivedReply(final ListFilesReply reply) {
  }

  @Override
  public void receivedReply(final ListProcessesReply reply) {
  }

  @Override
  public void receivedReply(final MemoryMapReply reply) {
  }

  @Override
  public void receivedReply(final ModuleLoadedReply reply) {
  }

  @Override
  public void receivedReply(final ModuleUnloadedReply reply) {
  }

  @Override
  public void receivedReply(final ProcessClosedReply reply) {
    if (reply.success()) {
      events += "PROCESS_CLOSED;";
    }
  }

  @Override
  public void receivedReply(final ProcessStartReply reply) {
    if (reply.success()) {
      events += "PROCESS_START;";
    }
  }

  @Override
  public void receivedReply(final QueryDebuggerEventSettingsReply reply) {
  }

  @Override
  public void receivedReply(final ReadMemoryReply reply) {
    if (reply.success()) {
      events +=
          "RECEIVED_MEMORY/" + reply.getAddress().toHexString() + "/" + reply.getData().length
              + ";";
    } else {
      events += "ERROR_READING_MEMORY/" + reply.getErrorCode() + ";";
    }
  }

  @Override
  public void receivedReply(final RegistersReply reply) {
  }

  @Override
  public void receivedReply(final RequestTargetReply reply) {
  }

  @Override
  public void receivedReply(final ResumeReply reply) {
  }

  @Override
  public void receivedReply(final ResumeThreadReply reply) {
    events += "RESUMED_THREAD;";
  }

  @Override
  public void receivedReply(final SearchReply reply) {
    events += "SEARCH_REPLY;";
  }

  @Override
  public void receivedReply(final SelectFileReply reply) {
  }

  @Override
  public void receivedReply(final SelectProcessReply reply) {
  }

  @Override
  public void receivedReply(final SetDebuggerEventSettingsReply reply) {
  }

  @Override
  public void receivedReply(final SetExceptionSettingsReply reply) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void receivedReply(final SetRegisterReply reply) {
    if (reply.success()) {
    } else {
      events += "ERROR_SET_REGISTERS/" + reply.getErrorCode() + ";";
    }
  }

  @Override
  public void receivedReply(final SingleStepReply reply) {
    if (reply.success()) {
    } else {
      events += "ERROR_SINGLE_STEP/" + reply.getErrorCode() + ";";
    }
  }

  @Override
  public void receivedReply(final StepBreakpointHitReply reply) {
  }

  @Override
  public void receivedReply(final StepBreakpointSetReply reply) {
  }

  @Override
  public void receivedReply(final StepBreakpointsRemovedReply reply) {
  }

  @Override
  public void receivedReply(final SuspendThreadReply reply) {
    events += "SUSPENDED_THREAD;";
  }

  @Override
  public void receivedReply(final TargetInformationReply reply) {
    events += "RECEIVED_TARGET_INFORMATION;";
  }

  @Override
  public void receivedReply(final TerminateReply reply) {
  }

  @Override
  public void receivedReply(final ThreadClosedReply reply) {
  }

  @Override
  public void receivedReply(final ThreadCreatedReply reply) {
  }

  @Override
  public void receivedReply(final ValidateMemoryReply reply) {
  }

  @Override
  public void receivedReply(final WriteMemoryReply reply) {
    events += "RECEIVED_MEMORY_REPLY;";
  }
}
