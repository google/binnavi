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

import java.util.List;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
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
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.PairHelpers;
import com.google.security.zynamics.zylib.strings.CircularStringBuffer;
import com.google.security.zynamics.zylib.strings.Commafier;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.common.ICollectionMapper;



/**
 * Helper class for building the strings of the debug log.
 */
public final class CHistoryStringBuilder {
  /**
   * String shown in the text field.
   */
  // private final StringFIFO m_string = new StringFIFO(100);
  private final CircularStringBuffer m_string = new CircularStringBuffer(100);

  /**
   * The debugger that provides the logged events.
   */
  private IDebugger m_debugger;

  /**
   * Listeners that are notified about changes in the history builder.
   */
  private final ListenerProvider<IHistoryStringBuilderListener> m_listeners =
      new ListenerProvider<IHistoryStringBuilderListener>();

  /**
   * Keeps track of the debug client events and updates the text field.
   */
  private final IDebugEventListener m_listener = new IDebugEventListener() {
    private void updateText(final String text) {
      m_string.add(text);

      for (final IHistoryStringBuilderListener listener : m_listeners) {
        try {
          listener.changedText(m_string.getText());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void debugException(final DebugExceptionWrapper debugException) {
      // Do not log this
    }

    @Override
    public void debuggerClosed(final int errorCode) {
      updateText("SUCCESS" + ": " + "Debugger closed");
    }

    @Override
    public void receivedReply(final AttachReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": " + "Attached to target" : "ERROR" + ": "
          + String.format("Failed to attach to target (Error Code %d)", reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final AuthenticationFailedReply reply) {
      updateText("ERROR" + ": " + "Could not authenticate the debug client");
    }

    @Override
    public void receivedReply(final BreakpointConditionSetReply reply) {
      // Do not log this
    }

    @Override
    public void receivedReply(final BreakpointHitReply reply) {
      try {
        final IAddress programCounter =
            getProgramCounter(reply.getThreadId(), reply.getRegisterValues());

        updateText("SUCCESS"
            + ": "
            + String.format("Hit Breakpoint (Address: %s / TID: %d)", programCounter.toHexString(),
                reply.getThreadId()));
      } catch (final MaybeNullException e) {
        updateText("ERROR" + ": " + ("Hit Breakpoint (Could not determine event address)"));
      }
    }

    @Override
    public void receivedReply(final BreakpointSetReply reply) {
      final String succString = getSuccessfulAddresses(reply.getAddresses());
      final String errorString = getUnsuccessfulAddresses(reply.getAddresses());

      if (!succString.isEmpty()) {
        updateText("SUCCESS" + ": " + String.format("Breakpoints set at addresses %s", succString));
      }

      if (!errorString.isEmpty()) {
        updateText("ERROR" + ": "
            + String.format("Breakpoints could not be set at addresses %s", errorString));
      }
    }

    @Override
    public void receivedReply(final BreakpointsRemovedReply reply) {
      final String succString = getSuccessfulAddresses(reply.getAddresses());
      final String errorString = getUnsuccessfulAddresses(reply.getAddresses());

      if (!succString.isEmpty()) {
        updateText("SUCCESS" + ": "
            + String.format("Breakpoints removed from addresses %s", succString));
      }

      if (!errorString.isEmpty()) {
        updateText("ERROR" + ": "
            + String.format("Breakpoints could not be removed from addresses %s", errorString));
      }
    }

    @Override
    public void receivedReply(final CancelTargetSelectionReply reply) {
      // Not logging this
    }

    @Override
    public void receivedReply(final DebuggerClosedUnexpectedlyReply reply) {
      updateText("ERROR" + ": " + String.format("Debugger closed unexpectedly"));
    }

    @Override
    public void receivedReply(final DetachReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": " + "Detached from target" : "ERROR" + ": "
          + String.format("Failed to detach from target (Error Code %d)", reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final EchoBreakpointHitReply reply) {
      try {
        final IAddress programCounter =
            getProgramCounter(reply.getThreadId(), reply.getRegisterValues());

        updateText("SUCCESS"
            + ": "
            + String.format("Hit Echo Breakpoint (Address: %s / TID: %d)",
                programCounter.toHexString(), reply.getThreadId()));
      } catch (final MaybeNullException e) {
        updateText("ERROR" + ": " + "Hit Echo Breakpoint (Could not determine event address)");
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointSetReply reply) {
      final String succString = getSuccessfulAddresses(reply.getAddresses());
      final String errorString = getUnsuccessfulAddresses(reply.getAddresses());

      if (!succString.isEmpty()) {
        updateText("SUCCESS" + ": "
            + String.format("Echo Breakpoints set at addresses %s", succString));
      }

      if (!errorString.isEmpty()) {
        updateText("ERROR" + ": "
            + String.format("Echo Breakpoints could not be set at addresses %s", errorString));
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointsRemovedReply reply) {
      final String succString = getSuccessfulAddresses(reply.getAddresses());
      final String errorString = getUnsuccessfulAddresses(reply.getAddresses());

      if (!succString.isEmpty()) {
        updateText("SUCCESS" + ": "
            + String.format("Echo Breakpoints removed from addresses %s", succString));
      }

      if (!errorString.isEmpty()) {
        updateText("ERROR" + ": "
            + String.format("Echo Breakpoints could not be removed from addresses %s", errorString));
      }
    }

    @Override
    public void receivedReply(final ExceptionOccurredReply reply) {
      updateText("SUCCESS"
          + ": "
          + String.format(
              "Exception occured in the target process (Address: %s / TID: %d / Code: %d)", reply
                  .getAddress().toString(), reply.getThreadId(), reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final HaltReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": " + "Halted the target process" : "ERROR"
          + ": "
          + String
              .format("Failed to halt the target process (Error Code %d)", reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final ListFilesReply reply) {
      // Do not log this.
    }

    @Override
    public void receivedReply(final ListProcessesReply reply) {
      // Do not log this.
    }

    @Override
    public void receivedReply(final MemoryMapReply reply) {
      updateText(reply.success()
          ? "SUCCESS" + ": " + "Received memory map of the target process"
          : "ERROR"
              + ": "
              + String
                  .format(
                      "Debug client could not determine the memory map of the target process (Error Code %d)",
                      reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final ModuleLoadedReply reply) {
      updateText("SUCCESS" + ": "
          + String.format("Loaded module '%s'", reply.getModule().getName()));
    }

    @Override
    public void receivedReply(final ModuleUnloadedReply reply) {
      updateText("SUCCESS" + ": "
          + String.format("Unloaded module '%s'", reply.getModule().getName()));
    }

    @Override
    public void receivedReply(final ProcessClosedReply reply) {
      updateText("SUCCESS" + ": " + "Target process closed");
    }

    @Override
    public void receivedReply(final ProcessStartReply reply) {
      final ProcessStart ps = reply.getProcessStart();
      updateText("SUCCESS"
          + ": "
          + String
              .format(
                  "The new process was started: thread with TID %d was created and the module from %s was mapped to the base address %s",
                  ps.getThread().getThreadId(), ps.getModule().getName(), ps.getModule()
                      .getBaseAddress().getAddress().toHexString()));
    }

    @Override
    public void receivedReply(final QueryDebuggerEventSettingsReply reply) {
      updateText("SUCCESS: Received query for debugger event settings");
    }

    @Override
    public void receivedReply(final ReadMemoryReply reply) {
      updateText(reply.success() ? "SUCCESS"
          + ": "
          + String.format("Read %d bytes of memory from address %s", reply.getData().length, reply
              .getAddress().toHexString()) : "ERROR" + ": "
          + String.format("Failed to read memory (Error Code %d)", reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final RegistersReply reply) {
      updateText(reply.success()
          ? "SUCCESS" + ": " + "Received register values of the target process"
          : "ERROR"
              + ": "
              + String
                  .format(
                      "Debug client could not determine the register values of the target process (Error Code %d)",
                      reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final RequestTargetReply reply) {
      // Do not log this
    }

    @Override
    public void receivedReply(final ResumeReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": " + "Continued the target process" : "ERROR"
          + ": "
          + String.format("Debug client could not continue the target process (Error Code %d)",
              reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final ResumeThreadReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": "
          + String.format(" Resumed thread with TID %d", reply.getThreadId()) : "ERROR"
          + ": "
          + String.format("Thread with TID %d could not be resumed (Error Code %d)",
              reply.getErrorCode(), reply.getThreadId()));
    }

    @Override
    public void receivedReply(final SearchReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": " + "Received result of a memory search request"
          : "ERROR"
              + ": "
              + String.format("Memory search could not be executed (Error Code %d)",
                  reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final SelectFileReply reply) {
      // Do not log this
    }

    @Override
    public void receivedReply(final SelectProcessReply reply) {
      // Do not log this
    }

    @Override
    public void receivedReply(final SetDebuggerEventSettingsReply reply) {
      // Not logging this
    }

    @Override
    public void receivedReply(final SetExceptionSettingsReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": "
          + String.format("Exception settings were set in the debugger") : "ERROR" + ": "
          + String.format("Unable to set exception settings in the debugger"));

    }

    @Override
    public void receivedReply(final SetRegisterReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": " + "Changed value of register" : "ERROR"
          + ": "
          + String.format("Value of the register could not be changed (Error Code %d)",
              reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final SingleStepReply reply) {
      updateText(reply.success() ? "SUCCESS"
          + ": "
          + String.format("Executed a single step in thread %d (New PC address: %s)",
              reply.getThreadId(), reply.getAddress().getAddress().toHexString()) : "ERROR"
          + ": "
          + String
              .format("Single step could not be executed (Error Code %d)", reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final StepBreakpointHitReply reply) {
      try {
        updateText("SUCCESS"
            + ": "
            + String.format("Stepped to address %s",
                getProgramCounter(reply.getThreadId(), reply.getRegisterValues()).toHexString()));
      } catch (final MaybeNullException e) {
        updateText("ERROR" + ": " + "Stepped to a new address (Could not determine event address)");
      }
    }

    @Override
    public void receivedReply(final StepBreakpointSetReply reply) {
      // Do not log this
    }

    @Override
    public void receivedReply(final StepBreakpointsRemovedReply reply) {
      // Do not log this
    }

    @Override
    public void receivedReply(final SuspendThreadReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": "
          + String.format("Suspended thread with TID %d", reply.getThreadId()) : "ERROR"
          + ": "
          + String.format("Thread with TID %d could not be suspended (Error Code %d)",
              reply.getErrorCode(), reply.getThreadId()));
    }

    @Override
    public void receivedReply(final TargetInformationReply reply) {
      updateText("SUCCESS" + ": " + "Received target information");
    }

    @Override
    public void receivedReply(final TerminateReply reply) {
      updateText(reply.success() ? "SUCCESS" + ": "
          + String.format("Terminated the target process") : "ERROR" + ": "
          + String.format("Target process could not terminated"));
    }

    @Override
    public void receivedReply(final ThreadClosedReply reply) {
      updateText("SUCCESS" + ": "
          + String.format("Thread with TID %d was closed", reply.getThreadId()));
    }

    @Override
    public void receivedReply(final ThreadCreatedReply reply) {
      updateText("SUCCESS" + ": "
          + String.format("New thread with TID %d was created", reply.getThreadId()));
    }

    @Override
    public void receivedReply(final ValidateMemoryReply reply) {
      updateText(reply.success() ? "SUCCESS"
          + ": "
          + String.format("Determined valid memory range between %s and %s", reply.getStart()
              .toHexString(), reply.getEnd().toHexString()) : "ERROR"
          + ": "
          + String.format("Memory range could not be determined (Error Code %d)",
              reply.getErrorCode()));
    }

    @Override
    public void receivedReply(final WriteMemoryReply reply) {
      // Not logging this
    }
  };

  /**
   * Creates an address string for a list of addresses.
   * 
   * @param addresses The addresses to put into the string.
   * @param filter Filters the addresses.
   * 
   * @return The address string.
   */
  private static String getAddressString(final List<Pair<RelocatedAddress, Integer>> addresses,
      final ICollectionFilter<Pair<RelocatedAddress, Integer>> filter) {
    final List<RelocatedAddress> validAddresses =
        PairHelpers.projectFirst(CollectionHelpers.filter(addresses, filter));

    final Iterable<String> addressStrings =
        CollectionHelpers.map(validAddresses, new ICollectionMapper<RelocatedAddress, String>() {
          @Override
          public String map(final RelocatedAddress item) {
            return item.getAddress().toHexString();
          }
        });

    return Commafier.commafy(addressStrings);
  }

  /**
   * Returns the program counter value for a given thread.
   * 
   * @param threadId The ID of the thread.
   * @param registerValues The register values returned from the client.
   * 
   * @return The value of the program counter.
   * 
   * @throws MaybeNullException Thrown if the value of the program counter could not be determined.
   */
  private static IAddress getProgramCounter(final long threadId,
      final RegisterValues registerValues) throws MaybeNullException {
    for (final ThreadRegisters threadRegisters : registerValues) {
      if (threadRegisters.getTid() == threadId) {
        for (final RegisterValue registerValue : threadRegisters) {
          if (registerValue.isPc()) {
            return new CAddress(registerValue.getValue());
          }
        }
      }
    }

    throw new MaybeNullException();
  }

  /**
   * Returns a text string containing the addresses marked as successful.
   * 
   * @param addresses All addresses to process.
   * 
   * @return The string containing the addresses.
   */
  private static String getSuccessfulAddresses(
      final List<Pair<RelocatedAddress, Integer>> addresses) {
    return getAddressString(addresses, new ICollectionFilter<Pair<RelocatedAddress, Integer>>() {
      @Override
      public boolean qualifies(final Pair<RelocatedAddress, Integer> item) {
        return item.second() == 0;
      }
    });
  }

  /**
   * Returns a text string containing the addresses marked as unsuccessful.
   * 
   * @param addresses All addresses to process.
   * 
   * @return The string containing the addresses.
   */
  private static String getUnsuccessfulAddresses(
      final List<Pair<RelocatedAddress, Integer>> addresses) {
    return getAddressString(addresses, new ICollectionFilter<Pair<RelocatedAddress, Integer>>() {
      @Override
      public boolean qualifies(final Pair<RelocatedAddress, Integer> item) {
        return item.second() != 0;
      }
    });
  }

  /**
   * Adds a listener object that is notified about changes in the history builder.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final IHistoryStringBuilderListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Removes a listening listener object.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final IHistoryStringBuilderListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Sets the active debugger.
   * 
   * @param debugger The new active debugger.
   */
  public void setDebugger(final IDebugger debugger) {
    if (m_debugger != null) {
      m_debugger.removeListener(m_listener);
    }

    m_debugger = debugger;

    if (m_debugger != null) {
      m_debugger.addListener(m_listener);
    }
  }
}
