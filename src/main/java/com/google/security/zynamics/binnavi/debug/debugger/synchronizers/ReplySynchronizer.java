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
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for the individual message synchronizers. Whenever there is a new reply type
 * that could come from the debug client, a new subclass of this class should be generated that
 * synchronizes the data sent by the debug client with the simulated process information available
 * in com.google.security.zynamics.binnavi.
 *
 * @param <T> The type of the debugger reply handled by the synchronizer.
 */
public abstract class ReplySynchronizer<T extends DebuggerReply> {
  /**
   * The debugger that sends the messages to be synchronized.
   */
  private final IDebugger debugger;

  /**
   * The listeners that are notified about incoming messages.
   */
  private final ListenerProvider<IDebugEventListener> listeners;

  /**
   * Crates a new base synchronizer object.
   *
   * @param debugger The debugger that sends the messages to be synchronized.
   * @param listeners The listeners that are notified about incoming messages.
   */
  protected ReplySynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    this.debugger =
        Preconditions.checkNotNull(debugger, "IE01043: Debugger argument can not be null");
    this.listeners =
        Preconditions.checkNotNull(listeners, "IE01044: Listeners argument can not be null");
  }

  /**
   * Deactivates all regular breakpoints which are not disabled.
   */
  private void deactivateBreakpoints() {
    final BreakpointManager manager = debugger.getBreakpointManager();

    final Set<BreakpointAddress> addressesToRemove = new HashSet<>();
    final Set<BreakpointAddress> addressesToDisable = new HashSet<>();

    for (final Breakpoint breakpoint : manager.getBreakpoints(BreakpointType.REGULAR)) {
      final BreakpointAddress address = breakpoint.getAddress();

      if (manager.getBreakpointStatus(address, BreakpointType.REGULAR)
          == BreakpointStatus.BREAKPOINT_DELETING) {
        // When the target process is reset, the BreakpointDeleted message will never arrive.
        // It is therefore necessary to delete the DELETING breakpoints manually.
        // See Case 2109 for an example of what can happen.

        addressesToRemove.add(address);

      } else if (manager.getBreakpointStatus(address, BreakpointType.REGULAR)
          != BreakpointStatus.BREAKPOINT_DISABLED) {
        addressesToDisable.add(address);
      }
    }

    manager.removeBreakpoints(BreakpointType.REGULAR, addressesToRemove);
    manager.setBreakpointStatus(addressesToDisable, BreakpointType.REGULAR,
        BreakpointStatus.BREAKPOINT_INACTIVE);
  }

  /**
   * Returns the debugger that sends the messages to be synchronized.
   *
   * @return The debugger that sends the messages to be synchronized.
   */
  protected IDebugger getDebugger() {
    return debugger;
  }

  /**
   * Is overridden in classes which synchronize erroneous replies.
   *
   * @param reply The erroneous reply to synchronize.
   */
  protected void handleError(T reply) {}

  /**
   * Is overridden in classes which synchronize successful replies.
   *
   * @param reply The successful reply to synchronize.
   */
  protected void handleSuccess(T reply) {}

  /**
   * Notifies the listeners about an exception that occurred during message synchronization.
   *
   * @param debugException The exception that happened during message synchronization.
   */
  protected void issueDebugException(final DebugExceptionWrapper debugException) {
    Preconditions.checkNotNull(debugException, "IE01045: Debug exception argument can not be null");

    for (final IDebugEventListener listener : listeners) {
      try {
        listener.debugException(debugException);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Notifies all interested listeners that a reply arrived from the debug client.
   *
   * @param listener The listener to be notified.
   * @param reply The reply to be sent to the listener.
   */
  protected abstract void notifyListener(IDebugEventListener listener, T reply);

  /**
   * Sends a registers request to the debug client.
   */
  protected void refreshRegisters() {
    try {
      debugger.readRegisters();
    } catch (final DebugExceptionWrapper exception) {
      NaviLogger.severe("Error: Could not read registers: Exception %s", exception);

      for (final IDebugEventListener listener : listeners) {
        try {
          listener.debugException(exception);
        } catch (final Exception exception2) {
          CUtilityFunctions.logException(exception2);
        }
      }
    }
  }

  /**
   * Resets information about the target process.
   */
  protected void resetTargetProcess() {
    final BreakpointManager manager = debugger.getBreakpointManager();
    final ProcessManager processManager = debugger.getProcessManager();

    debugger.setTerminated();

    manager.clearBreakpointsPassive(BreakpointType.ECHO);
    manager.clearBreakpointsPassive(BreakpointType.STEP);

    deactivateBreakpoints();

    processManager.getMemory().clear();
    processManager.setMemoryMap(new MemoryMap(new ArrayList<MemorySection>()));

    final Collection<TargetProcessThread> threads = processManager.getThreads();

    for (final TargetProcessThread thread : threads) {
      processManager.removeThread(thread);
    }

    for (final MemoryModule module : processManager.getModules()) {
      processManager.removeModule(module);
    }

    processManager.setAttached(false);
    processManager.setActiveThread(null);
  }

  /**
   * Updates the thread the register data belongs to with the new values.
   *
   * @param registerValues The new register values.
   */
  protected void setRegisterValues(final RegisterValues registerValues) {
    Preconditions.checkNotNull(registerValues, "IE01046: Register values argument can not be null");

    final ProcessManager processManager = debugger.getProcessManager();

    for (final ThreadRegisters threadRegister : registerValues) {
      for (final TargetProcessThread thread : processManager.getThreads()) {
        if (thread.getThreadId() == threadRegister.getTid()) {
          // Update the thread with the new register values.

          thread.setRegisterValues(threadRegister.getRegisters());

          for (final RegisterValue registerValue : threadRegister.getRegisters()) {
            if (registerValue.isPc()) {
              thread.setCurrentAddress(
                  new RelocatedAddress(new CAddress(registerValue.getValue())));
            }
          }
        }
      }
    }
  }

  /**
   * Updates the hit state of breakpoints.
   *
   * @param address The current value of the program counter which is used to determine the state of
   *        breakpoints.
   */
  protected void updateHitBreakpoints(final BreakpointAddress address) {
    Preconditions.checkNotNull(address, "IE01048: Address argument can not be null");

    final BreakpointManager manager = debugger.getBreakpointManager();

    for (final Breakpoint breakpoint : manager.getBreakpoints(BreakpointType.REGULAR)) {
      final boolean isAddressSame = address.equals(breakpoint.getAddress());

      if ((manager.getBreakpointStatus(breakpoint.getAddress(), BreakpointType.REGULAR)
          == BreakpointStatus.BREAKPOINT_HIT) && !isAddressSame) {
        // Activate the previously hit breakpoint
        manager.setBreakpointStatus(Sets.newHashSet(breakpoint.getAddress()),
            BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ACTIVE);
        break;
      } else if (isAddressSame) {
        // Hit the currently hit breakpoint
        try {
          manager.setBreakpointStatus(Sets.newHashSet(breakpoint.getAddress()),
              BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_HIT);
        } catch (final IllegalArgumentException exception) {
          // Not one of our breakpoints.
          return;
        }
      }
    }
  }

  /**
   * Handles a reply. Depending on whether the reply is a successful reply or an erroneous reply,
   * handling the reply means different things. Either way, the information from the reply is
   * synchronized with the known process state and interested listeners are notified about the
   * reply.
   *
   * @param reply The reply to handle.
   */
  public void handle(final T reply) {
    Preconditions.checkNotNull(reply, "IE01220: Reply argument can not be null");

    if (reply.success()) {
      handleSuccess(reply);
    } else {
      handleError(reply);
    }

    for (final IDebugEventListener listener : listeners) {
      try {
        notifyListener(listener, reply);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
